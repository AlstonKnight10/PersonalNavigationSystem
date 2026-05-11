package feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an intersection of streets, containing lists of inbound and outbound street
 * segments.
 */
public class Intersection
{
  private List<StreetSegment> inbound;
  private List<StreetSegment> outbound;

  /**
   * Constructs an Intersection with empty lists of inbound and outbound street segments.
   */
  public Intersection()
  {
    inbound = new ArrayList<>();
    outbound = new ArrayList<>();
  }

  /**
   * Adds a street segment to the list of inbound segments for this intersection.
   * 
   * @param segment the StreetSegment to add as inbound. Must not be null.
   */
  public void addInbound(final StreetSegment segment)
  {
    inbound.add(segment);
  }

  /**
   * Adds a street segment to the list of outbound segments for this intersection.
   * @param segment the StreetSegment to add as outbound. Must not be null.
   */
  public void addOutbound(final StreetSegment segment)
  {
    outbound.add(segment);
  }

  /**
   * Returns the list of inbound street segments for this intersection.
   * @return the list of inbound StreetSegments
   */
  public List<StreetSegment> getInbound()
  {
    return inbound;
  }

  /**
   * Returns the list of outbound street segments for this intersection.
   * @return the list of outbound StreetSegments
   */
  public List<StreetSegment> getOutbound()
  {
    return outbound;
  }
}
