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

/**
 * MOVEMENT LOG for 8in1
 *
 * 2018-04-08 19:51:44.566 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [156 -> ffff :
 * 2ca][342-65535-cmd:714-deviceCode:319] [ SOURCE = sub:1 devId:86] [ TARGET = sub:-1 devId:-1] + DATA[ 2 2 2 1 1 1 0
 * 0]
 * 2018-04-08 19:51:45.750 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [156 -> ffff :
 * 2ca][342-65535-cmd:714-deviceCode:319] [ SOURCE = sub:1 devId:86] [ TARGET = sub:-1 devId:-1] + DATA[ 2 2 2 1 1 1 0
 * 0]
 *
 * 2018-04-08 19:51:52.640 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [156 -> ffff :
 * 2ca][342-65535-cmd:714-deviceCode:319] [ SOURCE = sub:1 devId:86] [ TARGET = sub:-1 devId:-1] + DATA[ 2 2 2 1 1 0 0
 * 0]
 * 2018-04-08 19:51:53.784 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [156 -> ffff :
 * 2ca][342-65535-cmd:714-deviceCode:319] [ SOURCE = sub:1 devId:86] [ TARGET = sub:-1 devId:-1] + DATA[ 2 2 2 1 1 0 0
 * 0]
 *
 * 2018-04-08 19:52:02.932 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [156 -> ffff :
 * 2ca][342-65535-cmd:714-deviceCode:319] [ SOURCE = sub:1 devId:86] [ TARGET = sub:-1 devId:-1] + DATA[ 2 2 2 1 1 1 0
 * 0]
 * 2018-04-08 19:52:04.096 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [156 -> ffff :
 * 2ca][342-65535-cmd:714-deviceCode:319] [ SOURCE = sub:1 devId:86] [ TARGET = sub:-1 devId:-1] + DATA[ 2 2 2 1 1 1 0
 * 0]
 *
 *
 * 2018-04-08 19:52:10.939 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [156 -> ffff :
 * 2ca][342-65535-cmd:714-deviceCode:319] [ SOURCE = sub:1 devId:86] [ TARGET = sub:-1 devId:-1] + DATA[ 2 2 2 1 1 0 0
 * 0]
 * 2018-04-08 19:52:12.088 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [156 -> ffff :
 * 2ca][342-65535-cmd:714-deviceCode:319] [ SOURCE = sub:1 devId:86] [ TARGET = sub:-1 devId:-1] + DATA[ 2 2 2 1 1 0 0
 * 0]
 *
 *
 *
 *
 * MOVEMENT LOG FOR PIR
 *
 * 2018-04-08 19:59:30.973 [DEBUG] [.binding.Smartg4control.internal.bus.Smartg4controlServer] - other [119 -> 115 :
 * 31][281-277-cmd:49-deviceCode:135] [ SOURCE = sub:1 devId:25] [ TARGET = sub:1 devId:21] + DATA[ 12 0 0 0]
 * ////// SO THIS DOES NOT REALLY WORK< SINCE IT IS NOT A BROADCAST MESSAGE
 *
 * @author tr
 *
 */

public class Smartg4controlSensor extends GenericSmartg4controlDevice {
    private final Logger logger = LoggerFactory.getLogger(Smartg4controlSensor.class);

    public static final int CMD_READ_TEMP = 0xDC00;
    public static final int CMD_READ_TEMP_RESPONSE = 0xDC01;
    public static final int CMD_READ_ALL = 0xDB00;
    public static final int CMD_READ_ALL_RESPONSE = 0xDB01;

    public static final int CMD_DDP_READ_TEMP = 0XE3E7;
    public static final int CMD_DDP_READ_TEMP_RESPONSE = 0XE3E8;
    public static final int DDP_CELSIUS = 1;
    public static final int DDP_FAHRENHEIT = 0;

    public static final int DEVICE_ID_8IN1 = 319;
    public static final int DEVICE_ID_9IN1 = 309;
    public static final int DEVICE_ID_6IN1 = 313;
    public static final int DEVICE_ID_5IN1 = 314;
    public static final int DEVICE_ID_PIR = 135;

    public static final int MAX_SENSORS = 9;
    public static final int SENSOR_TEMPERATURE = 1;

    public static final int CELSIUS = 0;
    public static final int FAHRENHEIT = 1;

    ScheduledFuture[] futures = new ScheduledFuture[MAX_SENSORS];
    Smartg4controlHandler handler;

    boolean autoupdate = true;

    public Smartg4controlSensor(int subnet, int deviceId, Smartg4controlServer server, Smartg4controlHandler handler) {
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
        // logger.debug("sensor: {}", p);
        byte[] data;
        switch (p.getCommand()) {

            case CMD_READ_TEMP_RESPONSE:
                data = p.getData();

                if (data.length < 2) {
                    return;
                }
                setFuture(SENSOR_TEMPERATURE, null);

                int temp = data[1];
                // logger.debug("TEMPERATURE response {} {} {}", data[0], data[1], temp);
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

            case CMD_READ_ALL_RESPONSE:
                logger.debug("ALL response: {}", p);

                break;

            default:
                break;
        }
    }

    public void readTemp() throws IOException {

        Smartg4controlPacket p = new Smartg4controlPacket();
        p.setTargetDevideID(getDeviceId());
        p.setTargetSubnetID(getSubnet());

        // logger.debug("read temp called {}:{} ", getSubnet(), getDeviceId());

        p.setCommand(CMD_READ_TEMP);

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
