package dk.itu.ai;

import java.util.List;

import mu.nu.nullpo.game.subsystem.mode.GameMode;
import mu.nu.nullpo.game.subsystem.mode.GradeMania3Mode;

import org.apache.log4j.Logger;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.integration.TargetFitnessFunction;
import com.anji.integration.TranscriberException;
import com.anji.util.Configurable;
import com.anji.util.Properties;

/**
 * This class implements the fitness function used when evolving artifical neural networks with ANJI.
 */
public class TGMFitnessFunction implements BulkFitnessFunction, Configurable {

	private static final long serialVersionUID = 1L;
	
	private final static String TRANSCRIBER_CLASS_KEY = "nullpominai.transcriber";

	private ActivatorTranscriber activatorFactory;

	private static Logger logger = Logger.getLogger( TargetFitnessFunction.class );
	
	/**
	 * The game seeds used for simulations during fitness calculation.
	 */
	private static final String[] FITNESS_TEST_SEEDS = {"15478945", "897494638", "4697358", "62c2fb46d0bb6b53", "5a9fbb07dd13c6fc", "-dd27fc1755162cd", "-3bd84a30fecd8f13"}; // I facerolled my numpad, sue me!  -Kas
	
	private static Object lock = new Object();
	
	@Override
	public void init(Properties props) throws Exception {
		activatorFactory = (ActivatorTranscriber) props.newObjectProperty( TRANSCRIBER_CLASS_KEY );
	}

	@Override
	public void evaluate(List subjects) {
		List<Chromosome> genotypes = (List<Chromosome>) subjects;
		
		for(Chromosome chromosome : genotypes){
			
			try {
				// Get average fitness over all test runs
				int fitness = 0;
				
				int total = 0;
				
				// Start all test games
				GameRunner[] gr = new GameRunner[FITNESS_TEST_SEEDS.length];
				Thread[] threads = new Thread[gr.length];
				for (int i = 0; i < FITNESS_TEST_SEEDS.length; i++) {

					gr[i] = new GameRunner(chromosome, FITNESS_TEST_SEEDS[i]);
					
					threads[i] = new Thread(gr[i]);
					threads[i].start();
				}
				
				// Wait for all threads to finish
				for (Thread thread : threads) {
					thread.join();
				}
				
				// Sum up scores from all runs
				for (GameRunner gameRunner : gr) {
					total += gameRunner.returnScore();
				}
				
//				System.out.println("ALL THREADS DONE. SCORE: " + total);
				
				fitness = total;
				
				
				chromosome.setFitnessValue(fitness);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("SOMETHING WENT WRONG WITH THE THREAD");
				chromosome.setFitnessValue(1);
			}
		}
		
	}

	@Override
	public int getMaxFitnessValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Utility class for multi-threading of simulations.
	 */
	class GameRunner implements Runnable {
		
		String seed;
		Chromosome chromosome;
		private volatile int score;
		
		GameRunner(Chromosome chromosmne, String seed) {
			this.seed = seed;
			this.chromosome = chromosmne;
		}
		
		public void run() {
//			System.out.println("Running with SEED: " + seed);
			
			
			Activator activator;
			try {
				activator = activatorFactory.newActivator( chromosome );
				GameMode simulationMode = new GradeMania3Mode();
				String simulationRulePath = "config\\rule\\Classic3.rul";
				
				Simulator simulation = new Simulator(simulationMode, simulationRulePath, new NeatAI(activator));
				simulation.setCustomSeed(seed);
				simulation.runSimulation();
				
				score = simulation.getGM3Grade();
//				score = simulation.getLevel();
				
//				System.out.println("Thread done: " + seed + "\tScore: " + score);
				
			} catch (TranscriberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public int returnScore() {
			return score;
		}
	}
	

}
