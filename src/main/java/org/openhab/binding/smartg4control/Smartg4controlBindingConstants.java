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
package org.openhab.binding.smartg4control;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Smartg4controlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author TinusRautenbach - Initial contribution
 */
@NonNullByDefault
public class Smartg4controlBindingConstants {

    private static final String BINDING_ID = "sg4";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SMARTBUS_G4_RELAY = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_G4_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_G4_SENSOR = new ThingTypeUID(BINDING_ID, "sensor");
    public static final ThingTypeUID THING_TYPE_G4_DDP = new ThingTypeUID(BINDING_ID, "ddp");
    public static final ThingTypeUID THING_TYPE_G4_LOGIC = new ThingTypeUID(BINDING_ID, "logic");

    // List of all Channel ids
    public static final String CHANNEL_lightlevel = "lightlevel";
    public static final String CHANNEL_switchstatus = "switchstatus";
    public static final String CHANNEL_temperature = "temperature";
    public static final String CHANNEL_motion = "motion";
    public static final String CHANNEL_datetime = "datetime";
}
