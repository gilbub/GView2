package gvdecoder;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;

public class ClipboardHelper implements ActionListener, Transferable {

final Toolkit toolkit = Toolkit.getDefaultToolkit();
final Clipboard clipboard = toolkit.getSystemClipboard();
Image image = null;
Robot robot;


final JFrame frame = new JFrame();
final JLabel label = new JLabel();
final JButton button1 = new JButton("Copy");
final JButton button2 = new JButton("Paste");
final JPanel pastePanel = new JPanel();

/**
* todo
* @todo gfgfd
*/
public static void main(String[] args) {
ClipboardHelper test = new ClipboardHelper(args.length > 0 ? args[0] : null);
}

public ClipboardHelper(String imageName) {
try {
robot = new Robot();
} catch (AWTException e) {
e.printStackTrace();
}
if (imageName != null) {
image = toolkit.createImage(imageName);
label.setIcon(new ImageIcon(image));
} else {
}

button1.addActionListener(this);
button2.addActionListener(this);

Container cont = frame.getContentPane();
cont.setLayout(new FlowLayout());
cont.add(label);
cont.add(button1);
cont.add(button2);
cont.add(pastePanel);
frame.pack();
frame.setVisible(true);
}

public void actionPerformed(ActionEvent e) {
try {
if (e.getSource() == button1) {
Rectangle rect = new Rectangle(300,300);
image = robot.createScreenCapture(rect);
clipboard.setContents(this, null);
} else {
final Transferable transferable = clipboard.getContents(null);
if
(transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
final Image img =
(Image)transferable.getTransferData(DataFlavor.imageFlavor);
pastePanel.add(new JLabel(new ImageIcon(img)));
} else {
System.err.println("No image data in the system clipboard.");
}
frame.pack();
frame.invalidate();
frame.validate();
frame.repaint();
}

} catch (Throwable t) {
t.printStackTrace();
}
}

public DataFlavor[] getTransferDataFlavors() {
return new DataFlavor[] {DataFlavor.imageFlavor };
}

public boolean isDataFlavorSupported(DataFlavor flavor) {
return DataFlavor.imageFlavor.equals(flavor);
}

public Object getTransferData(DataFlavor flavor)
throws UnsupportedFlavorException {
if (!isDataFlavorSupported(flavor)) {
throw new UnsupportedFlavorException(flavor);
}

return image;
}
}