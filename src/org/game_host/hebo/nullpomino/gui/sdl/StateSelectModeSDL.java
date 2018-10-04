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
package org.game_host.hebo.nullpomino.gui.sdl;

import sdljava.SDLException;
import sdljava.video.SDLSurface;

/**
 * モード選択画面のステート
 */
public class StateSelectModeSDL extends BaseStateSDL {
	/** 1画面に表示する最大モード数 */
	public static final int MAX_MODE_IN_ONE_PAGE = 25;

	/** モード名の配列 */
	protected String[] modenames;

	/** カーソル位置 */
	protected int cursor = 0;

	/**
	 * コンストラクタ
	 */
	public StateSelectModeSDL() {
		String lastmode = NullpoMinoSDL.propGlobal.getProperty("name.mode", null);
		modenames = NullpoMinoSDL.modeManager.getModeNames(false);
		cursor = NullpoMinoSDL.modeManager.getIDbyName(lastmode);
		if(cursor < 0) cursor = 0;
	}

	/*
	 * 画面描画
	 */
	@Override
	public void render(SDLSurface screen) throws SDLException {
		ResourceHolderSDL.imgMenu.blitSurface(screen);

		NormalFontSDL.printFontGrid(1, 1,
									"MODE SELECT (" + (cursor + 1) + "/" + modenames.length + ")",
									NormalFontSDL.COLOR_ORANGE);

		int maxmode = modenames.length;
		if(maxmode > MAX_MODE_IN_ONE_PAGE) maxmode = MAX_MODE_IN_ONE_PAGE;
		int y = 0;
		int num = (cursor / MAX_MODE_IN_ONE_PAGE) * MAX_MODE_IN_ONE_PAGE;

		for(int i = 0; i < maxmode; i++) {
			if(num + i < modenames.length) {
				NormalFontSDL.printFontGrid(2, 3 + y, modenames[num + i], (cursor == num + i));
				if(cursor == num + i) NormalFontSDL.printFontGrid(1, 3 + y, "b", NormalFontSDL.COLOR_RED);
				y++;
			}
		}
	}

	/*
	 * ゲーム状態の更新
	 */
	@Override
	public void update() throws SDLException {
		// カーソル移動
		if(GameKeySDL.gamekey[0].isMenuRepeatKey(GameKeySDL.BUTTON_UP)) {
			cursor--;
			if(cursor < 0) cursor = modenames.length - 1;
			ResourceHolderSDL.soundManager.play("cursor");
		}
		if(GameKeySDL.gamekey[0].isMenuRepeatKey(GameKeySDL.BUTTON_DOWN)) {
			cursor++;
			if(cursor > modenames.length - 1) cursor = 0;
			ResourceHolderSDL.soundManager.play("cursor");
		}

		// 決定ボタン
		if(GameKeySDL.gamekey[0].isPushKey(GameKeySDL.BUTTON_A)) {
			ResourceHolderSDL.soundManager.play("decide");
			NullpoMinoSDL.propGlobal.setProperty("name.mode", modenames[cursor]);
			NullpoMinoSDL.saveConfig();

			StateInGameSDL s = (StateInGameSDL)NullpoMinoSDL.gameStates[NullpoMinoSDL.STATE_INGAME];
			s.startNewGame();

			NullpoMinoSDL.enterState(NullpoMinoSDL.STATE_INGAME);
		}

		// キャンセルボタン
		if(GameKeySDL.gamekey[0].isPushKey(GameKeySDL.BUTTON_B)) {
			NullpoMinoSDL.enterState(NullpoMinoSDL.STATE_TITLE);
		}
	}
}
