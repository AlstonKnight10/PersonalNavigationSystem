package graph;

import feature.StreetSegment;

/**
 * Stores the current shortest-path information for one intersection in the street network.
 */
public class Label
{
  private boolean permanent;
  private double value;
  private int id;
  private StreetSegment predecessor;

  /**
   * Creates a label with a default ID and an initial value of positive infinity.
   */
  public Label()
  {
    this(-1);
  }

  /**
   * Creates a label for the given intersection ID.
   *
   * @param id
   *          the ID of the intersection this label belongs to
   */
  public Label(final int id)
  {
    this.id = id;
    this.value = Double.POSITIVE_INFINITY;
    this.predecessor = null;
    this.permanent = false;
  }

  /**
   * Updates this label if the given value is better than the current one.
   *
   * @param possibleValue
   *          the new candidate distance
   * @param possiblePredecessor
   *          the segment used to reach this label
   */
  public void adjustValue(final double possibleValue, final StreetSegment possiblePredecessor)
  {
    if (possibleValue < value)
    {
      value = possibleValue;
      predecessor = possiblePredecessor;
    }
  }

  /**
   * Returns the ID of the intersection associated with this label.
   *
   * @return the intersection ID
   */
  public int getID()
  {
    return id;
  }

  /**
   * Returns the street segment that precedes this label in the current best path.
   *
   * @return the predecessor street segment
   */
  public StreetSegment getPredecessor()
  {
    return predecessor;
  }

  /**
   * Returns the current value of this label.
   *
   * @return the current shortest-known distance
   */
  public double getValue()
  {
    return value;
  }

  /**
   * Returns whether this label has been made permanent.
   *
   * @return true if the label is permanent; false otherwise
   */
  public boolean isPermanent()
  {
    return permanent;
  }

  /**
   * Marks this label as permanent.
   */
  public void makePermanent()
  {
    permanent = true;
  }

  /**
   * Sets the value of this label.
   *
   * @param value
   *          the new label value
   */
  public void setValue(final double value)
  {
    this.value = value;
  }
}
