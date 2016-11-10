cd ..
cd ..
java -jar dist/MM-NEATv2.jar runNumber:%1 randomSeed:%1 base:tetris trials:5 maxGens:300 mu:50 io:true netio:true mating:true task:edu.utexas.cs.nn.tasks.rlglue.tetris.TetrisTask  rlGlueEnvironment:org.rlcommunity.environments.tetris.Tetris rlGlueExtractor:edu.utexas.cs.nn.tasks.rlglue.featureextractors.tetris.ExtendedBertsekasTsitsiklisTetrisExtractor tetrisTimeSteps:true tetrisBlocksOnScreen:false rlGlueAgent:edu.utexas.cs.nn.tasks.rlglue.tetris.TetrisAfterStateAgent splitRawTetrisInputs:true senseHolesDifferently:true hyperNEAT:false substrateMapping:edu.utexas.cs.nn.networks.hyperneat.BottomSubstrateMapping HNTTetrisProcessDepth:1 log:Tetris-HNSeedFeatures saveTo:HNSeedFeatures steps:500000 netLinkRate:0.0 netSpliceRate:0.0 linkExpressionThreshold:-1 hyperNEATSeedTask:edu.utexas.cs.nn.tasks.rlglue.tetris.HyperNEATTetrisTask extraHNLinks:true