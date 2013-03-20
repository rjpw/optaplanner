/*
 * Copyright 2012 JBoss Inc
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

package org.optaplanner.core.heuristic.selector.move.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.optaplanner.core.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.heuristic.selector.common.decorator.FixedSelectorProbabilityWeightFactory;
import org.optaplanner.core.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.move.DummyMove;
import org.optaplanner.core.impl.phase.AbstractSolverPhaseScope;
import org.optaplanner.core.impl.phase.step.AbstractStepScope;
import org.optaplanner.core.solver.scope.DefaultSolverScope;
import org.junit.Test;

import static org.optaplanner.core.impl.testdata.util.PlannerAssert.*;
import static org.mockito.Mockito.*;

public class UnionMoveSelectorTest {

    @Test
    public void originSelection() {
        ArrayList<MoveSelector> childMoveSelectorList = new ArrayList<MoveSelector>();
        childMoveSelectorList.add(SelectorTestUtils.mockMoveSelector(
                DummyMove.class, new DummyMove("a1"), new DummyMove("a2"), new DummyMove("a3")));
        childMoveSelectorList.add(SelectorTestUtils.mockMoveSelector(
                DummyMove.class,new DummyMove("b1"), new DummyMove("b2")));
        UnionMoveSelector moveSelector = new UnionMoveSelector(childMoveSelectorList, false);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        moveSelector.solvingStarted(solverScope);
        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        moveSelector.phaseStarted(phaseScopeA);
        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        moveSelector.stepStarted(stepScopeA1);

        assertAllCodesOfEndingMoveSelector(moveSelector, "a1", "a2", "a3", "b1", "b2");

        moveSelector.stepEnded(stepScopeA1);
        moveSelector.phaseEnded(phaseScopeA);
        moveSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(childMoveSelectorList.get(0), 1, 1, 1);
        verifySolverPhaseLifecycle(childMoveSelectorList.get(1), 1, 1, 1);
    }

    @Test
    public void randomSelection() {
        ArrayList<MoveSelector> childMoveSelectorList = new ArrayList<MoveSelector>();
        Map<MoveSelector, Double> fixedProbabilityWeightMap = new HashMap<MoveSelector, Double>();
        childMoveSelectorList.add(SelectorTestUtils.mockMoveSelector(DummyMove.class,
                new DummyMove("a1"), new DummyMove("a2"), new DummyMove("a3")));
        fixedProbabilityWeightMap.put(childMoveSelectorList.get(0), 1000.0);
        childMoveSelectorList.add(SelectorTestUtils.mockMoveSelector(DummyMove.class,
                new DummyMove("b1"), new DummyMove("b2")));
        fixedProbabilityWeightMap.put(childMoveSelectorList.get(1), 20.0);
        UnionMoveSelector moveSelector = new UnionMoveSelector(childMoveSelectorList, true,
                new FixedSelectorProbabilityWeightFactory(fixedProbabilityWeightMap));

        Random workingRandom = mock(Random.class);
        when(workingRandom.nextDouble()).thenReturn(1.0 / 1020.0, 1019.0 / 1020.0, 1000.0 / 1020.0, 0.0, 999.0 / 1020.0);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getWorkingRandom()).thenReturn(workingRandom);
        moveSelector.solvingStarted(solverScope);
        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        when(phaseScopeA.getWorkingRandom()).thenReturn(workingRandom);
        moveSelector.phaseStarted(phaseScopeA);
        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        when(stepScopeA1.getWorkingRandom()).thenReturn(workingRandom);
        moveSelector.stepStarted(stepScopeA1);

        // A union of ending MoveSelectors does end, even with randomSelection
        assertAllCodesOfEndingMoveSelector(moveSelector, "a1", "b1", "b2", "a2", "a3");

        moveSelector.stepEnded(stepScopeA1);
        moveSelector.phaseEnded(phaseScopeA);
        moveSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(childMoveSelectorList.get(0), 1, 1, 1);
        verifySolverPhaseLifecycle(childMoveSelectorList.get(1), 1, 1, 1);
    }

}
