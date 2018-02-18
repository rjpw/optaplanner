/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.app.LoggingMain;
import org.optaplanner.examples.common.persistence.StringDataGenerator;
import org.optaplanner.examples.musicacademy.app.MusicAcademyApp;
import org.optaplanner.examples.musicacademy.domain.Session;
import org.optaplanner.examples.musicacademy.domain.SessionSchedule;
import org.optaplanner.examples.musicacademy.domain.Curriculum;
import org.optaplanner.examples.musicacademy.domain.Day;
import org.optaplanner.examples.musicacademy.domain.Meeting;
import org.optaplanner.examples.musicacademy.domain.Period;
import org.optaplanner.examples.musicacademy.domain.Room;
import org.optaplanner.examples.musicacademy.domain.Instructor;
import org.optaplanner.examples.musicacademy.domain.Timeslot;
import org.optaplanner.examples.musicacademy.domain.UnavailablePeriodPenalty;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

import static org.optaplanner.examples.common.persistence.AbstractSolutionImporter.*;

public class MusicAcademyGenerator extends LoggingMain {

    private static final int DAY_LIST_SIZE = 5;
    private static final int TIMESLOT_LIST_SIZE = 7;
    private static final int PERIOD_LIST_SIZE = DAY_LIST_SIZE * TIMESLOT_LIST_SIZE - TIMESLOT_LIST_SIZE + 4;

    public static void main(String[] args) {
        MusicAcademyGenerator generator = new MusicAcademyGenerator();
        generator.writeSessionSchedule(200, 8);
        generator.writeSessionSchedule(400, 16);
        generator.writeSessionSchedule(800, 32);
    }

    private final int[] roomCapacityOptions = {
            20,
            25,
            30,
            40,
            50
    };

    private final String[] SessionCodes = new String[]{
                    "Math",
                    "Chemistry",
                    "Physics",
                    "Geography",
                    "Biology",
                    "History",
                    "English",
                    "Spanish",
                    "French",
                    "German",
                    "ICT",
                    "Economics",
                    "Psychology",
                    "Art",
                    "Music"};

    private final StringDataGenerator instructorNameGenerator = StringDataGenerator.buildFullNames();

    protected final SolutionFileIO<SessionSchedule> solutionFileIO;
    protected final File outputDir;

    protected Random random;

    public MusicAcademyGenerator() {
        solutionFileIO = new XStreamSolutionFileIO<>(SessionSchedule.class);
        outputDir = new File(CommonApp.determineDataDir(MusicAcademyApp.DATA_DIR_NAME), "unsolved");
    }

    private void writeSessionSchedule(int meetingListSize, int curriculumListSize) {
        int SessionListSize = meetingListSize * 2 / 9 + 1;
        int instructorListSize = SessionListSize / 3 + 1;
        int roomListSize = meetingListSize * 2 / PERIOD_LIST_SIZE;
        String fileName = determineFileName(meetingListSize, PERIOD_LIST_SIZE, roomListSize);
        File outputFile = new File(outputDir, fileName + ".xml");
        SessionSchedule schedule = createSessionSchedule(fileName, instructorListSize, curriculumListSize, SessionListSize, meetingListSize, roomListSize);
        solutionFileIO.write(schedule, outputFile);
        logger.info("Saved: {}", outputFile);
    }

    private String determineFileName(int meetingListSize, int periodListSize, int roomListSize) {
        return meetingListSize + "meetings-" + periodListSize + "periods-" + roomListSize + "rooms";
    }

    public SessionSchedule createSessionSchedule(String fileName, int instructorListSize, int curriculumListSize, int SessionListSize, int meetingListSize, int roomListSize) {
        random = new Random(37);
        SessionSchedule schedule = new SessionSchedule();
        schedule.setId(0L);

        createDayList(schedule);
        createTimeslotList(schedule);
        createPeriodList(schedule);
        createInstructorList(schedule, instructorListSize);
        createSessionList(schedule, SessionListSize);
        createMeetingList(schedule, meetingListSize);
        createRoomList(schedule, roomListSize);
        createCurriculumList(schedule, curriculumListSize);
        createUnavailablePeriodPenaltyList(schedule);

        int possibleForOneMeetingSize = schedule.getPeriodList().size() * schedule.getRoomList().size();
        BigInteger possibleSolutionSize = BigInteger.valueOf(possibleForOneMeetingSize).pow(
                schedule.getMeetingList().size());
        logger.info("SessionSchedule {} has {} instructors, {} curricula, {} Sessions, {} meetings," +
                        " {} periods, {} rooms and {} unavailable period constraints with a search space of {}.",
                fileName,
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

    private void createDayList(SessionSchedule schedule) {
        List<Day> dayList = new ArrayList<>(DAY_LIST_SIZE);
        for (int i = 0; i < DAY_LIST_SIZE; i++) {
            Day day = new Day();
            day.setId((long) i);
            day.setDayIndex(i);
            day.setPeriodList(new ArrayList<>(TIMESLOT_LIST_SIZE));
            dayList.add(day);
        }
        schedule.setDayList(dayList);
    }

    private void createTimeslotList(SessionSchedule schedule) {
        List<Timeslot> timeslotList = new ArrayList<>(TIMESLOT_LIST_SIZE);
        for (int i = 0; i < TIMESLOT_LIST_SIZE; i++) {
            Timeslot timeslot = new Timeslot();
            timeslot.setId((long) i);
            timeslot.setTimeslotIndex(i);
            timeslotList.add(timeslot);
        }
        schedule.setTimeslotList(timeslotList);
    }

    private void createPeriodList(SessionSchedule schedule) {
        List<Period> periodList = new ArrayList<>(schedule.getDayList().size() * schedule.getTimeslotList().size());
        long periodId = 0L;
        for (Day day : schedule.getDayList()) {
            for (Timeslot timeslot : schedule.getTimeslotList()) {
                if (day.getDayIndex() == 2 && timeslot.getTimeslotIndex() >= 4) {
                    // No meetings Wednesday afternoon
                    continue;
                }
                Period period = new Period();
                period.setId(periodId);
                periodId++;
                period.setDay(day);
                day.getPeriodList().add(period);
                period.setTimeslot(timeslot);
                periodList.add(period);
            }
        }
        schedule.setPeriodList(periodList);
    }

    private void createInstructorList(SessionSchedule schedule, int instructorListSize) {
        List<Instructor> instructorList = new ArrayList<>(instructorListSize);
        instructorNameGenerator.predictMaximumSizeAndReset(instructorListSize);
        for (int i = 0; i < instructorListSize; i++) {
            Instructor instructor = new Instructor();
            instructor.setId((long) i);
            instructor.setCode(instructorNameGenerator.generateNextValue());
            instructorList.add(instructor);
        }
        schedule.setInstructorList(instructorList);
    }

    private void createSessionList(SessionSchedule schedule, int SessionListSize) {
        List<Instructor> instructorList = schedule.getInstructorList();
        List<Session> SessionList = new ArrayList<>(SessionListSize);
        Set<String> codeSet = new HashSet<>();
        for (int i = 0; i < SessionListSize; i++) {
            Session Session = new Session();
            Session.setId((long) i);
            String code = (i < SessionCodes.length * 2)
                    ? SessionCodes[i % SessionCodes.length]
                    : SessionCodes[random.nextInt(SessionCodes.length)];
            StringDataGenerator codeSuffixGenerator = new StringDataGenerator("")
                    .addAToZPart(true, 0);
            if (SessionListSize >= SessionCodes.length) {
                String codeSuffix = codeSuffixGenerator.generateNextValue();
                while (codeSet.contains(code + codeSuffix)) {
                    codeSuffix = codeSuffixGenerator.generateNextValue();
                }
                code = code + codeSuffix;
                codeSet.add(code);
            }
            Session.setCode(code);
            Instructor instructor = (i < instructorList.size() * 2)
                    ? instructorList.get(i % instructorList.size())
                    : instructorList.get(random.nextInt(instructorList.size()));
            Session.setInstructor(instructor);
            Session.setMeetingSize(0);
            Session.setMinWorkingDaySize(1);
            Session.setCurriculumList(new ArrayList<>());
            Session.setStudentSize(0);
            SessionList.add(Session);
        }
        schedule.setSessionList(SessionList);
    }

    private void createMeetingList(SessionSchedule schedule, int meetingListSize) {
        List<Session> SessionList = schedule.getSessionList();
        List<Meeting> meetingList = new ArrayList<>(meetingListSize);
        for (int i = 0; i < meetingListSize; i++) {
            Meeting meeting = new Meeting();
            meeting.setId((long) i);
            Session Session = (i < SessionList.size() * 2)
                    ? SessionList.get(i % SessionList.size())
                    : SessionList.get(random.nextInt(SessionList.size()));
            meeting.setSession(Session);
            meeting.setMeetingIndexInSession(Session.getMeetingSize());
            Session.setMeetingSize(Session.getMeetingSize() + 1);
            meeting.setLocked(false);
            meetingList.add(meeting);
        }
        schedule.setMeetingList(meetingList);

    }

    private void createRoomList(SessionSchedule schedule, int roomListSize) {
        List<Room> roomList = new ArrayList<>(roomListSize);
        for (int i = 0; i < roomListSize; i++) {
            Room room = new Room();
            room.setId((long) i);
            room.setCode("R" + ((i / 50 * 100) + 1 + i));
            room.setCapacity(roomCapacityOptions[random.nextInt(roomCapacityOptions.length)]);
            roomList.add(room);
        }
        schedule.setRoomList(roomList);
    }

    private void createCurriculumList(SessionSchedule schedule, int curriculumListSize) {
        int maximumCapacity = schedule.getRoomList().stream().mapToInt(Room::getCapacity).max().getAsInt();
        List<Session> SessionList = schedule.getSessionList();
        List<Curriculum> curriculumList = new ArrayList<>(curriculumListSize);
        StringDataGenerator codeGenerator = new StringDataGenerator("")
                .addAToZPart(true, 0).addAToZPart(false, 1).addAToZPart(false, 1).addAToZPart(false, 1);
        codeGenerator.predictMaximumSizeAndReset(curriculumListSize);
        for (int i = 0; i < curriculumListSize; i++) {
            Curriculum curriculum = new Curriculum();
            curriculum.setId((long) i);
            curriculum.setCode("Group " + codeGenerator.generateNextValue());
            // The studentSize is more likely to be 15 than 5 or 25
            int studentSize = 5 + random.nextInt(10) + random.nextInt(10);

            List<Session> SessionSubList = SessionList.stream()
                    .filter(Session -> Session.getStudentSize() + studentSize < maximumCapacity)
                    .collect(Collectors.toList());
            Collections.shuffle(SessionSubList, random);

            int meetingCount = 0;
            for (Session Session : SessionSubList) {
                meetingCount += Session.getMeetingSize();
                if (meetingCount > PERIOD_LIST_SIZE) {
                    break;
                }
                Session.getCurriculumList().add(curriculum);
                Session.setStudentSize(Session.getStudentSize() + studentSize);
            }

            curriculumList.add(curriculum);
        }
        schedule.setCurriculumList(curriculumList);
    }

    private void createUnavailablePeriodPenaltyList(SessionSchedule schedule) {
        List<Session> SessionList = schedule.getSessionList();
        List<Period> periodList = schedule.getPeriodList();
        List<UnavailablePeriodPenalty> unavailablePeriodPenaltyList = new ArrayList<>(SessionList.size());
        long penaltyId = 0L;
        for (Session Session : SessionList) {
            UnavailablePeriodPenalty penalty = new UnavailablePeriodPenalty();
            penalty.setId(penaltyId);
            penaltyId++;
            penalty.setSession(Session);
            penalty.setPeriod(periodList.get(random.nextInt(periodList.size())));
            unavailablePeriodPenaltyList.add(penalty);
        }
        schedule.setUnavailablePeriodPenaltyList(unavailablePeriodPenaltyList);
    }

}
