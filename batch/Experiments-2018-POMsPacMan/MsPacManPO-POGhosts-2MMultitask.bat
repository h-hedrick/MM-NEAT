cd ..
cd ..
java -jar target/MM-NEAT-0.0.1-SNAPSHOT.jar runNumber:%1 randomSeed:%1 base:poghosts maxGens:200 mu:100 io:true netio:true mating:true task:edu.southwestern.tasks.mspacman.MsPacManTask highLevel:true infiniteEdibleTime:false imprisonedWhileEdible:false pacManLevelTimeLimit:8000 pacmanInputOutputMediator:edu.southwestern.tasks.mspacman.sensors.mediators.po.POCheckEachDirectionMediator trials:10 log:POGhosts-3MMultitask saveTo:3MMultitask fs:false edibleTime:200 trapped:true specificGhostEdibleThreatSplit:true specificGhostProximityOrder:true specific:false multitaskModes:2 pacmanMultitaskScheme:edu.southwestern.tasks.mspacman.multitask.po.POProbableGhostStateModeSelector2Mod perLinkMutateRate:0.05 netLinkRate:0.4 netSpliceRate:0.2 crossoverRate:0.5 partiallyObservablePacman:true pacmanPO:true useGhostModel:true usePillModel:true probabilityThreshold:0.125 ghostPO:true rawScorePacMan:true
