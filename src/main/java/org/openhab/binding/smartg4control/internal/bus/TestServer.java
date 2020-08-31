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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author TinusRautenbach - Initial contribution
 */
public class TestServer {
    private final Logger logger = LoggerFactory.getLogger(TestServer.class);
    Smartg4controlServer hs = new Smartg4controlServer();

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        TestServer ts = new TestServer();
        ts.startTest();

    }

    public static final int CMD_DIMMER_SET_STATE = 0x0031;
    public static final int CMD_READ_MAC_ADDRESS = 0xF003;

    public void startTest() {
        try {

            hs.start("192.168.1.220", "255.255.255.255");
            // hs.start("fd7c:7d3d:9d06:a700:fc59:7062:2ed5:8a99", "255.255.255.255");

            Smartg4controlPacket p = new Smartg4controlPacket();
            p.setTargetDevideID(255);
            p.setTargetSubnetID(1);

            p.setCommand(GenericSmartg4controlDevice.CMD_DETECT_SEARCH_ONLINE);
            int channel = 4;
            int value = 100;

            // p.setData(new byte[] { (byte) channel, (byte) value, 0, 0 });
            hs.sendPacket(p);

            /**
             * for (int i = 0; i < 127; i++) {
             *
             * p.setTargetDevideID(i);
             * hs.sendPacket(p);
             * }
             **/
            p.setTargetDevideID(40);
            p.setCommand(Smartg4controlDimmer.CMD_DIMMER_SET_STATE);
            value = 0;
            p.setData(new byte[] { (byte) channel, (byte) value, 0, 0 });

            hs.sendPacket(p);
            value = 100;
            p.setData(new byte[] { (byte) channel, (byte) value, 0, 0 });

            hs.sendPacket(p);
            value = 0;
            p.setData(new byte[] { (byte) channel, (byte) value, 0, 0 });

            hs.sendPacket(p);
            value = 100;
            p.setData(new byte[] { (byte) channel, (byte) value, 0, 0 });

            hs.sendPacket(p);
            value = 0;

            p.setData(new byte[] { (byte) channel, (byte) value, 0, 0 });
            hs.sendPacket(p);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.debug("ERROR:", e);
            System.out.println(e.toString());
        }
        hs.stop();

    }

}
