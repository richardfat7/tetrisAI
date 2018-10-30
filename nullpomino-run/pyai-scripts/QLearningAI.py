from PyAI import PyAI
import pickle
import sys
from random import random, choice
import time

from mu.nu.nullpo.game.play import GameEngine
from mu.nu.nullpo.game.component import Controller, Field, Statistics
from mu.nu.nullpo.game.event import EventReceiver
from mu.nu.nullpo.util import GeneralUtil

from java.lang import Math, String

IS_TRAINING = True
IS_DEBUG = False

def argmax(adict):
    if IS_DEBUG:
        print(adict)
    maxScore = max(adict.values())
    allMax = [x for x in adict if adict[x] == maxScore]
    if IS_DEBUG:
        print(allMax)
    return choice(allMax)
    #return max(adict, key=adict.get)

class BasePvPAI(PyAI):
    NUM_OF_TETROMINO = 7
    NUM_OF_STATE = 10000
    NUM_HEIGHT_CONSIDERED = 1
    EXPLORE_EPSILON = 0.3 if IS_TRAINING else 0
    HOLD_EPSILON = 0.0 #0.5
    ALPHA = 0.1 if IS_TRAINING else 0
    GAMMA = 0.3
    
    def __init__(self, name):
        self.name = name
        self.pieceSinceStart = 0
        
        self.qtable = None
        self.loadQTable()
        if IS_DEBUG:
            print("INITIALIZEING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    
    def init(self, engine, playerID):
        self.gEngine = engine
        self.gManager = engine.owner
        
        self.delay = 0
        self.thinkRequest = False
        self.thinking = False
        self.threadRunning = False
        
        self.forgive = False
        self.bestX = 0
        self.bestY = 21
        self.bestXSub = 0
        self.bestYSub = 0
        self.bestRtSub = 0
        self.bestPts = 0
        self.bestHold = False
        self.forceHold = False
        self.bestRt = 0
        self.bestRtSub = 0
        self.delay = 0
        self.gEngine = None
        self.gManager = None
        self.thinkDelay = None
        
        self.lastState = None
        self.lastAction = None
        self.lastScore = None
        self.lastField = None
        self.lastMaxRowDensity = None
        self.lastReward = None
        self.lastqvalue = None
        self.lastExploreChoice = None
        self.lastHole = None
        
        self.time = 0
        
        self.pieceSinceInitStart = 0
        self.bag = set(range(self.NUM_OF_TETROMINO))
        
        if IS_DEBUG:
            print("init!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    
    def newPiece(self, engine, playerID):
        #self.forgive = True
        #return
        self.deltaTime(">>>start")
        self.pieceSinceStart += 1
        self.pieceSinceInitStart += 1
        nowid = engine.nowPieceObject.id
        self.bag.remove(nowid)
        if len(self.bag) == 0:
            self.bag = set(range(self.NUM_OF_TETROMINO))
        self.thinkMain(engine, playerID)
        #if self.pieceSinceStart % 1000 == 0:
        #    self.saveQTable()
    
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
        r.drawScoreFont(engine, playerID, 19, 44, "REWARD:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 44, String.valueOf(self.lastReward), EventReceiver.COLOR_WHITE if self.lastReward == 0 else (EventReceiver.COLOR_GREEN if self.lastReward > 0 else EventReceiver.COLOR_RED), 0.5)
        r.drawScoreFont(engine, playerID, 19, 45, "QVALUE:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 45, String.valueOf(self.lastqvalue), EventReceiver.COLOR_WHITE if self.lastqvalue == 0 else (EventReceiver.COLOR_GREEN if self.lastqvalue > 0 else EventReceiver.COLOR_RED), 0.5)
        r.drawScoreFont(engine, playerID, 19, 46, "CHOICE:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 46, String.valueOf(self.lastExploreChoice), EventReceiver.COLOR_RED if self.lastExploreChoice == "EXPLORE" else EventReceiver.COLOR_GREEN, 0.5)
        r.drawScoreFont(engine, playerID, 19, 47, "HOLE  :", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 27, 47, String.valueOf(self.lastHole), EventReceiver.COLOR_RED if self.lastHole > 0 else EventReceiver.COLOR_WHITE, 0.5)
        r.drawScoreFont(engine, playerID, 19, 48, "THINK ACTIVE:", EventReceiver.COLOR_BLUE, 0.5)
        r.drawScoreFont(engine, playerID, 32, 48, GeneralUtil.getOorX(self.thinking), 0.5)
        #r.drawScoreFont(engine, playerID, 19, 46, "IN ARE:", EventReceiver.COLOR_BLUE, 0.5)
        #r.drawScoreFont(engine, playerID, 26, 46, GeneralUtil.getOorX(inARE), 0.5)
        pass
    
    def setControl(self, engine, playerID, ctrl):
        if( (not engine.nowPieceObject == None) and (engine.stat == GameEngine.Status.MOVE) and (self.delay >= engine.aiMoveDelay) and (engine.statc[0] > 0) and
            (not engine.aiUseThread or (self.threadRunning and not self.thinking and (self.thinkCurrentPieceNo <= self.thinkLastPieceNo))) ):

            input = 0    #  button input data
            if (self.forgive):
                input |= ctrl.BUTTON_BIT_UP
                ctrl.setButtonBit(input)
                return
            
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

            self.delay = 0
            ctrl.setButtonBit(input)
        else:
            self.delay += 1
            ctrl.setButtonBit(0)
        pass
    
    def shutdown(self, engine, playerID):
        self.saveQTable()
        
    def renderHint(self, engine, playerID):
        pass
    
    def loadQTable(self):
        
        if IS_DEBUG:
            print("LOAD BEGIN")
        try:
            with open('qtable.pkl', 'rb') as f:
                self.qtable = pickle.load(f)
        except IOError:
            print("qtable.pkl not found.")
            # qtable[state][hold][current]
            #self.qtable = [[0 for x in range(self.NUM_OF_TETROMINO)] for y in range(self.NUM_OF_STATE)]
            self.qtable = {}
            #print(sys.getsizeof(self.qtable))
        
        if IS_DEBUG:
            print("LOAD END")
    
    def saveQTable(self):
        if not IS_TRAINING:
            return
        
        if IS_DEBUG:
            print("SAVE BEGIN")
        with open('qtable.pkl', 'wb') as f:
            pickle.dump(self.qtable, f)
        
        if IS_DEBUG:
            print("SAVE END")
    
    def computeState(self, fld, nowPiece, nextPiece):
        heights = [fld.getHighestBlockY(i) for i in range(fld.getWidth())]
        maxHeight = min(heights)
        heights = [min(h - maxHeight, self.NUM_HEIGHT_CONSIDERED) for h in heights]
        return "".join(map(str, heights + [nowPiece.id, nextPiece.id] ))
    
    def computePieceState(self, x, rt):
        return "".join(map(str, [x, rt]))
    
    def decomputePieceStaete(self, pieceState):
        return (int(pieceState[:-1]), int(pieceState[-1]))
    
    def getRowDensity(self, fld, y):
        row = fld.getRow(y)
        empty = 0
        for item in row:
            if item.isEmpty():
                empty += 1
        return fld.getWidth() - empty
        
    def getMaxRowDensity(self, fld):
        allDensity = [self.getRowDensity(fld, i) for i in range(0, fld.getHeight())]
        return max(allDensity)
    
    def thinkMain(self, engine, playerID):
        self.deltaTime(">>>start thinkMain")
        fld = engine.field
        #if fld.getHighestBlockY() < 15:
        #    self.forgive = True
        #    return
        nowPiece = engine.nowPieceObject
        holdPiece = engine.holdPieceObject
        nextPiece = engine.getNextObject(engine.nextPieceCount)
        holdChance = self.HOLD_EPSILON if holdPiece != None else 0
        nowState = self.computeState(engine.field, nowPiece, nextPiece)
        holdState = self.computeState(engine.field, holdPiece, nextPiece) if holdPiece != None else nowState
        
        # Set initial state for the state
        state = nowState
        if not state in self.qtable:
            # Initialize the qtable
            self.qtable[state] = {}
        for rt in range(0, 4):
            for x in range(-nowPiece.getMostMovableLeft(engine.nowPieceX, engine.nowPieceY, rt, fld),
                       nowPiece.getMostMovableRight(engine.nowPieceX, engine.nowPieceY, rt, fld) + 1):
                pieceState = self.computePieceState(x, rt)
                if pieceState in self.qtable[state]:
                    pass
                else:
                    self.qtable[state][pieceState] = 0
        
        '''      
        state = holdState
        if not state in self.qtable:
            # Initialize the qtable
            self.qtable[state] = {}
        for rt in range(0, 4):
            for x in range(nowPiece.getMostMovableLeft(engine.nowPieceX, engine.nowPieceY, rt, fld),
                       nowPiece.getMostMovableRight(engine.nowPieceX, engine.nowPieceY, rt, fld) + 1):
                pieceState = self.computePieceState(x, rt)
                if pieceState in self.qtable[state]:
                    pass
                else:
                    self.qtable[state][pieceState] = 0
        '''
        
        # Process evaluation last action
        if self.lastState != None and self.lastAction != None:
            lastHeight = self.lastField.getHighestBlockY()
            nowHeight = engine.field.getHighestBlockY()
            #panalty on height
            reward  = (nowHeight - lastHeight)
            #reward on line clear
            reward += (engine.statistics.lines - self.lastScore.lines) * 1000
            #reward on fulfilling a line
            reward += (self.getMaxRowDensity(fld) - self.lastMaxRowDensity) * 10
            #panalty on hole
            reward += - (engine.field.getHowManyBlocksCovered() - self.lastHole) * 10
            
            if IS_DEBUG:
                print("REWARD = ", reward)
            self.lastReward = reward
            curQ = self.qtable[self.lastState][self.lastAction]
            nowStateMinusNext = nowState[:-1]
            allPossibleState = []
            for i in self.bag:
                if not nowStateMinusNext + str(i) in self.qtable:
                    self.qtable[nowStateMinusNext + str(i)] = {}
                try:
                    maxi = max(self.qtable[nowStateMinusNext + str(i)].values())
                except ValueError:
                    maxi = 0
                allPossibleState.append(maxi)
            bestQOfNowAndHoldState = max([
                sum(allPossibleState) / len(allPossibleState),
                #max(self.qtable[holdState].values()),
            ])
            self.qtable[self.lastState][self.lastAction] = curQ + self.ALPHA * (reward + self.GAMMA * (bestQOfNowAndHoldState - curQ))
        
        self.lastScore = Statistics(engine.statistics)
        self.lastField = Field(engine.field)
        self.lastHole = engine.field.getHowManyBlocksCovered()
        self.lastMaxRowDensity = self.getMaxRowDensity(fld)
        ########################################################
        
        if IS_DEBUG:
            print("QSCORE:", max(self.qtable[nowState].values()), min(self.qtable[nowState].values()))
        
        # Process choosing next action
        # Choose an action
        if random() < holdChance:
            # Do hold
            state = holdState
            isHold = True
        else:
            # Do original state
            state = nowState
            isHold = False
        
        if IS_DEBUG:
            print(state)
        self.thinkState(engine, playerID, state, nowPiece, nextPiece, isHold)
    
    def thinkState(self, engine, playerID, state, nowPiece, nextPiece, isHold):
        fld = engine.field
        possibleAction = {}
        
        self.deltaTime(">>>start thinkState")
        #print("START")
        #for rt in range(0, 4):
        #    print(nowPiece.getMostMovableLeft(engine.nowPieceX, engine.nowPieceY, rt, fld), nowPiece.getMostMovableRight(engine.nowPieceX, engine.nowPieceY, rt, fld) + 1)
        #print("END")

        for rt in range(0, 4):
            for x in range(nowPiece.getMostMovableLeft(engine.nowPieceX, engine.nowPieceY, rt, fld),
                       nowPiece.getMostMovableRight(engine.nowPieceX, engine.nowPieceY, rt, fld) + 1):
                pieceState = self.computePieceState(x, rt)
                if pieceState in self.qtable[state]:
                    pass
                else:
                    self.qtable[state][pieceState] = 0
                possibleAction[pieceState] = self.qtable[state][pieceState]
        if random() < self.EXPLORE_EPSILON:
            # Choose explore
            chosenAction = choice(list(possibleAction))
            self.lastExploreChoice = "EXPLORE"
                
            if IS_DEBUG:
                print("EXPLORE", chosenAction)
        else:
            # Choose best action
            chosenAction = argmax(possibleAction)
            self.lastExploreChoice = "BEST"
            
            if IS_DEBUG:
                print("BEST", chosenAction)
        
        # Do chosen action
        (x, rt) = self.decomputePieceStaete(chosenAction)
        y = nowPiece.getBottom(x, engine.nowPieceY, rt, fld)
        self.bestX = x
        self.bestY = 21
        self.bestRt = rt
        self.bestXSub = x
        self.bestYSub = 21
        self.bestRtSub = -1
        self.bestHold = isHold
        self.lastState = state
        self.lastAction = chosenAction
        self.lastqvalue = self.qtable[state][chosenAction]
        #print("BOTTOM=", nowPiece.getBottom(x, engine.nowPieceY, rt, fld))
        
        self.deltaTime(">>>end")
        
    def deltaTime(self, message):
        return
        t = time.time()
        print(message, t - self.time)
        self.time = t
    
exportAI = BasePvPAI
