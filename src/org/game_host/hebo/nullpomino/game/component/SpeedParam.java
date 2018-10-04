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
package org.game_host.hebo.nullpomino.game.component;

import java.io.Serializable;

/**
 * ブロックピースの落下速度や出現待ち時間などのデータ
 */
public class SpeedParam implements Serializable {
	/** シリアルバージョンID */
	private static final long serialVersionUID = -955934100998757270L;

	/** 落下速度 */
	public int gravity;

	/** 落下速度の分母（gravity==denominatorなら1Gになる） */
	public int denominator;

	/** 出現待ち時間 */
	public int are;

	/** ライン消去後の出現待ち時間 */
	public int areLine;

	/** ライン消去時間 */
	public int lineDelay;

	/** 固定時間 */
	public int lockDelay;

	/** 横移動時間 */
	public int das;

	/**
	 * コンストラクタ
	 */
	public SpeedParam() {
		reset();
	}

	/**
	 * コピーコンストラクタ
	 * @param s コピー元
	 */
	public SpeedParam(SpeedParam s) {
		copy(s);
	}

	/**
	 * 初期値に戻す
	 */
	public void reset() {
		gravity = 4;
		denominator = 256;
		are = 24;
		areLine = 24;
		lineDelay = 40;
		lockDelay = 30;
		das = 14;
	}

	/**
	 * 別のSpeedParamからコピー
	 * @param s コピー元
	 */
	public void copy(SpeedParam s) {
		gravity = s.gravity;
		denominator = s.denominator;
		are = s.are;
		areLine = s.areLine;
		lineDelay = s.lineDelay;
		lockDelay = s.lockDelay;
		das = s.das;
	}
}
