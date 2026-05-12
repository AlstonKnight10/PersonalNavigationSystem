package gps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import feature.StreetSegment;
import geography.MapProjection;
import gui.Cartographer;
import gui.CartographyDocument;
import gui.CartographyPanel;

/**
 * A CartographyPanel that updates its display using GPS data.
 *
 * @param <T>
 *          The type of data being displayed
 */
public class DynamicCartographyPanel<T> extends CartographyPanel<T> implements GPSObserver
{
  private static final long serialVersionUID = 1L;

  private GPGGASentence current;
  private double[] currentProjected;
  private MapProjection projection;
  private MapMatcher mapMatcher;
  private StreetSegment currentSegment;

  /**
   * Constructs a new DynamicCartographyPanel.
   *
   * @param model
   *          The document to display
   * @param cartographer
   *          The cartographer used to draw the model
   * @param projection
   *          The map projection to use
   */
  public DynamicCartographyPanel(final CartographyDocument<T> model,
      final Cartographer<T> cartographer, final MapProjection projection)
  {
    this(model, cartographer, projection, null);
  }

  /**
   * Constructs a new DynamicCartographyPanel.
   *
   * @param model
   *          The document to display
   * @param cartographer
   *          The cartographer used to draw the model
   * @param projection
   *          The map projection to use
   * @param mapMatcher
   *          The map matcher used to snap GPS positions to roads (optional)
   */
  public DynamicCartographyPanel(final CartographyDocument<T> model,
      final Cartographer<T> cartographer, final MapProjection projection,
      final MapMatcher mapMatcher)
  {
    super(model, cartographer);
    this.projection = projection;
    this.mapMatcher = mapMatcher;
    this.current = null;
    this.currentProjected = null;
    this.currentSegment = null;
  }

  /**
   * Gets the current map-matched street segment.
   *
   * @return The current segment, or null if none is available yet
   */
  public StreetSegment getCurrentSegment()
  {
    return currentSegment;
  }

  /**
   * Sets the current map-matched street segment.
   *
   * @param currentSegment
   *          The current street segment
   */
  public void setCurrentSegment(final StreetSegment currentSegment)
  {
    this.currentSegment = currentSegment;
  }

  /**
   * Handles incoming GPS data.
   *
   * @param data
   *          The GPS sentence
   */
  @Override
  public void handleGPSData(final String data)
  {
    if ((data != null) && data.startsWith("$GPGGA"))
    {
      try
      {
        current = GPGGASentence.parseGPGGA(data);
        double[] projected = projection.forward(new double[] {current.getLongitude(),
            current.getLatitude()});
        if (mapMatcher != null)
        {
          currentProjected = mapMatcher.snap(projected);
        }
        else
        {
          currentProjected = projected;
        }
        repaint();
      }
      catch (IllegalArgumentException e)
      {
        // Ignore bad GPS data
      }
    }
  }

  /**
   * Paints the map and the current GPS position.
   *
   * @param g
   *          The Graphics object
   */
  @Override
  public void paint(final Graphics g)
  {
    if (currentProjected != null)
    {
      zoomStack.set(0, new Rectangle2D.Double(currentProjected[0] - 1.0, currentProjected[1] - 1.0,
          2.0, 2.0));
    }

    super.paint(g);

    if (currentProjected == null)
    {
      return;
    }

    Graphics2D g2 = (Graphics2D) g;
    double[] screen = new double[2];

    displayTransform.getTransform(g2.getClipBounds(), zoomStack.getFirst()).transform(currentProjected,
        0, screen, 0, 1);

    g2.setColor(Color.RED);
    g2.fill(new Ellipse2D.Double(screen[0] - 4.0, screen[1] - 4.0, 8.0, 8.0));
  }

}
