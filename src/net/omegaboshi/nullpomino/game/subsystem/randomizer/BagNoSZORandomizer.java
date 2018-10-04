package net.omegaboshi.nullpomino.game.subsystem.randomizer;

import org.game_host.hebo.nullpomino.game.component.Piece;

public class BagNoSZORandomizer extends BagRandomizer {
	
	boolean firstBag;
	
	public BagNoSZORandomizer() {
		super();
	}
	
	public BagNoSZORandomizer(boolean[] pieceEnable, long seed) {
		super(pieceEnable, seed);
		firstBag = true;
	}

	public void shuffle() {
		if (firstBag && pieces != new int[] {Piece.PIECE_O, Piece.PIECE_Z, Piece.PIECE_S}) {
			do {
				super.shuffle();
			} while (bag[0] == Piece.PIECE_O || bag[0] == Piece.PIECE_Z || bag[0] == Piece.PIECE_S);
		} else {
			super.shuffle();
		}
	}
}
