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

import org.game_host.hebo.nullpomino.game.component.Block;
import org.game_host.hebo.nullpomino.game.play.GameEngine;
import org.game_host.hebo.nullpomino.game.play.GameManager;
import org.game_host.hebo.nullpomino.util.CustomProperties;

/**
 * ゲームモードのインターフェイス
 */
public interface GameMode {
	/**
	 * モード名を取得
	 * @return モード名
	 */
	public String getName();

	/**
	 * このモードのプレイヤーの人数を取得
	 * @return プレイヤーの人数
	 */
	public int getPlayers();

	/**
	 * ゲーム画面表示直前に呼び出される処理
	 * @param manager このモードを所有するGameManager
	 */
	public void modeInit(GameManager manager);

	/**
	 * 各プレイヤーの初期化が終わるときに呼び出される処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void playerInit(GameEngine engine, int playerID);

	/**
	 * Ready→Go直後、最初のピースが現れる直前の処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void startGame(GameEngine engine, int playerID);

	/**
	 * 各プレイヤーの最初の処理の時に呼び出される
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void onFirst(GameEngine engine, int playerID);

	/**
	 * 各プレイヤーの最後の処理の時に呼び出される
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void onLast(GameEngine engine, int playerID);

	/**
	 * 開始前の設定画面のときの処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと自動的にReady画面に移動しない
	 */
	public boolean onSetting(GameEngine engine, int playerID);

	/**
	 * Ready→Goのときの処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onReady(GameEngine engine, int playerID);

	/**
	 * ブロックピースの移動処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onMove(GameEngine engine, int playerID);

	/**
	 * ブロック固定直後の光っているときの処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onLockFlash(GameEngine engine, int playerID);

	/**
	 * ライン消去処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onLineClear(GameEngine engine, int playerID);

	/**
	 * ARE中の処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onARE(GameEngine engine, int playerID);

	/**
	 * エンディング突入時の処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onEndingStart(GameEngine engine, int playerID);

	/**
	 * 各ゲームモードが自由に使えるステータスの処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueでもfalseでも意味は変わりません
	 */
	public boolean onCustom(GameEngine engine, int playerID);

	/**
	 * エンディング画面の処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onExcellent(GameEngine engine, int playerID);

	/**
	 * ゲームオーバー画面の処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onGameOver(GameEngine engine, int playerID);

	/**
	 * 結果画面の処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onResult(GameEngine engine, int playerID);

	/**
	 * フィールドエディット画面の処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean onFieldEdit(GameEngine engine, int playerID);

	/**
	 * 各プレイヤーの最初の描画処理の時に呼び出される
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderFirst(GameEngine engine, int playerID);

	/**
	 * 各プレイヤーの最後の描画処理の時に呼び出される
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderLast(GameEngine engine, int playerID);

	/**
	 * 開始前の設定画面のときの描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderSetting(GameEngine engine, int playerID);

	/**
	 * Ready→Goのときの描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderReady(GameEngine engine, int playerID);

	/**
	 * ブロックピースの移動描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderMove(GameEngine engine, int playerID);

	/**
	 * ブロック固定直後の光っているときの描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderLockFlash(GameEngine engine, int playerID);

	/**
	 * ライン消去描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderLineClear(GameEngine engine, int playerID);

	/**
	 * ARE中の描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderARE(GameEngine engine, int playerID);

	/**
	 * エンディング突入時の描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderEndingStart(GameEngine engine, int playerID);

	/**
	 * 各ゲームモードが自由に使えるステータスの描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderCustom(GameEngine engine, int playerID);

	/**
	 * エンディング画面の描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderExcellent(GameEngine engine, int playerID);

	/**
	 * ゲームオーバー画面の描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderGameOver(GameEngine engine, int playerID);

	/**
	 * 結果画面の描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderResult(GameEngine engine, int playerID);

	/**
	 * フィールドエディット画面の描画処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 */
	public void renderFieldEdit(GameEngine engine, int playerID);

	/**
	 * ブロックを消す演出を出すときの処理
	 * @param engine GameEngineのインスタンス
	 * @param playerID プレイヤーID
	 * @param x X座標
	 * @param y Y座標
	 * @param blk ブロック
	 */
	public void blockBreak(GameEngine engine, int playerID, int x, int y, Block blk);

	/**
	 * スコア計算(pieceLockedの前)
	 * @param engine GameEngineのインスタンス
	 * @param playerID プレイヤーID
	 * @param lines 消えるライン数（消えなかった場合は0）
	 */
	public void calcScore(GameEngine engine, int playerID, int lines);

	/**
	 * ソフトドロップ使用後の処理
	 * @param engine GameEngineのインスタンス
	 * @param playerID プレイヤーID
	 * @param fall 今落下した段数
	 */
	public void afterSoftDropFall(GameEngine engine, int playerID, int fall);

	/**
	 * ハードドロップ使用後の処理
	 * @param engine GameEngineのインスタンス
	 * @param playerID プレイヤーID
	 * @param fall 今落下した段数
	 */
	public void afterHardDropFall(GameEngine engine, int playerID, int fall);

	/**
	 * フィールドエディット画面から出たときの処理
	 * @param engine GameEngineのインスタンス
	 * @param playerID プレイヤーID
	 */
	public void fieldEditExit(GameEngine engine, int playerID);

	/**
	 * ブロックピースが固定されたときの処理(calcScoreの直後)
	 * @param engine GameEngineのインスタンス
	 * @param playerID プレイヤーID
	 * @param lines 消えるライン数（消えなかった場合は0）
	 */
	public void pieceLocked(GameEngine engine, int playerID, int lines);

	/**
	 * ライン消去が終わるときに呼び出される処理
	 * @param engine GameEngine
	 * @param playerID プレイヤーID
	 * @return trueを返すと通常の処理を行わない
	 */
	public boolean lineClearEnd(GameEngine engine, int playerID);

	/**
	 * リプレイ保存時の処理
	 * @param engine GameEngineのインスタンス
	 * @param playerID プレイヤーID
	 * @param prop リプレイ保存先のプロパティセット
	 */
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop);

	/**
	 * リプレイ読み込み時の処理
	 * @param engine GameEngineのインスタンス
	 * @param playerID プレイヤーID
	 * @param prop リプレイ読み込み元のプロパティセット
	 */
	public void loadReplay(GameEngine engine, int playerID, CustomProperties prop);

	/**
	 * ネットプレイ用モードかどうかを取得
	 * @return ネットプレイ用モードならtrue
	 */
	public boolean isNetplayMode();

	/**
	 * ネットプレイ準備
	 * @param obj 任意のオブジェクト(今のところNetLobbyFrame)
	 */
	public void netplayInit(Object obj);
}
