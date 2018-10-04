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
package org.game_host.hebo.nullpomino.gui.net;

import java.io.IOException;

import org.game_host.hebo.nullpomino.game.net.NetPlayerClient;
import org.game_host.hebo.nullpomino.game.net.NetRoomInfo;

/**
 * ロビー画面のイベント処理をするインターフェース
 */
public interface NetLobbyListener {
	/**
	 * 初期化完了
	 * @param lobby NetLobbyFrame
	 */
	public void netlobbyOnInit(NetLobbyFrame lobby);

	/**
	 * ログイン完了
	 * @param lobby NetLobbyFrame
	 * @param client NetClient
	 */
	public void netlobbyOnLoginOK(NetLobbyFrame lobby, NetPlayerClient client);

	/**
	 * ルームに入ったとき
	 * @param lobby NetLobbyFrame
	 * @param client NetClient
	 * @param roomInfo ルーム情報
	 */
	public void netlobbyOnRoomJoin(NetLobbyFrame lobby, NetPlayerClient client, NetRoomInfo roomInfo);

	/**
	 * ルームを出てロビーに戻ったとき
	 * @param lobby NetLobbyFrame
	 * @param client NetClient
	 */
	public void netlobbyOnRoomLeave(NetLobbyFrame lobby, NetPlayerClient client);

	/**
	 * 切断されたとき
	 * @param lobby NetLobbyFrame
	 * @param client NetClient
	 * @param ex 切断原因となった例外(不明な場合と正常終了の場合はnull)
	 */
	public void netlobbyOnDisconnect(NetLobbyFrame lobby, NetPlayerClient client, Throwable ex);

	/**
	 * メッセージ受信
	 * @param lobby NetLobbyFrame
	 * @param client NetClient
	 * @param message 受信したメッセージ(タブ区切り済み)
	 * @throws IOException 何かエラーがあったとき
	 */
	public void netlobbyOnMessage(NetLobbyFrame lobby, NetPlayerClient client, String[] message) throws IOException;

	/**
	 * ロビー画面を閉じたとき
	 * @param lobby NetLobbyFrame
	 */
	public void netlobbyOnExit(NetLobbyFrame lobby);
}
