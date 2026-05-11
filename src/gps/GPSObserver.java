package gps;

/**
 * An object that can receive GPS data.
 */
public interface GPSObserver
{
  /**
   * Handles GPS data.
   *
   * @param sentence
   *          The GPS sentence
   */
  public abstract void handleGPSData(String sentence);
}
