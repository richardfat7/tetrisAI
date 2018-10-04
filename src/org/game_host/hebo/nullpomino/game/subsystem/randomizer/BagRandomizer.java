/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package org.game_host.hebo.nullpomino.game.subsystem.randomizer;

import java.util.Random;

import org.game_host.hebo.nullpomino.game.component.Piece;
import org.game_host.hebo.nullpomino.util.GeneralUtil;

/**
 * 出現する可能性のあるピースが全て偏らずに1個ずつ出る出現順
 */
public class BagRandomizer implements Randomizer {
	/*
	 * NEXTの順番を生成
	 */
	public int[] createPieceSequence(boolean[] pieceEnable, Random random, int arrayMax) {
		int[] pieceArray = new int[arrayMax];
		int pieceKind = GeneralUtil.getNumberOfPiecesCanAppear(pieceEnable);

		for(int i = 0; i < arrayMax / pieceKind; i++) {
			// ピースがもう出たフラグ
			boolean[] alreadyAppeared = new boolean[Piece.PIECE_COUNT];

			// ツモ作成
			for(int j = 0; j < pieceKind; j++) {
				int id = 0;

				// ツモを引く
				do {
					id = random.nextInt(Piece.PIECE_COUNT);
				} while((pieceEnable[id] == false) || (alreadyAppeared[id] == true));

				// ブロックが出たフラグON
				alreadyAppeared[id] = true;

				// NEXTリストに入れる
				pieceArray[i * pieceKind + j] = id;
			}
		}

		return pieceArray;
	}
}
