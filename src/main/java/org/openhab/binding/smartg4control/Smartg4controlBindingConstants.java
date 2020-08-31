/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartg4control;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
