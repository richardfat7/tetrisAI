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
package mu.nu.nullpo.gui.sdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import mu.nu.nullpo.util.CustomProperties;

import sdljava.SDLException;
import sdljava.video.SDLSurface;

/**
 * ルール選択画面のステート
 */
public class StateConfigRuleSelectSDL extends BaseStateSDL {
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

	/** Rule name一覧 */
	protected String[] strRuleNameList;

	/** Current ルールファイル */
	protected String strCurrentFileName;

	/** Current Rule name */
	protected String strCurrentRuleName;

	/** カーソル位置 */
	protected int cursor = 0;

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
	public void enter() throws SDLException {
		firstSetupMode = NullpoMinoSDL.propConfig.getProperty("option.firstSetupMode", true);

		strFileNameList = getRuleFileList();
		updateDetails();

		strCurrentFileName = NullpoMinoSDL.propGlobal.getProperty(player + ".rulefile", "");
		strCurrentRuleName = NullpoMinoSDL.propGlobal.getProperty(player + ".rulename", "");
		for(int i = 0; i < strFileNameList.length; i++) {
			if(strCurrentFileName.equals(strFileNameList[i])) {
				cursor = i;
			}
		}
	}

	/*
	 * 描画
	 */
	@Override
	public void render(SDLSurface screen) throws SDLException {
		ResourceHolderSDL.imgMenu.blitSurface(screen);

		if(strFileNameList == null) {
			NormalFontSDL.printFontGrid(1, 1, "RULE DIRECTORY NOT FOUND", NormalFontSDL.COLOR_RED);
		} else if(strFileNameList.length <= 0) {
			NormalFontSDL.printFontGrid(1, 1, "NO RULE FILE", NormalFontSDL.COLOR_RED);
		} else {
			String title = "SELECT " + (player + 1) + "P RULE (" + (cursor + 1) + "/" + (strFileNameList.length) + ")";
			NormalFontSDL.printFontGrid(1, 1, title, NormalFontSDL.COLOR_ORANGE);

			int maxfile = strFileNameList.length;
			if(maxfile > MAX_FILE_IN_ONE_PAGE) maxfile = MAX_FILE_IN_ONE_PAGE;
			int y = 0;
			int num = (cursor / MAX_FILE_IN_ONE_PAGE) * MAX_FILE_IN_ONE_PAGE;

			for(int i = 0; i < maxfile; i++) {
				if(num + i < strFileNameList.length) {
					NormalFontSDL.printFontGrid(2, 3 + y, strRuleNameList[num + i].toUpperCase(), (cursor == num + i));
					if(cursor == num + i) NormalFontSDL.printFontGrid(1, 3 + y, "b", NormalFontSDL.COLOR_RED);
					y++;
				}
			}

			NormalFontSDL.printFontGrid(1, 26, "FILE:" + strFileNameList[cursor].toUpperCase(), NormalFontSDL.COLOR_CYAN);
			NormalFontSDL.printFontGrid(1, 27, "CURRENT:" + strCurrentRuleName.toUpperCase(), NormalFontSDL.COLOR_BLUE);

			NormalFontSDL.printFontGrid(1, 28, "A:OK", NormalFontSDL.COLOR_GREEN);
		}

		if(firstSetupMode) {
			NormalFontSDL.printFontGrid(6, 28, "D:USE DEFAULT RULE", NormalFontSDL.COLOR_GREEN);
		} else {
			NormalFontSDL.printFontGrid(6, 28, "B:CANCEL D:USE DEFAULT RULE", NormalFontSDL.COLOR_GREEN);
		}
	}

	/*
	 * 内部状態の更新
	 */
	@Override
	public void update() throws SDLException {
		if((strFileNameList != null) && (strFileNameList.length > 0)) {
			// カーソル移動
			if(GameKeySDL.gamekey[0].isMenuRepeatKey(GameKeySDL.BUTTON_UP)) {
				cursor--;
				if(cursor < 0) cursor = strFileNameList.length - 1;
				ResourceHolderSDL.soundManager.play("cursor");
			}
			if(GameKeySDL.gamekey[0].isMenuRepeatKey(GameKeySDL.BUTTON_DOWN)) {
				cursor++;
				if(cursor > strFileNameList.length - 1) cursor = 0;
				ResourceHolderSDL.soundManager.play("cursor");
			}

			// 決定ボタン
			if(GameKeySDL.gamekey[0].isPushKey(GameKeySDL.BUTTON_A)) {
				ResourceHolderSDL.soundManager.play("decide");
				NullpoMinoSDL.propConfig.setProperty("option.firstSetupMode", false);
				NullpoMinoSDL.propGlobal.setProperty(player + ".rule", strFilePathList[cursor]);
				NullpoMinoSDL.propGlobal.setProperty(player + ".rulefile", strFileNameList[cursor]);
				NullpoMinoSDL.propGlobal.setProperty(player + ".rulename", strRuleNameList[cursor]);
				NullpoMinoSDL.saveConfig();
				if(!firstSetupMode) NullpoMinoSDL.enterState(NullpoMinoSDL.STATE_CONFIG_MAINMENU);
				else NullpoMinoSDL.enterState(NullpoMinoSDL.STATE_TITLE);
				return;
			}
		}

		// デフォルトルールに設定
		if(GameKeySDL.gamekey[0].isPushKey(GameKeySDL.BUTTON_D)) {
			ResourceHolderSDL.soundManager.play("decide");
			NullpoMinoSDL.propConfig.setProperty("option.firstSetupMode", false);
			NullpoMinoSDL.propGlobal.setProperty(player + ".rule", "");
			NullpoMinoSDL.propGlobal.setProperty(player + ".rulefile", "");
			NullpoMinoSDL.propGlobal.setProperty(player + ".rulename", "");
			NullpoMinoSDL.saveConfig();
			if(!firstSetupMode) NullpoMinoSDL.enterState(NullpoMinoSDL.STATE_CONFIG_MAINMENU);
			else NullpoMinoSDL.enterState(NullpoMinoSDL.STATE_TITLE);
			return;
		}

		// Cancelボタン
		if(GameKeySDL.gamekey[0].isPushKey(GameKeySDL.BUTTON_B) && !firstSetupMode) {
			NullpoMinoSDL.enterState(NullpoMinoSDL.STATE_CONFIG_MAINMENU);
			return;
		}
	}
}
