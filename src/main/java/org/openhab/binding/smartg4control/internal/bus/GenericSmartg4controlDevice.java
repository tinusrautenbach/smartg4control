/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartg4control.internal.bus;

import java.util.LinkedList;

/**
 *
 * @author TinusRautenbach - Initial contribution
 */

public abstract class GenericSmartg4controlDevice implements ISmartg4controlDevice {

    public static final int DEVICE_ID_RSIP = 1201;
    public static final int DEVICE_ID_PIR = 000000000;

    public static final int CMD_READ_CHANNEL_REMARK = 0xF00E;
    public static final int CMD_READ_DEVICE_REMARK = 0x000E;

    public static final int CMD_MAC_RESPONSE = 0xF004;
    public static final int CMD_MAC_READ = 0xF003;
    public static final int CMD_DETECT_ADDRESS = 0xE5F5;
    public static final int CMD_DETECT_ADDRESS_RESPONSE = 0xE5F6;
    public static final int CMD_DETECT_SEARCH_ONLINE = 0x000E;
    public static final int CMD_DETECT_SEARCH_ONLINE_RESPONSE = 0x000F;

    protected String address;
    protected int addressasint;
    protected int subnet;
    protected int deviceId;

    protected LinkedList<Object> observers = new LinkedList<Object>();
    protected Smartg4controlServer server;

    @Override
    public String getAddress() {
        return address;
    }

    public void addListener(Object listener) {
        synchronized (observers) {
            observers.add(listener);
        }
    }

    public void removeListener(Object listener) {
        synchronized (observers) {
            observers.remove(listener);
        }
    }
}
