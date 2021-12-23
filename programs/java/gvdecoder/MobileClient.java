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
import java.lang.Thread;

/* Reads all bytes from the specified stream until it finds a line feed character (\n).
 * For simplicity's sake I'm reading one character at a time.
 * It might be better to use a PushbackInputStream, read more bytes at
 * once, and push the surplus bytes back into the stream...
 */



public class MobileClient extends Thread {


	Socket echoSocket = null;
	PrintStream out = null;
	BufferedReader in = null;
	InputStream is = null;

	String home="192.168.1.172";
    String work="10.121.165.59";
    String work2="142.157.18.17";
    String work3="142.157.51.203";
	boolean connected=false;
	public int loopsleep=500;
	public boolean requestDisconnect=false;
    String res; //this string receives input.
    JythonViewer jv;


    String[] functions={"open(?,?,?)","close()","rescale()","setExposure(?)","runCamera(?,?,?)","closeCamera()","ledON(?)","ledOff()","help()"};

    String[] tags     ={"conditions,100 degrees,room temp,lid off,temp unknown",
                        "imaging,high frame rate,65 fps,25 fps,<br>,zoom 0.63,zoom 1000,zoom 2,zoom 3,zoom 4,zoom 5,zoom max,zoom unknown",
                        "cheese,cheddar,swiss,goat,american,provalone"};
    String[] files    ={"datafile1.dat", "datafile2.dat", "datafile3.dat"};




    public MobileClient(JythonViewer myjython){
		jv=myjython;
	}

    public void mobilePythonCommand(String command){
		jv.editor_runString(command);
	}

	public void mobilePythonNotUnderstood(){
		jv.editor_runString("mobileNotUnderstoodError()");
	}

	public void mobileMoveImage(){
		jv.editor_runString("mobileMoveImage()");
	}

	public void mobilePythonComment(String comment){
		jv.editor_runString("mobile_voicecomment('"+comment+"')");
	}

    public String parseString(String str){
		int pos=res.indexOf('=');
		return res.substring(pos+1,res.length());
	}


    public void mobileAlertDisconnect(){
		jv.editor_runString("mobileAlertDisconnect()");
	}

    public boolean connect(){
	 return connect(work3);
	}
	String lastIP=null;
	public boolean connect(String ipa){
	 if (connected) disconnect();
	 connected=false;
	 try {
			echoSocket = new Socket(ipa, 8080);
			is = echoSocket.getInputStream();
			out = new PrintStream(echoSocket.getOutputStream());
			lastIP=ipa;
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: localhost.");

		} catch (IOException e) {
			System.err.println("Couldn't get I/O for he connection to: localhost.");

		}
	 connected=true;
	 return connected;
	}

	public boolean disconnect(){
		connected=false;
		try{
		is.close();
		out.close();
		echoSocket.close();
		mobileAlertDisconnect();
	   }catch(Exception e){e.printStackTrace(); return false;}
		return true;
	}

	public boolean reconnect(){
		if (connected) disconnect();
		if (lastIP!=null) return connect(lastIP);
		else return false;
	}

    public boolean contains(String str ,String substr){
		return (str.indexOf(substr)!=-1);
	}
	public boolean voiceCommand(){
		String command=parseString(res);
		if (contains(command,"lights on")||contains(command,"light on")){ mobilePythonCommand("mobile_lightOn()");}
		else
		if (contains(command,"lights off")||contains(command,"light off")){ mobilePythonCommand("mobile_lightOff()");}
		else
		if (contains(command,"run ") && contains(command,"program") && (contains(command,"please")||contains(command,"PLS"))) {

		 if (contains(command," one") || contains(command," 1")) mobilePythonCommand("mobile_function(1)");
		 else
		 if (contains(command," two") || contains(command," to") ||contains(command," 2") || contains(command," too") ) mobilePythonCommand("mobile_function(2)");
		 else
		 if (contains(command," three") || contains(command," 3")) mobilePythonCommand("mobile_function(3)");
	     else
	     if (contains(command," four") || contains(command," 4") || contains(command," for"))  mobilePythonCommand("mobile_function(4)");
	     else
	   	 if (contains(command," five") || contains(command," 5"))  mobilePythonCommand("mobile_function(5)");
	     else
		 if (contains(command," six") || contains(command," 6")) mobilePythonCommand("mobile_function(6)");
		 else
		 if (contains(command," seven") || contains(command," 7")) mobilePythonCommand("mobile_function(7)");
		 else
		 if (contains(command," eight") || contains(command," 8")) mobilePythonCommand("mobile_function(8)");
		 else
		 if (contains(command," nine") || contains(command," 9")) mobilePythonCommand("mobile_function(9)");
		 else
		 if (contains(command," ten") || contains(command," 10")) mobilePythonCommand("mobile_function(10)");
	     else mobilePythonNotUnderstood();
	    }
		else mobilePythonNotUnderstood();
		return true;
	}

  public boolean runFunction(){
	  String command=parseString(res);
	  mobilePythonCommand(command);
	  return true;
  }

   public boolean qualityRating(){
	   int rating =0;
	   int rating_index=res.indexOf("rating =");
	   if (rating_index>0){
		 String rating_number=res.substring(rating_index+9,rating_index+10);
		 System.out.println("rating number = "+rating_number);
		 rating=Integer.parseInt(rating_number);
	   }
	   StringBuilder strb=new StringBuilder(res.length());

	   for (int i=0;i<rating;i++){
		   strb.append("<span class='fa fa-star checked'></span>");
	   }
	   for (int j=0;j<5-rating;j++){
	       strb.append("<span class='fa fa-star'></span>");
	   }

	   //String command=parseString(res);
       String res1=res.replace("<"," _");
	   String res2=res1.replace(">"," ");
	   String res3=res2.replace(",","<br>");
	   String res4=res3.replace("************","</p></tags>");
	   String res5=res4.replace("*** TAGS ***","<tags><p><u><b>Tags</b></u>");
	   String res6=res5.replace("_rating = 5.0","&#9733 &#9733 &#9733 &#9733 &#9733 <rating=5>");
	   String res7=res6.replace("_rating = 4.0","&#9733 &#9733 &#9733 &#9733 <rating=4> ");
	   String res8=res7.replace("_rating = 3.0","&#9733 &#9733 &#9733 <rating=3>");
	   String res9=res8.replace("_rating = 2.0","&#9733 &#9733 <rating=2>");
	   String res10=res9.replace("_rating = 1.0","&#9733 <rating=1>");


	   mobilePythonComment("grade : "+res10);
	   mobilePythonComment("rating : "+strb.toString());
	   return true;
   }

	public boolean voiceComment(){
		String command=parseString(res);
		mobilePythonComment(command);
		return true;
	}


	public boolean getAndSaveImage(){
     try{
		String num=parseString(res);
		System.out.println("number = "+num);
		int filesize=Integer.parseInt(num);
		out.print("from java, file size = "+num+"\n");
		out.flush();

		int bytesRead;
		int current;
		byte [] mybytearray  = new byte [filesize];

		FileOutputStream fos = new FileOutputStream("temp.jpg"); // destination path and name of file
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bytesRead = is.read(mybytearray,0,mybytearray.length);
		current = bytesRead;
	    System.out.println("bytesread="+bytesRead+" of "+mybytearray.length);
		do {
		   bytesRead =is.read(mybytearray, current, (mybytearray.length-current));
		   System.out.println("bytesread="+bytesRead+" of "+mybytearray.length);
		   if(bytesRead >= 0) current += bytesRead;
		   } while(current<mybytearray.length-1);

		bos.write(mybytearray, 0 , current);
		bos.flush();
		System.out.println("done writing file");
		bos.close();
		out.print("_RECEIVED\n");
		out.flush();
		System.out.println("just done: number of bytes still in stream = "+is.available());
	  }catch(Exception e){
		  e.printStackTrace();
	  }
        mobileMoveImage();
		//in=new BufferedReader(new InputStreamReader(is));
	    return true;
	 }

    public boolean updateFileNames=false;
    public void setFilenames(String[] filenames){
	  files=filenames;
	  updateFileNames=true;
	}


	public boolean updateTags=false;
	public void setTags(String[] newtags){
		  tags=newtags;
		  updateTags=true;
    }

    public boolean updateFunctions=false;
    public void setFunctions(String[] newfunctions){
		functions=newfunctions;
		updateFunctions=true;
	}


	public void run(){
     if (!connected) {
		 if (!connect()) return;
	 }
     while (!requestDisconnect){
            res=null;
            try{
		     	String tmp=readLineFrom(is);
		     	res=tmp.replaceAll("'","");
		    }catch(Exception e){
			 e.printStackTrace();
			 connected=false;
			 requestDisconnect=true;
			}
			if ((res!=null)&&(connected)){

				System.out.println("received "+res);
				if (res.startsWith("_PING")){
					if (updateFileNames){
						out.print("_HASFILENAMES\n");
						out.flush();
						updateFileNames=false;
				     }else
				    if (updateTags){
						out.print("_HASTAGS\n");
						out.flush();
						updateTags=false;
					}else
					if (updateFunctions){
						out.print("_HASFUNCTIONS\n");
						out.flush();
						updateFunctions=false;
					}else
					{
					out.print("_PONG\n");
					out.flush();
				   }
			    }else
			    if (res.startsWith("_GETFUNCTIONLIST")){
			  					  System.out.println("received _GETFUNCTIONLIST request");
			  					  sendStrings(functions);
			  	}else
			  	if (res.startsWith("_GETFILELIST")){
			  			         System.out.println("received _GETFILELIST request");
			  					 sendStrings(files);
			  	}else
			  	if (res.startsWith("_GETTAGLIST")){
			  				      System.out.println("received _GETTAGLIST request");
			  					  sendStrings(tags);
				}else

			    if (res.startsWith("_IMAGEFILESIZE=")){
			     getAndSaveImage();

			    }else
				if (res.startsWith("_VOICECOMMAND=")){
				 voiceCommand();
				}else
				if (res.startsWith("_VOICECOMMENT=")){
				 voiceComment();
				} else

				if ((res.startsWith("_RUNCOMMAND="))||(res.startsWith("_RUNFUNCTION"))){
					runFunction();
				}else
				if (res.indexOf("*** TAGS")>=0){
				 qualityRating();
				}else
				if (res.startsWith("_DISCONNECT")){
					requestDisconnect=true;
				}else System.out.println("\necho ="+res);

	        }
	        try{java.lang.Thread.sleep(loopsleep); }catch(Exception e){}
	  }

	  System.out.println("Successful disconnect from socket = "+disconnect());
  }

/*

	public static void main(String[] args) throws IOException {

		System.out.println("EchoClient.main()");
        if (!connect())  System.out.println("unable to get socket connection");

		// String userInput;
		System.out.println("connected!!");
		int counter = 0;
		System.out.println("socket state closed = "+echoSocket.isClosed());
		System.out.println("socket state connected = "+echoSocket.isConnected());

		// TODO monitor
		while (true) {
			counter++;
			try{
            java.lang.Thread.sleep(500);
		    }catch(Exception e){}
                System.out.println("number of bytes still in stream = "+is.available());
				res=readLineFrom(is);
				if (res!=null){
					//out.println("update" + new Date().getSeconds());
					System.out.println("echo: " + res);
					if (res.startsWith("_PING")){
					out.print("_PONG\n");
					out.flush();
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
						   // thanks to A. Cádiz for the bug fix
						    do {
						       bytesRead =is.read(mybytearray, current, (mybytearray.length-current));
						       System.out.println("bytesread="+bytesRead+" of "+mybytearray.length);
						       if(bytesRead >= 0) current += bytesRead;
						       } while(current<mybytearray.length-1);

						    bos.write(mybytearray, 0 , current);
						    bos.flush();

						    System.out.println("done writing file");
						    bos.close();

                       System.out.println("just done: number of bytes still in stream = "+is.available());
                       //in=new BufferedReader(new InputStreamReader(is));
					}
					}else{System.out.println("res = null");}
			  if (echoSocket.isClosed()) System.out.println("Socket closed");
			  if (!echoSocket.isConnected()) System.out.println("Socket is not connected");

		}
	}
*/

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