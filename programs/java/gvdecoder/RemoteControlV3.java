package gvdecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.io.File;
import java.net.UnknownHostException;
import java.util.Date;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.lang.Thread;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.text.SimpleDateFormat;

import java.util.logging.Handler;

import java.util.logging.LogRecord;



/* Reads all bytes from the specified stream until it finds a line feed character (\n).
 * For simplicity's sake I'm reading one character at a time.
 * It might be better to use a PushbackInputStream, read more bytes at
 * once, and push the surplus bytes back into the stream...
 */


class Response extends Level{
	public static final int    PING				          =1;
	public static final String PINGSTRING			      ="_PING";
	public static final int    HASTAGS                    = 1000;
	public static final String HASTAGSSTRING              ="_HASTAGS";
	public static final int    HASFILENAMES               =1001;
	public static final String HASFILENAMESSTRING         ="_HASFILENAMES";
	public static final int    HASFUNCTIONS               =1002;
	public static final String HASFUNCTIONSSTRING         ="_HASFUNCTIONS";
	public static final int    GETFUNCTIONLIST            =1003;
	public static final String GETFUNCTIONLISTSTRING      ="_GETFUNCTIONLIST";
	public static final int    GETFILELIST                =1004;
	public static final String GETFILELISTSTRING          ="_GETFILELIST";
	public static final int    GETTAGLIST                 =1005;
	public static final String GETTAGLISTSTRING           ="_GETTAGLIST";
	public static final int    GETIMAGEFILESIZE           =870;
	public static final String GETIMAGEFILESIZESTRING     ="_IMAGEFILESIZE";
	public static final int    GETFILENAMES               =860; //repeat from getfilenames
	public static final String GETFILENAMESSTRING         ="_GETFILENAMES";
	public static final int    GETSUBFRAME                =850;
	public static final String GETSUBFRAMESTRING          ="_GETSUBFRAME";
	public static final int    FUNCTIONWINDOWCOMMAND      =840;
	public static final String FUNCTIONWINDOWCOMMANDSTRING="_FUNCTIONWINDOWCOMMAND";
	public static final int    VOICECOMMAND               =810;
	public static final String VOICECOMMANDSTRING         ="_VOICECOMMAND";
	public static final int    VOICECOMMENT               =820;
    public static final String VOICECOMMENTSTRING         ="_VOICECOMMENT";
    public static final int    TEXTCOMMENT                =825;
    public static final String TEXTCOMMENTSTRING          ="_TEXTCOMMENT";
    public static final int    TAGRATING				  =830;
    public static final String TAGRATINGSTRING            =",*** TAGS";
    public static final int    ERROR                      =-1;
    public static final String ERRORSTRING                ="_ERROR";
    public static final int    WELCOME					  =3000;
    public static final String WELCOMESTRING              ="connected to";

    public static int get(String code){
	  if (code.startsWith(PINGSTRING))                 return PING;
	  if (code.startsWith(HASTAGSSTRING))              return HASTAGS;
	  if (code.startsWith(HASFILENAMESSTRING))         return HASFILENAMES;
	  if (code.startsWith(HASFUNCTIONSSTRING))         return HASFUNCTIONS;
	  if (code.startsWith(GETFUNCTIONLISTSTRING))      return GETFUNCTIONLIST;
	  if (code.startsWith(GETFILELISTSTRING))          return GETFILELIST;
	  if (code.startsWith(GETTAGLISTSTRING))           return GETTAGLIST;
	  if (code.startsWith(GETIMAGEFILESIZESTRING))     return GETIMAGEFILESIZE;
	  if (code.startsWith(GETFILENAMESSTRING))         return GETFILENAMES;
	  if (code.startsWith(GETSUBFRAMESTRING))          return GETSUBFRAME;
      if (code.startsWith(FUNCTIONWINDOWCOMMANDSTRING))return FUNCTIONWINDOWCOMMAND;
      if (code.startsWith(VOICECOMMANDSTRING))         return VOICECOMMAND;
      if (code.startsWith(VOICECOMMENTSTRING))         return VOICECOMMENT;
      if (code.startsWith(TAGRATINGSTRING))            return TAGRATING;
      if (code.startsWith(WELCOMESTRING))              return WELCOME;
      if (code.startsWith(TEXTCOMMENTSTRING))          return TEXTCOMMENT;
      return -1;
	}

	public static Response TAGRATINGLEVEL =      new Response("TAGRATING", TAGRATING);
	public static Response VOICECOMMENTLEVEL   = new Response("VOICECOMMENT",VOICECOMMENT);
    public static Response VOICECOMMANDLEVEL   = new Response("VOICECOMMAND",VOICECOMMAND);
    public static Response FUNCTIONCOMMANDLEVEL= new Response("FUNCTIONCOMMAND",FUNCTIONWINDOWCOMMAND);
    public static Response IMAGELEVEL          = new Response("GETIMAGE",GETIMAGEFILESIZE);
    public static Response TEXTCOMMENTLEVEL    = new Response("TEXTCOMMENT",TEXTCOMMENT);
	//generate a level
	private Response(String name, int value){ super (name, value);}

}

public class RemoteControlV3 implements Runnable{



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
    public static RemoteControlV3 ec;
    gvdecoder.JythonViewer jv;
    Thread thread;

    static FileHandler fileTxt;
    static SimpleFormatter formatterTxt;
    static FileHandler fileHTML;
    static Formatter formatterHTML;
    public Logger logger;
    String loggingdirectoryname;
    String logfilename;
    File imagedirectory;
    boolean connection_ok;


	public static void main(String[] args)  {
      ec=new RemoteControlV3(home,"defaultlog");
      ec.run();

    }

    public void setupLogger(String loggingfilepath){
		try{



			logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

			// suppress the logging output to the console
	        logger.setUseParentHandlers(false);
			logger.setLevel(Level.ALL);
            fileTxt = new FileHandler(loggingfilepath, true);
            int tmp_l=loggingfilepath.length();
            fileHTML = new FileHandler(loggingfilepath.substring(0,tmp_l-4)+".html", true);
            formatterTxt = new SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);
            formatterHTML = new MyHtmlFormatter();
		    fileHTML.setFormatter(formatterHTML);
            logger.addHandler(fileHTML);
		}catch(IOException e){e.printStackTrace();}

	}


   public File uniqueImageName(){
	   File newfile=null;
	   try{
	    newfile= File.createTempFile("img_",".jpg",imagedirectory);
	  }catch(IOException e){e.printStackTrace();}
	  return newfile;

    }


    public void reconnect(){
       connection_ok=false;
       try{
	     java.lang.Thread.sleep(50);
	   }catch(Exception e){}
       thread=new Thread(this);
	   thread.start();
	}

    public RemoteControlV3(String ipaddress, String loggingpath){
	try{


		File tmp1=new File(loggingpath);
		//generate a unique log name
		if (tmp1.isDirectory()) loggingdirectoryname=tmp1.getPath();
		else loggingdirectoryname=tmp1.getParent();
		File tempFile = new File(loggingdirectoryname+File.separator+"logging.css");
				            if (!tempFile.exists()){
								PrintWriter out = new PrintWriter(tempFile);
								out.println(css_body);
								out.close();
			}
		logfilename=(File.createTempFile("log",".txt",new File(loggingdirectoryname))).getPath();

		setupLogger(logfilename);

		String imagedirectoryname=loggingdirectoryname+"\\logged_images";
		imagedirectory = new File(imagedirectoryname);
		if (! imagedirectory.exists()) imagedirectory.mkdir();

	  }catch(IOException e){e.printStackTrace();}



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

    public void logInput(int logtype, String str){
	  String data=str;
	  if ((logtype!=Response.TAGRATING)&&(logtype!=Response.GETIMAGEFILESIZE)&&(logtype!=Response.WELCOME)) {
		  String[] tmp =str.split("=");
		  if (tmp.length>0) data=tmp[1];
	  }

      switch (logtype){
		  case Response.WELCOME:
		    logger.info("connection established");
		    break;
		  case Response.VOICECOMMAND:
		    logger.log(Response.VOICECOMMANDLEVEL,data);
		    break;
		  case Response.VOICECOMMENT:
		    logger.log(Response.VOICECOMMENTLEVEL,data);
		    break;
		  case Response.TAGRATING:
		    logger.log(Response.TAGRATINGLEVEL,data);
		    break;
		  case Response.GETIMAGEFILESIZE:
		    logger.log(Response.IMAGELEVEL,data);
		    break;
		  case Response.FUNCTIONWINDOWCOMMAND:
		    logger.log(Response.FUNCTIONCOMMANDLEVEL, data);
		    break;
		  case Response.TEXTCOMMENT:
		    logger.log(Response.TEXTCOMMENTLEVEL,data);
		    break;
		  default:
		    logger.severe("unclassified input = "+str);

	  }
	}

   public void log(String str){
	   logger.fine("external = "+str);
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
			//System.exit(1);
			return;
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for "
					+ "the connection to: localhost.");
			//System.exit(1);
			return;
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
	    connection_ok=true;
		int bad_response=0;
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
					int responsetype=Response.get(res);
					switch(responsetype){
					  case Response.WELCOME:
					    logInput(Response.WELCOME,"connection established");
					    break;

					  case Response.PING:
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
				         break;

					 case Response.GETFUNCTIONLIST:
					      System.out.println("received _GETFUNCTIONLIST request");
					      ec.sendStrings(functions);
					      break;

				     case Response.GETFILELIST:
					      System.out.println("received _GETFILELIST request");
					      ec.sendStrings(files);
					      break;

					 case Response.GETTAGLIST:
				    	  System.out.println("received _GETTAGLIST request");
					      ec.sendStrings(tags);
					      break;

					 case Response.GETIMAGEFILESIZE:
                     	  int pos=res.indexOf('=');
						  String num=res.substring(pos+1,res.length());
						  System.out.println("number = "+num);
						  int filesize=Integer.parseInt(num);
						  out.print("from java, file size = "+num+"\n");
						  out.flush();
                          int bytesRead;
                          int current;
						  byte [] mybytearray  = new byte [filesize];
						  File tmp1=uniqueImageName();
						  logInput(Response.GETIMAGEFILESIZE,tmp1.getPath());

						  FileOutputStream fos = new FileOutputStream(tmp1); // destination path and name of file
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

    					  System.out.println("\ndone writing file");
						  bos.close();
						  out.print("_RECEIVED\n");
						  out.flush();
                          System.out.println("just done: number of bytes still in stream = "+is.available());
                          break;



                     case Response.GETSUBFRAME:
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
							}
						}
			           dataout.write(tmp);
					   dataout.flush();
					   break;

					 case Response.FUNCTIONWINDOWCOMMAND:
					   logInput(Response.FUNCTIONWINDOWCOMMAND, res);
					   runJythonCommand("remotecontrolcommand('"+res+"')");
					   break;

                     case Response.TAGRATING:
                       logInput(Response.TAGRATING, res);
                       break;

                     case Response.VOICECOMMENT:
                       logInput(Response.VOICECOMMENT,res);
                       break;

                     case Response.VOICECOMMAND:
                       logInput(Response.VOICECOMMAND,res);
					   runJythonCommand("remotecontrolcommand('"+res+"')");
                       break;

                     case Response.TEXTCOMMENT:
                       logInput(Response.TEXTCOMMENT,res);
                       break;

                     default:
                       System.out.println("res = "+res+" not understood");
                       logInput(Response.ERROR,"res = "+res+" not understood");
                       bad_response+=1;
                       if (bad_response>=10) connection_ok=false;
                       //connection_ok=false;

				    }
				  }
				  else{
						System.out.println("res = null or blank. Connection lost?");
						connection_ok=false;
			      }
			  if (echoSocket.isClosed()) {System.out.println("Socket closed"); connection_ok=false;}
			  if (!echoSocket.isConnected()) {System.out.println("Socket is not connected"); connection_ok=false;}
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

//keeping it here in case it doesn't get copied.
public String css_body="\n"
+".wrap-collabsible {			\n"
+"  margin-bottom: 1.2rem 0;	  \n"
+"}								\n"
+"								\n"
+"input[type='checkbox'] {		\n"
+"  display: none;				\n"
+"}								\n"
+"								\n"
+".lbl-toggle {					\n"
+"  display: block;				\n"
+"								\n"
+"  font-weight: bold;			\n"
+"  font-family: monospace;		\n"
+"  font-size: 1.0rem;			\n"
+"  text-transform: uppercase;	\n"
+"  text-align: left;			\n"
+"								\n"
+"  padding: 0.25rem;		    \n"
+"								\n"
+"  color: #A77B0E;				\n"
+"  background: #CBE8FA;		\n"
+"								\n"
+"  cursor: pointer;			\n"
+"	width : 660px;				 \n"
+"  border-radius: 7px;				\n"
+"  transition: all 0.25s ease-out;	\n"
+"}									\n"
+"									\n"
+".lbl-toggle:hover {				\n"
+"  color: #7C5A0B;					\n"
+"}									\n"
+"									\n"
+".lbl-toggle::before {				\n"
+"  content: ' ';					\n"
+"  display: inline-block;					 \n"
+"											 \n"
+"  border-top: 5px solid transparent;		 \n"
+"  border-bottom: 5px solid transparent;	 \n"
+"  border-left: 5px solid currentColor;	 \n"
+"  vertical-align: middle;					 \n"
+"  margin-right: .7rem;					 \n"
+"  transform: translateY(-2px);			 \n"
+"											 \n"
+"  transition: transform .2s ease-out;		 \n"
+"}											 \n"
+"											 \n"
+".toggle:checked + .lbl-toggle::before {	 \n"
+"  transform: rotate(90deg) translateX(-3px); \n"
+"}												 \n"
+"												 \n"
+".collapsible-content {						  \n"
+"  max-height: 0px;							  \n"
+"  overflow: hidden;							 \n"
+"  transition: max-height .25s ease-in-out;     \n"
+"}												 \n"
+"												 \n"
+".toggle:checked + .lbl-toggle + .collapsible-content { \n"
+"  max-height: 600px;					 \n"
+"}										 \n"
+"										 \n"
+".toggle:checked + .lbl-toggle {		 \n"
+"  border-bottom-right-radius: 0;		 \n"
+"  border-bottom-left-radius: 0;		 \n"
+"}										 \n"
+"										 \n"
+".collapsible-content .content-inner {	 \n"
+"  background: rgba(219, 237, 248, .2); \n"
+"  border-bottom: 1px solid rgba(250, 224, 66, .45);	 \n"
+"  border-bottom-left-radius: 7px;						 \n"
+"  border-bottom-right-radius: 7px;					 \n"
+"  padding: .5rem 1rem;	width : 640px;			     \n"
+"   }													 \n"
+ "table { width: 100% }\n"
+ "th { font:bold 10pt Tahoma; }\n"
+ "td { font:normal 10pt Tahoma; }\n"
+ "h1 {font:normal 11pt Tahoma;}\n"
+ ".usertag {\n"
+ " background: rgba(250, 224, 66, .2);\n"
+ " margin: 5px;\n"
+ " padding: 5px;\n"
+ "font-family: monospace;\n"
+ "}\n";

}

class MyHtmlFormatter extends Formatter {
    // this method is called for every log records
    int rowcount=0;

    public String[] getKeyValue(String input){
		String[] tmp=input.split("=");
		if (tmp.length>1){
		 String c0=tmp[1].replace("><",",");
		 String c1=c0.replace('>',' ');
		 String c2=c1.replace('<',' ');
		 tmp[1]=c2;
	    }
		return tmp;
	}

    public String formatTags(String input){
		StringBuilder newstr=new StringBuilder();
		//*** TAGS ***,reference=notset.dat,title not set=< tag not set>,title not set=<NOT SET>,title not set=<NOT SET>,title not set=<NOT SET>,title not set=<NOT SET>,<rating = 4.0>,************,
	    //1 split on commas
	    String[] strs=input.split(",");
	    //2 sort through strs, and do something with lines that don't contain the words 'not set' or notset
	    for (int i=1;i<strs.length-1;i++){
			String tmp=strs[i].toLowerCase();
			if ((tmp.indexOf("not set")==-1) && (tmp.indexOf("notset")==-1)){
				if (tmp.indexOf("reference")!=-1){
					String[] kv=getKeyValue(tmp);
					if (kv.length>1){
					newstr.append("<b><u>");
					newstr.append(kv[1]);
					newstr.append("</b></u><br>\n");
				    }
				 }
				if (tmp.indexOf("rating")!=-1){
					String[] rt=getKeyValue(tmp);
					if (rt.length>1){
					int v = (int)(Double.parseDouble(rt[1]));
					newstr.append("<br>\n");
					for (int p=0;p<v;p++){
						newstr.append("<span style='font-size:30px;' class='rating'>&#9733;</span>");
					}
					for (int q=0;q<5-v;q++){
						newstr.append("<span style='font-size:30px;' class='rating'>&#9734;</span>");
					}
					newstr.append("<br>\n");
				  }
				}
				else{
					String[] gv=getKeyValue(tmp);
					if (gv.length>1){
					newstr.append("<b>");
					newstr.append(gv[0]);
		 			newstr.append(":</b>");
		            String[] c4=gv[1].split(",");

		           for (int k=0;k<c4.length;k++){
			        newstr.append("<span class='usertag'>");
			        newstr.append(c4[k]);
			        newstr.append("</span> ");
		            }

					newstr.append("<br>\n");
				   }

				}

		   }
		}
		return newstr.toString();
	}
	int collapsableindex=0;
    public String format(LogRecord rec) {
		rowcount+=1;
        StringBuffer buf = new StringBuffer(1000);
        buf.append("<tr>\n");
        buf.append("\t<td valign='top'>");
        buf.append(rowcount);
        buf.append("</td>\n");
        buf.append("\t<td valign='top'>");
        buf.append(rec.getLevel());
        buf.append("</td>\n");
        buf.append("\t<td valign='top'>");
        buf.append(calcDate(rec.getMillis()));
        buf.append("</td>\n");
        int cl=rec.getLevel().intValue();
        switch(cl){
			case Response.GETIMAGEFILESIZE:
			  collapsableindex+=1;
        	  buf.append("\t<td>");
			  String tmp=rec.getMessage();
			  buf.append("<div class='wrap-collabsible'>\n");
              buf.append("<input id='collapsible");
              buf.append(collapsableindex);
              buf.append("' class='toggle' type='checkbox'>\n");
              buf.append("<label for='collapsible");
              buf.append(collapsableindex);
              buf.append("' class='lbl-toggle'>image (remote)</label>\n");
              buf.append("<div class='collapsible-content'>\n");
              buf.append("<div class='content-inner'>\n");

			  buf.append("<img src='");
			  buf.append(tmp);
			  buf.append("' height='520' width='640'>");
			  buf.append("<br>\n");
			  buf.append(tmp);
			  buf.append("\n</div></div></div>\n");
			  break;
			case Response.TAGRATING:
			  buf.append("\t<td>");
			  buf.append(formatTags(rec.getMessage()));
			  buf.append("</td>\n");
			  /*
			  String c1=rec.getMessage().replace('>',' ');
			  String c2=c1.replace('<',' ');
			  String[] tmpc=c2.split(",");
			  buf.append("\t<td>");
			  buf.append("<b>");
			  buf.append(tmpc[2]);
			  buf.append("</b><br>\n");

			  for (int k=3;k<tmpc.length-2;k++){
			    buf.append(tmpc[k]);
			    buf.append("<br>\n");
			  }
              buf.append("<b>");
              buf.append(tmpc[tmpc.length-2]);
              buf.append("</b>\n");
              */
			  break;
			default:
		      buf.append("\t<td valign='top'>");
              buf.append(formatMessage(rec));
	      }
		buf.append("</td>\n");
        buf.append("</tr>\n");

        return buf.toString();
    }

    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }

    // this method is called just after the handler using this
    // formatter is created
    public String getHead(Handler h) {
        return "<!DOCTYPE html>\n<head>\n<link rel='stylesheet' type='text/css' href='logging.css'>\n"
            + "</head>\n"
            + "<body  onload='scrollbottom()'>\n"
            + "<script type='text/javascript'>\n"
            + "function scrollbottom(){window.scrollTo(0,document.body.scrollHeight);}\n"
            + "</script>\n"
            + "<h1>" + (new Date()) + "</h1>\n"
            + "<table border=\"0\" cellpadding=\"5\" cellspacing=\"3\">\n"
            + "<tr align=\"left\">\n"
            + "\t<th style=\"width:5%\">Entry</th>\n"
            + "\t<th style=\"width:10%\">Type</th>\n"
            + "\t<th style=\"width:10%\">Time</th>\n"
            + "\t<th style=\"width:75%\">Log Message</th>\n"
            + "</tr>\n";
      }

    // this method is called just after the handler using this
    // formatter is closed
    public String getTail(Handler h) {
        return "</table>\n</body>\n</html>";
    }
}























