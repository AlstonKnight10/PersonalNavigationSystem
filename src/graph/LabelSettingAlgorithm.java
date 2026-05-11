package graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import feature.Intersection;
import feature.StreetSegment;

/**
 * Implements a label-setting shortest-path algorithm.
 */
public class LabelSettingAlgorithm extends AbstractShortestPathAlgorithm
{
  private PermanentLabelManager labels;

  /**
   * Creates a label-setting algorithm with the given label manager.
   *
   * @param labels
   *          the permanent label manager used by the algorithm
   */
  public LabelSettingAlgorithm(final PermanentLabelManager labels)
  {
    this.labels = labels;
  }

  /**
   * Finds a path between the given origin and destination intersections.
   *
   * @param origin
   *          the starting intersection ID
   * @param destination
   *          the ending intersection ID
   * @param net
   *          the street network to search
   * @return a map of segment IDs to street segments in the path
   */
  @Override
  public Map<String, StreetSegment> findPath(final int origin, final int destination,
      final StreetNetwork net)
  {
    Map<String, StreetSegment> path = new LinkedHashMap<String, StreetSegment>();
    Label workingLabel;
    Intersection workingIntersection;
    boolean valid;
    boolean pathExists;
    List<StreetSegment> reversedPath;
    Label currentLabel;
    StreetSegment predecessor;

    if (net == null)
    {
      throw new IllegalArgumentException("net cannot be null");
    }

    valid = labels.getLabel(origin) != null && labels.getLabel(destination) != null;
    pathExists = valid;

    if (valid)
    {
      labels.getLabel(origin).setValue(0.0);
      workingLabel = labels.getSmallestLabel();

      while (workingLabel != null && workingLabel.getID() != destination && pathExists)
      {
        if (Double.isInfinite(workingLabel.getValue()))
        {
          pathExists = false;
        }
        else
        {
          labels.makePermanent(workingLabel.getID());
          workingIntersection = net.getIntersection(workingLabel.getID());

          if (workingIntersection != null)
          {
            for (StreetSegment segment : workingIntersection.getOutbound())
            {
              if (!labels.getLabel(segment.getHead()).isPermanent())
              {
                labels.adjustHeadValue(segment);
              }
            }
          }

          workingLabel = labels.getSmallestLabel();
        }
      }

      if (workingLabel == null)
      {
        pathExists = false;
      }
      else if (pathExists)
      {
        labels.makePermanent(workingLabel.getID());

        if (Double.isInfinite(labels.getLabel(destination).getValue()))
        {
          pathExists = false;
        }
      }

      if (pathExists)
      {
        reversedPath = new ArrayList<StreetSegment>();
        currentLabel = labels.getLabel(destination);

        while (currentLabel != null && currentLabel.getID() != origin && pathExists)
        {
          predecessor = currentLabel.getPredecessor();

          if (predecessor == null)
          {
            pathExists = false;
          }
          else
          {
            reversedPath.add(predecessor);
            currentLabel = labels.getLabel(predecessor.getTail());
          }
        }

        if (pathExists)
        {
          for (int i = reversedPath.size() - 1; i >= 0; i--)
          {
            predecessor = reversedPath.get(i);
            path.put(predecessor.getID(), predecessor);
          }
        }
      }
    }

    return path;
  }
}
