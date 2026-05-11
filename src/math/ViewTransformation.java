package math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Interface representing a view transformation, which provides methods to retrieve the last
 * reflection and transformation applied, as well as a method to calculate a new transformation
 * based on given display and content bounds. The getTransform method is responsible for calculating
 * an AffineTransform that maps content coordinates to display coordinates, including any necessary
 * reflections and scaling to fit the content within the display bounds while maintaining the aspect
 * ratio.
 */
public interface ViewTransformation
{

  /**
   * Returns the last reflection transformation applied. This method allows for
   * 
   * @return An AffineTransform representing the last reflection applied to the content
   */
  AffineTransform getLastReflection();

  /**
   * Returns the last transformation applied. This method allows for retrieval of the
   * 
   * @return An AffineTransform representing the last transformation applied to the content
   */
  AffineTransform getLastTransform();

  /**
   * Calculates and returns an AffineTransform that maps content coordinates to display coordinates,
   * including any necessary reflections and scaling to fit the content within the display bounds
   * while maintaining the aspect ratio. The method takes into account the dimensions of both the
   * display and the content, and applies a uniform scaling factor to ensure that the content fits
   * within the display bounds without distortion. If necessary, the method may also include a
   * reflection across the horizontal axis to ensure that the content is oriented correctly when
   * displayed.
   * 
   * @param displayBounds
   *          The bounding rectangle representing the display area where the content will
   * @param contentBounds
   *          The bounding rectangle representing the spatial extent of the content to be
   *          transformed. The method will calculate a transformation that fits the content within
   *          the display bounds while maintaining the aspect ratio, and may include a reflection
   *          across the horizontal axis if necessary.
   * @return An AffineTransform that maps content coordinates to display coordinates, including any
   *         necessary reflections and scaling to fit the content within the display bounds while
   *         maintaining the aspect ratio.
   */
  AffineTransform getTransform(Rectangle2D displayBounds, Rectangle2D contentBounds);
}
