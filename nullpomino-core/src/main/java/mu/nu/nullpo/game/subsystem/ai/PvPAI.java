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
package mu.nu.nullpo.game.subsystem.ai;

import org.apache.log4j.Logger;

import jep.Jep;
import jep.JepException;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * PvPAI - Base class for AI players
 */
public class PvPAI extends DummyAI implements AIPlayer {
	/** Log */
	static Logger log = Logger.getLogger(PvPAI.class);
	
	/** ホールド使用予定 */
	public boolean bestHold;

	/** Plan to putX-coordinate */
	public int bestX;

	/** Plan to putY-coordinate */
	public int bestY;

	/** Plan to putDirection */
	public int bestRt;

	/** Hold Force */
	public boolean forceHold;

	/** Current piece number */
	public int thinkCurrentPieceNo;

	/** Peace of thoughts is finished number */
	public int thinkLastPieceNo;

	/** Did the thinking thread finish successfully? */
	public boolean thinkComplete;
	
	private Jep jep;

	public String getName() {
		return "PvPAI";
	}

	public void init(GameEngine engine, int playerID) {
		try {
			jep = new Jep();
		    jep.runScript("pvpai.py");
		    jep.eval("ai = PvPAI('pvpai')");
		    Object v = jep.getValue("ai.name");
			log.debug("AI name is "+v.toString());
		} catch (JepException j) {
			log.error("JEP cannot be init!", j);
		}
	}

	public void newPiece(GameEngine engine, int playerID) {
		try {
			jep.set("engine", engine);
			jep.set("playerID", playerID);
			jep.eval("ai.newPiece(engine, playerID)");
		} catch (JepException j) {
			log.error("Error in newPiece, ", j);
		}
	}

	public void onFirst(GameEngine engine, int playerID) {
		try {
			jep.set("engine", engine);
			jep.set("playerID", playerID);
			jep.eval("ai.onFirst(engine, playerID)");
		} catch (JepException j) {
			log.error("Error in onFirst, ", j);
		}
	}

	public void onLast(GameEngine engine, int playerID) {
		try {
			jep.set("engine", engine);
			jep.set("playerID", playerID);
			jep.eval("ai.onLast(engine, playerID)");
		} catch (JepException j) {
			log.error("Error in onLast, ", j);
		}
	}

	public void renderState(GameEngine engine, int playerID) {
		try {
			jep.set("engine", engine);
			jep.set("playerID", playerID);
			jep.eval("ai.renderState(engine, playerID)");
		} catch (JepException j) {
			log.error("Error in renderState, ", j);
		}
	}

	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		try {
			jep.set("engine", engine);
			jep.set("playerID", playerID);
			jep.eval("ai.setControl(engine, playerID, ctrl)");
		} catch (JepException j) {
			log.error("Error in setControl, ", j);
		}
	}

	public void shutdown(GameEngine engine, int playerID) {
		try {
			if (jep != null) {
				jep.eval("ai.shutdown()");
				jep.close();
			}
		} catch (JepException j) {
			log.error("Error in shutdown, ", j);
		}
	}

	public void renderHint(GameEngine engine, int playerID) {
		try {
			jep.set("engine", engine);
			jep.set("playerID", playerID);
			jep.eval("ai.renderHint(engine, playerID)");
		} catch (JepException j) {
			log.error("Error in renderHint, ", j);
		}
	}
}
