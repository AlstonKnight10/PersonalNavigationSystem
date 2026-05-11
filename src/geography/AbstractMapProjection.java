package geography;

/**
 * An abstract class that is extended to construct different types map projections.
 *
 * @author Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public abstract class AbstractMapProjection implements MapProjection
{
  // Radius of spherical Earth (in km)
  protected static final double R = 6369.0;

  protected static final double PI = Math.PI;
  protected static final double TWO_PI = 2.0 * Math.PI;
  protected static final double PI_OVER_TWO = Math.PI / 2.0;
  protected static final double PI_OVER_FOUR = Math.PI / 4.0;

  protected static final double RADIANS_PER_DEGREE = 2.0 * Math.PI / 360.0;

  protected static final double DEGREES_PER_RADIAN = 1.0 / RADIANS_PER_DEGREE;

  /**
   * The forward transformation (i.e., from Longitude/Latitude in __degrees__ to kilometers above
   * the equator and to the west of the reference meridian).
   *
   * @param ll
   *          The longitude and latitude in __degrees__ (in that order)
   * @return KMs west of reference and north of equator (in that order)
   */
  @Override
  public double[] forward(final double[] ll)
  {
    double lambda = ll[0] * RADIANS_PER_DEGREE;
    double phi = ll[1] * RADIANS_PER_DEGREE;

    return forward(lambda, phi);
  }

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
  @Override
  public abstract double[] forward(final double lambda, final double phi);

  /**
   * The inverse transformation from kilometers (above the equator and to the west of the reference
   * meridian) to Longitude/Latitude in __degrees__.
   *
   * @param km
   *          The point in above,west coordinates (in that order)
   * @return The longitude and latitude in __degrees__ (in that order)
   */
  @Override
  public double[] inverse(final double[] km)
  {
    double[] ll = inverse(km[0], km[1]);// In radians
    ll[0] = ll[0] * DEGREES_PER_RADIAN; // In degrees
    ll[1] = ll[1] * DEGREES_PER_RADIAN; // In degrees

    return ll;
  }

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
  @Override
  public abstract double[] inverse(final double ew, final double ns);

}
