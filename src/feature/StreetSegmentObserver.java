package feature;

import java.util.List;

/**
 * An observer of events related to StreetSegment objects.
 * 
 * @author Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public interface StreetSegmentObserver
{
  /**
   * Respond to handleStreetSegments() messages.
   * 
   * @param segmentIDs The IDs of the StreetSegment objects
   */
  public abstract void handleStreetSegments(List<String> segmentIDs);
}
