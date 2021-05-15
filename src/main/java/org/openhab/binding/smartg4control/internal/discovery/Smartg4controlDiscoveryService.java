/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartg4control.internal.discovery;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.smartg4control.Smartg4controlBindingConstants;
import org.openhab.binding.smartg4control.internal.bus.Smartg4controlServer;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service to scan for AllPlay devices.
 *
 * @author Dominic Lerbs - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.smartg4control")
public class Smartg4controlDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(Smartg4controlDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT = 30;
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .singleton(Smartg4controlBindingConstants.THING_TYPE_G4_DIMMER);
    private Smartg4controlServer hs;

    public Smartg4controlDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIMEOUT);
    }

    @Override
    protected void startScan() {
    }

    @Override
    protected void stopScan() {
        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Starting background scan for Smartg4control devices");
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.trace("Stopping background scan for Smartg4control devices");
        stopScan();
    }

    @Override
    public void deactivate() {
        removeOlderResults(getTimestampOfLastScan());
    }
}
