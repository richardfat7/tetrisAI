package dk.itu.ai.stimulus;

import com.anji.util.Properties;

import dk.itu.ai.navigation.Move;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * Stimulus set: 
 * 0-2: Are there neighbors to the left, right and/or top? (0/1)
 * 3: Line clears (0,1,2,3,4 maps to 0, 2, 12, 13, 30 respectively - then normalized)
 * 4: Is this an allClear? (0/1)
 * 5: Are we in danger aka. new height is >= 12 (0/1)
 * 6: How many holes have appeared/disappeared? (normalized, should be within 0 and 1)
 * 7: How many lids have appeared/disappeared? (normalized, should be within 0 and 1)
 * 8: How many valley's needing an I-piece have appeared/disappeared? (normalized, should be within 0 and 1)
 * 9: How much has the height increased/decreased? (normalized, should be within 0 and 1)
 * 10: Combo value (normalized, should be within 0 and 1) (note: not sure about upper and lower bound, so normalization may not be entirely correc
 * 
 * When using this class, set the property "stimulus.size" to 11.
 * 
 * @author Oliver Phillip
 */
public class BasicStimulus4 implements StimulusGenerator {

	@Override
	public void init(Properties props) throws Exception {
		// TODO Auto-generated method stub		
	}
	
	private double asDouble(boolean b)
	{
		double result = b ? 1 : 0;
		
		return(result);
	}
	
	private double asDouble(int i, int min, int max)
	{
		double result;
		
		if(min > max)
		{
			result = asDouble(i, max, min);
		}
		else
		{
			result = ((double)i) / ((double)(max - min));
		}
		
		return(result);
	}

	@Override
	public double[] makeStimuli(GameEngine engine, Move move) {
		
		double[] stimuli = new double[11];
		
		Field fld = new Field(engine.field);
		Piece piece = new Piece(move.piece);
		int x = move.x;
		int y = move.y;
		int rt = move.rotation;
		
		// Features of the old field.
		
		// -- Adjacency checks
		boolean neighborOnLeft = piece.checkCollision(x - 1, y, fld);
		stimuli[0] = asDouble(neighborOnLeft);
		
		boolean neighborOnRight = piece.checkCollision(x + 1, y, fld);
		stimuli[1] = asDouble(neighborOnRight);
		
		boolean neighborOnTop = piece.checkCollision(x, y - 1, fld);
		stimuli[2] = asDouble(neighborOnTop);
		
		
		// -- Number of holes and valleys needing an I piece (before placement)
		int holeBefore = fld.getHowManyHoles();
		int lidBefore = fld.getHowManyLidAboveHoles();
		int needIValleyBefore = fld.getTotalValleyNeedIPiece();
		
		// -- Field height (before clears)
		int heightBefore = fld.getHighestBlockY();
		
		// Place the piece onto the field
		piece.placeToField(x, y, rt, fld);
		
		// Features of the new field.
		
		// -- Line clears
		int lines = fld.checkLine();
		
		int igp = 0;
		
		switch(lines)
		{
			case 1: { igp =  2; } break;
			case 2: { igp = 12; } break;
			case 3: { igp = 13; } break;
			case 4: { igp = 30; } break;
		}
		
		stimuli[3] = asDouble(igp, 0, 30);
		
		if(lines > 0) 
		{
			fld.clearLine();
			fld.downFloatingBlocks();
		}
		
		// -- All clear
		boolean allclear = fld.isEmpty();
		stimuli[4] = asDouble(allclear);
		
		// -- Field height (after clears)
		int heightAfter = fld.getHighestBlockY();
		
		// -- Danger flag
		boolean danger = (heightAfter <= 12);
		stimuli[5] = asDouble(danger);
		
		// -- Number of holes and valleys needing an I piece (after placement)
		int holeAfter = fld.getHowManyHoles();
		int lidAfter = fld.getHowManyLidAboveHoles();
		int needIValleyAfter = fld.getTotalValleyNeedIPiece();
		
		// -- Difference in holes before and after. 
		// Negative values means hole count has decreased (good)
		// Positive values mean hole count has increased (bad).
		int holeDelta = holeAfter - holeBefore;
		stimuli[6] = asDouble(holeDelta, 0, 200);
		
		// -- Difference in lids before and after. 
		// Negative values means lid count has decreased (good)
		// Positive values mean lid count has increased (bad).
		int lidDelta = lidAfter - lidBefore;
		stimuli[7] = asDouble(lidDelta, 0, 200);
		
		// -- Difference in I-valleys before and after.
		int needIValleyDelta = needIValleyAfter - needIValleyBefore;
		stimuli[8] = asDouble(needIValleyDelta, 0, 10);
		
		// -- Difference in height before and after.
		int heightDelta = heightAfter - heightBefore;
		stimuli[9] = asDouble(heightDelta, 0, 20);
		
		// Other
		int combo = (engine.combo) + (2 * lines) - 2;
		stimuli[10] = asDouble(combo, 0, 100);
		
		return stimuli;
	}

}
