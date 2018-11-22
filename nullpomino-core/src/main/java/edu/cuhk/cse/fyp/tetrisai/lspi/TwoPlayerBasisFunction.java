package edu.cuhk.cse.fyp.tetrisai.lspi;

public class TwoPlayerBasisFunction {

	// discount value, part of the LRQ algo.
	final private static double DISCOUNT = 0.96f;
	
	final private static double LAMBDA = 0.7f;

	/**
	 * Below are features being used for scoring.
	 * These are based on the _next_ state.
	 * This means these numbers represent the values that are reported if the attempted action is taken.
	 * The values starting with DIFF_ represent the differnece between the _next_ state and the current state
	 * 
	 * Notes:
	 * -	SUM_ADJ_DIFF seems to work better squared.
	 * -	A well is a whole starting at the top of the wall. Both sides of a well are blocks.
	 * 
	 */
	
	private static int count = 0;

	// player
	final private static int MAX_HEIGHT						= count++;
	final private static int COVERED_GAPS					= count++;	//(PD)holes in the tetris wall which are inaccessible from the top
	final private static int DIFF_LINES_SENT				= count++;	//(ESTR FYP)
	final private static int DIFF_ROWS_COMPLETED			= count++;	//(LSPI paper)
	final private static int MAX_MIN_DIFF					= count++;	//(Novel)
	final private static int MAX_WELL_DEPTH					= count++;	//(Novel)maximum well depth
	final private static int TOTAL_WELL_DEPTH				= count++;	//(PD)total depth of all wells on the tetris wall.
	final private static int TOTAL_BLOCKS					= count++;	//(CF)total number of blocks in the wall
	final private static int COL_TRANS						= count++;	//(PD)
	final private static int ROW_TRANS						= count++;	//(PD)
	final private static int DIFF_AVG_HEIGHT				= count++;	//(LSPI paper)
	final private static int SUM_ADJ_DIFF					= count++;	//(Handout)
	final private static int DIFF_COVERED_GAPS				= count++;	//(Novel)
	final private static int WEIGHTED_WELL_DEPTH			= count++;	//(CF)the deeper the well is, the heavier the "weightage".
//	final private static int LANDING_HEIGHT					= count++;	//(PD)
	final private static int COL_STD_DEV					= count++;	//(Novel)
//	final private static int ERODED_PIECE_CELLS				= count++;	//Intemediary step for WEIGHTED_ERODED_PIECE_CELLS
//	final private static int WEIGHTED_ERODED_PIECE_CELLS	= count++;	//(PD)
//	final private static int CENTER_DEV						= count++;	//(PD) priority value used to break tie in PD
//	final private static int SUM_ADJ_DIFF_SQUARED			= count++;	//(Novel)(sum of the difference between adjacent columns)^2
	final private static int CONSECUTIVE_HOLES				= count++;	//(ESTR FYP) leave a vertical empty column
	final private static int DIFF_CONSECUTIVE_HOLES			= count++;	//(ESTR FYP)
	final private static int LINES_STACK					= count++;	//(ESTR FYP) record line stack
//	final private static int DIFF_LINES_STACK				= count++;	//(ESTR FYP)
	final private static int single_player_features = count;

	// opponent
	final private static int OPPO_MAX_HEIGHT				= count++;
	final private static int OPPO_COVERED_GAPS				= count++;	//(PD)holes in the tetris wall which are inaccessible from the top
//	final private static int OPPO_DIFF_LINES_SENT			= count++;	//(ESTR FYP)
	//final private static int OPPO_DIFF_ROWS_COMPLETED		= count++;	//(LSPI paper)
	final private static int OPPO_MAX_MIN_DIFF				= count++;	//(Novel)
	final private static int OPPO_MAX_WELL_DEPTH			= count++;	//(Novel)maximum well depth
	final private static int OPPO_TOTAL_WELL_DEPTH			= count++;	//(PD)total depth of all wells on the tetris wall.
	final private static int OPPO_TOTAL_BLOCKS				= count++;	//(CF)total number of blocks in the wall
	final private static int OPPO_COL_TRANS					= count++;	//(PD)
	final private static int OPPO_ROW_TRANS					= count++;	//(PD)
//	final private static int OPPO_DIFF_AVG_HEIGHT			= count++;	//(LSPI paper)
	final private static int OPPO_SUM_ADJ_DIFF				= count++;	//(Handout)
//	final private static int OPPO_DIFF_COVERED_GAPS			= count++;	//(Novel)
	final private static int OPPO_WEIGHTED_WELL_DEPTH		= count++;	//(CF)the deeper the well is, the heavier the "weightage".
//	final private static int OPPO_LANDING_HEIGHT			= count++;	//(PD)
	final private static int OPPO_COL_STD_DEV				= count++;	//(Novel)
//	final private static int OPPO_ERODED_PIECE_CELLS		= count++;	//Intemediary step for WEIGHTED_ERODED_PIECE_CELLS
//	final private static int OPPO_WEIGHTED_ERODED_PIECE_CELLS= count++;	//(PD)
//	final private static int OPPO_CENTER_DEV				= count++;	//(PD) priority value used to break tie in PD
//	final private static int OPPO_SUM_ADJ_DIFF_SQUARED		= count++;	//(Novel)(sum of the difference between adjacent columns)^2
	final private static int OPPO_CONSECUTIVE_HOLES			= count++;	//(ESTR FYP) leave a vertical empty column
//	final private static int OPPO_DIFF_CONSECUTIVE_HOLES	= count++;	//(ESTR FYP)
	final private static int OPPO_LINES_STACK				= count++;	//(ESTR FYP) record line stack
//	final private static int OPPO_DIFF_LINES_STACK			= count++;	//(ESTR FYP)

	final private static int OPPO_DIE						= count++;	//(ESTR FYP)
	final public static int FEATURE_COUNT = count;


	/* A and b are important matrices in the LSPI algorithm. Every move the "player" makes, A and b are updated.
	 * 
	 * A += current_features * (current_features - DISCOUNT*future_features)
	 * 						 ^
	 * 						 -------This is a matrix multiplication with the transpose of features,
	 * 								 so for k features, a k x k matrix is produced.
	 * b += reward*current_features
	 * 
	 * reward = DIFF_ROWS_COMPLETED
	 */
	double[][] A = new double[FEATURE_COUNT][FEATURE_COUNT];
	double[][] b = new double[FEATURE_COUNT][1]; 
	double[][] z = new double[FEATURE_COUNT][1]; 
	double[] weight = new double[FEATURE_COUNT];
	
	{
		for (int i = 0; i < FEATURE_COUNT; i++) {
			A[i][i] = 0.00001;
		}
		
	}
	

	{
//		weight = new double[] {
//				-0.20622177896937224,
//				0.016205541492002477,
//				1.235381323112748,
//				-0.10319223307355785,
//				0.17926263371212325,
//				-0.1207512567880561,
//				0.0012024281039741216,
//				0.030968487941437833,
//				-0.048679464029570685,
//				-0.05071593215113885,
//				-0.21783239120802741,
//				0.006756927509485396,
//				-0.035859960335610785,
//				-0.01236896860152976,
//				-0.07801093262463116,
//				0.5121838888641603,
//				0.039860795790573694,
//				-0.47129131883711217,
//				-0.08611146660838641,
//				-0.011886868102488419,
//				0.07821468656690553,
//				0.007416087768147295,
//				0.0018930216988144151,
//				0.0035250046186331477,
//				0.026010339108017994,
//				0.01245467654216692,
//				0.00859661483721517,
//				-0.00850448693556203,
//				-0.02545573777396601,
//				-0.010207511160263676,
//				-5.2050798435802195,
//				0.008857241632929875,
//
//
//		};
	}
	
	private double[] features = new double[FEATURE_COUNT]; 
	private double[] past     = new double[FEATURE_COUNT];

	/**
	 * Function to get feature array for current state.
	 */
	public double[] getFeatureArray(State s1, FutureState fs1, State s2, FutureState fs2) {
		//simple features
		//features[ROWS_COMPLETED] = fs1.getRowsCleared();
		features[DIFF_LINES_SENT] = fs1.getLinesSent();
		features[DIFF_ROWS_COMPLETED] = fs1.getRowsCleared() - s1.getRowsCleared();
		features[LINES_STACK] = -s1.getLinesStack()-1;
		//features[DIFF_LINES_STACK] = features[LINES_STACK] - past[LINES_STACK];
		//compute height features
		int currentTurn1 = s1.getTurnNumber();
		int currentPiece1 = s1.getNextPiece();
		heightFeatures(s1, past, currentPiece1, currentTurn1);
		heightFeatures(fs1, features, currentPiece1, currentTurn1);
		features[DIFF_AVG_HEIGHT] 	=		features[DIFF_AVG_HEIGHT] - past[DIFF_AVG_HEIGHT];
		features[DIFF_COVERED_GAPS] =		features[COVERED_GAPS] - past[COVERED_GAPS];
		features[DIFF_CONSECUTIVE_HOLES] =	features[CONSECUTIVE_HOLES] - past[CONSECUTIVE_HOLES];

		//features[DIFF_MAX_HEIGHT] = 	features[MAX_HEIGHT]-past[MAX_HEIGHT];
		//features[DIFF_SUM_ADJ_DIFF] = 	features[SUM_ADJ_DIFF]-past[SUM_ADJ_DIFF];
		//features[DIFF_TOTAL_WELL_DEPTH] =	features[TOTAL_WELL_DEPTH]-past[TOTAL_WELL_DEPTH];

		//features[CENTER_DEV] = Math.abs(move[State.SLOT] - State.COLS/2.0);
		//features[WEIGHTED_ERODED_PIECE_CELLS] = (fs.getRowsCleared()- s.getRowsCleared())*features[ERODED_PIECE_CELLS];
		//features[LANDING_HEIGHT] = s.getTop()[move[State.SLOT]];
		
		//simple features
		//features[OPPO_DIFF_LINES_SENT] = fs2.getLinesSent();
		features[OPPO_LINES_STACK] = -s2.getLinesStack()-1;
		//features[OPPO_DIFF_LINES_STACK] = features[OPPO_LINES_STACK] - past[OPPO_LINES_STACK];
		
		//compute height features
		int currentTurn2 = s2.getTurnNumber();
		int currentPiece2 = s2.getNextPiece();
		oppoHeightFeatures(s2, past, currentPiece2, currentTurn2);
		oppoHeightFeatures(fs2, features, currentPiece2, currentTurn2);
		//features[OPPO_DIFF_AVG_HEIGHT] 	=		features[OPPO_DIFF_AVG_HEIGHT] - past[OPPO_DIFF_AVG_HEIGHT];
		//features[OPPO_DIFF_COVERED_GAPS] =		features[OPPO_COVERED_GAPS] - past[OPPO_COVERED_GAPS];
		//features[OPPO_DIFF_CONSECUTIVE_HOLES] =	features[OPPO_CONSECUTIVE_HOLES] - past[OPPO_CONSECUTIVE_HOLES];

		//features[OPPO_DIFF_MAX_HEIGHT] = 	features[OPPO_MAX_HEIGHT]-past[OPPO_MAX_HEIGHT];
		//features[OPPO_DIFF_SUM_ADJ_DIFF] = 	features[OPPO_SUM_ADJ_DIFF]-past[OPPO_SUM_ADJ_DIFF];
		//features[OPPO_DIFF_TOTAL_WELL_DEPTH] =	features[OPPO_TOTAL_WELL_DEPTH]-past[OPPO_TOTAL_WELL_DEPTH];

		//features[OPPO_CENTER_DEV] = Math.abs(move[State.SLOT] - State.COLS/2.0);
		//features[OPPO_WEIGHTED_ERODED_PIECE_CELLS] = (fs2.getRowsCleared()- s2.getRowsCleared())*features[OPPO_ERODED_PIECE_CELLS];
		//features[OPPO_LANDING_HEIGHT] = s2.getTop()[move[State.SLOT]];

		features[OPPO_DIE] = (Math.pow(features[OPPO_MAX_HEIGHT] + s2.getLinesStack(), 2) - Math.pow(features[OPPO_MAX_HEIGHT], 2))
				           - (Math.pow(features[MAX_HEIGHT] + s1.getLinesStack(), 2) - Math.pow(features[MAX_HEIGHT], 2));
		return features;
	}

	public void cellOperations(int[]top,int[][] field,double[] vals,int turnNo){
		int rowTrans = 0;
		int colTrans = 0;
		int coveredGaps = 0;
		int totalBlocks = 0;
		int currentPieceCells = 0;
		for(int i=0;i<State.ROWS-1;i++){
			if(field[i][0]==0) rowTrans++;
			if(field[i][State.COLS-1]==0)rowTrans++;
			for(int j=0;j<State.COLS;j++){
				if(j>0 &&((field[i][j]==0)!=(field[i][j-1]==0)))	rowTrans++;
				if((field[i][j]==0)!=(field[i+1][j]==0))			colTrans++;
				if(i<top[j] && field[i][j]==0) 						coveredGaps++; 
				if(field[i][j]!=0) 									totalBlocks++;
				if(field[i][j]==turnNo)								currentPieceCells++;
			}
		}
//		vals[ERODED_PIECE_CELLS] = 4 - currentPieceCells;
		vals[COL_TRANS] = colTrans;
		vals[ROW_TRANS] = rowTrans;
		vals[COVERED_GAPS] = coveredGaps;
		vals[TOTAL_BLOCKS] = totalBlocks;
	}

	public void oppoCellOperations(int[]top,int[][] field,double[] vals,int turnNo){
		int rowTrans = 0;
		int colTrans = 0;
		int coveredGaps = 0;
		int totalBlocks = 0;
		int currentPieceCells = 0;
		for(int i=0;i<State.ROWS-1;i++){
			if(field[i][0]==0) rowTrans++;
			if(field[i][State.COLS-1]==0)rowTrans++;
			for(int j=0;j<State.COLS;j++){
				if(j>0 &&((field[i][j]==0)!=(field[i][j-1]==0)))	rowTrans++;
				if((field[i][j]==0)!=(field[i+1][j]==0))			colTrans++;
				if(i<top[j] && field[i][j]==0) 						coveredGaps++; 
				if(field[i][j]!=0) 									totalBlocks++;
				if(field[i][j]==turnNo)								currentPieceCells++;
			}
		}
//		vals[ERODED_PIECE_CELLS] = 4 - currentPieceCells;
		vals[OPPO_COL_TRANS] = colTrans;
		vals[OPPO_ROW_TRANS] = rowTrans;
		vals[OPPO_COVERED_GAPS] = coveredGaps;
		vals[OPPO_TOTAL_BLOCKS] = totalBlocks;
	}

	/** 
	 * Shared method for obtaining features about the height of the tetris wall.
	 * Its pretty messy, but I didn't want to split extracting these features into
	 * separate methods, or it might end up doing redundant stuff.
	 * 
	 * @param s
	 * @param vals
	 */
	public void heightFeatures(State s,double[] vals,int currentPiece,int currentTurn) {
		int[][] field = s.getField();
		int[] top = s.getTop();
		int c = State.COLS - 1;
		double maxWellDepth = 0,
		totalWellDepth = 0,
		totalWeightedWellDepth = 0,
		maxHeight = 0,
		minHeight = Integer.MAX_VALUE,
		total = 0,
		totalHeightSquared = 0,
		diffTotal = 0,
		squaredDiffTotal = 0,
		conHoles = 0;
		for(int j=0;j<State.COLS;j++){ //by column
			total += top[j];
			totalHeightSquared += Math.pow(top[j], 2);	
			diffTotal += (j>0)?Math.abs(top[j-1]-top[j]):0;
			squaredDiffTotal += (j>0)?Math.abs(Math.pow(top[j-1],2)-Math.pow(top[j],2)):0;
			maxHeight = Math.max(maxHeight,top[j]);
			minHeight = Math.min(minHeight,top[j]);

			if((j==0||top[j-1]>top[j]) && (j==c||top[j+1]>top[j])) {
				int wellDepth = (j==0)?top[j+1]-top[j]: (
						(j==c)?top[j-1]-top[j]:
							Math.min(top[j-1],top[j+1])-top[j]
				);
				maxWellDepth 			= Math.max(wellDepth, maxWellDepth);
				totalWellDepth 			+= maxWellDepth;
				totalWeightedWellDepth 	+= (wellDepth*(wellDepth+1))/2;
			}

			int consecutiveHoles = 0;
			for(int i=top[j];i<State.ROWS-1;i++){
				boolean full = true;
				for(int k=0;k<State.COLS;k++){
					if(k!=j&&field[i][k]==0){
						full = false;
						break;
					}
				}
				if(full==true) consecutiveHoles++;
			}
//			if(consecutiveHoles>1&&consecutiveHoles<4) conHoles += consecutiveHoles;
//			else conHoles -= consecutiveHoles;

			if (consecutiveHoles > 0)
				conHoles = 4 - Math.abs(4 - consecutiveHoles);
		}
		cellOperations(top,field,vals,currentTurn);
		vals[MAX_WELL_DEPTH] = maxWellDepth;
		vals[TOTAL_WELL_DEPTH] = totalWellDepth;
		vals[WEIGHTED_WELL_DEPTH] = totalWeightedWellDepth;
		vals[DIFF_AVG_HEIGHT] = ((double)total)/State.COLS;
		vals[SUM_ADJ_DIFF] = diffTotal;
		//vals[SUM_ADJ_DIFF_SQUARED] = squaredDiffTotal;
		vals[MAX_MIN_DIFF] = maxHeight-minHeight;
		vals[MAX_HEIGHT] = maxHeight;
		vals[COL_STD_DEV] = (totalHeightSquared - total*((double)total)/State.COLS)/(double)(State.COLS-1);
		vals[CONSECUTIVE_HOLES] = conHoles;
	}

	public void oppoHeightFeatures(State s,double[] vals,int currentPiece,int currentTurn) {
		int[][] field = s.getField();
		int[] top = s.getTop();
		int c = State.COLS - 1;
		double maxWellDepth = 0,
		totalWellDepth = 0,
		totalWeightedWellDepth = 0,
		maxHeight = 0,
		minHeight = Integer.MAX_VALUE,
		total = 0,
		totalHeightSquared = 0,
		diffTotal = 0,
		squaredDiffTotal = 0,
		conHoles = 0;
		for(int j=0;j<State.COLS;j++){ //by column
			total += top[j];
			totalHeightSquared += Math.pow(top[j], 2);	
			diffTotal += (j>0)?Math.abs(top[j-1]-top[j]):0;
			squaredDiffTotal += (j>0)?Math.abs(Math.pow(top[j-1],2)-Math.pow(top[j],2)):0;
			maxHeight = Math.max(maxHeight,top[j]);
			minHeight = Math.min(minHeight,top[j]);

			if((j==0||top[j-1]>top[j]) && (j==c||top[j+1]>top[j])) {
				int wellDepth = (j==0)?top[j+1]-top[j]: (
						(j==c)?top[j-1]-top[j]:
							Math.min(top[j-1],top[j+1])-top[j]
				);
				maxWellDepth 			= Math.max(wellDepth, maxWellDepth);
				totalWellDepth 			+= maxWellDepth;
				totalWeightedWellDepth 	+= (wellDepth*(wellDepth+1))/2;
			}

			int consecutiveHoles = 0;
			for(int i=top[j];i<State.ROWS-1;i++){
				boolean full = true;
				for(int k=0;k<State.COLS;k++){
					if(k!=j&&field[i][k]==0){
						full = false;
						break;
					}
				}
				if(full==true) consecutiveHoles++;
			}
			if(consecutiveHoles>1&&consecutiveHoles<4) conHoles += consecutiveHoles;
			else conHoles -= consecutiveHoles;
		}
		oppoCellOperations(top,field,vals,currentTurn);
		vals[OPPO_MAX_WELL_DEPTH] = maxWellDepth;
		vals[OPPO_TOTAL_WELL_DEPTH] = totalWellDepth;
		vals[OPPO_WEIGHTED_WELL_DEPTH] = totalWeightedWellDepth;
		//vals[OPPO_DIFF_AVG_HEIGHT] = ((double)total)/State.COLS;
		vals[OPPO_SUM_ADJ_DIFF] = diffTotal;
		//vals[SUM_ADJ_DIFF_SQUARED] = squaredDiffTotal;
		vals[OPPO_MAX_MIN_DIFF] = maxHeight-minHeight;
		vals[OPPO_MAX_HEIGHT] = maxHeight;
		vals[OPPO_COL_STD_DEV] = (totalHeightSquared - total*((double)total)/State.COLS)/(double)(State.COLS-1);
		vals[OPPO_CONSECUTIVE_HOLES] = conHoles;
	}

	private double[][] tmpA = new double[FEATURE_COUNT][FEATURE_COUNT];
	private double[][] mWeight = new double[FEATURE_COUNT][1];
	private double[][] mFeatures = new double[FEATURE_COUNT][1];
	private double[][] mFutureFeatures = new double[1][FEATURE_COUNT];
	private double[][] mRowFeatures = new double[1][FEATURE_COUNT];
	private double[][] changeToA = new double[FEATURE_COUNT][FEATURE_COUNT];
	private double[][] zz = new double[FEATURE_COUNT][1];

	/**
	 * Matrix update function. See above for descriptions.
	 * 
	 * ( I know there are many arrays all over the place,
	 * 	 but I'm trying to _NOT_ create any new arrays to reduce compute time and memory
	 * 	 that's why you see all the System.arraycopy everywhere )
	 * @param s
	 * @param features
	 * @param futureFeatures
	 */
	public void updateMatrices(State s,double[] features,double[] futureFeatures) {
		//preprocessing
		
		Matrix.arrayToCol(features, mFeatures);
		

		Matrix.multiply(DISCOUNT*LAMBDA, z);
		Matrix.sum(z, mFeatures);
		Matrix.copy(z, zz);
		
		Matrix.arrayToRow(futureFeatures, mFutureFeatures);
		Matrix.arrayToRow(features, mRowFeatures);
		Matrix.multiply(-1*DISCOUNT, mFutureFeatures);
		Matrix.sum(mRowFeatures, mFutureFeatures);
		Matrix.product(z, mRowFeatures,changeToA);
		Matrix.sum(A,changeToA);
//		Matrix.multiply(features[OPPO_DIFF_DIE], mFeatures);
		Matrix.multiply(features[DIFF_LINES_SENT], zz);
//		Matrix.multiply(features[DIFF_ROWS_COMPLETED], mFeatures);
		Matrix.sum(b, zz);
	}

	/**
	 * The computation of the weights can be separate from the updating of matrix A & b.
	 * 
	 * weights = A^(-1)b
	 * 
	 * The way I'm doing this in the back-end is running the Gauss-Jordan Elimination algo alongside the b matrix.
	 * This saves computing inverse of A and then multiplying it with b.
	 */
	public void computeWeights() {
		if(Matrix.premultiplyInverse(A, b, mWeight, tmpA)==null) return;;
		Matrix.colToArray(mWeight, weight);
		//printField(mWeight);
	}


	public void printField(double[][] field){
		for(int i=0;i<field.length;i++) {
			for(int j=0;j<field[0].length;j++){
				System.out.print(" "+field[i][j]);
			}
			System.out.println();
		}
	}

}