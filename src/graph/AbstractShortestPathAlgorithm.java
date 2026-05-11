package graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import feature.StreetSegment;
import feature.StreetSegmentObserver;

/**
 * Provides shared observer support for shortest-path algorithms.
 */
public abstract class AbstractShortestPathAlgorithm implements ShortestPathAlgorithm
{
  private Collection<StreetSegmentObserver> observers;

  /**
   * Creates a shortest-path algorithm with no observers.
   */
  public AbstractShortestPathAlgorithm()
  {
    observers = new ArrayList<StreetSegmentObserver>();
  }

  /**
   * Finds a path between the given origin and destination intersections.
   *
   * @param origin
   *          the starting intersection ID
   * @param destination
   *          the ending intersection ID
   * @param net
   *          the street network to search
   * @return a map of segment IDs to street segments in the path
   */
  @Override
  public abstract Map<String, StreetSegment> findPath(int origin, int destination,
      StreetNetwork net);

  /**
   * Adds an observer that will be notified about street-segment updates.
   *
   * @param observer
   *          the observer to add
   */
  @Override
  public void addStreetSegmentObserver(final StreetSegmentObserver observer)
  {
    if (observer != null)
    {
      observers.add(observer);
    }
  }

  /**
   * Removes a street-segment observer.
   *
   * @param observer
   *          the observer to remove
   */
  @Override
  public void removeStreetSegmentObserver(final StreetSegmentObserver observer)
  {
    observers.remove(observer);
  }

  /**
   * Notifies observers about the given segment IDs.
   *
   * @param segmentIDs
   *          the IDs of the segments to report
   */
  @Override
  public void notifyStreetSegmentObservers(final List<String> segmentIDs)
  {
    for (StreetSegmentObserver observer : observers)
    {
      observer.handleStreetSegments(segmentIDs);
    }
  }
}
