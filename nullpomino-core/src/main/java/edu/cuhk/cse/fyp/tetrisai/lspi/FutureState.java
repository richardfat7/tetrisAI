package edu.cuhk.cse.fyp.tetrisai.lspi;

import java.util.Arrays;
import java.util.Random;

public class FutureState extends State{

	private int[][][] pBottom = State.getpBottom();
	private int[][] pHeight = State.getpHeight();
	private int[][][] pTop = State.getpTop();

	private int turn = 0;
	private int cleared = 0;

	private int[][] field = new int[ROWS][COLS];
	private int[] top = new int[COLS];

	protected int nextPiece;
	
	private boolean b2b = false;
	private int sent = 0;
	private boolean lastCleared = false;
	private int combo = 0;
	private int linesStack = 0;
	private int totalSent = 0;


	public int[][] getField() {
		return field;
	}

	public int[] getTop() {
		return top;
	}


	public int getNextPiece() {
		return nextPiece;
	}
	public void setNextPiece(int nextPiece){
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
	
	public int getLinesSent() {
		return sent;
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
	
	void resetToCurrentState(State s) {
		int[][] field = s.getField();
		this.nextPiece = s.getNextPiece();
		this.lost = s.hasLost();
		this.cleared = s.getRowsCleared();
		this.turn = s.getTurnNumber();
		this.b2b = s.getB2B();
		this.sent = s.getLinesSent();
		this.lastCleared = s.getLastCleared();
		this.combo = s.getCombo();
		this.linesStack = s.getLinesStack();
		this.totalSent = s.getTotalLinesSent();
		Arrays.fill(this.top,0);
		for(int i=field.length-1;i>=0;i--) {
			System.arraycopy(field[i], 0, this.field[i], 0, field[i].length);
			for(int j=0;j<top.length;j++) if(top[j]==0 && field[i][j]>0) top[j]=i+1;
		}
	}
	
	// add lines stack to field
	private boolean addLines() {
		// check if game end
		for (int i = ROWS-1; i > ROWS-1-linesStack; i--) {
			for (int j = 0; j < COLS; j++) {
				if (field[i][j] != 0) {
					lost = true;
					return false;
				}
			}
		}
		Random rand = new Random();
		int hole = rand.nextInt(COLS);
		for (int i = ROWS-1; i >= 0; i--) {
			for (int j = 0; j < COLS; j++) {
				if (i < linesStack) field[i][j] = (j==hole?0:turn);
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
		int height = top[slot] - pBottom[nextPiece][orient][0];
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
				field[h][i+slot] = turn;
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
		return valid;
	}

}


