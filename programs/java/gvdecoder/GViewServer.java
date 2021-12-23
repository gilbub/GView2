package gvdecoder;
import java.net.*;
import java.io.*;

public class GViewServer extends Thread {


        public ServerSocket serverSocket = null;
        public Socket clientSocket = null;
        public PrintWriter out;
        public BufferedReader in;
        public boolean serverstarted=false;
        public GView gv;

        public String inputLine;
        public String outputLine;
        public String script;

        public GViewServer(GView gv){
		 this.gv=gv;
		 //startserver();
		 //run();
		}

        public boolean startserver(){
        serverstarted=true;
        try {
            serverSocket = new ServerSocket(4444);




        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            serverstarted=false;
            }

        try {
            clientSocket = serverSocket.accept();
            in = new BufferedReader(
										new InputStreamReader(
							clientSocket.getInputStream()));
             out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Accept failed.");
            serverstarted=false;
        }



        return serverstarted;
	    }

        public void runscript(){
			System.out.println("Captured Script=");
			System.out.println(script);
			//gv.jv.editor.setText(script);
			gv.jv.editor_runString(script);
		}

	    public void run(){
			    try{
				startserver();
				while ((inputLine = in.readLine()) != null) {
		             outputLine = "recieved";
		             System.out.println("from client="+inputLine);
		             if (inputLine.equalsIgnoreCase("startscript"))
		              script=new String("");
		             else if (inputLine.equalsIgnoreCase("endscript"))
		              runscript();
		             else
		              script+=inputLine+"\n";

		             //out.println(outputLine);
		             out.println("nothing");
		             if (outputLine.equals("Bye."))
		                break;
					try {
					     sleep(100);
					  } catch (InterruptedException e) { }


					}
	      }catch(IOException e){
			  System.out.println("Error in run of GViewServer.java");
			  e.printStackTrace();
		  }
		}





        public boolean closeserver(){
        boolean passed=true;
        try{
        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
	    }catch(IOException e){
			passed=false;
		}
		return passed;
	   }

    }

