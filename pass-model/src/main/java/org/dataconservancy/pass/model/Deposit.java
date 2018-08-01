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

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.dataconservancy.pass.model.Constants.DepositStatus.ACCEPTED_STATUS;
import static org.dataconservancy.pass.model.Constants.DepositStatus.ACCEPTED_URI;
import static org.dataconservancy.pass.model.Constants.DepositStatus.FAILED_STATUS;
import static org.dataconservancy.pass.model.Constants.DepositStatus.FAILED_URI;
import static org.dataconservancy.pass.model.Constants.DepositStatus.REJECTED_STATUS;
import static org.dataconservancy.pass.model.Constants.DepositStatus.REJECTED_URI;
import static org.dataconservancy.pass.model.Constants.DepositStatus.SUBMITTED_STATUS;
import static org.dataconservancy.pass.model.Constants.DepositStatus.SUBMITTED_URI;

/**
 * A Submission can have multiple Deposits, each to a different Repository. This describes a single deposit to a Repository and captures 
 * its current status.
 * @author Karen Hanson
 */

public class Deposit extends PassEntity {

    static final String STATUS_NS = "http://oapass.org/ns/status/deposit#";

    /** 
     * A URL or some kind of reference that can be dereferenced, entity body parsed, and used to determine the status of Deposit
     */
    private String depositStatusRef;
    
    /** 
     * Status of deposit 
     */
    private DepositStatus depositStatus;
    
    /**
     * URI of the Submission that this Deposit is a part of
     */
    private URI submission;
   
    /** 
     * URI of Repository being deposited to 
     */
    private URI repository;
    
    /**
     * URI of the Repository Copy representing the copy that is reltaed to this Deposit. The value is null if there is no copy
     */
    private URI repositoryCopy;
    
    /**
     * Possible deposit statuses. Note that some repositories may not go through every status.
     * <dl>
     *     <dt>Intermediate status</dt>
     *     <dd>A {@code Deposit} with an <em>intermediate</em> status indicates that the processing of the
     *         {@code Deposit} is not yet complete.  At some indeterminate point in the future, the status <em>may</em>
     *         be updated to a <em>terminal</em> state.</dd>
     *     <dt>Terminal status</dt>
     *     <dd>A {@code Deposit} with a <em>terminal</em> status indicates that the processing of the {@code Deposit}
     *         is complete.</dd>
     * </dl>
     */
    public enum DepositStatus {

        /**
         * PASS has sent a package to the target {@code Repository} and is waiting for an update on the status.  This
         * is considered an <em>intermediate</em> state.  The {@link Deposit#getDepositStatusRef() Deposit status
         * reference} (if it exists) can be periodically consulted to determine the current state of the
         * {@code Deposit}.
         */
        @JsonProperty(SUBMITTED_STATUS)
        SUBMITTED(SUBMITTED_URI, SUBMITTED_STATUS),

        /**
         * The target {@code Repository} has accepted custody of the materials represented by the {@code Deposit}.  This
         * is considered a <em>terminal</em> state.
         */
        @JsonProperty(ACCEPTED_STATUS)
        ACCEPTED(ACCEPTED_URI, ACCEPTED_STATUS),

       /**
        * The target {@code Repository} has rejected custody of the materials represented by the {@code Deposit}.  This
        * is considered a <em>terminal</em> state.
        */
        @JsonProperty(REJECTED_STATUS)
        REJECTED(REJECTED_URI, REJECTED_STATUS),

        /**
         * A failure occurred performing the deposit; it may be re-tried later.  This is considered an
         * <em>intermediate</em> state.
         */
        @JsonProperty(FAILED_STATUS)
        FAILED(FAILED_URI, FAILED_STATUS);

        private URI uri;
        
        private String value;

        private DepositStatus(URI uri, String value) {
            this.uri = uri;
            this.value = value;
        }

        /**
         * Parses a string representation as a {@code DepositStatus}
         *
         * @param status a URI or string form of a {@code DepositStatus}
         * @return the status
         * @throws IllegalArgumentException if the supplied {@code status} does not represent a valid
         *         {@code DepositStatus}
         */
        public static DepositStatus of(String status) {
            return Stream.of(values()).filter(candidate ->
                    candidate.value.equals(status) || candidate.uri.toString().equals(status)).findAny()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Deposit Status: " + status));
        }

        /**
         * The {@code String} representation of {@code Deposit.DepositStatus}
         *
         * @return a string representing this {@code DepositStatus}
         */
        String getValue() {
            return value;
        }

        /**
         * The {@code URI} representation of {@code Deposit.DepositStatus}
         *
         * @return a URI representing this {@code DepositStatus}
         */
        public URI asUri() {
            return uri;
        }

        @Override
        public String toString() {
            return this.value;
        }
        
    }
    
    /**
     * @return the deposit status
     */
    public DepositStatus getDepositStatus() {
        return depositStatus;
    }

    
    /**
     * @param deposit status the deposit status to set
     */
    public void setDepositStatus(DepositStatus depositStatus) {
        this.depositStatus = depositStatus;
    }

    
    /**
     * @return the URI of the repository
     */
    public URI getRepository() {
        return repository;
    }

    
    /**
     * @param repository the URI of the repository to set
     */
    public void setRepository(URI repository) {
        this.repository = repository;
    }
    
    
    /**
     * @return the depositStatusRef
     */
    public String getDepositStatusRef() {
        return depositStatusRef;
    }

    
    /**
     * @param depositStatusRef the depositStatusRef to set
     */
    public void setDepositStatusRef(String depositStatusRef) {
        this.depositStatusRef = depositStatusRef;
    }

    
    /**
     * @return the submission
     */
    public URI getSubmission() {
        return submission;
    }


    /**
     * @param submission the submission to set
     */
    public void setSubmission(URI submission) {
        this.submission = submission;
    }

    
    /**
     * @return the repositoryCopy
     */
    public URI getRepositoryCopy() {
        return repositoryCopy;
    }


    /**
     * @param repositoryCopy the repositoryCopy to set
     */
    public void setRepositoryCopy(URI repositoryCopy) {
        this.repositoryCopy = repositoryCopy;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Deposit that = (Deposit) o;

        if (depositStatusRef != null ? !depositStatusRef.equals(that.depositStatusRef) : that.depositStatusRef != null) return false;
        if (depositStatus != null ? !depositStatus.equals(that.depositStatus) : that.depositStatus != null) return false;
        if (submission != null ? !submission.equals(that.submission) : that.submission != null) return false;
        if (repository != null ? !repository.equals(that.repository) : that.repository != null) return false;
        if (repositoryCopy != null ? !repositoryCopy.equals(that.repositoryCopy) : that.repositoryCopy != null) return false;
        return true;
    }
    

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (depositStatusRef != null ? depositStatusRef.hashCode() : 0);
        result = 31 * result + (depositStatus != null ? depositStatus.hashCode() : 0);
        result = 31 * result + (submission != null ? submission.hashCode() : 0);
        result = 31 * result + (repository != null ? repository.hashCode() : 0);
        result = 31 * result + (repositoryCopy != null ? repositoryCopy.hashCode() : 0);
        return result;
    }
    
}
