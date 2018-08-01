/*
 * Copyright 2018 Johns Hopkins University
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
package org.dataconservancy.pass.model;

import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DepositStatusTest {

    /**
     * Insure no typos used in DepositStatus value and URI.  If this test fails and there are no typos, adjust this
     * test.
     */
    @Test
    public void statusValueAndUriSanityCheck() {
        Stream.of(Deposit.DepositStatus.values()).forEach(depositStatus -> {
            assertTrue(depositStatus.asUri().toString().endsWith(depositStatus.getValue()));
        });
    }

    /**
     * Insures the DepositStatus can be looked up by URI and by string value.
     */
    @Test
    public void lookupByValueAndUri() {
        Stream.of(Deposit.DepositStatus.values()).forEach(depositStatus -> {
            assertEquals(depositStatus, Deposit.DepositStatus.of(depositStatus.getValue()));
            assertEquals(depositStatus, Deposit.DepositStatus.of(depositStatus.asUri().toString()));
        });
    }
}