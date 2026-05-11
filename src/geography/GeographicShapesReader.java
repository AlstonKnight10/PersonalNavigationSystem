package geography;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.InputStreamReader;

import gui.CartographyDocument;

/**
 * A class responsible for reading geographic shapes from an input stream and converting them into a
 * CartographyDocument.
 */
public class GeographicShapesReader
{
  private static final String TYPE_PREFIX = "Type:";
  private static final String ID_PREFIX = "ID:";
  private static final String CODE_PREFIX = "Code:";
  private static final String END_MARKER = "END";

  private static final String COMMENT_TYPE = "Comment";
  private static final String POLYGON_TYPE = "Polygon";
  private static final String CURVE_TYPE = "PiecewiseLinearCurve";

  protected BufferedReader in;
  protected MapProjection proj;

  /**
   * Constructs a new GeographicShapesReader with the specified input stream and map projection.
   * 
   * @param is
   *          The input stream to read from. Must not be null.
   * @param proj
   *          The map projection to use for converting geographic coordinates. Must not be null.
   */
  public GeographicShapesReader(final InputStream is, final MapProjection proj)
  {
    if (is == null || proj == null)
    {
      throw new IllegalArgumentException("InputStream and MapProjection cannot be null");
    }
    this.in = new BufferedReader(new InputStreamReader(is));
    this.proj = proj;
  }

  /**
   * Reads geographic shapes from the input stream and constructs a CartographyDocument containing
   * those shapes.
   * 
   * @return A CartographyDocument containing the shapes read from the input stream. The document
   *         will include the bounding rectangle of all shapes.
   */
  public CartographyDocument<GeographicShape> read()
  {
    Map<String, GeographicShape> shapes = new LinkedHashMap<>();

    double minX = Double.POSITIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;

    String line;

    try
    {
      while ((line = in.readLine()) != null)
      {
        line = line.trim();
        if (line.isEmpty())
        {
          continue;
        }

        if (!line.startsWith(TYPE_PREFIX))
        {
          continue;
        }

        String type = parseType(line);
        String id = parseID(line);

        String key = id;
        if (COMMENT_TYPE.equalsIgnoreCase(type))
        {
          while ((line = in.readLine()) != null && !END_MARKER.equalsIgnoreCase(line.trim()))
          {
            line.trim();
          }
        }

        PiecewiseLinearCurve shape;

        if (POLYGON_TYPE.equalsIgnoreCase(type))
        {
          shape = new Polygon(key);
        }
        else if (CURVE_TYPE.equalsIgnoreCase(type))
        {
          shape = new PiecewiseLinearCurve(key);
        }
        else
        {
          continue;
        }

        while ((line = in.readLine()) != null)
        {
          line = line.trim();
          if (line.isEmpty())
          {
            continue;
          }

          if (END_MARKER.equalsIgnoreCase(line))
          {
            break;
          }

          String[] parts = line.split("\\s+");
          if (parts.length < 2)
          {
            continue;
          }

          double p1 = Double.parseDouble(parts[0]);
          double p2 = Double.parseDouble(parts[1]);

          double[] km = proj.forward(new double[] {p1, p2});
          shape.add(km);

          if (km[0] < minX)
          {
            minX = km[0];
          }
          if (km[1] < minY)
          {
            minY = km[1];
          }
          if (km[0] > maxX)
          {
            maxX = km[0];
          }
          if (km[1] > maxY)
          {
            maxY = km[1];
          }
        }

        shapes.put(key, shape);
      }
    }
    catch (NumberFormatException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    double w = maxX - minX;
    double h = maxY - minY;

    if (w < 0)
    {
      double t = minX;
      minX = maxX;
      maxX = t;
      w = -w;
    }
    if (h < 0)
    {
      double t = minY;
      minY = maxY;
      maxY = t;
      h = -h;
    }

    Rectangle2D.Double bounds;
    if (minX == Double.POSITIVE_INFINITY)
    {
      bounds = new Rectangle2D.Double(0, 0, 0, 0);
    }
    else
    {
      bounds = new Rectangle2D.Double(minX, minY, w, h);
    }

    return new CartographyDocument<>(shapes, bounds);
  }

  private static String parseType(final String header)
  {
    int typeStart = header.indexOf(TYPE_PREFIX) + TYPE_PREFIX.length();
    int idIndex = header.indexOf(ID_PREFIX);
    if (idIndex < 0)
    {
      return header.substring(typeStart).trim();
    }
    return header.substring(typeStart, idIndex).trim();
  }

  private static String parseID(final String header)
  {
    int idStart = header.indexOf(ID_PREFIX);
    if (idStart < 0)
    {
      return "";
    }

    String rest = header.substring(idStart + ID_PREFIX.length()).trim();
    int codeIdx = rest.indexOf(CODE_PREFIX);
    if (codeIdx >= 0)
    {
      rest = rest.substring(0, codeIdx).trim();
    }
    return rest.trim();
  }
}
