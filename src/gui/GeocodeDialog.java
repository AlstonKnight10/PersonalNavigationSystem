package gui;

import dataprocessing.*;
import feature.Street;
import feature.StreetSegmentObserver;
import feature.StreetSegmentSubject;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A dialog box for a Geocoder.
 * 
 * @author Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public class GeocodeDialog extends JDialog implements 
    ActionListener, ListSelectionListener, StreetSegmentSubject
{
  // Constants with package visibility
  static final String CATEGORY       = "Category";
  static final String GEOCODE        = "Geocode";
  static final String NAME           = "Name";
  static final String NUMBER         = "Number";
  static final String PREFIX         = "Prefix";
  static final String SUFFIX         = "Suffix";

  // Constants with private visibility
  private static final String QUOTE = "\"";
  private static final long serialVersionUID = 1L;

  // Attributes
  private List<String> ids;
  private List<StreetSegmentObserver> observers;
  
  private Geocoder geocoder;
  private JButton geocodeButton;
  private JComboBox<String> categoryField, prefixField, suffixField;
  private JList<String> resultsArea;
  private JTextField nameField, numberField;

  /**
   * Explicit Value Constructor.
   * 
   * @param owner The owning JFrame
   * @param geocoder The Geocoder
   */
  public GeocodeDialog(final JFrame owner, final Geocoder geocoder)
  {
    super(owner, "Geocoder", false);
    this.geocoder = geocoder;
    
    observers = new ArrayList<StreetSegmentObserver>();
    
    numberField   = createJTextField();
    prefixField   = createJComboBox(null);
    nameField     = createJTextField();
    categoryField = createJComboBox(null);
    suffixField   = createJComboBox(null);
    
    geocodeButton = new JButton(GEOCODE);
    geocodeButton.addActionListener(this);
    
    resultsArea = new JList<String>(new DefaultListModel<String>());
    resultsArea.addListSelectionListener(this);
    
    String[] directions = new String[] {"E","N","NE","NW","S","SE","SW","W"};
    populate(prefixField, directions);
    populate(categoryField, new String[] {"Aly","Arc","Ave","Blvd","Br","Brg","Byp","Cir",
        "Cres","Cswy","Ct","Ctr","Cv","Dr","Expy","Fwy","Grd","Hwy","Ln","Loop","Mal","Pass",
        "Path","Pike","Pky","Pl","Plz","Ramp","Rd","Row","Rte","Rue","Run","Spur","Sq","St",
        "Ter","Tpke","Trce","Trl","Tunl","Walk","Way","Xing"});
    populate(suffixField, directions);
    

    performLayout();
    setSize(800, 400);
  }
  
  /**
   * Handle actionPerformed() messages.
   * 
   * @param evt The event that generated the message.
   */
  public void actionPerformed(final ActionEvent evt)
  {
    DefaultListModel<String> listModel = (DefaultListModel<String>)resultsArea.getModel();
    listModel.clear();
    
    String prefix = prefixField.getItemAt(prefixField.getSelectedIndex());
    String name = nameField.getText();
    String category = categoryField.getItemAt(categoryField.getSelectedIndex());
    String suffix = suffixField.getItemAt(suffixField.getSelectedIndex());
    int number = 0;
    try
    {
      number = Integer.parseInt(numberField.getText());
      String canonicalName = Street.createCanonicalName(prefix, name, category, suffix);
      ids = new ArrayList<String>();
      List<double[]> locations = geocoder.geocode(canonicalName, number, ids);
      List<String> lonlat = new ArrayList<String>();
      for (double[] location: locations) lonlat.add(location[0] + "," + location[1]);
      displayResults(lonlat);
    }
    catch (NumberFormatException nfe)
    {
      JOptionPane.showMessageDialog(this, QUOTE+numberField.getText()+QUOTE
          + " is not a valid street number!",
          "Input Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Add a StreetSegmentObserver to this subject.
   * 
   * @param observer The observer
   */
  @Override
  public void addStreetSegmentObserver(final StreetSegmentObserver observer)
  {
    observers.add(observer);
  }
  
  
  /**
   * Create a JPanel containing a Jcomponent and a titled-border.
   *
   * @param title      The title
   * @param component  The JComponent
   * @return The JPanel
   */
  private JPanel createTitledComponent(final String title, final JComponent component)
  {
    JPanel result = new JPanel();
    result.setLayout(new BorderLayout());
    result.add(component, BorderLayout.CENTER);
    result.setBorder(BorderFactory.createTitledBorder(title));
    
    return result;
  }


  /**
   * Create a JComboBox.
   * 
   * @param items  The items to include (after the blank)
   * @return The JComboBox
   */
  private JComboBox<String> createJComboBox(final String[] items)
  {
    JComboBox<String> result = new JComboBox<String>();
    result.setEditable(false);
    populate(result, items);
 
    return result;
  }

  /**
   * Create a JTextField.
   * 
   * @return The JTextField
   */
  private JTextField createJTextField()
  {
    JTextField    result = new JTextField("", 6);
    return result;
  }

  /**
   * Display result.
   * 
   * @param results The results to display
   */
  private void displayResults(final List<String> results)
  {
    DefaultListModel<String> model = (DefaultListModel<String>)resultsArea.getModel();
    model.clear();
    if (results != null)
    {
      for (String line: results) model.addElement(line);
    }
  }

  /**
   * Notify all of the StreetSegmentObserver objects of a 
   * handleStreetSegment() message.
   * 
   * @param segmendIDs The IDs of the StreetSegment objects
   */
  private void notifyStreetSegmentObservers(final List<String> segmendIDs)
  {
    for (StreetSegmentObserver observer: observers) observer.handleStreetSegments(segmendIDs);
  }
  
  /**
   * Layout this Component.
   * 
   */
  private void performLayout()
  {
    JPanel contentPane = (JPanel)getContentPane();

    contentPane.setLayout(new BorderLayout());

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.addSeparator();
    toolBar.add(geocodeButton);
    contentPane.add(toolBar, BorderLayout.SOUTH);

    Row entryPanel = new Row();
    entryPanel.add(createTitledComponent(NUMBER, numberField),     1,   0.0);
    entryPanel.add(createTitledComponent(PREFIX, prefixField),     1,   0.0);
    entryPanel.add(createTitledComponent(NAME, nameField),         3, 100.0);
    entryPanel.add(createTitledComponent(CATEGORY, categoryField), 1,   0.0);
    entryPanel.add(createTitledComponent(SUFFIX, suffixField),     1,   0.0);


    JPanel center = new JPanel();
    center.setLayout(new BorderLayout());
    center.add(entryPanel, BorderLayout.NORTH);
    JScrollPane sp = new JScrollPane(resultsArea);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    center.add(sp,  BorderLayout.CENTER);

    contentPane.add(center, BorderLayout.CENTER);
  }
  
  /**
   * Populate a JComboBox.
   * 
   * @param comboBox The JComboBox to populate
   * @param items    The items to populate it with (or null to reset)
   */
  private void populate(final JComboBox<String> comboBox, final String[] items)
  {
    comboBox.removeAllItems();
    comboBox.addItem("");
    if (items != null)
    {
      Arrays.sort(items);
      for (String item: items) comboBox.addItem(item);
    }
  }

  /**
   * Remove a StreetSegmentObserver from this subject.
   * 
   * @param observer The observer
   */
  @Override
  public void removeStreetSegmentObserver(final StreetSegmentObserver observer)
  {
    observers.remove(observer);
  }

  /**
   * Set the selection mode (e.g., ListSelectionModel.SINGLE_SELECTION) for the JList.
   * 
   * @param mode The selection mode 
   */
  public void setSelectionMode(final int mode)
  {
    resultsArea.setSelectionMode(mode);
  }
  
  /**
   * Handle valueChanged() messages.
   * 
   * @param evt The event that generated the message
   */
  @Override
  public void valueChanged(final ListSelectionEvent evt)
  {
    if (!evt.getValueIsAdjusting())
    {
      int indexes[] = resultsArea.getSelectedIndices();
      List<String> selectedIDs = new ArrayList<String>();
      for (int i=0; i<indexes.length; i++)
      {
        selectedIDs.add(ids.get(indexes[i]));
      }
      notifyStreetSegmentObservers(selectedIDs);
    }
  }
}
