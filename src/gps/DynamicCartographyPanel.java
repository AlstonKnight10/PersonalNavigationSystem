package gps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

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
  private MapProjection projection;

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
    super(model, cartographer);
    this.projection = projection;
    this.current = null;
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
    if (current != null)
    {
      double[] km = getProjectedLocation();
      zoomStack.set(0, new Rectangle2D.Double(km[0] - 1.0, km[1] - 1.0, 2.0, 2.0));
    }

    super.paint(g);

    if (current == null)
    {
      return;
    }

    double[] km = getProjectedLocation();
    Graphics2D g2 = (Graphics2D) g;
    double[] screen = new double[2];

    displayTransform.getTransform(g2.getClipBounds(), zoomStack.getFirst()).transform(km, 0, screen,
        0, 1);

    g2.setColor(Color.RED);
    g2.fill(new Ellipse2D.Double(screen[0] - 4.0, screen[1] - 4.0, 8.0, 8.0));
  }

  /**
   * Projects the current GPS position into map coordinates.
   *
   * @return The projected position in kilometers
   */
  private double[] getProjectedLocation()
  {
    return projection.forward(new double[] {current.getLongitude(), current.getLatitude()});
  }
}
