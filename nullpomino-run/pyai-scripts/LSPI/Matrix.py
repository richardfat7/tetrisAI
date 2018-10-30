import copy

class Matrix:
	# preprocessing
	def arrayToCol(self,w,colW):
		for i in range(len(w)):
			colW[i][0] = w[i]
		return colW
	def arrayToRow(self,w,rowW):
		for i in range(len(w)):
			rowW[0][i] = w[i]
		return rowW
	def colToArray(self,colW,w):
		for i in range(len(w)):
			w[i] = colW[i][0]
		return w
	
	def copy(self,a,b):
		for i in range(len(a)):
			b[i] = copy.deepcopy(a[i])
		return b
	def findFirstNonZero(self,m,col):
		for i in range(len(m)):
			if m[i][col] != 0:
				return i
		return -1
	def swapRow(self,m,r1,r2):
		if r1 != r2:
			m[r1], m[r2] = m[r2], m[r1]
		return m
	def multiplyRow(self,m,r,factor):
		for i in range(len(m[r])):
			m[r][i] = factor * m[r][i]
		return m
	def addMultiple(self,m,r1,r2,factor):
		for i in range(len(m[r1])):
			m[r1][i] = m[r1][i] + factor * m[r2][i]
		return m
	
	def identity(self,r):
		for i in range(len(r)):
			for j in range(len(r[i])):
				if i == j:
					r[i][j] = 1
				else:
					r[i][j] = 0
		return r
	
	def main(self,args):
		A = [[1,2,3],[2,5,3],[1,0,8]]
		I = [[0 for _ in range(3)] for _ in range(3)]
		result = [[0 for _ in range(3)] for _ in range(3)]
		tmp = [[0 for _ in range(3)] for _ in range(3)]
		I = identity(I)
		result = self.premultiplyInverse(A,I,result,tmp)
		self.printField(result)
	
	def multiply(self,f,m):
		for i in range(len(m)):
			for j in range(len(m[0])):
				m[i][j] = m[i][j] * f
		return m
	def sum(self,a,b):
		for i in range(len(b)):
			for j in range(len(b[0])):
				a[i][j] = a[i][j] + b[i][j]
		return a


	def premultiplyInverse(self,l,r,result,tmp):
		l = self.copy(l,tmp)
		result = self.copy(r,result)
		for col in range(len(l)):
			nonZero = self.findFirstNonZero(l,col)
			if nonZero < 0:
				print(col," FULL EMPTY COLUMN")
				return (l, result, None)
			l = self.swapRow(l,col,nonZero)
			result = self.swapRow(result,col,nonZero)
			f = float(1) / l[col][col]
			l = self.multiplyRow(l,col,f)
			result = self.multiplyRow(result,col,f)
			for row in range(col+1,len(l)):
				if l[row][col] != 0:
					e = float(-1) * l[row][col]
					l = self.addMultiple(l,row,col,e)
					result = self.addMultiple(result,row,col,e)
		for col in reversed(range(len(l))):
			for row in reversed(range(col)):
				if l[row][col] != 0:
					e = float(-1) * l[row][col]
					l = self.addMultiple(l,row,col,e)
					result = self.addMultiple(result,row,col,e)
		return (l, result, result)

	def product(self,a,b,result):
		if len(a[0]) != len(b):
			return (result, None)
		elif (not (len(result) == len(a) and len(result[0]) == len(b[0]))):
			return (result, None)
		for i in range(len(result)):
			for j in range(len(result[0])):
				result[i][j] = 0
				for k in range(len(a[0])):
					result[i][j] += a[i][k] * b[k][j]
		return (result, result)
	
	def printField(self,field):
		for i in range(field):
			for j in range(len(field[0])):
				print(" "+str(field[i][j]),end='')
			print()