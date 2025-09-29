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
package org.openhab.binding.smartg4control.internal;

/**
 * The {@link Smartg4controlConfiguration} class contains fields mapping thing configuration paramters.
 *
 * @author TinusRautenbach - Initial contribution
 */
public class Smartg4controlConfiguration {
    /**
     * Configuration parameters for the binding, matching those in binding.xml and thing-types.xml
     */
    public String listenAddress = "192.168.1.238";
    public String gateWayAddress = "255.255.255.255";
    public int gateWayPort = 6000;
    public int listenPort = 6000;
    public int sensor_refresh = 300;
}
