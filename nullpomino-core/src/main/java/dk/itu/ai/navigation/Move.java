package dk.itu.ai.navigation;

import mu.nu.nullpo.game.component.Piece;

/**
 * Move representation class, with accompanying required input to get to this position
 * 
 * @author Kas
 */
public class Move
{
	/** X-axis translation / Piece rotation (none) */
	public static final int NONE = 0;
	
	/** X-axis translation (left: -1) */
	public static final int LEFT = -1;
	/** X-axis translation (right: +1) */
	public static final int RIGHT = 1;
	
	/** Piece rotation (counter-clockwise: -1) */
	public static final int CCW = -1;
	/** Piece rotation (clockwise: +1) */
	public static final int CW = 1;
	
	// Iterator help
	public static final int[] TRANSLATIONS = {NONE, RIGHT, LEFT};
	public static final int[] ROTATIONS = {NONE, CW, CCW};
	
	/** Target x-cordinate. */
	public final int x;
	
	/** Target y-coordinate. */
	public final int y;
	
	/** Target rotation. */
	public final int rotation;
	
	/** Delta movement on x-axis required to reach target X. */
	public final int dx;
	
	/** Delta rotation required to reach target rotation. */
	public final int drt;
	
	/** Count of how many floor kicks have been performed so far. */
	public final int floorKicksPerformed;
	
	/** The piece that is associated with this move. */
	public final Piece piece;

	/** The move that comes before this move. null if this is the first (root) move. */
	public final Move parent;
	
	/**
	 * Constructs a new Move.
	 * 
	 * @param x Target x-cordinate. 
	 * @param y Target y-cordinate.
	 * @param rotation Target rotation.
	 * @param dx Delta movement on x-axis required to reach target X.
	 * @param drt Delta rotation required to reach target rotation.
	 * @param floorKicksPerformed Count of how many floor kicks have been performed so far.
	 * @param piece The piece that is associated with this move.
	 * @param parent The move that comes before this move. null if this is the first (root) move.
	 */
	public Move(int x, int y, int rotation, int dx, int drt, int floorKicksPerformed, Piece piece, Move parent) {
		this.x = x;
		this.y = y;
		this.rotation = rotation;
		this.dx = dx;
		this.drt = drt;
		this.floorKicksPerformed = floorKicksPerformed;
		this.piece = piece;
		this.parent = parent;
	}

	/* Automatically generated code below */
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rotation;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + floorKicksPerformed;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Move other = (Move) obj;
		if (rotation != other.rotation)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (floorKicksPerformed != other.floorKicksPerformed)
			return false;
		return true;
	}
}