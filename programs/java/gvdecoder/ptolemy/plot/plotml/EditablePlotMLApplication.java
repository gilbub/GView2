/* Editable plotter application that is capable of reading PlotML files.

@Author: Edward A. Lee

@Version: $Id: EditablePlotMLApplication.java,v 1.17.2.1 2001/02/09 20:29:09 cxh Exp $

@Copyright (c) 1997-2001 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY
@ProposedRating red (eal@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/
package ptolemy.plot.plotml;

import ptolemy.plot.EditablePlot;
import ptolemy.plot.PlotBox;
import ptolemy.gui.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// EditablePlotMLApplication

/**
An application that can plot data in PlotML format from a URL or
from files specified on the command line, and can then permit the
user to edit the plot.
To compile and run this application, do the following:
<pre>
    javac -classpath ../../.. EditablePlotMLApplication.java
    java -classpath ../../.. ptolemy.plot.plotml.EditablePlotMLApplication
</pre>
Initially, none of the data sets is editable. Use the Edit menu's
Edit Dataset item to make a data set editable.

@author Edward A. Lee
@version $Id: EditablePlotMLApplication.java,v 1.17.2.1 2001/02/09 20:29:09 cxh Exp $
@see ptolemy.plot.PlotBox
@see ptolemy.plot.Plot
*/
public class EditablePlotMLApplication extends PlotMLApplication {

    /** Construct a plot with no command-line arguments.
     *  It initially displays a sample plot.
     *  @exception Exception If command line arguments have problems.
     */
    public EditablePlotMLApplication() throws Exception {
        this(null);
    }

    /** Construct a plot with the specified command-line arguments.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public EditablePlotMLApplication(String args[]) throws Exception {
        this(new EditablePlot(), args);
    }

    /** Construct a plot with the specified command-line arguments
     *  and instance of plot.
     *  @param plot The instance of EditablePlot to use.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public EditablePlotMLApplication(EditablePlot plot, String args[])
            throws Exception {
        super(plot, args);

        // The default is that no set is editable.
        ((EditablePlot)plot).setEditable(-1);

        // Edit menu
        JMenuItem select = new JMenuItem("Edit Dataset", KeyEvent.VK_E);
        SelectListener selectListener = new SelectListener();
        select.addActionListener(selectListener);
        _editMenu.add(select);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new plot window and map it to the screen.
     */
    public static void main(String args[]) {
        try {
            EditablePlotMLApplication plot =
                new EditablePlotMLApplication(new EditablePlot(), args);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    /** Display basic information about the application.
     */
    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "EditablePlotMLApplication class\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu " +
                "and Christopher Hylands, cxh@eecs.berkeley.edu\n" +
                "Version " + PlotBox.PTPLOT_RELEASE +
                ", Build: $Id: EditablePlotMLApplication.java,v 1.17.2.1 2001/02/09 20:29:09 cxh Exp $\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n\n" +
                "Copyright (c) 1997-2001, " +
                "The Regents of the University of California.",
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        JOptionPane.showMessageDialog(this,
                "EditablePlotMLApplication is a standalone plot " +
                " application.\n" +
                "  File formats understood: PlotML and Ptplot ASCII.\n" +
                "  Left mouse button: Zooming.\n" +
                "  Right mouse button: Editing data (use edit menu to select " +
                "a dataset).\n\n" +
                _usage(),
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Open a dialog to select a dataset to edit.
     */
    protected void _selectDataset() {
        Query query = new Query();
        int numSets = ((EditablePlot)plot).getNumDataSets();
        String[] choices = new String[numSets + 1];
        for (int i = 0; i < numSets; i++) {
            choices[i + 1] = plot.getLegend(i);
            if (choices[i + 1] == null) {
                choices[i + 1] = "" + i;
            }
        }
        choices[0] = "none";
        query.setTextWidth(20);
        query.addChoice("choice",
                "Choose a data set, then drag the right mouse button",
                choices, choices[0]);
        ComponentDialog dialog =
            new ComponentDialog(this, "Select dataset", query);
        String buttonPressed = dialog.buttonPressed();
        if (buttonPressed.equals("OK")) {
            int result = query.intValue("choice");
            if (result > 0) {
                ((EditablePlot)plot).setEditable(result - 1);
            } else {
                // none...
                ((EditablePlot)plot).setEditable(-1);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    class SelectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            _selectDataset();
        }
    }
}
