/*
 *
 */
package gvdecoder;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import javax.accessibility.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;


import javax.swing.text.html.parser.*;


//import org.w3c.dom.Document;

/**
 <img src="file:///C:/DOCUME~1/Gil/MYDOCU~1/GViewFiles/SavedImages/a2008_81_1031_10005.jpg">

 */
public class HTMLReportEditor extends aFrame{

    JEditorPane html;
    JEditorPane txt;
    GView gv;
    JButton insertimg,resethtml;
    JPopupMenu popup;
    /**

     */

    public String getPlot(){
		//String name=AnalysisHelper.getAnalysisHelper().saveImage(gv.jh.pl.p.getImage());
	    String name=null;
	    if ((gv.jh.sv==null)&&(gv.jh.pl==null)) return null;

	    if (gv.jh.sv!=null)
	      name=gv.jh.sv.saveHtml();
	    else
	      name=gv.jh.pl.saveHtml();
	    //return "<img src='file:///C:/DOCUME~1/Gil/MYDOCU~1/GViewFiles/SavedImages/"+AnalysisHelper.getAnalysisHelper().last_name+"'>";
	    return name;
	}

 	public void insertHTML(String str){
		try{
		HTMLEditorKit kit=(HTMLEditorKit)html.getEditorKit();
		Document doc=html.getDocument();
		StringReader reader=new StringReader(str);
		int loc=html.getCaret().getDot();
		kit.read(reader,doc,loc);
	    }catch(Exception e){
			e.printStackTrace();
		}
	}

	public void insertHTML(String str, int loc){
			try{
			HTMLEditorKit kit=(HTMLEditorKit)html.getEditorKit();
			Document doc=html.getDocument();
			StringReader reader=new StringReader(str);
			kit.read(reader,doc,loc);
		    }catch(Exception e){
				e.printStackTrace();
			}
	}

	public void insertLastPlot(){
		String htmlfragment=getPlot();
		if (htmlfragment!=null)
		 insertHTML(htmlfragment);
	}


    public String reformat(String text){
		int start=text.indexOf("<body");
		int end=text.indexOf("</body>");
		String headstring=text.substring(0,start);
		String bodystring=text.substring(start,end);
		String newbodystring=bodystring.replaceAll("\n","<br>");
		String result=headstring.concat(newbodystring);
		return result.concat("</body></html>");
	}

	public void savetextfile(String filename){
		   try{

		       PrintWriter file=new PrintWriter(new FileWriter(filename),true);
		   	   file.print(html.getText());
		   	   //file.print(reformat(html.getText()));
		   	   file.close();
		   	}
			catch(Exception e){System.out.println("error, see stdout");e.printStackTrace();}

	   }



	   public void save(){
		    try{
		      gv.fc.openFile(gv.fc.ScriptDir);;
		      if (gv.fc.approved) savetextfile(gv.fc.getAbsolutePath());
		  }catch(Exception e){System.out.println("error, see stdout"); e.printStackTrace();}


	   }


     public void load(){}
     public void clear(){}

   HTMLDocument.Iterator iterator=null;
   public void findCodeBlocks(){
	   if ((iterator==null)||(!iterator.isValid())){
		   iterator=((HTMLDocument)html.getDocument()).getIterator(HTML.Tag.CODE);
	   }else{
	   iterator.next();
       }
	   if (iterator.isValid()){
	   int start=iterator.getStartOffset();
	   int end=iterator.getEndOffset();
	   html.getCaret().setDot(start);
	   html.getCaret().moveDot(end);
	   html.grabFocus();

       }
	   //System.out.println("start="+start+" end="+end);
   }




    public HTMLReportEditor(GView gv, boolean newreport) {
        // Set the title for this demo, and an icon used to represent this
        // demo inside the SwingSet2 app.
         super("report editor");

/*
        try {
	    URL url = null;
	    // System.getProperty("user.dir") +
	    // System.getProperty("file.separator");
	    String path = null;
	    try {
		path = gv.ph.getStringProperty("User dir", "gvdecoder");

		url = getClass().getResource(path);
            } catch (Exception e) {
		System.err.println("Failed to open " + path);
		url = null;
            }

            if(url != null) {
                html = new JEditorPane(url);
                html.setEditable(true);
                //html.addHyperlinkListener(createHyperLinkListener());
*/
		this.gv=gv;
		//html=new JEditorPane("text/html","<html><head><title>GView report</title></head><body><h1>report</h1><br>text<br></body></html>");
		try{
		if (newreport){
		String path=(String)gv.ph.prp.get("User dir");
		path=path+File.separator+"GViewReportEditorTemplate.html";
		File template=new File(path);
		html=new JEditorPane(template.toURI().toURL());
	    }else{
			File htmlfile=new File(gv.fc.openFile(gv.ph.getProperty("User dir",".")));
			html=new JEditorPane(htmlfile.toURI().toURL());
		}

		}catch(Exception e){
		 e.printStackTrace();
		 html=new JEditorPane("text/html","<html><head><title>GView report</title></head><body><h1>Error</h1><br><b>Warning: A file called 'GViewReportEditorTemplate.html' could not be found in your 'User directory' directory.<br> Please ensure it exists, and check paths in Prefs and Controls->Directories menu.<br> Close this window and reopen once fixed... <br></body></html>");
		 System.out.println("Editor default used, URL not valid");
		}
		JScrollPane scroller = new JScrollPane(html);

	     JButton loadtext=new JButton("load");
	        loadtext.addActionListener(new ActionListener(){
						 public void actionPerformed(ActionEvent evt){
						   load();
				 }});

	     JButton savetext=new JButton("save");
	        savetext.addActionListener(new ActionListener(){
						 public void actionPerformed(ActionEvent evt){
						   save();
				 }});

	     JButton cleartext=new JButton("clear");
	        cleartext.addActionListener(new ActionListener(){
						 public void actionPerformed(ActionEvent evt){
						   clear();
				 }});

	     JButton codeblock=new JButton("code");
	        codeblock.addActionListener(new ActionListener(){
						 public void actionPerformed(ActionEvent evt){
						   findCodeBlocks();
				 }});

		 insertimg=new JButton("insert img");
	        insertimg.addActionListener(new ActionListener(){
						 public void actionPerformed(ActionEvent evt){
						   insertLastPlot();
				 }});
		 insertimg.setEnabled(false);


		resethtml=new JButton("show text");
	        resethtml.addActionListener(new ActionListener(){
						 public void actionPerformed(ActionEvent evt){
						   resetHtml();
				 }});

		JPanel bottombar=new JPanel();
		bottombar.add(loadtext);
		bottombar.add(savetext);
		bottombar.add(cleartext);
		bottombar.add(codeblock);
        bottombar.add(insertimg);
		bottombar.add(resethtml);

	    this.getContentPane().add(scroller,BorderLayout.CENTER);
	    this.getContentPane().add(bottombar,BorderLayout.SOUTH);

		this.setSize(400,200);

		 popup=new JPopupMenu();
		 JMenu submenu=new JMenu("run selection");

		 JMenuItem menuItem = new JMenuItem("as script");
		 menuItem.setToolTipText("... if selection contains function definitions");

		 menuItem.addActionListener(new ActionListener(){
			         public void actionPerformed(ActionEvent evt){
						 runSelection();
					 }
			 });
		 submenu.add(menuItem);
		 menuItem = new JMenuItem("through console");
		 menuItem.setToolTipText("...if selection should output to Jython console");
		 menuItem.addActionListener(new ActionListener(){
		 			         public void actionPerformed(ActionEvent evt){
		 						 runSelectionLineByLine();
		 					 }
		 			 });
		 submenu.add(menuItem);
		 popup.add(submenu);
         popup.addSeparator();
         submenu=new JMenu("format");
		 menuItem = new JMenuItem("header");
		 menuItem.addActionListener(new ActionListener(){
					 public void actionPerformed(ActionEvent evt){
						 formatSelection("header");
					 }
		 });
		 submenu.add(menuItem);
		 menuItem = new JMenuItem("code");
		 menuItem.addActionListener(new ActionListener(){
					 public void actionPerformed(ActionEvent evt){
						 formatSelection("code");
					 }
		 });
		 submenu.add(menuItem);
		 menuItem = new JMenuItem("italics");
		 		 menuItem.addActionListener(new ActionListener(){
		 					 public void actionPerformed(ActionEvent evt){
		 						 formatSelection("italics");
		 					 }
		 });
		 submenu.add(menuItem);
		 menuItem = new JMenuItem("normal");
		 menuItem.addActionListener(new ActionListener(){
					 public void actionPerformed(ActionEvent evt){
						 formatSelection("normal");
					 }
		 });
		 submenu.add(menuItem);
		 popup.add(submenu);

        MouseListener popupListener = new PopupListener();
		html.addMouseListener(popupListener);
		//gv.desktop.add(this);
		//this.show();
		display();
/*
         }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e);
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
*/
    }

    public void setSaveDescription(String str){
		insertimg.setText(str);
		repaint();
	}

	public boolean isHTML=true;
	public void resetHtml(){
    if (isHTML){
		showText();
		resethtml.setText("show html");
		isHTML=false;
	}else{
		showHTML();
		resethtml.setText("show text");
		isHTML=true;
	}
    }


	public int caretposition=0;
	public void showText(){
		caretposition=html.getCaretPosition();
		String str=html.getText();
		String fix1=str.replaceAll("&lt;","<");
		String fix2=fix1.replaceAll("&gt;",">");
		String fix3=fix2.replaceAll("&quot;","\"");
		html.setContentType("text/plain");
		html.setText(fix3);
		try{html.setCaretPosition(caretposition);}catch(IllegalArgumentException e){;}
	}

	public void showHTML(){
		caretposition=html.getCaretPosition();
		String str=html.getText();
		html.setContentType("text/html");
		html.setText(str);
		try{html.setCaretPosition(caretposition);}catch(IllegalArgumentException e){;}
	}

	public void formatSelection(String format){
	 String str=html.getSelectedText();
	 int p1=html.getCaret().getMark();
	 int p2=html.getCaret().getDot();

	 if (format.equals("header")){
	 	html.cut();
	 	String newstring="</pre><h2>"+str+"</h2><pre>";
	 	insertHTML(newstring);
	 }else
	 if (format.equals("code")){
		html.cut();
		insertHTML("<pre><code color='#000080'>"+str+"</code></pre>");

	 }else
	 if (format.equals("italics")){
		insertHTML("<i>",p1);
		insertHTML("</i>",p2);
	  }else
	 if (format.equals("normal")){
		//String newstring="</pre><i>"+str+"</i><pre>";
		html.cut();
		insertHTML(str);

	 }

	}

	public void runSelection(){
		System.out.println("called runselection");
		if (gv.jv!=null){
			gv.jv.interp.exec(html.getSelectedText());
		}
	}

	public boolean JythonReady(){
	 return (gv.jv.jythonthread == null || !gv.jv.jythonthread.isAlive());
	}

	public void runSelectionLineByLine(){
		String strings=html.getSelectedText();
		String [] strs=strings.split("\n");
		for (int i=0;i<strs.length;i++){
			gv.jv.textField.setText(strs[i]);
		    while (!JythonReady()){
			 try{Thread.sleep(100);}catch(Exception e){e.printStackTrace();}
			}
		    gv.jv.processText();

		}
	}

public HTMLEditorKit.ParserCallback callback;
public void setupParser(){
	 callback = new ParseTest();
}


    public HyperlinkListener createHyperLinkListener() {
	return new HyperlinkListener() {
	    public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		    if (e instanceof HTMLFrameHyperlinkEvent) {
			((HTMLDocument)html.getDocument()).processHTMLFrameHyperlinkEvent(
			    (HTMLFrameHyperlinkEvent)e);
		    } else {
			try {
			    html.setPage(e.getURL());
			} catch (IOException ioe) {
			    System.out.println("IOE: " + ioe);
			}
		    }
		}
	    }
	};
    }

    class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}

public class ParseTest extends HTMLEditorKit.ParserCallback {


  public void handleText(char[] data, int pos){

    System.out.println(new String(data));

  }



  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos){

    System.out.println("start: " + t);

  }



  public void handleEndTag(HTML.Tag t, int pos){

    System.out.println("end: " + t);

  }



  public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos){



    if (t == HTML.Tag.META){

      String name1=(String)a.getAttribute(HTML.Attribute.NAME);

      if (name1 != null){

        System.out.println("META name1: " + name1);

      }

      String content1 = (String)a.getAttribute(HTML.Attribute.CONTENT);

      if (content1 != null){

        System.out.println("META content1: " + content1);

      }

    }

  }


}

}