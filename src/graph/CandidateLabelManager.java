package graph;

/**
 * Defines operations for managing candidate labels.
 */
public interface CandidateLabelManager extends LabelManager
{
  /**
   * Returns the next candidate label to process.
   *
   * @return the next candidate label
   */
  Label getCandidateLabel();
}
