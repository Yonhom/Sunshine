/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.xuyonghong.sunshine;

import com.xuyonghong.sunshine.data.TestDb;
import com.xuyonghong.sunshine.data.TestPractice;
import com.xuyonghong.sunshine.data.TestProvider;
import com.xuyonghong.sunshine.data.TestUriMatcher;
import com.xuyonghong.sunshine.data.TestUtilities;
import com.xuyonghong.sunshine.data.TestWeatherContract;

import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//JUnit Suite Test
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestDb.class, TestPractice.class, TestUtilities.class,
        TestProvider.class, TestUriMatcher.class, TestWeatherContract.class,
        TestFetchWeatherTask.class
})

public class FullTestSuite extends TestSuite {
//    public static Test suite() {
//        // TestSuiteBuilder is not supported with AndroidJUnitRunner
//        return new TestSuiteBuilder(FullTestSuite.class)
//                .includeAllPackagesUnderHere().build();
//    }
//
//    public FullTestSuite() {
//        super();
//    }
}
