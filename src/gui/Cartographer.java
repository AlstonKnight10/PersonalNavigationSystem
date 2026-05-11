package gui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * An interface representing a cartographer, responsible for painting geographic shapes onto a
 * graphical context. The cartographer takes a CartographyDocument containing shapes and paints them
 * onto a Graphics2D context, applying an AffineTransform to position and scale the shapes
 * appropriately. The cartographer can paint both highlights and regular shapes, allowing for
 * different visual styles to be applied to different types of shapes or different states of the
 * same shape.
 * 
 * @param <T>
 *          shapes onto a graphical context. The cartographer provides methods for painting both
 *          highlights and regular shapes, allowing for different visual styles to be applied to
 *          different types of shapes or different states of the same shape.
 */
public interface Cartographer<T>
{

  /**
   * Paints the highlighted shapes from the given CartographyDocument onto the provided Graphics2D
   * context, applying the specified AffineTransform to position and scale the shapes appropriately.
   * 
   * @param model
   *          the CartographyDocument containing the shapes to be painted. Must not be null.
   * @param g2
   *          the Graphics2D context onto which the shapes will be painted. Must not be null.
   * @param at
   *          the AffineTransform to be applied to the shapes when painting. Must not be null.
   */
  void paintHighlights(CartographyDocument<T> model, Graphics2D g2, AffineTransform at);

  /**
   * Paints the regular shapes from the given CartographyDocument onto the provided Graphics2D
   * context, applying the specified AffineTransform to position and scale the shapes appropriately.
   * 
   * @param model
   *          the CartographyDocument containing the shapes to be painted. Must not be null.
   * @param g2
   *          the Graphics2D context onto which the shapes will be painted. Must not be null.
   * @param at
   *          the AffineTransform to be applied to the shapes when painting. Must not be null.
   */
  void paintShapes(CartographyDocument<T> model, Graphics2D g2, AffineTransform at);
}
