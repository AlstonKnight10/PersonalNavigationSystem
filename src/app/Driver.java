package app;

import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

/**
 * The driver for the Personal Navigation System application.
 * 
 */
public class Driver 
{
  /**
   * The entry point for the application.
   * 
   * @param args The command-line arguments (IGNORED)
   * @throws InterruptedException if something goes wrong with Swing
   * @throws InvocationTargetException if something goes wrong with Swing
   */
  public static void main(final String[] args) 
      throws InterruptedException, InvocationTargetException 
  {
    SwingUtilities.invokeAndWait(new App());
  }
}
