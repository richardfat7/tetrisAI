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
package org.game_host.hebo.nullpomino.game.subsystem.wallkick;

import org.game_host.hebo.nullpomino.game.component.Block;
import org.game_host.hebo.nullpomino.game.component.Controller;
import org.game_host.hebo.nullpomino.game.component.Field;
import org.game_host.hebo.nullpomino.game.component.Piece;
import org.game_host.hebo.nullpomino.game.component.WallkickResult;

/**
 * ClassicWallkick - クラシックルールな壁蹴り（旧バージョンのCLASSIC1と2相当）
 */
public class ClassicWallkick implements Wallkick {
	/*
	 * 壁蹴り
	 */
	public WallkickResult executeWallkick(int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward, Piece piece, Field field, Controller ctrl) {
		int check = 0;
		if(piece.big) check = 1;

		// 通常の壁蹴り（I以外）
		if(piece.id != Piece.PIECE_I) {
			if(checkCollisionKick(piece, x, y, rtNew, field) || (piece.id == Piece.PIECE_I2) || (piece.id == Piece.PIECE_L3)) {
				int temp = 0;

				if(!piece.checkCollision(x - 1 - check, y, rtNew, field)) temp = -1 - check;
				if(!piece.checkCollision(x + 1 + check, y, rtNew, field)) temp = 1 + check;

				if(temp != 0) {
					return new WallkickResult(temp, 0, rtNew);
				}
			}
		}

		return null;
	}

	/**
	 * 壁蹴り可能かどうか調べる
	 * @param piece ブロックピース
	 * @param x X座標
	 * @param y Y座標
	 * @param rt 方向
	 * @param fld フィールド
	 * @return 壁蹴り可能ならtrue
	 */
	private boolean checkCollisionKick(Piece piece, int x, int y, int rt, Field fld) {
		// ビッグでは専用処理
		if(piece.big == true) return checkCollisionKickBig(piece, x, y, rt, fld);

		for(int i = 0; i < piece.getMaxBlock(); i++) {
			if(piece.dataX[rt][i] != 1 + piece.dataOffsetX[rt]) {
				int x2 = x + piece.dataX[rt][i];
				int y2 = y + piece.dataY[rt][i];

				if(x2 >= fld.getWidth()) {
					return true;
				}
				if(y2 >= fld.getHeight()) {
					return true;
				}
				if(fld.getCoordAttribute(x2, y2) == Field.COORD_WALL) {
					return true;
				}
				if((fld.getCoordAttribute(x2, y2) != Field.COORD_VANISH) && (fld.getBlockColor(x2, y2) != Block.BLOCK_COLOR_NONE)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 壁蹴り可能かどうか調べる（ビッグ用）
	 * @param piece ブロックピース
	 * @param x X座標
	 * @param y Y座標
	 * @param rt 方向
	 * @param fld フィールド
	 * @return 壁蹴り可能ならtrue
	 */
	private boolean checkCollisionKickBig(Piece piece, int x, int y, int rt, Field fld) {
		for(int i = 0; i < piece.getMaxBlock(); i++) {
			if(piece.dataX[rt][i] != 1 + piece.dataOffsetX[rt]) {
				int x2 = (x + piece.dataX[rt][i] * 2);
				int y2 = (y + piece.dataY[rt][i] * 2);

				// 4ブロック分調べる
				for(int k = 0; k < 2; k++)for(int l = 0; l < 2; l++) {
					int x3 = x2 + k;
					int y3 = y2 + l;

					if(x3 >= fld.getWidth()) {
						return true;
					}
					if(y3 >= fld.getHeight()) {
						return true;
					}
					if(fld.getCoordAttribute(x3, y3) == Field.COORD_WALL) {
						return true;
					}
					if((fld.getCoordAttribute(x3, y3) != Field.COORD_VANISH) && (fld.getBlockColor(x3, y3) != Block.BLOCK_COLOR_NONE)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
