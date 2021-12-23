import java.awt.event.*;
import javax.swing.*;
import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.component.*;

public class HelloWorld extends JFrame {

    public HelloWorld() {
				// Support exiting application
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

				// Set up basic frame
	setBounds(100, 100, 400, 400);
	setVisible(true);
	ZCanvas canvas = new ZCanvas();
	getContentPane().add(canvas);
	validate();

                                // Add some sample text
        ZText text = new ZText("Hello World!");
	ZVisualLeaf leaf = new ZVisualLeaf(text);
        canvas.getLayer().addChild(leaf);
    }
        
    public static void main(String args[]) {
        HelloWorld app = new HelloWorld();
    }
}
