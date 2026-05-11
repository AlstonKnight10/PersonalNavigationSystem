package graph;

import java.util.ArrayDeque;
import java.util.Deque;

import feature.StreetSegment;

/**
 * Manages candidate labels using a deque-based policy.
 */
public class CandidateLabelList extends AbstractLabelManager implements CandidateLabelManager
{
  public static final String NEWEST = "N";
  public static final String OLDEST = "O";

  private Deque<Integer> candidates;
  private String policy;
  private boolean[] inCandidates;

  /**
   * Creates a candidate label manager with the given selection policy.
   *
   * @param policy
   *          the candidate selection policy
   * @param networkSize
   *          the number of intersections in the network
   */
  public CandidateLabelList(final String policy, final int networkSize)
  {
    super(networkSize);
    this.policy = policy;
    this.candidates = new ArrayDeque<>(networkSize);
    this.inCandidates = new boolean[networkSize];
  }

  /**
   * Updates the label at the head of the given segment and tracks it as a candidate when
   * appropriate.
   *
   * @param segment
   *          the segment used to try to improve a label
   */
  @Override
  public void adjustHeadValue(final StreetSegment segment)
  {
    if (segment == null)
    {
      throw new IllegalArgumentException("segment cannot be null");
    }

    final int head = segment.getHead();
    final Label headLabel = getLabel(head);

    if (headLabel != null)
    {
      final double oldValue = headLabel.getValue();
      super.adjustHeadValue(segment);

      if (headLabel.getValue() < oldValue && !inCandidates[head])
      {
        candidates.addLast(head);
        inCandidates[head] = true;
      }
    }
  }

  /**
   * Returns the next candidate label according to the configured policy.
   *
   * @return the next candidate label, or null if none remain
   */
  @Override
  public Label getCandidateLabel()
  {
    final Integer index;

    if (candidates.isEmpty())
    {
      return null;
    }

    if (NEWEST.equals(policy))
    {
      index = candidates.removeLast();
    }
    else
    {
      index = candidates.removeFirst();
    }

    inCandidates[index] = false;
    return labels[index];
  }
}
