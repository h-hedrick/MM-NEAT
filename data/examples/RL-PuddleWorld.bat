cd ..
cd ..
java -jar dist/MONE.jar runNumber:0 base:puddle trials:1 maxGens:500 mu:50 io:true netio:true mating:true task:edu.utexas.cs.nn.tasks.rlglue.RLGlueTask cleanOldNetworks:false fs:false noisyTaskStat:edu.utexas.cs.nn.util.stats.Average log:RL-Puddle saveTo:Puddle rlGlueEnvironment:org.rlcommunity.environments.puddleworld.PuddleWorld