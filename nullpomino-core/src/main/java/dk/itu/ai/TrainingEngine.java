package dk.itu.ai;

import org.apache.log4j.Logger;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

/**
 * A proxy for the mu.nu.nullpo.game.play.GameEngine class, costumized for training purposes (In-game Dojo Mode).
 * 
 * @author Oliver Phillip
 */
public class TrainingEngine extends GameEngine {
	
	/** Log (Apache log4j) */
	static Logger log = Logger.getLogger(TrainingEngine.class);

	public TrainingEngine(GameManager owner, int playerID) {
		super(owner, playerID);
	}

	public TrainingEngine(GameManager owner, int playerID, RuleOptions ruleopt, Wallkick wallkick,
			Randomizer randomizer) {
		super(owner, playerID, ruleopt, wallkick, randomizer);
	}

	private void renderStat() {
		// 各ステータスの処理
		switch (stat) {
		case NOTHING:
			break;
		case SETTING:
			if (owner.mode != null)
				owner.mode.renderSetting(this, playerID);
			owner.receiver.renderSetting(this, playerID);
			break;
		case READY:
			if (owner.mode != null)
				owner.mode.renderReady(this, playerID);
			owner.receiver.renderReady(this, playerID);
			break;
		case MOVE:
			if (owner.mode != null)
				owner.mode.renderMove(this, playerID);
			owner.receiver.renderMove(this, playerID);
			break;
		case LOCKFLASH:
			if (owner.mode != null)
				owner.mode.renderLockFlash(this, playerID);
			owner.receiver.renderLockFlash(this, playerID);
			break;
		case LINECLEAR:
			if (owner.mode != null)
				owner.mode.renderLineClear(this, playerID);
			owner.receiver.renderLineClear(this, playerID);
			break;
		case ARE:
			if (owner.mode != null)
				owner.mode.renderARE(this, playerID);
			owner.receiver.renderARE(this, playerID);
			break;
		case ENDINGSTART:
			if (owner.mode != null)
				owner.mode.renderEndingStart(this, playerID);
			owner.receiver.renderEndingStart(this, playerID);
			break;
		case CUSTOM:
			if (owner.mode != null)
				owner.mode.renderCustom(this, playerID);
			owner.receiver.renderCustom(this, playerID);
			break;
		case EXCELLENT:
			if (owner.mode != null)
				owner.mode.renderExcellent(this, playerID);
			owner.receiver.renderExcellent(this, playerID);
			break;
		case GAMEOVER:
			log.debug(stat);
			if (owner.mode != null)
				owner.mode.renderGameOver(this, playerID);
			owner.receiver.renderGameOver(this, playerID);
			break;
		case RESULT:
			if (owner.mode != null)
				owner.mode.renderResult(this, playerID);
			owner.receiver.renderResult(this, playerID);
			break;
		case FIELDEDIT:
			if (owner.mode != null)
				owner.mode.renderFieldEdit(this, playerID);
			owner.receiver.renderFieldEdit(this, playerID);
			break;
		case INTERRUPTITEM:
			break;
		}
	}

	public void render(boolean pause) {
		// 最初の処理
		owner.receiver.renderFirst(this, playerID);
		if (owner.mode != null)
			owner.mode.renderFirst(this, playerID);

		if (rainbowAnimate)
			Block.updateRainbowPhase(this);

		renderStat();
		
		if (owner.showInput) {
			if (owner.mode != null)
				owner.mode.renderInput(this, playerID);
			owner.receiver.renderInput(this, playerID);
		}
		if (ai != null) {
			if (aiShowState)
				ai.renderState(this, playerID);
			if (aiShowHint)
				ai.renderHint(this, playerID);
		}

		// 最後の処理
		if (owner.mode != null)
			owner.mode.renderLast(this, playerID);
		owner.receiver.renderLast(this, playerID);
	}

}
