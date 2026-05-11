package gps;

/**
 * A GPGGA sentence containing latitude and longitude.
 */
public class GPGGASentence extends NMEASentence
{
  private static final String PREFIX = "$GPGGA";
  private static final String DELIMITER = ",";

  private double latitude;
  private double longitude;

  /**
   * Constructs a GPGGASentence.
   *
   * @param latitude
   *          The latitude
   * @param longitude
   *          The longitude
   */
  public GPGGASentence(final double latitude, final double longitude)
  {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  /**
   * Parses a GPGGA sentence.
   *
   * @param sentence
   *          The sentence to parse
   * @return The parsed GPGGASentence
   */
  public static GPGGASentence parseGPGGA(final String sentence)
  {
    if ((sentence == null) || !sentence.startsWith(PREFIX))
    {
      throw new IllegalArgumentException("Not a GPGGA sentence.");
    }

    String body = sentence;
    int star = body.indexOf('*');
    if (star >= 0)
    {
      body = body.substring(0, star);
    }

    String[] tokens = body.split(DELIMITER, -1);
    if (tokens.length < 6)
    {
      throw new IllegalArgumentException("Malformed GPGGA sentence.");
    }

    if (tokens[2].isEmpty() || tokens[3].isEmpty() || tokens[4].isEmpty() || tokens[5].isEmpty())
    {
      throw new IllegalArgumentException("GPGGA sentence missing position.");
    }

    GPGGASentence helper = new GPGGASentence(0.0, 0.0);
    double latitude = helper.convertLatitude(tokens[2] + DELIMITER + tokens[3]);
    double longitude = helper.convertLongitude(tokens[4] + DELIMITER + tokens[5]);

    return new GPGGASentence(latitude, longitude);
  }

  /**
   * Gets the latitude.
   *
   * @return The latitude
   */
  public double getLatitude()
  {
    return latitude;
  }

  /**
   * Gets the longitude.
   *
   * @return The longitude
   */
  public double getLongitude()
  {
    return longitude;
  }
}
