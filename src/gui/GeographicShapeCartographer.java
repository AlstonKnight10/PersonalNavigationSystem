package gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

import geography.GeographicShape;

/**
 * GeographicShapeCartographer is a concrete implementation of the Cartographer interface for
 * GeographicShape objects. It is responsible for painting geographic shapes onto a graphical
 * context, using a specified color for the shapes and a different color for highlights. The
 * cartographer iterates through the shapes in the provided CartographyDocument and applies the
 * appropriate transformations and styles when rendering them.
 */
public class GeographicShapeCartographer implements Cartographer<GeographicShape>
{
  public static final String ERROR = "Model, Graphics2D, and AffineTransform cannot be null";

  protected Color color;

  /**
   * Constructs a new GeographicShapeCartographer with the specified color for painting shapes.
   * 
   * @param color
   *          The color to use for painting the geographic shapes. Must not be null.
   */
  public GeographicShapeCartographer(final Color color)
  {
    if (color == null)
    {
      throw new IllegalArgumentException("Color cannot be null");
    }
    this.color = color;
  }

  @Override
  public void paintHighlights(final CartographyDocument<GeographicShape> model, final Graphics2D g2,
      final AffineTransform at)
  {
    if (model == null || g2 == null || at == null)
    {
      throw new IllegalArgumentException(ERROR);
    }

    Color highlightColor = new Color(255, 255, 0, 128);

    Color old = g2.getColor();
    g2.setColor(highlightColor);

    Iterator<GeographicShape> it = model.highlighted();
    while (it.hasNext())
    {
      GeographicShape shape = it.next();
      if (shape != null)
      {
        Shape s = shape.getShape();
        if (s != null)
        {
          g2.fill(at.createTransformedShape(s));
        }
      }
    }

    g2.setColor(old);
  }

  @Override
  public void paintShapes(final CartographyDocument<GeographicShape> model, final Graphics2D g2,
      final AffineTransform at)
  {
    if (model == null || g2 == null || at == null)
    {
      throw new IllegalArgumentException(ERROR);
    }

    g2.setColor(this.color);

    for (GeographicShape shape : model)
    {
      if (shape == null)
        continue;

      Shape s = shape.getShape();
      if (s == null)
        continue;

      g2.draw(at.createTransformedShape(s));
    }
  }
}
