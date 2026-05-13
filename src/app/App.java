package app;

import geography.*;
import graph.*;
import gui.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

import com.fazecast.jSerialComm.SerialPort;

import dataprocessing.Geocoder;
import feature.*;
import gps.*;

/**
 * The application for PA6.
 *
 * @author Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public class App implements Runnable, ActionListener, StreetSegmentObserver, PropertyChangeListener
{
  private static final int SET_DESTINATION = 0;

  private static final String CALCULATE = "Calculate";
  private static final String DESTINATION = "Destination";
  private static final String EMPTY_ROUTE_MESSAGE = "[App] Cleared active route lock.";
  private static final String EXIT = "Exit";
  private static final String GPGGA = "GPGGA";
  private static final String GPS = "GPS";
  private static final String MISSING_CURRENT_LOCATION = "Missing Current Location";
  private static final String MISSING_DESTINATION = "Missing Destination";

  private CartographyDocument<StreetSegment> document;
  private GeocodeDialog dialog;
  private JFrame frame;
  private MapMatcher matcher;
  private DynamicCartographyPanel<StreetSegment> panel;
  private RouteRecalculator routeRecalculator;
  private StreetNetwork network;

  private Map<String, StreetSegment> currentPath;
  private PathFindingWorker task;
  private ShortestPathAlgorithm alg;
  private StreetSegment destinationSegment;
  private int mode;

  /**
   * Handle actionPerformed() messages.
   *
   * @param evt
   *          The event that generated the message
   */
  @Override
  public void actionPerformed(final ActionEvent evt)
  {
    String ac = evt.getActionCommand();

    if (DESTINATION.equals(ac))
    {
      mode = SET_DESTINATION;

      if (!dialog.isVisible())
      {
        dialog.setLocation((int) frame.getBounds().getMaxX(), (int) frame.getBounds().getY());
        dialog.setVisible(true);
      }
    }
    else if (CALCULATE.equals(ac))
    {
      calculatePath();
    }
    else if (EXIT.equals(ac))
    {
      exit();
    }
  }

  /**
   * Handle propertyChange() messages.
   *
   * @param evt
   *          The event that generated the message
   */
  @Override
  public void propertyChange(final PropertyChangeEvent evt)
  {
    if ("state".equals(evt.getPropertyName())
        && SwingWorker.StateValue.DONE.equals(evt.getNewValue()))
    {
      handleFinishedPathTask();
    }
  }

  /**
   * The code to be executed in the event dispatch thread.
   */
  @Override
  public void run()
  {
    try
    {
      InputStream isgeo = new FileInputStream(new File("rockingham-streets-2024.geo"));
      AbstractMapProjection proj = new ConicalEqualAreaProjection(-96.0, 37.5, 29.5, 45.5);
      GeographicShapesReader gsReader = new GeographicShapesReader(isgeo, proj);
      CartographyDocument<GeographicShape> geographicShapes = gsReader.read();
      System.out.println("Read the .geo file");

      InputStream iss = new FileInputStream(new File("rockingham-streets-2024.str"));
      StreetsReader sReader = new StreetsReader(iss, geographicShapes);
      Map<String, Street> streets = new HashMap<String, Street>();
      document = sReader.read(streets);
      System.out.println("Read the .str file");

      network = StreetNetwork.createStreetNetwork(streets);
      routeRecalculator = new RouteRecalculator();
      matcher = new MapMatcher(document);

      panel = new DynamicCartographyPanel<StreetSegment>(document, new StreetSegmentCartographer(),
          proj, matcher);

      setupFrame();
      setupGeocodeDialog(geographicShapes, streets);
      setupGpsReader();
      startRouteRecalculator();
    }
    catch (IOException ioe)
    {
      JOptionPane.showMessageDialog(frame, ioe.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Handle a collection of StreetSegment objects.
   *
   * @param segmentIDs
   *          The IDs of the StreetSegment objects
   */
  @Override
  public void handleStreetSegments(final List<String> segmentIDs)
  {
    HashMap<String, StreetSegment> highlighted = new HashMap<String, StreetSegment>();

    for (String id : segmentIDs)
    {
      highlighted.put(id, document.getElement(id));
    }

    document.setHighlighted(highlighted);
    panel.repaint();

    if (!segmentIDs.isEmpty() && mode == SET_DESTINATION)
    {
      destinationSegment = highlighted.get(segmentIDs.get(0));
      routeRecalculator.setDestinationSegment(destinationSegment);
      clearActiveRoute();
      System.out.println("Destination: " + destinationSegment);
    }
  }

  /**
   * Calculates the path from the current GPS/map-matched location to the selected destination.
   */
  private void calculatePath()
  {
    calculatePathFrom(panel.getCurrentSegment(), true);
  }

  /**
   * Calculates a path from the given origin segment to the selected destination.
   *
   * @param originSegment
   *          The origin segment
   * @param showWarnings
   *          Whether to show warning dialogs
   */
  private void calculatePathFrom(final StreetSegment originSegment, final boolean showWarnings)
  {
    if (task != null)
    {
      return;
    }

    if (originSegment == null)
    {
      showWarning(showWarnings, "Current GPS/map-matched location is not available.",
          MISSING_CURRENT_LOCATION);
      return;
    }

    if (destinationSegment == null)
    {
      showWarning(showWarnings, "Please select a destination.", MISSING_DESTINATION);
      return;
    }

    clearActiveRoute();

    PermanentLabelManager labels = new PermanentLabelBuckets(network.size());
    alg = new LabelSettingAlgorithm(labels);

    task = new PathFindingWorker(alg, originSegment.getHead(), destinationSegment.getHead(),
        network, document, panel);
    task.addPropertyChangeListener(this);
    task.shouldShowIntermediateResults(false);

    if (dialog != null)
    {
      dialog.setVisible(false);
    }

    BackgroundTaskDialog<Map<String, StreetSegment>, String> btd =
        new BackgroundTaskDialog<>(frame, "Calculating...", task);

    btd.execute();
  }

  /**
   * Clears the active route in the matcher.
   */
  private void clearActiveRoute()
  {
    if (matcher != null)
    {
      matcher.setActiveRoute(Collections.<StreetSegment> emptyList());
      System.out.println(EMPTY_ROUTE_MESSAGE);
    }
  }

  /**
   * Exits the application.
   */
  private void exit()
  {
    if (routeRecalculator != null)
    {
      routeRecalculator.stop();
    }

    if (dialog != null)
    {
      dialog.dispose();
    }

    frame.dispose();
    System.exit(0);
  }

  /**
   * Handles the completed path-finding task.
   */
  private void handleFinishedPathTask()
  {
    try
    {
      currentPath = task.get();
      routeRecalculator.setCurrentPath(currentPath);

      if (matcher != null)
      {
        List<StreetSegment> orderedPath = buildOrderedPath(currentPath, panel.getCurrentSegment(),
            destinationSegment);
        matcher.setActiveRoute(orderedPath);
        System.out.println("[App] Route path size=" + currentPath.size() + " ordered="
            + orderedPath.size() + " lock=" + matcher.isRouteLockEnabled());
      }

      document.setHighlighted(currentPath);
      panel.repaint();
      task = null;
    }
    catch (InterruptedException | ExecutionException e)
    {
      JOptionPane.showMessageDialog(frame, "Could not calculate path.", "Exception",
          JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  /**
   * Shows a warning message if warnings are enabled.
   *
   * @param showWarnings
   *          true to show the warning
   * @param message
   *          warning message
   * @param title
   *          warning title
   */
  private void showWarning(final boolean showWarnings, final String message, final String title)
  {
    if (showWarnings)
    {
      JOptionPane.showMessageDialog(frame, message, title, JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * Sets up the main application frame.
   */
  private void setupFrame()
  {
    frame = new JFrame("Map");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(600, 600);

    JMenuBar menuBar = new JMenuBar();
    frame.setJMenuBar(menuBar);

    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);

    JMenuItem exitItem = new JMenuItem(EXIT);
    exitItem.addActionListener(this);
    fileMenu.add(exitItem);

    JMenu geocodeMenu = new JMenu("Geocode");
    menuBar.add(geocodeMenu);

    JMenuItem destinationItem = new JMenuItem(DESTINATION);
    destinationItem.addActionListener(this);
    geocodeMenu.add(destinationItem);

    JMenu pathMenu = new JMenu("Path");
    menuBar.add(pathMenu);

    JMenuItem calculateItem = new JMenuItem(CALCULATE);
    calculateItem.addActionListener(this);
    pathMenu.add(calculateItem);

    frame.setContentPane(panel);
  }

  /**
   * Sets up the geocode dialog.
   *
   * @param geographicShapes
   *          geographic shapes document
   * @param streets
   *          street map
   */
  private void setupGeocodeDialog(final CartographyDocument<GeographicShape> geographicShapes,
      final Map<String, Street> streets)
  {
    Geocoder geocoder = new Geocoder(geographicShapes, document, streets);
    dialog = new GeocodeDialog(frame, geocoder);
    dialog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    dialog.addStreetSegmentObserver(this);
    dialog.setLocation((int) frame.getBounds().getMaxX(), (int) frame.getBounds().getY());
  }

  /**
   * Sets up and starts the GPS reader.
   *
   * @throws IOException
   *           if the GPS input stream cannot be opened
   */
  private void setupGpsReader() throws IOException
  {
    String gpsPath = findGpsPath();

    // Setup the serial port.
    // SerialPort gps = SerialPort.getCommPort(gpsPath);
    // gps.openPort();
    // gps.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
    // InputStream is = gps.getInputStream();

    // Use GPS simulator for testing instead.
    GPSSimulator gpsSim = new GPSSimulator("rockingham.gps");
    gpsSim.setDelay(20);

    InputStream is = gpsSim.getInputStream();

    if (is == null)
    {
      throw new IOException("Could not open GPS simulator input stream.");
    }

    GPSReaderTask gpsReader = new GPSReaderTask(is, GPGGA);
    gpsReader.addGPSObserver(panel);

    frame.setVisible(true);
    gpsReader.execute();

    if (gpsPath != null)
    {
      System.out.println("GPS path: " + gpsPath);
    }
  }

  /**
   * Finds the system path for the GPS serial port.
   *
   * @return GPS serial port path, or null if unavailable
   */
  private String findGpsPath()
  {
    String gpsPath = null;
    SerialPort[] ports = SerialPort.getCommPorts();

    for (SerialPort port : ports)
    {
      String description = port.getPortDescription();
      String path = port.getSystemPortPath();

      if (description.indexOf(GPS) >= 0)
      {
        gpsPath = path;
      }
    }

    return gpsPath;
  }

  /**
   * Starts the route recalculation timer.
   */
  private void startRouteRecalculator()
  {
    routeRecalculator.start(new ActionListener()
    {
      /**
       * Handles route recalculation timer ticks.
       *
       * @param event
       *          timer event
       */
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        StreetSegment currentSegment = panel.getCurrentSegment();

        if (routeRecalculator.shouldRecalculate(currentSegment))
        {
          System.out.println("Off route. Recalculating...");
          routeRecalculator.resetOffRouteCount();
          calculatePathFrom(currentSegment, false);
        }
      }
    });
  }

  /**
   * Builds an ordered path from a path map.
   *
   * @param path
   *          path map
   * @param originSegment
   *          origin segment
   * @param targetSegment
   *          destination segment
   * @return ordered path
   */
  private List<StreetSegment> buildOrderedPath(final Map<String, StreetSegment> path,
      final StreetSegment originSegment, final StreetSegment targetSegment)
  {
    List<StreetSegment> ordered = new ArrayList<StreetSegment>();

    if (path == null || path.isEmpty())
    {
      return ordered;
    }

    Map<Integer, List<StreetSegment>> byTail = new HashMap<Integer, List<StreetSegment>>();
    Set<String> pathIDs = new HashSet<String>();

    fillPathLookups(path, byTail, pathIDs);

    StreetSegment start = findStartSegment(path, byTail, originSegment);
    addOrderedSegments(ordered, pathIDs, byTail, start, targetSegment);

    if (ordered.isEmpty())
    {
      ordered.addAll(path.values());
    }

    return ordered;
  }

  /**
   * Adds path segments to lookup collections.
   *
   * @param path
   *          path map
   * @param byTail
   *          segments grouped by tail
   * @param pathIDs
   *          path segment IDs
   */
  private void fillPathLookups(final Map<String, StreetSegment> path,
      final Map<Integer, List<StreetSegment>> byTail, final Set<String> pathIDs)
  {
    for (StreetSegment segment : path.values())
    {
      if (segment != null)
      {
        pathIDs.add(segment.getID());

        List<StreetSegment> outgoing = byTail.get(segment.getTail());
        if (outgoing == null)
        {
          outgoing = new ArrayList<StreetSegment>();
          byTail.put(segment.getTail(), outgoing);
        }

        outgoing.add(segment);
      }
    }
  }

  /**
   * Finds the first segment to use when ordering a path.
   *
   * @param path
   *          path map
   * @param byTail
   *          segments grouped by tail
   * @param originSegment
   *          origin segment
   * @return start segment
   */
  private StreetSegment findStartSegment(final Map<String, StreetSegment> path,
      final Map<Integer, List<StreetSegment>> byTail, final StreetSegment originSegment)
  {
    StreetSegment start = null;

    if (originSegment != null)
    {
      List<StreetSegment> outgoing = byTail.get(originSegment.getHead());
      if (outgoing != null && !outgoing.isEmpty())
      {
        start = outgoing.get(0);
      }
    }

    if (start == null)
    {
      start = findSegmentWithoutPredecessor(path);
    }

    if (start == null)
    {
      start = path.values().iterator().next();
    }

    return start;
  }

  /**
   * Finds a path segment without a predecessor.
   *
   * @param path
   *          path map
   * @return segment without a predecessor, or null if unavailable
   */
  private StreetSegment findSegmentWithoutPredecessor(final Map<String, StreetSegment> path)
  {
    StreetSegment start = null;

    for (StreetSegment candidate : path.values())
    {
      if (candidate != null && !hasPredecessor(candidate, path))
      {
        start = candidate;
        break;
      }
    }

    return start;
  }

  /**
   * Determines whether a segment has a predecessor in a path.
   *
   * @param candidate
   *          candidate segment
   * @param path
   *          path map
   * @return true if the segment has a predecessor
   */
  private boolean hasPredecessor(final StreetSegment candidate,
      final Map<String, StreetSegment> path)
  {
    boolean result = false;

    for (StreetSegment other : path.values())
    {
      if (other != null && other.getHead() == candidate.getTail())
      {
        result = true;
        break;
      }
    }

    return result;
  }

  /**
   * Adds ordered segments to a list.
   *
   * @param ordered
   *          ordered path
   * @param pathIDs
   *          path segment IDs
   * @param byTail
   *          segments grouped by tail
   * @param start
   *          start segment
   * @param targetSegment
   *          destination segment
   */
  private void addOrderedSegments(final List<StreetSegment> ordered, final Set<String> pathIDs,
      final Map<Integer, List<StreetSegment>> byTail, final StreetSegment start,
      final StreetSegment targetSegment)
  {
    StreetSegment current = start;
    Set<String> visited = new HashSet<String>();

    while (current != null && !visited.contains(current.getID())
        && pathIDs.contains(current.getID()))
    {
      ordered.add(current);
      visited.add(current.getID());

      if (targetSegment != null && current.getID().equals(targetSegment.getID()))
      {
        current = null;
      }
      else
      {
        current = findNextSegment(byTail, visited, current);
      }
    }
  }

  /**
   * Finds the next unvisited segment after the current segment.
   *
   * @param byTail
   *          segments grouped by tail
   * @param visited
   *          visited segment IDs
   * @param current
   *          current segment
   * @return next segment, or null if unavailable
   */
  private StreetSegment findNextSegment(final Map<Integer, List<StreetSegment>> byTail,
      final Set<String> visited, final StreetSegment current)
  {
    StreetSegment next = null;
    List<StreetSegment> outgoing = byTail.get(current.getHead());

    if (outgoing != null)
    {
      for (StreetSegment candidate : outgoing)
      {
        if (candidate != null && !visited.contains(candidate.getID()))
        {
          next = candidate;
          break;
        }
      }
    }

    return next;
  }
}
