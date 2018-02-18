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

import org.optaplanner.examples.common.persistence.AbstractTxtSolutionExporter;
import org.optaplanner.examples.common.persistence.SolutionConverter;
import org.optaplanner.examples.musicacademy.app.MusicAcademyApp;
import org.optaplanner.examples.musicacademy.domain.SessionSchedule;
import org.optaplanner.examples.musicacademy.domain.Meeting;

public class MusicAcademyExporter extends AbstractTxtSolutionExporter<SessionSchedule> {
    private static final String OUTPUT_FILE_SUFFIX = "sol";

    public static void main(String[] args) {
        SolutionConverter<SessionSchedule> converter = SolutionConverter.createExportConverter(
                MusicAcademyApp.DATA_DIR_NAME, SessionSchedule.class, new MusicAcademyExporter());
        converter.convertAll();
    }

    @Override
    public String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    @Override
    public TxtOutputBuilder<SessionSchedule> createTxtOutputBuilder() {
        return new MusicAcademyOutputBuilder();
    }

    public static class MusicAcademyOutputBuilder extends TxtOutputBuilder<SessionSchedule> {

        @Override
        public void writeSolution() throws IOException {
            for (Meeting meeting : solution.getMeetingList()) {
                bufferedWriter.write(meeting.getSession().getCode()
                        + " r" + meeting.getRoom().getCode()
                        + " " + meeting.getPeriod().getDay().getDayIndex()
                        + " " + meeting.getPeriod().getTimeslot().getTimeslotIndex() + "\r\n");
            }
        }
    }

}
