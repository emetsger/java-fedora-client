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

import java.net.URI;

/**
 * Constants used throughout the PASS Java client.  Used to align the 
 * <a href="https://github.com/OA-PASS/pass-data-model/blob/master/README.md">model documentation</a> with
 * implementation.
 * 
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
class Constants {
    
    private Constants() {
        // prevent instantiation
    }

    /**
     * The PASS URI.
     */
    static final String OAPASS_BASE_URI = "http://oapass.org/";

    /**
     * Base URI used when identifying concepts relating to PASS.
     */
    static final String OAPASS_NS_URI = OAPASS_BASE_URI + "ns/";

    /**
     * Codifies Deposit statuses as documented <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">here</a>
     */
    static final class DepositStatus {

        private DepositStatus() {
            // prevent instantiation
        }
        
        /**
         * String representation of a <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> status in the {@code submitted} state.
         */
        static final String SUBMITTED_STATUS = "submitted";

        /**
         * String representation of a <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> status in the {@code rejected} state.
         */
        static final String REJECTED_STATUS = "rejected";

        /**
         * String representation of a <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> status in the {@code failed} state.
         */
        static final String FAILED_STATUS = "failed";

        /**
         * String representation of a <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> status in the {@code accepted} state.
         */
        static final String ACCEPTED_STATUS = "accepted";

        /**
         * The base URI used to represent the status of <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> resources.
         */
        static final URI STATUS_BASE_URI = URI.create(Constants.OAPASS_NS_URI + "status/deposit#");

        /**
         * URI representation of a <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> status in the {@code submitted} state.
         */
        static final URI SUBMITTED_URI = URI.create(STATUS_BASE_URI + SUBMITTED_STATUS);

        /**
         * URI representation of a <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> status in the {@code rejected} state.
         */
        static final URI REJECTED_URI = URI.create(STATUS_BASE_URI + REJECTED_STATUS);

        /**
         * URI representation of a <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> status in the {@code failed} state.
         */
        static final URI FAILED_URI = URI.create(STATUS_BASE_URI + FAILED_STATUS);

        /**
         * URI representation of a <a href="https://github.com/OA-PASS/pass-data-model/blob/master/documentation/Deposit.md">Deposit</a> status in the {@code accepted} state.
         */
        static final URI ACCEPTED_URI = URI.create(STATUS_BASE_URI + ACCEPTED_STATUS);

    }

}
