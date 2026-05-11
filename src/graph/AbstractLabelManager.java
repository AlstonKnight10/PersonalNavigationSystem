package graph;

import feature.StreetSegment;

/**
 * Provides common label storage behavior for label manager implementations.
 */
public abstract class AbstractLabelManager implements LabelManager
{
  protected Label[] labels;

  /**
   * Creates a label manager with one label for each intersection index in the network.
   *
   * @param networkSize
   *          the number of usable intersection indices in the network
   */
  public AbstractLabelManager(final int networkSize)
  {
    if (networkSize < 0)
    {
      throw new IllegalArgumentException("networkSize cannot be negative");
    }

    labels = new Label[networkSize];

    for (int i = 0; i < networkSize; i++)
    {
      labels[i] = new Label(i);
    }
  }

  /**
   * Updates the label at the head of the given segment if a better value is found.
   *
   * @param segment
   *          the segment used to try to improve a label
   */
  @Override
  public void adjustHeadValue(final StreetSegment segment)
  {
    int tail;
    int head;
    Label tailLabel;
    Label headLabel;
    double possibleValue;

    if (segment == null)
    {
      throw new IllegalArgumentException("segment cannot be null");
    }

    tail = segment.getTail();
    head = segment.getHead();

    tailLabel = getLabel(tail);
    headLabel = getLabel(head);

    if (tailLabel == null || headLabel == null)
    {
      return;
    }

    if (Double.isInfinite(tailLabel.getValue()))
    {
      return;
    }

    possibleValue = tailLabel.getValue() + segment.getLength();
    headLabel.adjustValue(possibleValue, segment);
  }

  /**
   * Returns the label for the given intersection ID.
   *
   * @param intersectionID
   *          the intersection ID
   * @return the label for that intersection, or null if the ID is invalid
   */
  @Override
  public Label getLabel(final int intersectionID)
  {
    if (intersectionID < 0 || intersectionID >= labels.length)
    {
      return null;
    }

    return labels[intersectionID];
  }
}
