import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JPanel;

public class CustomPanel extends JPanel {

	private static final Color BACKGROUND_COLOR = Color.white;// new Color(40,
																// 40, 40);
	private static final Color IMMUNE_COLOR = new Color(63, 101, 135, 25);
	private static final Color INCUBATION_COLOR = new Color(63, 148, 170, 200);
	private static final Color WEAK_COLOR = new Color(241, 196, 122);
	private static final Color SICK_BASE_COLOR = new Color(218, 80, 96);
	private static final Color HEALTHY_COLOR = new Color(255, 255, 255, 100);

	public CustomPanel() {
		super();
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());

		for (Agent agent : MainEpidemy.population) {
			if (agent.isDead()) {
				// g.setColor(Color.gray);
				continue;
			} else if (agent.isSick()) {
				double sickDurationRatio = agent.getSickRatio();
				Color c = new Color(SICK_BASE_COLOR.getRed(), SICK_BASE_COLOR.getGreen(), SICK_BASE_COLOR.getBlue(),
						25 + (int) (sickDurationRatio * 230));
				g.setColor(c);
			} else if (agent.isIncubating()) {
				g.setColor(INCUBATION_COLOR);
			} else if (agent.isImmune()) {
				g.setColor(IMMUNE_COLOR);
			} else if (agent.isWeak()) {
				g.setColor(WEAK_COLOR);
			} else {
				g.setColor(HEALTHY_COLOR);
			}

			g.fillRect(agent.getX() - MainEpidemy.AGENT_SIZE / 2, agent.getY() - MainEpidemy.AGENT_SIZE / 2,
					MainEpidemy.AGENT_SIZE, MainEpidemy.AGENT_SIZE);
		}

		/**
		 * // Panel of scores int weaks = 0; int sicks = 0; int deads = 0;
		 * 
		 * for (Agent agent : MainEpidemy.population) { if (agent.isDead())
		 * deads++; else if (agent.isSick()) sicks++; else if (agent.isWeak())
		 * weaks++; }
		 * 
		 * g.setColor(new Color(255, 255, 255, 230)); g.fillRect(0, 0, 110, 80);
		 * g.setColor(Color.black); g.drawString("Sick: " +
		 * MainEpidemy.formatter.format(100 * sicks / ((double)
		 * MainEpidemy.POPULATION_SIZE)) + "%", 10, 20); g.drawString("Dead: " +
		 * MainEpidemy.formatter.format(100 * deads / (double) (deads + weaks))
		 * + "%", 10, 60);
		 **/
	}
}