import random

class State:
	def __init__(self):
		self.COLS = 10
		self.ROWS = 21
		self.N_PIECES = 7

		self.lost = False
	
		# current turn
		self.turn = 0
		self.cleared = 0
	
		# each square in the grid - int means empty - other values mean the turn it was placed
		self.field = [[0 for _ in range(self.COLS)] for _ in range(self.ROWS)]
		# top row+1 of each column
		# 0 means empty
		self.top = [0 for _ in range(self.COLS)]
	
		# all legal moves - first index is piece type - then a list of 2-length arrays
		self.legalMoves = [0 for _ in range(self.N_PIECES)]
	
		# indices for legalMoves
		self.ORIENT = 0
		self.SLOT = 1
	
		# possible orientations for a given piece type
		self.pOrients = [1,2,4,4,4,2,2]
	
		# the next several arrays define the piece vocabulary in detail
		# width of the pieces [piece ID][orientation]
		self.pWidth = [
			[2],
			[1,4],
			[2,3,2,3],
			[2,3,2,3],
			[2,3,2,3],
			[3,2],
			[3,2]
		]
		# height of the pieces [piece ID][orientation]
		self.pHeight = [
			[2],
			[4,1],
			[3,2,3,2],
			[3,2,3,2],
			[3,2,3,2],
			[2,3],
			[2,3]
		]
		self.pBottom = [
			[[0,0]],
			[[0],[0,0,0,0]],
			[[0,0],[0,1,1],[2,0],[0,0,0]],
			[[0,0],[0,0,0],[0,2],[1,1,0]],
			[[0,1],[1,0,1],[1,0],[0,0,0]],
			[[0,0,1],[1,0]],
			[[1,0,0],[0,1]]
		]
		self.pTop = [
			[[2,2]],
			[[4],[1,1,1,1]],
			[[3,1],[2,2,2],[3,3],[1,1,2]],
			[[1,3],[2,1,1],[3,3],[2,2,2]],
			[[3,2],[2,2,2],[2,3],[1,2,1]],
			[[1,2,2],[3,2]],
			[[2,2,1],[2,3]]
		]
	
		# initialize legalMoves
	#{
		# for each piece type
		for i in range(self.N_PIECES):
			# figure number of legal moves
			n = 0
			for j in range(self.pOrients[i]):
				# number of locations in this orientation
				n += self.COLS + 1 - self.pWidth[i][j]
			# allocate space
			self.legalMoves[i] = [[0 for _ in range(2)] for _ in range(n)]
			# for each orientation
			n = 0
			for j in range(self.pOrients[i]):
				# for each slot
				for k in range(self.COLS+1-self.pWidth[i][j]):
					self.legalMoves[i][n][self.ORIENT] = j
					self.legalMoves[i][n][self.SLOT] = k
					n += 1
					
		self.nextPiece = self.randomPiece()
	#}
	
	def saveField(self, field):
		for c in range(self.COLS):
			heightest = 0
			for r in range(self.ROWS):
				try:
					try:
						rr = (self.ROWS - r - 2)
						if rr > 0:
							tmp = field[(self.ROWS - r - 2)][c]
					except:
						tmp = 0
					self.field[r][c] = tmp
					if tmp == 1:
						heightest = max(r + 1, heightest)
				except:
					pass
			self.top[c] = heightest
		'''
		print(self.top);
		print("SELFFIELD")
		for r in range(self.ROWS):
			for c in range(self.COLS):
				print("{:>5}".format(self.field[r][c]), end="")
			print()
		print("FIELD")
		for r in range(len(field)):
			for c in range(len(field[r])):
				print("{:>5}".format(field[r][c]), end="")
			print()
		'''

	def getField(self):
		return self.field

	def getTop(self):
		return self.top

	def getpOrients(self):
		return self.pOrients

	def getpWidth(self):
		return self.pWidth

	def getpHeight(self):
		return self.pHeight

	def getpBottom(self):
		return self.pBottom

	def getpTop(self):
		return self.pTop

	def getNextPiece(self):
		return self.nextPiece

	def hasLost(self):
		return self.lost

	def getRowsCleared(self):
		return self.cleared

	def getTurnNumber(self):
		return self.turn

	# random integer, returns 0-6
	def randomPiece(self):
		return random.randint(0,self.N_PIECES-1)


	# gives legal moves for 
	def getLegalMoves(self):
		return self.legalMoves[self.nextPiece]
	
	def makeMove(self,*args):
		# make a move based on the move index - its order in the legalMoves list
		if len(args) == 1 and isinstance(args[0],int):
			self.makeMove(self.legalMoves[self.nextPiece][args[0]])
		# make a move based on an array of orient and slot
		elif len(args) == 1 and isinstance(args[0],list):
			self.makeMove(args[0][self.ORIENT],args[0][self.SLOT])
		# returns False if you lose - True otherwise
		elif len(args) == 2 and isinstance(args[0],int) and isinstance(args[0],int):
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

			# pick a new piece
			# self.nextPiece = self.randomPiece()
			return True
		else:
			print("State MakeMove ERROR!")