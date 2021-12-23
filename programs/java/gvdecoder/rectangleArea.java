package gvdecoder;
/*
 * Swing version.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/*
 * Displays a framed area.  When the user clicks within
 * the area, this program displays a filled rectangle
 * and a string indicating the coordinates where the
 * click occurred.
 */


class simpleGraph extends JPanel {
    Point point = null;

    Dimension preferredSize = new Dimension(300,100);
    int rectWidth = 50;
    int rectHeight = 50;

    public simpleGraph() {
       // Border raisedBevel = BorderFactory.createRaisedBevelBorder();
       // Border loweredBevel = BorderFactory.createLoweredBevelBorder();
       // Border compound = BorderFactory.createCompoundBorder
                              (raisedBevel, loweredBevel);
       // setBorder(compound);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (point == null) {
                    point = new Point(x, y);
                } else {
                    point.x = x;
                    point.y = y;
                }
                repaint();
            }
        });
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);  //paint background

        //Paint a filled rectangle at user's chosen point.
        if (point != null) {
            g.drawRect(point.x, point.y,
                       rectWidth - 1, rectHeight - 1);
            g.setColor(Color.yellow);
            g.fillRect(point.x + 1, point.y + 1,
                       rectWidth - 2, rectHeight - 2);

        }
    }
}
