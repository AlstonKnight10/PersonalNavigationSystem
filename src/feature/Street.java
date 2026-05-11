package feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import geography.GeographicShape;
import geography.PiecewiseLinearCurve;

/**
 * Class representing a street, which consists of multiple street segments and has a name and
 * category.
 */
public class Street extends AbstractFeature
{
  private static final String NULL_SEGMENT_ERROR = "Segment cannot be null";
  private static final String SPACE = " ";

  private PiecewiseLinearCurve shape;
  @SuppressWarnings("unused")
  private String category;
  @SuppressWarnings("unused")
  private String code;
  @SuppressWarnings("unused")
  private String name;
  @SuppressWarnings("unused")
  private String prefix;
  @SuppressWarnings("unused")
  private String suffix;
  private List<StreetSegment> segments;

  /**
   * Constructs a Street with the given components and initializes an empty list of segments and an
   * empty shape. The code is used as the unique identifier for this street.
   * 
   * @param prefix
   *          optional prefix for the street name (e.g., "N", "S", "E", "W")
   * @param name
   *          the main name of the street (e.g., "Main")
   * @param category
   *          the category of the street (e.g., "St", "Ave", "Blvd")
   * @param suffix
   *          optional suffix for the street name (e.g., "NW", "SE")
   * @param code
   *          the unique identifier for this street
   */
  public Street(final String prefix, final String name, final String category, final String suffix,
      final String code)
  {
    super(code);
    this.prefix = prefix;
    this.name = name;
    this.category = category;
    this.suffix = suffix;
    this.code = code;
    this.shape = new PiecewiseLinearCurve(code);
    this.segments = new ArrayList<>();
  }

  /**
   * Adds a street segment to this street and updates the shape to include the segment's geographic
   * shape.
   * 
   * @param segment
   *          the StreetSegment to add. Must not be null.
   */
  public void addSegment(final StreetSegment segment)
  {
    if (segment == null)
    {
      throw new IllegalArgumentException(NULL_SEGMENT_ERROR);
    }

    this.segments.add(segment);
    this.shape.append(segment.getGeographicShape().getShape(), true);
  }

  /**
   * Creates a canonical name for a street by concatenating the prefix, name, category, and suffix,
   * trimming whitespace, and converting to lowercase. Components that are null or empty are
   * skipped.
   * 
   * @param prefix
   *          optional prefix for the street name (e.g., "N", "S", "E", "W")
   * @param name
   *          the main name of the street (e.g., "Main")
   * @param category
   *          the category of the street (e.g., "St", "Ave", "Blvd")
   * @param suffix
   *          optional suffix for the street name (e.g., "NW", "SE")
   * @return the canonical name for the street
   */
  public static String createCanonicalName(final String prefix, final String name,
      final String category, final String suffix)
  {
    StringBuilder sb = new StringBuilder();

    if (prefix != null && !prefix.trim().isEmpty())
    {
      sb.append(prefix.trim()).append(SPACE);
    }

    if (name != null && !name.trim().isEmpty())
    {
      sb.append(name.trim()).append(SPACE);
    }

    if (category != null && !category.trim().isEmpty())
    {
      sb.append(category.trim()).append(SPACE);
    }

    if (suffix != null && !suffix.trim().isEmpty())
    {
      sb.append(suffix.trim());
    }

    return sb.toString().trim().toLowerCase();
  }

  /**
   * Returns a list of street segments that contain the given address number within their address
   * range.
   * 
   * @param number
   *          the address number to search for
   * @return matching StreetSegments or empty list
   */
  public List<StreetSegment> getSegments(final int number)
  {
    List<StreetSegment> result = new ArrayList<>();

    for (StreetSegment segment : this.segments)
    {
      if (number >= segment.getLowAddress() && number <= segment.getHighAddress())
      {
        result.add(segment);
      }
    }

    return result;
  }

  /**
   * Returns an iterator over all street segments that belong to this street.
   * 
   * @return iterator of StreetSegments
   */
  public Iterator<StreetSegment> getSegments()
  {
    return this.segments.iterator();
  }

  @Override
  public GeographicShape getGeographicShape()
  {
    return this.shape;
  }

  /**
   * Returns the number of street segments that belong to this street.
   *
   * @return number of segments
   */
  public int getSize()
  {
    return this.segments.size();
  }
}
