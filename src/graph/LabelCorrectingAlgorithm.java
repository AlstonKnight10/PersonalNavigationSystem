package graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import feature.Intersection;
import feature.StreetSegment;

/**
 * Implements a label-correcting shortest-path algorithm.
 */
public class LabelCorrectingAlgorithm extends AbstractShortestPathAlgorithm
{
  private final CandidateLabelManager labels;

  /**
   * Creates the algorithm.
   *
   * @param labels
   *          the candidate label manager
   */
  public LabelCorrectingAlgorithm(final CandidateLabelManager labels)
  {
    this.labels = labels;
  }

  @Override
  public Map<String, StreetSegment> findPath(final int origin, final int destination,
      final StreetNetwork net)
  {
    final Map<String, StreetSegment> path = new LinkedHashMap<String, StreetSegment>();

    if (net == null)
    {
      throw new IllegalArgumentException("net cannot be null");
    }

    final Label originLabel = labels.getLabel(origin);
    final Label destinationLabel = labels.getLabel(destination);

    if (originLabel == null || destinationLabel == null || origin == destination)
    {
      return path;
    }

    originLabel.setValue(0.0);
    relaxIntersection(net.getIntersection(origin), destinationLabel);

    Label current = labels.getCandidateLabel();

    while (current != null)
    {
      if (Double.isInfinite(destinationLabel.getValue())
          || current.getValue() < destinationLabel.getValue())
      {
        relaxIntersection(net.getIntersection(current.getID()), destinationLabel);
      }

      current = labels.getCandidateLabel();
    }

    if (Double.isInfinite(destinationLabel.getValue()))
    {
      return path;
    }

    return buildPath(origin, destinationLabel);
  }

  private Map<String, StreetSegment> buildPath(final int origin, final Label destinationLabel)
  {
    final List<StreetSegment> reversed = new ArrayList<StreetSegment>();
    Label current = destinationLabel;

    while (current != null && current.getID() != origin)
    {
      final StreetSegment predecessor = current.getPredecessor();

      if (predecessor == null)
      {
        return new LinkedHashMap<String, StreetSegment>();
      }

      reversed.add(predecessor);
      current = labels.getLabel(predecessor.getTail());
    }

    final Map<String, StreetSegment> path = new LinkedHashMap<String, StreetSegment>(
        reversed.size() * 2 + 1);

    for (int i = reversed.size() - 1; i >= 0; i--)
    {
      final StreetSegment segment = reversed.get(i);
      path.put(segment.getID(), segment);
    }

    return path;
  }

  private void relaxIntersection(final Intersection intersection, final Label destinationLabel)
  {
    if (intersection == null)
    {
      return;
    }

    final List<StreetSegment> outbound = intersection.getOutbound();
    final List<String> changed = new ArrayList<String>();

    for (final StreetSegment segment : outbound)
    {
      final Label tailLabel = labels.getLabel(segment.getTail());
      final Label headLabel = labels.getLabel(segment.getHead());

      if (tailLabel == null || headLabel == null)
      {
        continue;
      }

      final double tailValue = tailLabel.getValue();

      if (Double.isInfinite(tailValue))
      {
        continue;
      }

      if (!Double.isInfinite(destinationLabel.getValue())
          && tailValue + segment.getLength() >= destinationLabel.getValue())
      {
        continue;
      }

      final double oldValue = headLabel.getValue();
      labels.adjustHeadValue(segment);

      if (headLabel.getValue() < oldValue)
      {
        changed.add(segment.getID());
      }
    }

    if (!changed.isEmpty())
    {
      notifyStreetSegmentObservers(changed);
    }
  }
}
