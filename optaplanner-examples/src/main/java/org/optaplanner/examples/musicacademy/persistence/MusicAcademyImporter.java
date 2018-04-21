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

package org.optaplanner.examples.musicacademy.persistence;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter;
import org.optaplanner.examples.common.persistence.SolutionConverter;
import org.optaplanner.examples.musicacademy.app.MusicAcademyApp;
import org.optaplanner.examples.musicacademy.domain.SessionSchedule;
import org.optaplanner.examples.musicacademy.domain.Curriculum;
import org.optaplanner.examples.musicacademy.domain.Day;
import org.optaplanner.examples.musicacademy.domain.Meeting;
import org.optaplanner.examples.musicacademy.domain.Period;
import org.optaplanner.examples.musicacademy.domain.Room;
import org.optaplanner.examples.musicacademy.domain.Session;
import org.optaplanner.examples.musicacademy.domain.Instructor;
import org.optaplanner.examples.musicacademy.domain.Timeslot;
import org.optaplanner.examples.musicacademy.domain.UnavailablePeriodPenalty;

public class MusicAcademyImporter extends AbstractTxtSolutionImporter<SessionSchedule> {

    private static final String INPUT_FILE_SUFFIX = "ctt";

    public static void main(String[] args) {
        SolutionConverter<SessionSchedule> converter = SolutionConverter.createImportConverter(
                MusicAcademyApp.DATA_DIR_NAME, new MusicAcademyImporter(), SessionSchedule.class);
        converter.convertAll();
    }

    @Override
    public String getInputFileSuffix() {
        return INPUT_FILE_SUFFIX;
    }

    @Override
    public TxtInputBuilder<SessionSchedule> createTxtInputBuilder() {
        return new MusicAcademyInputBuilder();
    }

    public static class MusicAcademyInputBuilder extends TxtInputBuilder<SessionSchedule> {

        @Override
        public SessionSchedule readSolution() throws IOException {
            SessionSchedule schedule = new SessionSchedule();
            schedule.setId(0L);
            // Name: ToyExample
            schedule.setName(readStringValue("Name:"));
            // Sessions: 4
            int sessionListSize = readIntegerValue("Sessions:");
            // Rooms: 2
            int roomListSize = readIntegerValue("Rooms:");
            // Days: 5
            int dayListSize = readIntegerValue("Days:");
            // Periods_per_day: 4
            int timeslotListSize = readIntegerValue("Periods_per_day:");
            // Curricula: 2
            int curriculumListSize = readIntegerValue("Curricula:");
            // Constraints: 8
            int unavailablePeriodPenaltyListSize = readIntegerValue("Constraints:");

            Map<String, Session> sessionMap = readSessionListAndInstructorList(
                    schedule, sessionListSize);
            
            readRoomList(
                    schedule, roomListSize);
            
            Map<List<Integer>, Period> periodMap = createPeriodListAndDayListAndTimeslotList(
                    schedule, dayListSize, timeslotListSize);

            readCurriculumList(
                    schedule, sessionMap, curriculumListSize);

            readUnavailablePeriodPenaltyList(
                    schedule, sessionMap, periodMap, unavailablePeriodPenaltyListSize);

            readEmptyLine();
            readConstantLine("END\\.");
            createMeetingList(schedule);

            int possibleForOneMeetingSize = schedule.getPeriodList().size() * schedule.getRoomList().size();
            BigInteger possibleSolutionSize = BigInteger.valueOf(possibleForOneMeetingSize).pow(
                    schedule.getMeetingList().size());

            logger.info("SessionSchedule {} has {} instructors, {} curricula, {} sessions, {} meetings," +
                    " {} periods, {} rooms and {} unavailable period constraints with a search space of {}.",
                    getInputId(),
                    schedule.getInstructorList().size(),
                    schedule.getCurriculumList().size(),
                    schedule.getSessionList().size(),
                    schedule.getMeetingList().size(),
                    schedule.getPeriodList().size(),
                    schedule.getRoomList().size(),
                    schedule.getUnavailablePeriodPenaltyList().size(),
                    getFlooredPossibleSolutionSize(possibleSolutionSize));

            return schedule;

        }

        private Map<String, Session> readSessionListAndInstructorList(
                SessionSchedule schedule, int sessionListSize) throws IOException {

            Map<String, Session> sessionMap = new HashMap<>(sessionListSize);
            Map<String, Instructor> instructorMap = new HashMap<>();
            List<Session> sessionList = new ArrayList<>(sessionListSize);
            readEmptyLine();

            readConstantLine("SESSIONS:");

            for (int i = 0; i < sessionListSize; i++) {
                Session session = new Session();
                session.setId((long) i);
                // Sessions: <SessionID> <Instructor> <# Meetings> <MinWorkingDays> <# Students>
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line, 5);
                session.setCode(lineTokens[0]);
                session.setInstructor(findOrCreateInstructor(instructorMap, lineTokens[1]));
                session.setMeetingSize(Integer.parseInt(lineTokens[2]));
                session.setMinWorkingDaySize(Integer.parseInt(lineTokens[3]));
                session.setCurriculumList(new ArrayList<>());
                session.setStudentSize(Integer.parseInt(lineTokens[4]));
                sessionList.add(session);
                sessionMap.put(session.getCode(), session);
            }

            schedule.setSessionList(sessionList);
            List<Instructor> instructorList = new ArrayList<>(instructorMap.values());
            schedule.setInstructorList(instructorList);
            return sessionMap;
        }

        private Instructor findOrCreateInstructor(Map<String, Instructor> instructorMap, String code) {
            Instructor instructor = instructorMap.get(code);
            if (instructor == null) {
                instructor = new Instructor();
                int id = instructorMap.size();
                instructor.setId((long) id);
                instructor.setCode(code);
                instructorMap.put(code, instructor);
            }
            return instructor;
        }

        private void readRoomList(SessionSchedule schedule, int roomListSize)
                throws IOException {
            readEmptyLine();
            readConstantLine("ROOMS:");
            List<Room> roomList = new ArrayList<>(roomListSize);
            for (int i = 0; i < roomListSize; i++) {
                Room room = new Room();
                room.setId((long) i);
                // Rooms: <RoomID> <Capacity>
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line, 2);
                room.setCode(lineTokens[0]);
                room.setCapacity(Integer.parseInt(lineTokens[1]));
                roomList.add(room);
            }
            schedule.setRoomList(roomList);
        }

        private Map<List<Integer>, Period> createPeriodListAndDayListAndTimeslotList(
                SessionSchedule schedule, int dayListSize, int timeslotListSize) throws IOException {
            int periodListSize = dayListSize * timeslotListSize;
            Map<List<Integer>, Period> periodMap = new HashMap<>(periodListSize);
            List<Day> dayList = new ArrayList<>(dayListSize);
            for (int i = 0; i < dayListSize; i++) {
                Day day = new Day();
                day.setId((long) i);
                day.setDayIndex(i);
                day.setPeriodList(new ArrayList<>(timeslotListSize));
                dayList.add(day);
            }
            schedule.setDayList(dayList);
            List<Timeslot> timeslotList = new ArrayList<>(timeslotListSize);
            for (int i = 0; i < timeslotListSize; i++) {
                Timeslot timeslot = new Timeslot();
                timeslot.setId((long) i);
                timeslot.setTimeslotIndex(i);
                timeslotList.add(timeslot);
            }
            schedule.setTimeslotList(timeslotList);
            List<Period> periodList = new ArrayList<>(periodListSize);
            for (int i = 0; i < dayListSize; i++) {
                Day day = dayList.get(i);
                for (int j = 0; j < timeslotListSize; j++) {
                    Period period = new Period();
                    period.setId((long) (i * timeslotListSize + j));
                    period.setDay(day);
                    period.setTimeslot(timeslotList.get(j));
                    periodList.add(period);
                    periodMap.put(Arrays.asList(i, j), period);
                    day.getPeriodList().add(period);
                }
            }
            schedule.setPeriodList(periodList);
            return periodMap;
        }

        private void readCurriculumList(SessionSchedule schedule,
                Map<String, Session> sessionMap, int curriculumListSize) throws IOException {

            readEmptyLine();
            readConstantLine("CURRICULA:");
            List<Curriculum> curriculumList = new ArrayList<>(curriculumListSize);

            for (int i = 0; i < curriculumListSize; i++) {
                Curriculum curriculum = new Curriculum();
                curriculum.setId((long) i);
                // Curricula: <CurriculumID> <# Sessions> <MemberID> ... <MemberID>
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line);
                if (lineTokens.length < 2) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain at least 2 tokens.");
                }
                curriculum.setCode(lineTokens[0]);
                int sessionsInCurriculum = Integer.parseInt(lineTokens[1]);
                if (lineTokens.length != (sessionsInCurriculum + 2)) {
                    throw new IllegalArgumentException("Read line (" + line + ") is expected to contain "
                            + (sessionsInCurriculum + 2) + " tokens.");
                }
                for (int j = 2; j < lineTokens.length; j++) {
                    Session session = sessionMap.get(lineTokens[j]);
                    if (session == null) {
                        throw new IllegalArgumentException("Read line (" + line + ") uses an unexisting session("
                                + lineTokens[j] + ").");
                    }
                    session.getCurriculumList().add(curriculum);
                }
                curriculumList.add(curriculum);
            }

            schedule.setCurriculumList(curriculumList);

        }

        private void readUnavailablePeriodPenaltyList(SessionSchedule schedule, Map<String, Session> sessionMap,
                Map<List<Integer>, Period> periodMap, int unavailablePeriodPenaltyListSize)
                throws IOException {

            readEmptyLine();
            readConstantLine("UNAVAILABILITY_CONSTRAINTS:");
            List<UnavailablePeriodPenalty> penaltyList = new ArrayList<>(
                    unavailablePeriodPenaltyListSize);

            for (int i = 0; i < unavailablePeriodPenaltyListSize; i++) {

                UnavailablePeriodPenalty penalty = new UnavailablePeriodPenalty();
                penalty.setId((long) i);

                // Unavailability_Constraints: <SessionID> <Day> <Day_Period>
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line, 3);
                penalty.setSession(sessionMap.get(lineTokens[0]));
                int dayIndex = Integer.parseInt(lineTokens[1]);
                int timeslotIndex = Integer.parseInt(lineTokens[2]);

                Period period = periodMap.get(Arrays.asList(dayIndex, timeslotIndex));
                if (period == null) {
                    throw new IllegalArgumentException("Read line (" + line + ") uses an unexisting period("
                            + dayIndex + " " + timeslotIndex + ").");
                }

                penalty.setPeriod(period);
                penaltyList.add(penalty);

            }

            schedule.setUnavailablePeriodPenaltyList(penaltyList);

        }

        private void createMeetingList(SessionSchedule schedule) {

            List<Session> sessionList = schedule.getSessionList();
            List<Meeting> meetingList = new ArrayList<>(sessionList.size());

            long id = 0L;

            for (Session session : sessionList) {
                for (int i = 0; i < session.getMeetingSize(); i++) {
                    Meeting meeting = new Meeting();
                    meeting.setId(id);
                    id++;
                    meeting.setSession(session);
                    meeting.setMeetingIndexInSession(i);
                    meeting.setLocked(false);
                    // Notice that we leave the PlanningVariable properties on null
                    meetingList.add(meeting);
                }
            }

            schedule.setMeetingList(meetingList);

        }

    }

}
