package gvdecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.lang.Thread;

/* Reads all bytes from the specified stream until it finds a line feed character (\n).
 * For simplicity's sake I'm reading one character at a time.
 * It might be better to use a PushbackInputStream, read more bytes at
 * once, and push the surplus bytes back into the stream...
 */



public class RemoteControl implements Runnable{
	static String home2="192.168.1.189";
	static String home="192.168.1.172";
    static String work="10.121.165.59";
    static String work2="10.121.113.253";
    static String work3="10.121.196.179";
    String ServerAddress="";
	Socket echoSocket = null;
	PrintStream out = null;
	OutputStream binaryoutput=null;
	BufferedReader in = null;
	DataOutputStream dataout=null;
    InputStream is = null;
    public boolean HaveNewFunctions=false;
    public boolean HaveNewTags=false;
    public boolean HaveNewFilenames=false;
    String[] functions={"open(?,?,?)","close()","rescale()","setExposure(?)","runCamera(?,?,?)","closeCamera()","ledON(?)","ledOff()","help()"};
    String[] tags     ={"conditions,100 degrees,room temp,lid off,temp unknown",
                        "imaging,high frame rate,65 fps,25 fps,<br>,zoom 0.63,zoom 1000,zoom 2,zoom 3,zoom 4,zoom 5,zoom max,zoom unknown",
                        "cheese,cheddar,swiss,goat,american,provalone"};
    String[] files    ={"datafile1.dat", "datafile2.dat", "datafile3.dat"};
    public static RemoteControl ec;
    gvdecoder.JythonViewer jv;
    Thread thread;

	public static void main(String[] args)  {
      ec=new RemoteControl(home);
      ec.run();

    }


    public RemoteControl(String ipaddress){

		ServerAddress=ipaddress;
		jv=gvdecoder.GView.getGView().jv;
		ec=this;
		thread=new Thread(this);
		thread.start();
	}

    public void runJythonCommand(String str){
		jv.textField.setText(str);
		jv.processText();
	}

	public void restart(){
		System.out.println("connection lost - restarting!");
		thread=new Thread(this);
		thread.start();
	}


	public void run(){
		System.out.println("RemoteControl");
		try {
			echoSocket = new Socket(ServerAddress, 8080);
			is = echoSocket.getInputStream();
			binaryoutput=echoSocket.getOutputStream();
			out = new PrintStream(binaryoutput);
			dataout=new DataOutputStream(binaryoutput);

			//in = new BufferedReader(new InputStreamReader(is));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: localhost.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for "
					+ "the connection to: localhost.");
			System.exit(1);
		}

		// String userInput;
		System.out.println("connected!!");
		int counter = 0;
		System.out.println("socket state closed = "+echoSocket.isClosed());
		System.out.println("socket state connected = "+echoSocket.isConnected());
        String unprocessed="conditions,100 degrees,room temp,lid off,temp unknown@@@imaging,high frame rate,65 fps,25 fps,<br>,zoom 0.63,zoom 1000,zoom 2,zoom 3,zoom 4,zoom 5,zoom max,zoom unknown@@@cheese,cheddar,swiss,goat,american,provalone";
        boolean tagsunsent=true;
        boolean functionsunsent=true;
        boolean filenamesunsent=true;
        int subframecounter=0;
		// TODO monitor
		boolean connection_ok=true;
		while (connection_ok) {
			counter++;
			try{
            java.lang.Thread.sleep(25);
		    }catch(Exception e){}
                //System.out.println("number of bytes still in stream = "+is.available());
			try{
			  String res=readLineFrom(is);
			  if ((res!=null)&&(res!="")){
					//out.println("update" + new Date().getSeconds());
					//System.out.println("echo: " + res);
					if (res.startsWith("_PING")){
					   if (HaveNewFunctions){
							out.print("_HASFUNCTIONS\n");
							out.flush();
							System.out.println("sent _HASFUNCTIONS");
							HaveNewFunctions=false;
						}else
					   if (HaveNewFilenames){
						 out.print("_HASFILENAMES\n");
						 out.flush();
						 System.out.println("sent _HASFILENAMES");
				         HaveNewFilenames=false;
				       }else
					   if (HaveNewTags){
						out.print("_HASTAGS\n");
						out.flush();
						System.out.println("sent _HASTAGS");
						HaveNewTags=false;

				       }
					else{
					out.print("_PONG\n");
					out.flush();
					System.out.print(".");
				     }
					}else

					if (res.startsWith("_GETFUNCTIONLIST")){
					  System.out.println("received _GETFUNCTIONLIST request");
					  ec.sendStrings(functions);
				    }else
				    if (res.startsWith("_GETFILELIST")){
					 System.out.println("received _GETFILELIST request");
					 ec.sendStrings(files);
					}else
				    if (res.startsWith("_GETTAGLIST")){
				      System.out.println("received _GETTAGLIST request");
					  ec.sendStrings(tags);
					}else

					if (res.startsWith("_IMAGEFILESIZE=")){
						int pos=res.indexOf('=');
						String num=res.substring(pos+1,res.length());
						System.out.println("number = "+num);
						int filesize=Integer.parseInt(num);
						out.print("from java, file size = "+num+"\n");
						out.flush();
                           int bytesRead;
                           int current;
						   byte [] mybytearray  = new byte [filesize];

						   FileOutputStream fos = new FileOutputStream("test.jpg"); // destination path and name of file
						   BufferedOutputStream bos = new BufferedOutputStream(fos);
						   bytesRead = is.read(mybytearray,0,mybytearray.length);
						   current = bytesRead;
                           System.out.println("bytesread="+bytesRead+" of "+mybytearray.length);
						   // thanks to A. C�diz for the bug fix
						    do {
						       bytesRead =is.read(mybytearray, current, (mybytearray.length-current));
						       System.out.println("bytesread="+bytesRead+" of "+mybytearray.length);
						       if(bytesRead >= 0) current += bytesRead;
						       } while(current<mybytearray.length-1);

						    bos.write(mybytearray, 0 , current);
						    bos.flush();

						    System.out.println("\ndone writing file");
						    bos.close();
						    out.print("_RECEIVED\n");
						    out.flush();

                       System.out.println("just done: number of bytes still in stream = "+is.available());
                       //in=new BufferedReader(new InputStreamReader(is));

					}
					else
					if (res.startsWith("_GETFILENAMES")){
						out.print("_FILES=not chosen,file_"+counter+".dat,file_"+(counter+1)+".dat,file_"+(counter+2)+".dat\n");
						out.flush();
                        }
                    else
                    if (res.startsWith("_GETSUBFRAME")){
						subframecounter++;
						//_GETSUBFRAME("+tx+","+ty+","+width+","+height+")\n"
						int b1=res.indexOf("(");
						int b2=res.indexOf(")");
						String[] params=res.substring(b1+1,b2).split(",");
						for (int i=0;i<params.length;i++){ System.out.println(params[i]);}
						int t1=Integer.parseInt(params[0]);
						int t2=Integer.parseInt(params[1]);
						int w=Integer.parseInt(params[2]);
						int h=Integer.parseInt(params[3]);

						if (subframecounter>h) subframecounter=0;
						byte[] tmp=new byte[w*h];
						for (int p=0;p<h;p++){
							for (int q=0;q<w;q++){
								int c=255;
								if ((p==0)||(p==h-1)||(p==h/2)||(q==w/2)) c=0;
								if (p==subframecounter) c=128;
								tmp[p*w+q]=(byte)c;
								//dataout.writeInt(createPixel(subframecounter%255,p%255,c,255));
							}
						}
			           dataout.write(tmp);
					   dataout.flush();
					}




                     else{
						 System.out.println("\necho ="+res);
					      runJythonCommand("remotecontrolcommand('"+res+"')");
					 }

					}else{
						System.out.println("res = null or blank. Connection lost?");
						connection_ok=false;
					}
			  if (echoSocket.isClosed()) System.out.println("Socket closed");
			  if (!echoSocket.isConnected()) System.out.println("Socket is not connected");
		  } catch(Exception e){connection_ok=false;e.printStackTrace();}
		}//while connection ok
		if (!connection_ok){
		 try{
	      echoSocket.close();
	      }catch(IOException e){e.printStackTrace();}
	      restart();
	   }
	}

int createPixel(int r, int g, int b, int a) {
        return (a<<24) | (r<<16) | (g<<8) | b;
    }

public void sendStrings(String[] list){
   System.out.println("in sendstrings");
   int n=list.length;
   out.print("_NUMBER_OF_ENTRIES="+n+"\n");
   out.flush();
   for (int j=0;j<n;j++){
   	 out.print("_STRING="+list[j]+"\n");
   	 out.flush();
   	}
   out.print("_STATUS=DONE\n");
   out.flush();

}

private static String readLineFrom(InputStream stream) throws IOException {
    InputStreamReader reader = new InputStreamReader(stream);
    StringBuffer buffer = new StringBuffer();

    for (int character = reader.read(); character != -1; character = reader.read()) {
        if (character == '\n')
            break;
        buffer.append((char)character);
    }

    return buffer.toString();
}
}