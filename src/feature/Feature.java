package feature;

import geography.GeographicShape;

/**
 * An interface representing a geographic feature with a unique identifier and a geographic shape.
 */
public interface Feature
{
  /**
   * Returns the unique identifier for this feature.
   * @return the unique identifier as a String
   */
  String getID();

  /**
   * Returns the geographic shape associated with this feature.
   * @return the geographic shape as a GeographicShape object
   */
  GeographicShape getGeographicShape();
}
