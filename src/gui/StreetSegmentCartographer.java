package gui;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

import feature.StreetSegment;
import feature.StreetThemeLibrary;
import geography.Theme;
import geography.ThemeLibrary;

/**
 * Cartographer implementation for rendering StreetSegment features, using a ThemeLibrary to
 * determine visual styles based on segment codes and providing highlighting for emphasized
 * segments.
 */
public class StreetSegmentCartographer implements Cartographer<StreetSegment>
{
  private ThemeLibrary themes;

  /**
   * Constructs a StreetSegmentCartographer with a default StreetThemeLibrary for determining visual
   * styles based on street segment codes.
   */
  public StreetSegmentCartographer()
  {
    this.themes = new StreetThemeLibrary();
  }

  @Override
  public void paintShapes(final CartographyDocument<StreetSegment> model, final Graphics2D g2,
      final AffineTransform at)
  {
    for (StreetSegment segment : model)
    {
      Theme theme = themes.getTheme(segment.getCode());

      g2.setColor(theme.getColor());
      g2.setStroke(theme.getStroke());

      Shape transformed = at.createTransformedShape(segment.getGeographicShape().getShape());
      g2.draw(transformed);
    }
  }

  @Override
  public void paintHighlights(final CartographyDocument<StreetSegment> model, final Graphics2D g2,
      final AffineTransform at)
  {
    Theme highlight = themes.getHighlightTheme();

    g2.setColor(highlight.getColor());
    g2.setStroke(highlight.getStroke());

    Iterator<StreetSegment> it = model.highlighted();
    while (it.hasNext())
    {
      StreetSegment segment = it.next();

      Shape transformed = at.createTransformedShape(segment.getGeographicShape().getShape());
      g2.draw(transformed);
    }
  }
}
