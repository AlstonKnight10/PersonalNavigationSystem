package geography;

/**
 * Class for representing the Sinusoidal map projection, which is an equal-area projection that
 * preserves area but distorts shapes and angles.
 */
public class SinusoidalProjection extends AbstractMapProjection
{

  /**
   * Constructs a new SinusoidalProjection with the default reference meridian and parallel (0
   * degrees).
   */
  public SinusoidalProjection()
  {
    super();
  }

  @Override
  public double[] forward(final double lambda, final double phi)
  {
    return new double[] {R * lambda * Math.cos(phi), R * phi};
  }

  @Override
  public double[] inverse(final double ew, final double ns)
  {
    double phi = ns / R;
    return new double[] {ew / (R * Math.cos(phi)), phi};
  }
}
