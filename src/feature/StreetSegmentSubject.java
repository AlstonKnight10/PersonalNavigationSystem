package feature;

/**
 * An generator of events related to StreetSegment objects.
 * 
 * @author Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public interface StreetSegmentSubject
{
  /**
   * Add a StreetSegmentObserver.
   * 
   * @param observer The observer
   */
  public abstract void addStreetSegmentObserver(final StreetSegmentObserver observer);

  /**
   * Remove a StreetSegmentObserver.
   * 
   * @param observer The observer
   */
  public abstract void removeStreetSegmentObserver(final StreetSegmentObserver observer);
}
