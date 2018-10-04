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
package org.game_host.hebo.nullpomino.gui.slick;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import org.game_host.hebo.nullpomino.util.CustomProperties;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 * ルール選択画面のステート
 */
public class StateConfigRuleSelect extends BasicGameState {
	/** このステートのID */
	public static final int ID = 7;

	/** 1画面に表示する最大ファイル数 */
	public static final int MAX_FILE_IN_ONE_PAGE = 20;

	/** プレイヤーID */
	public int player = 0;

	/** 初期設定モード */
	protected boolean firstSetupMode;

	/** ファイル名 */
	protected String[] strFileNameList;

	/** ファイルパス一覧 */
	protected String[] strFilePathList;

	/** ルール名一覧 */
	protected String[] strRuleNameList;

	/** 現在のルールファイル */
	protected String strCurrentFileName;

	/** 現在のルール名 */
	protected String strCurrentRuleName;

	/** カーソル位置 */
	protected int cursor = 0;

	/** スクリーンショット撮影フラグ */
	protected boolean ssflag = false;

	/*
	 * このステートのIDを取得
	 */
	@Override
	public int getID() {
		return ID;
	}

	/**
	 * ルールファイル一覧を取得
	 * @return ルールファイルのファイル名の配列。ディレクトリがないならnull
	 */
	protected String[] getRuleFileList() {
		File dir = new File("config/rule");

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir1, String name) {
				return name.endsWith(".rul");
			}
		};

		String[] list = dir.list(filter);

		return list;
	}

	/**
	 * 詳細情報を更新
	 */
	protected void updateDetails() {
		strFilePathList = new String[strFileNameList.length];
		strRuleNameList = new String[strFileNameList.length];

		for(int i = 0; i < strFileNameList.length; i++) {
			File file = new File("config/rule/" + strFileNameList[i]);
			strFilePathList[i] = file.getPath();

			CustomProperties prop = new CustomProperties();

			try {
				FileInputStream in = new FileInputStream("config/rule/" + strFileNameList[i]);
				prop.load(in);
				in.close();
				strRuleNameList[i] = prop.getProperty("0.ruleopt.strRuleName", "");
			} catch (Exception e) {
				strRuleNameList[i] = "";
			}
		}
	}

	/*
	 * このステートに入ったときの処理
	 */
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		firstSetupMode = NullpoMinoSlick.propConfig.getProperty("option.firstSetupMode", true);

		strFileNameList = getRuleFileList();
		updateDetails();

		strCurrentFileName = NullpoMinoSlick.propGlobal.getProperty(player + ".rulefile", "");
		strCurrentRuleName = NullpoMinoSlick.propGlobal.getProperty(player + ".rulename", "");
		for(int i = 0; i < strFileNameList.length; i++) {
			if(strCurrentFileName.equals(strFileNameList[i])) {
				cursor = i;
			}
		}
	}

	/*
	 * ステートの初期化
	 */
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
	}

	/*
	 * 画面描画
	 */
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		// 背景
		g.drawImage(ResourceHolder.imgMenu, 0, 0);

		// メニュー
		if(strFileNameList == null) {
			NormalFont.printFontGrid(1, 1, "RULE DIRECTORY NOT FOUND", NormalFont.COLOR_RED);
		} else if(strFileNameList.length <= 0) {
			NormalFont.printFontGrid(1, 1, "NO RULE FILE", NormalFont.COLOR_RED);
		} else {
			String title = "SELECT " + (player + 1) + "P RULE (" + (cursor + 1) + "/" + (strFileNameList.length) + ")";
			NormalFont.printFontGrid(1, 1, title, NormalFont.COLOR_ORANGE);

			int maxfile = strFileNameList.length;
			if(maxfile > MAX_FILE_IN_ONE_PAGE) maxfile = MAX_FILE_IN_ONE_PAGE;
			int y = 0;
			int num = (cursor / MAX_FILE_IN_ONE_PAGE) * MAX_FILE_IN_ONE_PAGE;

			for(int i = 0; i < maxfile; i++) {
				if(num + i < strFileNameList.length) {
					NormalFont.printFontGrid(2, 3 + y, strRuleNameList[num + i].toUpperCase(), (cursor == num + i));
					if(cursor == num + i) NormalFont.printFontGrid(1, 3 + y, "b", NormalFont.COLOR_RED);
					y++;
				}
			}

			NormalFont.printFontGrid(1, 26, "FILE:" + strFileNameList[cursor].toUpperCase(), NormalFont.COLOR_CYAN);
			NormalFont.printFontGrid(1, 27, "CURRENT:" + strCurrentRuleName.toUpperCase(), NormalFont.COLOR_BLUE);

			NormalFont.printFontGrid(1, 28, "A:OK", NormalFont.COLOR_GREEN);
		}

		if(firstSetupMode) {
			NormalFont.printFontGrid(6, 28, "D:USE DEFAULT RULE", NormalFont.COLOR_GREEN);
		} else {
			NormalFont.printFontGrid(6, 28, "B:CANCEL D:USE DEFAULT RULE", NormalFont.COLOR_GREEN);
		}

		// FPS
		NullpoMinoSlick.drawFPS(container);
		// オブザーバー
		NullpoMinoSlick.drawObserverClient();
		// スクリーンショット
		if(ssflag) {
			NullpoMinoSlick.saveScreenShot(container, g);
			ssflag = false;
		}

		if(!NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
	}

	/*
	 * ゲーム状態の更新
	 */
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		if(!container.hasFocus()) {
			if(NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
			return;
		}

		// キー入力状態を更新
		GameKey.gamekey[0].update(container.getInput());

		if((strFileNameList != null) && (strFileNameList.length > 0)) {
			// カーソル移動
			if(GameKey.gamekey[0].isMenuRepeatKey(GameKey.BUTTON_UP)) {
				cursor--;
				if(cursor < 0) cursor = strFileNameList.length - 1;
				ResourceHolder.soundManager.play("cursor");
			}
			if(GameKey.gamekey[0].isMenuRepeatKey(GameKey.BUTTON_DOWN)) {
				cursor++;
				if(cursor > strFileNameList.length - 1) cursor = 0;
				ResourceHolder.soundManager.play("cursor");
			}

			// 決定ボタン
			if(GameKey.gamekey[0].isPushKey(GameKey.BUTTON_A)) {
				ResourceHolder.soundManager.play("decide");
				NullpoMinoSlick.propGlobal.setProperty(player + ".rule", strFilePathList[cursor]);
				NullpoMinoSlick.propGlobal.setProperty(player + ".rulefile", strFileNameList[cursor]);
				NullpoMinoSlick.propGlobal.setProperty(player + ".rulename", strRuleNameList[cursor]);
				NullpoMinoSlick.propConfig.setProperty("option.firstSetupMode", false);
				NullpoMinoSlick.saveConfig();

				if(!firstSetupMode) game.enterState(StateConfigMainMenu.ID);
				else game.enterState(StateTitle.ID);
				return;
			}
		}

		// デフォルトルールに設定
		if(GameKey.gamekey[0].isPushKey(GameKey.BUTTON_D)) {
			ResourceHolder.soundManager.play("decide");
			NullpoMinoSlick.propGlobal.setProperty(player + ".rule", "");
			NullpoMinoSlick.propGlobal.setProperty(player + ".rulefile", "");
			NullpoMinoSlick.propGlobal.setProperty(player + ".rulename", "");
			NullpoMinoSlick.propConfig.setProperty("option.firstSetupMode", false);
			NullpoMinoSlick.saveConfig();
			if(!firstSetupMode) game.enterState(StateConfigMainMenu.ID);
			else game.enterState(StateTitle.ID);
			return;
		}

		// キャンセルボタン
		if(GameKey.gamekey[0].isPushKey(GameKey.BUTTON_B) && !firstSetupMode) {
			game.enterState(StateConfigMainMenu.ID);
			return;
		}

		// スクリーンショットボタン
		if(GameKey.gamekey[0].isPushKey(GameKey.BUTTON_SCREENSHOT)) ssflag = true;

		// 終了ボタン
		if(GameKey.gamekey[0].isPushKey(GameKey.BUTTON_QUIT)) container.exit();

		if(NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
	}
}
