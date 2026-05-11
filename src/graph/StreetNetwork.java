package graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import feature.Intersection;
import feature.Street;
import feature.StreetSegment;

/**
 * Represents a network of intersections used for shortest-path algorithms.
 */
public class StreetNetwork
{
  private List<Intersection> intersections;

  /**
   * Creates an empty street network.
   */
  public StreetNetwork()
  {
    intersections = new ArrayList<Intersection>();
  }

  /**
   * Adds an intersection at the given index in the network.
   *
   * @param index
   *          the index of the intersection
   * @param intersection
   *          the intersection to add
   */
  public void addIntersection(final int index, final Intersection intersection)
  {
    while (intersections.size() <= index)
    {
      intersections.add(null);
    }
    intersections.set(index, intersection);
  }

  /**
   * Returns the intersection at the given index.
   *
   * @param index
   *          the intersection index
   * @return the intersection at that index
   */
  public Intersection getIntersection(final int index)
  {
    return intersections.get(index);
  }

  /**
   * Returns the number of intersections in the network.
   *
   * @return the network size
   */
  public int size()
  {
    return intersections.size();
  }

  /**
   * Creates a street network from the given collection of streets.
   *
   * @param streets
   *          the streets used to build the network
   * @return the created street network
   */
  public static StreetNetwork createStreetNetwork(final Map<String, Street> streets)
  {
    StreetNetwork network = new StreetNetwork();

    if (streets == null)
    {
      throw new IllegalArgumentException("streets cannot be null");
    }

    for (Street street : streets.values())
    {
      Iterator<StreetSegment> iterator = street.getSegments();

      while (iterator.hasNext())
      {
        StreetSegment segment = iterator.next();
        int tail = segment.getTail();
        int head = segment.getHead();

        if (network.getIntersectionSafely(tail) == null)
        {
          network.addIntersection(tail, new Intersection());
        }

        if (network.getIntersectionSafely(head) == null)
        {
          network.addIntersection(head, new Intersection());
        }

        network.getIntersection(tail).addOutbound(segment);
        network.getIntersection(head).addInbound(segment);
      }
    }

    return network;
  }

  /**
   * Returns the intersection at the given index, or null if the index is outside the current
   * network bounds.
   *
   * @param index
   *          the intersection index
   * @return the intersection at that index, or null if none exists
   */
  private Intersection getIntersectionSafely(final int index)
  {
    if (index < 0 || index >= intersections.size())
    {
      return null;
    }
    return intersections.get(index);
  }
}
