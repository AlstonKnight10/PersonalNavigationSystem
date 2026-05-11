package geography;

import java.awt.Shape;

/**
 * An abstract base class for geographic shapes, providing common functionality for all shapes.
 */
public abstract class AbstractGeographicShape implements GeographicShape
{

  protected String id;

  /**
   * Constructs a new AbstractGeographicShape with the specified unique identifier.
   * 
   * @param id
   *          The unique identifier for this geographic shape. Must not be null or empty.
   */
  public AbstractGeographicShape(final String id)
  {
    this.id = id;
  }

  @Override
  public String getID()
  {
    return id;
  }

  @Override
  public abstract Shape getShape();
}
