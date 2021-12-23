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
 * A server program which accepts requests from clients to
 * capitalize strings.  When clients connect, a new thread is
 * started to handle an interactive dialog in which the client
 * sends in a string and the server thread sends back the
 * capitalized version of the string.
 *
 * The program is runs in an infinite loop, so shutdown in platform
 * dependent.  If you ran it from a console window with the "java"
 * interpreter, Ctrl+C generally will shut it down.
 */
public class ServerTest {

    /**
     * Application method to run the server runs in an infinite loop
     * listening on port 9898.  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.  The server keeps a unique client number for each
     * client that connects just to show interesting logging
     * messages.  It is certainly not necessary to do this.
     */
    public static void main(String[] args){
        System.out.println("The capitalization server is running.");
        int clientNumber = 0;
        ServerSocket listener;
        try {
           listener = new ServerSocket(9898);

            while (true) {
                new Capitalizer(listener.accept(), clientNumber++).start();
            }
        }catch(Exception e){e.printStackTrace();}

}
    /**
     * A private thread to handle capitalization requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private static class Capitalizer extends Thread implements ClipboardOwner{
        private Socket socket;
        private int clientNumber;

        public Capitalizer(Socket socket, int clientNumber) {
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
                //out.println("Enter a line with only a period to quit\n");

                // Get messages from the client, line by line; return them
                // capitalized

                while (true) {

				    try{
                	  java.lang.Thread.sleep(250);
				      }catch(InterruptedException e){log("interrupted"); e.printStackTrace();}

                   String input = in.readLine();
                   //log("after readline");
                   if (input!=null){

                     if (input.length()<1) log("short input");
                     else
                     if (input.equals("-")){
						log("dash");
						out.println("1");
						out.println("single dash, single line");
					 }else
                     if (input.length()>0){
                      int n=(int)(Math.random()*10)+2;
                      log("going to send "+n+" lines with "+input);
                      StringBuilder str=new StringBuilder();
                      str.append("GView output:\n");
                      for (int i=0;i<n;i++) {
					 	str.append(input.toUpperCase()+" "+i+"\n");
                      }
                	  setClipboardContents(str.toString());
                	  try{
                	  java.lang.Thread.sleep(250);
				      }catch(InterruptedException e){log("interrupted"); e.printStackTrace();}
                	  out.println(""+n);
				     }
				     else log("don't know");
				  }
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