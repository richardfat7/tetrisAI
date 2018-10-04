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

import java.util.Random;

import org.apache.log4j.Logger;
import org.game_host.hebo.nullpomino.game.component.BGMStatus;
import org.game_host.hebo.nullpomino.game.component.Controller;
import org.game_host.hebo.nullpomino.game.event.EventReceiver;
import org.game_host.hebo.nullpomino.game.play.GameEngine;
import org.game_host.hebo.nullpomino.game.play.GameManager;
import org.game_host.hebo.nullpomino.util.CustomProperties;
import org.game_host.hebo.nullpomino.util.GeneralUtil;

/**
 * GRADE MANIA 3モード
 */
public class GradeMania3Mode extends DummyMode {
	/** ログ */
	static final Logger log = Logger.getLogger(GradeMania3Mode.class);

	/** 現在のバージョン */
	private static final int CURRENT_VERSION = 2;

	/** セクションCOOL基準タイム */
	private static final int[] tableTimeCool =
	{
		3120, 3120, 2940, 2700, 2700, 2520, 2520, 2280, 2280, 0
	};

	/** セクションREGRET基準タイム */
	private static final int[] tableTimeRegret =
	{
		5400, 4500, 4500, 4080, 3600, 3600, 3000, 3000, 3000, 3000
	};

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

	/** AREテーブル */
	private static final int[] tableARE       = {23, 23, 23, 23, 23, 23, 23, 14, 10, 10,  4,  3,  2};

	/** ライン消去後AREテーブル */
	private static final int[] tableARELine   = {23, 23, 23, 23, 23, 23, 14, 10,  4,  4,  4,  3,  2};

	/** ライン消去時間テーブル */
	private static final int[] tableLineDelay = {40, 40, 40, 40, 40, 25, 16, 12,  6,  6,  6,  6,  6};

	/** 固定時間テーブル */
	private static final int[] tableLockDelay = {31, 31, 31, 31, 31, 31, 31, 31, 31, 18, 18, 16, 16};

	/** DASテーブル */
	private static final int[] tableDAS       = {15, 15, 15, 15, 15,  9,  9,  9,  9,  7,  7,  7,  7};

	/** BGMがフェードアウトするレベル */
	private static final int[] tableBGMFadeout = {485,685,-1};

	/** BGMが変わるレベル */
	private static final int[] tableBGMChange  = {500,700,-1};

	/** ライン消去時に入る段位ポイント */
	private static final int[][] tableGradePoint =
	{
		{10,10,10,10,10, 5, 5, 5, 5, 5, 2},
		{20,20,20,15,15,15,10,10,10,10,12},
		{40,30,30,30,20,20,20,15,15,15,13},
		{50,40,40,40,40,30,30,30,30,30,30},
	};

	/** 段位ポイントのコンボボーナス */
	private static final float[][] tableGradeComboBonus =
	{
		{1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f},
		{1.0f,1.2f,1.2f,1.4f,1.4f,1.4f,1.4f,1.5f,1.5f,2.0f},
		{1.0f,1.4f,1.5f,1.6f,1.7f,1.8f,1.9f,2.0f,2.1f,2.5f},
		{1.0f,1.5f,1.8f,2.0f,2.2f,2.3f,2.4f,2.5f,2.6f,3.0f},
	};

	/** 実際の段位を上げるのに必要な内部段位 */
	private static final int[] tableGradeChange =
	{
		1, 2, 3, 4, 5, 7, 9, 12, 15, 18, 19, 20, 23, 25, 27, 29, 31, -1
	};

	/** 段位ポイントが1つ減る時間 */
	private static final int[] tableGradeDecayRate =
	{
		125, 80, 80, 50, 45, 45, 45, 40, 40, 40, 40, 40, 30, 30, 30, 20, 20, 20, 20, 20, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 10, 10
	};

	///** 段位の数 */
	//private static final int GRADE_MAX = 33;

	/** 段位の名前 */
	private static final String[] tableGradeName =
	{
		 "9",  "8",  "7",  "6",  "5",  "4",  "3",  "2",  "1",	//  0～ 8
		"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",	//  9～17
		"M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9",	// 18～26
		 "M", "MK", "MV", "MO", "MM", "GM"						// 27～32
	};

	/** 裏段位の名前 */
	private static final String[] tableSecretGradeName =
	{
		 "9",  "8",  "7",  "6",  "5",  "4",  "3",  "2",  "1",	//  0～ 8
		"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",	//  9～17
		"GM"													// 18
	};

	/** LV999ロールの時間 */
	private static final int ROLLTIMELIMIT = 3238;

	/** ランキングに記録する数 */
	private static final int RANKING_MAX = 10;

	/** ランキングの種類 */
	private static final int RANKING_TYPE = 2;

	/** 段位履歴のサイズ */
	private static final int GRADE_HISTORY_SIZE = 7;

	/** 段位認定試験の発生確率(EXAM_CHANCE分の1の確率で発生) */
	private static final int EXAM_CHANCE = 3;

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

	/** 内部レベル */
	private int internalLevel;

	/** レベルが増えたフラグ */
	private boolean lvupflag;

	/** 最終結果などに表示される実際の段位 */
	private int grade;

	/** メインの段位 */
	private int gradeBasicReal;

	/** メインの段位の内部段位 */
	private int gradeBasicInternal;

	/** メインの段位の内部段位ポイント */
	private int gradeBasicPoint;

	/** メインの段位の内部段位ポイントが1つ減る時間 */
	private int gradeBasicDecay;

	/** 最後に段位が上がった時間 */
	private int lastGradeTime;

	/** ハードドロップした段数 */
	private int harddropBonus;

	/** コンボボーナス */
	private int comboValue;

	/** 直前に手に入れたスコア */
	private int lastscore;

	/** 獲得スコア表示がされる残り時間 */
	private int scgettime;

	/** このセクションでCOOLを出すとtrue */
	private boolean cool;

	/** COOL回数 */
	private int coolcount;

	/** 直前のセクションでCOOLを出したらtrue */
	private boolean previouscool;

	/** 直前のセクションでのレベル70通過タイム */
	private int coolprevtime;

	/** このセクションでCOOLチェックをしたらtrue */
	private boolean coolchecked;

	/** このセクションでCOOL表示をしたらtrue */
	private boolean cooldisplayed;

	/** COOLを表示する残りフレーム数 */
	private int cooldispframe;

	/** REGRETを表示する残りフレーム数 */
	private int regretdispframe;

	/** ロール経過時間 */
	private int rolltime;

	/** ロール完全クリアフラグ */
	private int rollclear;

	/** ロール開始フラグ */
	private boolean rollstarted;

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

	/** クリアしたセクション数 */
	private int sectionscomp;

	/** 平均セクションタイム */
	private int sectionavgtime;

	/** 直前のセクションタイム */
	private int sectionlasttime;

	/** 消えロール開始フラグ */
	private boolean mrollFlag;

	/** ロール中に稼いだポイント（段位上昇用） */
	private float rollPoints;

	/** ロール中に稼いだポイント（合計） */
	private float rollPointsTotal;

	/** ACメダル状態 */
	private int medalAC;

	/** STメダル状態 */
	private int medalST;

	/** SKメダル状態 */
	private int medalSK;

	/** COメダル状態 */
	private int medalCO;

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

	/** 段位表示 */
	private boolean gradedisp;

	/** LV500の足切りタイム */
	private int lv500torikan;

	/** 昇格・降格試験有効 */
	private boolean enableexam;

	/** バージョン */
	private int version;

	/** 今回のプレイのランキングでのランク */
	private int rankingRank;

	/** ランキングの段位 */
	private int[][] rankingGrade;

	/** ランキングのレベル */
	private int[][] rankingLevel;

	/** ランキングのタイム */
	private int[][] rankingTime;

	/** ランキングのロール完全クリアフラグ */
	private int[][] rankingRollclear;

	/** セクションタイム記録 */
	private int[][] bestSectionTime;

	/** 段位履歴（昇格・降格試験用） */
	private int[] gradeHistory;

	/** 昇格試験の目標段位 */
	private int promotionalExam;

	/** 現在の認定段位 */
	private int qualifiedGrade;

	/** 降格試験ポイント（30以上溜まると降格試験発生） */
	private int demotionPoints;

	/** 昇格試験フラグ */
	private boolean promotionFlag;

	/** 降格試験フラグ */
	private boolean demotionFlag;

	/** 降格試験での目標段位 */
	private int demotionExamGrade;

	/** 試験開始前演出のフレーム数 */
	private int readyframe;

	/** 試験終了演出のフレーム数 */
	private int passframe;

	/*
	 * モード名
	 */
	@Override
	public String getName() {
		return "GRADE MANIA 3";
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
		internalLevel = 0;
		lvupflag = true;
		grade = 0;
		gradeBasicReal = 0;
		gradeBasicInternal = 0;
		gradeBasicPoint = 0;
		gradeBasicDecay = 0;
		lastGradeTime = 0;
		harddropBonus = 0;
		comboValue = 0;
		lastscore = 0;
		scgettime = 0;
		cool = false;
		coolcount = 0;
		previouscool = false;
		coolprevtime = 0;
		coolchecked = false;
		cooldisplayed = false;
		cooldispframe = 0;
		regretdispframe = 0;
		rolltime = 0;
		rollclear = 0;
		rollstarted = false;
		secretGrade = 0;
		bgmlv = 0;
		gradeflash = 0;
		sectiontime = new int[SECTION_MAX];
		sectionIsNewRecord = new boolean[SECTION_MAX];
		sectionscomp = 0;
		sectionavgtime = 0;
		sectionlasttime = 0;
		mrollFlag = false;
		rollPoints = 0f;
		rollPointsTotal = 0f;
		medalAC = 0;
		medalST = 0;
		medalSK = 0;
		medalCO = 0;
		isShowBestSectionTime = false;
		startlevel = 0;
		alwaysghost = false;
		always20g = false;
		lvstopse = false;
		big = false;
		gradedisp = false;
		lv500torikan = 25200;
		enableexam = false;

		promotionalExam = 0;
		qualifiedGrade = 0;
		demotionPoints = 0;
		readyframe = 0;
		passframe = 0;
		gradeHistory = new int[GRADE_HISTORY_SIZE];
		promotionFlag = false;
		demotionFlag = false;
		demotionExamGrade = 0;

		rankingRank = -1;
		rankingGrade = new int[RANKING_MAX][RANKING_TYPE];
		rankingLevel = new int[RANKING_MAX][RANKING_TYPE];
		rankingTime = new int[RANKING_MAX][RANKING_TYPE];
		rankingRollclear = new int[RANKING_MAX][RANKING_TYPE];
		bestSectionTime = new int[SECTION_MAX][RANKING_TYPE];

		engine.tspinEnable = false;
		engine.b2bEnable = false;
		engine.framecolor = GameEngine.FRAME_COLOR_BLUE;
		engine.comboType = GameEngine.COMBO_TYPE_DOUBLE;
		engine.bighalf = true;
		engine.bigmove = true;
		engine.staffrollEnable = true;
		engine.staffrollNoDeath = false;

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			version = owner.replayProp.getProperty("grademania3.version", 0);

			for(int i = 0; i < SECTION_MAX; i++) {
				bestSectionTime[i][enableexam ? 1 : 0] = DEFAULT_SECTION_TIME;
			}

			if(enableexam) {
				promotionalExam = owner.replayProp.getProperty("grademania3.exam", 0);
				demotionPoints = owner.replayProp.getProperty("grademania3.demopoint", 0);
				demotionExamGrade = owner.replayProp.getProperty("grademania3.demotionExamGrade", 0);
				if (promotionalExam > 0) {
					promotionFlag = true;
					readyframe = 100;
					passframe = 600;
				} else if (demotionPoints >= 30) {
					demotionFlag = true;
					passframe = 600;
				}

				log.debug("** Exam data from replay START **");
				log.debug("Promotional Exam Grade:" + getGradeName(promotionalExam) + " (" + promotionalExam + ")");
				log.debug("Promotional Exam Flag:" + promotionFlag);
				log.debug("Demotion Points:" + demotionPoints);
				log.debug("Demotional Exam Grade:" + getGradeName(demotionExamGrade) + " (" + demotionExamGrade + ")");
				log.debug("Demotional Exam Flag:" + demotionFlag);
				log.debug("*** Exam data from replay END ***");
			}
		}

		owner.backgroundStatus.bg = startlevel;
	}

	/**
	 * プロパティファイルから設定を読み込み
	 * @param prop プロパティファイル
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("grademania3.startlevel", 0);
		alwaysghost = prop.getProperty("grademania3.alwaysghost", false);
		always20g = prop.getProperty("grademania3.always20g", false);
		lvstopse = prop.getProperty("grademania3.lvstopse", true);
		showsectiontime = prop.getProperty("grademania3.showsectiontime", false);
		big = prop.getProperty("grademania3.big", false);
		gradedisp = prop.getProperty("grademania3.gradedisp", false);
		lv500torikan = prop.getProperty("grademania3.lv500torikan", 25200);
		enableexam = prop.getProperty("grademania3.enableexam", false);
	}

	/**
	 * プロパティファイルに設定を保存
	 * @param prop プロパティファイル
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("grademania3.startlevel", startlevel);
		prop.setProperty("grademania3.alwaysghost", alwaysghost);
		prop.setProperty("grademania3.always20g", always20g);
		prop.setProperty("grademania3.lvstopse", lvstopse);
		prop.setProperty("grademania3.showsectiontime", showsectiontime);
		prop.setProperty("grademania3.big", big);
		prop.setProperty("grademania3.gradedisp", gradedisp);
		prop.setProperty("grademania3.lv500torikan", lv500torikan);
		prop.setProperty("grademania3.enableexam", enableexam);
	}

	/**
	 * ゲーム開始時のBGMを設定
	 * @param engine GameEngine
	 */
	private void setStartBgmlv(GameEngine engine) {
		bgmlv = 0;
		while((tableBGMChange[bgmlv] != -1) && (engine.statistics.level >= tableBGMChange[bgmlv])) bgmlv++;
	}

	/**
	 * 落下速度を更新
	 * @param engine GameEngine
	 */
	private void setSpeed(GameEngine engine) {
		if((always20g == true) || (engine.statistics.time >= 54000)) {
			engine.speed.gravity = -1;
		} else {
			while(internalLevel >= tableGravityChangeLevel[gravityindex]) gravityindex++;
			engine.speed.gravity = tableGravityValue[gravityindex];
		}

		if(engine.statistics.time >= 54000) {
			engine.speed.are = 2;
			engine.speed.areLine = 1;
			engine.speed.lineDelay = 3;
			engine.speed.lockDelay = 13;
			engine.speed.das = 5;
		} else {
			int section = internalLevel / 100;
			if(section > tableARE.length - 1) section = tableARE.length - 1;
			engine.speed.are = tableARE[section];
			engine.speed.areLine = tableARELine[section];
			engine.speed.lineDelay = tableLineDelay[section];
			engine.speed.lockDelay = tableLockDelay[section];
			engine.speed.das = tableDAS[section];
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
	 * STメダルチェック
	 * @param engine GameEngine
	 * @param sectionNumber セクション番号
	 */
	private void stMedalCheck(GameEngine engine, int sectionNumber) {
		int type = enableexam ? 1 : 0;
		int best = bestSectionTime[sectionNumber][type];

		if(sectionlasttime < best) {
			if(medalST < 3) {
				engine.playSE("medal");
				medalST = 3;
			}
			if(!owner.replayMode) {
				sectionIsNewRecord[sectionNumber] = true;
			}
		} else if((sectionlasttime < best + 300) && (medalST < 2)) {
			engine.playSE("medal");
			medalST = 2;
		} else if((sectionlasttime < best + 600) && (medalST < 1)) {
			engine.playSE("medal");
			medalST = 1;
		}
	}

	/**
	 * メダルの文字色を取得
	 * @param medalColor メダル状態
	 * @return メダルの文字色
	 */
	private int getMedalFontColor(int medalColor) {
		if(medalColor == 1) return EventReceiver.COLOR_RED;
		if(medalColor == 2) return EventReceiver.COLOR_WHITE;
		if(medalColor == 3) return EventReceiver.COLOR_YELLOW;
		return -1;
	}

	/**
	 * COOLのチェック
	 * @param engine GameEngine
	 */
	private void checkCool(GameEngine engine) {
		// COOLチェック
		if((engine.statistics.level % 100 >= 70) && (coolchecked == false)) {
			int section = engine.statistics.level / 100;

			if( (sectiontime[section] <= tableTimeCool[section]) &&
				((previouscool == false) || ((previouscool == true) && (sectiontime[section] <= coolprevtime + 120))) )
			{
				cool = true;
			}
			coolprevtime = sectiontime[section];
			coolchecked = true;
		}

		// COOL表示
		if((engine.statistics.level % 100 >= 82) && (cool == true) && (cooldisplayed == false)) {
			engine.playSE("cool");
			cooldispframe = 180;
			cooldisplayed = true;
		}
	}

	/**
	 * REGRETのチェック
	 * @param engine GameEngine
	 * @param levelb ライン消去前のレベル
	 */
	private void checkRegret(GameEngine engine, int levelb) {
		if(sectionlasttime > tableTimeRegret[levelb / 100]) {
			previouscool = false;

			coolcount--;
			if(coolcount < 0) coolcount = 0;

			grade--;
			if(grade < 0) grade = 0;
			gradeflash = 180;

			regretdispframe = 180;
			engine.playSE("regret");
		}
	}

	/**
	 * @return 何らかの試験中ならtrue
	 */
	private boolean isAnyExam() {
		return promotionFlag || demotionFlag;
	}

	/**
	 * 段位名を取得
	 * @param g 段位番号
	 * @return 段位名(範囲外ならN/A)
	 */
	private String getGradeName(int g) {
		if((g < 0) || (g >= tableGradeName.length)) return "N/A";
		return tableGradeName[g];
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
				if(engine.statc[2] < 0) engine.statc[2] = 8;
				engine.playSE("cursor");
			}
			// 下
			if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
				engine.statc[2]++;
				if(engine.statc[2] > 8) engine.statc[2] = 0;
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
				case 6:
					gradedisp = !gradedisp;
					break;
				case 7:
					lv500torikan += 60 * change;
					if(lv500torikan < 0) lv500torikan = 72000;
					if(lv500torikan > 72000) lv500torikan = 0;
					break;
				case 8:
					enableexam = !enableexam;
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

				Random rand = new Random();
				if(!always20g && !big && enableexam && (rand.nextInt(EXAM_CHANCE) == 0)) {
					setPromotionalGrade();
					if(promotionalExam > qualifiedGrade) {
						promotionFlag = true;
						readyframe = 100;
						passframe = 600;
					} else if(demotionPoints >= 30) {
						demotionFlag = true;
						demotionExamGrade = qualifiedGrade;
						passframe = 600;
						demotionPoints = 0;
					}

					log.debug("** Exam debug log START **");
					log.debug("Current Qualified Grade:" + getGradeName(qualifiedGrade) + " (" + qualifiedGrade + ")");
					log.debug("Promotional Exam Grade:" + getGradeName(promotionalExam) + " (" + promotionalExam + ")");
					log.debug("Promotional Exam Flag:" + promotionFlag);
					log.debug("Demotion Points:" + demotionPoints);
					log.debug("Demotional Exam Grade:" + getGradeName(demotionExamGrade) + " (" + demotionExamGrade + ")");
					log.debug("Demotional Exam Flag:" + demotionFlag);
					log.debug("*** Exam debug log END ***");
				}

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
		receiver.drawMenuFont(engine, playerID, 0, 12, "GRADE DISP", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 1, 13, GeneralUtil.getONorOFF(gradedisp), (engine.statc[2] == 6));
		receiver.drawMenuFont(engine, playerID, 0, 14, "LV500LIMIT", EventReceiver.COLOR_BLUE);
		String strTorikan = GeneralUtil.getTime(lv500torikan);
		if(lv500torikan == 0) strTorikan = "NONE";
		receiver.drawMenuFont(engine, playerID, 1, 15, strTorikan, (engine.statc[2] == 7));
		receiver.drawMenuFont(engine, playerID, 0, 16, "EXAM", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 1, 17, GeneralUtil.getONorOFF(enableexam), (engine.statc[2] == 8));
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

		internalLevel = engine.statistics.level;

		owner.backgroundStatus.bg = engine.statistics.level / 100;

		engine.big = big;

		setSpeed(engine);
		setStartBgmlv(engine);
		owner.bgmStatus.bgm = bgmlv;
	}

	/*
	 * スコア表示
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		receiver.drawScoreFont(engine, playerID, 0, 0, "GRADE MANIA 3 " + (enableexam ? "(+EXAM)" : ""), EventReceiver.COLOR_CYAN);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) ) {
			if((startlevel == 0) && (!big) && (!always20g) && (!owner.replayMode) && (engine.ai == null)) {
				if(!isShowBestSectionTime) {
					// ランキング
					receiver.drawScoreFont(engine, playerID, 3, 2, "GRADE LEVEL TIME", EventReceiver.COLOR_BLUE);

					for(int i = 0; i < RANKING_MAX; i++) {
						int type = enableexam ? 1 : 0;
						int gcolor = EventReceiver.COLOR_WHITE;
						if((rankingRollclear[i][type] == 1) || (rankingRollclear[i][type] == 3)) gcolor = EventReceiver.COLOR_GREEN;
						if((rankingRollclear[i][type] == 2) || (rankingRollclear[i][type] == 4)) gcolor = EventReceiver.COLOR_ORANGE;

						receiver.drawScoreFont(engine, playerID, 0, 3 + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW);
						receiver.drawScoreFont(engine, playerID, 3, 3 + i, getGradeName(rankingGrade[i][type]), gcolor);
						receiver.drawScoreFont(engine, playerID, 9, 3 + i, String.valueOf(rankingLevel[i][type]), (i == rankingRank));
						receiver.drawScoreFont(engine, playerID, 15, 3 + i, GeneralUtil.getTime(rankingTime[i][type]), (i == rankingRank));
					}

					if(enableexam) {
						receiver.drawScoreFont(engine, playerID, 0, 14, "QUALIFIED GRADE", EventReceiver.COLOR_YELLOW);
						receiver.drawScoreFont(engine, playerID, 0, 15, getGradeName(qualifiedGrade));
					}

					receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW SECTION TIME", EventReceiver.COLOR_GREEN);
				} else {
					// セクションタイム
					receiver.drawScoreFont(engine, playerID, 0, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);

					int totalTime = 0;
					for(int i = 0; i < SECTION_MAX; i++) {
						int type = enableexam ? 1 : 0;

						int temp = Math.min(i * 100, 999);
						int temp2 = Math.min(((i + 1) * 100) - 1, 999);

						String strSectionTime;
						strSectionTime = String.format("%3d-%3d %s", temp, temp2, GeneralUtil.getTime(bestSectionTime[i][type]));

						receiver.drawScoreFont(engine, playerID, 0, 3 + i, strSectionTime, (sectionIsNewRecord[i] && !isAnyExam()));

						totalTime += bestSectionTime[i][type];
					}

					receiver.drawScoreFont(engine, playerID, 0, 14, "TOTAL", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(totalTime));
					receiver.drawScoreFont(engine, playerID, 9, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 9, 15, GeneralUtil.getTime(totalTime / SECTION_MAX));

					receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW RANKING", EventReceiver.COLOR_GREEN);
				}
			}
		} else {
			if(promotionFlag) {
				// 試験段位
				receiver.drawScoreFont(engine, playerID, 0, 5, "QUALIFY", EventReceiver.COLOR_YELLOW);
				receiver.drawScoreFont(engine, playerID, 0, 6, getGradeName(promotionalExam));
			}

			if(gradedisp) {
				// 段位
				int rgrade = grade;
				if(enableexam && (rgrade >= 32) && (qualifiedGrade < 32)) rgrade = 31;

				receiver.drawScoreFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 3, getGradeName(rgrade), ((gradeflash > 0) && (gradeflash % 4 == 0)));

				if(!promotionFlag) {
					// スコア
					receiver.drawScoreFont(engine, playerID, 0, 5, "SCORE", EventReceiver.COLOR_BLUE);
					String strScore;
					if((lastscore == 0) || (scgettime <= 0)) {
						strScore = String.valueOf(engine.statistics.score);
					} else {
						strScore = String.valueOf(engine.statistics.score) + "\n(+" + String.valueOf(lastscore) + ")";
					}
					receiver.drawScoreFont(engine, playerID, 0, 6, strScore);
				}
			}

			// レベル
			receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
			int tempLevel = engine.statistics.level;
			if(tempLevel < 0) tempLevel = 0;
			String strLevel = String.format("%3d", tempLevel);
			receiver.drawScoreFont(engine, playerID, 0, 10, strLevel);

			int speed = engine.speed.gravity / 128;
			if(engine.speed.gravity < 0) speed = 40;
			receiver.drawSpeedMeter(engine, playerID, 0, 11, speed);

			receiver.drawScoreFont(engine, playerID, 0, 12, String.format("%3d", nextseclv));

			// タイム
			receiver.drawScoreFont(engine, playerID, 0, 14, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(engine.statistics.time));

			// ロール残り時間
			if((engine.gameActive) && (engine.ending == 2)) {
				int time = ROLLTIMELIMIT - rolltime;
				if(time < 0) time = 0;
				receiver.drawScoreFont(engine, playerID, 0, 17, "ROLL TIME", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
			}

			if(regretdispframe > 0) {
				// REGRET表示
				receiver.drawMenuFont(engine,playerID,2,21,"REGRET",(regretdispframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_ORANGE);
			} else if(cooldispframe > 0) {
				// COOL表示
				receiver.drawMenuFont(engine,playerID,2,21,"COOL!!",(cooldispframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_ORANGE);
			}

			// メダル
			if(medalAC >= 1) receiver.drawScoreFont(engine, playerID, 0, 20, "AC", getMedalFontColor(medalAC));
			if(medalST >= 1) receiver.drawScoreFont(engine, playerID, 3, 20, "ST", getMedalFontColor(medalST));
			if(medalSK >= 1) receiver.drawScoreFont(engine, playerID, 0, 21, "SK", getMedalFontColor(medalSK));
			if(medalCO >= 1) receiver.drawScoreFont(engine, playerID, 3, 21, "CO", getMedalFontColor(medalCO));

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

						receiver.drawScoreFont(engine, playerID, 12, 3 + i, strSectionTime, (sectionIsNewRecord[i] == true));
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
	 * Ready→Goの処理
	 */
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		if(promotionFlag) {
			engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;

			if(readyframe == 100) engine.playSE("tspin3");

			if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_B))
				readyframe = 0;

			if(readyframe > 0) {
				readyframe--;
				return true;
			}
		} else if(demotionFlag) {
			engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
			engine.playSE("danger");
		}

		return false;
	}

	/*
	 * Ready→Goのときの描画
	 */
	@Override
	public void renderReady(GameEngine engine, int playerID) {
		if(promotionFlag && readyframe > 0) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "PROMOTION", EventReceiver.COLOR_YELLOW);
			receiver.drawMenuFont(engine, playerID, 6, 3, "EXAM", EventReceiver.COLOR_YELLOW);
			receiver.drawMenuFont(engine, playerID, 4, 6, getGradeName(promotionalExam), (readyframe % 4 ==0),
				EventReceiver.COLOR_WHITE, EventReceiver.COLOR_ORANGE);
		}
	}

	/*
	 * 移動中の処理
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// 新規ピース出現時
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (!lvupflag)) {
			// レベルアップ
			if(engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				internalLevel++;
				if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) engine.playSE("levelstop");
			}
			levelUp(engine);

			// ハードドロップボーナス初期化
			harddropBonus = 0;
		}
		if( (engine.ending == 0) && (engine.statc[0] > 0) && ((version >= 1) || (engine.holdDisable == false)) ) {
			lvupflag = false;
		}

		// 段位ポイント減少
		if((engine.timerActive == true) && (gradeBasicPoint > 0) && (engine.combo <= 0) && (engine.lockDelayNow < engine.getLockDelay() - 1)) {
			gradeBasicDecay++;

			int index = gradeBasicInternal;
			if(index > tableGradeDecayRate.length - 1) index = tableGradeDecayRate.length - 1;

			if(gradeBasicDecay >= tableGradeDecayRate[index]) {
				gradeBasicDecay = 0;
				gradeBasicPoint--;
			}
		}

		// エンディングスタート
		if((engine.ending == 2) && (rollstarted == false)) {
			rollstarted = true;

			if(mrollFlag) {
				engine.blockHidden = engine.ruleopt.lockflash;
				engine.blockHiddenAnim = false;
				engine.blockShowOutlineOnly = true;
			} else {
				engine.blockHidden = 300;
				engine.blockHiddenAnim = true;
			}

			owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
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
				internalLevel++;
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

		// COOLチェック
		checkCool(engine);

		// 速度変更
		setSpeed(engine);

		// LV100到達でゴーストを消す
		if((engine.statistics.level >= 100) && (!alwaysghost)) engine.ghost = false;

		// BGMフェードアウト
		int tempLevel = internalLevel;
		if(cool) tempLevel += 100;

		if((tableBGMFadeout[bgmlv] != -1) && (tempLevel >= tableBGMFadeout[bgmlv])) {
			owner.bgmStatus.fadesw  = true;
		}

		// BGM切り替え
		if((tableBGMChange[bgmlv] != -1) && (internalLevel >= tableBGMChange[bgmlv])) {
			bgmlv++;
			owner.bgmStatus.fadesw = false;
			owner.bgmStatus.bgm = bgmlv;
		}
	}

	/*
	 * スコア計算
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// コンボ
		if(lines == 0) {
			comboValue = 1;
		} else {
			comboValue = comboValue + (2 * lines) - 2;
			if(comboValue < 1) comboValue = 1;
		}

		if((lines >= 1) && (engine.ending == 0)) {
			// 段位ポイント
			int index = gradeBasicInternal;
			if(index > 10) index = 10;
			int basepoint = tableGradePoint[lines - 1][index];

			int indexcombo = engine.combo - 1;
			if(indexcombo < 0) indexcombo = 0;
			if(indexcombo > tableGradeComboBonus[lines - 1].length - 1) indexcombo = tableGradeComboBonus[lines - 1].length - 1;
			float combobonus = tableGradeComboBonus[lines - 1][indexcombo];

			int levelbonus = 1 + (engine.statistics.level / 250);

			float point = (basepoint * combobonus) * levelbonus;
			gradeBasicPoint += (int)point;

			// 内部段位上昇
			if(gradeBasicPoint >= 100) {
				gradeBasicPoint = 0;
				gradeBasicDecay = 0;
				gradeBasicInternal++;

				if((tableGradeChange[gradeBasicReal] != -1) && (gradeBasicInternal >= tableGradeChange[gradeBasicReal])) {
					if(gradedisp) engine.playSE("gradeup");
					gradeBasicReal++;
					grade++;
					if(grade > 31) grade = 31;
					gradeflash = 180;
					lastGradeTime = engine.statistics.time;
				}
			}

			// 4ライン消しカウント
			if(lines >= 4) {
				// SKメダル
				if(big == true) {
					if((engine.statistics.totalFour == 1) || (engine.statistics.totalFour == 2) || (engine.statistics.totalFour == 4)) {
						engine.playSE("medal");
						medalSK++;
					}
				} else {
					if((engine.statistics.totalFour == 10) || (engine.statistics.totalFour == 20) || (engine.statistics.totalFour == 35)) {
						engine.playSE("medal");
						medalSK++;
					}
				}
			}

			// ACメダル
			if(engine.field.isEmpty()) {
				engine.playSE("bravo");

				if(medalAC < 3) {
					engine.playSE("medal");
					medalAC++;
				}
			}

			// COメダル
			if(big == true) {
				if((engine.combo >= 2) && (medalCO < 1)) {
					engine.playSE("medal");
					medalCO = 1;
				} else if((engine.combo >= 3) && (medalCO < 2)) {
					engine.playSE("medal");
					medalCO = 2;
				} else if((engine.combo >= 4) && (medalCO < 3)) {
					engine.playSE("medal");
					medalCO = 3;
				}
			} else {
				if((engine.combo >= 4) && (medalCO < 1)) {
					engine.playSE("medal");
					medalCO = 1;
				} else if((engine.combo >= 5) && (medalCO < 2)) {
					engine.playSE("medal");
					medalCO = 2;
				} else if((engine.combo >= 7) && (medalCO < 3)) {
					engine.playSE("medal");
					medalCO = 3;
				}
			}

			// レベルアップ
			int levelb = engine.statistics.level;

			int levelplus = lines;
			if(lines == 3) levelplus = 4;
			if(lines >= 4) levelplus = 6;

			engine.statistics.level += levelplus;
			internalLevel += levelplus;

			levelUp(engine);

			if(engine.statistics.level >= 999) {
				// エンディング
				engine.statistics.level = 999;
				engine.timerActive = false;
				engine.ending = 1;
				rollclear = 1;

				lastGradeTime = engine.statistics.time;

				// セクションタイムを記録
				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// STメダル
				stMedalCheck(engine, levelb / 100);

				// REGRET判定
				checkRegret(engine, levelb);

				// 条件を全て満たしているなら消えロール発動
				if(version >= 2) {
					if((grade >= 24) && (coolcount >= 9)) mrollFlag = true;
				} else {
					if((grade >= 15) && (coolcount >= 9)) mrollFlag = true;
				}
			} else if((nextseclv == 500) && (engine.statistics.level >= 500) && (lv500torikan > 0) && (engine.statistics.time > lv500torikan) &&
					  (!promotionFlag) && (!demotionFlag)) {
				// レベル500とりカン
				engine.statistics.level = 999;
				engine.timerActive = false;
				engine.gameActive = false;
				engine.staffrollEnable = false;
				engine.ending = 1;

				secretGrade = engine.field.getSecretGrade();

				// セクションタイムを記録
				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// STメダル
				stMedalCheck(engine, levelb / 100);

				// REGRET判定
				checkRegret(engine, levelb);
			} else if(engine.statistics.level >= nextseclv) {
				// 次のセクション
				engine.playSE("levelup");

				// 背景切り替え
				owner.backgroundStatus.fadesw = true;
				owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.fadebg = nextseclv / 100;

				// BGM切り替え
				if((tableBGMChange[bgmlv] != -1) && (internalLevel >= tableBGMChange[bgmlv])) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv;
				}

				// セクションタイムを記録
				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// STメダル
				stMedalCheck(engine, levelb / 100);

				// REGRET判定
				checkRegret(engine, levelb);

				// COOLを取ってたら
				if(cool == true) {
					previouscool = true;

					coolcount++;
					grade++;
					if(grade > 31) grade = 31;
					gradeflash = 180;

					if(gradedisp) engine.playSE("gradeup");

					internalLevel += 100;
				} else {
					previouscool = false;
				}

				cool = false;
				coolchecked = false;
				cooldisplayed = false;

				// 次のセクションレベルを更新
				nextseclv += 100;
				if(nextseclv > 999) nextseclv = 999;
			} else if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) {
				engine.playSE("levelstop");
			}

			// スコア計算
			int manuallock = 0;
			if(engine.manualLock == true) manuallock = 1;

			int bravo = 1;
			if(engine.field.isEmpty()) bravo = 2;

			int speedBonus = engine.getLockDelay() - engine.statc[0];
			if(speedBonus < 0) speedBonus = 0;

			lastscore = ( ((levelb + lines) / 4 + engine.softdropFall + manuallock + harddropBonus) * lines * comboValue + speedBonus +
						(engine.statistics.level / 2) ) * bravo;

			engine.statistics.score += lastscore;
			scgettime = 120;
		} else if((lines >= 1) && (engine.ending == 2)) {
			// ロール中のライン消去
			float points = 0f;
			if(mrollFlag == false) {
				if(lines == 1) points = 0.04f;
				if(lines == 2) points = 0.08f;
				if(lines == 3) points = 0.12f;
				if(lines == 4) points = 0.26f;
			} else {
				if(lines == 1) points = 0.1f;
				if(lines == 2) points = 0.2f;
				if(lines == 3) points = 0.3f;
				if(lines == 4) points = 1.0f;
			}
			rollPoints += points;
			rollPointsTotal += points;

			while((rollPoints >= 1.0f) && (grade < 31)) {
				rollPoints -= 1.0f;
				grade++;
				gradeflash = 180;
				if(gradedisp) engine.playSE("gradeup");
			}
		}
	}

	/*
	 * ハードドロップしたときの処理
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		if(fall * 2 > harddropBonus) harddropBonus = fall * 2;
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

		// REGRET表示
		if(regretdispframe > 0) regretdispframe--;

		// COOL表示
		if(cooldispframe > 0) cooldispframe--;

		// 15分経過
		if(engine.statistics.time >= 54000) {
			setSpeed(engine);
		}

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
				rollclear = 2;

				secretGrade = engine.field.getSecretGrade();

				if(mrollFlag == false) {
					rollPoints += 0.5f;
					rollPointsTotal += 0.5f;
				} else {
					rollPoints += 1.6f;
					rollPointsTotal += 1.6f;
				}

				while(rollPoints >= 1.0f) {
					rollPoints -= 1.0f;
					grade++;
					if(grade > 32) grade = 32;
					gradeflash = 180;
					if(gradedisp) engine.playSE("gradeup");
				}

				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;
				engine.blockShowOutlineOnly = false;

				engine.gameActive = false;
				engine.resetStatc();
				engine.stat = GameEngine.STAT_EXCELLENT;
			}
		}
	}

	/*
	 * ゲームオーバー
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		// This code block will executed only once
		if(engine.statc[0] == 0) {
			secretGrade = engine.field.getSecretGrade();

			if(enableexam) {
				if(grade < (qualifiedGrade - 7)) {
					demotionPoints += (qualifiedGrade - grade - 7);
				}
				if(promotionFlag && grade >= promotionalExam) {
					qualifiedGrade = promotionalExam;
					demotionPoints = 0;
				}
				if(demotionFlag && grade < demotionExamGrade) {
					qualifiedGrade = demotionExamGrade - 1;
					if(qualifiedGrade < 0) qualifiedGrade = 0;
				}

				log.debug("** Exam result log START **");
				log.debug("Current Qualified Grade:" + getGradeName(qualifiedGrade) + " (" + qualifiedGrade + ")");
				log.debug("Promotional Exam Grade:" + getGradeName(promotionalExam) + " (" + promotionalExam + ")");
				log.debug("Promotional Exam Flag:" + promotionFlag);
				log.debug("Demotion Points:" + demotionPoints);
				log.debug("Demotional Exam Grade:" + getGradeName(demotionExamGrade) + " (" + demotionExamGrade + ")");
				log.debug("Demotional Exam Flag:" + demotionFlag);
				log.debug("*** Exam result log END ***");
			}
		}

		return false;
	}

	/*
	 * 結果画面
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		if(passframe > 0) {
			if (promotionFlag) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "PROMOTION", EventReceiver.COLOR_YELLOW);
				receiver.drawMenuFont(engine, playerID, 6, 3, "EXAM", EventReceiver.COLOR_YELLOW);
				receiver.drawMenuFont(engine, playerID, 4, 6, getGradeName(promotionalExam), (passframe % 4 == 0),
						EventReceiver.COLOR_WHITE, EventReceiver.COLOR_ORANGE);

				if(passframe < 420) {
					if(grade < promotionalExam) {
						receiver.drawMenuFont(engine,playerID,3,11,"FAIL",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_RED);
					} else {
						receiver.drawMenuFont(engine,playerID,2,11,"PASS!!",(passframe % 4 == 0),EventReceiver.COLOR_ORANGE,EventReceiver.COLOR_YELLOW);
					}
				}
			} else if (demotionFlag) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "DEMOTION", EventReceiver.COLOR_RED);
				receiver.drawMenuFont(engine, playerID, 6, 3, "EXAM", EventReceiver.COLOR_RED);

				if(passframe < 420) {
					if(grade < demotionExamGrade) {
						receiver.drawMenuFont(engine,playerID,3,11,"FAIL",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_RED);
					} else {
						receiver.drawMenuFont(engine,playerID,3,11,"PASS",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_YELLOW);
					}
				}
			}
		} else {
			receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/3", EventReceiver.COLOR_RED);

			if(engine.statc[1] == 0) {
				int rgrade = grade;
				if(enableexam && (rgrade >= 32) && (qualifiedGrade < 32)) rgrade = 31;
				int gcolor = EventReceiver.COLOR_WHITE;
				if((rollclear == 1) || (rollclear == 3)) gcolor = EventReceiver.COLOR_GREEN;
				if((rollclear == 2) || (rollclear == 4)) gcolor = EventReceiver.COLOR_ORANGE;
				if((grade >= 32) && (engine.statc[2] % 2 == 0)) gcolor = EventReceiver.COLOR_YELLOW;
				receiver.drawMenuFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
				String strGrade = String.format("%10s", getGradeName(rgrade));
				receiver.drawMenuFont(engine, playerID, 0, 3, strGrade, gcolor);

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
					String strRank = String.format("%10s", tableSecretGradeName[secretGrade-1]);
					receiver.drawMenuFont(engine, playerID, 0, 16, strRank);
				}
			} else if(engine.statc[1] == 1) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "SECTION", EventReceiver.COLOR_BLUE);

				for(int i = 0; i < sectiontime.length; i++) {
					if(sectiontime[i] > 0) {
						receiver.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectiontime[i]), (sectionIsNewRecord[i] == true));
					}
				}

				if(sectionavgtime > 0) {
					receiver.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(sectionavgtime));
				}
			} else if(engine.statc[1] == 2) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "MEDAL", EventReceiver.COLOR_BLUE);
				if(medalAC >= 1) receiver.drawMenuFont(engine, playerID, 5, 3, "AC", getMedalFontColor(medalAC));
				if(medalST >= 1) receiver.drawMenuFont(engine, playerID, 8, 3, "ST", getMedalFontColor(medalST));
				if(medalSK >= 1) receiver.drawMenuFont(engine, playerID, 5, 4, "SK", getMedalFontColor(medalSK));
				if(medalCO >= 1) receiver.drawMenuFont(engine, playerID, 8, 4, "CO", getMedalFontColor(medalCO));

				if(rollPointsTotal > 0) {
					receiver.drawMenuFont(engine, playerID, 0, 6, "ROLL POINT", EventReceiver.COLOR_BLUE);
					String strRollPointsTotal = String.format("%10g", rollPointsTotal);
					receiver.drawMenuFont(engine, playerID, 0, 7, strRollPointsTotal);
				}

				receiver.drawMenuFont(engine, playerID, 0, 9, "LINE/MIN", EventReceiver.COLOR_BLUE);
				String strLPM = String.format("%10g", engine.statistics.lpm);
				receiver.drawMenuFont(engine, playerID, 0, 10, strLPM);

				receiver.drawMenuFont(engine, playerID, 0, 11, "SCORE/MIN", EventReceiver.COLOR_BLUE);
				String strSPM = String.format("%10g", engine.statistics.spm);
				receiver.drawMenuFont(engine, playerID, 0, 12, strSPM);

				receiver.drawMenuFont(engine, playerID, 0, 13, "PIECE", EventReceiver.COLOR_BLUE);
				String strPiece = String.format("%10d", engine.statistics.totalPieceLocked);
				receiver.drawMenuFont(engine, playerID, 0, 14, strPiece);

				receiver.drawMenuFont(engine, playerID, 0, 15, "PIECE/SEC", EventReceiver.COLOR_BLUE);
				String strPPS = String.format("%10g", engine.statistics.pps);
				receiver.drawMenuFont(engine, playerID, 0, 16, strPPS);
			}
		}
	}

	/*
	 * 結果画面の処理
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		if(passframe > 0) {
			engine.allowTextRenderByReceiver = false; // Turn off RETRY/END menu

			if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_B)) {
				if(passframe > 420)
					passframe = 420;
				else if(passframe < 300)
					passframe = 0;
			}

			if(promotionFlag) {
				if(passframe == 420) {
					if(grade >= promotionalExam) {
						engine.playSE("excellent");
					} else {
						engine.playSE("regret");
					}
				}
			} else if(demotionFlag) {
				if(passframe == 420) {
					if(grade >= qualifiedGrade) {
						engine.playSE("gradeup");
					} else {
						engine.playSE("gameover");
					}
				}
			}

			passframe--;
			return true;
		}

		engine.allowTextRenderByReceiver = true;

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

		engine.statc[2]++;

		return false;
	}

	/*
	 * リプレイ保存
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(owner.replayProp);
		owner.replayProp.setProperty("result.grade.name", getGradeName(grade));
		owner.replayProp.setProperty("result.grade.number", grade);
		owner.replayProp.setProperty("grademania3.version", version);
		owner.replayProp.setProperty("grademania3.exam", (promotionFlag ? promotionalExam : 0));
		owner.replayProp.setProperty("grademania3.demopoint", demotionPoints);
		owner.replayProp.setProperty("grademania3.demotionExamGrade", demotionExamGrade);

		// ランキング更新
		if((owner.replayMode == false) && (startlevel == 0) && (always20g == false) && (big == false) && (engine.ai == null)) {
			int rgrade = grade;
			if(enableexam && (rgrade >= 32) && (qualifiedGrade < 32)) {
				rgrade = 31;
			}
			if(!enableexam || !isAnyExam()) {
				updateRanking(rgrade, engine.statistics.level, lastGradeTime, rollclear, enableexam ? 1 : 0);
			} else {
				rankingRank = -1;
			}

			if(enableexam) updateGradeHistory(grade);

			if((medalST == 3) && !isAnyExam()) updateBestSectionTime();

			if((rankingRank != -1) || (enableexam) || (medalST == 3)) {
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
			for(int j = 0; j < RANKING_TYPE; j++) {
				if(j == 0) {
					rankingGrade[i][j] = prop.getProperty("grademania3.ranking." + ruleName + ".grade." + i, 0);
					rankingLevel[i][j] = prop.getProperty("grademania3.ranking." + ruleName + ".level." + i, 0);
					rankingTime[i][j] = prop.getProperty("grademania3.ranking." + ruleName + ".time." + i, 0);
					rankingRollclear[i][j] = prop.getProperty("grademania3.ranking." + ruleName + ".rollclear." + i, 0);
				} else {
					rankingGrade[i][j] = prop.getProperty("grademania3.ranking.exam." + ruleName + ".grade." + i, 0);
					rankingLevel[i][j] = prop.getProperty("grademania3.ranking.exam." + ruleName + ".level." + i, 0);
					rankingTime[i][j] = prop.getProperty("grademania3.ranking.exam." + ruleName + ".time." + i, 0);
					rankingRollclear[i][j] = prop.getProperty("grademania3.ranking.exam." + ruleName + ".rollclear." + i, 0);
				}
			}
		}

		for(int i = 0; i < SECTION_MAX; i++) {
			for(int j = 0; j < RANKING_TYPE; j++) {
				bestSectionTime[i][j] = prop.getProperty("grademania3.bestSectionTime." + j + "." + ruleName + "." + i, DEFAULT_SECTION_TIME);
			}
		}

		for(int i = 0; i < GRADE_HISTORY_SIZE; i++) {
			gradeHistory[i] = prop.getProperty("grademania3.gradehistory." + ruleName + "." + i, -1);
		}
		qualifiedGrade = prop.getProperty("grademania3.qualified." + ruleName, 0);
		demotionPoints = prop.getProperty("grademania3.demopoint." + ruleName, 0);
	}

	/**
	 * プロパティファイルにランキングを保存
	 * @param prop プロパティファイル
	 * @param ruleName ルール名
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < RANKING_TYPE; j++) {
				if(j == 0) {
					prop.setProperty("grademania3.ranking." + ruleName + ".grade." + i, rankingGrade[i][j]);
					prop.setProperty("grademania3.ranking." + ruleName + ".level." + i, rankingLevel[i][j]);
					prop.setProperty("grademania3.ranking." + ruleName + ".time." + i, rankingTime[i][j]);
					prop.setProperty("grademania3.ranking." + ruleName + ".rollclear." + i, rankingRollclear[i][j]);
				} else {
					prop.setProperty("grademania3.ranking.exam." + ruleName + ".grade." + i, rankingGrade[i][j]);
					prop.setProperty("grademania3.ranking.exam." + ruleName + ".level." + i, rankingLevel[i][j]);
					prop.setProperty("grademania3.ranking.exam." + ruleName + ".time." + i, rankingTime[i][j]);
					prop.setProperty("grademania3.ranking.exam." + ruleName + ".rollclear." + i, rankingRollclear[i][j]);
				}
			}
		}

		for(int i = 0; i < SECTION_MAX; i++) {
			for(int j = 0; j < RANKING_TYPE; j++) {
				prop.setProperty("grademania3.bestSectionTime." + j + "." + ruleName + "." + i, bestSectionTime[i][j]);
			}
		}

		for(int i = 0; i < GRADE_HISTORY_SIZE; i++) {
			prop.setProperty("grademania3.gradehistory." + ruleName+ "." + i, gradeHistory[i]);
		}
		prop.setProperty("grademania3.qualified." + ruleName, qualifiedGrade);
		prop.setProperty("grademania3.demopoint." + ruleName, demotionPoints);
	}

	/**
	 * ランキングを更新
	 * @param gr 段位
	 * @param lv レベル
	 * @param time タイム
	 */
	private void updateRanking(int gr, int lv, int time, int clear, int type) {
		rankingRank = checkRanking(gr, lv, time, clear, type);

		if(rankingRank != -1) {
			// ランキングをずらす
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingGrade[i][type] = rankingGrade[i - 1][type];
				rankingLevel[i][type] = rankingLevel[i - 1][type];
				rankingTime[i][type] = rankingTime[i - 1][type];
				rankingRollclear[i][type] = rankingRollclear[i - 1][type];
			}

			// 新しいデータを登録
			rankingGrade[rankingRank][type] = gr;
			rankingLevel[rankingRank][type] = lv;
			rankingTime[rankingRank][type] = time;
			rankingRollclear[rankingRank][type] = clear;
		}
	}

	/**
	 * ランキングの順位を取得
	 * @param gr 段位
	 * @param lv レベル
	 * @param time タイム
	 * @return 順位(ランク外なら-1)
	 */
	private int checkRanking(int gr, int lv, int time, int clear, int type) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(gr > rankingGrade[i][type]) {
				return i;
			} else if((gr == rankingGrade[i][type]) && (clear > rankingRollclear[i][type])) {
				return i;
			} else if((gr == rankingGrade[i][type]) && (clear == rankingRollclear[i][type]) && (lv > rankingLevel[i][type])) {
				return i;
			} else if((gr == rankingGrade[i][type]) && (clear == rankingRollclear[i][type]) && (lv == rankingLevel[i][type]) &&
					  (time < rankingTime[i][type]))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * 段位履歴を更新
	 * @param gr 段位
	 */
	private void updateGradeHistory(int gr) {
		for(int i = GRADE_HISTORY_SIZE - 1; i > 0; i--) {
			gradeHistory[i] = gradeHistory[i - 1];
		}
		gradeHistory[0] = gr;

		// Debug log
		log.debug("** Exam grade history START **");
		for(int i = 0; i < gradeHistory.length; i++) {
			log.debug(i + ": " + getGradeName(gradeHistory[i]) + " (" + gradeHistory[i] + ")");
		}
		log.debug("*** Exam grade history END ***");
	}

	/**
	 * 昇格試験の目標段位を設定
	 * @author Zircean
	 */
	private void setPromotionalGrade() {
		int gradesOver;

		for(int i = tableGradeName.length - 1; i >= 0; i--) {
			gradesOver = 0;
			for(int j = 0; j < GRADE_HISTORY_SIZE; j++) {
				if(gradeHistory[j] == -1) {
					promotionalExam = 0;
					return;
				} else {
					if(gradeHistory[j] >= i) {
						gradesOver++;
					}
				}
			}
			if(gradesOver > 3) {
				promotionalExam = i;
				if(qualifiedGrade < 31 && promotionalExam == 32) {
					promotionalExam = 31;
				}
				return;
			}
		}
	}

	/**
	 * ベストセクションタイム更新
	 */
	private void updateBestSectionTime() {
		for(int i = 0; i < SECTION_MAX; i++) {
			if(sectionIsNewRecord[i]) {
				int type = enableexam ? 1 : 0;
				bestSectionTime[i][type] = sectiontime[i];
			}
		}
	}
}
