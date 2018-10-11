package dk.itu.ai.stimulus;

import com.anji.util.Properties;

import dk.itu.ai.Util;
import dk.itu.ai.navigation.Move;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;


/**
 * Stimulus set: 
 * 0-9: Column valley depths 
 * 10:  Highest block absolute y-coordinate 
 * 11:  Amount of empty holes below the block surface
 * 12-15:  Type of line clear, if any (Single, Double, Triple, or Tetris?) 
 * 
 * When using this class, set the property "stimulus.size" to 16
 * 
 * @author Kas
 */
public class AllAround2 implements StimulusGenerator {

	@Override
	public void init(Properties props) throws Exception {
		// Nothing to initialize here
	}
	
	@Override
	public double[] makeStimuli(GameEngine engine, Move move) {
		Field field = Util.getFieldAfter(engine, move);
		
		double[] stimuli = makeStimuli(field);
		
		return(stimuli);
	}

	private double[] makeStimuli(Field field) {
		double[] result = new double[16];
		
		// Process potetial line clears
		int linesCleared = field.checkLine();  
		if (linesCleared > 0) {
			field.clearLine();
			field.downFloatingBlocks();
		}
		
		for (int x = 0; x < 10; x++) {
			result[x] = field.getValleyDepth(x);
		}
		
		
		// Pass the total height as well
		result[10] = field.getHighestBlockY();
		
		result[11] = field.getHowManyHoles();
		
		switch (linesCleared) {
		case 1: result[12] = 1.0; break;
		case 2: result[13] = 1.0; break;
		case 3: result[14] = 1.0; break;
		case 4: result[15] = 1.0; break;
		default: break;
			
		}
		
		return result;
	
	}
}
