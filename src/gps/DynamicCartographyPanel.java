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

  private static final double DOT_DIAMETER = 8.0;
  private static final double DOT_OFFSET = 4.0;
  private static final double VIEW_SIZE = 2.0;
  private static final double VIEW_OFFSET = 1.0;

  private GPGGASentence current;
  private double[] currentProjected;
  private MapMatcher mapMatcher;
  private MapProjection projection;
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
   *          The map matcher used to snap GPS positions to roads
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
   * @return The current segment, or null if none is available
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
      updateCurrentPosition(data);
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
      zoomStack.set(0, new Rectangle2D.Double(currentProjected[0] - VIEW_OFFSET,
          currentProjected[1] - VIEW_OFFSET, VIEW_SIZE, VIEW_SIZE));
    }

    super.paint(g);

    if (currentProjected != null)
    {
      paintCurrentPosition(g);
    }
  }

  /**
   * Updates the current projected position from a GPS sentence.
   *
   * @param data
   *          The GPS sentence
   */
  private void updateCurrentPosition(final String data)
  {
    try
    {
      current = GPGGASentence.parseGPGGA(data);
      double[] projected = projection
          .forward(new double[] {current.getLongitude(), current.getLatitude()});

      if (mapMatcher != null)
      {
        MapMatcher.MapMatchResult result = mapMatcher.match(projected);
        currentProjected = result.getPoint();
        currentSegment = result.getSegment();
      }
      else
      {
        currentProjected = projected;
        currentSegment = null;
      }

      repaint();
    }
    catch (IllegalArgumentException e)
    {
      currentProjected = null;
      currentSegment = null;
    }
  }

  /**
   * Paints the current projected GPS position.
   *
   * @param g
   *          The Graphics object
   */
  private void paintCurrentPosition(final Graphics g)
  {
    Graphics2D g2 = (Graphics2D) g;
    double[] screen = new double[2];

    displayTransform.getTransform(g2.getClipBounds(), zoomStack.getFirst())
        .transform(currentProjected, 0, screen, 0, 1);

    g2.setColor(Color.RED);
    g2.fill(new Ellipse2D.Double(screen[0] - DOT_OFFSET, screen[1] - DOT_OFFSET, DOT_DIAMETER,
        DOT_DIAMETER));
  }
}
