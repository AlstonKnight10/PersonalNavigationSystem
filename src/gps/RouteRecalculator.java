package gps;

import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.Timer;

import feature.StreetSegment;

/**
 * Decides when the route should be recalculated.
 */
public class RouteRecalculator
{
  private static final int OFF_ROUTE_LIMIT = 3;
  private static final int CHECK_DELAY_MS = 1000;

  private Map<String, StreetSegment> currentPath;
  private StreetSegment destinationSegment;
  private int offRouteCount;
  private Timer timer;

  /**
   * Constructs a RouteRecalculator.
   */
  public RouteRecalculator()
  {
    this.currentPath = null;
    this.destinationSegment = null;
    this.offRouteCount = 0;
    this.timer = null;
  }

  /**
   * Sets the destination segment.
   *
   * @param destinationSegment
   *          The destination segment
   */
  public void setDestinationSegment(final StreetSegment destinationSegment)
  {
    this.destinationSegment = destinationSegment;
    this.currentPath = null;
    this.offRouteCount = 0;
  }

  /**
   * Gets the destination segment.
   *
   * @return The destination segment
   */
  public StreetSegment getDestinationSegment()
  {
    return destinationSegment;
  }

  /**
   * Sets the current path.
   *
   * @param currentPath
   *          The current path
   */
  public void setCurrentPath(final Map<String, StreetSegment> currentPath)
  {
    this.currentPath = currentPath;
    this.offRouteCount = 0;
  }

  /**
   * Gets the current path.
   *
   * @return The current path
   */
  public Map<String, StreetSegment> getCurrentPath()
  {
    return currentPath;
  }

  /**
   * Determines whether the route should be recalculated.
   *
   * @param currentSegment
   *          The current street segment
   * @return true if the route should be recalculated
   */
  public boolean shouldRecalculate(final StreetSegment currentSegment)
  {
    if (currentSegment == null || destinationSegment == null)
    {
      return false;
    }

    if (currentPath == null || currentPath.isEmpty())
    {
      return true;
    }

    if (currentPath.containsKey(currentSegment.getID()))
    {
      offRouteCount = 0;
      return false;
    }

    offRouteCount++;
    return offRouteCount >= OFF_ROUTE_LIMIT;
  }

  /**
   * Resets the off-route counter.
   */
  public void resetOffRouteCount()
  {
    offRouteCount = 0;
  }

  /**
   * Starts periodically checking whether the route needs recalculation.
   *
   * @param listener
   *          The action to run every timer tick
   */
  public void start(final ActionListener listener)
  {
    stop();

    timer = new Timer(CHECK_DELAY_MS, listener);
    timer.start();
  }

  /**
   * Stops checking for route recalculation.
   */
  public void stop()
  {
    if (timer != null)
    {
      timer.stop();
      timer = null;
    }
  }
}
