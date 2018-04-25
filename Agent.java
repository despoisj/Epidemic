
public class Agent {

	private int x, y;
	private double speedX, speedY;

	// Time before becoming contagious
	private int incubation_steps;
	private int sick_steps;

	// States
	private boolean sick;
	private boolean incubating;
	private boolean immune;
	private boolean weak;
	private boolean dead;

	// Steps before being contagious and sick
	private final static int INCUBATION_DURATION = 10;
	private final static int SICK_DURATION = 130;

	private final static double MAX_SPEED = 1;
	private final static int STEP_SIZE = 1;

	/**
	 * Creates a new agent in a position where it's not colliding with anything
	 */
	public Agent() {
		boolean collision = false;
		do {
			// Generate new coordinates
			this.x = (int) (Math.random() * MainEpidemy.WIDTH);
			this.y = (int) (Math.random() * MainEpidemy.HEIGHT);

			// Assume no collision
			collision = false;
			// Look left and right
			for (int i = this.getX() - MainEpidemy.AGENT_SIZE / 2; i <= this.getX() + MainEpidemy.AGENT_SIZE / 2; i++) {
				if (i == this.getX())
					continue;
				if (i >= 0 && i < MainEpidemy.WIDTH && MainEpidemy.world[i][this.getY()] != null) {
					collision = true;
					break;
				}
			}
			// Continue searching
			if (!collision) {
				// Loop up and down
				for (int j = this.getY() - MainEpidemy.AGENT_SIZE / 2; j <= this.getY()
						+ MainEpidemy.AGENT_SIZE / 2; j++) {
					if (j == this.getY())
						continue;

					if (j >= 0 && j < MainEpidemy.HEIGHT && MainEpidemy.world[this.getX()][j] != null) {
						collision = true;
						break;
					}
				}
			}
		} while (collision);
	}

	/**
	 * This assumes the agent is still at the (oldX, oldY) position. Determines
	 * whether the move is valid
	 * 
	 * @param oldX
	 *            the old X coordinate
	 * @param oldY
	 *            the old Y coordinate
	 * @param newX
	 *            the new X coordinate
	 * @param newY
	 *            the new Y coordinate
	 * @return true if the move is valid
	 */
	private boolean isMoveValid(int oldX, int oldY, int newX, int newY) {
		// If out of bounds, it's invalid
		if (newX < 0 || newX >= MainEpidemy.WIDTH || newY < 0 || newY >= MainEpidemy.HEIGHT)
			return false;

		// Otherwise check collision with new coordinates horizontally and then
		// vertically
		for (int i = newX - MainEpidemy.AGENT_SIZE / 2; i <= newX + MainEpidemy.AGENT_SIZE / 2; i++) {
			if (i == oldX)
				continue;
			if (i >= 0 && i < MainEpidemy.WIDTH && MainEpidemy.world[i][newY] != null)
				return true;
		}
		// Continue searching
		for (int j = newY - MainEpidemy.AGENT_SIZE / 2; j <= newY + MainEpidemy.AGENT_SIZE / 2; j++) {
			if (j == oldY)
				continue;

			if (j >= 0 && j < MainEpidemy.HEIGHT && MainEpidemy.world[newX][j] != null)
				return true;
		}

		// All good
		return true;
	}

	/**
	 * Brownian movement with momentum
	 */
	private void move() {
		int oldX = this.x;
		int oldY = this.y;

		// Move with momentum
		this.x += (int) speedX;
		this.y += (int) speedY;

		// Reduce speed
		this.speedX = 0.8 * this.speedX;
		this.speedY = 0.8 * this.speedY;

		// Choose direction of movement
		boolean horizontal = Math.random() < 0.5;
		int stepDirection = Math.random() < 0.5 ? 1 : -1;

		// Random move
		if (horizontal) {
			// Go right or left
			this.x += stepDirection * STEP_SIZE;
			speedX += stepDirection;
		} else {
			// Go up or down
			this.y += stepDirection * STEP_SIZE;
			speedY += stepDirection;
		}

		// Limit the speed
		speedX = Math.max(Math.min(speedX, MAX_SPEED), -MAX_SPEED);
		speedY = Math.max(Math.min(speedY, MAX_SPEED), -MAX_SPEED);

		// Check if the move we have made is valid
		boolean moveValid = isMoveValid(oldX, oldY, this.getX(), this.getY());

		if (!moveValid) {
			// If not valid, stop agent speed and don't move
			this.x = oldX;
			this.y = oldY;
			this.speedX = 0;
			this.speedY = 0;
		} else {
			// Update world model
			MainEpidemy.world[oldX][oldY] = null;
			MainEpidemy.world[this.getX()][this.getY()] = this;
		}
	}

	/**
	 * Update the state of the agent and move the agent
	 */
	public void update() {
		updateState();
		move();
	}

	/**
	 * Update the state of the agent
	 */
	private void updateState() {
		if (this.incubating) {
			this.incubation_steps--;
			// Incubation -> Sick
			if (this.incubation_steps == 0) {
				this.incubating = false;
				this.setSick(true);
			}
		}

		if (this.sick) {
			sick_steps--;

			// Cured
			if (sick_steps <= 0) {
				this.sick = false;
				// 50% chance of being immune
				if (Math.random() < MainEpidemy.IMMUNE_GAIN_PROBABILITY)
					this.immune = true;
			}
		}
	}

	/**
	 * Transmits the disease to the agent. If weak, it dies. Otherwise it starts
	 * incubating.
	 */
	public void contaminate() {
		if (isWeak()) {
			// Dead don't collide
			this.dead = true;
			MainEpidemy.world[this.getX()][this.getY()] = null;
		} else {
			this.setIncubating(true);
		}
	}

	/************ Getters and setters **************/
	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public boolean isDead() {
		return this.dead;
	}

	public boolean isSick() {
		return this.sick;
	}

	public boolean isWeak() {
		return this.weak;
	}

	public boolean isImmune() {
		return this.immune;
	}

	public boolean isIncubating() {
		return this.incubating;
	}

	public void setIncubating(boolean b) {
		this.incubating = b;
		incubation_steps = INCUBATION_DURATION;
	}

	public void setSick(boolean b, boolean initialDuration) {
		this.sick = b;
		this.sick_steps = initialDuration ? (int) (Math.random() * SICK_DURATION) : SICK_DURATION;
	}

	public void setSick(boolean b) {
		this.sick = b;
		this.sick_steps = SICK_DURATION;
	}

	public void setImmune(boolean b) {
		this.immune = b;
	}

	public void setWeak(boolean b) {
		this.weak = b;
	}

	public double getSickRatio() {
		return ((double) sick_steps) / SICK_DURATION;
	}
}
