import copy

from LSPI.State import State

class FutureState(State):
	def __init__(self):
		super().__init__()
		s = State()

		self.pBottom = s.getpBottom()
		self.pHeight = s.getpHeight()
		self.pTop = s.getpTop()

		self.turn = 0
		self.cleared = 0

		self.field = [[0 for _ in range(self.COLS)] for _ in range(self.ROWS)]
		self.top = [0 for _ in range(self.COLS)]

	def getField(self):
		return self.field

	def getTop(self):
		return self.top

	def getNextPiece(self):
		return self.nextPiece
	def setNextPiece(self,nextPiece):
		self.nextPiece = nextPiece

	def hasLost(self):
		return self.lost

	def getRowsCleared(self):
		return self.cleared

	def getTurnNumber(self):
		return self.turn
	def resetToCurrentState(self,s):
		field = s.getField()
		self.nextPiece = s.getNextPiece()
		self.lost = s.hasLost()
		self.cleared = s.getRowsCleared()
		self.turn = s.getTurnNumber()
		for i in range(len(self.top)):
			self.top[i] = 0
		for i in reversed(range(len(field))):
			self.field[i] = copy.deepcopy(field[i])
			for j in range(len(self.top)):
				if self.top[j] == 0 and field[i][j] > 0:
					self.top[j] = i + 1
	# gives legal moves for 
	def getLegalMoves(self):
		return self.legalMoves[self.nextPiece]

	def makeMove(self,*args):
		# make a move based on the move index - its order in the legalMoves list
		# args[0] - move index
		if len(args) == 1 and isinstance(args[0],int):
			self.makeMove(self.legalMoves[self.nextPiece][args[0]])
		# make a move based on an array of orient and slot
		# args[0] - move list
		elif len(args) == 1 and isinstance(args[0],list):
			self.makeMove(args[0][self.ORIENT],args[0][self.SLOT])
		# returns False if you lose - True otherwise
		# args[0] - orient
		# args[1] - slot
		elif len(args) == 2 and isinstance(args[0],int) and isinstance(args[1],int):
			self.turn += 1
			# height if the first column makes contact
			height = self.top[args[1]] - self.pBottom[self.nextPiece][args[0]][0]
			# for each column beyond the first in the piece
			for c in range(1,self.pWidth[self.nextPiece][args[0]]):
				height = max(height,self.top[args[1]+c]-self.pBottom[self.nextPiece][args[0]][c])
			# check if game ended
			if height + self.pHeight[self.nextPiece][args[0]] >= self.ROWS:
				self.lost = True
				return False
			# for each column in the piece - fill in the appropriate blocks
			for i in range(self.pWidth[self.nextPiece][args[0]]):
				# from bottom to top of brick
				for h in range(height+self.pBottom[self.nextPiece][args[0]][i],height+self.pTop[self.nextPiece][args[0]][i]):
					self.field[h][i+args[1]] = self.turn
			# adjust top
			for c in range(self.pWidth[self.nextPiece][args[0]]):
				self.top[args[1]+c] = height + self.pTop[self.nextPiece][args[0]][c]

			rowsCleared = 0
			# check for full rows - starting at the top
			for r in reversed(range(height,height+self.pHeight[self.nextPiece][args[0]])):
				# check all columns in the row
				full = True
				for c in range(self.COLS):
					if self.field[r][c] == 0:
						full = False
						break
				# if the row was full - remove it and slide above stuff down
				if full:
					rowsCleared += 1
					self.cleared += 1
					# for each column
					for c in range(self.COLS):
						# slide down all bricks
						for i in range(r,self.top[c]):
							self.field[i][c] = self.field[i+1][c]
						# lower the top
						self.top[c] -= 1
						while self.top[c] >= 1 and self.field[self.top[c]-1][c] == 0:
							self.top[c] -= 1
			return True
		else:
			print("FutureState MakeMove ERROR!")