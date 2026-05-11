package dataprocessing;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import feature.Street;
import feature.StreetSegment;
import geography.GeographicShape;
import gui.CartographyDocument;

/**
 * Performs geocoding by mapping a street name and number to one or more coordinates along matching
 * street segments.
 */
public class Geocoder
{
  @SuppressWarnings("unused")
  private CartographyDocument<GeographicShape> shapes;
  @SuppressWarnings("unused")
  private CartographyDocument<StreetSegment> segments;
  private Map<String, Street> streets;

  /**
   * Creates a Geocoder using shape data, street segments, and a map of streets indexed by canonical
   * name.
   *
   * @param shapes
   *          geographic shapes (from .geo)
   * @param segments
   *          street segments (from .str)
   * @param streets
   *          map of canonical street names to Street objects
   */
  public Geocoder(final CartographyDocument<GeographicShape> shapes,
      final CartographyDocument<StreetSegment> segments, final Map<String, Street> streets)
  {
    if (shapes == null || segments == null || streets == null)
    {
      throw new IllegalArgumentException();
    }

    this.shapes = shapes;
    this.segments = segments;
    this.streets = streets;
  }

  /**
   * Returns coordinates for the given street name and number. Matching segment IDs are added to the
   * provided list.
   *
   * @param canonicalName
   *          normalized street name (lowercase, trimmed)
   * @param streetNumber
   *          address number to locate
   * @param segmentIDs
   *          output list of matching segment IDs
   * @return list of coordinates [x, y] for matches (may be empty)
   */
  public List<double[]> geocode(final String canonicalName, final int streetNumber,
      final List<String> segmentIDs)
  {
    if (canonicalName == null || segmentIDs == null)
    {
      throw new IllegalArgumentException();
    }

    // Store the final geocoded locations.
    List<double[]> locations = new ArrayList<>();

    // Look up the street by canonical name.
    Street street = streets.get(canonicalName);
    if (street == null)
    {
      return locations;
    }

    // Find all segments whose address range matches the number.
    List<StreetSegment> matches = street.getSegments(streetNumber);

    for (StreetSegment segment : matches)
    {
      // Record the matching segment ID.
      segmentIDs.add(segment.getID());

      // Get the address range for this segment.
      int low = segment.getLowAddress();
      int high = segment.getHighAddress();

      // Compute where the address falls along the segment.
      double ratio = (streetNumber - low) / (double) (high - low);

      // Read the shape for this segment.
      Shape shape = segment.getGeographicShape().getShape();
      List<double[]> points = new ArrayList<>();

      // Extract all line points from the shape.
      PathIterator it = shape.getPathIterator(null);
      double[] coords = new double[6];

      while (!it.isDone())
      {
        int type = it.currentSegment(coords);

        if (type != PathIterator.SEG_CLOSE)
        {
          points.add(new double[] {coords[0], coords[1]});
        }

        it.next();
      }

      // Compute the total length of the polyline.
      double totalLength = 0.0;
      for (int i = 1; i < points.size(); i++)
      {
        double[] a = points.get(i - 1);
        double[] b = points.get(i);

        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        totalLength += Math.sqrt(dx * dx + dy * dy);
      }

      // Find the target distance along the segment.
      double targetLength = ratio * totalLength;
      double currentLength = 0.0;

      // Default to the last point if interpolation does not stop earlier.
      double[] location = points.get(points.size() - 1);

      for (int i = 1; i < points.size(); i++)
      {
        double[] start = points.get(i - 1);
        double[] end = points.get(i);

        double dx = end[0] - start[0];
        double dy = end[1] - start[1];
        double segmentLength = Math.sqrt(dx * dx + dy * dy);

        // Stop once the target distance is on this small segment.
        if (currentLength + segmentLength >= targetLength)
        {
          double amountAlongSegment;

          amountAlongSegment = (targetLength - currentLength) / segmentLength;

          location = new double[] {start[0] + amountAlongSegment * (end[0] - start[0]),
              start[1] + amountAlongSegment * (end[1] - start[1])};
          break;
        }

        currentLength += segmentLength;
      }

      // Save the computed location.
      locations.add(location);
    }

    return locations;
  }
}
