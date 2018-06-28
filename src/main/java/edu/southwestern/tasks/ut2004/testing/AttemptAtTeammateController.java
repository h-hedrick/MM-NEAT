package edu.southwestern.tasks.ut2004.testing;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Senses;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.collections.MyCollections;
import edu.southwestern.tasks.ut2004.actions.BotAction;
import edu.southwestern.tasks.ut2004.actions.EmptyAction;
import edu.southwestern.tasks.ut2004.actions.FollowTeammateAction;
import edu.southwestern.tasks.ut2004.actions.NavigateToLocationAction;
import edu.southwestern.tasks.ut2004.actions.OldActionWrapper;
import edu.southwestern.tasks.ut2004.controller.BotController;
import edu.southwestern.tasks.ut2004.controller.RandomItemPathExplorer;
import edu.southwestern.tasks.ut2004.controller.behaviors.AttackEnemyAloneModule;
import edu.utexas.cs.nn.Constants;
import edu.utexas.cs.nn.weapons.WeaponPreferenceTable;
import mockcz.cuni.pogamut.Client.AgentMemory;
import utopia.agentmodel.actions.ApproachEnemyAction;
import utopia.agentmodel.actions.QuickTurnAction;
import utopia.controllers.scripted.ChasingController;

public class AttemptAtTeammateController implements BotController {
	
	AttackEnemyAloneModule attackAlone = new AttackEnemyAloneModule();
	RandomItemPathExplorer runAround = new RandomItemPathExplorer();
	AgentMemory memory;
	
	private ChasingController chaseController;
	
	public static final int FULL_HEALTH = 100;
	public static final int THRESHOLD_HEALTH_LEVEL = 20;
	//var timesteps teammate hasn't moved
	/**
	 * 
	 */
	public BotAction control(@SuppressWarnings("rawtypes") UT2004BotModuleController bot) {//loops thourhg over and over again
		Player nearestFriend = bot.getPlayers().getNearestVisibleFriend();
		Player lastSeenFriend = bot.getPlayers().getNearestFriend(10);
		Player nearestEnemy = bot.getPlayers().getNearestVisibleEnemy();
		Player lastSeenEnemy = bot.getPlayers().getNearestEnemy(10);
		
		//bot should look for health pickups if it drops below 20hp
		if(bot.getBot().getSelf().getHealth() < (THRESHOLD_HEALTH_LEVEL)) {
			//tell bot to abandon whatever it's doing and go find health
			Location getHealth =  bot.getItems().getNearestItem(ItemType.Category.HEALTH).getLocation();
			return new NavigateToLocationAction(getHealth);
		}
		
		//start randomly running around to get items
		if(nearestEnemy == null && nearestFriend == null) {
			return runAround.control(bot);
		}
		
		if(bot.getSenses().isBeingDamaged() && nearestEnemy == null) {
			return new OldActionWrapper(new QuickTurnAction(OldActionWrapper.getAgentMemory(bot)));
		}
		
		
		if(nearestEnemy !=  null && nearestFriend == null) {
//			if(shouldChase(nearestEnemy, bot)) {
			return new OldActionWrapper(new ApproachEnemyAction(OldActionWrapper.getAgentMemory(bot), true, true, false, true));
//			}
			//return attackAlone.control(bot);
		}
		
//		if(nearestEnemy == null && lastSeenEnemy != null) {
//			
//		}
		
		if(nearestFriend != null) { //do you see your friend?
			return new FollowTeammateAction(nearestFriend);
		}
//
		if(attackAlone.trigger(bot)) {
			return attackAlone.control(bot);
		}
//		return new EmptyAction();	
		return runAround.control(bot);
	}
	


	
	/**
	 * initializes the controller
	 */
	public void initialize(@SuppressWarnings("rawtypes") UT2004BotModuleController bot) {
		memory = OldActionWrapper.getAgentMemory(bot);
		//this.chaseController = new ChasingController(bot.getBot(), memory);
	}

	
	
	/**
	 * resets the controller
	 */
	public void reset(@SuppressWarnings("rawtypes") UT2004BotModuleController bot) {
	}

}
