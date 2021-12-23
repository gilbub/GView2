import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;

public class GlassPaneDemo {
    static private MyGlassPane myGlassPane;

    public static void main(String[] args) {
        JFrame frame = new JFrame("GlassPaneDemo");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        JCheckBox changeButton = 
                new JCheckBox("Glass pane \"visible\"");
        changeButton.setSelected(false);
        changeButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                myGlassPane.setVisible(e.getStateChange() 
                                       == ItemEvent.SELECTED);
            }
        });

        //Set up the content pane, where the "main GUI" lives.
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new FlowLayout());
        contentPane.add(changeButton);
        contentPane.add(new JButton("Button 1"));
        contentPane.add(new JButton("Button 2"));

        //Set up the menu bar, which appears above the content pane.
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menu.add(new JMenuItem("Do nothing"));
        menuBar.add(menu);
        frame.setJMenuBar(menuBar); 

        //Set up the glass pane, which appears over both menu bar
        //and content pane.
        myGlassPane = new MyGlassPane(changeButton, menuBar,
                                      frame.getContentPane());
        frame.setGlassPane(myGlassPane);

        frame.pack();
        frame.setVisible(true);
    }
}

/**
 * We have to provide our own glass pane so that it can paint.
 */
class MyGlassPane extends JComponent {
    Point point;

    public void paint(Graphics g) {
        if (point != null) {
            g.setColor(Color.red);
            g.fillOval(point.x - 10, point.y - 10, 20, 20);
        }
    }

    public void setPoint(Point p) {
        point = p;
    }

    public MyGlassPane(AbstractButton aButton, 
                       JMenuBar menuBar,
                       Container contentPane) {
        CBListener listener = new CBListener(aButton, menuBar,
                                             this, contentPane);
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }
}

/** 
 * Listen for all events that our check box is likely to be
 * interested in.  Redispatch them to the check box.
 */
class CBListener extends MouseInputAdapter {
    Toolkit toolkit;
    Component liveButton;
    JMenuBar menuBar;
    MyGlassPane glassPane;
    Container contentPane;
    boolean inDrag = false;

    public CBListener(Component liveButton, JMenuBar menuBar,
                      MyGlassPane glassPane, Container contentPane) {
        toolkit = Toolkit.getDefaultToolkit();
        this.liveButton = liveButton;
        this.menuBar = menuBar;
        this.glassPane = glassPane;
        this.contentPane = contentPane;
    }

    public void mouseMoved(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    /*
     * We must forward at least the mouse drags that started
     * with mouse presses over the check box.  Otherwise,
     * when the user presses the check box then drags off,
     * the check box isn't disarmed -- it keeps its dark
     * gray background or whatever its L&F uses to indicate
     * that the button is currently being pressed.
     */
    public void mouseDragged(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseClicked(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseEntered(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseExited(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mousePressed(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseReleased(MouseEvent e) {
        redispatchMouseEvent(e, true);
        inDrag = false;
    }

    private void redispatchMouseEvent(MouseEvent e,
                                      boolean repaint) {
        boolean inButton = false;
        boolean inMenuBar = false;
        Point glassPanePoint = e.getPoint();
        Component component = null;
        Container container = contentPane;
        Point containerPoint = SwingUtilities.convertPoint(
                                        glassPane,
                                        glassPanePoint, 
                                        contentPane);
        int eventID = e.getID();

        if (containerPoint.y < 0) {
            inMenuBar = true;
            container = menuBar;
            containerPoint = SwingUtilities.convertPoint(
                                        glassPane,
                                        glassPanePoint,
                                        menuBar);
            testForDrag(eventID);
        } 

        //XXX: If the event is from a component in a popped-up menu,
        //XXX: then the container should probably be the menu's
        //XXX: JPopupMenu, and containerPoint should be adjusted
        //XXX: accordingly.
        component = SwingUtilities.getDeepestComponentAt(
                                        container,
                                        containerPoint.x,
                                        containerPoint.y);

        if (component == null) {
            return;
        }

        if (component.equals(liveButton)) {
            inButton = true;
            testForDrag(eventID);
        }

        if (inMenuBar || inButton || inDrag) {
            Point componentPoint = SwingUtilities.convertPoint(
                                        glassPane,
                                        glassPanePoint, 
                                        component);
            component.dispatchEvent(new MouseEvent(component,
                                                 eventID,
                                                 e.getWhen(),
                                                 e.getModifiers(),
                                                 componentPoint.x,
                                                 componentPoint.y,
                                                 e.getClickCount(),
                                                 e.isPopupTrigger()));
        }

        if (repaint) {
            toolkit.beep();
            glassPane.setPoint(glassPanePoint);
            glassPane.repaint();
        }
    }

    private void testForDrag(int eventID) {
        if (eventID == MouseEvent.MOUSE_PRESSED) {
            inDrag = true;
        }
    }
}
