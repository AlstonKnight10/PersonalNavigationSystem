package gps;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import feature.StreetSegment;
import gui.CartographyDocument;

/**
 * Snaps projected points to road geometry.
 */
public class MapMatcher
{
  private static final double DEFAULT_CELL_SIZE_KM = 0.25;
  private static final int DEFAULT_MAX_EXPANSION_RINGS = 8;

  private List<LineSegment2D> roadSegments;
  private Map<Long, List<Integer>> grid;
  private double cellSizeKm;
  private int maxExpansionRings;
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

    this.activeRoute = Collections.emptyList();
    this.activeRouteIDs = Collections.emptySet();
    this.currentRouteIndex = -1;
    this.routeLockEnabled = false;
    this.offRouteFixCount = 0;
    this.pendingTurnIndex = -1;
    this.pendingTurnFixCount = 0;
    this.previousMatchedPoint = null;
    this.lastMatchedPoint = null;
    this.fixCounter = 0L;
    this.currentSegment = null;

    for (StreetSegment segment : document)
    {
      addShapeSegments(segment.getGeographicShape().getShape(), segment);
    }
  }

  /**
   * Gets the segment from the most recent snap result.
   *
   * @return currently matched segment, or null if unavailable
   */
  public StreetSegment getCurrentSegment()
  {
    return currentSegment;
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
      return new double[] {point[0], point[1]};
    }

    fixCounter++;

    if (!activeRoute.isEmpty() && routeLockEnabled)
    {
      MapMatchResult routeMatched = matchOnRoute(point[0], point[1]);
      if (routeMatched != null)
      {
        return remember(routeMatched);
      }
    }

    MapMatchResult fallback = matchAgainstGlobal(point[0], point[1]);
    attemptRelock(fallback);
    updateMatchedPointHistory(fallback.getPoint());

    if (shouldLog())
    {
      String id = (fallback.getSegment() == null) ? "null" : fallback.getSegment().getID();
      log("Fallback match used: seg=" + id + " distKm=" + fallback.getDistanceKm()
          + " lock=" + routeLockEnabled + " routeIdx=" + currentRouteIndex + " offCount="
          + offRouteFixCount);
    }

    return remember(fallback);
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

  private MapMatchResult remember(final MapMatchResult result)
  {
    currentSegment = (result == null) ? null : result.getSegment();
    return result;
  }

  private MapMatchResult matchOnRoute(final double px, final double py)
  {
    RouteCandidate best = null;
    RouteCandidate currentCandidate = null;
    RouteCandidate nextCandidate = null;

    int startIndex;
    int endIndex;

    if (currentRouteIndex < 0)
    {
      startIndex = 0;
      endIndex = activeRoute.size() - 1;
    }
    else
    {
      startIndex = Math.max(0, currentRouteIndex - ROUTE_WINDOW_BACK);
      endIndex = Math.min(activeRoute.size() - 1, currentRouteIndex + ROUTE_WINDOW_FORWARD);
    }

    for (int i = startIndex; i <= endIndex; i++)
    {
      RouteCandidate candidate = activeRoute.get(i).match(px, py, i);
      if (candidate == null)
      {
        continue;
      }

      if (i == currentRouteIndex)
      {
        currentCandidate = candidate;
      }
      else if ((currentRouteIndex >= 0) && (i == currentRouteIndex + 1))
      {
        nextCandidate = candidate;
      }

      if ((best == null) || (candidate.distanceKm < best.distanceKm))
      {
        best = candidate;
      }

    }

    RouteCandidate selected = selectRouteCandidate(currentCandidate, nextCandidate, best);

    if (selected == null)
    {
      if (shouldLog())
      {
        log("No route candidate: lock=" + routeLockEnabled + " routeIdx=" + currentRouteIndex);
      }
      return null;
    }

    if (selected.distanceKm > HARD_OFF_ROUTE_DISTANCE_KM)
    {
      offRouteFixCount++;
      if (offRouteFixCount >= HARD_OFF_ROUTE_FIX_COUNT)
      {
        routeLockEnabled = false;
        pendingTurnIndex = -1;
        pendingTurnFixCount = 0;
        log("Route lock disabled: distKm=" + selected.distanceKm + " offCount=" + offRouteFixCount);
        return null;
      }
    }
    else
    {
      offRouteFixCount = 0;
    }

    currentRouteIndex = selected.routeIndex;
    updateMatchedPointHistory(selected.point);

    if (shouldLog())
    {
      log("Route match: seg=" + selected.segment.getID() + " idx=" + selected.routeIndex
          + " distKm=" + selected.distanceKm + " endKm=" + selected.distanceToEndKm
          + " pendingTurnIdx=" + pendingTurnIndex + " pendingFixes=" + pendingTurnFixCount
          + " offCount=" + offRouteFixCount);
    }

    return new MapMatchResult(selected.point, selected.segment, selected.distanceKm);
  }

  private RouteCandidate selectRouteCandidate(final RouteCandidate currentCandidate,
      final RouteCandidate nextCandidate, final RouteCandidate best)
  {
    if (currentRouteIndex < 0)
    {
      pendingTurnIndex = -1;
      pendingTurnFixCount = 0;
      if (shouldLog())
      {
        String bestID = (best == null || best.segment == null) ? "null" : best.segment.getID();
        log("Selecting initial route candidate: idx=-1 bestSeg=" + bestID);
      }
      return best;
    }

    if (currentCandidate == null)
    {
      pendingTurnIndex = -1;
      pendingTurnFixCount = 0;
      if (shouldLog())
      {
        log("Current route segment unavailable; considering next only");
      }
      return nextCandidate;
    }

    if (currentCandidate.distanceToEndKm > INTERSECTION_ZONE_KM)
    {
      pendingTurnIndex = -1;
      pendingTurnFixCount = 0;
      if (shouldLog())
      {
        log("Stay current (not in intersection zone): currentSeg=" + currentCandidate.segment.getID()
            + " endKm=" + currentCandidate.distanceToEndKm);
      }
      return currentCandidate;
    }

    if (nextCandidate == null)
    {
      pendingTurnIndex = -1;
      pendingTurnFixCount = 0;
      if (shouldLog())
      {
        log("Stay current (no next candidate): currentSeg=" + currentCandidate.segment.getID());
      }
      return currentCandidate;
    }

    if (!isTransitionAllowed(currentRouteIndex, currentRouteIndex + 1, currentCandidate))
    {
      pendingTurnIndex = -1;
      pendingTurnFixCount = 0;
      if (shouldLog())
      {
        log("Stay current (turn gate blocked): currentSeg=" + currentCandidate.segment.getID()
            + " nextSeg=" + nextCandidate.segment.getID() + " endKm="
            + currentCandidate.distanceToEndKm);
      }
      return currentCandidate;
    }

    if (!headingSupportsTurn(currentCandidate, nextCandidate))
    {
      pendingTurnIndex = -1;
      pendingTurnFixCount = 0;
      if (shouldLog())
      {
        log("Stay current (heading rejected): currentSeg=" + currentCandidate.segment.getID()
            + " nextSeg=" + nextCandidate.segment.getID());
      }
      return currentCandidate;
    }

    if ((currentCandidate.distanceKm - nextCandidate.distanceKm) < TURN_DISTANCE_GAIN_KM)
    {
      pendingTurnIndex = -1;
      pendingTurnFixCount = 0;
      if (shouldLog())
      {
        log("Stay current (distance gain too small): currentDist=" + currentCandidate.distanceKm
            + " nextDist=" + nextCandidate.distanceKm + " gain="
            + (currentCandidate.distanceKm - nextCandidate.distanceKm));
      }
      return currentCandidate;
    }

    if (pendingTurnIndex != nextCandidate.routeIndex)
    {
      pendingTurnIndex = nextCandidate.routeIndex;
      pendingTurnFixCount = 1;
      if (shouldLog())
      {
        log("Turn pending started: fromIdx=" + currentRouteIndex + " toIdx=" + nextCandidate.routeIndex);
      }
      return currentCandidate;
    }

    pendingTurnFixCount++;
    if (pendingTurnFixCount < TURN_COMMIT_FIX_COUNT)
    {
      if (shouldLog())
      {
        log("Turn pending progress: toIdx=" + pendingTurnIndex + " fixes=" + pendingTurnFixCount
            + "/" + TURN_COMMIT_FIX_COUNT);
      }
      return currentCandidate;
    }

    pendingTurnIndex = -1;
    pendingTurnFixCount = 0;
    log("Turn committed: newIdx=" + nextCandidate.routeIndex + " seg=" + nextCandidate.segment.getID());
    return nextCandidate;
  }

  private boolean headingSupportsTurn(final RouteCandidate currentCandidate,
      final RouteCandidate nextCandidate)
  {
    if (currentCandidate == null || nextCandidate == null)
    {
      return false;
    }

    double routeDot = currentCandidate.dirX * nextCandidate.dirX
        + currentCandidate.dirY * nextCandidate.dirY;

    if (routeDot >= 0.85)
    {
      return true;
    }

    if (previousMatchedPoint == null || lastMatchedPoint == null)
    {
      return false;
    }

    double headingX = lastMatchedPoint[0] - previousMatchedPoint[0];
    double headingY = lastMatchedPoint[1] - previousMatchedPoint[1];
    double headingLength = Math.sqrt(headingX * headingX + headingY * headingY);

    if (headingLength < MIN_HEADING_MOVEMENT_KM)
    {
      return false;
    }

    headingX /= headingLength;
    headingY /= headingLength;

    double cosCurrent = headingX * currentCandidate.dirX + headingY * currentCandidate.dirY;
    double cosNext = headingX * nextCandidate.dirX + headingY * nextCandidate.dirY;

    return (cosNext >= HEADING_MIN_COS) && (cosNext >= cosCurrent + HEADING_ADVANTAGE_COS);
  }

  private void updateMatchedPointHistory(final double[] point)
  {
    if (point == null || point.length != 2)
    {
      return;
    }

    if (lastMatchedPoint == null)
    {
      lastMatchedPoint = new double[] {point[0], point[1]};
      return;
    }

    previousMatchedPoint = lastMatchedPoint;
    lastMatchedPoint = new double[] {point[0], point[1]};
  }

  private boolean isTransitionAllowed(final int currentIndex, final int candidateIndex,
      final RouteCandidate currentCandidate)
  {
    if (currentCandidate == null)
    {
      return false;
    }

    if (candidateIndex > currentIndex)
    {
      double gate = TURN_GATE_DISTANCE_KM * (candidateIndex - currentIndex);
      return currentCandidate.distanceToEndKm <= gate;
    }
    else if (candidateIndex < currentIndex)
    {
      double gate = TURN_GATE_DISTANCE_KM * (currentIndex - candidateIndex);
      return currentCandidate.alongKm <= gate;
    }

    return true;
  }

  private void attemptRelock(final MapMatchResult fallback)
  {
    if (activeRoute.isEmpty() || fallback == null || fallback.getSegment() == null)
    {
      return;
    }

    if (fallback.getDistanceKm() > RELOCK_DISTANCE_KM)
    {
      return;
    }

    String id = fallback.getSegment().getID();
    if (!activeRouteIDs.contains(id))
    {
      return;
    }

    for (int i = 0; i < activeRoute.size(); i++)
    {
      if (id.equals(activeRoute.get(i).segment.getID()))
      {
        currentRouteIndex = i;
        routeLockEnabled = true;
        offRouteFixCount = 0;
        pendingTurnIndex = -1;
        pendingTurnFixCount = 0;
        log("Route lock re-enabled on segment=" + id + " idx=" + i);
        return;
      }
    }
  }

  private boolean shouldLog()
  {
    return DEBUG_LOGGING && ((fixCounter % DEBUG_LOG_EVERY_N_FIXES) == 0);
  }

  private void log(final String message)
  {
    if (DEBUG_LOGGING)
    {
      System.out.println("[MapMatcher] " + message);
    }
  }

  private MapMatchResult matchAgainstGlobal(final double px, final double py)
  {
    int cellX = toCell(px);
    int cellY = toCell(py);

    Set<Integer> candidateIndexes = collectCandidateIndexes(cellX, cellY);
    if (candidateIndexes.isEmpty())
    {
      return matchAgainstAll(px, py);
    }

    return matchAgainstCandidates(px, py, candidateIndexes);
  }

  private MapMatchResult matchAgainstAll(final double px, final double py)
  {
    double bestDistanceSquared = Double.POSITIVE_INFINITY;
    double[] bestPoint = new double[] {px, py};
    StreetSegment bestSegment = null;

    for (LineSegment2D segment : roadSegments)
    {
      CandidateMatch current = evaluateCandidate(segment, px, py);
      if ((best == null) || (current.distanceKm < best.distanceKm))
      {
        bestDistanceSquared = distanceSquared;
        bestPoint = candidate;
        bestSegment = segment.owner;
      }
    }

    currentSegment = bestSegment;

    return bestPoint;
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
      CandidateMatch current = evaluateCandidate(segment, px, py);
      if ((best == null) || (current.distanceKm < best.distanceKm))
      {
        bestDistanceSquared = distanceSquared;
        bestPoint = candidate;
        bestSegment = segment.owner;
      }
    }

    currentSegment = bestSegment;

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

    int firstFoundRing = -1;

    for (int ring = 1; ring <= maxExpansionRings; ring++)
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

      if (!candidates.isEmpty())
      {
        if (firstFoundRing < 0)
        {
          firstFoundRing = ring;
        }
        else if (ring >= firstFoundRing + 1)
        {
          break;
        }
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

    private ClosestPoint closestPoint(final double px, final double py)
    {
      double abx = bx - ax;
      double aby = by - ay;
      double abLengthSquared = abx * abx + aby * aby;

      if (abLengthSquared == 0.0)
      {
        double dx = ax - px;
        double dy = ay - py;
        return new ClosestPoint(new double[] {ax, ay}, 0.0, Math.sqrt(dx * dx + dy * dy));
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

      double cx = ax + t * abx;
      double cy = ay + t * aby;
      double dx = cx - px;
      double dy = cy - py;

      return new ClosestPoint(new double[] {cx, cy}, t, Math.sqrt(dx * dx + dy * dy));
    }

    private double length()
    {
      double dx = bx - ax;
      double dy = by - ay;
      return Math.sqrt(dx * dx + dy * dy);
    }
  }

  private static class ClosestPoint
  {
    private final double[] point;
    private final double t;
    private final double distanceKm;

    private ClosestPoint(final double[] point, final double t, final double distanceKm)
    {
      this.point = point;
      this.t = t;
      this.distanceKm = distanceKm;
    }
  }

  private static class CandidateMatch
  {
    private final double[] point;
    private final StreetSegment segment;
    private final double distanceKm;

    private CandidateMatch(final double[] point, final StreetSegment segment, final double distanceKm)
    {
      this.point = point;
      this.segment = segment;
      this.distanceKm = distanceKm;
    }
  }

  private static class RouteSegmentMatcher
  {
    private final StreetSegment segment;
    private final List<RouteLinePiece> pieces;
    private final double totalLengthKm;

    private RouteSegmentMatcher(final StreetSegment segment, final List<RouteLinePiece> pieces,
        final double totalLengthKm)
    {
      this.segment = segment;
      this.pieces = pieces;
      this.totalLengthKm = totalLengthKm;
    }

    private static RouteSegmentMatcher fromSegment(final StreetSegment segment)
    {
      Shape shape = segment.getGeographicShape().getShape();
      PathIterator iterator = shape.getPathIterator(null);
      double[] coords = new double[6];

      List<RouteLinePiece> pieces = new ArrayList<RouteLinePiece>();
      double total = 0.0;

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
            LineSegment2D line = new LineSegment2D(lastX, lastY, coords[0], coords[1], segment);
            double len = line.length();
            if (len > 0.0)
            {
              pieces.add(new RouteLinePiece(line, total, len));
              total += len;
            }
          }
          lastX = coords[0];
          lastY = coords[1];
          hasLast = true;
        }
        else if (type == PathIterator.SEG_CLOSE)
        {
          if (hasLast)
          {
            LineSegment2D line = new LineSegment2D(lastX, lastY, startX, startY, segment);
            double len = line.length();
            if (len > 0.0)
            {
              pieces.add(new RouteLinePiece(line, total, len));
              total += len;
            }
          }
        }

        iterator.next();
      }

      if (pieces.isEmpty())
      {
        return null;
      }

      return new RouteSegmentMatcher(segment, pieces, total);
    }

    private RouteCandidate match(final double px, final double py, final int routeIndex)
    {
      RouteCandidate best = null;

      for (RouteLinePiece piece : pieces)
      {
        ClosestPoint cp = piece.line.closestPoint(px, py);
        double alongKm = piece.startOffsetKm + cp.t * piece.lengthKm;
        double distanceToEndKm = Math.max(0.0, totalLengthKm - alongKm);

        double dirX = piece.line.bx - piece.line.ax;
        double dirY = piece.line.by - piece.line.ay;
        double dirLength = Math.sqrt(dirX * dirX + dirY * dirY);
        if (dirLength > 0.0)
        {
          dirX /= dirLength;
          dirY /= dirLength;
        }

        RouteCandidate current = new RouteCandidate(cp.point, segment, routeIndex, cp.distanceKm,
            alongKm, distanceToEndKm, dirX, dirY);

        if ((best == null) || (current.distanceKm < best.distanceKm))
        {
          best = current;
        }
      }

      return best;
    }
  }

  private static class RouteLinePiece
  {
    private final LineSegment2D line;
    private final double startOffsetKm;
    private final double lengthKm;

    private RouteLinePiece(final LineSegment2D line, final double startOffsetKm,
        final double lengthKm)
    {
      this.line = line;
      this.startOffsetKm = startOffsetKm;
      this.lengthKm = lengthKm;
    }
  }

  private static class RouteCandidate
  {
    private final double[] point;
    private final StreetSegment segment;
    private final int routeIndex;
    private final double distanceKm;
    private final double alongKm;
    private final double distanceToEndKm;
    private final double dirX;
    private final double dirY;

    private RouteCandidate(final double[] point, final StreetSegment segment, final int routeIndex,
        final double distanceKm, final double alongKm, final double distanceToEndKm,
        final double dirX, final double dirY)
    {
      this.point = point;
      this.segment = segment;
      this.routeIndex = routeIndex;
      this.distanceKm = distanceKm;
      this.alongKm = alongKm;
      this.distanceToEndKm = distanceToEndKm;
      this.dirX = dirX;
      this.dirY = dirY;
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
