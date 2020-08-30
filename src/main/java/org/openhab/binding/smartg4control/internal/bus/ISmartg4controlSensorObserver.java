/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartg4control.internal.bus;

import org.eclipse.jdt.annotation.NonNull;

/**
 *
 * @author TinusRautenbach - Initial contribution
 */
public interface ISmartg4controlSensorObserver {
    public abstract void onTemperatureStateChanged(@NonNull GenericSmartg4controlDevice sensor, int state);
}
