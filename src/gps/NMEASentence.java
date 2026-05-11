package gps;

/**
 * A base class for NMEA sentences.
 */
public abstract class NMEASentence
{
  private static final String DELIMITER = ",";
  private static final String SOUTH = "S";
  private static final String WEST = "W";

  /**
   * Converts a latitude string to decimal degrees.
   *
   * @param latitudeString
   *          The latitude string
   * @return The latitude in decimal degrees
   */
  protected double convertLatitude(final String latitudeString)
  {
    double decimal = Double.NaN;

    if (latitudeString != null)
    {
      String trimmed = latitudeString.trim();

      if (!trimmed.isEmpty())
      {
        String[] parts = trimmed.split(DELIMITER);

        if ((parts.length > 0) && (parts[0] != null) && !parts[0].trim().isEmpty())
        {
          String raw = parts[0].trim();
          String hemisphere = "";

          if (parts.length > 1)
          {
            hemisphere = parts[1].trim();
          }

          double value = Double.parseDouble(raw);
          int degrees = (int) (value / 100.0);
          double minutes = value - (degrees * 100.0);
          decimal = degrees + (minutes / 60.0);

          if (SOUTH.equalsIgnoreCase(hemisphere))
          {
            decimal = -decimal;
          }
        }
      }
    }

    return decimal;
  }

  /**
   * Converts a longitude string to decimal degrees.
   *
   * @param longitudeString
   *          The longitude string
   * @return The longitude in decimal degrees
   */
  protected double convertLongitude(final String longitudeString)
  {
    double decimal = Double.NaN;

    if (longitudeString != null)
    {
      String trimmed = longitudeString.trim();

      if (!trimmed.isEmpty())
      {
        String[] parts = trimmed.split(DELIMITER);

        if ((parts.length > 0) && (parts[0] != null) && !parts[0].trim().isEmpty())
        {
          String raw = parts[0].trim();
          String hemisphere = "";

          if (parts.length > 1)
          {
            hemisphere = parts[1].trim();
          }

          double value = Double.parseDouble(raw);
          int degrees = (int) (value / 100.0);
          double minutes = value - (degrees * 100.0);
          decimal = degrees + (minutes / 60.0);

          if (WEST.equalsIgnoreCase(hemisphere))
          {
            decimal = -decimal;
          }
        }
      }
    }

    return decimal;
  }
}
