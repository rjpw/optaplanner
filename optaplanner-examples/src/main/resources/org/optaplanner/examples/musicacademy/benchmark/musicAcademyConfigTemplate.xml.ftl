<?xml version="1.0" encoding="UTF-8"?>
<plannerBenchmark>
  <benchmarkDirectory>local/data/musicacademy/template</benchmarkDirectory>
  <parallelBenchmarkCount>AUTO</parallelBenchmarkCount>

  <inheritedSolverBenchmark>
    <problemBenchmarks>
      <xStreamAnnotatedClass>org.optaplanner.examples.musicacademy.domain.CourseSchedule</xStreamAnnotatedClass>
      <inputSolutionFile>data/musicacademy/unsolved/comp01.xml</inputSolutionFile>
    </problemBenchmarks>

    <solver>
      <solutionClass>org.optaplanner.examples.musicacademy.domain.SessionSchedule</solutionClass>
      <entityClass>org.optaplanner.examples.musicacademy.domain.Meeting</entityClass>
      <scoreDirectorFactory>
        <scoreDrl>org/optaplanner/examples/musicacademy/solver/musicAcademyScoreRules.drl</scoreDrl>
      </scoreDirectorFactory>
      <termination>
        <secondsSpentLimit>300</secondsSpentLimit>
      </termination>
      <constructionHeuristic>
        <constructionHeuristicType>FIRST_FIT_DECREASING</constructionHeuristicType>
      </constructionHeuristic>
    </solver>
  </inheritedSolverBenchmark>

  <#list [9] as entityTabuSize>
    <#list [900] as acceptedCountLimit>
      <solverBenchmark>
        <name>Entity Tabu ${entityTabuSize} (acceptedCount ${acceptedCountLimit})</name>
        <solver>
          <localSearch>
            <unionMoveSelector>
              <changeMoveSelector/>
              <swapMoveSelector>
                <filterClass>org.optaplanner.examples.musicacademy.solver.move.DifferentSessionSwapMoveFilter</filterClass>
              </swapMoveSelector>
            </unionMoveSelector>
            <acceptor>
              <entityTabuSize>${entityTabuSize}</entityTabuSize>
            </acceptor>
            <forager>
              <acceptedCountLimit>${acceptedCountLimit}</acceptedCountLimit>
            </forager>
          </localSearch>
        </solver>
      </solverBenchmark>
    </#list>
  </#list>
  <#list [600] as lateAcceptanceSize>
    <#list [4] as acceptedCountLimit>
      <solverBenchmark>
        <name>Late Acceptance ${lateAcceptanceSize} (acceptedCount ${acceptedCountLimit})</name>
        <solver>
          <localSearch>
            <unionMoveSelector>
              <changeMoveSelector/>
              <swapMoveSelector>
                <filterClass>org.optaplanner.examples.musicacademy.solver.move.DifferentSessionSwapMoveFilter</filterClass>
              </swapMoveSelector>
            </unionMoveSelector>
            <acceptor>
              <lateAcceptanceSize>${lateAcceptanceSize}</lateAcceptanceSize>
            </acceptor>
            <forager>
              <acceptedCountLimit>${acceptedCountLimit}</acceptedCountLimit>
            </forager>
          </localSearch>
        </solver>
      </solverBenchmark>
    </#list>
  </#list>
  <#list [200] as stepCountingHillClimbingSize>
    <#list [1] as acceptedCountLimit>
      <solverBenchmark>
        <name>Step Counting Hill Climbing ${stepCountingHillClimbingSize} (acceptedCount ${acceptedCountLimit})</name>
        <solver>
          <localSearch>
            <unionMoveSelector>
              <changeMoveSelector/>
              <swapMoveSelector>
                <filterClass>org.optaplanner.examples.musicacademy.solver.move.DifferentSessionSwapMoveFilter</filterClass>
              </swapMoveSelector>
            </unionMoveSelector>
            <acceptor>
              <stepCountingHillClimbingSize>${stepCountingHillClimbingSize}</stepCountingHillClimbingSize>
            </acceptor>
            <forager>
              <acceptedCountLimit>${acceptedCountLimit}</acceptedCountLimit>
            </forager>
          </localSearch>
        </solver>
      </solverBenchmark>
    </#list>
  </#list>
</plannerBenchmark>
