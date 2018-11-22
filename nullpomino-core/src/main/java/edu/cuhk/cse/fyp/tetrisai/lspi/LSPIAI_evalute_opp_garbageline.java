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
package edu.cuhk.cse.fyp.tetrisai.lspi;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.mode.VSBattleMode;

import org.apache.log4j.Logger;

/**
 * CommonAI
 */
public class LSPIAI extends DummyAI implements Runnable {
	/** Log */
	static Logger log = Logger.getLogger(LSPIAI.class);

	/** ホールド使用予定 */
	public boolean bestOppHold;

	/** Plan to putX-coordinate */
	public int bestOppX;

	/** Plan to putY-coordinate */
	public int bestOppY;

	/** Plan to putDirection */
	public int bestOppRt;

	/** Hold Force */
	public boolean OppforceHold;
	
	/** After that I was groundedX-coordinate */
	public int bestXSub;

	/** After that I was groundedY-coordinate */
	public int bestYSub;

	/** After that I was groundedDirection(-1: None) */
	public int bestRtSub;

	/** The best moveEvaluation score */
	public double bestPts;

	/** After that I was groundedX-coordinate */
	public int bestOppXSub;

	/** After that I was groundedY-coordinate */
	public int bestOppYSub;

	/** After that I was groundedDirection(-1: None) */
	public int bestOppRtSub;

	/** The best moveEvaluation score */
	public double bestOppPts;

	/** Delay the move for changecount */
	public int delay;

	/** The GameEngine that owns this AI */
	public GameEngine gEngine;

	/** The GameManager that owns this AI */
	public GameManager gManager;

	/** When true,To threadThink routineInstructing the execution of the */
	public boolean thinkRequest;

	/** true when thread is executing the think routine. */
	public boolean thinking;

	/** To stop a thread time */
	public int thinkDelay;

	/** When true,Running thread */
	public volatile boolean threadRunning;

	/** Thread for executing the think routine */
	public Thread thread;

	
	/**                     
	 * NEW THINGS IN ESTR LSPI AI
	 * 
	 * 
	 */
	
	public static final boolean TRAIN = false;
	public static final boolean DEBUG = false;
	
	
	/** Map LSPI AI piece to nullpomino piece */
	public static final int[] lspi2nullpomino = { 2, 0, 1, 5, 4, 6, 3 };

	/** Map nullpomino piece to LSPI AI piece */
	public static final int[] nullpomino2lspi = { 1, 2, 0, 6, 4, 3, 5 };

	/** Map LSPI AI rotate to nullpomino rotate according piece */
	public static final int[][] lspi2rotate = { { 0 }, 
										 { 3, 0 },
										 { 1, 2, 3, 0 },
										 { 3, 0, 1, 2 },
										 { 1, 2, 3, 0 },
										 { 0, 1 },
										 { 0, 1 } 
									   };

	/** Map LSPI AI X to nullpomino X according piece */
	public static final int[][] lspi2X = { { 0 }, 
									{ -1, 0 }, 
									{ -1, 0, 0, 0 }, 
									{ 0, 0, -1, 0 },
									{ -1, 0, 0, 0 }, 
									{ 0, -1 },
									{ 0, -1 }
								  };

	public PlayerSkeleton player;

	public State nowState;
	
	public State oppNowState;
	
	public FutureState futureState;
	
	public FutureState oppFutureState;

	public State nowState_opp;
	
	public State oppNowState_opp;
	
	public FutureState futureState_opp;
	
	public FutureState oppFutureState_opp;
	
	public int move;
	
	public int nowid;
	
	public int height;

	public int hiddenHeight;
	
	public int width;
	
	public boolean twoPlayerGame = false;

	public double[] lastf;
	
	public double[] savedLastf;
	
	/*
	 * AIOfName
	 */
	@Override
	public String getName() {
		return "LSPI";
	}

	/*
	 * Called at initialization
	 */
	@Override
	public void init(GameEngine engine, int playerID) {
		player = new PlayerSkeleton();
		player.learns = LSPIAI.TRAIN;
		nowState = new State();
		oppNowState = new State();
		move = 0;
		nowid = 0;
		

		int no_players = engine.owner.getPlayers();
		twoPlayerGame = false;
		if (no_players == 2)
			twoPlayerGame = true;
		
		gEngine = engine;
		gManager = engine.owner;
		thinkRequest = false;
		thinking = false;
		threadRunning = false;

		if (((thread == null) || !thread.isAlive()) && (engine.aiUseThread)) {
			thread = new Thread(this, "AI_" + playerID);
			thread.setDaemon(true);
			thread.start();
			thinkDelay = engine.aiThinkDelay;
			thinkCurrentPieceNo = 0;
			thinkLastPieceNo = 0;
		}
	}

	/*
	 * End processing
	 */
	@Override
	public void shutdown(GameEngine engine, int playerID) {
		if ((thread != null) && (thread.isAlive())) {
			thread.interrupt();
			threadRunning = false;
			thread = null;
		}
	}

	/*
	 * Called whenever a new piece is spawned
	 */
	@Override
	public void newPiece(GameEngine engine, int playerID) {
		if (!engine.aiUseThread) {
			thinkBestPosition(engine, playerID);
		} else {
			thinkRequest = true;
			thinkCurrentPieceNo++;
		}
	}

	/*
	 * Called at the start of each frame
	 */
	@Override
	public void onFirst(GameEngine engine, int playerID) {
	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
	}

	/*
	 * Set button input states
	 */
	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		if ((engine.nowPieceObject != null) && (engine.stat == GameEngine.Status.MOVE) && (delay >= engine.aiMoveDelay)
				&& (engine.statc[0] > 0)
				&& (!engine.aiUseThread || (threadRunning && !thinking && (thinkCurrentPieceNo <= thinkLastPieceNo)))) {
			int input = 0; // button input data
			Piece pieceNow = engine.nowPieceObject;
			int nowX = engine.nowPieceX;
			int nowY = engine.nowPieceY;
			int rt = pieceNow.direction;
			Field fld = engine.field;
			boolean pieceTouchGround = pieceNow.checkCollision(nowX, nowY + 1, fld);

			if ((bestHold || forceHold) && engine.isHoldOK()) {
				// Hold
				input |= Controller.BUTTON_BIT_D;
			} else {
				// rotation
				if (rt != bestRt) {
					int lrot = engine.getRotateDirection(-1);
					int rrot = engine.getRotateDirection(1);

					if ((Math.abs(rt - bestRt) == 2) && (engine.ruleopt.rotateButtonAllowDouble)
							&& !ctrl.isPress(Controller.BUTTON_E)) {
						input |= Controller.BUTTON_BIT_E;
					} else if (!ctrl.isPress(Controller.BUTTON_B) && engine.ruleopt.rotateButtonAllowReverse
							&& !engine.isRotateButtonDefaultRight() && (bestRt == rrot)) {
						input |= Controller.BUTTON_BIT_B;
					} else if (!ctrl.isPress(Controller.BUTTON_B) && engine.ruleopt.rotateButtonAllowReverse
							&& engine.isRotateButtonDefaultRight() && (bestRt == lrot)) {
						input |= Controller.BUTTON_BIT_B;
					} else if (!ctrl.isPress(Controller.BUTTON_A)) {
						input |= Controller.BUTTON_BIT_A;
					}
				}

				// Whether reachable position
				int minX = pieceNow.getMostMovableLeft(nowX, nowY, rt, fld);
				int maxX = pieceNow.getMostMovableRight(nowX, nowY, rt, fld);

				if (((bestX < minX - 1) || (bestX > maxX + 1) || (bestY < nowY)) && (rt == bestRt)) {
					// Again because it is thought unreachable
					// thinkBestPosition(engine, playerID);
					thinkRequest = true;
					// thinkCurrentPieceNo++;
					// System.out.println("rethink c:" + thinkCurrentPieceNo + " l:" +
					// thinkLastPieceNo);
				} else {
					// If you are able to reach
					if ((nowX == bestX) && (pieceTouchGround) && (rt == bestRt)) {
						// Groundrotation
						if (bestRtSub != -1) {
							bestRt = bestRtSub;
							bestRtSub = -1;
						}
						// Shift move
						if (bestX != bestXSub) {
							bestX = bestXSub;
							bestY = bestYSub;
						}
					}

					if (nowX > bestX) {
						// Left
						if (!ctrl.isPress(Controller.BUTTON_LEFT) || (engine.aiMoveDelay >= 0))
							input |= Controller.BUTTON_BIT_LEFT;
					} else if (nowX < bestX) {
						// Right
						if (!ctrl.isPress(Controller.BUTTON_RIGHT) || (engine.aiMoveDelay >= 0))
							input |= Controller.BUTTON_BIT_RIGHT;
					} else if ((nowX == bestX) && (rt == bestRt)) {
						// Funnel
						if ((bestRtSub == -1) && (bestX == bestXSub)) {
							if (engine.ruleopt.harddropEnable && !ctrl.isPress(Controller.BUTTON_UP))
								input |= Controller.BUTTON_BIT_UP;
							else if (engine.ruleopt.softdropEnable || engine.ruleopt.softdropLock)
								input |= Controller.BUTTON_BIT_DOWN;
						} else {
							if (engine.ruleopt.harddropEnable && !engine.ruleopt.harddropLock
									&& !ctrl.isPress(Controller.BUTTON_UP))
								input |= Controller.BUTTON_BIT_UP;
							else if (engine.ruleopt.softdropEnable && !engine.ruleopt.softdropLock)
								input |= Controller.BUTTON_BIT_DOWN;
						}
					}
				}
			}

			delay = 0;
			ctrl.setButtonBit(input);
		} else {
			delay++;
			ctrl.setButtonBit(0);
		}
	}

	/**
	 * Search for the best choice
	 * 
	 * @param engine   The GameEngine that owns this AI
	 * @param playerID Player ID
	 */
	public void thinkBestPosition(GameEngine engine, int playerID) {
		bestHold = false;
		bestX = 0;
		bestY = 0;
		bestRt = 0;
		bestXSub = 0;
		bestYSub = 0;
		bestRtSub = -1;
		bestPts = 0;
		forceHold = false;
		height = engine.fieldHeight;
		hiddenHeight = engine.fieldHiddenHeight;
		width = engine.fieldWidth;
		
		/*
		Field fld = engine.field;
        int[][] transformed_fld = new int[height][width];
        for (int c = 0; c < width; c++) {
        	for (int r = 0; r < height; r++) {
        		transformed_fld[r][c] = fld.getBlockEmpty(c, r) ? 0 : 1;
        	}
        }
        int heightest;
        int rr;
        int tmp;
        for (int c = 0; c < State.COLS; c++) {
			heightest = 0;
	        for (int r = 0; r < State.ROWS; r++) {
				try {
					tmp = 0;
					try {
						rr = (State.ROWS - r + 1);
						if (rr > 0)
							tmp = transformed_fld[(State.ROWS - r + 1)][c];
					} catch(Exception e) {
						tmp = 0;
					}
					state.getField()[r][c] = tmp;
					if (tmp == 1)
						heightest = Math.max(r + 1, heightest);
				} catch(Exception e) {
					System.out.println(e);
				}
	        }
			state.top[c] = heightest;
        }

        nowid = engine.nowPieceObject.id;
        state.nextPiece = nullpomino2lspi[nowid];
        if (DEBUG) {
            log.info("NowId:" + nowid);
            log.info("Piece:" + state.nextPiece);
        }
        int[][] tmpLegalMoves = state.legalMoves();
        move = player.pickMove(state,tmpLegalMoves);
        
        int[] tempNum = tmpLegalMoves[move];
        int orient = tempNum[0];
        int offset = tempNum[1];
        bestX = offset + lspi2X[state.nextPiece][orient];
        bestXSub = bestX;
        bestRt = lspi2rotate[state.nextPiece][orient];
        if (DEBUG) {
        	log.info("Orient LSPI:" + orient);
        	log.info("Offset LSPI:" + offset);
        	log.info("Orient NULL:" + bestRt);
        	log.info("Offset NULL:" + bestX);
        }
            
        // simulate inside AI
        state.makeMove(move);
        if (TRAIN) {
            //player.getBasisFunctions().computeWeights();
            //log.info(self.player.bs.weight);
        }
        if (DEBUG) {
            // print field
            player.printField(state.getField());
        }
        
        */
		Piece pieceNow = engine.nowPieceObject;
		int nowX = engine.nowPieceX;
		int nowY = engine.nowPieceY;
		boolean holdOK = engine.isHoldOK();
		boolean holdEmpty = false;
		Piece pieceHold = engine.holdPieceObject;
		Piece pieceNext = engine.getNextObject(engine.nextPieceCount);
		Field fld = new Field(engine.field);
		if (pieceHold == null) {
			holdEmpty = true;
		}
		
		// Convert to now State
		// My now state
		nowState = createState(fld, engine);

		if (twoPlayerGame) {
			GameEngine oppEngine;
			Field oppFld;
			oppEngine = engine.owner.engine[1 - engine.playerID];
			oppFld = new Field(oppEngine.field);
			// Opp now state
			oppNowState = createState(oppFld, oppEngine);
		}
        

		for (int depth = 0; depth < getMaxThinkDepth(); depth++) {
			for (int rt = 0; rt < Piece.DIRECTION_COUNT; rt++) {
				// Peace for now
				int minX = pieceNow.getMostMovableLeft(nowX, nowY, rt, engine.field);
				int maxX = pieceNow.getMostMovableRight(nowX, nowY, rt, engine.field);

				for (int x = minX; x <= maxX; x++) {
					fld.copy(engine.field);
					int y = pieceNow.getBottom(x, nowY, rt, fld);

					if (!pieceNow.checkCollision(x, y, rt, fld)) {
						// As it is
						double pts = thinkMain(engine, x, y, rt, -1, fld, pieceNow, pieceNext, pieceHold, depth);

						if (pts >= bestPts) {
							bestHold = false;
							bestX = x;
							bestY = y;
							bestRt = rt;
							bestXSub = x;
							bestYSub = y;
							bestRtSub = -1;
							bestPts = pts;
							savedLastf = lastf;
						}

						if ((depth > 0) || (pieceNow.id == Piece.PIECE_T)) {
						//if ((depth > 0) || (bestPts <= 10) || (pieceNow.id == Piece.PIECE_T)) {
							// Left shift
							fld.copy(engine.field);
							if (!pieceNow.checkCollision(x - 1, y, rt, fld)
									&& pieceNow.checkCollision(x - 1, y - 1, rt, fld)) {
								pts = thinkMain(engine, x - 1, y, rt, -1, fld, pieceNow, pieceNext, pieceHold, depth);

								if (pts > bestPts) {
									bestHold = false;
									bestX = x;
									bestY = y;
									bestRt = rt;
									bestXSub = x - 1;
									bestYSub = y;
									bestRtSub = -1;
									bestPts = pts;
									savedLastf = lastf;
								}
							}

							// Right shift
							fld.copy(engine.field);
							if (!pieceNow.checkCollision(x + 1, y, rt, fld)
									&& pieceNow.checkCollision(x + 1, y - 1, rt, fld)) {
								pts = thinkMain(engine, x + 1, y, rt, -1, fld, pieceNow, pieceNext, pieceHold, depth);

								if (pts > bestPts) {
									bestHold = false;
									bestX = x;
									bestY = y;
									bestRt = rt;
									bestXSub = x + 1;
									bestYSub = y;
									bestRtSub = -1;
									bestPts = pts;
									savedLastf = lastf;
								}
							}

							// Leftrotation
							if (!engine.isRotateButtonDefaultRight() || engine.ruleopt.rotateButtonAllowReverse) {
								int rot = pieceNow.getRotateDirection(-1, rt);
								int newX = x;
								int newY = y;
								fld.copy(engine.field);
								pts = 0;

								if (!pieceNow.checkCollision(x, y, rot, fld)) {
									pts = thinkMain(engine, x, y, rot, rt, fld, pieceNow, pieceNext, pieceHold, depth);
								} else if ((engine.wallkick != null) && (engine.ruleopt.rotateWallkick)) {
									boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0)
											|| (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
									WallkickResult kick = engine.wallkick.executeWallkick(x, y, -1, rt, rot,
											allowUpward, pieceNow, fld, null);

									if (kick != null) {
										newX = x + kick.offsetX;
										newY = y + kick.offsetY;
										pts = thinkMain(engine, newX, newY, rot, rt, fld, pieceNow, pieceNext,
												pieceHold, depth);
									}
								}

								if (pts > bestPts) {
									bestHold = false;
									bestX = x;
									bestY = y;
									bestRt = rt;
									bestXSub = newX;
									bestYSub = newY;
									bestRtSub = rot;
									bestPts = pts;
									savedLastf = lastf;
								}
							}

							// Rightrotation
							if (engine.isRotateButtonDefaultRight() || engine.ruleopt.rotateButtonAllowReverse) {
								int rot = pieceNow.getRotateDirection(1, rt);
								int newX = x;
								int newY = y;
								fld.copy(engine.field);
								pts = 0;

								if (!pieceNow.checkCollision(x, y, rot, fld)) {
									pts = thinkMain(engine, x, y, rot, rt, fld, pieceNow, pieceNext, pieceHold, depth);
								} else if ((engine.wallkick != null) && (engine.ruleopt.rotateWallkick)) {
									boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0)
											|| (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
									WallkickResult kick = engine.wallkick.executeWallkick(x, y, 1, rt, rot, allowUpward,
											pieceNow, fld, null);

									if (kick != null) {
										newX = x + kick.offsetX;
										newY = y + kick.offsetY;
										pts = thinkMain(engine, newX, newY, rot, rt, fld, pieceNow, pieceNext,
												pieceHold, depth);
									}
								}

								if (pts > bestPts) {
									bestHold = false;
									bestX = x;
									bestY = y;
									bestRt = rt;
									bestXSub = newX;
									bestYSub = newY;
									bestRtSub = rot;
									bestPts = pts;
									savedLastf = lastf;
								}
							}

							// 180-degree rotation
							if (engine.ruleopt.rotateButtonAllowDouble) {
								int rot = pieceNow.getRotateDirection(2, rt);
								int newX = x;
								int newY = y;
								fld.copy(engine.field);
								pts = 0;

								if (!pieceNow.checkCollision(x, y, rot, fld)) {
									pts = thinkMain(engine, x, y, rot, rt, fld, pieceNow, pieceNext, pieceHold, depth);
								} else if ((engine.wallkick != null) && (engine.ruleopt.rotateWallkick)) {
									boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0)
											|| (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
									WallkickResult kick = engine.wallkick.executeWallkick(x, y, 2, rt, rot, allowUpward,
											pieceNow, fld, null);

									if (kick != null) {
										newX = x + kick.offsetX;
										newY = y + kick.offsetY;
										pts = thinkMain(engine, newX, newY, rot, rt, fld, pieceNow, pieceNext,
												pieceHold, depth);
									}
								}

								if (pts > bestPts) {
									bestHold = false;
									bestX = x;
									bestY = y;
									bestRt = rt;
									bestXSub = newX;
									bestYSub = newY;
									bestRtSub = rot;
									bestPts = pts;
									savedLastf = lastf;
								}
							}
						}
					}
				}

				if (pieceHold == null) {
					pieceHold = engine.getNextObject(engine.nextPieceCount);
				}
				// Hold Peace
				if ((holdOK == true) && (pieceHold != null) && (depth == 0)) {
					int spawnX = engine.getSpawnPosX(engine.field, pieceHold);
					int spawnY = engine.getSpawnPosY(pieceHold);
					int minHoldX = pieceHold.getMostMovableLeft(spawnX, spawnY, rt, engine.field);
					int maxHoldX = pieceHold.getMostMovableRight(spawnX, spawnY, rt, engine.field);

					for (int x = minHoldX; x <= maxHoldX; x++) {
						fld.copy(engine.field);
						int y = pieceHold.getBottom(x, spawnY, rt, fld);

						if (!pieceHold.checkCollision(x, y, rt, fld)) {
							Piece pieceNext2 = engine.getNextObject(engine.nextPieceCount);
							if (holdEmpty)
								pieceNext2 = engine.getNextObject(engine.nextPieceCount + 1);

							double pts = thinkMain(engine, x, y, rt, -1, fld, pieceHold, pieceNext2, null, depth);

							if (pts > bestPts) {
								bestHold = true;
								bestX = x;
								bestY = y;
								bestRt = rt;
								bestRtSub = -1;
								bestPts = pts;
								savedLastf = lastf;
							}
						}
					}
				}
			}

			if (bestPts > 0)
				break;
		}
		thinkLastPieceNo++;

		System.out.println("X:" + bestX + " Y:" + bestY + " R:" + bestRt + " H:" +
		bestHold + " Pts:" + bestPts);
//		for (int i = 0; i < TwoPlayerBasisFunction.FEATURE_COUNT; i++)
//			System.out.println(lastf[i]);
		System.out.println(savedLastf[3]);
	}
	
	/**
	 * Search for the bestOpp choice
	 * 
	 * @param engine   The GameEngine that owns this AI
	 * @param playerID Player ID
	 */
	public void thinkbestOppPosition(GameEngine engine, int playerID) {
		bestOppHold = false;
		bestOppX = 0;
		bestOppY = 0;
		bestOppRt = 0;
		bestOppXSub = 0;
		bestOppYSub = 0;
		bestOppRtSub = -1;
		bestOppPts = 0;
		OppforceHold = false;
		height = engine.fieldHeight;
		hiddenHeight = engine.fieldHiddenHeight;
		width = engine.fieldWidth;
		
		Piece pieceNow = engine.nowPieceObject;
		int nowX = engine.nowPieceX;
		int nowY = engine.nowPieceY;
		boolean holdOK = engine.isHoldOK();
		boolean holdEmpty = false;
		Piece pieceHold = engine.holdPieceObject;
		Piece pieceNext = engine.getNextObject(engine.nextPieceCount);
		Field fld = new Field(engine.field);
		if (pieceHold == null) {
			holdEmpty = true;
		}
		
		// Convert to now State
		// My now state
		nowState_opp = createState(fld, engine);

		if (twoPlayerGame) {
			GameEngine oppEngine;
			Field oppFld;
			oppEngine = engine.owner.engine[1 - engine.playerID];
			oppFld = new Field(oppEngine.field);
			// Opp now state
			oppNowState_opp = createState(oppFld, oppEngine);
		}
        

		for (int depth = 0; depth < getMaxThinkDepth(); depth++) {
			for (int rt = 0; rt < Piece.DIRECTION_COUNT; rt++) {
				// Peace for now
				int minX = pieceNow.getMostMovableLeft(nowX, nowY, rt, engine.field);
				int maxX = pieceNow.getMostMovableRight(nowX, nowY, rt, engine.field);

				for (int x = minX; x <= maxX; x++) {
					fld.copy(engine.field);
					int y = pieceNow.getBottom(x, nowY, rt, fld);

					if (!pieceNow.checkCollision(x, y, rt, fld)) {
						// As it is
						double pts = thinkMainByLineStack(engine, x, y, rt, -1, fld, pieceNow, pieceNext, pieceHold, depth);

						if (pts >= bestOppPts) {
							bestOppHold = false;
							bestOppX = x;
							bestOppY = y;
							bestOppRt = rt;
							bestOppXSub = x;
							bestOppYSub = y;
							bestOppRtSub = -1;
							bestOppPts = pts;
							savedLastf = lastf;
						}

						if ((depth > 0) || (pieceNow.id == Piece.PIECE_T)) {
						//if ((depth > 0) || (bestOppPts <= 10) || (pieceNow.id == Piece.PIECE_T)) {
							// Left shift
							fld.copy(engine.field);
							if (!pieceNow.checkCollision(x - 1, y, rt, fld)
									&& pieceNow.checkCollision(x - 1, y - 1, rt, fld)) {
								pts = thinkMainByLineStack(engine, x - 1, y, rt, -1, fld, pieceNow, pieceNext, pieceHold, depth);

								if (pts > bestOppPts) {
									bestOppHold = false;
									bestOppX = x;
									bestOppY = y;
									bestOppRt = rt;
									bestOppXSub = x - 1;
									bestOppYSub = y;
									bestOppRtSub = -1;
									bestOppPts = pts;
									savedLastf = lastf;
								}
							}

							// Right shift
							fld.copy(engine.field);
							if (!pieceNow.checkCollision(x + 1, y, rt, fld)
									&& pieceNow.checkCollision(x + 1, y - 1, rt, fld)) {
								pts = thinkMainByLineStack(engine, x + 1, y, rt, -1, fld, pieceNow, pieceNext, pieceHold, depth);

								if (pts > bestOppPts) {
									bestOppHold = false;
									bestOppX = x;
									bestOppY = y;
									bestOppRt = rt;
									bestOppXSub = x + 1;
									bestOppYSub = y;
									bestOppRtSub = -1;
									bestOppPts = pts;
									savedLastf = lastf;
								}
							}

							// Leftrotation
							if (!engine.isRotateButtonDefaultRight() || engine.ruleopt.rotateButtonAllowReverse) {
								int rot = pieceNow.getRotateDirection(-1, rt);
								int newX = x;
								int newY = y;
								fld.copy(engine.field);
								pts = 0;

								if (!pieceNow.checkCollision(x, y, rot, fld)) {
									pts = thinkMainByLineStack(engine, x, y, rot, rt, fld, pieceNow, pieceNext, pieceHold, depth);
								} else if ((engine.wallkick != null) && (engine.ruleopt.rotateWallkick)) {
									boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0)
											|| (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
									WallkickResult kick = engine.wallkick.executeWallkick(x, y, -1, rt, rot,
											allowUpward, pieceNow, fld, null);

									if (kick != null) {
										newX = x + kick.offsetX;
										newY = y + kick.offsetY;
										pts = thinkMainByLineStack(engine, newX, newY, rot, rt, fld, pieceNow, pieceNext,
												pieceHold, depth);
									}
								}

								if (pts > bestOppPts) {
									bestOppHold = false;
									bestOppX = x;
									bestOppY = y;
									bestOppRt = rt;
									bestOppXSub = newX;
									bestOppYSub = newY;
									bestOppRtSub = rot;
									bestOppPts = pts;
									savedLastf = lastf;
								}
							}

							// Rightrotation
							if (engine.isRotateButtonDefaultRight() || engine.ruleopt.rotateButtonAllowReverse) {
								int rot = pieceNow.getRotateDirection(1, rt);
								int newX = x;
								int newY = y;
								fld.copy(engine.field);
								pts = 0;

								if (!pieceNow.checkCollision(x, y, rot, fld)) {
									pts = thinkMainByLineStack(engine, x, y, rot, rt, fld, pieceNow, pieceNext, pieceHold, depth);
								} else if ((engine.wallkick != null) && (engine.ruleopt.rotateWallkick)) {
									boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0)
											|| (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
									WallkickResult kick = engine.wallkick.executeWallkick(x, y, 1, rt, rot, allowUpward,
											pieceNow, fld, null);

									if (kick != null) {
										newX = x + kick.offsetX;
										newY = y + kick.offsetY;
										pts = thinkMainByLineStack(engine, newX, newY, rot, rt, fld, pieceNow, pieceNext,
												pieceHold, depth);
									}
								}

								if (pts > bestOppPts) {
									bestOppHold = false;
									bestOppX = x;
									bestOppY = y;
									bestOppRt = rt;
									bestOppXSub = newX;
									bestOppYSub = newY;
									bestOppRtSub = rot;
									bestOppPts = pts;
									savedLastf = lastf;
								}
							}

							// 180-degree rotation
							if (engine.ruleopt.rotateButtonAllowDouble) {
								int rot = pieceNow.getRotateDirection(2, rt);
								int newX = x;
								int newY = y;
								fld.copy(engine.field);
								pts = 0;

								if (!pieceNow.checkCollision(x, y, rot, fld)) {
									pts = thinkMainByLineStack(engine, x, y, rot, rt, fld, pieceNow, pieceNext, pieceHold, depth);
								} else if ((engine.wallkick != null) && (engine.ruleopt.rotateWallkick)) {
									boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0)
											|| (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
									WallkickResult kick = engine.wallkick.executeWallkick(x, y, 2, rt, rot, allowUpward,
											pieceNow, fld, null);

									if (kick != null) {
										newX = x + kick.offsetX;
										newY = y + kick.offsetY;
										pts = thinkMainByLineStack(engine, newX, newY, rot, rt, fld, pieceNow, pieceNext,
												pieceHold, depth);
									}
								}

								if (pts > bestOppPts) {
									bestOppHold = false;
									bestOppX = x;
									bestOppY = y;
									bestOppRt = rt;
									bestOppXSub = newX;
									bestOppYSub = newY;
									bestOppRtSub = rot;
									bestOppPts = pts;
									savedLastf = lastf;
								}
							}
						}
					}
				}

				if (pieceHold == null) {
					pieceHold = engine.getNextObject(engine.nextPieceCount);
				}
				// Hold Peace
				if ((holdOK == true) && (pieceHold != null) && (depth == 0)) {
					int spawnX = engine.getSpawnPosX(engine.field, pieceHold);
					int spawnY = engine.getSpawnPosY(pieceHold);
					int minHoldX = pieceHold.getMostMovableLeft(spawnX, spawnY, rt, engine.field);
					int maxHoldX = pieceHold.getMostMovableRight(spawnX, spawnY, rt, engine.field);

					for (int x = minHoldX; x <= maxHoldX; x++) {
						fld.copy(engine.field);
						int y = pieceHold.getBottom(x, spawnY, rt, fld);

						if (!pieceHold.checkCollision(x, y, rt, fld)) {
							Piece pieceNext2 = engine.getNextObject(engine.nextPieceCount);
							if (holdEmpty)
								pieceNext2 = engine.getNextObject(engine.nextPieceCount + 1);

							double pts = thinkMainByLineStack(engine, x, y, rt, -1, fld, pieceHold, pieceNext2, null, depth);

							if (pts > bestOppPts) {
								bestOppHold = true;
								bestOppX = x;
								bestOppY = y;
								bestOppRt = rt;
								bestOppRtSub = -1;
								bestOppPts = pts;
								savedLastf = lastf;
							}
						}
					}
				}
			}

			if (bestOppPts > 0)
				break;
		}

		System.out.println("X:" + bestOppX + " Y:" + bestOppY + " R:" + bestOppRt + " H:" +
		bestOppHold + " Pts:" + bestOppPts);
//		for (int i = 0; i < TwoPlayerBasisFunction.FEATURE_COUNT; i++)
//			System.out.println(lastf[i]);
		System.out.println(savedLastf[3]);
	}
	
	/**
	 * Think routine
	 * 
	 * @param engine    GameEngine
	 * @param x         X-coordinate
	 * @param y         Y-coordinate
	 * @param rt        Direction
	 * @param rtOld     Direction before rotation (-1: None)
	 * @param fld       Field (Can be modified without problems)
	 * @param piece     Piece
	 * @param nextpiece NEXTPeace
	 * @param holdpiece HOLDPeace(nullMay be)
	 * @param depth     Compromise level (ranges from 0 through getMaxThinkDepth-1)
	 * @return Evaluation score
	 */
	public double thinkMainByLineStack(GameEngine engine, int x, int y, int rt, int rtOld, Field fld, Piece piece, Piece nextpiece,
			Piece holdpiece, int depth) {
		double pts = 0;
		
		// T-Spin flag
		boolean tspin = false;
		if ((piece.id == Piece.PIECE_T) && (rtOld != -1) && (fld.isTSpinSpot(x, y, piece.big))) {
			tspin = true;
		}
		
		// Place the piece
		if (!piece.placeToField(x, y, rt, fld)) {
			return 0;
		}

		// Line clear
		int lines = fld.checkLine();
		if (lines > 0) {
			fld.clearLine();
			fld.downFloatingBlocks();
		}

		// All clear
		boolean allclear = fld.isEmpty();
		if (allclear)
			pts += 500000;
		
		int lineSent = nowState_opp.calLinesSentResult(lines);
		
		// Convert to future State

		futureState_opp = null;
		oppFutureState_opp = null;
		
		// My future state
		futureState_opp = createFutureState(nowState_opp, fld, engine, 0, lineSent, lines);

		if (twoPlayerGame) {
			GameEngine oppEngine;
			Field oppFld;
			oppEngine = engine.owner.engine[1 - engine.playerID];
			oppFld = oppEngine.field;
			// Opp future state
			oppFutureState_opp = createFutureState(oppNowState_opp, oppFld, oppEngine, lineSent, 0, 0);
		}
		
		double[] f;
		if (twoPlayerGame) {
			f = player.getTwoPlayerBasisFunctions().getFeatureArray(nowState_opp, futureState_opp, oppNowState_opp, oppFutureState_opp);
			for (int i = 0; i < TwoPlayerBasisFunction.FEATURE_COUNT; i++) {
				pts += f[i] * player.getTwoPlayerBasisFunctions().weight[i];
				//log.error(i + ":" + f[i]);
			}
		} else {
			f = player.getBasisFunctions().getFeatureArray(nowState_opp, futureState_opp);
			for (int i = 0; i < BasisFunction.FEATURE_COUNT; i++) {
				pts += f[i] * player.getBasisFunctions().weight[i];
				//log.error(i + ":" + f[i]);
			}
		}
		
		lastf = f;
        
		return pts;
	}
	
	/**
	 * Think routine
	 * 
	 * @param engine    GameEngine
	 * @param x         X-coordinate
	 * @param y         Y-coordinate
	 * @param rt        Direction
	 * @param rtOld     Direction before rotation (-1: None)
	 * @param fld       Field (Can be modified without problems)
	 * @param piece     Piece
	 * @param nextpiece NEXTPeace
	 * @param holdpiece HOLDPeace(nullMay be)
	 * @param depth     Compromise level (ranges from 0 through getMaxThinkDepth-1)
	 * @return Evaluation score
	 */
	public double thinkMain(GameEngine engine, int x, int y, int rt, int rtOld, Field fld, Piece piece, Piece nextpiece,
			Piece holdpiece, int depth) {
		double pts = 0;
		
		State oppNowState = this.oppNowState;
		
		// T-Spin flag
		boolean tspin = false;
		if ((piece.id == Piece.PIECE_T) && (rtOld != -1) && (fld.isTSpinSpot(x, y, piece.big))) {
			tspin = true;
		}
		
		// Place the piece
		if (!piece.placeToField(x, y, rt, fld)) {
			return 0;
		}

		// Line clear
		int lines = fld.checkLine();
		if (lines > 0) {
			fld.clearLine();
			fld.downFloatingBlocks();
		}
		
		// All clear
		boolean allclear = fld.isEmpty();
		if (allclear)
			pts += 500000;
		
		int lineSent = nowState.calLinesSentResult(lines);
		
		// Convert to future State

		futureState = null;
		oppFutureState = null;
		

		if (twoPlayerGame) {	
			GameEngine oppEngine;
			oppEngine = engine.owner.engine[1 - engine.playerID];
			Field oppFld = new Field();
			Piece oppNow = new Piece();
			int oppLines = 0;
			Piece saveNowPiece = oppEngine.nowPieceObject;
			if (saveNowPiece != null) {
				oppFld.copy(oppEngine.field);
				oppNow.copy(saveNowPiece);
				//thinkbestOppPosition(oppEngine, 1 - engine.playerID);
				oppFld.addSingleHoleGarbage((int)(Math.random() * width), 0, 0, lineSent);
			}
			oppNowState = createState(oppFld, oppEngine);
			int oppLineSent = oppNowState.calLinesSentResult(lines);
			//oppFutureState = createFutureState(oppNowState, oppFld, oppEngine, 0, oppLineSent, oppLines);
			oppFutureState = createFutureState(oppNowState, oppFld, oppEngine, 0, 0, 0);
			
			// My future state
			//futureState = createFutureState(nowState, fld, engine, oppLineSent, lineSent, lines);
			futureState = createFutureState(nowState, fld, engine, 0, lineSent, lines);
		} else {
			// My future state
			futureState = createFutureState(nowState, fld, engine, 0, lineSent, lines);
		}
		
		double[] f;
		if (twoPlayerGame) {
			f = player.getTwoPlayerBasisFunctions().getFeatureArray(nowState, futureState, oppNowState, oppFutureState);
			for (int i = 0; i < TwoPlayerBasisFunction.FEATURE_COUNT; i++) {
				pts += f[i] * player.getTwoPlayerBasisFunctions().weight[i];
				//log.error(i + ":" + f[i]);
			}
		} else {
			f = player.getBasisFunctions().getFeatureArray(nowState, futureState);
			for (int i = 0; i < BasisFunction.FEATURE_COUNT; i++) {
				pts += f[i] * player.getBasisFunctions().weight[i];
				//log.error(i + ":" + f[i]);
			}
		}
		
		lastf = f;
        
		//log.error(pts);
		return pts;
		/*
		int pts = 0;

		// Add points for being adjacent to other blocks
		if (piece.checkCollision(x - 1, y, fld))
			pts += 1;
		if (piece.checkCollision(x + 1, y, fld))
			pts += 1;
		if (piece.checkCollision(x, y - 1, fld))
			pts += 100;

		// Number of holes and valleys needing an I piece (before placement)
		int holeBefore = fld.getHowManyHoles();
		int lidBefore = fld.getHowManyLidAboveHoles();
		int needIValleyBefore = fld.getTotalValleyNeedIPiece();
		// Field height (before placement)
		int heightBefore = fld.getHighestBlockY();
		// T-Spin flag
		boolean tspin = false;
		if ((piece.id == Piece.PIECE_T) && (rtOld != -1) && (fld.isTSpinSpot(x, y, piece.big))) {
			tspin = true;
		}

		// Place the piece
		if (!piece.placeToField(x, y, rt, fld)) {
			return 0;
		}

		// Line clear
		int lines = fld.checkLine();
		if (lines > 0) {
			fld.clearLine();
			fld.downFloatingBlocks();
		}

		// All clear
		boolean allclear = fld.isEmpty();
		if (allclear)
			pts += 500000;

		// Field height (after clears)
		int heightAfter = fld.getHighestBlockY();

		// Danger flag
		boolean danger = (heightAfter <= 12);

		// Additional points for lower placements
		if ((!danger) && (depth == 0))
			pts += y * 10;
		else
			pts += y * 20;

		// LinescountAdditional points in
		if ((lines == 1) && (!danger) && (depth == 0) && (heightAfter >= 16) && (holeBefore < 3) && (!tspin)
				&& (engine.combo < 1)) {
			return 0;
		}
		if ((!danger) && (depth == 0)) {
			if (lines == 1)
				pts += 10;
			if (lines == 2)
				pts += 50;
			if (lines == 3)
				pts += 100;
			if (lines >= 4)
				pts += 100000;
		} else {
			if (lines == 1)
				pts += 5000;
			if (lines == 2)
				pts += 10000;
			if (lines == 3)
				pts += 30000;
			if (lines >= 4)
				pts += 100000;
		}

		if ((lines < 4) && (!allclear)) {
			// Number of holes and valleys needing an I piece (after placement)
			int holeAfter = fld.getHowManyHoles();
			int lidAfter = fld.getHowManyLidAboveHoles();
			int needIValleyAfter = fld.getTotalValleyNeedIPiece();

			if (holeAfter > holeBefore) {
				// Demerits for new holes
				pts -= (holeAfter - holeBefore) * 10;
				if (depth == 0)
					return 0;
			} else if (holeAfter < holeBefore) {
				// Add points for reduction in number of holes
				if (!danger)
					pts += (holeBefore - holeAfter) * 5;
				else
					pts += (holeBefore - holeAfter) * 10;
			}

			if (lidAfter > lidBefore) {
				// Is riding on top of the holeBlockIncreasing the deduction
				if (!danger)
					pts -= (lidAfter - lidBefore) * 10;
				else
					pts -= (lidAfter - lidBefore) * 20;
			} else if (lidAfter < lidBefore) {
				// Add points for reduction in number blocks above holes
				if (!danger)
					pts += (lidBefore - lidAfter) * 10;
				else
					pts += (lidBefore - lidAfter) * 20;
			}

			if ((tspin) && (lines >= 1)) {
				// T-Spin bonus
				pts += 100000 * lines;
			}

			if ((needIValleyAfter > needIValleyBefore) && (needIValleyAfter >= 2)) {
				// 2One or moreIDeduction and make a hole type is required
				pts -= (needIValleyAfter - needIValleyBefore) * 10;
				if (depth == 0)
					return 0;
			} else if (needIValleyAfter < needIValleyBefore) {
				// Add points for reduction in number of holes
				if ((depth == 0) && (!danger))
					pts += (needIValleyBefore - needIValleyAfter) * 10;
				else
					pts += (needIValleyBefore - needIValleyAfter) * 20;
			}

			if (heightBefore < heightAfter) {
				// Add points for reducing the height
				if ((depth == 0) && (!danger))
					pts += (heightAfter - heightBefore) * 10;
				else
					pts += (heightAfter - heightBefore) * 20;
			} else if (heightBefore > heightAfter) {
				// Demerits for increase in height
				if ((depth > 0) || (danger))
					pts -= (heightBefore - heightAfter) * 4;
			}

			// Combo bonus
			if ((lines >= 1) && (engine.comboType != GameEngine.COMBO_TYPE_DISABLE)) {
				pts += lines * engine.combo * 100;
			}
		}

		return pts;
		*/
	}

	private State createState(Field fld, GameEngine engine) {
		State newState = new State();
		int[][] transformed_fld = new int[height + hiddenHeight][width];
        for (int c = 0; c < width; c++) {
        	for (int r = -hiddenHeight; r < height; r++) {
        		transformed_fld[r + hiddenHeight][c] = fld.getBlockEmpty(c, r) ? 0 : 1;
        	}
        }
        int heightest;
        int rr;
        int tmp;
        for (int c = 0; c < State.COLS; c++) {
			heightest = 0;
	        for (int r = 0; r < State.ROWS; r++) {
				try {
					tmp = 0;
					try {
						rr = (State.ROWS - r - 1);
						if (rr > 0)
							tmp = transformed_fld[(State.ROWS - r - 1)][c];
					} catch(Exception e) {
						tmp = 0;
					}
					newState.getField()[r][c] = tmp;
					if (tmp == 1)
						heightest = Math.max(r + 1, heightest);
				} catch(Exception e) {
					System.out.println(e);
				}
	        }
	        newState.getTop()[c] = heightest;
        }
        newState.addLinesStack(engine.meterValue / 8);
        if (engine.owner.mode instanceof VSBattleMode) {
        	VSBattleMode mode = (VSBattleMode)engine.owner.mode;
        	newState.addLineSent(mode.getCurGarbageSent()[engine.playerID]);
        } else {
        	newState.addLineSent(engine.lineClearing);
        }
        newState.addLineCleared(engine.lineClearing);
        return newState;
	}
	
	private FutureState createFutureState(State oldState, Field fld, GameEngine engine, int deltaMeter, int deltaLineSent, int deltaLineCleared) {
		FutureState newState = new FutureState();
		newState.resetToCurrentState(oldState);
		int[][] transformed_fld = new int[height + hiddenHeight][width];
        for (int c = 0; c < width; c++) {
        	for (int r = -hiddenHeight; r < height; r++) {
        		transformed_fld[r + hiddenHeight][c] = fld.getBlockEmpty(c, r) ? 0 : 1;
        	}
        }
        int heightest;
        int rr;
        int tmp;
        for (int c = 0; c < State.COLS; c++) {
			heightest = 0;
	        for (int r = 0; r < State.ROWS; r++) {
				try {
					tmp = 0;
					try {
						rr = (State.ROWS - r - 1);
						if (rr > 0)
							tmp = transformed_fld[(State.ROWS - r - 1)][c];
					} catch(Exception e) {
						tmp = 0;
					}
					newState.getField()[r][c] = tmp;
					if (tmp == 1)
						heightest = Math.max(r + 1, heightest);
				} catch(Exception e) {
					System.out.println(e);
				}
	        }
	        newState.getTop()[c] = heightest;
        }
        newState.addLinesStack(deltaMeter);
        if (engine.owner.mode instanceof VSBattleMode) {
        	VSBattleMode mode = (VSBattleMode)engine.owner.mode;
        	newState.addLineSent(deltaLineSent);
        } else {
        	newState.addLineSent(deltaLineCleared);
        }
        newState.addLineCleared(deltaLineCleared);
        return newState;
	}

	/**
	 * MaximumCompromise levelGet the
	 * 
	 * @return MaximumCompromise level
	 */
	public int getMaxThinkDepth() {
		return 2;
	}

	/*
	 * Processing of the thread
	 */
	public void run() {
		log.info("LSPIAI: Thread start");
		threadRunning = true;

		while (threadRunning) {
			if (thinkRequest) {
				thinkRequest = false;
				thinking = true;
				try {
					thinkBestPosition(gEngine, gEngine.playerID);
				} catch (Throwable e) {
					log.debug("LSPIAI: thinkBestPosition Failed", e);
				}
				thinking = false;
			}

			if (thinkDelay > 0) {
				try {
					Thread.sleep(thinkDelay);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		threadRunning = false;
		log.info("LSPIAI: Thread end");
	}
}
