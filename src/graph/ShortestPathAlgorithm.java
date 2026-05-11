package graph;

import java.util.List;
import java.util.Map;

import feature.StreetSegment;
import feature.StreetSegmentObserver;

/**
 * Defines behavior for shortest-path algorithms on a street network.
 */
public interface ShortestPathAlgorithm
{
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
  Map<String, StreetSegment> findPath(int origin, int destination, StreetNetwork net);

  /**
   * Adds an observer that will be notified about street-segment updates.
   *
   * @param observer
   *          the observer to add
   */
  void addStreetSegmentObserver(StreetSegmentObserver observer);

  /**
   * Removes a street-segment observer.
   *
   * @param observer
   *          the observer to remove
   */
  void removeStreetSegmentObserver(StreetSegmentObserver observer);

  /**
   * Notifies observers about the given segment IDs.
   *
   * @param segmentIDs
   *          the IDs of the segments to report
   */
  void notifyStreetSegmentObservers(List<String> segmentIDs);
}
