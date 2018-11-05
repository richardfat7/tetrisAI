package edu.cuhk.cse.fyp.tetrisai.lspi;

import java.text.DecimalFormat;




public class PlayerSkeleton {
	//implement this function to have a working system

	private FutureState fs;
	private FutureState fs2;
	private DecimalFormat formatter = new DecimalFormat("#00");

	protected double[] feature = null;
	private double[] past = null;
	protected BasisFunction bs = new BasisFunction();
	protected TwoPlayerBasisFunction bs2 = new TwoPlayerBasisFunction();
	protected boolean learns = false;

	int count = 0;

	//*************************************************************************************************************
	// single player
	public int pickMove(State s, int[][] legalMoves) {
		if(feature==null) feature = new double[BasisFunction.FEATURE_COUNT];
		if(fs==null) fs = new FutureState();
		if(fs2==null) fs2 = new FutureState();
		fs.resetToCurrentState(s); //set the state to the current to prepare to simulate next move.
		int maxMove = pickBestMove(s, legalMoves, feature);  //pick best move returns the highest scoring weighted features
		fs.makeMove(maxMove);//simulate next step
		if(learns) {
			if(past == null) past = new double[BasisFunction.FEATURE_COUNT]; 
			else {
				bs.updateMatrices(s, past, feature); //updates the matrices - adds the current "instance" into its training data
			}
		}
		double[] tmp = feature;		//swap the past and present around - reuse, reduce and recycle arrays.:)
		feature = past;
		past = tmp;
		count++;
		//if(count%1000==0) System.out.println(Arrays.toString(test)); 
		return maxMove;
	}
	BasisFunction getBasisFunctions() {
		return bs;
	}
	/**
	 * Given the current state, and the set of legal moves, decide the index of the move to be taken.
	 * Score is essentially does multiplies each feature with its corresponding weight, and sums it all up.
	 * 
	 * @param s Current state
	 * @param legalMoves Available moves to be made
	 * @param feature Temporary array for storing features
	 * @return
	 */
	public int pickBestMove(State s, int[][] legalMoves, double[] feature){
		double score;
		double maxScore = Double.NEGATIVE_INFINITY;

		int d = legalMoves.length;
		int init = (int)(Math.random()*d); //randomise the starting point to look at so that 0 is not always the first highest score
		int m = (init+1)%d;
		int maxMove = m ;
		while(m != init){
			fs.makeMove(m);
			if(!fs.hasLost()) {
				double[] f = bs.getFeatureArray(s, fs);
				score = score(f);
				if(maxScore < score) {
					maxScore = score;
					maxMove = m;
					if(learns) System.arraycopy(f,0,feature,0,f.length);
				}
			}
			fs.resetToCurrentState(s);
			m = (m+1)%d;
		}
		
		return maxMove;
	}
	private double score(double[] features){
		double total=0;
		for(int i=0;i<features.length;i++) {
			double score = features[i] * bs.weight[i];
			total+=score;
		}
		return total;
	}

	// ********************************************************************************************************************
	// double player
	public int pickMove(State s1, State s2, int[][] legalMoves) {
		if(feature==null) feature = new double[TwoPlayerBasisFunction.FEATURE_COUNT];
		if(fs==null) fs = new FutureState();
		if(fs2==null) fs2 = new FutureState();
		fs.resetToCurrentState(s1); //set the state to the current to prepare to simulate next move.
		fs2.resetToCurrentState(s2);
		int maxMove = pickBestMove(s1, s2, legalMoves, feature);  //pick best move returns the highest scoring weighted features
		fs.makeMove(maxMove);//simulate next step
		if(learns) {
			if(past == null) past = new double[TwoPlayerBasisFunction.FEATURE_COUNT]; 
			else {
				bs2.updateMatrices(s1, past, feature); //updates the matrices - adds the current "instance" into its training data
			}
		}
		double[] tmp = feature;		//swap the past and present around - reuse, reduce and recycle arrays.:)
		feature = past;
		past = tmp;
		count++;
		//if(count%1000==0) System.out.println(Arrays.toString(test)); 
		return maxMove;
	}
	TwoPlayerBasisFunction getTwoPlayerBasisFunctions() {
		return bs2;
	}
	/**
	 * Given the current state, and the set of legal moves, decide the index of the move to be taken.
	 * Score is essentially does multiplies each feature with its corresponding weight, and sums it all up.
	 * 
	 * @param s Current state
	 * @param legalMoves Available moves to be made
	 * @param feature Temporary array for storing features
	 * @return
	 */
	// double player
	public int pickBestMove(State s1, State s2, int[][] legalMoves, double[] feature){
		double score;
		double maxScore = Double.NEGATIVE_INFINITY;

		int d = legalMoves.length;
		int init = (int)(Math.random()*d); //randomise the starting point to look at so that 0 is not always the first highest score
		int m = (init+1)%d;
		int maxMove = m ;
		while(m != init){
			fs.makeMove(m);
			fs2.addLinesStack(fs.getLinesSent());
			if(!fs.hasLost()) {
				double[] f = bs2.getFeatureArray(s1, fs, s2, fs2);
				score = 0;
				for(int i=0;i<f.length;i++) score += f[i] * bs2.weight[i];
				if(maxScore < score) {
					maxScore = score;
					maxMove = m;
					if(learns) System.arraycopy(f,0,feature,0,f.length);
				}
			}
			fs.resetToCurrentState(s1);
			fs2.resetToCurrentState(s2);
			m = (m+1)%d;
		}
		
		return maxMove;
	}
	// *************************************************************************************************************


	public void printField(int[][] field){
		for(int i=0;i<field.length;i++) {
			for(int j=0;j<field[0].length;j++){
				System.out.print(" "+formatter.format(field[i][j]));
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		PlayerSkeleton p = new PlayerSkeleton();

		State s = new State();
		TFrame t = new TFrame(s);

		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(s.getLinesSent());
			//BasisFunction.computeWeights();
		}
		System.out.print(s.getTotalLinesSent());
		System.out.print("/");
		System.out.println(s.getTurnNumber());
	}

}
