/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartg4control.internal;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartg4control.Smartg4controlBindingConstants;
import org.openhab.binding.smartg4control.handler.Smartg4controlHandler;
import org.openhab.binding.smartg4control.internal.bus.Smartg4controlServer;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link Smartg4controlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author TinusRautenbach - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "sg4")

public class Smartg4controlHandlerFactory extends BaseThingHandlerFactory {
    Smartg4controlServer hs;
    private Smartg4controlBindingProperties bindingProperties;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(Smartg4controlBindingConstants.THING_TYPE_G4_DIMMER);
        SUPPORTED_THING_TYPES_UIDS.add(Smartg4controlBindingConstants.THING_TYPE_SMARTBUS_G4_RELAY);
        SUPPORTED_THING_TYPES_UIDS.add(Smartg4controlBindingConstants.THING_TYPE_G4_SENSOR);
        SUPPORTED_THING_TYPES_UIDS.add(Smartg4controlBindingConstants.THING_TYPE_G4_DDP);
        SUPPORTED_THING_TYPES_UIDS.add(Smartg4controlBindingConstants.THING_TYPE_G4_LOGIC);

    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new Smartg4controlHandler(thing, hs);
        }

        return null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        Dictionary<String, Object> properties = componentContext.getProperties();
        bindingProperties = new Smartg4controlBindingProperties(properties);

        hs = Smartg4controlServer.getInstance(bindingProperties.getListenAddress(),
                bindingProperties.getGateWayAddress());
    };

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        hs.stop();
        hs = null;
    };
}
