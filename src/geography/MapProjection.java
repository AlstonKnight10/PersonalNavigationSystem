package geography;

/**
 * The requirements of a map projection.
 * 
 * @author Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public interface MapProjection
{

  /**
   * The forward transformation (i.e., from Longitude/Latitude in __degrees__ to kilometers above
   * the equator and to the west of the reference meridian).
   *
   * @param ll
   *          The longitude and latitude in __degrees__ (in that order)
   * @return KMs west of reference and north of equator (in that order)
   */
  public abstract double[] forward(double[] ll);

  /**
   * The forward transformation (i.e., from Longitude/Latitude in __radians__ to kilometers above
   * the equator and to the west of the reference meridian).
   *
   * @param lambda
   *          The longitude in __radians__
   * @param phi
   *          The latitude in __radians__
   * @return KMs west of reference and north of equator (in that order)
   */
  public abstract double[] forward(double lambda, double phi);

  /**
   * The inverse transformation from kilometers (above the equator and to the west of the reference
   * meridian) to Longitude/Latitude in __degrees__.
   *
   * @param km
   *          The point in above,west coordinates (in that order)
   * @return The longitude and latitude in __degrees__ (in that order)
   */
  public abstract double[] inverse(double[] km);

  /**
   * The inverse transformation from kilometers (above the equator and to the west of the reference
   * meridian) to Longitude/Latitude in __radians__.
   *
   * @param ew
   *          The distance east/west in kilometers
   * @param ns
   *          The distance north/south in kilometers
   * @return The longitude and latitude in __radians__ (in that order)
   */
  public abstract double[] inverse(double ew, double ns);
}
