import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class MainEpidemy {

	// Panel parameters
	private static CustomPanel panel;
	public static final int HEIGHT = 600;
	public static final int WIDTH = 600;

	// World model
	public static ArrayList<Agent> population;
	public static Agent[][] world;

	// Initial conditions
	public static final int POPULATION_SIZE = 18000;
	public static final double WEAK_PROPORTION = 0.1;

	// Epidemy parameters
	public static final double CONTAGION_PROBABILITY = 0.7;
	private static final int CONTAGION_DISTANCE = 6;
	public static final double IMMUNE_GAIN_PROBABILITY = 0.98; // When the agent
																// is cured, it
																// can become
																// immune

	public static final int AGENT_SIZE = 4; // The agent spans half of this on
											// each side, plus it's central
											// position

	// Animation
	private static final int ANIMATION_DELAY = 2; // ms
	public static final NumberFormat formatter = new DecimalFormat("#0.0");

	public static void main(String[] args) throws InterruptedException {

		// Record death rate for various immune proportion
		// for (int IMMUNE_PROPORTION = 66; IMMUNE_PROPORTION < 100;
		// IMMUNE_PROPORTION += 1){
		// runSimulation((float)(IMMUNE_PROPORTION)/100, false);
		// }

		runSimulation(0.2, true);
	}

	/**
	 * Runs the simulation
	 * 
	 * @param immuneProportion
	 * @throws InterruptedException
	 */
	public static void runSimulation(double immuneProportion, boolean graphical) throws InterruptedException {
		System.out.println("Running simulation with " + (int) (100 * immuneProportion) + "% immune.");

		generatePopulation(immuneProportion);
		generateOutbreak();

		// Setup the panel and window
		if (graphical) {
			setupGraphics();
		}

		// Run until no one is longer sick
		boolean done = false;
		while (!done) {
			// Update the agents and spread the disease
			done = updatePopulation();

			// Repaint and enforce framerate
			if (graphical) {
				panel.repaint();
				Thread.sleep(ANIMATION_DELAY);
			}
		}

		// Compute death ratio
		double deathRatio = getDeathRatio();
		System.out.println("The outbreak killed " + formatter.format(deathRatio) + "% of weak people.");
	}

	/**
	 * Updates the agents, and computes the spreading of the disease
	 * 
	 * @return true if no agent is no longer sick or incubating, false otherwise
	 */
	private static boolean updatePopulation() {
		boolean done = true;
		for (Agent agent : population) {
			// Skip
			if (agent.isDead())
				continue;

			// Update state and position
			agent.update();

			// Contamination of the others
			if (agent.isSick()) {
				done = false;
				contaminateNeighbors(agent);
			} else if (agent.isIncubating()) {
				done = false;
			}
		}
		return done;
	}

	/**
	 * Spreads the disease to neighbors
	 * 
	 * @param agent
	 *            the sick agent
	 */
	private static void contaminateNeighbors(Agent agent) {
		// Look in square neighborhood of sick agent
		for (int tempX = agent.getX() - CONTAGION_DISTANCE / 2; tempX < agent.getX()
				+ CONTAGION_DISTANCE / 2; tempX++) {
			for (int tempY = agent.getY() - CONTAGION_DISTANCE / 2; tempY < agent.getY()
					+ CONTAGION_DISTANCE / 2; tempY++) {
				// Neighbor is agent, skip
				if (tempX == agent.getX() && tempY == agent.getY())
					continue;

				// Get neighbor if it exists
				Agent neighbor = tempX >= 0 && tempX < WIDTH && tempY >= 0 && tempY < HEIGHT ? world[tempX][tempY]
						: null;

				// Contaminate neighbor
				if (neighbor != null && !neighbor.isDead() && !neighbor.isImmune() && !neighbor.isSick()
						&& Math.random() < CONTAGION_PROBABILITY) {
					// Contaminate the neighbor. It will incubate before being
					// sick and contaminating others.
					neighbor.contaminate();
				}
			}
		}
	}

	/**
	 * Creates the world and population with correct immune proportion
	 * 
	 * @param immuneProportion
	 *            the approximate porportion of immune people in population
	 */
	private static void generatePopulation(double immuneProportion) {
		// Setup empty world and population
		population = new ArrayList<>();
		world = new Agent[WIDTH][HEIGHT];

		for (int i = 0; i < POPULATION_SIZE; i++) {
			// Create new agent in empty space
			Agent newAgent = new Agent();

			// Make some immune
			if (Math.random() < immuneProportion)
				newAgent.setImmune(true);
			// Make some weak if not immune
			else if (Math.random() < WEAK_PROPORTION)
				newAgent.setWeak(true);

			population.add(newAgent);
			world[newAgent.getX()][newAgent.getY()] = newAgent;
		}
	}

	/**
	 * Generates an outbreak of sick agents in the middle of the world
	 */
	private static void generateOutbreak() {
		// Initial outbreak, random duration of sickness
		for (Agent agent : population) {
			// In the middle of map
			if (Math.sqrt(Math.pow(agent.getX() - WIDTH / 2, 2) + Math.pow(agent.getY() - HEIGHT / 2, 2)) < 20) {
				// Make sick by default
				agent.setWeak(false);
				agent.setImmune(false);
				agent.setSick(true, true);
			}
		}
	}

	/**
	 * Setups the window and panel for dislay
	 * 
	 * @throws InterruptedException
	 *             with Thread.sleep
	 */
	private static void setupGraphics() throws InterruptedException {
		panel = new CustomPanel();
		JFrame mainWindow = new JFrame("Epidemy Simulation");
		mainWindow.setSize(HEIGHT, WIDTH);
		mainWindow.add(panel);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setVisible(true);

		panel.repaint();
		Thread.sleep(2000);
	}

	/**
	 * Computes the proportion of weak people who died
	 * 
	 * @return the ratio (0.0 -> 1.0) of dead
	 */
	private static double getDeathRatio() {
		int weaks = 0;
		int deads = 0;
		for (Agent agent : MainEpidemy.population) {
			if (agent.isDead())
				deads++;
			else if (agent.isWeak())
				weaks++;
		}
		return 100 * deads / (double) (deads + weaks);
	}

}
