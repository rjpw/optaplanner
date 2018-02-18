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

package org.optaplanner.examples.musicacademy.domain.solver;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplanner.examples.musicacademy.domain.Session;
import org.optaplanner.examples.musicacademy.domain.SessionSchedule;
import org.optaplanner.examples.musicacademy.domain.Meeting;
import org.optaplanner.examples.musicacademy.domain.UnavailablePeriodPenalty;

public class MeetingDifficultyWeightFactory implements SelectionSorterWeightFactory<SessionSchedule, Meeting> {

    @Override
    public MeetingDifficultyWeight createSorterWeight(SessionSchedule schedule, Meeting meeting) {
        Session session = meeting.getSession();
        int unavailablePeriodPenaltyCount = 0;
        for (UnavailablePeriodPenalty penalty : schedule.getUnavailablePeriodPenaltyList()) {
            if (penalty.getSession().equals(session)) {
                unavailablePeriodPenaltyCount++;
            }
        }
        return new MeetingDifficultyWeight(meeting, unavailablePeriodPenaltyCount);
    }

    public static class MeetingDifficultyWeight implements Comparable<MeetingDifficultyWeight> {

        private final Meeting meeting;
        private final int unavailablePeriodPenaltyCount;

        public MeetingDifficultyWeight(Meeting meeting, int unavailablePeriodPenaltyCount) {
            this.meeting = meeting;
            this.unavailablePeriodPenaltyCount = unavailablePeriodPenaltyCount;
        }

        @Override
        public int compareTo(MeetingDifficultyWeight other) {
            Session session = meeting.getSession();
            Session otherCourse = other.meeting.getSession();
            return new CompareToBuilder()
                    .append(session.getCurriculumList().size(), otherCourse.getCurriculumList().size())
                    .append(unavailablePeriodPenaltyCount, other.unavailablePeriodPenaltyCount)
                    .append(session.getMeetingSize(), otherCourse.getMeetingSize())
                    .append(session.getStudentSize(), otherCourse.getStudentSize())
                    .append(session.getMinWorkingDaySize(), otherCourse.getMinWorkingDaySize())
                    .append(meeting.getId(), other.meeting.getId())
                    .toComparison();
        }

    }

}
