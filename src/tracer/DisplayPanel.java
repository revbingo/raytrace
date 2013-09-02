/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer;

import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import javax.swing.JPanel;

/**
 * A JPanel based display for the raytraced image. Uses double buffering.
 * 
 * @author Hj. Malthaner
 */
public class DisplayPanel extends JPanel implements DisplayInterface {
	private BufferedImage bufferImg1;
	private BufferedImage bufferImg2;

	private BufferedImage showBuffer;

	private int bufferW;
	private int bufferH;

	private volatile boolean bufferOk;

	public DisplayPanel() {
		setDoubleBuffered(false);
		bufferOk = false;

		addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				createBackBuffer();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentShown(ComponentEvent e) {
				createBackBuffer();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}

	@Override
	public void paint(Graphics gr) {
		// ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		// RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		paintBuffered(gr);
	}

	private void paintBuffered(Graphics gr) {
		if (showBuffer == null) {
			createBackBuffer();
		}

		gr.drawImage(showBuffer, 0, 0, null);
	}

	private void createBackBuffer() {
		if (showBuffer == null || bufferW != getWidth() || bufferH != getHeight()) {
			bufferOk = false;

			bufferW = getWidth();
			bufferH = getHeight();

			bufferImg1 = new BufferedImage(Math.max(bufferW, 16), Math.max(bufferH, 16), BufferedImage.TYPE_INT_RGB);
			bufferImg2 = new BufferedImage(Math.max(bufferW, 16), Math.max(bufferH, 16), BufferedImage.TYPE_INT_RGB);

			showBuffer = bufferImg1;

			bufferOk = true;
		}
	}

	public void prepareFrame() {
		if (showBuffer == null || bufferW != getWidth() || bufferH != getHeight()) {
			createBackBuffer();
		}
	}

	@Override
	public void setLine(int y, int[] linepix) {
		if (bufferOk) {
			WritableRaster raster;

			if (showBuffer == bufferImg1) {
				raster = bufferImg2.getRaster();
			} else {
				raster = bufferImg1.getRaster();
			}

			DataBuffer buffer = raster.getDataBuffer();

			final int off = y * bufferW;

			int[] bufferData = ((DataBufferInt) buffer).getData();

			if (bufferData.length > off + linepix.length) {
				System.arraycopy(linepix, 0, bufferData, off, linepix.length);
			}
		}
	}

	public void switchBuffers() {
		if (showBuffer == bufferImg1) {
			showBuffer = bufferImg2;
		} else {
			showBuffer = bufferImg1;
		}
	}
}
