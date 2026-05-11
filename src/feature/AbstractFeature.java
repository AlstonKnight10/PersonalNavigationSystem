package feature;

/**
 * An abstract base class for features that provides a common implementation of the getID() method.
 */
public abstract class AbstractFeature implements Feature
{

  private String id;

  /**
   * Constructs an AbstractFeature with the specified ID.
   * @param id The unique identifier for this feature. Must not be null.
   */
  public AbstractFeature(final String id)
  {
    this.id = id;
  }

  @Override
  public String getID()
  {
    return id;
  }
}
