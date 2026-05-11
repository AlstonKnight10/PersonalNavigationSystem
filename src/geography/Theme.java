package geography;

import java.awt.Color;
import java.awt.Stroke;

/**
 * Class representing a visual theme for rendering geographic features, including color and stroke
 * style.
 */
public class Theme
{
  private Color color;
  private Stroke stroke;

  /**
   * Constructs a Theme with the specified color and stroke style.
   * 
   * @param color
   *          the color to use for rendering features with this theme
   * @param stroke
   *          the stroke style to use for rendering features with this theme
   */
  public Theme(final Color color, final Stroke stroke)
  {
    this.color = color;
    this.stroke = stroke;
  }

  /**
   * Returns the color associated with this theme.
   * 
   * @return the color for rendering features with this theme
   */
  public Color getColor()
  {
    return color;
  }

  /**
   * Returns the stroke style associated with this theme.
   * 
   * @return the stroke style for rendering features with this theme
   */
  public Stroke getStroke()
  {
    return stroke;
  }
}
