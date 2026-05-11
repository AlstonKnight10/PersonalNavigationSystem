package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * An abstract modal JDialog that can be extended to create
 * dialogs for specific purposes.
 *
 * @author  Prof. David Bernstein, James Madison University
 * @version 1.0
 */
public abstract class AbstractModalDialog extends JDialog implements ActionListener, WindowListener
{

  private static final long serialVersionUID = 1L;
  private static final String CANCEL_KEY = "Cancel";
  private static final String OK_KEY     = "OK";

  public static final int CANCEL_OPTION  = JOptionPane.CANCEL_OPTION;
  public static final int OK_OPTION      = JOptionPane.OK_OPTION;

  private boolean includeCancel, includeOK, layoutRequired;    
  private Component  owner;
  private int returnStatus;    
  private JButton cancelButton, okButton; 


  /**
   * Explicit Value Constructor.
   *
   * @param owner   The parent Dialog
   * @param title   The title of this JDialog
   */
  public AbstractModalDialog(final Dialog owner, final String title)
  {
    super(owner, title, true);       
    this.owner = owner;
    performCommonConstructionTasks(title);
  }

  /**
   * Explicit Value Constructor.
   *
   * @param owner   The parent Frame
   * @param title   The title of this JDialog
   */
  public AbstractModalDialog(final Frame owner, final String title)
  {
    super(owner, title, true);       
    this.owner = owner;
    performCommonConstructionTasks(title);
  }

  /**
   * Handle actionPerformed messages.
   *
   * @param e   The event that generated the message
   */
  public void actionPerformed(final ActionEvent e)
  {
    String ac = e.getActionCommand();
    if (ac.equals(CANCEL_KEY))
    {
      returnStatus = CANCEL_OPTION;          
      setVisible(false);
    }
    else if (ac.equals(OK_KEY))
    {
      returnStatus = OK_OPTION;          
      setVisible(false);
    }
  }

  /**
   * Create the main pane in this JDialog
   * (must be implemented by concrete children).
   *
   * @return  The main pane
   */
  protected abstract JComponent createMainPane();

  /**
   * Perform tasks required by all constructors.
   * 
   * @param title The title of the dialog
   */
  private void performCommonConstructionTasks(final String title)
  {
    cancelButton = new JButton(CANCEL_KEY);
    okButton     = new JButton(OK_KEY);

    includeCancel = true;
    includeOK     = true;

    if (title != null) setTitle(title);       
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);       

    cancelButton.setActionCommand(CANCEL_KEY);
    cancelButton.addActionListener(this);

    okButton.setActionCommand(OK_KEY);       
    okButton.addActionListener(this);       

    addWindowListener(this);
    layoutRequired = true;

    setSize(400,400);      
  }

  /**
   * Layout this JDialog.
   */
  private void performLayout()
  {
    JComponent          cp, mainPane;       
    JPanel              buttonPanel;       

    cp = (JComponent)getContentPane();
    cp.setLayout(new BorderLayout());

    mainPane = createMainPane();
    cp.add(mainPane, BorderLayout.CENTER);

    buttonPanel = new JPanel();
    if (includeOK && includeCancel) 
      buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
    else
      buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    if (includeOK)     buttonPanel.add(okButton);
    if (includeCancel) buttonPanel.add(cancelButton);
    cp.add(buttonPanel, BorderLayout.SOUTH);

    pack();       

    layoutRequired = false;       
  }

  /**
   * Indicate whether the Cancel button should be included.
   *
   * (Note: This method must be called before the first call
   * to showDialog)
   *
   * @param include   true to include; false otherwise
   */
  protected void setIncludeCancel(final boolean include)
  {
    includeCancel = include;       
  }

  /**
   * Indicate whether the OK button should be included.
   *
   * (Note: This method must be called before the first call
   * to showDialog)
   *
   * @param include   true to include; false otherwise
   */
  protected void setIncludeOK(final boolean include)
  {
    includeOK = include;       
  }

  /**
   * Show this JDialog (and block the thread of execution until
   * the user responds).
   *
   * @return   Either CANCEL_OPTION or OK_OPTION
   */
  public int showDialog()
  {
    int         x, y;
    Rectangle   r;


    if (layoutRequired) performLayout();       

    returnStatus = CANCEL_OPTION;

    r = owner.getBounds();
    x = (int)(r.getX() + r.getWidth()/2  - getWidth()/2);
    y = (int)(r.getY() + r.getHeight()/2 - getHeight()/2);
    setLocation(x, y);

    setVisible(true);

    return returnStatus;       
  }

  /**
   * Handle windowActivated messages.
   *
   * @param e   The relevant WindowEvent
   */
  public void windowActivated(final WindowEvent e)
  {
    // Ignore
  }


  /**
   * Handle windowClosed messages.
   *
   * @param e   The relevant WindowEvent
   */
  public void windowClosed(final WindowEvent e)
  {
    // Ignore
  }

  /**
   * Handle windowClosing messages.
   *
   * @param e   The relevant WindowEvent
   */
  public void windowClosing(final WindowEvent e)
  {
    returnStatus = CANCEL_OPTION;
    setVisible(false);       
  }

  /**
   * Handle windowDeactivated messages.
   *
   * @param e   The relevant WindowEvent
   */
  public void windowDeactivated(final WindowEvent e)
  {
    // Ignore
  }

  /**
   * Handle windowDeiconified messages.
   *
   * @param e   The relevant WindowEvent
   */
  public void windowDeiconified(final WindowEvent e)
  {
  }

  /**
   * Handle windowIconified messages.
   *
   * @param e   The relevant WindowEvent
   */
  public void windowIconified(final WindowEvent e)
  {
    // Ignore
  }

  /**
   * Handle windowOpened messages.
   *
   * @param e   The relevant WindowEvent
   */
  public void windowOpened(final WindowEvent e)
  {
    // Ignore
  }

}
