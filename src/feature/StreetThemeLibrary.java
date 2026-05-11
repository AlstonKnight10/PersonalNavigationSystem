package feature;
import geography.*;

import java.awt.*;
import java.util.*;


/**
 * A ThemeLibrary for use with Street objects.
 *
 * @author  Prof. David Bernstein, James Madison University
 * @version 1
 */
public class StreetThemeLibrary implements ThemeLibrary
{
  private static final Theme DEFAULT_THEME = new Theme(Color.BLACK, new BasicStroke());

  private Theme highlightTheme;
  private Map<String, Theme> themes;
  
  /**
   * Default Constructor.
   */
  public StreetThemeLibrary()
  {
    themes = new HashMap<String, Theme>();

    // Interstate, U.S., and other highways
    themes.put("A1", new Theme(new Color(  0, 153, 204), new BasicStroke(3.0f)));
    themes.put("A2", new Theme(new Color(102, 153, 204), new BasicStroke(1.0f)));

    // Secondary and connecting roads
    themes.put("A3",  new Theme(new Color(102, 153, 102), new BasicStroke(3.0f)));

    // Local roads and special purpose roads
    themes.put("A4", new Theme(new Color(153, 153, 153), new BasicStroke(1.0f)));
    themes.put("A6", new Theme(new Color(153, 153, 153), new BasicStroke(1.0f)));

    // Dirt roads, alleys, and unclassified roads
    float[] pattern = new float[2];
    pattern[0] = 10.0f;
    pattern[1] = 10.0f;
    themes.put("A5", new Theme(new Color(211,211,211), 
        new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 
            10.0f, pattern, 0.0f)));
    themes.put("A7", new Theme(new Color(211,211,211), 
        new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 
            10.0f, pattern, 0.0f)));

    // Highlighted elements
    highlightTheme = new Theme(new Color(  0, 255, 0, 128), new BasicStroke(5.0f));
  }

  /**
   * Get the Theme to use for highlighted elements.
   * 
   * @return The Theme
   */
  @Override
  public Theme getHighlightTheme()
  {
    return highlightTheme;
  }

  /**
   * Get the Theme to use for the given code.
   * 
   * @param code  The code
   * @return The Theme
   */
  @Override
  public Theme getTheme(final String code)
  {
    Theme result = themes.get(code);
    if (result == null) result = DEFAULT_THEME;

    return result;
  }

}

