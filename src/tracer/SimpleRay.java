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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
	private Scene scene;
	private View view;

	public JFrame getFrame() {
		return frame;
	}

	public SimpleRay() {
		frame = new JFrame(title);
		frame.setLayout(new BorderLayout());
		frame.setSize(800, 600);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case 38:
						view.zoomIn();
						break;
					case 40:
						view.zoomOut();
						break;
					case 37:
						view.rotateLeft();
						break;
					case 39:
						view.rotateRight();
						break;
				}
			}
		
		});

		displayPanel = new DisplayPanel();

		scene = new Scene();
		view = new View();
		tracer = new Tracer(displayPanel, scene, view);
		tracer.createWorkers(16);

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
						t1 = t;
						scene.animate();
						view.animate();

						tracer.nextFrame(gr);

						displayPanel.switchBuffers();

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
