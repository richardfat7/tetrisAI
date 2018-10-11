package dk.itu.ai;

import org.apache.log4j.Logger;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.BackgroundStatus;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.gui.slick.RendererSlick;
import mu.nu.nullpo.util.CustomProperties;

/**
 * A proxy for the mu.nu.nullpo.game.play.GameManager class, costumized for training purposes (In-game Dojo Mode).
 * 
 * @author Oliver Phillip
 */
public class TrainingManager extends GameManager 
{
	/** Log (Apache log4j) */
	static Logger log = Logger.getLogger(TrainingManager.class);

	
	public TrainingManager(RendererSlick rendererSlick) {
		super(rendererSlick);
	}

	/**
	 * Initialize the game
	 */
	@Override
	public void init() {
		log.debug("TrainingManager init()");

		if(receiver == null) receiver = new EventReceiver();

		modeConfig = receiver.loadModeConfig();
		if(modeConfig == null) modeConfig = new CustomProperties();

		if(replayProp == null) {
			replayProp = new CustomProperties();
			replayMode = false;
		}

		replayRerecord = false;
		menuOnly = false;

		bgmStatus = new BGMStatus();
		backgroundStatus = new BackgroundStatus();

		int players = 1;
		if(mode != null) {
			mode.modeInit(this);
			players = mode.getPlayers();
		}
		engine = new GameEngine[players];
		for(int i = 0; i < engine.length; i++) engine[i] = new TrainingEngine(this, i);
	}
	
	/**
	 * Dispatches all render events to EventReceiver
	 */
	public void renderAll(boolean pause) {
		for(int i = 0; i < engine.length; i++) {
			try{
				TrainingEngine trainingEngine = (TrainingEngine) engine[i];
				trainingEngine.render(pause);
			} catch(Exception e)
			{
				engine[i].render();
			}
		}
	}
}
