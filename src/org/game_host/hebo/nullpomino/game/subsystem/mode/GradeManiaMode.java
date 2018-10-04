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
package org.game_host.hebo.nullpomino.game.subsystem.mode;

import org.game_host.hebo.nullpomino.game.component.BGMStatus;
import org.game_host.hebo.nullpomino.game.component.Controller;
import org.game_host.hebo.nullpomino.game.event.EventReceiver;
import org.game_host.hebo.nullpomino.game.play.GameEngine;
import org.game_host.hebo.nullpomino.game.play.GameManager;
import org.game_host.hebo.nullpomino.util.CustomProperties;
import org.game_host.hebo.nullpomino.util.GeneralUtil;

/**
 * GRADE MANIAモード
 */
public class GradeManiaMode extends DummyMode {
	/** 現在のバージョン */
	private static final int CURRENT_VERSION = 1;

	/** 落下速度テーブル */
	private static final int[] tableGravityValue =
	{
		4, 6, 8, 10, 12, 16, 32, 48, 64, 80, 96, 112, 128, 144, 4, 32, 64, 96, 128, 160, 192, 224, 256, 512, 768, 1024, 1280, 1024, 768, -1
	};

	/** 落下速度が変わるレベル */
	private static final int[] tableGravityChangeLevel =
	{
		30, 35, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 170, 200, 220, 230, 233, 236, 239, 243, 247, 251, 300, 330, 360, 400, 420, 450, 500, 10000
	};

	/** 段位上昇に必要なスコア */
	private static final int[] tableGradeScore =
	{
		   400,   800,  1400,  2000,  3500,  5500,  8000,  12000,			// 8～1
		 16000, 22000, 30000, 40000, 52000, 66000, 82000, 100000, 120000,	// S1～S9
		126000																// GM
	};

	/** 段位の名前 */
	private static final String[] tableGradeName =
	{
		 "9",  "8",  "7",  "6",  "5",  "4",  "3",  "2",  "1",	//  0～ 8
		"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",	//  9～17
		"GM"													// 18
	};

	/** GMの時の評価タイム */
	private static final int[] tablePier21GradeTime = {48600,43200,39600,37800,36000,33600,32400};

	/** GMの時の評価 */
	private static final String[] tablePier21GradeName =
	{
		"ALUMINUM","STEEL","BRONZE","SILVER","GOLD","PLATINUM","DIAMOND"
	};

	/** LV999ロールの時間 */
	private static final int ROLLTIMELIMIT = 2968;

	/** GMを取るために必要なLV300到達時の最低段位 */
	private static final int GM_300_GRADE_REQUIRE = 8;

	/** GMを取るために必要なLV500到達時の最低段位 */
	private static final int GM_500_GRADE_REQUIRE = 12;

	/** GMを取るために必要なLV300到達時のタイム */
	private static final int GM_300_TIME_REQUIRE = 15300;

	/** GMを取るために必要なLV500到達時のタイム */
	private static final int GM_500_TIME_REQUIRE = 27000;

	/** GMを取るために必要なLV500到達時のタイム(古いバージョン用) */
	private static final int GM_500_TIME_REQUIRE_V0 = 25200;

	/** GMを取るために必要なLV999到達時のタイム */
	private static final int GM_999_TIME_REQUIRE = 48600;

	/** ランキングに記録する数 */
	private static final int RANKING_MAX = 10;

	/** 最大セクション数 */
	private static final int SECTION_MAX = 10;

	/** デフォルトのセクションタイム */
	private static final int DEFAULT_SECTION_TIME = 5400;

	/** このモードを所有するGameManager */
	private GameManager owner;

	/** 描画などのイベント処理 */
	private EventReceiver receiver;

	/** 現在の落下速度の番号（tableGravityChangeLevelのレベルに到達するたびに1つ増える） */
	private int gravityindex;

	/** 次のセクションのレベル（これ-1のときにレベルストップする） */
	private int nextseclv;

	/** レベルが増えたフラグ */
	private boolean lvupflag;

	/** 段位 */
	private int grade;

	/** 最後に段位が上がった時間 */
	private int lastGradeTime;

	/** コンボボーナス */
	private int comboValue;

	/** 直前に手に入れたスコア */
	private int lastscore;

	/** 獲得スコア表示がされる残り時間 */
	private int scgettime;

	/** ロール経過時間 */
	private int rolltime;

	/** LV300到達時に段位が規定数以上だったらtrueになる */
	private boolean gm300;

	/** LV500到達時に段位が規定数以上＆タイムが規定以下だったらtrueになる */
	private boolean gm500;

	/** 裏段位 */
	private int secretGrade;

	/** 現在のBGM */
	private int bgmlv;

	/** 段位表示を光らせる残りフレーム数 */
	private int gradeflash;

	/** セクションタイム */
	private int[] sectiontime;

	/** 新記録が出たセクションはtrue */
	private boolean[] sectionIsNewRecord;

	/** どこかのセクションで新記録を出すとtrue */
	private boolean sectionAnyNewRecord;

	/** クリアしたセクション数 */
	private int sectionscomp;

	/** 平均セクションタイム */
	private int sectionavgtime;

	/** セクションタイム記録表示中ならtrue */
	private boolean isShowBestSectionTime;

	/** 開始時のレベル */
	private int startlevel;

	/** trueなら常にゴーストON */
	private boolean alwaysghost;

	/** trueなら常に20G */
	private boolean always20g;

	/** trueならレベルストップ音有効 */
	private boolean lvstopse;

	/** ビッグモード */
	private boolean big;

	/** trueならセクションタイム表示有効 */
	private boolean showsectiontime;

	/** バージョン */
	private int version;

	/** 今回のプレイのランキングでのランク */
	private int rankingRank;

	/** ランキングの段位 */
	private int[] rankingGrade;

	/** ランキングのレベル */
	private int[] rankingLevel;

	/** ランキングのタイム */
	private int[] rankingTime;

	/** セクションタイム記録 */
	private int[] bestSectionTime;

	/*
	 * モード名
	 */
	@Override
	public String getName() {
		return "GRADE MANIA";
	}

	/*
	 * 初期化
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;

		gravityindex = 0;
		nextseclv = 0;
		lvupflag = true;
		grade = 0;
		lastGradeTime = 0;
		comboValue = 0;
		lastscore = 0;
		scgettime = 0;
		rolltime = 0;
		gm300 = false;
		gm500 = false;
		secretGrade = 0;
		bgmlv = 0;
		gradeflash = 0;
		sectiontime = new int[SECTION_MAX];
		sectionIsNewRecord = new boolean[SECTION_MAX];
		sectionAnyNewRecord = false;
		sectionscomp = 0;
		sectionavgtime = 0;
		isShowBestSectionTime = false;
		startlevel = 0;
		alwaysghost = false;
		always20g = false;
		lvstopse = false;
		big = false;

		rankingRank = -1;
		rankingGrade = new int[RANKING_MAX];
		rankingLevel = new int[RANKING_MAX];
		rankingTime = new int[RANKING_MAX];
		bestSectionTime = new int[SECTION_MAX];

		engine.tspinEnable = false;
		engine.b2bEnable = false;
		engine.comboType = GameEngine.COMBO_TYPE_DOUBLE;
		engine.bighalf = false;
		engine.bigmove = false;
		engine.staffrollNoDeath = true;

		engine.speed.are = 25;
		engine.speed.areLine = 25;
		engine.speed.lineDelay = 41;
		engine.speed.lockDelay = 30;
		engine.speed.das = 15;

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			version = owner.replayProp.getProperty("grademania.version", 0);
		}

		owner.backgroundStatus.bg = startlevel;
	}

	/**
	 * プロパティファイルから設定を読み込み
	 * @param prop プロパティファイル
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("grademania.startlevel", 0);
		alwaysghost = prop.getProperty("grademania.alwaysghost", false);
		always20g = prop.getProperty("grademania.always20g", false);
		lvstopse = prop.getProperty("grademania.lvstopse", false);
		showsectiontime = prop.getProperty("grademania.showsectiontime", false);
		big = prop.getProperty("grademania.big", false);
	}

	/**
	 * プロパティファイルに設定を保存
	 * @param prop プロパティファイル
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("grademania.startlevel", startlevel);
		prop.setProperty("grademania.alwaysghost", alwaysghost);
		prop.setProperty("grademania.always20g", always20g);
		prop.setProperty("grademania.lvstopse", lvstopse);
		prop.setProperty("grademania.showsectiontime", showsectiontime);
		prop.setProperty("grademania.big", big);
	}

	/**
	 * 落下速度を更新
	 * @param engine GameEngine
	 */
	private void setSpeed(GameEngine engine) {
		if(always20g == true) {
			engine.speed.gravity = -1;
		} else {
			while(engine.statistics.level >= tableGravityChangeLevel[gravityindex]) gravityindex++;
			engine.speed.gravity = tableGravityValue[gravityindex];
		}
	}

	/**
	 * 平均セクションタイムを更新
	 */
	private void setAverageSectionTime() {
		if(sectionscomp > 0) {
			int temp = 0;
			for(int i = startlevel; i < startlevel + sectionscomp; i++) {
				if((i >= 0) && (i < sectiontime.length)) temp += sectiontime[i];
			}
			sectionavgtime = temp / sectionscomp;
		} else {
			sectionavgtime = 0;
		}
	}

	/**
	 * セクションタイム更新処理
	 * @param sectionNumber セクション番号
	 */
	private void stNewRecordCheck(int sectionNumber) {
		if((sectiontime[sectionNumber] < bestSectionTime[sectionNumber]) && (!owner.replayMode)) {
			sectionIsNewRecord[sectionNumber] = true;
			sectionAnyNewRecord = true;
		}
	}

	/*
	 * 設定画面の処理
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// メニュー
		if(engine.owner.replayMode == false) {
			// 上
			if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
				engine.statc[2]--;
				if(engine.statc[2] < 0) engine.statc[2] = 5;
				engine.playSE("cursor");
			}
			// 下
			if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
				engine.statc[2]++;
				if(engine.statc[2] > 5) engine.statc[2] = 0;
				engine.playSE("cursor");
			}

			// 設定変更
			int change = 0;
			if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_LEFT)) change = -1;
			if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_RIGHT)) change = 1;

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
				case 0:
					startlevel += change;
					if(startlevel < 0) startlevel = 9;
					if(startlevel > 9) startlevel = 0;
					owner.backgroundStatus.bg = startlevel;
					break;
				case 1:
					alwaysghost = !alwaysghost;
					break;
				case 2:
					always20g = !always20g;
					break;
				case 3:
					lvstopse = !lvstopse;
					break;
				case 4:
					showsectiontime = !showsectiontime;
					break;
				case 5:
					big = !big;
					break;
				}
			}

			// セクションタイム表示切替
			if(engine.ctrl.isPush(Controller.BUTTON_F) && (engine.statc[3] >= 5)) {
				engine.playSE("change");
				isShowBestSectionTime = !isShowBestSectionTime;
			}

			// 決定
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				saveSetting(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);
				isShowBestSectionTime = false;
				sectionscomp = 0;
				return false;
			}

			// キャンセル
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
			}

			engine.statc[3]++;
		} else {
			engine.statc[3]++;
			engine.statc[2] = -1;

			if(engine.statc[3] >= 60) {
				return false;
			}
		}

		return true;
	}

	/*
	 * 設定画面の描画処理
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		if(engine.owner.replayMode == false) {
			receiver.drawMenuFont(engine, playerID, 0, (engine.statc[2] * 2) + 1, "b", EventReceiver.COLOR_RED);
		}

		receiver.drawMenuFont(engine, playerID, 0, 0, "LEVEL", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 1, 1, String.valueOf(startlevel * 100), (engine.statc[2] == 0));
		receiver.drawMenuFont(engine, playerID, 0, 2, "FULL GHOST", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 1, 3, GeneralUtil.getONorOFF(alwaysghost), (engine.statc[2] == 1));
		receiver.drawMenuFont(engine, playerID, 0, 4, "20G MODE", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 1, 5, GeneralUtil.getONorOFF(always20g), (engine.statc[2] == 2));
		receiver.drawMenuFont(engine, playerID, 0, 6, "LVSTOPSE", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 1, 7, GeneralUtil.getONorOFF(lvstopse), (engine.statc[2] == 3));
		receiver.drawMenuFont(engine, playerID, 0, 8, "SHOW STIME", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 1, 9, GeneralUtil.getONorOFF(showsectiontime), (engine.statc[2] == 4));
		receiver.drawMenuFont(engine, playerID, 0, 10, "BIG", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 1, 11, GeneralUtil.getONorOFF(big), (engine.statc[2] == 5));
	}

	/*
	 * ゲーム開始時の処理
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel * 100;

		nextseclv = engine.statistics.level + 100;
		if(engine.statistics.level < 0) nextseclv = 100;
		if(engine.statistics.level >= 900) nextseclv = 999;

		owner.backgroundStatus.bg = engine.statistics.level / 100;

		if(engine.statistics.level < 500) bgmlv = 0;
		else bgmlv = 1;

		engine.big = big;

		setSpeed(engine);
		owner.bgmStatus.bgm = bgmlv;
	}

	/*
	 * スコア表示
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		receiver.drawScoreFont(engine, playerID, 0, 0, "GRADE MANIA", EventReceiver.COLOR_CYAN);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (startlevel == 0) && (big == false) && (always20g == false) && (engine.ai == null)) {
				if(!isShowBestSectionTime) {
					// ランキング
					receiver.drawScoreFont(engine, playerID, 3, 2, "GRADE LEVEL TIME", EventReceiver.COLOR_BLUE);

					for(int i = 0; i < RANKING_MAX; i++) {
						receiver.drawScoreFont(engine, playerID, 0, 3 + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW);
						if((rankingGrade[i] >= 0) && (rankingGrade[i] < tableGradeName.length))
							receiver.drawScoreFont(engine, playerID, 3, 3 + i, tableGradeName[rankingGrade[i]], (i == rankingRank));
						receiver.drawScoreFont(engine, playerID, 9, 3 + i, String.valueOf(rankingLevel[i]), (i == rankingRank));
						receiver.drawScoreFont(engine, playerID, 15, 3 + i, GeneralUtil.getTime(rankingTime[i]), (i == rankingRank));
					}

					receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW SECTION TIME", EventReceiver.COLOR_GREEN);
				} else {
					// セクションタイム
					receiver.drawScoreFont(engine, playerID, 0, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);

					int totalTime = 0;
					for(int i = 0; i < SECTION_MAX; i++) {
						int temp = Math.min(i * 100, 999);
						int temp2 = Math.min(((i + 1) * 100) - 1, 999);

						String strSectionTime;
						strSectionTime = String.format("%3d-%3d %s", temp, temp2, GeneralUtil.getTime(bestSectionTime[i]));

						receiver.drawScoreFont(engine, playerID, 0, 3 + i, strSectionTime, sectionIsNewRecord[i]);

						totalTime += bestSectionTime[i];
					}

					receiver.drawScoreFont(engine, playerID, 0, 14, "TOTAL", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(totalTime));
					receiver.drawScoreFont(engine, playerID, 9, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 9, 15, GeneralUtil.getTime(totalTime / SECTION_MAX));

					receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW RANKING", EventReceiver.COLOR_GREEN);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
			if((grade >= 0) && (grade < tableGradeName.length))
				receiver.drawScoreFont(engine, playerID, 0, 3, tableGradeName[grade], ((gradeflash > 0) && (gradeflash % 4 == 0)));

			receiver.drawScoreFont(engine, playerID, 0, 5, "POINTS", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime <= 0)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = String.valueOf(engine.statistics.score) + "(+" + String.valueOf(lastscore) + ")";
			}
			receiver.drawScoreFont(engine, playerID, 0, 6, strScore);
			if(grade < 17) {
				receiver.drawScoreFont(engine, playerID, 0, 7, String.valueOf(tableGradeScore[grade]));
			}

			receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
			int tempLevel = engine.statistics.level;
			if(tempLevel < 0) tempLevel = 0;
			String strLevel = String.format("%3d", tempLevel);
			receiver.drawScoreFont(engine, playerID, 0, 10, strLevel);

			int speed = engine.speed.gravity / 128;
			if(engine.speed.gravity < 0) speed = 40;
			receiver.drawSpeedMeter(engine, playerID, 0, 11, speed);

			receiver.drawScoreFont(engine, playerID, 0, 12, String.format("%3d", nextseclv));

			receiver.drawScoreFont(engine, playerID, 0, 14, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(engine.statistics.time));

			if((engine.gameActive) && (engine.ending == 2)) {
				int time = ROLLTIMELIMIT - rolltime;
				if(time < 0) time = 0;
				receiver.drawScoreFont(engine, playerID, 0, 17, "ROLL TIME", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
			}

			// セクションタイム
			if((showsectiontime == true) && (sectiontime != null)) {
				receiver.drawScoreFont(engine, playerID, 12, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);

				for(int i = 0; i < sectiontime.length; i++) {
					if(sectiontime[i] > 0) {
						int temp = i * 100;
						if(temp > 999) temp = 999;

						int section = engine.statistics.level / 100;
						String strSeparator = " ";
						if((i == section) && (engine.ending == 0)) strSeparator = "b";

						String strSectionTime;
						strSectionTime = String.format("%3d%s%s", temp, strSeparator, GeneralUtil.getTime(sectiontime[i]));

						receiver.drawScoreFont(engine, playerID, 12, 3 + i, strSectionTime, sectionIsNewRecord[i]);
					}
				}

				if(sectionavgtime > 0) {
					receiver.drawScoreFont(engine, playerID, 12, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 12, 15, GeneralUtil.getTime(sectionavgtime));
				}
			}
		}
	}

	/*
	 * 移動中の処理
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// 新規ピース出現時
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (!lvupflag)) {
			if(engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) engine.playSE("levelstop");
			}
			levelUp(engine);
		}
		if( (engine.ending == 0) && (engine.statc[0] > 0) && ((version >= 1) || (engine.holdDisable == false)) ) {
			lvupflag = false;
		}

		return false;
	}

	/*
	 * ARE中の処理
	 */
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		// 最後のフレーム
		if((engine.ending == 0) && (engine.statc[0] >= engine.statc[1] - 1) && (!lvupflag)) {
			if(engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) engine.playSE("levelstop");
			}
			levelUp(engine);
			lvupflag = true;
		}

		return false;
	}

	/**
	 * レベルが上がったときの共通処理
	 */
	private void levelUp(GameEngine engine) {
		// メーター
		engine.meterValue = ((engine.statistics.level % 100) * receiver.getMeterMax(engine)) / 99;
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(engine.statistics.level % 100 >= 50) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(engine.statistics.level % 100 >= 80) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(engine.statistics.level == nextseclv - 1) engine.meterColor = GameEngine.METER_COLOR_RED;

		// 速度変更
		setSpeed(engine);

		// LV100到達でゴーストを消す
		if((engine.statistics.level >= 100) && (!alwaysghost)) engine.ghost = false;

		// BGMフェードアウト
		if((bgmlv == 0) && (engine.statistics.level >= 490))
			owner.bgmStatus.fadesw  = true;
	}

	/*
	 * スコア計算
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		if(engine.ending != 0) return;

		// コンボ
		if(lines == 0) {
			comboValue = 1;
		} else {
			comboValue = comboValue + (2 * lines) - 2;
			if(comboValue < 1) comboValue = 1;
		}

		if(lines >= 1) {
			// スコア計算
			int manuallock = 0;
			if(engine.manualLock == true) manuallock = 1;

			int bravo = 1;
			if(engine.field.isEmpty()) {
				bravo = 4;
				engine.playSE("bravo");
			}

			lastscore = ( ((engine.statistics.level + lines) / 4) + engine.softdropFall + engine.harddropFall + manuallock ) * lines * comboValue * bravo;
			engine.statistics.score += lastscore;
			scgettime = 120;

			// 段位上昇
			while((grade < 17) && (engine.statistics.score >= tableGradeScore[grade])) {
				engine.playSE("gradeup");
				grade++;
				gradeflash = 180;
				lastGradeTime = engine.statistics.time;
			}

			// レベルアップ
			engine.statistics.level += lines;
			levelUp(engine);

			if(engine.statistics.level >= 999) {
				// エンディング
				engine.statistics.level = 999;
				engine.timerActive = false;
				lastGradeTime = engine.statistics.time;

				sectionscomp++;
				setAverageSectionTime();
				stNewRecordCheck(sectionscomp - 1);

				if((engine.statistics.time <= GM_999_TIME_REQUIRE) && (engine.statistics.score >= tableGradeScore[17]) && (gm300) && (gm500)) {
					engine.playSE("endingstart");
					engine.playSE("gradeup");

					grade = 18;
					gradeflash = 180;

					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = BGMStatus.BGM_ENDING2;

					engine.ending = 2;
				} else {
					engine.gameActive = false;
					engine.ending = 1;
				}
			} else if(engine.statistics.level >= nextseclv) {
				// 次のセクション
				engine.playSE("levelup");

				owner.backgroundStatus.fadesw = true;
				owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.fadebg = nextseclv / 100;

				if(version >= 1) {
					if((nextseclv == 300) && (grade >= GM_300_GRADE_REQUIRE) && (engine.statistics.time <= GM_300_TIME_REQUIRE))
						gm300 = true;
					if((nextseclv == 500) && (grade >= GM_500_GRADE_REQUIRE) && (engine.statistics.time <= GM_500_TIME_REQUIRE))
						gm500 = true;
				} else {
					if((nextseclv == 300) && (grade >= GM_300_GRADE_REQUIRE))
						gm300 = true;
					if((nextseclv == 500) && (grade >= GM_500_GRADE_REQUIRE) && (engine.statistics.time <= GM_500_TIME_REQUIRE_V0))
						gm500 = true;
				}

				sectionscomp++;
				setAverageSectionTime();
				stNewRecordCheck(sectionscomp - 1);

				if((bgmlv == 0) && (nextseclv == 500)) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv;
				}

				nextseclv += 100;
				if(nextseclv > 999) nextseclv = 999;
			} else if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) {
				engine.playSE("levelstop");
			}
		}
	}

	/*
	 * 各フレームの終わりの処理
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		// 段位上昇時のフラッシュ
		if(gradeflash > 0) gradeflash--;

		// 獲得スコア表示
		if(scgettime > 0) scgettime--;

		// セクションタイム増加
		if((engine.timerActive) && (engine.ending == 0)) {
			int section = engine.statistics.level / 100;

			if((section >= 0) && (section < sectiontime.length)) {
				sectiontime[section]++;
			}
		}

		// エンディング
		if((engine.gameActive) && (engine.ending == 2)) {
			rolltime++;

			// 時間メーター
			int remainRollTime = ROLLTIMELIMIT - rolltime;
			engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / ROLLTIMELIMIT;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(remainRollTime <= 30*60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(remainRollTime <= 20*60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(remainRollTime <= 10*60) engine.meterColor = GameEngine.METER_COLOR_RED;

			// ロール終了
			if(rolltime >= ROLLTIMELIMIT) {
				engine.gameActive = false;
				engine.resetStatc();
				engine.stat = GameEngine.STAT_EXCELLENT;
			}
		}
	}

	/*
	 * ゲームオーバー時の処理
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		if(engine.statc[0] == 0) {
			secretGrade = engine.field.getSecretGrade();
		}
		return false;
	}

	/*
	 * 結果画面
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/3", EventReceiver.COLOR_RED);

		if(engine.statc[1] == 0) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
			String strGrade = String.format("%10s", tableGradeName[grade]);
			receiver.drawMenuFont(engine, playerID, 0, 3, strGrade);

			receiver.drawMenuFont(engine, playerID, 0, 4, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore = String.format("%10d", engine.statistics.score);
			receiver.drawMenuFont(engine, playerID, 0, 5, strScore);

			receiver.drawMenuFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
			String strLines = String.format("%10d", engine.statistics.lines);
			receiver.drawMenuFont(engine, playerID, 0, 7, strLines);

			receiver.drawMenuFont(engine, playerID, 0, 8, "LEVEL", EventReceiver.COLOR_BLUE);
			String strLevel = String.format("%10d", engine.statistics.level);
			receiver.drawMenuFont(engine, playerID, 0, 9, strLevel);

			receiver.drawMenuFont(engine, playerID, 0, 10, "TIME", EventReceiver.COLOR_BLUE);
			String strTime = String.format("%10s", GeneralUtil.getTime(lastGradeTime));
			receiver.drawMenuFont(engine, playerID, 0, 11, strTime);
			if(lastGradeTime != engine.statistics.time) {
				String strTime2 = String.format("%10s", GeneralUtil.getTime(engine.statistics.time));
				receiver.drawMenuFont(engine, playerID, 0, 12, strTime2);
			}

			if(rankingRank != -1) {
				receiver.drawMenuFont(engine, playerID, 0, 13, "RANK", EventReceiver.COLOR_BLUE);
				String strRank = String.format("%10d", rankingRank + 1);
				receiver.drawMenuFont(engine, playerID, 0, 14, strRank);
			}

			if(secretGrade > 4) {
				receiver.drawMenuFont(engine, playerID, 0, 15, "S. GRADE", EventReceiver.COLOR_BLUE);
				String strRank = String.format("%10s", tableGradeName[secretGrade-1]);
				receiver.drawMenuFont(engine, playerID, 0, 16, strRank);
			}
		} else if(engine.statc[1] == 1) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "SECTION", EventReceiver.COLOR_BLUE);

			for(int i = 0; i < sectiontime.length; i++) {
				if(sectiontime[i] > 0) {
					receiver.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectiontime[i]), sectionIsNewRecord[i]);
				}
			}

			if(sectionavgtime > 0) {
				receiver.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
				receiver.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(sectionavgtime));
			}
		} else if(engine.statc[1] == 2) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "LINE/MIN", EventReceiver.COLOR_BLUE);
			String strLPM = String.format("%10g", engine.statistics.lpm);
			receiver.drawMenuFont(engine, playerID, 0, 3, strLPM);

			receiver.drawMenuFont(engine, playerID, 0, 4, "SCORE/MIN", EventReceiver.COLOR_BLUE);
			String strSPM = String.format("%10g", engine.statistics.spm);
			receiver.drawMenuFont(engine, playerID, 0, 5, strSPM);

			receiver.drawMenuFont(engine, playerID, 0, 6, "PIECE", EventReceiver.COLOR_BLUE);
			String strPiece = String.format("%10d", engine.statistics.totalPieceLocked);
			receiver.drawMenuFont(engine, playerID, 0, 7, strPiece);

			receiver.drawMenuFont(engine, playerID, 0, 8, "PIECE/SEC", EventReceiver.COLOR_BLUE);
			String strPPS = String.format("%10g", engine.statistics.pps);
			receiver.drawMenuFont(engine, playerID, 0, 9, strPPS);

			if(grade == 18) {
				int pierRank = 0;
				for(int i = 1; i < tablePier21GradeTime.length; i++) {
					if(engine.statistics.time < tablePier21GradeTime[i]) pierRank = i;
				}
				receiver.drawMenuFont(engine, playerID, 0, 10, "PIER GRADE", EventReceiver.COLOR_BLUE);
				String strPierGrade = String.format("%10s", tablePier21GradeName[pierRank]);
				receiver.drawMenuFont(engine, playerID, 0, 11, strPierGrade);
			}
		}
	}

	/*
	 * 結果画面の処理
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		// ページ切り替え
		if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
			engine.statc[1]--;
			if(engine.statc[1] < 0) engine.statc[1] = 2;
			engine.playSE("change");
		}
		if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
			engine.statc[1]++;
			if(engine.statc[1] > 2) engine.statc[1] = 0;
			engine.playSE("change");
		}
		// セクションタイム表示切替
		if(engine.ctrl.isPush(Controller.BUTTON_F)) {
			engine.playSE("change");
			isShowBestSectionTime = !isShowBestSectionTime;
		}

		return false;
	}

	/*
	 * リプレイ保存
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(owner.replayProp);
		owner.replayProp.setProperty("result.grade.name", tableGradeName[grade]);
		owner.replayProp.setProperty("result.grade.number", grade);
		owner.replayProp.setProperty("grademania.version", version);

		// ランキング更新
		if((owner.replayMode == false) && (startlevel == 0) && (always20g == false) && (big == false) && (engine.ai == null)) {
			updateRanking(grade, engine.statistics.level, lastGradeTime);
			if(sectionAnyNewRecord) updateBestSectionTime();

			if((rankingRank != -1) || (sectionAnyNewRecord)) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * プロパティファイルからランキングを読み込み
	 * @param prop プロパティファイル
	 * @param ruleName ルール名
	 */
	private void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			rankingGrade[i] = prop.getProperty("grademania.ranking." + ruleName + ".grade." + i, 0);
			rankingLevel[i] = prop.getProperty("grademania.ranking." + ruleName + ".level." + i, 0);
			rankingTime[i] = prop.getProperty("grademania.ranking." + ruleName + ".time." + i, 0);
		}
		for(int i = 0; i < SECTION_MAX; i++) {
			bestSectionTime[i] = prop.getProperty("grademania.bestSectionTime." + ruleName + "." + i, DEFAULT_SECTION_TIME);
		}
	}

	/**
	 * プロパティファイルにランキングを保存
	 * @param prop プロパティファイル
	 * @param ruleName ルール名
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			prop.setProperty("grademania.ranking." + ruleName + ".grade." + i, rankingGrade[i]);
			prop.setProperty("grademania.ranking." + ruleName + ".level." + i, rankingLevel[i]);
			prop.setProperty("grademania.ranking." + ruleName + ".time." + i, rankingTime[i]);
		}
		for(int i = 0; i < SECTION_MAX; i++) {
			prop.setProperty("grademania.bestSectionTime." + ruleName + "." + i, bestSectionTime[i]);
		}
	}

	/**
	 * ランキングを更新
	 * @param gr 段位
	 * @param lv レベル
	 * @param time タイム
	 */
	private void updateRanking(int gr, int lv, int time) {
		rankingRank = checkRanking(gr, lv, time);

		if(rankingRank != -1) {
			// ランキングをずらす
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingGrade[i] = rankingGrade[i - 1];
				rankingLevel[i] = rankingLevel[i - 1];
				rankingTime[i] = rankingTime[i - 1];
			}

			// 新しいデータを登録
			rankingGrade[rankingRank] = gr;
			rankingLevel[rankingRank] = lv;
			rankingTime[rankingRank] = time;
		}
	}

	/**
	 * ランキングの順位を取得
	 * @param gr 段位
	 * @param lv レベル
	 * @param time タイム
	 * @return 順位(ランク外なら-1)
	 */
	private int checkRanking(int gr, int lv, int time) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(gr > rankingGrade[i]) {
				return i;
			} else if((gr == rankingGrade[i]) && (lv > rankingLevel[i])) {
				return i;
			} else if((gr == rankingGrade[i]) && (lv == rankingLevel[i]) && (time < rankingTime[i])) {
				return i;
			}
		}

		return -1;
	}


	/**
	 * ベストセクションタイム更新
	 */
	private void updateBestSectionTime() {
		for(int i = 0; i < SECTION_MAX; i++) {
			if(sectionIsNewRecord[i]) {
				bestSectionTime[i] = sectiontime[i];
			}
		}
	}
}
