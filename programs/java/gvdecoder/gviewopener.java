import ij.*;

import ij.plugin.*;
import java.io.*;
import java.util.*;
import java.awt.*;


/**
opens a gview instance
*/

public class gviewopener implements PlugIn {


	public void run(String arg) {

	   boolean fullframerequested=false;

	   GraphicsEnvironment env = GraphicsEnvironment. getLocalGraphicsEnvironment();
	   GraphicsDevice[] devices = env.getScreenDevices();



	 	GView frame = new GView(devices[0],fullframerequested);

		frame.mainWindow.setVisible(true);

	}
}

