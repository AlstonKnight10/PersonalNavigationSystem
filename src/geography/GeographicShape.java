package geography;

import java.awt.Shape;

/**
 * An interface representing a geographic shape, which has a unique identifier and an associated
 * geometric shape.
 */
public interface GeographicShape
{

  /**
   * Returns the unique identifier for this geographic shape.
   * 
   * @return the unique identifier for this geographic shape. Must not be null or empty.
   */
  String getID();

  /**
   * Returns the geometric shape associated with this geographic shape.
   * 
   * @return the geometric shape associated with this geographic shape. Must not be null.
   */
  Shape getShape();
}
