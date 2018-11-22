package edu.cuhk.cse.fyp.tetrisai.lspi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.newdawn.slick.util.Log;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameEngine.Status;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.ai.BasicAI;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.subsystem.mode.GameMode;
import mu.nu.nullpo.game.subsystem.mode.VSBattleMode;
import mu.nu.nullpo.gui.slick.LogSystemLog4j;
import mu.nu.nullpo.gui.slick.RendererSlick;
import mu.nu.nullpo.util.GeneralUtil;

public class Battler {

	// Run for this amount of round
	private static final int RUN_ESP = 1;
	
	// Each round run for this amount of simulation and then save Qtable
	private static final int RUN_STEP = 100;
	
	private GameManager gameManager;
	private GameEngine[] gameEngine;
	private EventReceiver receiver;
	private String customSeed = null;
	
	/**
	 * Get the current in-game grade from a game of Tetris The Grand Master 3: Terror Instinct.
	 * 
	 * @return The TGM3 grade.
	 */
//	public int getGM3Grade(){
//		VSBattleMode gm3m = (VSBattleMode) gameEngine.owner.mode;
//		Field field;
//		try {
//			field = gm3m.getClass().getDeclaredField("grade");
//			field.setAccessible(true);
//			return field.getInt(gm3m);
//		} catch (NoSuchFieldException | SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
//		
//		return -1;
//	}
	
	/**
	 * Get the current line cleared.
	 * 
	 * @return The line cleared
	 */
	public int getLineCleared(GameEngine gameEngine){
		VSBattleMode aitm = (VSBattleMode) gameEngine.owner.mode;
		Field field;
		try {
			field = aitm.getClass().getField("getGarbageSent");
			//getDeclaredField("garbageSent");
			//return field.getInt(aitm);
			//return field(aitm);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * Get the current in-game level.
	 * 
	 * @return The level.
	 */
//	public int getLevel() {
//		return gameEngine.statistics.level;
//	}
	
	/**
	 * Make a new Simulator object, ready to go.
	 * 
	 * @param mode Game mode Object
	 * @param rulePath path string to the rules file
	 * @param ai AI object to play (MAKE SURE IT SUPPORTS UNTHREADED !!! )
	 */
//	public Battler(GameMode mode, String rulePath, DummyAI ai)
//	{
//		this(mode, GeneralUtil.loadRule(rulePath), ai);
//	}
	
	/**
	 * Make a new Simulator object, ready to go.
	 * 
	 * @param mode Game mode Object
	 * @param rulePath path string to the rules file
	 * @param ai AI object to play (MAKE SURE IT SUPPORTS UNTHREADED !!! )
	 */
	public Battler(GameMode mode, String rulePath, DummyAI ai, DummyAI ai2)
	{
		this(mode, GeneralUtil.loadRule(rulePath), ai, ai2);
	}
	
	/**
	 * Make a new Simulator object, ready to go.
	 * @param mode Game mode Object
	 * @param rules Game rules Object
	 * @param ai AI object to play (MAKE SURE IT SUPPORTS UNTHREADED !!! )
	 */
	/*
	public Battler(GameMode mode, RuleOptions rules, DummyAI ai)
	{
		// UI
		//receiver = new RendererSlick();
		
		// Manager setup
		gameManager = new GameManager();
		
		gameManager.mode = mode;
		
		gameManager.init();
		
		gameManager.showInput = false;
		
		// Engine setup
		gameEngine = gameManager.engine[0]; 
		
		// - Rules
		gameEngine.ruleopt = rules;
		
		// - Randomizer
		gameEngine.randomizer = GeneralUtil.loadRandomizer(rules.strRandomizer);

		// - Wallkick	
		gameEngine.wallkick = GeneralUtil.loadWallkick(rules.strWallkick);
		
		// - AI
		gameEngine.ai = ai;
		gameEngine.aiMoveDelay = 0;
		gameEngine.aiThinkDelay = 0;
		gameEngine.aiUseThread = false;
		gameEngine.aiShowHint = false;
		gameEngine.aiPrethink = false;
		gameEngine.aiShowState = false;
	}
	*/
	
	/**
	 * Make a new Simulator object, ready to go.
	 * @param mode Game mode Object
	 * @param rules Game rules Object
	 * @param ai AI object to play (MAKE SURE IT SUPPORTS UNTHREADED !!! )
	 */
	public Battler(GameMode mode, RuleOptions rules, DummyAI ai, DummyAI ai2)
	{
		/* 
		 * NOTE(oliver): This code is a domain-specific version of 
		 * mu.nu.nullpo.gui.slick.StateInGame.startNewGame(String strRulePath)
		 */
		
		// UI
		//receiver = new RendererSlick();
		
		// Manager setup
		gameManager = new GameManager();
		
		gameManager.mode = mode;
		
		gameManager.init();
		
		gameManager.showInput = false;
		
		gameEngine = new GameEngine[2];
		
		for (int i = 0; i < 2; i++) {
			// Engine setup
			gameEngine[i] = gameManager.engine[i]; 
			
			// - Rules
			gameEngine[i].ruleopt = rules;
			
			// - Randomizer
			gameEngine[i].randomizer = GeneralUtil.loadRandomizer(rules.strRandomizer);
	
			// - Wallkick	
			gameEngine[i].wallkick = GeneralUtil.loadWallkick(rules.strWallkick);
			
			// - AI
			gameEngine[i].ai = i == 0 ? ai : ai2;
			gameEngine[i].aiMoveDelay = 0;
			gameEngine[i].aiThinkDelay = 0;
			gameEngine[i].aiUseThread = false;
			gameEngine[i].aiShowHint = false;
			gameEngine[i].aiPrethink = false;
			gameEngine[i].aiShowState = false;
		}
	}

	/**
	 * Performs a single simulation to completion (STATE == GAMEOVER)
	 */
	public void runSimulation() 
	{
		for (int i = 0; i < 2; i++) {
		// Start a new game.
			gameEngine[i].init();
		
	//		System.out.println(Long.toString(gameEngine.randSeed, 16));
			if(customSeed != null)
			{
				gameEngine[i].randSeed = Long.parseLong(customSeed, 16);
				log.debug("Engine seed overwritten with " + customSeed);
			}
		}
		
		// You have to spend at least 5 frames in the menu before you can start the game.
		for(int i = 0; i < 10; ++i)
		{
			for (int j = 0; j < 2; j++) {
				gameEngine[j].update();
			}
		}
		
		// Press and release A to start game.
		for (int i = 0; i < 2; i++)
			gameEngine[i].ctrl.setButtonPressed(Controller.BUTTON_A); 
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 10; j++) {
				gameEngine[i].update();
			}
		for (int i = 0; i < 2; i++)
			gameEngine[i].ctrl.setButtonUnpressed(Controller.BUTTON_A);

		for (int i = 0; i < 2; i++) {
			while (gameEngine[i].stat != Status.MOVE)
				gameEngine[i].update();
		}
		
		// Run the game until Game Over.
		while (gameEngine[0].stat != Status.GAMEOVER && gameEngine[1].stat != Status.GAMEOVER) {
			for (int i = 0; i < 2; i++) {
				Piece cur = gameEngine[i].nowPieceObject;
				do {
					gameEngine[i].update();
					if (gameEngine[0].stat == Status.GAMEOVER || gameEngine[1].stat == Status.GAMEOVER)
						break;
				} while (cur == gameEngine[i].nowPieceObject);
				int a = 1;
			}
//			logGameState();
		}
		

//		log.info("Game is over!");
//		log.info("Final Level: " + gameEngine.statistics.level);
	}
	
	/**
	 * Performs multiple sequential simulations to completion (STATE == GAMEOVER).
	 * 
	 * @param count The number of simulations.
	 */
	public void runSimulations(int count) 
	{
		for(int i = 1; i <= count; ++i)
		{
			log.info(String.format("-------- Simulation %d of %d --------", i, count));
			runSimulation();
		}
		//gameEngine.ai.shutdown(gameEngine, 0);
	}

	/**
	 * Performs multiple sequential simulations to completion (STATE == GAMEOVER),
	 * and records statistics.
	 * 
	 * @param count The number of simulations.
	 */
	public void runStatisticsSimulations(int esp, int step) 
	{
		int[] totalLines = new int[2];
		
		for (int i = 0; i < 2; i++)
			totalLines[i] = 0;
		
		int count = esp * step;
		
		int[] winnerCount = new int[2];
		for (int i = 0; i < 2; i++)
			winnerCount[i] = 0;
	
		for(int i = 0; i < count; i++)
		{
			log.info(String.format("-------- Simulation %d of %d --------", i+1, count));
			runSimulation();
			//if (getGM3Grade() == 32) {
			//	gm++;
			//}
			
			int winner;
			if (gameEngine[0].stat != Status.GAMEOVER && gameEngine[1].stat == Status.GAMEOVER) {
				winner = 0;
				winnerCount[0] += 1;
			} else if (gameEngine[1].stat != Status.GAMEOVER && gameEngine[0].stat == Status.GAMEOVER) {
				winner = 1;
				winnerCount[1] += 1;
			} else
				winner = -1;
			log.info("Winner:\t" + winner);
			for (int j = 0; j < 2; j++)
				log.info("win count " + j + ":\t" + winnerCount[j]);
			for (int j = 0; j < 2; j++)
				log.info("win count rate " + j + ":\t" + (double)(winnerCount[j]) / (i+1));
			for (int j = 0; j < 2; j++) {
				//int thisLines = getLineCleared(gameEngine[j]);
				log.info("Player " + j);
				int thisLines = ((VSBattleMode)(gameManager.mode)).getGarbageSent()[j];
				totalLines[j] += thisLines;
				log.info("Line cleared:\t" + thisLines);
				log.info("-------- stat till now --------");
				log.info("Total line cleared:\t" + totalLines[j]);
				log.info("Line per game:\t" + String.valueOf((double)(totalLines[j]) / (i + 1)));
			}
//			if (i % step == 0) {
//				PyAI pyai = (PyAI) gameEngine.ai;
//				pyai.invoke("sys.getsizeof(ai.qtable)");
//				pyai.invoke("ai.saveQTable()");
//			}
		}
		
		log.info("-------- COMPLETE --------");
		
		log.info("Total:\t" + count);
		log.info("Total line cleared:\t" + totalLines);
		//log.info("Line per game:\t" + String.valueOf(totalLines / count));
		
		//double prop = (double) totalLines / (double) count;
		
		//log.info("Sample Proportion:\t" + prop);
		
		double confidenceMultiplier = 1.96;
		
		//double interval = confidenceMultiplier * Math.sqrt(prop * (1 - prop)/ count );
		
		//log.info("Confidence interval:\t" + interval);
	}
	
	/**
	 * Sets a custom random seed for use in simulations. 
	 * 
	 * @param seed The seed.
	 */
	public void setCustomSeed(String seed) 
	{
		customSeed = seed;
	}

	static Logger log = Logger.getLogger(Battler.class);
	
	/**
	 * Log the current game state.
	 */
//	private void logGameState()
//	{
//		int level = gameEngine.statistics.level;
//		
//		String piece = 
//			gameEngine.nowPieceObject == null 
//				? " " 
//				: Piece.PIECE_NAMES[gameEngine.nowPieceObject.id];
//		
//		String state = gameEngine.stat.toString();
//		
//		log.info(String.format("\tLevel: %3d \tPiece: %s \tState: %s", level, piece, state));
//	}

	public static void main(String[] args) {
		
		// Logger initialization.
		PropertyConfigurator.configure("config/etc/log_slick.cfg");
		Log.setLogSystem(new LogSystemLog4j());
		
		// NOTE(oliver): For other GameModes, look inside src/mu/nu/nullpo/game/subsystem/mode
		GameMode mode = 
			new VSBattleMode();
		
		
		// NOTE(oliver): For other rules, look inside config/rule.
		String rulePath = 
			"config/rule/StandardAITraining.rul";
		
		
		// NOTE(oliver): For other AIs, look inside src/mu/nu/nullpo/game/subsystem/ai, or src/dk/itu/ai
		DummyAI ai = 
				new LSPIAI();
		DummyAI ai2 = 
				new mu.nu.nullpo.game.subsystem.ai.BasicAI();
				//new net.tetrisconcept.poochy.nullpomino.ai.PoochyBot();
		
		
		// Actual simulation.
		Battler battler = new Battler(mode, rulePath, ai, ai2);
		
		// Custom seeding.
		
//		String customSeed = 
//			"-2fac0ecd9c988463"
//			"15478945"
//			"897494638"
//			"4697358"
//			;
		
//		simulator.setCustomSeed(customSeed);
		
//		simulator.runSimulation();
		battler.runStatisticsSimulations(RUN_ESP, RUN_STEP);
	}
}
