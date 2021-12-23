package gvdecoder;

import java.util.Arrays;
import java.util.List;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.awt.HeadlessException;
import java.awt.datatransfer.Clipboard;


/**
 * @author Matthias Hinz
 */
class ClipboardTextListener implements Runnable {

    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
    JythonViewer jv;
    private volatile boolean running = true;

    public void terminate() {
        running = false;
    }

    public void start(){
		if (!running){
		running=true;
		Thread thread = new Thread(this);
        thread.start();
	   }else System.out.println("terminate() first");
	}

    public ClipboardTextListener(JythonViewer jv){
		this.jv=jv;
		running=false;
		start();
	}

    public void run() {
        System.out.println("Listening to clipboard...");
        // the first output will be when a non-empty text is detected
        String recentContent = "";
        // continuously perform read from clipboard
        while (running) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                // request what kind of data-flavor is supported
                List<DataFlavor> flavors = Arrays.asList(sysClip.getAvailableDataFlavors());
                // this implementation only supports string-flavor
                if (flavors.contains(DataFlavor.stringFlavor)) {
                    String data = (String) sysClip.getData(DataFlavor.stringFlavor);
                    if (!data.equals(recentContent)) {
                        recentContent = data;
                        // Do whatever you want to do when a clipboard change was detected, e.g.:
                        System.out.println("New clipboard text detected: " + data);
                        jv.editor_runString("fromClipboard('"+data+"')");
                    }
                }

            } catch (HeadlessException e1) {
                e1.printStackTrace();
            } catch (UnsupportedFlavorException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


}