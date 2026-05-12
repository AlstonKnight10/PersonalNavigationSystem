package gps;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import feature.StreetSegment;
import gui.CartographyDocument;

/**
 * Snaps projected points to the nearest street segment.
 */
public class MapMatcher
{
  private static final double DEFAULT_CELL_SIZE_KM = 0.25;
  private static final int DEFAULT_MAX_EXPANSION_RINGS = 8;

  private List<LineSegment2D> roadSegments;
  private Map<Long, List<Integer>> grid;
  private double cellSizeKm;
  private int maxExpansionRings;

  /**
   * Constructs a matcher from all street-segment geometry in the document.
   *
   * @param document
   *          street-segment document
   */
  public MapMatcher(final CartographyDocument<StreetSegment> document)
  {
    this(document, DEFAULT_CELL_SIZE_KM, DEFAULT_MAX_EXPANSION_RINGS);
  }

  /**
   * Constructs a matcher from all street-segment geometry in the document.
   *
   * @param document
   *          street-segment document
   * @param cellSizeKm
   *          spatial-grid cell size in kilometers
   * @param maxExpansionRings
   *          max neighboring rings to search before fallback
   */
  public MapMatcher(final CartographyDocument<StreetSegment> document, final double cellSizeKm,
      final int maxExpansionRings)
  {
    if (document == null)
    {
      throw new IllegalArgumentException("document cannot be null");
    }
    if (cellSizeKm <= 0.0)
    {
      throw new IllegalArgumentException("cellSizeKm must be positive");
    }
    if (maxExpansionRings < 0)
    {
      throw new IllegalArgumentException("maxExpansionRings cannot be negative");
    }

    this.roadSegments = new ArrayList<LineSegment2D>();
    this.grid = new HashMap<Long, List<Integer>>();
    this.cellSizeKm = cellSizeKm;
    this.maxExpansionRings = maxExpansionRings;

    for (StreetSegment segment : document)
    {
      addShapeSegments(segment.getGeographicShape().getShape());
    }
  }

  /**
   * Snaps a point in projected coordinates (km) to the nearest road segment.
   *
   * @param point
   *          projected point [x, y]
   * @return snapped projected point [x, y]
   */
  public double[] snap(final double[] point)
  {
    if (point == null || point.length != 2)
    {
      throw new IllegalArgumentException("point must be double[2]");
    }

    if (roadSegments.isEmpty())
    {
      return new double[] {point[0], point[1]};
    }

    int cellX = toCell(point[0]);
    int cellY = toCell(point[1]);

    Set<Integer> candidateIndexes = collectCandidateIndexes(cellX, cellY);
    if (candidateIndexes.isEmpty())
    {
      return snapAgainstAll(point[0], point[1]);
    }

    return snapAgainstCandidates(point[0], point[1], candidateIndexes);
  }

  private double[] snapAgainstAll(final double px, final double py)
  {
    double bestDistanceSquared = Double.POSITIVE_INFINITY;
    double[] bestPoint = new double[] {px, py};

    for (LineSegment2D segment : roadSegments)
    {
      double[] candidate = segment.closestPoint(px, py);
      double dx = candidate[0] - px;
      double dy = candidate[1] - py;
      double distanceSquared = dx * dx + dy * dy;

      if (distanceSquared < bestDistanceSquared)
      {
        bestDistanceSquared = distanceSquared;
        bestPoint = candidate;
      }
    }

    return bestPoint;
  }

  private double[] snapAgainstCandidates(final double px, final double py,
      final Set<Integer> candidateIndexes)
  {
    double bestDistanceSquared = Double.POSITIVE_INFINITY;
    double[] bestPoint = new double[] {px, py};

    for (Integer index : candidateIndexes)
    {
      LineSegment2D segment = roadSegments.get(index.intValue());
      double[] candidate = segment.closestPoint(px, py);
      double dx = candidate[0] - px;
      double dy = candidate[1] - py;
      double distanceSquared = dx * dx + dy * dy;

      if (distanceSquared < bestDistanceSquared)
      {
        bestDistanceSquared = distanceSquared;
        bestPoint = candidate;
      }
    }

    return bestPoint;
  }

  private Set<Integer> collectCandidateIndexes(final int centerX, final int centerY)
  {
    Set<Integer> candidates = new HashSet<Integer>();

    List<Integer> center = grid.get(key(centerX, centerY));
    if (center != null)
    {
      candidates.addAll(center);
    }

    for (int ring = 1; (ring <= maxExpansionRings) && candidates.isEmpty(); ring++)
    {
      int minX = centerX - ring;
      int maxX = centerX + ring;
      int minY = centerY - ring;
      int maxY = centerY + ring;

      for (int x = minX; x <= maxX; x++)
      {
        addCellCandidates(candidates, x, minY);
        addCellCandidates(candidates, x, maxY);
      }

      for (int y = minY + 1; y <= maxY - 1; y++)
      {
        addCellCandidates(candidates, minX, y);
        addCellCandidates(candidates, maxX, y);
      }
    }

    return candidates;
  }

  private void addCellCandidates(final Set<Integer> candidates, final int cellX, final int cellY)
  {
    List<Integer> bucket = grid.get(key(cellX, cellY));
    if (bucket != null)
    {
      candidates.addAll(bucket);
    }
  }

  private void addShapeSegments(final Shape shape)
  {
    PathIterator iterator = shape.getPathIterator(null);
    double[] coords = new double[6];

    double startX = 0.0;
    double startY = 0.0;
    double lastX = 0.0;
    double lastY = 0.0;
    boolean hasLast = false;

    while (!iterator.isDone())
    {
      int type = iterator.currentSegment(coords);

      if (type == PathIterator.SEG_MOVETO)
      {
        startX = coords[0];
        startY = coords[1];
        lastX = coords[0];
        lastY = coords[1];
        hasLast = true;
      }
      else if (type == PathIterator.SEG_LINETO)
      {
        if (hasLast)
        {
          addRoadSegment(lastX, lastY, coords[0], coords[1]);
        }
        lastX = coords[0];
        lastY = coords[1];
        hasLast = true;
      }
      else if (type == PathIterator.SEG_CLOSE)
      {
        if (hasLast)
        {
          addRoadSegment(lastX, lastY, startX, startY);
        }
      }

      iterator.next();
    }
  }

  private void addRoadSegment(final double ax, final double ay, final double bx, final double by)
  {
    LineSegment2D segment = new LineSegment2D(ax, ay, bx, by);
    int index = roadSegments.size();
    roadSegments.add(segment);

    int minCellX = toCell(Math.min(ax, bx));
    int maxCellX = toCell(Math.max(ax, bx));
    int minCellY = toCell(Math.min(ay, by));
    int maxCellY = toCell(Math.max(ay, by));

    for (int cellX = minCellX; cellX <= maxCellX; cellX++)
    {
      for (int cellY = minCellY; cellY <= maxCellY; cellY++)
      {
        long key = key(cellX, cellY);
        List<Integer> bucket = grid.get(key);
        if (bucket == null)
        {
          bucket = new ArrayList<Integer>();
          grid.put(key, bucket);
        }
        bucket.add(Integer.valueOf(index));
      }
    }
  }

  private int toCell(final double value)
  {
    return (int) Math.floor(value / cellSizeKm);
  }

  private long key(final int cellX, final int cellY)
  {
    return (((long) cellX) << 32) ^ (cellY & 0xffffffffL);
  }

  private static class LineSegment2D
  {
    private final double ax;
    private final double ay;
    private final double bx;
    private final double by;

    private LineSegment2D(final double ax, final double ay, final double bx, final double by)
    {
      this.ax = ax;
      this.ay = ay;
      this.bx = bx;
      this.by = by;
    }

    private double[] closestPoint(final double px, final double py)
    {
      double abx = bx - ax;
      double aby = by - ay;
      double abLengthSquared = abx * abx + aby * aby;

      if (abLengthSquared == 0.0)
      {
        return new double[] {ax, ay};
      }

      double apx = px - ax;
      double apy = py - ay;
      double t = (apx * abx + apy * aby) / abLengthSquared;

      if (t < 0.0)
      {
        t = 0.0;
      }
      else if (t > 1.0)
      {
        t = 1.0;
      }

      return new double[] {ax + t * abx, ay + t * aby};
    }
  }
}
