import sys

from LSPI.Matrix import Matrix
from LSPI.State import State

class BasisFunction:
	def __init__(self):
		# discount value, part of the LRQ algo.
		self.DISCOUNT = 0.96

		# Below are features being used for scoring.
		# These are based on the _next_ state.
		# This means these numbers represent the values that are reported if the attempted action is taken.
		# The values starting with DIFF_ represent the differnece between the _next_ state and the current state
		# 
		# Notes:
		# -	SUM_ADJ_DIFF seems to work better squared.
		# -	A well is a whole starting at the top of the wall. Both sides of a well are blocks.
		count = 0

		self.MAX_HEIGHT						= count; count += 1
		self.COVERED_GAPS					= count; count += 1	#(PD)holes in the tetris wall which are inaccessible from the top
		self.DIFF_ROWS_COMPLETED			= count; count += 1	#(LSPI paper)
		self.MAX_MIN_DIFF					= count; count += 1	#(Novel)
		self.MAX_WELL_DEPTH					= count; count += 1	#(Novel)maximum well depth
		self.TOTAL_WELL_DEPTH				= count; count += 1	#(PD)total depth of all wells on the tetris wall.
		self.TOTAL_BLOCKS					= count; count += 1	#(CF)total number of blocks in the wall
		self.COL_TRANS						= count; count += 1	#(PD)
		self.ROW_TRANS						= count; count += 1	#(PD)
		self.DIFF_AVG_HEIGHT				= count; count += 1	#(LSPI paper)
		self.SUM_ADJ_DIFF					= count; count += 1	#(Handout)
		self.DIFF_COVERED_GAPS				= count; count += 1	#(Novel)		
		self.WEIGHTED_WELL_DEPTH			= count; count += 1	#(CF)the deeper the well is, the heavier the "weightage".
		self.LANDING_HEIGHT					= count; count += 1	#(PD)
		self.COL_STD_DEV					= count; count += 1	#(Novel)
	#	self.ERODED_PIECE_CELLS				= count; count += 1	#Intemediary step for WEIGHTED_ERODED_PIECE_CELLS
	#	self.WEIGHTED_ERODED_PIECE_CELLS	= count; count += 1	#(PD)
	#	self.CENTER_DEV						= count; count += 1	#(PD) priority value used to break tie in PD
	#	self.SUM_ADJ_DIFF_SQUARED			= count; count += 1	#(Novel)(sum of the difference between adjacent columns)^2
		self.FEATURE_COUNT = count


		# A and b are important matrices in the LSPI algorithm. Every move the "player" makes, A and b are updated.
		# 
		# A += current_features * (current_features - DISCOUNT*future_features)
		# 						 ^
		# 						 -------This is a matrix multiplication with the transpose of features,
		# 								 so for k features, a k x k matrix is produced.
		# b += reward*current_features
		# 
		# reward = DIFF_ROWS_COMPLETED

		self.A = [[0 for _ in range(self.FEATURE_COUNT)] for _ in range(self.FEATURE_COUNT)]
		self.b = [[0 for _ in range(2)] for _ in range(self.FEATURE_COUNT)]
		self.weight = [0 for _ in range(self.FEATURE_COUNT)]

		#{
		self.weight = [
				-0.180164448251231,
				-0.4000820857296077,
				24.77849828060256,
				0.2064192581442733,
				0.045032898990512216,
				0.021320544290795714,
				0.08917677230797055,
				0.00819042152617841,
				-0.03390773883019901,
				23.781352321845173,
				-0.010694297443763184,
				-2.3906661321409253,
				-0.08863636063644799,
				-0.005240305848723416,
				-0.1524174444288417,
	#			-6.525668167122544E-4,
	#			-8.272234730529223E-4
			]
		#}
		self.features = [0 for _ in range(self.FEATURE_COUNT)]
		self.past     = [0 for _ in range(self.FEATURE_COUNT)]
		
		self.tmpA = [[0 for _ in range(self.FEATURE_COUNT)] for _ in range(self.FEATURE_COUNT)]
		self.mWeight = [[0 for _ in range(2)] for _ in range(self.FEATURE_COUNT)]
		self.mFeatures = [[0 for _ in range(2)] for _ in range(self.FEATURE_COUNT)]
		self.mFutureFeatures = [[0 for _ in range(self.FEATURE_COUNT)] for _ in range(2)]
		self.mRowFeatures = [[0 for _ in range(self.FEATURE_COUNT)] for _ in range(2)]
		self.changeToA = [[0 for _ in range(self.FEATURE_COUNT)] for _ in range(self.FEATURE_COUNT)]

	# Function to get feature array for current state.
	def getFeatureArray(self,s,fs,move):
		#simple features
		#features[ROWS_COMPLETED] = fs.getRowsCleared()
		self.features[self.DIFF_ROWS_COMPLETED] = fs.getRowsCleared() - s.getRowsCleared()
		#compute height features
		currentTurn = s.getTurnNumber()
		currentPiece = s.getNextPiece()
		self.past = self.heightFeatures(s,self.past,currentPiece,currentTurn)
		self.features = self.heightFeatures(fs,self.features,currentPiece,currentTurn)
		self.features[self.DIFF_AVG_HEIGHT] = self.features[self.DIFF_AVG_HEIGHT] - self.past[self.DIFF_AVG_HEIGHT]
		self.features[self.DIFF_COVERED_GAPS] = self.features[self.COVERED_GAPS] - self.past[self.COVERED_GAPS]

		#features[self.DIFF_MAX_HEIGHT] = features[self.MAX_HEIGHT] - past[self.MAX_HEIGHT]
		#features[self.DIFF_SUM_ADJ_DIFF] = features[self.SUM_ADJ_DIFF] - past[self.SUM_ADJ_DIFF]
		#features[self.DIFF_TOTAL_WELL_DEPTH] = features[self.TOTAL_WELL_DEPTH] - past[self.TOTAL_WELL_DEPTH]

		#features[self.CENTER_DEV] = Math.abs(move[State.SLOT] - State.COLS/2.0)
#		features[self.WEIGHTED_ERODED_PIECE_CELLS] = (fs.getRowsCleared()- s.getRowsCleared())*features[ERODED_PIECE_CELLS]
		self.features[self.LANDING_HEIGHT] = s.getTop()[move[s.SLOT]]
		return self.features

	def cellOperations(self,top,field,vals,turnNo):
		s = State()
		rowTrans = 0
		colTrans = 0
		coveredGaps = 0
		totalBlocks = 0
		currentPieceCells = 0
		for i in range(s.ROWS-1):
			if field[i][0] == 0:
				rowTrans += 1
			if field[i][s.COLS-1] == 0:
				rowTrans += 1
			for j in range(s.COLS):
				curIsZero = (field[i][j] == 0)
				prevColIsZero = (field[i][j-1] == 0)
				nextRowIsZero = (field[i+1][j] == 0)
				if j > 0 and curIsZero != prevColIsZero:
					rowTrans += 1
				if curIsZero != nextRowIsZero:
					colTrans += 1
				if i < top[j] and curIsZero:
					coveredGaps += 1
				if not curIsZero:
					totalBlocks += 1
				if field[i][j] == turnNo:
					currentPieceCells += 1
#		vals[self.ERODED_PIECE_CELLS] = 4 - currentPieceCells
		vals[self.COL_TRANS] = colTrans
		vals[self.ROW_TRANS] = rowTrans
		vals[self.COVERED_GAPS] = coveredGaps
		vals[self.TOTAL_BLOCKS] = totalBlocks
		return vals

	# Shared method for obtaining features about the height of the tetris wall.
	# Its pretty messy, but I didn't want to split extracting these features into
	# separate methods, or it might end up doing redundant stuff.
	# 
	# @param s
	# @param vals
	def heightFeatures(self,s,vals,currentPiece,currentTurn):
		field = s.getField()
		top = s.getTop()
		c = s.COLS - 1
		maxWellDepth = 0
		totalWellDepth = 0
		totalWeightedWellDepth = 0
		maxHeight = 0
		minHeight = sys.maxsize
		total = 0
		totalHeightSquared = 0
		diffTotal = 0
		squaredDiffTotal = 0
		for j in range(s.COLS): #by column
			total += top[j]
			totalHeightSquared += top[j] ** 2
			if j > 0:
				diffTotal += abs(top[j-1] - top[j])
				squaredDiffTotal += abs(top[j-1] ** 2 - top[j] ** 2)
			maxHeight = max(maxHeight,top[j])
			minHeight = min(minHeight,top[j])
			if((j == 0 or top[j-1] > top[j]) and (j == c or top[j+1] > top[j])):
				if j == 0:
					wellDepth = top[j+1] - top[j]
				else:
					if j == c:
						wellDepth = top[j-1] - top[j]
					else:
						wellDepth = min(top[j-1],top[j+1]) - top[j]
				maxWellDepth 			= max(wellDepth,maxWellDepth)
				totalWellDepth 			+= maxWellDepth
				totalWeightedWellDepth 	+= (wellDepth * (wellDepth + 1)) / 2
		vals = self.cellOperations(top,field,vals,currentTurn)
		vals[self.MAX_WELL_DEPTH] = maxWellDepth
		vals[self.TOTAL_WELL_DEPTH] = totalWellDepth
		vals[self.WEIGHTED_WELL_DEPTH] = totalWeightedWellDepth
		vals[self.DIFF_AVG_HEIGHT] = total / s.COLS
		vals[self.SUM_ADJ_DIFF] = diffTotal
		#vals[self.SUM_ADJ_DIFF_SQUARED] = squaredDiffTotal
		vals[self.MAX_MIN_DIFF] = maxHeight - minHeight
		vals[self.MAX_HEIGHT] = maxHeight
		vals[self.COL_STD_DEV] = (totalHeightSquared - total * total / s.COLS) / (s.COLS - 1)
		#print(colTrans)
		return vals

	# Matrix update function. See above for descriptions.
	# 
	# @param s
	# @param features
	# @param futureFeatures
	def updateMatrices(self,s,features,futureFeatures):
		#preprocessing
		m = Matrix()
		self.mFeatures = m.arrayToCol(features,self.mFeatures)
		self.mFutureFeatures = m.arrayToRow(futureFeatures,self.mFutureFeatures)
		self.mRowFeatures = m.arrayToRow(features,self.mRowFeatures)
		self.mFutureFeatures = m.multiply(-1 * self.DISCOUNT,self.mFutureFeatures)
		self.mRowFeatures = m.sum(self.mRowFeatures,self.mFutureFeatures)
		self.changeToA = m.product(self.mFeatures,self.mRowFeatures,self.changeToA)
		self.A = m.sum(self.A,self.changeToA)
		self.mFeatures = m.multiply(features[self.DIFF_ROWS_COMPLETED],self.mFeatures)
		self.b = m.sum(self.b,self.mFeatures)

	# The computation of the weights can be separate from the updating of matrix A & b.
	# 
	# weights = A^(-1)b
	# 
	# The way I'm doing this in the back-end is running the Gauss-Jordan Elimination algo alongside the b matrix.
	# This saves computing inverse of A and then multiplying it with b.
	def computeWeights(self):
		if Matrix.premultiplyInverse(self.A,self.b,self.mWeight,self.tmpA) != None:
			self.weight = Matrix.colToArray(self.mWeight,self.weight)
			#printField(mWeight)

	def printField(self,field):
		for i in range(len(field)):
			for j in range(len(field[0])):
				print(" "+str(field[i][j]),end='')
			print()