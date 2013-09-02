/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * A simple raytracer.
 * 
 * @author Hj. Malthaner
 */
public class SimpleRay {
	private static final String title = "HjM's SimpleRay Demo r6";

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		SimpleRay app;
		app = new SimpleRay();
		app.start();
	}

	private final JFrame frame;
	private final DisplayPanel displayPanel;
	private final Tracer tracer;

	public JFrame getFrame() {
		return frame;
	}

	public SimpleRay() {
		frame = new JFrame(title);
		frame.setLayout(new BorderLayout());
		// frame.setSize(1000, 600);
		frame.setSize(800, 600);
		// frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		displayPanel = new DisplayPanel();

		tracer = new Tracer(displayPanel);
		tracer.createWorkers(16);

		tracer.buildScene();

		frame.add(displayPanel);

		displayPanel.setIgnoreRepaint(true);
	}

	public void start() {
		frame.setVisible(true);

		Textures.init();

		boolean go = true;

		Graphics gr = displayPanel.getGraphics();

		int frameCount = 0;
		long t0 = System.nanoTime();
		long t1 = t0;

		while (go) {
			if (displayPanel.isVisible()) {
				final long t = System.nanoTime();
				final long deltaM = (t - t1);

				if (deltaM > 17000000) {
					// System.err.println("delta=" + deltaM);
					try {
						t1 = t;

						tracer.moveSphere();

						tracer.nextFrame(gr);

						displayPanel.switchBuffers();

					} catch (Exception ex) {
						Logger.getLogger(SimpleRay.class.getName()).log(Level.SEVERE, null, ex);
					}

					frameCount++;

					final long t2 = System.nanoTime();

					final int sleepTime = 15 - ((int) (t2 - t)) / 1000000;
					safeSleep(sleepTime);
				}

				final long deltaF = (t - t0);
				if (deltaF > 1000000000) {
					String s = title + ", FPS: " + frameCount;
					frame.setTitle(s);
					t0 = t;
					frameCount = 0;
				}

				Thread.yield();
			} else {
				safeSleep(200);
			}
		}
	}

	private void safeSleep(int sleepTime) {
		try {
			if (sleepTime > 1) {
				Thread.sleep(sleepTime);
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(SimpleRay.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
