package edu.cuhk.cse.fyp.tetrisai.lspi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Trainer {
	private static int ROUNDS = 30;
	private static int SPIN_STEP_DELAY = 2500;
	private static Writer out = null;
	
	public static void main(String[] args) throws IOException {
		if(out == null) out = new PrintWriter(new File("weight.log"));
		PlayerSkeleton p1 = new PlayerSkeleton();
		p1.learns = true;
		PlayerSkeleton p2 = new PlayerSkeleton();
		//p2.learns = true;
		State s1, s2;
		TFrame frame1 = null;
		TFrame frame2 = null;
		TwoPlayerBasisFunction bf1 = p1.getTwoPlayerBasisFunctions();
		TwoPlayerBasisFunction bf2 = p2.getTwoPlayerBasisFunctions();
		String score = "";
		int consecutive_all_wins = 0;
		int cnt = 0;
		int tp1wins = 0;
		int tp2wins = 0;
		
		// keep on training!
		while(true) {
			int p1wins = 0;
			int draw = 0;
			int p2wins = 0;
			int prevLength = 0;
			
			System.out.println("Training for " + ROUNDS + " rounds...");
			for(int i=0;i<ROUNDS;i++){
				s1 = new State();
				s2 = new State();
				s1.doublePlayer = true;
				s2.doublePlayer = true;
				if(frame1==null) frame1 = new TFrame(s1,"Player 1");
				else frame1.bindState(s1);
				if(frame2==null) frame2 = new TFrame(s2,"Player 2");
				else frame2.bindState(s2);
				playGame(s1,p1,s2,p2,score,i);
				if(s1.hasLost()&&!s2.hasLost()) p2wins++;
				else if(!s1.hasLost()&&s2.hasLost()) p1wins++;
				else draw++;
				tp1wins += p1wins;
				tp2wins += p2wins;
				
				double sent = ((double)s1.getTotalLinesSent()/s1.getTurnNumber());
				score = Double.toString(sent);
				System.out.print("\r  ");
				System.out.print(score);
				for(int j=0;j<=prevLength-score.length();j++) System.out.print(' ');
				prevLength = score.length();
			}
			System.out.print("\r\nP1 wins: ");
			System.out.println(p1wins);
			System.out.print("P2 wins: ");
			System.out.println(p2wins);
			System.out.print("Draw: ");
			System.out.println(draw);
			System.out.print("P1 win rate: ");
			System.out.println((double)tp1wins / (tp1wins + tp2wins));
			if(p1wins > ROUNDS*8/10) consecutive_all_wins++;
			else consecutive_all_wins = 0;
			if(consecutive_all_wins == 3) {
				System.out.println("COPY WEIGHT");
				tp1wins = 0;
				tp2wins = 0;
				// copy weight
				for(int i = 0; i < bf2.weight.length; i++) {
					System.out.println(bf1.weight[i]+",");
					bf2.weight[i] = bf1.weight[i];
				}
				consecutive_all_wins = 0;
			}
			
			bf1.computeWeights();
			//for(int i=0;i<weights.length;i++) {
			//	weights[i] = 0.1 * weights[i] + 0.9 * defWeights[i];
			//	weights[i] = 0.001 * (weights[i]*(0.5 - Math.random()));
			//}
			
			cnt++;
			if(cnt == 100) {
				System.out.println("Write weight to log file");
				out.write("Weight:\n");
				for(int i = 0; i < bf1.weight.length; i++) {
					out.write(Double.toString(bf1.weight[i]));
					out.write('\n');
				}
				out.write('\n');
				out.flush();
				cnt = 0;
			}
		}
	}
	
	private static void doNewWeightActions(double[] weights) {
		for(int i=0;i<weights.length;i++) {
			if(i != 0) System.out.println(',');
			System.out.print('\t');
			System.out.print(weights[i]);
		}
		System.out.println();
	}
	
	private static char[] rotating = new char[] {'-','\\','|','/'};
	private static void playGame(State s1, PlayerSkeleton p1, State s2, PlayerSkeleton p2, String prevScore, int round) {
		int i = 0;
		int spin = 0;
		int[] bag = {0, 1, 2, 3, 4, 5, 6};
		int bag_index = 7;
		while(!s1.hasLost()&&!s2.hasLost()){
			if (bag_index < 0 || bag_index > 6) {
				Random rnd = ThreadLocalRandom.current();
				for (int k = bag.length - 1; k > 0; k--) {
					int index = rnd.nextInt(k + 1);
					// Simple swap
					int tmp = bag[index];
					bag[index] = bag[k];
					bag[k] = tmp;
				}
				bag_index = 0;
			}
			int nextPiece = bag[bag_index++];
			s1.setNextPiece(nextPiece);
			s2.setNextPiece(nextPiece);

			s1.makeMove(p1.pickMove(s1, s2, s1.legalMoves()));
			s2.addLinesStack(s1.getLinesSent());
			//s1.draw();
			//s1.drawNext(0,0);
			//s2.draw();
			//s2.drawNext(0,0);
			//String input1 = System.console().readLine();
			if (!s2.hasLost()) {
				s2.makeMove(p2.pickMove(s2, s1, s2.legalMoves()));
				s1.addLinesStack(s2.getLinesSent());
				//s1.draw();
				//s1.drawNext(0,0);
				//s2.draw();
				//s2.drawNext(0,0);
				//String input2 = System.console().readLine();
			}
			if(i == SPIN_STEP_DELAY) {
				System.out.print("\r");
				System.out.print(rotating[spin]);
				System.out.print(" Round ");
				System.out.print(round);
				System.out.print(": ");
				System.out.print(prevScore);
				spin = (spin+1)%rotating.length;
				i=0;
			}
			i++;
		}
		s1.draw();
		s1.drawNext(0,0);
		s2.draw();
		s2.drawNext(0,0);
	}

	private static void drawBoard(State s, TFrame t) {
		t.bindState(s);
		s.draw();
		s.drawNext(0,0);
	}
}
