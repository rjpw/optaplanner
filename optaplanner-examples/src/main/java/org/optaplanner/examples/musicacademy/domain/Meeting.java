/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.musicacademy.domain;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.musicacademy.domain.solver.MeetingDifficultyWeightFactory;
import org.optaplanner.examples.musicacademy.domain.solver.MovableMeetingSelectionFilter;
import org.optaplanner.examples.musicacademy.domain.solver.PeriodStrengthWeightFactory;
import org.optaplanner.examples.musicacademy.domain.solver.RoomStrengthWeightFactory;

@PlanningEntity(movableEntitySelectionFilter = MovableMeetingSelectionFilter.class,
        difficultyWeightFactoryClass = MeetingDifficultyWeightFactory.class)
@XStreamAlias("Meeting")
public class Meeting extends AbstractPersistable {

    private Session session;
    private int meetingIndexInSession;
    private boolean locked;

    // Planning variables: changes during planning, between score calculations.
    private Period period;
    private Room room;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public int getMeetingIndexInSession() {
        return meetingIndexInSession;
    }

    public void setMeetingIndexInSession(int meetingIndexInSession) {
        this.meetingIndexInSession = meetingIndexInSession;
    }

    /**
     * @return true if immovable planning entity
     * @see MovableMeetingSelectionFilter
     */
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @PlanningVariable(valueRangeProviderRefs = {"periodRange"},
            strengthWeightFactoryClass = PeriodStrengthWeightFactory.class)
    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    @PlanningVariable(valueRangeProviderRefs = {"roomRange"},
            strengthWeightFactoryClass = RoomStrengthWeightFactory.class)
    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Instructor getInstructor() {
        return session.getInstructor();
    }

    public int getStudentSize() {
        return session.getStudentSize();
    }

    public List<Curriculum> getCurriculumList() {
        return session.getCurriculumList();
    }

    public Day getDay() {
        if (period == null) {
            return null;
        }
        return period.getDay();
    }

    public int getTimeslotIndex() {
        if (period == null) {
            return Integer.MIN_VALUE;
        }
        return period.getTimeslot().getTimeslotIndex();
    }

    public String getLabel() {
        return session.getCode() + "-" + meetingIndexInSession;
    }

    @Override
    public String toString() {
        return session + "-" + meetingIndexInSession;
    }

}
