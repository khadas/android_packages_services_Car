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
package android.automotive.computepipe.runner;

import android.automotive.computepipe.runner.PipeInputConfigInputType;
import android.automotive.computepipe.runner.PipeInputConfigFormatType;
import android.automotive.computepipe.runner.PipeInputConfigInputOptions;

/**
 * Transaction data types
 *
 *
 * Input config descriptor
 *
 * Structure that describes the input sources
 *
 * This is provided by the AIDL implementation to the client
 */
@VintfStability
parcelable PipeInputConfig {
    /**
     * input option.
     */
    PipeInputConfigInputOptions options;
    /**
     * ID for the option.
     */
    int configId;
}
