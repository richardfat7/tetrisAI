import copy
import random

from LSPI.BasisFunction import BasisFunction
from LSPI.FutureState import FutureState

class PlayerSkeleton:
	# implement this function to have a working system
	def __init__(self):
		self.fs = None
		self.s2 = None
		self.bs = BasisFunction()
		self.feature = [0 for _ in range(self.bs.FEATURE_COUNT)]
		self.past = None
		self.learns = False
		self.count = 0

	def pickMove(self,s,legalMoves):
		if self.fs == None:
			self.fs = FutureState()
		if self.s2 == None:
			self.s2 = FutureState()
		self.fs.resetToCurrentState(s) # set the state to the current to prepare to simulate next move.
		maxMove = self.pickBestMove(s,legalMoves,self.feature)  # pick best move returns the highest scoring weighted features
		self.fs.makeMove(maxMove) # simulate next step
		if self.learns:
			if self.past == None:
				self.past = [0 for _ in range(self.bs.FEATURE_COUNT)]
			else:
				self.bs.updateMatrices(s,self.past,self.feature) # updates the matrices - adds the current "instance" into its training data
		tmp = copy.deepcopy(self.feature) # swap the past and present around - reuse, reduce and recycle arrays.:)
		self.feature = copy.deepcopy(self.past)
		self.past = copy.deepcopy(tmp)
		self.count += 1
		# if count % 1000 == 0:
		#	print(Arrays.toString(test)) 
		return maxMove

	def getBasisFunctions(self):
		return self.bs


	# Given the current state, and the set of legal moves, decide the index of the move to be taken.
	# Score is essentially does multiplies each feature with its corresponding weight, and sums it all up.
	# 
	# @param s Current state
	# @param legalMoves Available moves to be made
	# @param feature Temporary array for storing features

	def pickBestMove(self,s,legalMoves,feature):
		score = 0
		maxScore = float("-inf")
		d = len(legalMoves)
		init = random.randint(0,d-1) # randomise the starting point to look at so that 0 is not always the first highest score
		m = (init + 1) % d
		maxMove = m 
		while m != init:
			self.fs.makeMove(m)
			if not self.fs.hasLost():
				f = self.bs.getFeatureArray(s,self.fs,legalMoves[m])
				self.score = self.cal_score(f)
				if maxScore < self.score:
					maxScore = self.score
					maxMove = m
				if self.learns:
					self.feature = copy.deepcopy(f)
			self.fs.resetToCurrentState(s)
			m = (m + 1) % d
		return maxMove

	def cal_score(self,features):
		total = 0
		for i in range(len(features)):
			tmp_score = features[i] * self.bs.weight[i]
			total += tmp_score
		return total

	def printField(self,field):
		for i in range(len(field)):
			for j in range(len(field[i])):
				print('{:>10}'.format(field[i][j]),end='')
			print()