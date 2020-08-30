/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 *
 * @author tr
 *
 */

public class Smartg4controlDDP extends GenericSmartg4controlDevice {
    private final Logger logger = LoggerFactory.getLogger(Smartg4controlDDP.class);

    public static final int CMD_DDP_READ_TEMP = 0XE3E7;
    public static final int CMD_DDP_READ_TEMP_RESPONSE = 0XE3E8;

    public static final int DDP_CELSIUS = 1;
    public static final int DDP_FAHRENHEIT = 0;

    public static final int DEVICE_ID_DDP = 149;

    public static final int MAX_SENSORS = 1;
    public static final int SENSOR_TEMPERATURE = 0;

    ScheduledFuture[] futures = new ScheduledFuture[MAX_SENSORS];
    Smartg4controlHandler handler;

    boolean autoupdate = true;

    public Smartg4controlDDP(int subnet, int deviceId, Smartg4controlServer server, Smartg4controlHandler handler) {
        this.subnet = subnet;
        this.deviceId = deviceId;

        this.addressasint = Smartg4controlPacket.ushort((byte) subnet, (byte) deviceId);
        this.server = server;
        this.handler = handler;
        this.address = this.subnet + ":" + this.deviceId;
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
        logger.debug("ddp: {}", p);
        byte[] data;
        switch (p.getCommand()) {

            case CMD_DDP_READ_TEMP_RESPONSE:
                data = p.getData();

                if (data.length < 2) {
                    return;
                }
                setFuture(SENSOR_TEMPERATURE, null);

                int temp = data[1];
                logger.debug("DDP TEMPERATURE response {} {} {}", data[0], data[1], temp);
                handler.handleUpdate(temp);

                synchronized (observers) {
                    if (observers != null) {
                        ListIterator<Object> i = observers.listIterator(0);
                        while (i.hasNext()) {
                            ISmartg4controlSensorObserver o = (ISmartg4controlSensorObserver) i.next();
                            o.onTemperatureStateChanged(this, data[1]);
                        }
                    }
                }
                break;

            default:
                break;
        }
    }

    public void readTemp() throws IOException {

        Smartg4controlPacket p = new Smartg4controlPacket();
        p.setTargetDevideID(getDeviceId());
        p.setTargetSubnetID(getSubnet());

        logger.debug("read DDP temp called {}:{} ", getSubnet(), getDeviceId());

        p.setCommand(CMD_DDP_READ_TEMP);
        p.setData(new byte[] { (byte) 1 }); // request CELSIUS

        ScheduledFuture f = server.sendPacketWithRetry(p);

        setFuture(SENSOR_TEMPERATURE, f);
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
            readTemp();
        } catch (IOException e) {
            logger.debug("error updating sensor", e);
        }
    }
}
