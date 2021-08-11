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
package org.openhab.binding.smartg4control.internal.bus;

import java.io.IOException;
import java.util.ListIterator;
import java.util.concurrent.ScheduledFuture;

import org.openhab.binding.smartg4control.handler.Smartg4controlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author TinusRautenbach - Initial contribution
 */
public class Smartg4controlDimmer extends GenericSmartg4controlDevice {
    private final Logger logger = LoggerFactory.getLogger(Smartg4controlDimmer.class);
    public static final int MAX_CHANNELS = 16;
    public static final int CMD_DIMMER_SET_STATE = 0x0031;
    public static final int CMD_DIMMER_SET_STATE_RESPONSE = 0x0032;
    public static final int CMD_DIMMER_READ_STATUS_OF_CHANNELS = 0x0033;
    public static final int CMD_DIMMER_RESPONSE_STATUS_OF_CHANNELS = 0x0034;
    public static final int CMD_DIMMER_STATE = 0xefff;
    public static final int CMD_FORWARDLY_REPORT_DIMMER_STATUS = 0xEFFF;

    public static final int DEVICE_ID_DIMMER_8 = 550;
    public static final int DEVICE_ID_RELAY_12 = 440;
    public static final int DEVICE_ID_RELAY_6 = 426;
    public static final int DEVICE_ID_RELAY_8 = 428;

    int channel;
    ScheduledFuture[] futures = new ScheduledFuture[MAX_CHANNELS];
    Smartg4controlHandler handler;
    int lowerLimit;
    int upperLimit;

    public Smartg4controlDimmer(int subnet, int deviceId, int channelid, int configLowerLimit, int configUpperLimit,
            Smartg4controlServer server, Smartg4controlHandler handler) {
        this.subnet = subnet;
        this.deviceId = deviceId;
        this.channel = channelid;
        this.lowerLimit = configLowerLimit;
        this.upperLimit = configUpperLimit;
        this.addressasint = Smartg4controlPacket.ushort((byte) subnet, (byte) deviceId);
        this.server = server;
        this.handler = handler;
        this.address = this.subnet + ":" + this.deviceId + "-" + this.channel;
    }

    private void setFuture(int channel, ScheduledFuture future) {
        synchronized (futures) {
            if (futures[channel] != null) {
                futures[channel].cancel(false);
            }
            futures[channel] = future;
        }
    }

    @Override
    public void processPacket(Smartg4controlPacket p) {
        logger.debug("processPacket {}", p);
        byte[] data;
        switch (p.getCommand()) {

            case CMD_DIMMER_SET_STATE_RESPONSE:
                data = p.getData();

                if (data.length < 3 || data[1] != (byte) 0xF8) {
                    return;
                }

                if (data[0] > 0 && data[0] < MAX_CHANNELS) {
                    setFuture(data[0], null);
                }
                logger.debug("dim response {} {}", data[0], data[2]);
                handler.handleUpdate(data[2]);
                synchronized (observers) {
                    if (observers != null) {
                        ListIterator<Object> i = observers.listIterator(0);
                        while (i.hasNext()) {
                            ISmartg4controlDimmerObserver o = (ISmartg4controlDimmerObserver) i.next();
                            o.onDimmerStateChanged(this, data[2]);
                        }
                    }
                }
                break;

            case CMD_DIMMER_RESPONSE_STATUS_OF_CHANNELS:
                data = p.getData();
                int level = data[channel];
                logger.debug("response Status update response {} ++> {}", channel, level);
                handler.handleUpdate(level);

                break;
            case CMD_DIMMER_STATE:
                break;

            default:
                break;
        }
    }

    public void dim(int value) throws IOException {
        if (channel >= MAX_CHANNELS) {
            return;
        }

        /*
         * if (value > upperLimit) {
         * value = upperLimit;
         * }
         * if (value < lowerLimit) {
         * value = lowerLimit;
         * }
         */

        Smartg4controlPacket p = new Smartg4controlPacket();
        p.setTargetDevideID(getDeviceId());
        p.setTargetSubnetID(getSubnet());

        logger.debug("dimchannel called {}:{}:{} value {}", getSubnet(), getDeviceId(), channel, value);

        p.setCommand(CMD_DIMMER_SET_STATE);
        p.setData(new byte[] { (byte) channel, (byte) value, 0, 0 });

        ScheduledFuture f = server.sendPacketWithRetry(p);

        setFuture(channel, f);
    }

    public void readStatus() throws IOException {

        Smartg4controlPacket p = new Smartg4controlPacket();
        p.setTargetDevideID(getDeviceId());
        p.setTargetSubnetID(getSubnet());

        logger.debug("dimchannel called {}:{}:{} ", getSubnet(), getDeviceId(), channel);

        p.setCommand(CMD_DIMMER_READ_STATUS_OF_CHANNELS);
        // p.setData(new byte[] { (byte) channel, (byte) value, 0, 0 });

        ScheduledFuture f = server.sendPacketWithRetry(p);

        setFuture(channel, f);
    }

    public void switchOn() throws IOException {
        dim(upperLimit);
    }

    public void switchOff() throws IOException {
        dim(lowerLimit);
    }

    @Override
    public int getDeviceId() {
        // TODO Auto-generated method stub
        return deviceId;
    }

    @Override
    public int getSubnet() {
        // TODO Auto-generated method stub
        return subnet;
    }

    @Override
    public void updateSensor() {
        try {
            readStatus();
        } catch (IOException e) {
            logger.error("readstatus error: ", e);
        }
    }
}
