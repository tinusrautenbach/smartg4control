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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
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

public class Smartg4controlLogic extends GenericSmartg4controlDevice {
    private final Logger logger = LoggerFactory.getLogger(Smartg4controlLogic.class);

    public static final int CMD_DATE_TIME_RESPONSE = 0XDA44;
    public static final int CMD_DATE_TIME_SET = 0XDA02;
    public static final int CMD_DATE_TIME_SET_RESPONSE = 0XDA03;

    public static final int DEVICE_ID_LOGIC = 1108;

    public static final int MAX_SENSORS = 1;
    public static final int CHANNEL_DATETIME = 0;
    ScheduledFuture[] futures = new ScheduledFuture[MAX_SENSORS];
    Smartg4controlHandler handler;

    boolean autoupdate = true;

    public Smartg4controlLogic(int subnet, int deviceId, Smartg4controlServer server, Smartg4controlHandler handler) {
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
        logger.info("logic: {}", p);
        byte[] data;
        switch (p.getCommand()) {

            case CMD_DATE_TIME_RESPONSE:
                data = p.getData();

                if (data.length < 6) {
                    return;
                }
                setFuture(CHANNEL_DATETIME, null);
                int yr = data[0] + 2000;
                int month = data[1] - 1;
                int day = data[2];
                int hour = data[3];
                int min = data[4];
                int seconds = data[5];
                int nano = 0;
                ZonedDateTime zt = ZonedDateTime.of(yr, month, day, hour, min, seconds, nano, ZoneId.systemDefault());

                logger.debug("logic datetime {}  ", zt.toString());
                handler.handleDateTimeUpdate(zt);

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

    public void updateDateTime() throws IOException {

        Smartg4controlPacket p = new Smartg4controlPacket();
        p.setTargetDevideID(getDeviceId());
        p.setTargetSubnetID(getSubnet());

        p.setCommand(CMD_DATE_TIME_SET);
        Calendar c = Calendar.getInstance();

        int yr = c.get(Calendar.YEAR) - 2000;
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);

        // [da02][510-270-cmd:da02-deviceCode:65534] [ SOURCE = sub:1 devId:254] [ TARGET = sub:1 devId:14] + DATA[ 19 4
        // 13 11 19 58 6]
        // [da03][270-510-cmd:da03-deviceCode:1108] [ SOURCE = sub:1 devId:14] [ TARGET = sub:1 devId:254] + DATA[ -8]

        p.setData(new byte[] { (byte) yr, (byte) month, (byte) day, (byte) hour, (byte) min, (byte) sec });

        logger.debug("updateDateTime called {}:{}:{} ", getSubnet(), getDeviceId(), p.getData());

        ScheduledFuture f = server.sendPacketWithRetry(p);

        setFuture(0, f);
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
            updateDateTime();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("updatesensor", e);
        }
    }
}
