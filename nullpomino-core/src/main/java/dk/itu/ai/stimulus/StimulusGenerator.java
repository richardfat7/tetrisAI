package dk.itu.ai.stimulus;

import com.anji.util.Configurable;

import dk.itu.ai.navigation.Move;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * Interface for Stimulus generator objects, for use with the NeatAI
 * 
 * This interface extends com.anji.util.Configurable. 
 * If implementing this interface, be sure to read the requirements of Configurable interface as well.
 * 
 * @author Kas
 */
public interface StimulusGenerator extends Configurable { 
	
	/**
	 * Makes an array of stimuli when given a game engine and a move to be considered.
	 * 
	 * @param engine The current game engine.
	 * @param move The potential move.
	 * @return The stimuli.
	 */
	public double[] makeStimuli(GameEngine engine, Move move);
}