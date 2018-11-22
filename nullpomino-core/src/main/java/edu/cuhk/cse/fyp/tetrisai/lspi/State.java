package edu.cuhk.cse.fyp.tetrisai.lspi;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class State {
	public static final int COLS = 10;
	public static final int ROWS = 23;
	public static final int N_PIECES = 7;

	

	public boolean lost = false;
	
	public boolean doublePlayer = false;
	

	
	public TLabel label;
	
	//current turn
	private int turn = 0;
	private int cleared = 0;
	
	//each square in the grid - int means empty - other values mean the turn it was placed
	private int[][] field = new int[ROWS][COLS];
	//top row+1 of each column
	//0 means empty
	private int[] top = new int[COLS];
	
	
	//number of next piece
	protected int nextPiece;
	// 7-bag generator
	protected int[] bag = {0, 1, 2, 3, 4, 5, 6};
	protected int bag_index = 7;
	
	private boolean b2b = false;
	private int sent = 0;
	private boolean lastCleared = false;
	private int combo = 0;
	private int linesStack = 0;
	private int totalSent = 0;
	
	//all legal moves - first index is piece type - then a list of 2-length arrays
	protected static int[][][] legalMoves = new int[N_PIECES][][];
	
	//indices for legalMoves
	public static final int ORIENT = 0;
	public static final int SLOT = 1;
	
	//possible orientations for a given piece type
	protected static int[] pOrients = {1,2,4,4,4,2,2};
	
	//the next several arrays define the piece vocabulary in detail
	//width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = {
			{2},
			{1,4},
			{2,3,2,3},
			{2,3,2,3},
			{2,3,2,3},
			{3,2},
			{3,2}
	};
	//height of the pieces [piece ID][orientation]
	private static int[][] pHeight = {
			{2},
			{4,1},
			{3,2,3,2},
			{3,2,3,2},
			{3,2,3,2},
			{2,3},
			{2,3}
	};
	private static int[][][] pBottom = {
		{{0,0}},
		{{0},{0,0,0,0}},
		{{0,0},{0,1,1},{2,0},{0,0,0}},
		{{0,0},{0,0,0},{0,2},{1,1,0}},
		{{0,1},{1,0,1},{1,0},{0,0,0}},
		{{0,0,1},{1,0}},
		{{1,0,0},{0,1}}
	};
	private static int[][][] pTop = {
		{{2,2}},
		{{4},{1,1,1,1}},
		{{3,1},{2,2,2},{3,3},{1,1,2}},
		{{1,3},{2,1,1},{3,3},{2,2,2}},
		{{3,2},{2,2,2},{2,3},{1,2,1}},
		{{1,2,2},{3,2}},
		{{2,2,1},{2,3}}
	};
	
	//initialize legalMoves
	{
		//for each piece type
		for(int i = 0; i < N_PIECES; i++) {
			//figure number of legal moves
			int n = 0;
			for(int j = 0; j < pOrients[i]; j++) {
				//number of locations in this orientation
				n += COLS+1-pWidth[i][j];
			}
			//allocate space
			legalMoves[i] = new int[n][2];
			//for each orientation
			n = 0;
			for(int j = 0; j < pOrients[i]; j++) {
				//for each slot
				for(int k = 0; k < COLS+1-pWidth[i][j];k++) {
					legalMoves[i][n][ORIENT] = j;
					legalMoves[i][n][SLOT] = k;
					n++;
				}
			}
		}
	
	}
	
	
	public int[][] getField() {
		return field;
	}

	public int[] getTop() {
		return top;
	}

	public static int[] getpOrients() {
		return pOrients;
	}
	
	public static int[][] getpWidth() {
		return pWidth;
	}

	public static int[][] getpHeight() {
		return pHeight;
	}

	public static int[][][] getpBottom() {
		return pBottom;
	}

	public static int[][][] getpTop() {
		return pTop;
	}


	public int getNextPiece() {
		return nextPiece;
	}
	
	public void setNextPiece(int nextPiece) {
		this.nextPiece = nextPiece;
	}
	
	public boolean hasLost() {
		return lost;
	}
	
	public int getRowsCleared() {
		return cleared;
	}
	
	public int getTurnNumber() {
		return turn;
	}
	
	public boolean getB2B() {
		return b2b;
	}
	
	public int getLinesSent() {
		return sent;
	}
	
	public int getCombo() {
		return combo;
	}
	
	public boolean getLastCleared() {
		return lastCleared;
	}
	
	public int getLinesStack() {
		return linesStack;
	}
	
	public int getTotalLinesSent() {
		return totalSent;
	}
	
	public void addLinesStack(int linesSent) {
		linesStack += linesSent;
	}
	
	public void addLineSent(int linesSent) {
		sent = linesSent;
		totalSent += linesSent;
	}
	
	public void addLineCleared(int linesCleared) {
		cleared += linesCleared;
	}
	
	//constructor
	public State() {
		nextPiece = randomPiece();
	}
	
	// random shuffle bag
	private void shuffleBag() {
		Random rnd = ThreadLocalRandom.current();
		for (int i = bag.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			int tmp = bag[index];
			bag[index] = bag[i];
			bag[i] = tmp;
		}
	}
	
	//random integer, returns 0-6
	private int randomPiece() {
		if (bag_index < 0 || bag_index > 6) {
			shuffleBag();
			bag_index = 0;
		}
		return bag[bag_index++];
	}
	
	// add lines stack to field
	private boolean addLines() {
		// check if game end
		for (int c = 0; c < COLS; c++) {
			if (top[c] + linesStack >= ROWS) {
				lost = true;
				return false;
			}
		}
		Random rand = new Random();
		int hole = rand.nextInt(COLS);
		for (int i = ROWS-1; i >= 0; i--) {
			for (int j = 0; j < COLS; j++) {
				if (i < linesStack) field[i][j] = (j==hole?0:1);//);
				else field[i][j] = field[i-linesStack][j];
			}
		}
		for (int i = 0; i < COLS; i++) top[i] += linesStack;
		linesStack = 0;
		return true;
	}
	
	// calculate lines sent
	private void calLinesSent(int rowsCleared) {
		sent = 0;
		if (rowsCleared == 0) {
			combo = 0;
			lastCleared = false;
		} else {
			// calculate combo
			if (lastCleared) {
				if (combo < 2) combo++;
				else if (combo < 4) combo += 2;
				else if (combo < 6) combo += 3;
				else combo += 4;
				sent += combo;
			} else {
				lastCleared = true;
			}

			// calculate lines sent
			if (rowsCleared == 4) {
				sent += 4;
				if (b2b) sent += 2;
				else b2b = true;
			} else {
				b2b = false;
				if (rowsCleared == 2) sent++;
				else if (rowsCleared == 3) sent += 2;
			}

			// perfect clear
			boolean perfectClear = true;
			for (int i = 0; i < ROWS; i++) {
				for (int j = 0; j < COLS; j++) {
					if (field[i][j] != 0) {
						perfectClear = false;
						break;
					}
				}
			}
			if (perfectClear) sent += 10;
			
			totalSent += sent;
		}
	}
	
	// calculate lines sent and return result
	protected int calLinesSentResult(int rowsCleared) {
		int sent = 0;
		int totalSent = 0;
		if (rowsCleared == 0) {
			combo = 0;
		} else {
			// calculate combo
			if (lastCleared) {
				if (combo < 2) combo++;
				else if (combo < 4) combo += 2;
				else if (combo < 6) combo += 3;
				else combo += 4;
				sent += combo;
			}

			// calculate lines sent
			if (rowsCleared == 4) {
				sent += 4;
				if (b2b) sent += 2;
				else b2b = true;
			} else {
				b2b = false;
				if (rowsCleared == 2) sent++;
				else if (rowsCleared == 3) sent += 2;
			}

			// perfect clear
			boolean perfectClear = true;
			for (int i = 0; i < ROWS; i++) {
				for (int j = 0; j < COLS; j++) {
					if (field[i][j] != 0) {
						perfectClear = false;
						break;
					}
				}
			}
			if (perfectClear) sent += 10;
			
			totalSent += sent;
		}
		return sent;
	}


	
	//gives legal moves for 
	public int[][] legalMoves() {
		return legalMoves[nextPiece];
	}
	
	//make a move based on the move index - its order in the legalMoves list
	public void makeMove(int move) {
		makeMove(legalMoves[nextPiece][move]);
	}
	
	//make a move based on an array of orient and slot
	public void makeMove(int[] move) {
		makeMove(move[ORIENT],move[SLOT]);
	}
	
	//returns false if you lose - true otherwise
	public boolean makeMove(int orient, int slot) {
		turn++;
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}
		
		//check if game ended
		if(height+pHeight[nextPiece][orient] >= 20) {
			lost = true;
			return false;
		}

		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = 1;//turn;
			}
		}
		
		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}
		
		int rowsCleared = 0;
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				cleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {
					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						if (i+1 == 23)
							field[i][c] = 0;
						else
							field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
		
		boolean valid = true;
		calLinesSent(rowsCleared);
		if(sent < linesStack) {
			linesStack -= sent;
			valid = addLines();
		}
		else {
			sent -= linesStack;
			linesStack = 0;
		}

		//pick a new piece
		if (doublePlayer == false)
			nextPiece = randomPiece();

		
		return valid;
	}
	
	public void draw() {
		label.clear();
		label.setPenRadius();
		//outline board
		label.line(0, 0, 0, ROWS+5);
		label.line(COLS, 0, COLS, ROWS+5);
		label.line(0, 0, COLS, 0);
		label.line(0, ROWS-3, COLS, ROWS-3);
		
		//show bricks
		for(int c = 0; c < COLS; c++) {
			for(int r = 0; r < top[c]; r++) {
				if(field[r][c] != 0) {
					drawBrick(c,r);
				}
			}
		}
		
		for(int i = 0; i < COLS; i++) {
			label.setPenColor(Color.red);
			label.line(i, top[i], i+1, top[i]);
			label.setPenColor();
		}
		
		label.show();
	}
	
	public static final Color brickCol = Color.gray;

	private void drawBrick(int c, int r) {
		label.filledRectangleLL(c, r, 1, 1, brickCol);
		label.rectangleLL(c, r, 1, 1);
	}
	
	public void drawNext(int slot, int orient) {
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			for(int j = pBottom[nextPiece][orient][i]; j <pTop[nextPiece][orient][i]; j++) {
				drawBrick(i+slot, j+ROWS+1);
			}
		}
		label.show();
	}
	
	//visualization
	//clears the area where the next piece is shown (top)
	public void clearNext() {
		label.filledRectangleLL(0, ROWS+.9, COLS, 4.2, TLabel.DEFAULT_CLEAR_COLOR);
		label.line(0, 0, 0, ROWS+5);
		label.line(COLS, 0, COLS, ROWS+5);
	}
	
}


