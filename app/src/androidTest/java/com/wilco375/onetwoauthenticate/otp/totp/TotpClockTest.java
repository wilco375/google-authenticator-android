/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 * Modified Copyright 2018 Wilco van Beijnum.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wilco375.onetwoauthenticate.otp.totp;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import com.wilco375.onetwoauthenticate.otp.totp.TotpClock;
import com.wilco375.onetwoauthenticate.util.Utilities;
import com.wilco375.onetwoauthenticate.testability.DependencyInjector;

/**
 * Unit tests for {@link TotpClock}.
 *
 * @author klyubin@google.com (Alex Klyubin)
 */
public class TotpClockTest extends AndroidTestCase {

    private TotpClock mClock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DependencyInjector.resetForIntegrationTesting(getContext());
        mClock = new TotpClock(DependencyInjector.getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        DependencyInjector.close();
        super.tearDown();
    }

    public void testCurrentTimeMillisUsesCurrentTimeAndTimeCorrection() {
        assertEquals(0, mClock.getTimeCorrectionMinutes());

        long millisBefore = System.currentTimeMillis();
        long actualMillis = mClock.currentTimeMillis();
        long millisAfter = System.currentTimeMillis();
        assertInRangeInclusive(actualMillis, millisBefore, millisAfter);

        mClock.setTimeCorrectionMinutes(137);
        millisBefore = System.currentTimeMillis();
        actualMillis = mClock.currentTimeMillis();
        millisAfter = System.currentTimeMillis();
        assertInRangeInclusive(
                actualMillis,
                millisBefore + 137 * Utilities.MINUTE_IN_MILLIS,
                millisAfter + 137 * Utilities.MINUTE_IN_MILLIS);
    }

    public void testTimeCorrectionBackedByPreferences() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(DependencyInjector.getContext());
        assertTrue(preferences.edit().putInt(TotpClock.PREFERENCE_KEY_OFFSET_MINUTES, 7).commit());
        assertEquals(7, mClock.getTimeCorrectionMinutes());
        mClock.setTimeCorrectionMinutes(42);
        assertEquals(42, preferences.getInt(TotpClock.PREFERENCE_KEY_OFFSET_MINUTES, 0));
        assertEquals(42, mClock.getTimeCorrectionMinutes());
    }

    private static void assertInRangeInclusive(
            long actual, long expectedMinValue, long expectedMaxValue) {
        if ((actual < expectedMinValue) || (actual > expectedMaxValue)) {
            fail(actual + " not in [" + expectedMinValue + ", " + expectedMaxValue + "]");
        }
    }
}
