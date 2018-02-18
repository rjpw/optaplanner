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

package org.optaplanner.examples.musicacademy.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.optaplanner.examples.common.swingui.CommonIcons;
import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplanner.examples.common.swingui.components.LabeledComboBoxRenderer;
import org.optaplanner.examples.common.swingui.timetable.TimeTablePanel;
import org.optaplanner.examples.musicacademy.domain.SessionSchedule;
import org.optaplanner.examples.musicacademy.domain.Curriculum;
import org.optaplanner.examples.musicacademy.domain.Day;
import org.optaplanner.examples.musicacademy.domain.Meeting;
import org.optaplanner.examples.musicacademy.domain.Period;
import org.optaplanner.examples.musicacademy.domain.Room;
import org.optaplanner.examples.musicacademy.domain.Instructor;
import org.optaplanner.swing.impl.SwingUtils;
import org.optaplanner.swing.impl.TangoColorFactory;

import static org.optaplanner.examples.common.swingui.timetable.TimeTablePanel.HeaderColumnKey.*;
import static org.optaplanner.examples.common.swingui.timetable.TimeTablePanel.HeaderRowKey.*;

public class MusicAcademyPanel extends SolutionPanel<SessionSchedule> {

    public static final String LOGO_PATH = "/org/optaplanner/examples/musicacademy/swingui/MusicAcademyLogo.png";

    private final TimeTablePanel<Room, Period> roomsPanel;
    private final TimeTablePanel<Instructor, Period> instructorsPanel;
    private final TimeTablePanel<Curriculum, Period> curriculaPanel;

    public MusicAcademyPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        roomsPanel = new TimeTablePanel<>();
        tabbedPane.add("Rooms", new JScrollPane(roomsPanel));
        instructorsPanel = new TimeTablePanel<>();
        tabbedPane.add("Instructors", new JScrollPane(instructorsPanel));
        curriculaPanel = new TimeTablePanel<>();
        tabbedPane.add("Curricula", new JScrollPane(curriculaPanel));
        add(tabbedPane, BorderLayout.CENTER);
        setPreferredSize(PREFERRED_SCROLLABLE_VIEWPORT_SIZE);
    }

    @Override
    public boolean isWrapInScrollPane() {
        return false;
    }

    @Override
    public void resetPanel(SessionSchedule courseSchedule) {
        roomsPanel.reset();
        instructorsPanel.reset();
        curriculaPanel.reset();
        defineGrid(courseSchedule);
        fillCells(courseSchedule);
        repaint(); // Hack to force a repaint of TimeTableLayout during "refresh screen while solving"
    }

    private void defineGrid(SessionSchedule courseSchedule) {
        JButton footprint = SwingUtils.makeSmallButton(new JButton("LinLetGre1-0"));
        int footprintWidth = footprint.getPreferredSize().width;

        roomsPanel.defineColumnHeaderByKey(HEADER_COLUMN_GROUP1); // Day header
        roomsPanel.defineColumnHeaderByKey(HEADER_COLUMN); // Period header
        for (Room room : courseSchedule.getRoomList()) {
            roomsPanel.defineColumnHeader(room, footprintWidth);
        }
        roomsPanel.defineColumnHeader(null, footprintWidth); // Unassigned

        instructorsPanel.defineColumnHeaderByKey(HEADER_COLUMN_GROUP1); // Day header
        instructorsPanel.defineColumnHeaderByKey(HEADER_COLUMN); // Period header
        for (Instructor instructor : courseSchedule.getInstructorList()) {
            instructorsPanel.defineColumnHeader(instructor, footprintWidth);
        }

        curriculaPanel.defineColumnHeaderByKey(HEADER_COLUMN_GROUP1); // Day header
        curriculaPanel.defineColumnHeaderByKey(HEADER_COLUMN); // Period header
        for (Curriculum curriculum : courseSchedule.getCurriculumList()) {
            curriculaPanel.defineColumnHeader(curriculum, footprintWidth);
        }

        roomsPanel.defineRowHeaderByKey(HEADER_ROW); // Room header
        instructorsPanel.defineRowHeaderByKey(HEADER_ROW); // Instructor header
        curriculaPanel.defineRowHeaderByKey(HEADER_ROW); // Curriculum header
        for (Period period : courseSchedule.getPeriodList()) {
            roomsPanel.defineRowHeader(period);
            instructorsPanel.defineRowHeader(period);
            curriculaPanel.defineRowHeader(period);
        }
        roomsPanel.defineRowHeader(null); // Unassigned period
        instructorsPanel.defineRowHeader(null); // Unassigned period
        curriculaPanel.defineRowHeader(null); // Unassigned period
    }

    private void fillCells(SessionSchedule courseSchedule) {
        roomsPanel.addCornerHeader(HEADER_COLUMN_GROUP1, HEADER_ROW, createTableHeader(new JLabel("Day")));
        roomsPanel.addCornerHeader(HEADER_COLUMN, HEADER_ROW, createTableHeader(new JLabel("Time")));
        fillRoomCells(courseSchedule);
        instructorsPanel.addCornerHeader(HEADER_COLUMN_GROUP1, HEADER_ROW, createTableHeader(new JLabel("Day")));
        instructorsPanel.addCornerHeader(HEADER_COLUMN, HEADER_ROW, createTableHeader(new JLabel("Time")));
        fillInstructorCells(courseSchedule);
        curriculaPanel.addCornerHeader(HEADER_COLUMN_GROUP1, HEADER_ROW, createTableHeader(new JLabel("Day")));
        curriculaPanel.addCornerHeader(HEADER_COLUMN, HEADER_ROW, createTableHeader(new JLabel("Time")));
        fillCurriculumCells(courseSchedule);
        fillDayCells(courseSchedule);
        fillMeetingCells(courseSchedule);
    }

    private void fillRoomCells(SessionSchedule courseSchedule) {
        for (Room room : courseSchedule.getRoomList()) {
            roomsPanel.addColumnHeader(room, HEADER_ROW,
                    createTableHeader(new JLabel(room.getLabel(), SwingConstants.CENTER)));
        }
        roomsPanel.addColumnHeader(null, HEADER_ROW,
                createTableHeader(new JLabel("Unassigned", SwingConstants.CENTER)));
    }

    private void fillInstructorCells(SessionSchedule courseSchedule) {
        for (Instructor instructor : courseSchedule.getInstructorList()) {
            instructorsPanel.addColumnHeader(instructor, HEADER_ROW,
                    createTableHeader(new JLabel(instructor.getLabel(), SwingConstants.CENTER)));
        }
    }

    private void fillCurriculumCells(SessionSchedule courseSchedule) {
        for (Curriculum curriculum : courseSchedule.getCurriculumList()) {
            curriculaPanel.addColumnHeader(curriculum, HEADER_ROW,
                    createTableHeader(new JLabel(curriculum.getLabel(), SwingConstants.CENTER)));
        }
    }

    private void fillDayCells(SessionSchedule courseSchedule) {
        for (Day day : courseSchedule.getDayList()) {
            Period dayStartPeriod = day.getPeriodList().get(0);
            Period dayEndPeriod = day.getPeriodList().get(day.getPeriodList().size() - 1);
            roomsPanel.addRowHeader(HEADER_COLUMN_GROUP1, dayStartPeriod, HEADER_COLUMN_GROUP1, dayEndPeriod,
                    createTableHeader(new JLabel(day.getLabel())));
            instructorsPanel.addRowHeader(HEADER_COLUMN_GROUP1, dayStartPeriod, HEADER_COLUMN_GROUP1, dayEndPeriod,
                    createTableHeader(new JLabel(day.getLabel())));
            curriculaPanel.addRowHeader(HEADER_COLUMN_GROUP1, dayStartPeriod, HEADER_COLUMN_GROUP1, dayEndPeriod,
                    createTableHeader(new JLabel(day.getLabel())));
            for (Period period : day.getPeriodList()) {
                roomsPanel.addRowHeader(HEADER_COLUMN, period,
                        createTableHeader(new JLabel(period.getTimeslot().getLabel())));
                instructorsPanel.addRowHeader(HEADER_COLUMN, period,
                        createTableHeader(new JLabel(period.getTimeslot().getLabel())));
                curriculaPanel.addRowHeader(HEADER_COLUMN, period,
                        createTableHeader(new JLabel(period.getTimeslot().getLabel())));
            }
        }
        roomsPanel.addRowHeader(HEADER_COLUMN_GROUP1, null, HEADER_COLUMN, null,
                createTableHeader(new JLabel("Unassigned")));
        instructorsPanel.addRowHeader(HEADER_COLUMN_GROUP1, null, HEADER_COLUMN, null,
                createTableHeader(new JLabel("Unassigned")));
        curriculaPanel.addRowHeader(HEADER_COLUMN_GROUP1, null, HEADER_COLUMN, null,
                createTableHeader(new JLabel("Unassigned")));
    }

    private void fillMeetingCells(SessionSchedule courseSchedule) {
        preparePlanningEntityColors(courseSchedule.getMeetingList());
        for (Meeting meeting : courseSchedule.getMeetingList()) {
            Color color = determinePlanningEntityColor(meeting, meeting.getSession());
            String toolTip = determinePlanningEntityTooltip(meeting);
            roomsPanel.addCell(meeting.getRoom(), meeting.getPeriod(),
                    createButton(meeting, color, toolTip));
            instructorsPanel.addCell(meeting.getInstructor(), meeting.getPeriod(),
                    createButton(meeting, color, toolTip));
            for (Curriculum curriculum : meeting.getCurriculumList()) {
                curriculaPanel.addCell(curriculum, meeting.getPeriod(),
                        createButton(meeting, color, toolTip));
            }
        }
    }

    private JPanel createTableHeader(JLabel label) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(label, BorderLayout.NORTH);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TangoColorFactory.ALUMINIUM_5),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        return headerPanel;
    }

    private JButton createButton(Meeting meeting, Color color, String toolTip) {
        JButton button = SwingUtils.makeSmallButton(new JButton(new MeetingAction(meeting)));
        button.setBackground(color);
        if (meeting.isLocked()) {
            button.setIcon(CommonIcons.LOCKED_ICON);
        }
        button.setToolTipText(toolTip);
        return button;
    }

    @Override
    public boolean isIndictmentHeatMapEnabled() {
        return true;
    }

    private class MeetingAction extends AbstractAction {

        private Meeting meeting;

        public MeetingAction(Meeting meeting) {
            super(meeting.getLabel());
            this.meeting = meeting;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JPanel listFieldsPanel = new JPanel(new GridLayout(3, 2));
            listFieldsPanel.add(new JLabel("Period:"));
            SessionSchedule courseSchedule = getSolution();
            List<Period> periodList = courseSchedule.getPeriodList();
            // Add 1 to array size to add null, which makes the entity unassigned
            JComboBox periodListField = new JComboBox(
                    periodList.toArray(new Object[periodList.size() + 1]));
            LabeledComboBoxRenderer.applyToComboBox(periodListField);
            periodListField.setSelectedItem(meeting.getPeriod());
            listFieldsPanel.add(periodListField);
            listFieldsPanel.add(new JLabel("Room:"));
            List<Room> roomList = courseSchedule.getRoomList();
            // Add 1 to array size to add null, which makes the entity unassigned
            JComboBox roomListField = new JComboBox(
                    roomList.toArray(new Object[roomList.size() + 1]));
            LabeledComboBoxRenderer.applyToComboBox(roomListField);
            roomListField.setSelectedItem(meeting.getRoom());
            listFieldsPanel.add(roomListField);
            listFieldsPanel.add(new JLabel("Locked:"));
            JCheckBox lockedField = new JCheckBox("immovable during planning");
            lockedField.setSelected(meeting.isLocked());
            listFieldsPanel.add(lockedField);
            int result = JOptionPane.showConfirmDialog(MusicAcademyPanel.this.getRootPane(), listFieldsPanel,
                    "Select period and room", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Period toPeriod = (Period) periodListField.getSelectedItem();
                if (meeting.getPeriod() != toPeriod) {
                    solutionBusiness.doChangeMove(meeting, "period", toPeriod);
                }
                Room toRoom = (Room) roomListField.getSelectedItem();
                if (meeting.getRoom() != toRoom) {
                    solutionBusiness.doChangeMove(meeting, "room", toRoom);
                }
                boolean toLocked = lockedField.isSelected();
                if (meeting.isLocked() != toLocked) {
                    if (solutionBusiness.isSolving()) {
                        logger.error("Not doing user change because the solver is solving.");
                        return;
                    }
                    meeting.setLocked(toLocked);
                }
                solverAndPersistenceFrame.resetScreen();
            }
        }

    }

}
