package feature;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import geography.GeographicShape;
import gui.CartographyDocument;

/**
 * Reads street segment data from an input stream and constructs a CartographyDocument of
 * StreetSegments.
 */
public class StreetsReader
{
  protected BufferedReader in;
  protected CartographyDocument<GeographicShape> geographicShapes;

  /**
   * Creates a StreetsReader with the given input stream and geographic shapes document.
   * 
   * @param is
   *          input stream to read street segment data from (e.g., .str file)
   * @param shapes
   *          document containing geographic shapes indexed by ID (from .geo file)
   */
  public StreetsReader(final InputStream is, final CartographyDocument<GeographicShape> shapes)
  {
    if (is == null || shapes == null)
    {
      throw new IllegalArgumentException("InputStream and shapes cannot be null");
    }

    this.in = new BufferedReader(new InputStreamReader(is));
    this.geographicShapes = shapes;
  }

  /**
   * Reads street segment data from the input stream, constructs StreetSegment objects, and
   * organizes them into a CartographyDocument. StreetSegments are also associated with Street
   * objects in the provided map, which is modified in-place.
   * 
   * @param streets
   *          map of canonical street names to Street objects, which will be populated with segments
   * @return a CartographyDocument containing the StreetSegments and their bounding box
   * @throws IOException
   *           if an I/O error occurs while reading from the input stream
   */
  public CartographyDocument<StreetSegment> read(final Map<String, Street> streets)
      throws IOException
  {
    if (streets == null)
    {
      throw new IllegalArgumentException("streets cannot be null");
    }

    Map<String, StreetSegment> segments = new LinkedHashMap<>();

    double minX = Double.POSITIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;

    String record;
    while ((record = in.readLine()) != null)
    {
      if (record.trim().isEmpty())
      {
        continue;
      }

      String[] raw = record.split("\\t", -1);
      String[] fields = new String[11];

      for (int i = 0; i < 11; i++)
      {
        fields[i] = (i < raw.length) ? raw[i].trim() : "";
      }

      String tailText = fields[0];
      String headText = fields[1];
      String lengthText = fields[2];
      String rawCode = fields[3];
      String id = fields[4];
      String prefix = fields[5];
      String name = fields[6];
      String category = fields[7];
      String suffix = fields[8];
      String addr1Text = fields[9];
      String addr2Text = fields[10];

      // Only require the truly necessary fields
      if (tailText.isEmpty() || headText.isEmpty() || lengthText.isEmpty() || rawCode.isEmpty()
          || id.isEmpty())
      {
        continue;
      }

      int tail;
      int head;
      double length;

      try
      {
        tail = Integer.parseInt(tailText);
        head = Integer.parseInt(headText);
        length = Double.parseDouble(lengthText);
      }
      catch (NumberFormatException e)
      {
        continue;
      }

      int addr1 = parseAddress(addr1Text);
      int addr2 = parseAddress(addr2Text);

      int lowAddress;
      int highAddress;

      if (addr1 < 0 && addr2 < 0)
      {
        lowAddress = Integer.MIN_VALUE;
        highAddress = Integer.MIN_VALUE;
      }
      else
      {
        lowAddress = Math.min(addr1, addr2);
        highAddress = Math.max(addr1, addr2);
      }

      GeographicShape shape = geographicShapes.getElement(id);
      if (shape == null)
      {
        continue;
      }

      StreetSegment segment = new StreetSegment(id, rawCode, shape, lowAddress, highAddress, tail,
          head, length);

      segments.put(id, segment);

      String canonicalName = Street.createCanonicalName(prefix, name, category, suffix);
      Street street = streets.get(canonicalName);
      if (street == null)
      {
        street = new Street(prefix, name, category, suffix, canonicalName);
        streets.put(canonicalName, street);
      }
      street.addSegment(segment);

      Rectangle2D sb = shape.getShape().getBounds2D();
      minX = Math.min(minX, sb.getMinX());
      minY = Math.min(minY, sb.getMinY());
      maxX = Math.max(maxX, sb.getMaxX());
      maxY = Math.max(maxY, sb.getMaxY());
    }

    Rectangle2D.Double bounds;
    if (minX == Double.POSITIVE_INFINITY)
    {
      bounds = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
    }
    else
    {
      bounds = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    return new CartographyDocument<>(segments, bounds);
  }

  private int parseAddress(final String text)
  {
    if (text == null || text.isEmpty())
    {
      return -1;
    }

    try
    {
      return Integer.parseInt(text);
    }
    catch (NumberFormatException e)
    {
      return -1;
    }
  }
}
