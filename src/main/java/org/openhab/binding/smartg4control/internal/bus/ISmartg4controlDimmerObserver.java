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

import org.eclipse.jdt.annotation.NonNull;

/**
 *
 * @author TinusRautenbach - Initial contribution
 */
public interface ISmartg4controlDimmerObserver {
    public abstract void onDimmerStateChanged(@NonNull Smartg4controlDimmer dimmer, int state);
}
