java -jar dist/MM-NEATv2.jar runNumber:%1 randomSeed:%1 base:torus trials:8 maxGens:100 mu:100 io:true netio:true mating:false fs:false task:edu.utexas.cs.nn.tasks.gridTorus.TorusEvolvedPredatorsVsStaticPreyTask log:CCQPredVsStaticPreyTeam-Control saveTo:Control allowDoNothingActionForPredators:true torusPreys:2 torusPredators:3 staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController PredatorMinimizeTotalTime:false predatorCatch:true predatorMinimizeDistance:true PredsEatEachPreyQuickly:true torusSenseTeammates:true