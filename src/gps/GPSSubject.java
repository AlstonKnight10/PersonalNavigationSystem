package gps;

/**
 * An object that manages GPS observers.
 */
public interface GPSSubject
{
  /**
   * Adds an observer.
   *
   * @param observer
   *          The observer to add
   */
  public abstract void addGPSObserver(GPSObserver observer);

  /**
   * Notifies all observers.
   *
   * @param sentence
   *          The GPS sentence
   */
  public abstract void notifyGPSObservers(String sentence);

  /**
   * Removes an observer.
   *
   * @param observer
   *          The observer to remove
   */
  public abstract void removeGPSObserver(GPSObserver observer);
}
