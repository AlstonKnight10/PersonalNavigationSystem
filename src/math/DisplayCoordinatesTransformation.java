package math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * DisplayCoordinatesTransformation is a class that implements the ViewTransformation interface,
 * providing a method to calculate an AffineTransform that maps content coordinates to display
 * coordinates. The transformation includes a reflection across the horizontal axis and a uniform
 * scaling to fit the content within the display bounds while maintaining the aspect ratio. The
 * class also stores the last reflection and transformation applied, allowing for retrieval of these
 * transformations if needed.
 */
public class DisplayCoordinatesTransformation implements ViewTransformation
{
  protected AffineTransform lastReflection;
  protected AffineTransform lastTransform;

  /**
   * Simple constructor that initializes the last reflection and last transform to identity.
   */
  public DisplayCoordinatesTransformation()
  {
    this.lastReflection = new AffineTransform();
    this.lastTransform = new AffineTransform();
  }

  @Override
  public AffineTransform getLastReflection()
  {
    return new AffineTransform(this.lastReflection);
  }

  @Override
  public AffineTransform getLastTransform()
  {
    return new AffineTransform(this.lastTransform);
  }

  @Override
  public AffineTransform getTransform(final Rectangle2D displayBounds,
      final Rectangle2D contentBounds)
  {
    if (displayBounds == null || contentBounds == null)
    {
      throw new IllegalArgumentException("Display bounds and content bounds cannot be null");
    }

    double displayWidth = displayBounds.getWidth();
    double displayHeight = displayBounds.getHeight();
    double contentWidth = contentBounds.getWidth();
    double contentHeight = contentBounds.getHeight();

    double uniformScale = Math.min(displayWidth / contentWidth, displayHeight / contentHeight);

    double scaledContentWidth = contentWidth * uniformScale;
    double scaledContentHeight = contentHeight * uniformScale;

    double horizontalPadding = displayBounds.getX() + (displayWidth - scaledContentWidth) / 2.0;
    double verticalPadding = displayBounds.getY() + (displayHeight - scaledContentHeight) / 2.0;

    lastReflection = new AffineTransform();
    lastReflection.translate(0.0, contentBounds.getMinY() + contentBounds.getMaxY());
    lastReflection.scale(1.0, -1.0);

    double translateX = horizontalPadding - uniformScale * contentBounds.getMinX();
    double translateY = verticalPadding - uniformScale * contentBounds.getMinY();

    lastTransform = new AffineTransform();
    lastTransform.translate(translateX, translateY);
    lastTransform.scale(uniformScale, uniformScale);

    AffineTransform finalTransform = new AffineTransform(lastTransform);
    finalTransform.concatenate(lastReflection);

    return finalTransform;
  }
}
