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

  private final List<LineSegment2D> roadSegments;
  private final Map<Long, List<Integer>> grid;
  private final double cellSizeKm;
  private final int maxExpansionRings;
  private StreetSegment currentSegment;

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
    this.currentSegment = null;

    for (StreetSegment segment : document)
    {
      addShapeSegments(segment.getGeographicShape().getShape(), segment);
    }
  }

  /**
   * Gets the segment from the most recent match result.
   *
   * @return currently matched segment, or null if unavailable
   */
  public StreetSegment getCurrentSegment()
  {
    return currentSegment;
  }

  /**
   * Compatibility no-op for code paths that provide route context.
   *
   * @param route ignored
   */
  public void setActiveRoute(final List<StreetSegment> route)
  {
    // Intentionally ignored in this simplified matcher version.
  }

  /**
   * Compatibility method for diagnostics in caller code.
   *
   * @return false for this simplified matcher version
   */
  public boolean isRouteLockEnabled()
  {
    return false;
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
    return match(point).getPoint();
  }

  /**
   * Returns a full map-match result including snapped point and matched segment.
   *
   * @param point
   *          projected point [x, y]
   * @return map-match result
   */
  public MapMatchResult match(final double[] point)
  {
    if (point == null || point.length != 2)
    {
      throw new IllegalArgumentException("point must be double[2]");
    }

    if (roadSegments.isEmpty())
    {
      currentSegment = null;
      return new MapMatchResult(new double[] {point[0], point[1]}, null, Double.POSITIVE_INFINITY);
    }

    int cellX = toCell(point[0]);
    int cellY = toCell(point[1]);

    Set<Integer> candidateIndexes = collectCandidateIndexes(cellX, cellY);
    if (candidateIndexes.isEmpty())
    {
      return matchAgainstAll(point[0], point[1]);
    }

    return matchAgainstCandidates(point[0], point[1], candidateIndexes);
  }

  private MapMatchResult matchAgainstAll(final double px, final double py)
  {
    double bestDistanceSquared = Double.POSITIVE_INFINITY;
    double[] bestPoint = new double[] {px, py};
    StreetSegment bestSegment = null;

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
        bestSegment = segment.owner;
      }
    }

    currentSegment = bestSegment;
    return new MapMatchResult(bestPoint, bestSegment, Math.sqrt(bestDistanceSquared));
  }

  private MapMatchResult matchAgainstCandidates(final double px, final double py,
      final Set<Integer> candidateIndexes)
  {
    double bestDistanceSquared = Double.POSITIVE_INFINITY;
    double[] bestPoint = new double[] {px, py};
    StreetSegment bestSegment = null;

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
        bestSegment = segment.owner;
      }
    }

    currentSegment = bestSegment;
    return new MapMatchResult(bestPoint, bestSegment, Math.sqrt(bestDistanceSquared));
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

  private void addShapeSegments(final Shape shape, final StreetSegment owner)
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
          addRoadSegment(lastX, lastY, coords[0], coords[1], owner);
        }
        lastX = coords[0];
        lastY = coords[1];
        hasLast = true;
      }
      else if (type == PathIterator.SEG_CLOSE)
      {
        if (hasLast)
        {
          addRoadSegment(lastX, lastY, startX, startY, owner);
        }
      }

      iterator.next();
    }
  }

  private void addRoadSegment(final double ax, final double ay, final double bx, final double by,
      final StreetSegment owner)
  {
    LineSegment2D segment = new LineSegment2D(ax, ay, bx, by, owner);
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
    private final StreetSegment owner;

    private LineSegment2D(final double ax, final double ay, final double bx, final double by,
        final StreetSegment owner)
    {
      this.ax = ax;
      this.ay = ay;
      this.bx = bx;
      this.by = by;
      this.owner = owner;
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

  public static class MapMatchResult
  {
    private final double[] point;
    private final StreetSegment segment;
    private final double distanceKm;

    private MapMatchResult(final double[] point, final StreetSegment segment,
        final double distanceKm)
    {
      this.point = new double[] {point[0], point[1]};
      this.segment = segment;
      this.distanceKm = distanceKm;
    }

    public double[] getPoint()
    {
      return new double[] {point[0], point[1]};
    }

    public StreetSegment getSegment()
    {
      return segment;
    }

    public double getDistanceKm()
    {
      return distanceKm;
    }
  }
}
