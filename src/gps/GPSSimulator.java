package gps;
import java.io.*;
import java.util.*;

/**
 * A GPSSubject that simulates an actual GPS receiver.
 * It reads GPS data from a File.
 *
 * @author  Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public class GPSSimulator implements Runnable
{
  private boolean keepRunning, repeat;
  private InputStream is;
  private int delay;
  private List<String> records;
  private Thread controlThread;
  private PrintWriter out;

  /**
   * Explicit Value Constructor.
   *
   * @param fn The name of the File.
   */
  public GPSSimulator(final String fn)
  {
    keepRunning = true;
    repeat = true;
    delay = 100;
    controlThread = new Thread(this);


    try
    {
      BufferedReader in = new BufferedReader(new FileReader(fn));
      records = new ArrayList<String>();

      String line;
      while ((line = in.readLine()) != null) records.add(line);
      in.close();

      PipedOutputStream os = new PipedOutputStream();
      is = new PipedInputStream(os);
      out = new PrintWriter(os, true);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    controlThread.start();
  }

  /**
   * Get the InputStream for this GPSSimulator.
   * 
   * @return The InputStream
   */
  public InputStream getInputStream()
  {
    return is;
  }

  /**
   * The code to execute in the control thread.
   */
  @Override
  public void run()
  {
    Iterator<String> iterator;

    while (keepRunning)
    {
      iterator = records.iterator();
      while (iterator.hasNext())
      {
        String record = iterator.next();
        out.println(record);

        try 
        {
          Thread.sleep(delay);
        }
        catch (InterruptedException ie)
        {
          // Continue
        }
      }

      if (!repeat) keepRunning = false;
    }
  }
  
  /**
   * Set the delay (in milliseconds) between reports.
   * 
   * @param delay The delay
   */
  public void setDelay(final int delay)
  {
    this.delay = delay;
  }
}
