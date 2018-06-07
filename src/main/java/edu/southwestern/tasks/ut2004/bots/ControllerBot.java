package edu.southwestern.tasks.ut2004.bots;

import cz.cuni.amis.pogamut.base.agent.params.IRemoteAgentParameters;
import cz.cuni.amis.pogamut.base.utils.Pogamut;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.utils.UT2004BotDescriptor;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.server.IUT2004Server;
import cz.cuni.amis.pogamut.ut2004.utils.MultipleUT2004BotRunner;
import cz.cuni.amis.pogamut.ut2004.utils.PogamutUT2004Property;
import cz.cuni.amis.utils.exception.PogamutException;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.tasks.ut2004.actions.BotAction;
import edu.southwestern.tasks.ut2004.controller.BotController;
import edu.southwestern.tasks.ut2004.controller.DummyController;
import edu.southwestern.tasks.ut2004.controller.RandomNavPointPathExplorer;
import edu.southwestern.tasks.ut2004.server.BotKiller;
import edu.utexas.cs.nn.bots.UT2;
import fr.enib.mirrorbot4.MirrorBot4;
import fr.enib.mirrorbot4.MirrorBotParameters;
import pogamut.hunter.HunterBot;
import pogamut.hunter.HunterBotParameters;
import edu.southwestern.tasks.ut2004.bots.MultiBotLauncher;

/**
 * A specific type of UT2004 that is controlled by a BotController "brain"
 * that returns a BotAction given the current bot state.
 * 
 * @author Jacob Schrum
 */
@SuppressWarnings("rawtypes")
@AgentScoped
public class ControllerBot extends UT2004BotModuleController {

	/**
	 * Controller for bot
	 */
	private BotController brain;

	/**
	 * This method returns the parameters of the bot, to be used. It is using
	 * {@link UT2004Bot#getParams()} and casts them to
	 * {@link CustomBotParameters} that is, this bot can't be used with
	 * different parameters (it will screw up).
	 *
	 * @return
	 */
	public ControllerBotParameters getParams() {
		// notice the cast to CustomBotParameters
		// this method will fail if you do not start the bot with
		// CustomBotParameters (which compiles, but fails during runtime)
		return (ControllerBotParameters) bot.getParams();
	}

	/**
	 * Here we can modify initializing command for our bot, e.g., sets its name
	 *
	 * @return instance of {@link Initialize}
	 */
	@Override
	public Initialize getInitializeCommand() {
		ControllerBotParameters params = getParams();
		brain = params.getController();
		// Listerners on these objects needed to track scores
		params.getStats().registerListeners(this);
		return new Initialize().setName(params.getName()).setDesiredSkill(params.getDesiredSkill());
	}

	/**
	 * ends the evaluation period for the bot and retrieves its fitnesses
	 */
	public void endEval() {
		getParams().getStats().endEval(this);
		BotKiller.killBot(bot);
	}

	@Override
	/**
	 * initializes the brain with the game data
	 * @param info (data feedback from the game)
	 * @param currentConfig (the current configuration of the bot being sent to the server)
	 * @param init (initial message sent to the server)
	 */
	public void botInitialized(GameInfo info, ConfigChange currentConfig, InitedMessage init) {
		brain.initialize(this);
	}

	@Override
	/**
	 * assigns actions for the bot to execute
	 */
	public void logic() throws PogamutException {
		if (game.getTime() > getParams().getEvalSeconds()) {
			endEval();
		}
		// Consult brain and act
		BotAction action = brain.control(this);
		String description = action.execute(this);
		if(Parameters.parameters == null || Parameters.parameters.booleanParameter("utBotLogOutput")) {
			System.out.println(getParams().getBotPort() + ":" + getInfo().getName() + ":" + description);
		}
	}

	@Override
	/**
	 * resets the brain and the bot when it is killed
	 */
	public void botKilled(BotKilled event) {
		brain.reset(this);
	}

	/**
	 * Launches a bot onto the provided host:port using the given network brain
	 * to control it
	 *
	 * @param server
	 *            Server instance
	 * @param name
	 *            In-game name of bot
	 * @param controllers
	 *            Bot controllers to run in game
	 * @param evalSeconds 
	 *            Number of seconds to spend in evaluation
	 * @param desiredSkill
	 *            Skill parameter for bots (affects accuracy)
	 * @param host
	 *            host of pre-loaded server
	 * @param botPort
	 *            port on server to connect bot
	 * @return Array of game data about each controller in game
	 * @throws PogamutException
	 */
	public static GameDataCollector[] launchBot(IUT2004Server server, String[] names, BotController[] controllers,
			int evalSeconds, int desiredSkill, String host, int botPort) {
		GameDataCollector[] collectors = new GameDataCollector[controllers.length];
		int numHunterBots = Parameters.parameters.integerParameter("numHunterBots");
		int numMirrorBots = Parameters.parameters.integerParameter("numMirrorBots");
		int numUT2Bots = Parameters.parameters.integerParameter("numUT2Bots");
		int totalBots = controllers.length + numHunterBots + numMirrorBots;
		IRemoteAgentParameters[] params = new IRemoteAgentParameters[totalBots];
		Class[] classes = new Class[totalBots];

		//adds all ControllerBots
		for (int i = 0; i < controllers.length; i++) {
			classes[i] = ControllerBot.class;
			collectors[i] = new GameDataCollector();
			params[i] = new ControllerBotParameters(server, controllers[i], names[i], collectors[i], evalSeconds,
					desiredSkill, botPort);
		}

		//adds hunter bots to the spaces in the array after ControllerBots
		for(int i = 0; i < numHunterBots; i++) {
			classes[i + controllers.length] = HunterBot.class;
			params[i + controllers.length] = new HunterBotParameters(evalSeconds); // HunterBot also needs to know when to stop
		}
		
		//adds mirror bots to the spaces in the array after ControllerBots
		for(int i = 0; i < numMirrorBots; i++) {
			classes[i + controllers.length] = MirrorBot4.class;
			params[i + controllers.length] = new MirrorBotParameters(evalSeconds); // MirrorBot also needs to know when to stop
		}
		
		//adds UT^2 bots to the spaces in the array after ControllerBots
		for(int i = 0; i < numUT2Bots; i++) {
			classes[i + controllers.length] = UT2.class;
			params[i + controllers.length] = new UT2.UT2Parameters(evalSeconds); // UT2 also needs to know when to stop
		}

		// This method still has some problems and causes weird exceptions sometimes
		MultiBotLauncher.launchMultipleBots(classes, params, host, botPort);

		// Old version of this code
		//		try {
		//			MultipleUT2004BotRunner multi = new MultipleUT2004BotRunner("MultipleBots").setHost(host).setPort(botPort);
		//			@SuppressWarnings("unchecked")
		//			UT2004BotDescriptor bots = new UT2004BotDescriptor().setController(ControllerBot.class).setAgentParameters(params);
		//			multi.setMain(true).startAgents(bots);
		//		} catch (PogamutException e) {
		//			// Obligatory exception that happens from stopping the bot
		//			// System.out.println("Exception after evaluation");
		//			// e.printStackTrace();
		//		}		

		//		System.out.println("Match over: ");
		//		for (int i = 0; i < collectors.length; i++) {
		//			System.out.println("\t" + collectors[i]);
		//		}
		return collectors;
	}

	@SuppressWarnings({ "unchecked" })
	public static void main(String[] args) {
		int port = Pogamut.getPlatform().getIntProperty(PogamutUT2004Property.POGAMUT_UT2004_BOT_PORT.getKey());
		MultipleUT2004BotRunner multi = new MultipleUT2004BotRunner("TEST").setHost("localhost").setPort(port);
		ControllerBotParameters b1 = new ControllerBotParameters(null, new RandomNavPointPathExplorer(),
				"ExplorationBot", new GameDataCollector(), 500, 1, port);
		ControllerBotParameters b2 = new ControllerBotParameters(null, new DummyController(), "DummyBot",
				new GameDataCollector(), 500, 1, port);
		UT2004BotDescriptor test = new UT2004BotDescriptor().setController(ControllerBot.class)
				.setAgentParameters(new IRemoteAgentParameters[] { b1, b2 });
		multi.setMain(true).startAgents(test);
		// GameDataCollector data = launchBot(null, "DummyBot", new
		// DummyController(), 500, 1, "localhost", port);
		// System.out.println(data);
	}
}
