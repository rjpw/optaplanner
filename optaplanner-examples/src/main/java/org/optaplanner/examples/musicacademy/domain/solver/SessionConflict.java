/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.musicacademy.domain.solver;

import java.io.Serializable;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.examples.musicacademy.domain.Session;

/**
 * Calculated during initialization, not modified during score calculation.
 */
public class SessionConflict implements Serializable, Comparable<SessionConflict> {

    private final Session leftSession;
    private final Session rightSession;
    private final int conflictCount;

    public SessionConflict(Session leftSession, Session rightSession, int conflictCount) {
        this.leftSession = leftSession;
        this.rightSession = rightSession;
        this.conflictCount = conflictCount;
    }

    public Session getLeftSession() {
        return leftSession;
    }

    public Session getRightSession() {
        return rightSession;
    }

    public int getConflictCount() {
        return conflictCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SessionConflict) {
            SessionConflict other = (SessionConflict) o;
            return new EqualsBuilder()
                    .append(leftSession, other.leftSession)
                    .append(rightSession, other.rightSession)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftSession)
                .append(rightSession)
                .toHashCode();
    }

    @Override
    public int compareTo(SessionConflict other) {
        return new CompareToBuilder()
                .append(leftSession, other.leftSession)
                .append(rightSession, other.rightSession)
                .toComparison();
    }

    @Override
    public String toString() {
        return leftSession + " & " + rightSession;
    }

}
