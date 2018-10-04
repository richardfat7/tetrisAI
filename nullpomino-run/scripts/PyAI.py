class PyAI():
    def __init__(self, name, engine, playerID):
        self.name = name
        pass
    
    def newPiece(self, engine, playerID):
        pass
    
    def onFirst(self, engine, playerID):
        pass
    
    def onLast(self, engine, playerID):
        pass
    
    def renderState(self, engine, playerID):
        pass
    
    def setControl(self, engine, playerID, ctrl):
        pass
    
    def shutdown(self, engine, playerID):
        pass
        
    def renderHint(self, engine, playerID):
        pass

exportAI = PyAI
