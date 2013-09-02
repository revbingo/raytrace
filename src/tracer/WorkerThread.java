/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tracer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A raytracing worker thread.
 * 
 * @author Hj. Malthaner
 */
public class WorkerThread extends Thread
{
    private final Tracer tracer;
    private int yStart;
    private int yEnd;
    
    private final TracerDataSet tracerData;;
    private volatile boolean go;

    public WorkerThread(Tracer tracer, int i)
    {
        this.tracer = tracer;
        tracerData = new TracerDataSet();
        
        setDaemon(true);
        setName("Worker #" + i);
    }
    
	public void startRendering(int yStart, int yEnd, int width)
    {
        this.yStart = yStart;
        this.yEnd = yEnd;

        tracerData.updateLinepix(width);
    }

    @Override
    public void run()
    {
        go = true;
        calculate();

        // System.err.println(getName() + " is quitting.");
    }

    private synchronized void calculate()
    {
        while(go)
        {
            try
            {
                // System.err.println(getName() + " waiting");
                wait();
                
                if(go)
                {
                    // System.err.println(getName() + " starting yStart=" + yStart + " yEnd=" + yEnd);
                    tracer.calculateScene(yStart, yEnd, tracerData);            
                    // System.err.println(getName() + " done, frame=" + frame);
                    tracer.workerDone();
                }
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    synchronized void quit()
    {
        go = false;
        notify();
    }
}
