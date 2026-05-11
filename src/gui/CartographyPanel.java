package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.awt.geom.*;
import java.util.*;

import math.*;

/**
 * A GUI component that can be extended to draw maps of various kinds.
 * 
 * @param <T>
 *          The type of the data
 * 
 * @author Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public class CartographyPanel<T> extends JPanel implements MouseListener, MouseMotionListener
{
  private static final long serialVersionUID = 1L;

  protected DisplayCoordinatesTransformation displayTransform;
  protected LinkedList<Rectangle2D.Double> zoomStack;

  private CartographyDocument<T> model;
  private Cartographer<T> cartographer;
  private int[] rbMax, rbMin, rbStart, rbStop; // For the rubber-band-box

  /**
   * Explicit Value Constructor.
   * 
   * @param model
   *          The model to use
   * @param cartographer
   *          The cartographer to use
   */
  public CartographyPanel(final CartographyDocument<T> model, final Cartographer<T> cartographer)
  {
    displayTransform = new DisplayCoordinatesTransformation();

    this.model = model;
    zoomStack = new LinkedList<Rectangle2D.Double>();
    zoomStack.add(model.getBounds());

    this.cartographer = cartographer;

    rbStart = new int[2];
    rbStop = new int[2];
    rbMax = new int[2];
    rbMin = new int[2];

    addMouseListener(this);
    addMouseMotionListener(this);

    setDoubleBuffered(false);
  }

  /**
   * Handle a de-selection event (i.e., a zoom-out).
   */
  public void handleDeselect()
  {
    if (zoomStack.size() > 1)
    {
      zoomStack.removeFirst();
      repaint();
    }
  }

  /**
   * Handle a selection event (i.e., a zoom-in).
   *
   * @param min
   *          The upper-left of the selection (in screen coordinates)
   * @param max
   *          The lower-right of the selection (in screen coordinates)
   */
  protected void handleSelect(final double[] min, final double[] max)
  {
    double scale = displayTransform.getLastTransform().getScaleX();
    double translateX = displayTransform.getLastTransform().getTranslateX();
    double translateY = displayTransform.getLastTransform().getTranslateY();

    double x = (min[0] - translateX) / scale;
    double y = (min[1] - translateY) / scale;
    double width = (max[0] - translateX) / scale - x;
    double height = (max[1] - translateY) / scale - y;

    Rectangle2D.Double r = new Rectangle2D.Double(x, y, width, height);
    zoomStack.addFirst((Rectangle2D.Double) (displayTransform.getLastReflection()
        .createTransformedShape(r).getBounds2D()));
    repaint();
  }

  /**
   * Initialize the zoom stack.
   * 
   * @param bounds
   *          The initial bounds
   */
  private void initializeZoomStack(final Rectangle2D.Double bounds)
  {
    zoomStack = new LinkedList<Rectangle2D.Double>();
    zoomStack.add(bounds);
  }

  /**
   * Handle mouseClicked events.
   *
   * @param evt
   *          The MouseEvent
   */
  @Override
  public void mouseClicked(final MouseEvent evt)
  {
    if (evt.getClickCount() > 1)
    {
      handleDeselect();
    }
  }

  /**
   * Handle mouseDragged events.
   *
   * @param evt
   *          The MouseEvent
   */
  @Override
  public void mouseDragged(final MouseEvent evt)
  {
    int over = evt.getX();
    int down = evt.getY();
    Graphics g = getGraphics();

    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

    // Erase the old rubber-band-box
    g.setXORMode(getBackground());
    g.setColor(Color.green);
    g.drawRect(rbMin[0], rbMin[1], rbMax[0] - rbMin[0], rbMax[1] - rbMin[1]);

    // Calculate the coordinates of the new rubber-band-box
    rbStop[0] = over;
    rbStop[1] = down;
    rbMin[0] = Math.min(rbStart[0], rbStop[0]);
    rbMin[1] = Math.min(rbStart[1], rbStop[1]);
    rbMax[0] = Math.max(rbStart[0], rbStop[0]);
    rbMax[1] = Math.max(rbStart[1], rbStop[1]);

    // Draw the new rubber-band-box
    g.drawRect(rbMin[0], rbMin[1], rbMax[0] - rbMin[0], rbMax[1] - rbMin[1]);
    g.setPaintMode();
    g.dispose();
  }

  /**
   * Handle mouseEntered events.
   *
   * @param evt
   *          The MouseEvent
   */
  @Override
  public void mouseEntered(final MouseEvent evt)
  {
  }

  /**
   * Handle mouseExited events.
   *
   * @param evt
   *          The MouseEvent
   */
  @Override
  public void mouseExited(final MouseEvent evt)
  {
  }

  /**
   * Handle mouseMoved events.
   *
   * @param evt
   *          The MouseEvent
   */
  @Override
  public void mouseMoved(final MouseEvent evt)
  {
  }

  /**
   * Handle mousePressed events.
   *
   * @param evt
   *          The MouseEvent
   */
  @Override
  public void mousePressed(final MouseEvent evt)
  {
    int over = evt.getX();
    int down = evt.getY();

    rbStart[0] = over;
    rbStart[1] = down;
    rbStop[0] = over;
    rbStop[1] = down;
    rbMin[0] = over;
    rbMin[1] = down;
    rbMax[0] = over;
    rbMax[1] = down;
  }

  /**
   * Handle mouseReleased events.
   *
   * @param evt
   *          The MouseEvent
   */
  @Override
  public void mouseReleased(final MouseEvent evt)
  {
    Graphics g = getGraphics();
    Dimension d = getSize();
    g.setClip(0, 0, d.width, d.height);

    // Erase the old line
    g.setXORMode(getBackground());
    g.setColor(Color.green);
    g.drawRect(rbMin[0], rbMin[1], rbMax[0] - rbMin[0], rbMax[1] - rbMin[1]);

    // Determine the selected region
    double[] min = new double[2];
    min[0] = (double) (rbMin[0]);
    min[1] = (double) (rbMin[1] - 1);

    double[] max = new double[2];
    max[0] = (double) (rbMax[0]);
    max[1] = (double) (rbMax[1] - 1);

    // Reset the graphics environment
    g.setPaintMode();
    g.dispose();
    setCursor(Cursor.getDefaultCursor());

    // Notify any children of the selection
    if ((min[0] != max[0]) || (min[1] != max[1]))
    {
      handleSelect(min, max);
    }

  }

  /**
   * Render this component.
   * 
   * @param g
   *          The rendering engine to use
   */
  @Override
  public void paint(final Graphics g)
  {
    Graphics2D g2 = (Graphics2D) g;
    Rectangle screenBounds = g2.getClipBounds();
    g2.setColor(getBackground());
    g2.fill(screenBounds);
    g2.setColor(Color.BLACK);

    Rectangle2D.Double bounds = zoomStack.getFirst();

    AffineTransform at = displayTransform.getTransform(screenBounds, bounds);

    cartographer.paintShapes(model, g2, at);
    cartographer.paintHighlights(model, g2, at);
  }

  /**
   * Set the model.
   * 
   * @param model
   *          The model
   */
  public void setModel(final CartographyDocument<T> model)
  {
    this.model = model;
    initializeZoomStack(model.getBounds());
    repaint();
  }
}
