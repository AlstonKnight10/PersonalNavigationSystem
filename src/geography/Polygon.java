package geography;

import java.awt.Shape;

/**
 * Class for representing a polygon, which is a closed piecewise linear curve.
 */
public class Polygon extends PiecewiseLinearCurve
{

  // tracks whether the path has been closed
  private boolean closed;

  /**
   * Constructs a new Polygon with the specified unique identifier. The underlying
   * 
   * @param id
   *          unique identifier for this polygon. Must not be null or empty.
   */
  public Polygon(final String id)
  {
    super(id);
    this.closed = false;
  }

  @Override
  public void add(final double[] point)
  {
    // when adding a point the polygon is no longer guaranteed closed
    this.closed = false;
    super.add(point);
  }

  @Override
  public void append(final Shape addition, final boolean connect)
  {
    // appending may change openness
    this.closed = false;
    super.append(addition, connect);
  }

  @Override
  public Shape getShape()
  {
    // ensure the underlying Path2D is closed before returning
    if (!closed && this.shape != null)
    {
      // closePath is idempotent if already closed; mark closed after calling
      this.shape.closePath();
      this.closed = true;
    }
    return this.shape;
  }
}
