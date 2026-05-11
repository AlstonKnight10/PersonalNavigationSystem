package gui;

import java.awt.*;
import javax.swing.*;

/**
 * A JPanel that is layed-out in the horizontal dimension.
 * Individual elements can be sized relative to each other and
 * their "expandability" can be specified.
 * 
 * @author Prof. David Bernstein, James Madsion University
 * @version 1.0
 */
public class Row extends JPanel
{
  private static final long serialVersionUID = 1L;

  private GridBagLayout layout;
  private int           column;


  /**
   * Default Constructor.
   */
  public Row()
  {
    super();
    column = 0;
    layout = new GridBagLayout();
    setLayout(layout);
  }

  /**
   * Add a Component to this Row.
   * Note: The expandability of all Component objects normally sum to 100.
   * 
   * @param component     The Component to add
   * @param width         The width (in cells) of this Component
   * @param expandability The percentage of extra space that this Component should use
   */
  public void add(final Component component, final int width, final double expandability)
  {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = column; 
    gbc.gridy = 0;
    gbc.gridwidth = width;
    gbc.gridheight = 1;
    if (expandability > 0.0) gbc.fill = GridBagConstraints.HORIZONTAL;
    else                     gbc.fill = GridBagConstraints.NONE; 
    gbc.weightx = expandability; 
    layout.setConstraints(component, gbc);
    add(component);

    column += width;
  }
}
