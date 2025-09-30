/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smartg4control.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartg4control.Smartg4controlBindingConstants;
import org.openhab.binding.smartg4control.internal.Smartg4controlConfiguration;
import org.openhab.binding.smartg4control.internal.bus.ISmartg4controlDimmerObserver;
import org.openhab.binding.smartg4control.internal.bus.Smartg4controlDDP;
import org.openhab.binding.smartg4control.internal.bus.Smartg4controlDimmer;
import org.openhab.binding.smartg4control.internal.bus.Smartg4controlLogic;
import org.openhab.binding.smartg4control.internal.bus.Smartg4controlSensor;
import org.openhab.binding.smartg4control.internal.bus.Smartg4controlServer;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Smartg4controlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author TinusRautenbach - Initial contribution
 */
@NonNullByDefault
public class Smartg4controlHandler extends BaseThingHandler implements ISmartg4controlDimmerObserver {

    private final Logger logger = LoggerFactory.getLogger(Smartg4controlHandler.class);

    Smartg4controlServer hs;

    int configSubnetID;
    int configDeviceID;
    int configChannelID;

    @Nullable
    private Smartg4controlConfiguration config;

    public Smartg4controlHandler(Thing thing, Smartg4controlServer hs2) {
        super(thing);
        hs = hs2;
    }

    String getAddressFromThisThing() {
        return configSubnetID + ":" + configDeviceID + "-" + configChannelID;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({},{})", channelUID, command);

        if (channelUID.getId().equals(Smartg4controlBindingConstants.CHANNEL_switchstatus)) {

            Smartg4controlDimmer hdim = (Smartg4controlDimmer) hs.getDevice(getAddressFromThisThing());
            if (command.equals(OnOffType.ON)) {

                try {
                    hdim.switchOn();
                } catch (NumberFormatException | IOException e) {

                    logger.error("cant switch on in command:", e);
                }

            } else if (command.equals(OnOffType.OFF)) {
                try {
                    hdim.switchOff();
                } catch (NumberFormatException | IOException e) {

                    logger.error("cant switch off in command:", e);
                }
            }
        } else if (channelUID.getId().equals(Smartg4controlBindingConstants.CHANNEL_lightlevel)) {
            Smartg4controlDimmer hdim = (Smartg4controlDimmer) hs.getDevice(getAddressFromThisThing());
            if (command instanceof RefreshType) {
                logger.debug("refreshtype {}", command);
            } else if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {

                    try {
                        hdim.switchOn();
                    } catch (Exception e) {
                        logger.error("channelUID:{}", channelUID.getId());
                        logger.error("cant send switchon", e);
                    }

                } else if (command.equals(OnOffType.OFF)) {
                    try {
                        hdim.switchOff();
                    } catch (Exception e) {
                        logger.error("channelUID:{}", channelUID.getId());

                        logger.error("cant switch off in command: ", e);
                    }
                }
            } else {
                int level = ((Number) command).intValue();
                try {
                    hdim.dim(level);
                } catch (Exception e) {
                    logger.error("channelUID:{}", channelUID.getId());
                    logger.error("cant dim channel:", e);
                }
            }

        }

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {

        super.channelLinked(channelUID);
        logger.debug("------------------------------channelLinked: {}", channelUID);
    }

    @Override
    public void dispose() {

        super.dispose();
        hs.stop();
    }

    @Override
    public void handleRemoval() {

        super.handleRemoval();
        hs.stop();
    }

    @Override
    public void initialize() {
        Configuration cfg = thing.getConfiguration();
        configSubnetID = ((BigDecimal) cfg.get("subnetid")).intValue();
        configDeviceID = ((BigDecimal) cfg.get("deviceid")).intValue();
        logger.debug("initialise {} {} {} ", configSubnetID, configDeviceID, thing.getThingTypeUID());

        if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_G4_DIMMER)
                || thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_SMARTBUS_G4_RELAY)) {
            if (cfg.get("channel") != null) {
                configChannelID = ((BigDecimal) cfg.get("channel")).intValue();
            }
            int configLowerLimit = 0;
            if (cfg.get("lower") != null) {
                configLowerLimit = ((BigDecimal) cfg.get("lower")).intValue();
            }
            int configUpperLimit = 100;
            if (cfg.get("upper") != null) {
                configUpperLimit = ((BigDecimal) cfg.get("upper")).intValue();
            }
            logger.debug("config {}", thing.getConfiguration());
            Smartg4controlDimmer hdim = new Smartg4controlDimmer(configSubnetID, configDeviceID, configChannelID,
                    configLowerLimit, configUpperLimit, hs, this);
            hs.addDevice(hdim);
            try {
                hdim.readStatus();
            } catch (IOException e) {
                logger.error("cant send read status message", e);
            }
            logger.debug("added device: {}", hdim.getDeviceId());
        } else if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_G4_SENSOR)) {
            Smartg4controlSensor hds = new Smartg4controlSensor(configSubnetID, configDeviceID, hs, this);
            hs.addDevice(hds);
            try {
                hds.readTemp();
            } catch (IOException e) {
                logger.error("cant send read status message", e);
            }
            logger.debug("added sensor device: {}", hds.getDeviceId());
        } else if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_G4_DDP)) {
            logger.debug("added DDP device: {}", configDeviceID);
            Smartg4controlDDP ddp = new Smartg4controlDDP(configSubnetID, configDeviceID, hs, this);
            hs.addDevice(ddp);
        } else if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_G4_LOGIC)) {
            logger.debug("added logic device: {}", configDeviceID);
            Smartg4controlLogic logic = new Smartg4controlLogic(configSubnetID, configDeviceID, hs, this);
            hs.addDevice(logic);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    public void handleDateTimeUpdate(ZonedDateTime dt) {
        if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_G4_LOGIC)) {

            try {

                final Channel channel = thing.getChannel(Smartg4controlBindingConstants.CHANNEL_datetime);
                logger.debug("updateState time {} {} {}", thing.getUID(), dt.toString(), channel);

                DateTimeType dtt = new DateTimeType(dt);

                updateState(channel.getUID(), dtt);

            } catch (Exception e) {
                logger.debug("pdateState time error ", e);
            }
        }
    }

    public void handleUpdate(int level) {
        // TODO Auto-generated method stub

        // logger.debug("thing {}", thing.getUID());

        for (Channel item : thing.getChannels()) {

            logger.debug("channels {}", item.getUID());

        }

        if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_G4_DIMMER)) {

            updateState(Smartg4controlBindingConstants.CHANNEL_lightlevel, new PercentType(level));
        } else if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_SMARTBUS_G4_RELAY)) {
            if (level != 0) {
                updateState(Smartg4controlBindingConstants.CHANNEL_switchstatus, OnOffType.ON);
            } else {
                updateState(Smartg4controlBindingConstants.CHANNEL_switchstatus, OnOffType.OFF);
            }

        } else if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_G4_SENSOR)) {

            try {

                BigDecimal f_temperature = new BigDecimal(level);

                final Channel channel = thing.getChannel(Smartg4controlBindingConstants.CHANNEL_temperature);
                // logger.debug("updateState temp update {} {} {}", thing.getUID(), f_temperature, channel);

                updateState(channel.getUID(), new DecimalType(f_temperature));

            } catch (Exception e) {
                logger.debug("update sensor error", e);
            }
        } else if (thing.getThingTypeUID().equals(Smartg4controlBindingConstants.THING_TYPE_G4_DDP)) {

            try {

                BigDecimal f_temperature = new BigDecimal(level);

                final Channel channel = thing.getChannel(Smartg4controlBindingConstants.CHANNEL_temperature);
                logger.debug("updateState DDP temp update {} {} {}", thing.getUID(), f_temperature, channel);

                updateState(channel.getUID(), new DecimalType(f_temperature));

            } catch (Exception e) {
                logger.debug("update DDP temp error ", e);
            }
        }
    }

    @Override
    public void onDimmerStateChanged(Smartg4controlDimmer dim, int state) {
        handleUpdate(state);
    }
}
