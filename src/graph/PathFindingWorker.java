package graph;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;

import feature.StreetSegment;
import feature.StreetSegmentObserver;
import gui.*;

/**
 * A SwingWorker that uses a path finding algorithm to find a shortest path in
 * a worker thread.
 * 
 * @author Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public class PathFindingWorker extends SwingWorker<Map<String, StreetSegment>, String>
    implements StreetSegmentObserver
{
  private int destination, origin;
  private ShortestPathAlgorithm alg;
  private StreetNetwork net;
  private CartographyDocument<StreetSegment> document;
  private CartographyPanel<StreetSegment> panel;
  
  /**
   * Explicit Value Constructor.
   * 
   * @param alg The algorithm to use
   * @param origin The origin Intersection
   * @param destination The destination Intersection
   * @param net The StreetNetwork
   * @param document The CartographyDocument
   * @param panel The CartographyPanel
   */
  public PathFindingWorker(final ShortestPathAlgorithm alg, 
      final int origin, final int destination, final StreetNetwork net,
      final CartographyDocument<StreetSegment> document, 
      final CartographyPanel<StreetSegment> panel)
  {
    this.alg = alg;
    this.origin = origin;
    this.destination = destination;
    this.net = net;
    this.document = document;
    this.panel = panel;
  }
  
  /**
   * The code to execute in the worker thread. 
   */
  @Override
  public Map<String, StreetSegment> doInBackground()
  {
    return alg.findPath(origin,  destination, net);
  }
  
  /**
   * Handle a collection of StreetSegment objects.
   * 
   * @param segmentIDs The IDs of the StreetSegment objects
   */
  @Override
  public void handleStreetSegments(final List<String> segmentIDs)
  {
    publish(segmentIDs.toArray(new String[segmentIDs.size()]));
  }
  
  /**
   * Handle intermediate results.
   */
  @Override
  public void process(final List<String> segmentIDs)
  {
    Map<String, StreetSegment> highlighted = new HashMap<String, StreetSegment>();
    for (String id: segmentIDs) highlighted.put(id, document.getElement(id));
    
    document.setHighlighted(highlighted);
    panel.repaint();
  }
  
  /**
   * Determines whether intermediate results should be displayed
   * on the map.
   * 
   * @param show true to show intermediate results; false otherwise
   */
  public void shouldShowIntermediateResults(final boolean show)
  {
    if (show) alg.addStreetSegmentObserver(this);
    else alg.removeStreetSegmentObserver(this);

  }

}
