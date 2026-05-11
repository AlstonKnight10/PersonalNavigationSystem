package feature;

import geography.GeographicShape;

/**
 * Class representing a street segment, which is a portion of a street defined by its endpoints
 * (tail and head). Each street segment has a length, a range of addresses (low and high), a
 * geographic shape, and a code that identifies the street it belongs to. The unique identifier for
 * this street segment is inherited from AbstractFeature and is typically the same as the segment's
 * code.
 */
public class StreetSegment extends AbstractFeature
{

  private double length;
  private int tail;
  private int head;
  private int highAddress;
  private int lowAddress;
  private GeographicShape geographicShape;
  private String code;

  /**
   * Constructs a StreetSegment with the specified parameters. The code is truncated to the first
   * two characters if it is longer than two characters. The geographic shape, address range, tail,
   * 
   * @param id
   *          the unique identifier for this street segment, typically the same as the code
   * @param code
   *          the code identifying the street this segment belongs to, truncated to 2 characters if
   *          longer
   * @param shape
   *          the geographic shape of this street segment
   * @param lowAddress
   *          the lowest address number on this street segment
   * @param highAddress
   *          the highest address number on this street segment
   * @param tail
   *          the identifier of the tail endpoint of this street segment
   * @param head
   *          the identifier of the head endpoint of this street segment
   * @param length
   *          the length of this street segment in meters
   */
  public StreetSegment(final String id, final String code, final GeographicShape shape,
      final int lowAddress, final int highAddress, final int tail, final int head,
      final double length)
  {
    super(id);
    this.code = (code != null && code.length() >= 2) ? code.substring(0, 2) : code;
    this.geographicShape = shape;
    this.lowAddress = lowAddress;
    this.highAddress = highAddress;
    this.tail = tail;
    this.head = head;
    this.length = length;
  }

  /**
   * Returns the length of this street segment in meters.
   *
   * @return the length of this street segment
   */
  public double getLength()
  {
    return length;
  }

  /**
   * Returns the identifier of the tail endpoint of this street segment.
   * 
   * @return the tail endpoint identifier
   */
  public int getTail()
  {
    return tail;
  }

  /**
   * Returns the identifier of the head endpoint of this street segment.
   * 
   * @return the head endpoint identifier
   */
  public int getHead()
  {
    return head;
  }

  /**
   * Returns the highest address number on this street segment.
   * 
   * @return the highest address number
   */
  public int getHighAddress()
  {
    return highAddress;
  }

  /**
   * Returns the lowest address number on this street segment.
   *
   * @return the lowest address number
   */
  public int getLowAddress()
  {
    return lowAddress;
  }

  @Override
  public GeographicShape getGeographicShape()
  {
    return geographicShape;
  }

  /**
   * Returns the code identifying the street this segment belongs to, which is truncated to 2
   * characters if it is longer than 2 characters.
   * 
   * @return the code of the street this segment belongs to
   */
  public String getCode()
  {
    return code;
  }
}
