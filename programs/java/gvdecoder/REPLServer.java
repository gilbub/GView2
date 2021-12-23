package gvdecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Math;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.lang.StringBuilder;


/**
 Based on a capitalization server example.
 This class allows REPL type control over an existing jython session from Sublime Text 3.
 All it does is write to the jython window's command field and relay the output back to sublime.
 Here 'input' is what Sublime sends... this is relayed to Jython by calling its REPL method, which returns a string.
 The string gets sent back to sublime.
 It uses the clipboard for responses bigger than one line.
 **/
public class REPLServer implements Runnable{

   static GView gv;
   static ServerSocket listener;
   static int clientNumber=0;
   public REPLServer(GView gv){
		this.gv=gv;
        Thread t = new Thread(this);
        t.start();
 }

 public void run(){

        System.out.println("The REPL server is running.");
        try {
           listener = new ServerSocket(9898);

            while (true) {
                new REPLRunner(listener.accept(), clientNumber++).start();
            }
        }catch(Exception e){e.printStackTrace();}

 }

    /**
    A thread to handle responses.
     */
    private static class REPLRunner extends Thread implements ClipboardOwner{
        private Socket socket;
        private int clientNumber;

        public REPLRunner(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("New connection with client# " + clientNumber + " at " + socket);
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                  out.println("Hello, you are client #" + clientNumber + ".");

                while (true) {

				    try{
                	  java.lang.Thread.sleep(250);
				      }catch(InterruptedException e){log("interrupted"); e.printStackTrace();}

                   String input = in.readLine();
                   //log("after readline");
                   if (input!=null){
                     if (input.length()<1) log("short input");
                     else
                     if (input.length()>0){

                      log("going to send "+input);
                      String response=gv.jv.REPL(input);
                      if (response.length()<250){
						out.println("1");
						out.println(""+response);
					  }else{
                	   setClipboardContents(response);
                	   try{
                	   java.lang.Thread.sleep(250);
				       }catch(InterruptedException e){log("interrupted"); e.printStackTrace();}
                	   out.println(""+2);
				     }
				    }				    }
				   log("client "+clientNumber+" end of loop - why not return?");
			   }
              }catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
                e.printStackTrace();
            } finally {
                try {
					log("closing socket?");
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
            log("end of run");
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
        public void lostOwnership(Clipboard clipboard, Transferable contents){
		}
        private void setClipboardContents(String aString){
	       StringSelection stringSelection = new StringSelection(aString);
	       Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	       clipboard.setContents(stringSelection, this);
        }

    }
}