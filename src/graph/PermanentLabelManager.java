package graph;

/**
 * Defines operations for managing permanent labels.
 */
public interface PermanentLabelManager extends LabelManager
{
  /**
   * Returns the smallest temporary label currently available.
   *
   * @return the smallest label
   */
  Label getSmallestLabel();

  /**
   * Marks the label for the given intersection as permanent.
   *
   * @param intersectionID
   *          the ID of the intersection
   */
  void makePermanent(int intersectionID);
}
