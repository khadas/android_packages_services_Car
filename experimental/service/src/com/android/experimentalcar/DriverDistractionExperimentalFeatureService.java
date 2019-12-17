/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.experimentalcar;

import com.android.car.CarServiceBase;

import java.io.PrintWriter;

/**
 * Driver Distraction Service for using the driver's awareness, the required awareness of the
 * driving environment to expose APIs for the driver's current distraction level.
 */
public final class DriverDistractionExperimentalFeatureService implements CarServiceBase {

    @Override
    public void init() {
        // Nothing to do
    }

    @Override
    public void release() {
        // Nothing to do
    }

    @Override
    public void dump(PrintWriter writer) {
        writer.println("*DriverDistractionExperimentalFeatureService*");
    }
}