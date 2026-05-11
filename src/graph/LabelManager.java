package graph;

import feature.StreetSegment;

/**
 * Defines operations for managing labels in a shortest-path algorithm.
 */
public interface LabelManager
{
  /**
   * Updates the label at the head of the given segment if a better value is found.
   *
   * @param segment
   *          the segment being used to relax a label
   */
  void adjustHeadValue(StreetSegment segment);

  /**
   * Returns the label for the given intersection ID.
   *
   * @param intersectionID
   *          the ID of the intersection
   * @return the label for that intersection
   */
  Label getLabel(int intersectionID);
}
