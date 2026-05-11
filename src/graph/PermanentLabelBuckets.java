package graph;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;

import feature.StreetSegment;

/**
 * Manages permanent labels using buckets keyed by exact label value.
 */
public class PermanentLabelBuckets extends AbstractLabelManager implements PermanentLabelManager
{
  /** Stores intersection IDs by label value. */
  private final TreeMap<Double, LinkedHashSet<Integer>> buckets;

  /** True after the buckets have been built once. */
  private boolean initialized;

  /**
   * Creates a bucket-based permanent label manager.
   *
   * @param networkSize
   *          the number of intersections
   */
  public PermanentLabelBuckets(final int networkSize)
  {
    super(networkSize);
    buckets = new TreeMap<Double, LinkedHashSet<Integer>>();
    initialized = false;
  }

  /**
   * Adjusts the head label for the given segment.
   *
   * @param segment
   *          the segment to use
   */
  @Override
  public void adjustHeadValue(final StreetSegment segment)
  {
    final int head;
    final Label label;
    final double oldValue;
    final double newValue;

    if (segment == null)
    {
      throw new IllegalArgumentException("segment cannot be null");
    }

    head = segment.getHead();
    label = getLabel(head);

    if (label != null && !label.isPermanent())
    {
      oldValue = label.getValue();
      super.adjustHeadValue(segment);
      newValue = label.getValue();

      if (initialized && newValue < oldValue)
      {
        removeFromBucket(oldValue, head);
        addToBucket(newValue, head);
      }
    }
  }

  /**
   * Returns the smallest non-permanent label.
   *
   * @return the smallest label, or null if none remain
   */
  @Override
  public Label getSmallestLabel()
  {
    if (!initialized)
    {
      buildBuckets();
      initialized = true;
    }

    while (!buckets.isEmpty())
    {
      final Map.Entry<Double, LinkedHashSet<Integer>> entry = buckets.firstEntry();
      final LinkedHashSet<Integer> bucket = entry.getValue();

      while (!bucket.isEmpty())
      {
        final int id = bucket.iterator().next();
        final Label label = getLabel(id);

        if (label != null && !label.isPermanent()
            && Double.compare(label.getValue(), entry.getKey()) == 0)
        {
          return label;
        }

        bucket.remove(id);
      }

      buckets.pollFirstEntry();
    }

    return null;
  }

  /**
   * Marks the given label as permanent.
   *
   * @param intersectionID
   *          the intersection ID
   */
  @Override
  public void makePermanent(final int intersectionID)
  {
    final Label label = getLabel(intersectionID);

    if (label != null && !label.isPermanent())
    {
      removeFromBucket(label.getValue(), intersectionID);
      label.makePermanent();
    }
  }

  /**
   * Builds the initial bucket structure.
   */
  private void buildBuckets()
  {
    for (int i = 0; i < labels.length; i++)
    {
      if (labels[i] != null && !labels[i].isPermanent())
      {
        addToBucket(labels[i].getValue(), i);
      }
    }
  }

  /**
   * Adds an intersection to a bucket.
   *
   * @param value
   *          the label value
   * @param id
   *          the intersection ID
   */
  private void addToBucket(final double value, final int id)
  {
    LinkedHashSet<Integer> bucket = buckets.get(value);

    if (bucket == null)
    {
      bucket = new LinkedHashSet<Integer>();
      buckets.put(value, bucket);
    }

    bucket.add(id);
  }

  /**
   * Removes an intersection from a bucket.
   *
   * @param value
   *          the label value
   * @param id
   *          the intersection ID
   */
  private void removeFromBucket(final double value, final int id)
  {
    final LinkedHashSet<Integer> bucket = buckets.get(value);

    if (bucket != null)
    {
      bucket.remove(id);

      if (bucket.isEmpty())
      {
        buckets.remove(value);
      }
    }
  }
}
