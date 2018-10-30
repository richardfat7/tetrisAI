from PyAI import PyAI
from LSPI.PlayerSkeleton import PlayerSkeleton
from LSPI.State import State

from mu.nu.nullpo.game.play import GameEngine
from mu.nu.nullpo.game.component import Controller, Field, Statistics, Block
from mu.nu.nullpo.game.event import EventReceiver
from mu.nu.nullpo.util import GeneralUtil

from java.lang import Math, String

import pickle

DEBUG = False
TRAIN = False

class PvPAI(PyAI):
    def __init__(self, name):
        self.name = name
        # lspi to nullpomino
        # lspi      : O, I, L, J, T, S, Z
        # nullpomino: I, L, O, Z, T, J, S
        self.lspi2nullpomino = {0: 2, 1: 0, 2: 1, 3: 5, 4: 4, 5: 6, 6: 3}
        self.nullpomino2lspi = {0: 1, 1: 2, 2: 0, 3: 6, 4: 4, 5: 3, 6: 5}
        self.lspi2rotate = [
            [0],
            [3, 0],
            [1, 2, 3, 0],
            [3, 0, 1, 2],
            [1, 2, 3, 0],
            [0, 1],
            [0, 1]
        ]
        self.lspi2X = [
            [0],
            [-1, 0],
            [-1, 0, 0, 0],
            [0, 0, -1, 0],
            [-1, 0, 0, 0],
            [0, -1],
            [0, -1]
        ]

        self.bestHold = False
        self.forceHold = False
    
    def init(self, engine, playerID):
        self.loadWeight();
        self.player = PlayerSkeleton()
        self.player.learns = TRAIN
        if len(self.weights) > 0:
            self.player.getBasisFunctions().weight = self.weights
        self.state = State()
        # init value
        self.move = 0
        self.nowid = 0

        self.delay = 0
        self.thinkRequest = False
        self.thinking = False
        self.threadRunning = False

        self.forgive = False
        self.bestX = 0 # change
        self.bestY = 21
        self.bestXSub = 0
        self.bestYSub = 21
        self.bestPts = 0
        self.bestHold = False
        self.forceHold = False
        self.bestRt = 0 # change
        self.bestRtSub = -1
        self.delay = 0
        self.gEngine = None
        self.gManager = None
        self.thinkDelay = None
        self.height = 20
        self.width = 10
    
    def newPiece(self, engine, playerID):
        # Transform filed into State field representation
        self.height = engine.fieldHeight
        self.width = engine.fieldWidth
        fld = engine.field
        transformed_fld = [[0 for _ in range(self.width)] for _ in range(self.height)]
        for c in range(self.width):
            for r in range(self.height):
                transformed_fld[r][c] = 0 if fld.getBlockEmpty(c, r) else 1
        self.state.saveField(transformed_fld)
        
        
        self.nowid = engine.nowPieceObject.id
        self.state.nextPiece = self.nullpomino2lspi[self.nowid]
        if DEBUG:
            print('NowId:',self.nowid)
            print('Piece:',self.state.nextPiece)
        tmpLegalMoves = self.state.getLegalMoves()
        self.move = self.player.pickMove(self.state,tmpLegalMoves)
        
        [orient, offset] = tmpLegalMoves[self.move]
        self.bestX = offset + self.lspi2X[self.state.nextPiece][orient]
        self.bestXSub = self.bestX
        self.bestRt = self.lspi2rotate[self.state.nextPiece][orient]
        if DEBUG:
            print('Orient LSPI:',orient)
            print('Offset LSPI:',offset)
            print('Orient NULL:',self.bestRt)
            print('Offset NULL:',self.bestX)

        # simulate inside AI
        self.state.makeMove(self.move)
        if TRAIN:
            #self.player.getBasisFunctions().computeWeights()
            #print(self.player.bs.weight)
            pass
        if DEBUG:
            # print field
            self.player.printField(self.state.getField())
    
    def onFirst(self, engine, playerID):
        pass
    
    def onLast(self, engine, playerID):
        pass
    
    def renderState(self, engine, playerID):
        r = engine.owner.receiver
        r.drawScoreFont(engine, playerID, 19, 39, self.name.upper(), EventReceiver.COLOR_GREEN, 0.5)
        r.drawScoreFont(engine, playerID, 24, 40, "X", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 40, "Y", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 30, 40, "RT", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 19, 41, "BEST:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 24, 41, String.valueOf(self.bestX), 0.5)
        r.drawScoreFont(engine, playerID, 27, 41, String.valueOf(self.bestY), 0.5)
        r.drawScoreFont(engine, playerID, 30, 41, String.valueOf(self.bestRt), 0.5)
        r.drawScoreFont(engine, playerID, 19, 42, "SUB:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 24, 42, String.valueOf(self.bestXSub), 0.5)
        r.drawScoreFont(engine, playerID, 27, 42, String.valueOf(self.bestYSub), 0.5)
        r.drawScoreFont(engine, playerID, 30, 42, String.valueOf(self.bestRtSub), 0.5)
        r.drawScoreFont(engine, playerID, 19, 43, "NOW:", EventReceiver.COLOR_BLUE, 0.5)
        if (engine.nowPieceObject == None):
            r.drawScoreFont(engine, playerID, 24, 43, "-- -- --", 0.5)
        else:
            r.drawScoreFont(engine, playerID, 24, 43, String.valueOf(engine.nowPieceX), 0.5)
            r.drawScoreFont(engine, playerID, 27, 43, String.valueOf(engine.nowPieceY), 0.5)
            r.drawScoreFont(engine, playerID, 30, 43, String.valueOf(engine.nowPieceObject.direction), 0.5)
        r.drawScoreFont(engine, playerID, 19, 44, "NOWID:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 44, String.valueOf(self.nowid), EventReceiver.COLOR_WHITE, 0.5)
        r.drawScoreFont(engine, playerID, 19, 45, "LSPIID:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 45, String.valueOf(self.state.nextPiece), EventReceiver.COLOR_WHITE, 0.5)
        r.drawScoreFont(engine, playerID, 19, 46, "ORIENT:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 46, String.valueOf(self.bestRt), EventReceiver.COLOR_RED, 0.5)
        r.drawScoreFont(engine, playerID, 19, 47, "OFFSET:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 47, String.valueOf(self.bestX), EventReceiver.COLOR_RED, 0.5)
    
    def setControl(self, engine, playerID, ctrl):
        if( (not engine.nowPieceObject == None) and (engine.stat == GameEngine.Status.MOVE) and (self.delay >= engine.aiMoveDelay) and (engine.statc[0] > 0) and
            (not engine.aiUseThread or (self.threadRunning and not self.thinking and (self.thinkCurrentPieceNo <= self.thinkLastPieceNo))) ):
            input = 0    #  button input data
            pieceNow = engine.nowPieceObject
            nowX = engine.nowPieceX
            nowY = engine.nowPieceY
            rt = pieceNow.direction
            fld = engine.field
            pieceTouchGround = pieceNow.checkCollision(nowX, nowY + 1, fld)

            if((self.bestHold or self.forceHold) and engine.isHoldOK()):
                # Hold
                input |= ctrl.BUTTON_BIT_D
            else:
                # rotation
                if(not rt == self.bestRt):
                    lrot = engine.getRotateDirection(-1)
                    rrot = engine.getRotateDirection(1)

                    if((Math.abs(rt - self.bestRt) == 2) and (engine.ruleopt.rotateButtonAllowDouble) and not ctrl.isPress(ctrl.BUTTON_E)):
                        input |= ctrl.BUTTON_BIT_E
                    elif(not ctrl.isPress(ctrl.BUTTON_B) and engine.ruleopt.rotateButtonAllowReverse and
                              not engine.isRotateButtonDefaultRight() and (self.bestRt == rrot)):
                        input |= ctrl.BUTTON_BIT_B
                    elif(not ctrl.isPress(ctrl.BUTTON_B) and engine.ruleopt.rotateButtonAllowReverse and
                              engine.isRotateButtonDefaultRight() and (self.bestRt == lrot)):
                        input |= ctrl.BUTTON_BIT_B
                    elif(not ctrl.isPress(ctrl.BUTTON_A)):
                        input |= ctrl.BUTTON_BIT_A

                # Whether reachable position
                minX = pieceNow.getMostMovableLeft(nowX, nowY, rt, fld)
                maxX = pieceNow.getMostMovableRight(nowX, nowY, rt, fld)

                if( ((self.bestX < minX - 1) or (self.bestX > maxX + 1) or (self.bestY < nowY)) and (rt == self.bestRt) ):
                    # Again because it is thought unreachable
                    #thinkBestPosition(engine, playerID)
                    self.thinkRequest = True
                    # Force place the block
                    #input |= ctrl.BUTTON_BIT_UP
                    #thinkCurrentPieceNo++
                    #System.out.println("rethink c:" + thinkCurrentPieceNo + " l:" + thinkLastPieceNo)
                else:
                    # If you are able to reach
                    if((nowX == self.bestX) and (pieceTouchGround) and (rt == self.bestRt)):
                        # Groundrotation
                        if(not self.bestRtSub == -1):
                            self.bestRt = self.bestRtSub
                            self.bestRtSub = -1
                        # Shift move
                        if(not self.bestX == self.bestXSub):
                            self.bestX = self.bestXSub
                            self.bestY = self.bestYSub

                    if(nowX > self.bestX):
                        # Left
                        if(not ctrl.isPress(ctrl.BUTTON_LEFT) or (engine.aiMoveDelay >= 0)):
                            input |= ctrl.BUTTON_BIT_LEFT
                    elif(nowX < self.bestX):
                        # Right
                        if(not ctrl.isPress(ctrl.BUTTON_RIGHT) or (engine.aiMoveDelay >= 0)):
                            input |= ctrl.BUTTON_BIT_RIGHT
                    elif((nowX == self.bestX) and (rt == self.bestRt)):
                        # Funnel
                        if((self.bestRtSub == -1) and (self.bestX == self.bestXSub)):
                            if(engine.ruleopt.harddropEnable and not ctrl.isPress(ctrl.BUTTON_UP)):
                                input |= ctrl.BUTTON_BIT_UP
                            elif(engine.ruleopt.softdropEnable or engine.ruleopt.softdropLock):
                                input |= ctrl.BUTTON_BIT_DOWN
                        else:
                            if(engine.ruleopt.harddropEnable and not engine.ruleopt.harddropLock and not ctrl.isPress(ctrl.BUTTON_UP)):
                                input |= ctrl.BUTTON_BIT_UP
                            elif(engine.ruleopt.softdropEnable and not engine.ruleopt.softdropLock):
                                input |= ctrl.BUTTON_BIT_DOWN

            ctrl.setButtonBit(input)
        else:
            ctrl.setButtonBit(0)
    
    def shutdown(self, engine, playerID):
        if TRAIN:
            print(self.player.getBasisFunctions().weight)
            self.player.getBasisFunctions().computeWeights()
            print(self.player.getBasisFunctions().weight)
            #print(self.player.bs.weight)
            self.saveWeight();
        pass
        
    def renderHint(self, engine, playerID):
        pass
    
    def saveWeight(self):
        print("SAVE WEIGHTS")
        print(self.player.getBasisFunctions().weight)
        with open('weights.pkl', 'wb') as f:
            pickle.dump(self.player.getBasisFunctions().weight, f)
    
    def loadWeight(self):
        print("LOAD WEIGHTS")
        try:
            with open('weights.pkl', 'rb') as f:
                self.weights = pickle.load(f)
        except IOError:
            self.weights = []
        print(self.weights)

exportAI = PvPAI
