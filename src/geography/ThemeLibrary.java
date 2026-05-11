package geography;

/**
 * An interface representing a library of visual themes for rendering geographic features, allowing
 * retrieval of themes based on feature codes and providing a default highlight theme for emphasized
 * features.
 */
public interface ThemeLibrary
{
  /**
   * Returns the default highlight theme used for rendering emphasized features.
   * 
   * @return the default highlight Theme for emphasized features
   */
  Theme getHighlightTheme();

  /**
   * Retrieves the Theme associated with the specified feature code.
   * 
   * @param code
   *          the feature code for which to retrieve the theme
   * @return the Theme associated with the specified feature code, or null if no theme is defined
   *         for that code
   */
  Theme getTheme(String code);
}
