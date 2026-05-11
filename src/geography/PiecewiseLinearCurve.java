package geography;

import java.awt.Shape;
import java.awt.geom.Path2D;

/**
 * A class representing a piecewise linear curve, which is a sequence of connected line segments
 * defined by a series of points. This class extends AbstractGeographicShape and implements the
 * GeographicShape interface, allowing it to be used as a geographic shape with a unique identifier
 * and an associated geometric shape.
 */
public class PiecewiseLinearCurve extends AbstractGeographicShape
{
  protected Path2D.Double shape;

  /**
   * Constructs a new PiecewiseLinearCurve with the specified unique identifier. The underlying
   * shape is initialized as an empty Path2D.Double.
   * 
   * @param id
   */
  public PiecewiseLinearCurve(final String id)
  {
    super(id);
    this.shape = new Path2D.Double();
  }

  /**
   * Constructs a new PiecewiseLinearCurve with the specified unique identifier and an initial
   * shape.
   * 
   * @param id
   *          the identifier for this PiecewiseLinearCurve
   * @param shape
   *          the initial shape for this PiecewiseLinearCurve. If null, an empty Path2D.Double will
   *          be used.
   */
  public PiecewiseLinearCurve(final String id, final Path2D.Double shape)
  {
    super(id);
    this.shape = (shape != null) ? shape : new Path2D.Double();
  }

  /**
   * Adds a point to the piecewise linear curve. If this is the first point being added, it will
   * 
   * @param point
   *          the point to add to the curve, represented as a double array of length 2 (where
   *          point[0] is
   */
  public void add(final double[] point)
  {
    if (point == null || point.length != 2)
    {
      throw new IllegalArgumentException("Point must be of double[2]");
    }

    if (this.shape.getCurrentPoint() == null)
    {
      this.shape.moveTo(point[0], point[1]);
    }
    else
    {
      this.shape.lineTo(point[0], point[1]);
    }
  }

  /**
   * Appends another shape to this piecewise linear curve. The new shape will be connected to the
   * 
   * @param addition
   *          the shape to append to this piecewise linear curve. Must not be null.
   * @param connect
   *          if true, the new shape will be connected to the existing shape with a line segment. If
   *          false,
   */
  public void append(final Shape addition, final boolean connect)
  {
    if (addition == null)
    {
      throw new IllegalArgumentException("Addition shape cannot be null");
    }
    this.shape.append(addition, connect);
  }

  @Override
  public Shape getShape()
  {
    return this.shape;
  }
}
