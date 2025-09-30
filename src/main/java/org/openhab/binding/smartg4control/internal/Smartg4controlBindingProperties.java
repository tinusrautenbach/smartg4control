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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AllPlay binding properties.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class Smartg4controlBindingProperties {

    private final Logger logger = LoggerFactory.getLogger(Smartg4controlBindingProperties.class);

    private final int gatewayPort;
    private final String gateWayAddress;
    private String listenAddress;
    private final int listenPort;
    private final int sensor_refresh;

    public int getSensor_refresh() {
        return sensor_refresh;
    }

    private static final String PROP_GATEWAY_PORT = "gateWayPort";
    private static final int GATEWAY_PORT_DEFAULT_VALUE = 6000;
    private static final String PROP_GATEWAY_ADDRESS = "gateWayAddress";
    private static final int LISTEN_PORT_DEFAULT_VALUE = 6000;
    private static final String PROP_LISTEN_ADDRESS = "listenAddress";
    private static final String LISTEN_ADDRESS_DEFAULT_VALUE = "192.168.1.111";
    private static final String GATEWAY_ADDRESS_DEFAULT_VALUE = "255.255.255.255";
    private static final String PROP_LISTEN_PORT = "listenPort";
    private static final String PROP_SENSOR_REFRESH = "sensor_refresh";
    private static final int PROP_SENSOR_REFRESH_DEFAULT_VALUE = 300;

    public Smartg4controlBindingProperties(Dictionary<String, Object> properties) {

        gatewayPort = getIntegerProperty(properties, PROP_GATEWAY_PORT, GATEWAY_PORT_DEFAULT_VALUE);
        listenPort = getIntegerProperty(properties, PROP_LISTEN_PORT, LISTEN_PORT_DEFAULT_VALUE);
        sensor_refresh = getIntegerProperty(properties, PROP_SENSOR_REFRESH, PROP_SENSOR_REFRESH_DEFAULT_VALUE);
        gateWayAddress = getStringProperty(properties, PROP_GATEWAY_ADDRESS, GATEWAY_ADDRESS_DEFAULT_VALUE);

        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            listenAddress = socket.getLocalAddress().getHostAddress();
            logger.info("listenAddress from network socket {}", listenAddress);
        }
        // listenAddress = getStringProperty(properties, PROP_LISTEN_ADDRESS, LISTEN_ADDRESS_DEFAULT_VALUE);
        catch (SocketException | UnknownHostException e) {
            // TODO Auto-generated catch block
            logger.error("cant get listenaddress", e);
        }

        if (listenAddress == null) {
            listenAddress = getStringProperty(properties, PROP_LISTEN_ADDRESS, LISTEN_ADDRESS_DEFAULT_VALUE);
        }
    }

    private int getIntegerProperty(Dictionary<String, Object> properties, String propertyKey, int defaultValue) {
        Object configValue = properties.get(propertyKey);
        int value = defaultValue;
        if (configValue instanceof String) {
            try {
                value = Integer.parseInt((String) configValue);
            } catch (NumberFormatException e) {
                logger.warn("Unable to convert value {} for config property {} to integer. Using default value.",
                        configValue, propertyKey);
            }
        } else if (configValue instanceof Integer) {
            value = (int) configValue;
        }
        return value;
    }

    public int getGatewayPort() {
        return gatewayPort;
    }

    public String getGateWayAddress() {
        return gateWayAddress;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public int getListenPort() {
        return listenPort;
    }

    private String getStringProperty(Dictionary<String, Object> properties, String propertyKey, String defaultValue) {
        String value = (String) properties.get(propertyKey);
        return value != null ? value : defaultValue;
    }
}
