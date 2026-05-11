package gps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

/**
 * A task that reads GPS data and notifies observers.
 */
public class GPSReaderTask extends SwingWorker<Void, String> implements GPSSubject
{
  private static final String PREFIX = "$";

  private BufferedReader in;
  private String[] sentences;
  private List<GPSObserver> observers;

  /**
   * Constructs a GPSReaderTask.
   *
   * @param in
   *          The input stream to read from
   * @param sentences
   *          The sentence types to process
   */
  public GPSReaderTask(final InputStream in, final String... sentences)
  {
    this.in = new BufferedReader(new InputStreamReader(in));
    this.sentences = sentences;
    this.observers = new ArrayList<GPSObserver>();
  }

  /**
   * Reads lines from the input stream.
   *
   * @return null
   * @throws Exception
   *           if an error occurs
   */
  @Override
  protected Void doInBackground() throws Exception
  {
    try
    {
      String line;
      while (!isCancelled() && (line = in.readLine()) != null)
      {
        publish(line);
      }
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (IOException e)
      {
        // Ignore close failure
      }
    }

    return null;
  }

  /**
   * Processes published lines.
   *
   * @param lines
   *          The lines to process
   */
  @Override
  protected void process(final List<String> lines)
  {
    for (String line : lines)
    {
      if (shouldProcess(line))
      {
        notifyGPSObservers(line);
      }
    }
  }

  /**
   * Determines whether a line should be processed.
   *
   * @param line
   *          The line to check
   * @return true if the line should be processed
   */
  private boolean shouldProcess(final String line)
  {
    if ((line == null) || (sentences == null) || (sentences.length == 0))
    {
      return false;
    }

    for (String sentence : sentences)
    {
      if (sentence == null)
      {
        continue;
      }

      if (line.startsWith(sentence) || line.startsWith(PREFIX + sentence))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Adds an observer.
   *
   * @param observer
   *          The observer to add
   */
  @Override
  public void addGPSObserver(final GPSObserver observer)
  {
    if ((observer != null) && !observers.contains(observer))
    {
      observers.add(observer);
    }
  }

  /**
   * Notifies all observers.
   *
   * @param sentence
   *          The GPS sentence
   */
  @Override
  public void notifyGPSObservers(final String sentence)
  {
    for (GPSObserver observer : observers)
    {
      observer.handleGPSData(sentence);
    }
  }

  /**
   * Removes an observer.
   *
   * @param observer
   *          The observer to remove
   */
  @Override
  public void removeGPSObserver(final GPSObserver observer)
  {
    observers.remove(observer);
  }
}
