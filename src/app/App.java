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
  private static final String EXIT = "Exit";
  private static final String DESTINATION = "Destination";

  private DynamicCartographyPanel<StreetSegment> panel;
  private CartographyDocument<StreetSegment> document;
  private GeocodeDialog dialog;
  private int mode;
  private JFrame frame;

  private ShortestPathAlgorithm alg;
  private PathFindingWorker task;
  private StreetSegment destinationSegment;
  private StreetNetwork network;

  private Map<String, StreetSegment> currentPath;
  private RouteRecalculator routeRecalculator;
  private MapMatcher matcher;

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

    if (ac.equals(DESTINATION))
    {
      mode = SET_DESTINATION;

      if (!dialog.isVisible())
      {
        dialog.setLocation((int) frame.getBounds().getMaxX(), (int) frame.getBounds().getY());
        dialog.setVisible(true);
      }
    }

    if (ac.equals(CALCULATE))
    {
      calculatePath();
    }

    if (ac.equals(EXIT))
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
      if (showWarnings)
      {
        JOptionPane.showMessageDialog(frame, "Current GPS/map-matched location is not available.",
            "Missing Current Location", JOptionPane.WARNING_MESSAGE);
      }

      return;
    }

    if (destinationSegment == null)
    {
      if (showWarnings)
      {
        JOptionPane.showMessageDialog(frame, "Please select a destination.", "Missing Destination",
            JOptionPane.WARNING_MESSAGE);
      }

      return;
    }

    if (matcher != null)
    {
      matcher.setActiveRoute(Collections.<StreetSegment>emptyList());
    }

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

    BackgroundTaskDialog<Map<String, StreetSegment>, String> btd = new BackgroundTaskDialog<Map<String, StreetSegment>, String>(
        frame, "Calculating...", task);

    btd.execute();
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
    if (evt.getPropertyName().equals("state"))
    {
      if (evt.getNewValue().equals(SwingWorker.StateValue.DONE))
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

      frame = new JFrame("Map");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(600, 600);

      JMenuBar menuBar = new JMenuBar();
      frame.setJMenuBar(menuBar);

      JMenuItem item;
      JMenu menu;

      menu = new JMenu("File");
      menuBar.add(menu);

      item = new JMenuItem(EXIT);
      item.addActionListener(this);
      menu.add(item);

      menu = new JMenu("Geocode");
      menuBar.add(menu);

      item = new JMenuItem(DESTINATION);
      item.addActionListener(this);
      menu.add(item);

      menu = new JMenu("Path");
      menuBar.add(menu);

      item = new JMenuItem(CALCULATE);
      item.addActionListener(this);
      menu.add(item);

      frame.setContentPane(panel);

      Geocoder geocoder = new Geocoder(geographicShapes, document, streets);
      dialog = new GeocodeDialog(frame, geocoder);
      dialog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      dialog.addStreetSegmentObserver(this);
      dialog.setLocation((int) frame.getBounds().getMaxX(), (int) frame.getBounds().getY());

      // Find the right serial port
      SerialPort[] ports = SerialPort.getCommPorts();
      String gpsPath = null;
      for (SerialPort port : ports)
      {
        String description = port.getPortDescription();
        String path = port.getSystemPortPath();

        if (description.indexOf("GPS") >= 0)
        {
          gpsPath = path;
        }
      }

      // Setup the serial port
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

      GPSReaderTask gpsReader = new GPSReaderTask(is, "GPGGA");
      gpsReader.addGPSObserver(panel);

      frame.setVisible(true);
      gpsReader.execute();

      routeRecalculator.start(event -> {
        StreetSegment currentSegment = panel.getCurrentSegment();

        if (routeRecalculator.shouldRecalculate(currentSegment))
        {
          System.out.println("Off route. Recalculating...");
          routeRecalculator.resetOffRouteCount();
          calculatePathFrom(currentSegment, false);
        }
      });
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

    if (segmentIDs.size() > 0)
    {
      if (mode == SET_DESTINATION)
      {
        destinationSegment = highlighted.get(segmentIDs.get(0));
        routeRecalculator.setDestinationSegment(destinationSegment);
        if (matcher != null)
        {
          matcher.setActiveRoute(Collections.<StreetSegment>emptyList());
          System.out.println("[App] Destination changed. Cleared active route lock.");
        }
        System.out.println("Destination: " + destinationSegment);
      }
    }
  }

  private List<StreetSegment> buildOrderedPath(final Map<String, StreetSegment> path,
      final StreetSegment originSegment, final StreetSegment destinationSegment)
  {
    List<StreetSegment> ordered = new ArrayList<StreetSegment>();

    if (path == null || path.isEmpty())
    {
      return ordered;
    }

    Map<Integer, List<StreetSegment>> byTail = new HashMap<Integer, List<StreetSegment>>();
    Set<String> pathIDs = new HashSet<String>();

    for (StreetSegment segment : path.values())
    {
      if (segment == null)
      {
        continue;
      }

      pathIDs.add(segment.getID());
      List<StreetSegment> outgoing = byTail.get(segment.getTail());
      if (outgoing == null)
      {
        outgoing = new ArrayList<StreetSegment>();
        byTail.put(segment.getTail(), outgoing);
      }
      outgoing.add(segment);
    }

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
      for (StreetSegment candidate : path.values())
      {
        if (candidate == null)
        {
          continue;
        }

        boolean hasPredecessor = false;
        for (StreetSegment other : path.values())
        {
          if (other != null && other.getHead() == candidate.getTail())
          {
            hasPredecessor = true;
            break;
          }
        }

        if (!hasPredecessor)
        {
          start = candidate;
          break;
        }
      }
    }

    if (start == null)
    {
      start = path.values().iterator().next();
    }

    StreetSegment current = start;
    Set<String> visited = new HashSet<String>();

    while (current != null && !visited.contains(current.getID()) && pathIDs.contains(current.getID()))
    {
      ordered.add(current);
      visited.add(current.getID());

      if (destinationSegment != null && current.getID().equals(destinationSegment.getID()))
      {
        break;
      }

      List<StreetSegment> outgoing = byTail.get(current.getHead());
      StreetSegment next = null;

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

      current = next;
    }

    if (ordered.isEmpty())
    {
      ordered.addAll(path.values());
    }

    return ordered;
  }
}
