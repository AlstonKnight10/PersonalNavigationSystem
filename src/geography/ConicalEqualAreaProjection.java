package geography;

/**
 * An implementation of the Conical Equal Area map projection, which preserves area while projecting
 * the globe onto a cone.
 */
public class ConicalEqualAreaProjection extends AbstractMapProjection
{

  private double lambda0;
  private double phi0;
  private double phi1;
  private double phi2;

  private double n;
  private double c;
  private double rho0;

  /**
   * Constructs a Conical Equal Area Projection with the given reference meridian and parallels.
   * 
   * @param refM
   *          The reference meridian (in degrees)
   * @param refP
   *          The reference parallel (in degrees)
   * @param refP1
   *          The first standard parallel (in degrees)
   * @param refP2
   *          The second standard parallel (in degrees)
   */
  public ConicalEqualAreaProjection(final double refM, final double refP, final double refP1,
      final double refP2)
  {

    lambda0 = refM * RADIANS_PER_DEGREE;
    phi0 = refP * RADIANS_PER_DEGREE;
    phi1 = refP1 * RADIANS_PER_DEGREE;
    phi2 = refP2 * RADIANS_PER_DEGREE;

    // preliminary calculations from slide
    n = 0.5 * (Math.sin(phi1) + Math.sin(phi2));

    c = Math.pow(Math.cos(phi1), 2) + 2 * n * Math.sin(phi1);

    rho0 = Math.sqrt(c - 2 * n * Math.sin(phi0)) / n;
  }

  @Override
  public double[] forward(final double lambda, final double phi)
  {
    double rho = Math.sqrt(c - 2 * n * Math.sin(phi)) / n;

    double theta = n * (lambda - lambda0);

    double p1 = R * rho * Math.sin(theta);
    double p2 = R * (rho0 - rho * Math.cos(theta));

    return new double[] {p1, p2};
  }

  @Override
  public double[] inverse(final double ew, final double ns)
  {
    double a = Math.sqrt(Math.pow(ew / R, 2) + Math.pow(rho0 - ns / R, 2));

    double b = Math.atan((ew / R) / (rho0 - ns / R));

    double phi = Math.asin((c - a * a * n * n) / (2 * n));

    double lambda = lambda0 + b / n;

    return new double[] {lambda, phi};
  }
}
