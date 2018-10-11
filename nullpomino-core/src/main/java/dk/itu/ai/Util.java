package dk.itu.ai;

import dk.itu.ai.navigation.Move;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * A class containing static utility functions.
 * 
 * @author Oliver Phillip
 */
public class Util {
	private Util() {}
	
	/**
	 * This method calculates a delta rotation between a current rotation (fromRt) and a target rotation (toRt).
	 * 
	 * The return values are either:
	 * -1 - The delta to get from the fromRt to the toRt is a single counter-clockwise rotation. 
	 * 	0 - No delta, the fromRt and toRt rotations are alike.
	 *  1 - The delta to get from the fromRt to the toRt is a single clockwise rotation.
	 *  2 - The delta to get from the fromRt to the toRt is two clockwise rotations.
	 *  		(two counter-clockwise rotations can be used for the same result.) 
	 *  
	 * @param fromRt The rotation the piece is currently in.
	 * @param toRt The rotation the piece should rotate towards.
	 * @return The rotation delta.
	 */
	public static int getDeltaRt(int fromRt, int toRt)
	{
		int diffRt = ((toRt - fromRt) + 4) % 4;
		
		if(diffRt > 2)
		{
			diffRt -= 4;
		}
		
		return(diffRt);
	}
	
	/**
	 * This method calculates the delta movement between a current X-coordinate (fromX) and a target X-coordinate (toX).
	 * 
	 * The return value 'd', is to be interpreted as follows:
	 * 
	 *  if d < 0 then: 
	 *  	The delta to get from the fromX coordinate to the toX coordinate is 'd' moves to the left.
	 *   
	 * 	if d = 0 then: 
	 * 		No delta, the fromX and toX coordinates are alike.
	 * 
	 *  if d > 0 then:
	 *  	The delta to get from the fromX coordinate to the toX coordinate is 'd' moves to the right. 
	 * 
	 * @param fromX The X-coordinate the piece is currently in.
	 * @param toX The X-coordinate the piece should move towards.
	 * @return The movement delta along the X-axis.
	 */
	public static int getDeltaX(int fromX, int toX)
	{
		int diffX = toX - fromX;
		return(diffX);
	}
	
	/**
	 * Given an engine with a current field inside, and a move, this method construct a new field that is how the old field would look after applying the move.
	 * 
	 * @param engine The game engine.
	 * @param move The move.
	 * @return The new field.
	 */
	public static Field getFieldAfter(GameEngine engine, Move move) {
		Piece piece = new Piece(move.piece);
		Field field = new Field(engine.field);
		
		piece.placeToField(move.x, move.y, move.rotation, field);
		
		return(field);
	}
}
