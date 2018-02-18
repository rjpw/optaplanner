/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.musicacademy.domain.solver.SessionConflict;
import org.optaplanner.persistence.xstream.api.score.buildin.hardsoft.HardSoftScoreXStreamConverter;

@PlanningSolution
@XStreamAlias("SessionSchedule")
public class SessionSchedule extends AbstractPersistable {

    private String name;

    private List<Instructor> instructorList;
    private List<Curriculum> curriculumList;
    private List<Session> sessionList;
    private List<Day> dayList;
    private List<Timeslot> timeslotList;
    private List<Period> periodList;
    private List<Room> roomList;

    private List<UnavailablePeriodPenalty> unavailablePeriodPenaltyList;

    private List<Meeting> meetingList;

    @XStreamConverter(HardSoftScoreXStreamConverter.class)
    private HardSoftScore score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ProblemFactCollectionProperty
    public List<Instructor> getInstructorList() {
        return instructorList;
    }

    public void setInstructorList(List<Instructor> instructorList) {
        this.instructorList = instructorList;
    }

    @ProblemFactCollectionProperty
    public List<Curriculum> getCurriculumList() {
        return curriculumList;
    }

    public void setCurriculumList(List<Curriculum> curriculumList) {
        this.curriculumList = curriculumList;
    }

    @ProblemFactCollectionProperty
    public List<Session> getSessionList() {
        return sessionList;
    }

    public void setSessionList(List<Session> sessionList) {
        this.sessionList = sessionList;
    }

    @ProblemFactCollectionProperty
    public List<Day> getDayList() {
        return dayList;
    }

    public void setDayList(List<Day> dayList) {
        this.dayList = dayList;
    }

    @ProblemFactCollectionProperty
    public List<Timeslot> getTimeslotList() {
        return timeslotList;
    }

    public void setTimeslotList(List<Timeslot> timeslotList) {
        this.timeslotList = timeslotList;
    }

    @ValueRangeProvider(id = "periodRange")
    @ProblemFactCollectionProperty
    public List<Period> getPeriodList() {
        return periodList;
    }

    public void setPeriodList(List<Period> periodList) {
        this.periodList = periodList;
    }

    @ValueRangeProvider(id = "roomRange")
    @ProblemFactCollectionProperty
    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    @ProblemFactCollectionProperty
    public List<UnavailablePeriodPenalty> getUnavailablePeriodPenaltyList() {
        return unavailablePeriodPenaltyList;
    }

    public void setUnavailablePeriodPenaltyList(List<UnavailablePeriodPenalty> unavailablePeriodPenaltyList) {
        this.unavailablePeriodPenaltyList = unavailablePeriodPenaltyList;
    }

    @PlanningEntityCollectionProperty
    public List<Meeting> getMeetingList() {
        return meetingList;
    }

    public void setMeetingList(List<Meeting> meetingList) {
        this.meetingList = meetingList;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @ProblemFactCollectionProperty
    private List<SessionConflict> calculateSessionConflictList() {
        List<SessionConflict> SessionConflictList = new ArrayList<>();
        for (Session leftCourse : sessionList) {
            for (Session rightCourse : sessionList) {
                if (leftCourse.getId() < rightCourse.getId()) {
                    int conflictCount = 0;
                    if (leftCourse.getInstructor().equals(rightCourse.getInstructor())) {
                        conflictCount++;
                    }
                    for (Curriculum curriculum : leftCourse.getCurriculumList()) {
                        if (rightCourse.getCurriculumList().contains(curriculum)) {
                            conflictCount++;
                        }
                    }
                    if (conflictCount > 0) {
                        SessionConflictList.add(new SessionConflict(leftCourse, rightCourse, conflictCount));
                    }
                }
            }
        }
        return SessionConflictList;
    }

}
