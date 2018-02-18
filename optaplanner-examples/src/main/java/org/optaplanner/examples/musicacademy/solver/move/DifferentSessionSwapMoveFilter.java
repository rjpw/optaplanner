/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.musicacademy.solver.move;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.musicacademy.domain.SessionSchedule;
import org.optaplanner.examples.musicacademy.domain.Meeting;

public class DifferentSessionSwapMoveFilter implements SelectionFilter<SessionSchedule, SwapMove> {

    @Override
    public boolean accept(ScoreDirector<SessionSchedule> scoreDirector, SwapMove move) {
        Meeting leftMeeting = (Meeting) move.getLeftEntity();
        Meeting rightMeeting = (Meeting) move.getRightEntity();
        return !leftMeeting.getSession().equals(rightMeeting.getSession());
    }

}
