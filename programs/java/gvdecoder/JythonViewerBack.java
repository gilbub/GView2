package gvdecoder;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.Border;
import org.python.core.*;
import org.python.core.Py.*;
import org.python.core.PySystemState;
import org.python.util.*;
import java.io.*;
import java.awt.font.*;
import java.util.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.CompoundBorder;
import java.util.Stack;

/**


**/

public class JythonViewer extends JPanel implements Runnable, ItemListener, KeyListener, ListSelectionListener{
    public PythonPreprocessor preprocessor=new PythonPreprocessor();
    public PythonInterpreter interp;
    public JTextArea console;
    public JTextArea editor;
    public JTextArea linenumbers;

    public JTextArea javaeditor;
    public JTextArea javalinenumbers;

    String startFile;
    public JTextField textField;
    public GView gv;
    public JythonHelper2 jh;

    public jvMenuListener myViewMenuListener;

	Font command;
	Font result;
    public Vector commandlist;
    public Vector methodlist;
    int totalcommands=0;
    int historyindex=0;
    public JList hist;
    public JList methods;
    GeneratedListModel listModel;
    public JScrollPane histPane;
    public JScrollPane methodsPane;
    JPopupMenu popup;
    public JScrollPane scrollingarea2,javascrollingarea2;

   // JComboBox cb;
    public float freemem;
    public float usedmem;
    private Runtime r = Runtime.getRuntime();
	public RedirectConsole cs;

	public ImageDecoder im;
	public Viewer2 vw;
	public ImagePanel jp;
	public java.util.ArrayList rois;
	public ROI presentroi;
	public int[] pixels;

	public int[] imagefile_info=new int[4];
    public JTabbedPane tabbedPane;
    public JPanel editorPanel;
    public JPanel javaeditorPanel;
    private Vector nullvector=new Vector();
    public final java.util.Timer timer=new java.util.Timer();
    public JPopupMenu infopop;
    public JPopupMenu notifypop;
    public JLabel notifylabel;
    public GraphButton graphbutton;
    public JLabel statuslabel;
    public boolean USEMINIGRAPH=false;

    private Highlighter.HighlightPainter[] hilitecolors;



    public JythonViewer(String startup, GView gv, JythonHelper2 jh) {

		this.startFile = startup;
		this.gv=gv;
		this.jh=jh;

        this.setLayout(new BorderLayout());
        this.console = new JTextArea();
        this.console.setDragEnabled(true);
        this.console.setTabSize(5);
        JScrollPane scrollingarea=new JScrollPane();
        JViewport prt=scrollingarea.getViewport();
        //this.console.setPreferredSize(new Dimension(200, 600));
        prt.add(console);
        hilitecolors=new Highlighter.HighlightPainter[10];
        hilitecolors[0]=new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);
        hilitecolors[1]=new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
        hilitecolors[2]=new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);
        hilitecolors[3]=new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);
        hilitecolors[4]=new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        hilitecolors[5]=new DefaultHighlighter.DefaultHighlightPainter(Color.MAGENTA);
        hilitecolors[6]=new DefaultHighlighter.DefaultHighlightPainter(Color.PINK);
        hilitecolors[7]=new DefaultHighlighter.DefaultHighlightPainter(Color.GRAY);
        hilitecolors[8]=new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
        hilitecolors[9]=new DefaultHighlighter.DefaultHighlightPainter(Color.BLUE);
        //orangePainter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);
        //cyanPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);

        /*
        jython script editor
        */
        this.editor = new JTextArea();
		this.editor.setDragEnabled(true);
		this.editor.setTabSize(4);
		String lnumbers=" ";
        this.linenumbers=new JTextArea(lnumbers,20,5);
        this.linenumbers.setForeground(new Color(180,180,200));
        this.linenumbers.setBackground(new Color(220,220,240));
        JPanel lineplusedit=new JPanel();
        lineplusedit.setLayout(new BorderLayout());
		lineplusedit.add(linenumbers,BorderLayout.WEST);
		lineplusedit.add(editor,BorderLayout.CENTER);
		//JScrollPane
		scrollingarea2=new JScrollPane();
        JViewport prt2=scrollingarea2.getViewport();

        prt2.add(lineplusedit);
        editorPanel=new JPanel();
        editorPanel.setLayout(new BorderLayout());
        editorPanel.add(scrollingarea2,BorderLayout.CENTER);
        JPanel buttons=new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createRigidArea(new Dimension(10,1)));
        //EmptyBorder eb = new EmptyBorder(5,5,5,5);
		SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.RAISED);
        buttons.setBorder(sbb);

        JButton runtext=new JButton("run all");
        runtext.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent evt){
			   editor_runAll();
			 }});
        /*
        JButton runselected=new JButton("run selected");
        runselected.addActionListener(new ActionListener(){
					 public void actionPerformed(ActionEvent evt){
					   editor_runSelected();
			 }});
		*/

        JButton loadtext=new JButton("load");
        loadtext.addActionListener(new ActionListener(){
					 public void actionPerformed(ActionEvent evt){
					   editor_load();
			 }});

        JButton savetext=new JButton("save");
        savetext.addActionListener(new ActionListener(){
					 public void actionPerformed(ActionEvent evt){
					   editor_save();
			 }});
        JButton cleartext=new JButton("clear");
        cleartext.addActionListener(new ActionListener(){
					 public void actionPerformed(ActionEvent evt){
					   editor_clear();
			 }});

	   JRadioButton highlightcurlybrace=new JRadioButton("{}");
	   highlightcurlybrace.addActionListener(new ActionListener(){
	   			 public void actionPerformed(ActionEvent evt){
				   //System.out.println("called highlightbrackets wuth {");
	   			   highlightbrackets('{','}',10);//highlightbrackets(char openbrace, char closebrace, int maxcount)
			 }});
	   JRadioButton highlightbrace=new JRadioButton("()");
	   highlightbrace.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent evt){
				//System.out.println("called highlightbrackets wuth (");
				 highlightbrackets('(',')',3);
			}});
	   JRadioButton highlightpunctuation=new JRadioButton(".,");
	   highlightpunctuation.addActionListener(new ActionListener(){
		   public void actionPerformed(ActionEvent evt){
				 highlightpunctuation();
	   }});
	   JRadioButton clearhighlights=new JRadioButton("c");
	   clearhighlights.addActionListener(new ActionListener(){
	   		   public void actionPerformed(ActionEvent evt){
	   				 clearhighlights();
	   }});
	   ButtonGroup group=new ButtonGroup();
	   group.add(highlightcurlybrace);
	   group.add(highlightbrace);
	   group.add(highlightpunctuation);
	   group.add(clearhighlights);



        buttons.add(runtext);
        //buttons.add(runselected);
        buttons.add(loadtext);
        buttons.add(savetext);
        buttons.add(cleartext);
        buttons.add(new JLabel(" "));

        buttons.add(highlightcurlybrace);
        buttons.add(highlightbrace);
        buttons.add(highlightpunctuation);
        buttons.add(clearhighlights);
        buttons.add(new JLabel(" "));
		statuslabel= new JLabel("  ");
        buttons.add(statuslabel);

        editorPanel.add(buttons,BorderLayout.SOUTH);

        /* java editor*/

		this.javaeditor = new JTextArea();
		this.javaeditor.setDragEnabled(true);
		this.javaeditor.setTabSize(4);
		String javalnumbers=" ";
		this.javalinenumbers=new JTextArea(javalnumbers,20,5);
		this.javalinenumbers.setForeground(new Color(180,180,200));
		this.javalinenumbers.setBackground(new Color(220,220,240));
		JPanel javalineplusedit=new JPanel();
		javalineplusedit.setLayout(new BorderLayout());
		javalineplusedit.add(javalinenumbers,BorderLayout.WEST);
		javalineplusedit.add(javaeditor,BorderLayout.CENTER);
		//JScrollPane
		javascrollingarea2=new JScrollPane();
		JViewport javaprt2=javascrollingarea2.getViewport();

		javaprt2.add(javalineplusedit);
		javaeditorPanel=new JPanel();
		javaeditorPanel.setLayout(new BorderLayout());
		javaeditorPanel.add(javascrollingarea2,BorderLayout.CENTER);
		JPanel javabuttons=new JPanel();
		javabuttons.setLayout(new BoxLayout(javabuttons, BoxLayout.X_AXIS));
		javabuttons.add(Box.createRigidArea(new Dimension(10,1)));
		//EmptyBorder eb = new EmptyBorder(5,5,5,5);
		SoftBevelBorder javasbb = new SoftBevelBorder(SoftBevelBorder.RAISED);
		buttons.setBorder(javasbb);

		JButton javaruntext=new JButton("compile");
		runtext.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent evt){
		 javaeditor_compile();
		}});


		JButton javaloadtext=new JButton("load");
		loadtext.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent evt){
			   javaeditor_load();
		}});

		JButton javasavetext=new JButton("save");
		savetext.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent evt){
			   javaeditor_save();
		}});
		JButton javacleartext=new JButton("clear");
		cleartext.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent evt){
			   javaeditor_clear();
		}});


		javabuttons.add(javaruntext);
		//javabuttons.add(javarunselected);
		javabuttons.add(javaloadtext);
		javabuttons.add(javasavetext);
		javabuttons.add(javacleartext);

		javaeditorPanel.add(javabuttons,BorderLayout.SOUTH);
        /* */


        tabbedPane=new JTabbedPane();
        tabbedPane.add("console",scrollingarea);
        tabbedPane.add("scripts",editorPanel);

        this.add(tabbedPane, BorderLayout.CENTER);
        /*
        JPanel botpanel=new JPanel();
        botpanel.setPreferredSize(new Dimension(210,130));
        JPanel showpanel=new JPanel();
        double[] tst=new double[100];
        for (int i=0;i<100;i++){ tst[i]=i;}
        graphbutton=new GraphButton(tst);
        showpanel.add(graphbutton);
        showpanel.setPreferredSize(new Dimension(200,100));
        botpanel.add(showpanel,BorderLayout.CENTER);

        */
       this.textField = new JTextField();
	   this.textField.setPreferredSize(new Dimension(200, 20));

        //botpanel.add(textField,BorderLayout.SOUTH);
        this.add(textField,BorderLayout.SOUTH);
        //this.add(this.textField, BorderLayout.SOUTH);
        this.textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                processText();
            }});

        this.textField.addKeyListener(this);

        result=new Font("monospaced",Font.PLAIN,12);
    	commandlist=new Vector();
        methodlist=new Vector();

        methods=new JList(methodlist);


		methods.setVisibleRowCount(5);
		methods.addListSelectionListener(this);
		methodsPane = new JScrollPane(methods);
		JScrollPane mscroller = new JScrollPane();
		JViewport mport = mscroller.getViewport();
		mport.add(methods);
		mscroller.addKeyListener(this);
		tabbedPane.add("methods",mscroller);
        nullvector.add("0 scripts must be run first");
        nullvector.add("0 before methods are found");


    	//hist=new History();
         hist=new JList();
         listModel = new GeneratedListModel(this);
		 hist.setModel(listModel);

         hist.setVisibleRowCount(5);
         hist.addListSelectionListener(this);
    	 histPane = new JScrollPane(hist);
    	 JScrollPane scroller = new JScrollPane();
	     JViewport port = scroller.getViewport();
	     port.add(hist);
	     tabbedPane.add("history",scroller);
         tabbedPane.add("java",javaeditorPanel);

        //jvMenuListener
        myViewMenuListener=new jvMenuListener(this);
		jvListener jvlistener = new jvListener(this);
	    console.addMouseListener(jvlistener);
	    console.addMouseMotionListener(jvlistener);

	    editor.addMouseListener(jvlistener);
	    editor.addMouseMotionListener(jvlistener);

	    console.setFont(result);
	    editor.setFont(result);
	    javaeditor.setFont(result);
	    linenumbers.setFont(result);
	    //console.setTabs(3);
	    popup=new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("goto line");
		menuItem.addActionListener(myViewMenuListener);
		popup.add(menuItem);

		menuItem = new JMenuItem("run selected");
		menuItem.addActionListener(myViewMenuListener);
		popup.add(menuItem);
		menuItem = new JMenuItem("save text");
		menuItem.addActionListener(myViewMenuListener);
		menuItem = new JMenuItem("cancel job");
		menuItem.addActionListener(myViewMenuListener);
		popup.add(menuItem);



		//JMenu submenu=new JMenu("history");
       // submenu.add(scroller);
       // popup.add(submenu);
    	//this.pack();

        infopop=new JPopupMenu();
        double[] tmp=new double[121];
        for (int k=0;k<121;k++){tmp[k]=k%23;}
        JPanel tmppan=new JPanel();
        graphbutton=new GraphButton(tmp);
        tmppan.add(graphbutton);
        infopop.add(tmppan);

        notifypop=new JPopupMenu();
        notifylabel=new JLabel("        ");
        notifypop.add(notifylabel);



        this.show();

    }

  /*
    else if (source.getText().startsWith("find")){vi.findSelectedText();}
      else if (source.getText().startsWith("next")){vi.findNextSelectedText();}
      else if (source.getText().equals("set mark")){vi.setMark();}
      else if (source.getText().equals("goto mark")){vi.gotoMark();}
    else if (source.getText().equals("clear marks")){vi.clearMarks();}
    */




  public int lastfoundposition=0;
  public int findSelectedText(){
	int pos=editor.getText().indexOf(searchtext);
	if (pos>0){
	try{
	//editor.setCaretPosition(pos);
	jumpto(pos);
	lastfoundposition=pos;
	Highlighter hilite=editor.getHighlighter();
	hilite.addHighlight(pos,pos+searchtext.length(),new DefaultHighlighter.DefaultHighlightPainter(Color.orange));
    }catch(Exception e){;}
    }
    return pos;
  }

  public int findNextSelectedText(){
	int pos=editor.getText().indexOf(searchtext,lastfoundposition+1);
	if (pos<0) pos=editor.getText().indexOf(searchtext);
	if (pos>0){
	try{
	//editor.setCaretPosition(pos);
	jumpto(pos);
	lastfoundposition=pos;
	Highlighter hilite=editor.getHighlighter();
	hilite.addHighlight(pos,pos+searchtext.length(),new DefaultHighlighter.DefaultHighlightPainter(Color.orange));
    }catch(Exception e){;}
    }
    return pos;
  }


  public int jumpto=0;
  public Runnable doCaretPosition=new Runnable(){
	  public void run(){editor.setCaretPosition(jumpto);}
  };

  public int markposition=0;
  public int editorposition=0;
  public void setMark(){
	 //java.awt.Point p=scrollingarea2.getViewport().getViewPosition();
	 //p.translate(0,lastpopupypos);
	 markposition=linenumbers.viewToModel(new java.awt.Point(0,lastpopupypos));
	 editorposition=editor.viewToModel(new java.awt.Point(0,lastpopupypos));
     //editor.getLineOfOffset(gv.jv.editor.viewToModel(gv.jv.scrollingarea2.getViewport().getViewPosition()))
    try{
		linenumbers.setCaretPosition(markposition);

		Highlighter hilite=linenumbers.getHighlighter();
		hilite.addHighlight(markposition,markposition+5,new DefaultHighlighter.DefaultHighlightPainter(Color.blue));
    }catch(Exception e){;}

  }

  public void gotoMark(){
   jumpto(editorposition);
  }

  public boolean CANCEL=false;
  public void cancelJob(){
   CANCEL=true;
   System.out.println("CANCEL=true");
  }

  public int jumptopos=0;
  public void jumpto(int pos){
	  jumptopos=pos;
	  timer.schedule(new TimerTask(){
	  	  public void run(){
	  		  editor.setCaretPosition(jumptopos);
	  		  //timer.cancel();
	  	  }
  },150);
  }

  public void clearMarks(){
  }


  public int MiniInWindow=0;
  public int MiniAbiveWindow=1;
  public int MiniBelowWindow=2;
  public int MiniInGv=3;
  public int miniposition=0;

  public Rectangle minigraphposition;
  public void setminigraphposition(int x, int y, int w, int h){
	  minigraphposition=new java.awt.Rectangle(x,y,w,h);
  }
  public void clearminigraphposition(){
	  minigraphposition=null;
  }

  public void showNotifyPopup(String note){
	  notifylabel.setText(note);
	  notifypop.show(gv.desktop,15,20);

  }

  public void showInfoPopup(){
	int w,h,minw;
	if (USEMINIGRAPH){
	switch(miniposition){
		case 0:
	 			w=(int)(getWidth()/2.0);
	 			h=(int)(getHeight()/2.5);
				minw=graphbutton.getTextStringWidth();
				if (w<minw+40) w=minw+40;
	 			if (h<100) h=100;
     			graphbutton.setPreferredSize(w, h);
     			infopop.show(this,(int)(getWidth()-w), 40);
     			break;
	   case 1:
	            w=(int)getWidth();
	            h=150;
	            minw=graphbutton.getTextStringWidth();
	            if (w<minw+40) w=minw+40;
	            graphbutton.setPreferredSize(w,h);
	            infopop.show(this,-5,-170);
	            break;
	   case 2:
				w=(int)getWidth();
				h=150;
				minw=graphbutton.getTextStringWidth();
				if (w<minw+40) w=minw+40;
				graphbutton.setPreferredSize(w,h);
				infopop.show(this,-5,(int)getHeight()+10);
	            break;
	   case 3:
				w=(int)gv.desktop.getWidth()-40;
				h=150;
				minw=graphbutton.getTextStringWidth();
				if (w<minw+40) w=minw+40;
				graphbutton.setPreferredSize(w,h);
				infopop.show(gv.desktop,15,20);
	            break;

       default:
				w=(int)gv.desktop.getWidth()-40;
				h=150;
				minw=graphbutton.getTextStringWidth();
				if (w<minw+40) w=minw+40;
				graphbutton.setPreferredSize(w,h);
				infopop.show(gv.desktop,15,(int)gv.desktop.getHeight()-170);
	            break;

     }
     /*else
    {
	 graphbutton.setPreferredSize(minigraphposition.width,minigraphposition.height);
	 infopop.show(gv.desktop,minigraphposition.x,minigraphposition.y);
	}
	*/
    textField.requestFocusInWindow();
    graphbutton.repaint();
    jh.setSaveHtml(graphbutton,"import series view");
   }
  }

   public String searchtext=null;
   int lastpopupypos=0;
   public JPopupMenu makeEditorPopup(int ypos){
       lastpopupypos=ypos;
	   JPopupMenu tmp=new JPopupMenu();
	   String findtext=editor.getSelectedText();

	   if (findtext!=null){
	    JMenuItem menuItem=new JMenuItem("find "+findtext);
	    menuItem.addActionListener(myViewMenuListener);
	    tmp.add(menuItem);
	    tmp.addSeparator();
	    searchtext=findtext;
	    }else{
		 if (searchtext!=null){
		JMenuItem menuItem=new JMenuItem("next "+searchtext);
	    menuItem.addActionListener(myViewMenuListener);
	    tmp.add(menuItem);
	    tmp.addSeparator();


		 }

		}

	    JMenuItem menu1=new JMenuItem("set mark");
	    menu1.addActionListener(myViewMenuListener);
	    tmp.add(menu1);
	    JMenuItem menu2=new JMenuItem("goto mark");
	    menu2.addActionListener(myViewMenuListener);
	    tmp.add(menu2);
        JMenuItem menu3=new JMenuItem("clear marks");
	    menu3.addActionListener(myViewMenuListener);
	    tmp.add(menu3);


	    return tmp;
   }
  /** utility method for opening an imagedecoder without viewing. Usefull
      in scripts. This will automatically call ReturnXYBandsFrames and dump
      it into an array called imagefile_info.
   **/
  public int instance=0; //instance is used to keep track of .spe files using the old .dll

  public ImageDecoder openImageDecoder(String absolutefilename, String filetype){
	   if (absolutefilename==null) return null;
   	   ImageDecoder im=ImageDecoderFactory.getDecoder(filetype);
       instance=im.OpenImageFile(absolutefilename);
       im.ReturnXYBandsFrames(imagefile_info,instance);
       print("file "+absolutefilename+"\n of type "+filetype+": (x,y,bands,frames)=("+imagefile_info[0]+","+imagefile_info[1]+","+imagefile_info[2]+","+imagefile_info[3]+")");
       return im;
       }

  /** utility method for opening an imagedecoder without viewing. Usefull
      in scripts. calls the file chooser in GView
   **/

   public ImageDecoder openImageDecoder(String filetype){

       return openImageDecoder(FilePicker.getFilePicker().openFile(),filetype);
   }


   public void editor_load(){opentextfile();}
   public void editor_save(){savetextfile();}

   public Vector editor_findMethodNames(){
	Vector results=new Vector();
	try{
		String s="";
		s=editor.getText();
		int index=0;
		int lastindex=0;
		int linenumber=0;
		/*do{
		  index=s.indexOf("\n",lastindex);
		  if (s.startsWith("def",lastindex+1)){
			    System.out.println( (s.substring(lastindex,index)+" linenumber ="+linenumber));
				results.add((s.substring(lastindex,index)+" linenumber ="+linenumber));
		  }
		  linenumber+=1;
		  lastindex=index;

		}while((index>-1)&&(linenumber<1000));
       */
       String[] arr=s.split("\n");
       for (int i=0;i<arr.length;i++){
		   if (arr[i].startsWith("def")){
			      System.out.println((i+1)+" " +(arr[i]));
			   	  results.add((i+1)+" "+(arr[i]));

		   }
	   }

    }catch(Exception e){e.printStackTrace();}
	methodlist=results;
	methods.setListData(methodlist);
	return results;
   }

public void formatEditor(){
	    editor_findMethodNames();
	    String tmp="";
	     for (int t=1;t<=editor.getLineCount();t++){
					 if (t<10) tmp+=t+"    \n"; else
					 if (t<100) tmp+=t+"   \n"; else
					 if (t<1000) tmp+=t+"  \n"; else
					 if (t<10000) tmp+=t+" \n"; else
					 tmp+=t+"\n";
				 }
	     linenumbers.setText(tmp);
}


public void gotoLine(){
	String s=console.getSelectedText();
	int ln=0;
    try{
   ln=Integer.parseInt(s);
   jumpto(editor.getLineStartOffset(ln)-1);
   tabbedPane.setSelectedComponent(editorPanel);
   System.out.println("goto line"+ln);
	}catch(Exception e){e.printStackTrace();}


}

public void jumptobadline(){

if (last_pye instanceof org.python.core.PySyntaxError){
	             try{
                        org.python.core.PySyntaxError se=(org.python.core.PySyntaxError)last_pye;
                        interp.exec("gv.jv.last_pyeline=gv.jv.last_pye.lineno\n");
                        int ln=last_pyeline;
		 				editor.setCaretPosition(editor.getLineStartOffset(ln-1));
		 				Highlighter hilite=editor.getHighlighter();
		 				hilite.addHighlight(editor.getLineStartOffset(ln-1),editor.getLineStartOffset(ln),new DefaultHighlighter.DefaultHighlightPainter(Color.red));
		                 tabbedPane.setSelectedComponent(editorPanel);
                }catch(Exception ex){ex.printStackTrace();}
}

}

public PyException last_pye=null;
public int last_pyeline;
public void javaeditor_runAll(){;}
public void javaeditor_save(){;}
public void javaeditor_load(){;}
public void javaeditor_compile(){;}

public void javaeditor_runSelected(){;}
public void javaeditor_clear(){;}
public void editor_runAll(){
	  // editor_findMethodNames();
       formatEditor();

	   try{
		   String s=editor.getText();
		   preprocessor.process(s);
		   if(!preprocessor.parsesuccess){
			   console.append("\n problem parsing file... either missing matching @'s or script leads with java code\n");
			   console.append(preprocessor.parseerror);
			   console.append("\n");
			   return;
		   }

		   preprocessor.compile();
		   if (!preprocessor.compilesuccess){
			   console.append("\n problem compiling java snippets\n");
			   console.append(preprocessor.compileerror);
			   console.append("\n");
			   highlightbrackets('{','}',10);
			   //highlightbrackets('(',')',3);
			   console.append("\n\n in command box, ctrl-shift-{ and ctrl-shift-( highlight, ctrl-space clears.\n");
			   return;
		   }
		   console.append("\n successfully finished preprocessing\n");
           Highlighter hilite=editor.getHighlighter();
	       hilite.removeAllHighlights();

		 for (CodeSegment code:preprocessor.codes){

			   String str=code.basename+" = gvdecoder.scripts.anonclasses."+code.classname+"()";
			   console.append("   setting python variable to java class: "+str+" \n");
			   interp.exec(str);

		   }

		   interp.exec(preprocessor.pythonstring);
		   console.append("\n\n in command box, ctrl-shift-{ and ctrl-shift-( highlight, ctrl-space clears.\n");
		   console.append("\n python script loaded\n");

	       }catch(PyException pye){
			last_pye=pye;

			JOptionPane.showMessageDialog(
			   			  		    this,
			   			  		    pye.toString(),
			   			  		    "python script error",
			  		                 JOptionPane.WARNING_MESSAGE);
				   console.append("\n script window err:"+pye.toString());

				   console.setCaretPosition(editor.getDocument().getLength());
           jumptobadline();

		   } catch(IOException e){
			   console.append(" IO error during preprocessor compile command\n ");
			   e.printStackTrace();

		   }

		 }




   public void editor_runString(String s){
	   	   try{
	   		   interp.exec(s);
	   	       }catch(PyException pye){
	   			JOptionPane.showMessageDialog(
	   			   			  		    this,
	   			   			  		    pye.toString(),
	   			   			  		    "python script error",
	   			  		                 JOptionPane.WARNING_MESSAGE);
	   				   console.append("\n script window err:"+pye.toString());
	   				   console.setCaretPosition(editor.getDocument().getLength());
	   				   last_pye=pye;
	   				   jumptobadline();
		 }
   }

   public void editor_runSelected(){
	   String s=editor.getSelectedText();
	   try{
	   interp.exec(s);
       }catch(PyException pye){
			 editor.append("\n err:"+pye.toString());
			 editor.setCaretPosition(console.getDocument().getLength());
			 JOptionPane.showMessageDialog(
			  		    this,
			  		    pye.toString(),
			  		    "python script error",
			  		    JOptionPane.WARNING_MESSAGE);
			 	   console.append("\n script window err:"+pye.toString());
				   console.setCaretPosition(editor.getDocument().getLength());
				   last_pye=pye;
	   			   jumptobadline();

		 }
   }
   public void editor_clear(){
	   editor.selectAll();
	   editor.cut();
	   try{
       methods.setListData(nullvector);
       }catch(Exception e){;}
   }


   public void opentextfile(String filename){
	   clear();
	   try{
	   File file = new File(filename);

	   if (file!=null){
	   		   Reader in = new FileReader(file);
	   		   char[] buff = new char[4096];
	   		   int nch;
	   		   while ((nch = in.read(buff, 0, buff.length)) != -1) {
	   		   		editor.append(new String(buff, 0, nch));
	   		        editor.setCaretPosition(editor.getDocument().getLength());
	   		   		}
	   		   	in.close();
	   	   }else
	   	    print("Error opening file: file doesn't exist");
	     }catch(Exception e){print("error, see stdout"); e.printStackTrace();}


    formatEditor();
   }

   public String lastScriptDir=null;
   public void  opentextfile(){
	   try{
	   gv.fc.openFile(gv.fc.ScriptDir);
	   if (gv.fc.approved) opentextfile(gv.fc.getAbsolutePath());
	   }catch(Exception e){print("error, see stdout"); e.printStackTrace();}
   }

   public void savetextfile(String filename){
	   try{

	       PrintWriter file=new PrintWriter(new FileWriter(filename),true);
	   	   file.print(editor.getText());
	   	   file.close();
	   	}
		catch(Exception e){print("error, see stdout");e.printStackTrace();}

   }



   public void savetextfile(){
	    try{
	      gv.fc.openFile(gv.fc.ScriptDir);;
	      if (gv.fc.approved) savetextfile(gv.fc.getAbsolutePath());
	  }catch(Exception e){print("error, see stdout"); e.printStackTrace();}


   }


	Dimension dim=new Dimension(350,200);
    public Dimension getPreferredSize(){
		return dim;
	}



    public boolean show_progress=true;
    public void displayProgress(double fraction){
		if (show_progress){
              if (fraction<1.0) gv.jythonwindow.setTitle("progress = "+fraction);
              else gv.jythonwindow.setTitle("jython");
	}
    }
     public String mem(){
		 float freeMemory = (float) r.freeMemory();
         float totalMemory = (float) r.totalMemory();
	 	 String str=String.valueOf((int) totalMemory/1024) + "K allocated";
         str += " "+String.valueOf(((int) (totalMemory - freeMemory))/1024)+"K used";
        return str;
	}

     public void actionPerformed(ActionEvent e){
		 JComboBox jcb=(JComboBox)e.getSource();
		 textField.setText((String)jcb.getSelectedItem());


	 }

     public void itemStateChanged(ItemEvent e){
		 System.out.println("item event="+e);
		 if (e.getStateChange()==ItemEvent.SELECTED){
			  textField.setText((String)e.getItem());
		 }
	 }



     public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {

             JList theList = (JList)e.getSource();


             int index = theList.getSelectedIndex();
             if (theList.equals(hist)){
               if (index<commandlist.size())
                textField.setText((String)commandlist.elementAt(index));
               }
             if (theList.equals(methods)){
				String str=(String)methodlist.elementAt(index);
				String[] arr=str.split(" ");
				int pos=Integer.parseInt(arr[0]);
				System.out.println((String)methodlist.elementAt(index)+ "="+ pos);
                try{
				editor.setCaretPosition(str.length()-1);
				editor.setCaretPosition(editor.getLineStartOffset(pos-1));
				Highlighter hilite=editor.getHighlighter();
				hilite.removeAllHighlights();
				hilite.addHighlight(editor.getLineStartOffset(pos-1),editor.getLineStartOffset(pos),new DefaultHighlighter.DefaultHighlightPainter(Color.yellow));
                tabbedPane.setSelectedComponent(editorPanel);
                }catch(Exception ex){;}
			   }

		    }
		 }

     public void keyPressed(KeyEvent e)
	  {
		 int c = e.getKeyCode();

	     if (c==KeyEvent.VK_UP) {history(historyindex-1); e.consume();   }
	     if (c==KeyEvent.VK_DOWN) {history(historyindex+1); e.consume(); }
         if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (e.getKeyChar()=='{')){
		       highlightbrackets('{','}',10);
		       e.consume();
		 }
		 if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (e.getKeyChar()=='(')){
		 		       highlightbrackets('(',')',3);
		 		       e.consume();
		 }
		 if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (e.getKeyChar()==' ')){
		 		 		       clearhighlights();
		 		 		       e.consume();
		 }
	  }

	  public void keyTyped(KeyEvent e) {
       //System.out.println("key hit");

	  }

	  public void keyReleased(KeyEvent e)
      {}


   public void highlightbrackets(char openbrace, char closebrace, int maxcount){
	   Highlighter hilite=editor.getHighlighter();
	   hilite.removeAllHighlights();
	   String txt=editor.getText();
	   int count=0;
	   Highlighter.HighlightPainter p;
	   Stack<Integer> braces=new Stack<Integer>();
	   try{
	   for (int i=0;i<txt.length();i++){
		   char t=txt.charAt(i);
		   if (t==openbrace) braces.push(new Integer(i));
		   if ((t==closebrace)&&(!braces.isEmpty())) {

			   p=hilitecolors[count%maxcount];
			   count++;
			   hilite.addHighlight(i, i+1, p);
			   int mi=braces.pop().intValue();
			   hilite.addHighlight(mi,mi+1,p);
		   }
		   }

	 }catch(Exception e){e.printStackTrace();}
   }


 public void highlightpunctuation(){
	 Highlighter hilite=editor.getHighlighter();
	 hilite.removeAllHighlights();
	 String txt=editor.getText();
	  try{
	 	   for (int i=0;i<txt.length();i++){
	 		   char t=txt.charAt(i);
	 		   if (t=='.') hilite.addHighlight(i,i+1,hilitecolors[0]);
	 		   if (t==',') hilite.addHighlight(i,i+1,hilitecolors[1]);
	 		   if (t==':') hilite.addHighlight(i,i+1,hilitecolors[2]);
	 		   if (t==';') hilite.addHighlight(i,i+1,hilitecolors[3]);


	 		   }

	 	 }catch(Exception e){e.printStackTrace();}
   }

int lastsearchlocation=0;
public int find(String searchfor){
	Highlighter hilite=editor.getHighlighter();
	hilite.removeAllHighlights();
	String txt=editor.getText();
	int startindex=0;
	if (lastsearchlocation!=-1) startindex=lastsearchlocation;
    int pos=txt.indexOf(searchfor,startindex);
    try{
    if (pos!=-1){
		hilite.addHighlight(pos,pos+searchfor.length(),hilitecolors[0]);
		jumpto(pos);
		lastsearchlocation=pos+1;
	    if (lastsearchlocation>txt.length()) lastsearchlocation=0;
	    return lastsearchlocation;
	}
	if (pos==-1){
	 lastsearchlocation=0;
	 return 0;

	}
}catch(BadLocationException e){e.printStackTrace();}
  return -1;
}

 public void clearhighlights(){
	   Highlighter hilite=editor.getHighlighter();
	   hilite.removeAllHighlights();
 }

    public void clear(){
		console.selectAll();
		console.cut();
	}

   public void runtext(){
	   //when selected text is run via popup window.
	   String s=console.getSelectedText();
	    try{
	   	   interp.exec(s);
	          }catch(PyException pye){
	   			 console.append("\n err:"+pye.toString());
	   			 console.setCaretPosition(console.getDocument().getLength());
	   			 //last_pye=pye;
	   		     //jumptobadline();
		 }
	   interp.exec(s);

   }

	public String history(int val){
		historyindex=val;
		if (historyindex<0) historyindex=0;
		if (historyindex>=totalcommands) historyindex=totalcommands-1;
		String tmp=(String)commandlist.elementAt(historyindex);
		textField.setText(tmp);
		return tmp;

	}
	public void show_history(){
		clear();
		for (int i=0;i<totalcommands;i++){
			console.append(i+")\t"+(String)commandlist.elementAt(i)+"\n");
	     }
	}

  public int maxstringlength=5000;
  public String format(String str){
	 if (str==null) return "null";
	 if (str.length()>maxstringlength){
		return (str.substring(0,maxstringlength)).concat(" ...");
	 }
	 else return str;
  }

  public int maxconsolelength=100000;
  //captures stderr, stdout
   public void write(String str){
	   // checkconsolelength();
	    String s=format(str);
   		console.append("%\t"+s+"\n");
		console.setCaretPosition(console.getDocument().getLength());
		statuslabel.setText(s);
   	}

	public void print(String str){
		//checkconsolelength();
		String s=format(str);
		console.append(">\t"+s+"\n");
		console.setCaretPosition(console.getDocument().getLength());
		statuslabel.setText(s);
	}

	public void checkconsolelength(){
		int l=console.getCaretPosition();
		if (l>maxconsolelength){
			console.setText(console.getText().substring(l-(maxconsolelength/4),maxconsolelength));
			console.setCaretPosition(console.getDocument().getLength());
		}
	}

	 public void print(Object o){
		   	  print(""+ o.toString());
		   }


		   public void print(double d){
		   	//System.out.println("jython: "+d);
		      print(""+d);
		   }

		   public void print(float f){
		   	//System.out.println("jython: "+f);
		    print(""+f);
		   }

		   public void print(int i){
		   	//System.out.println("jython: "+i);
		     print(""+i);
		   }

		   public void print(int x, int y){
		   	//System.out.println("jython: "+x+" "+y);
		     print(""+x+","+y);
		   }

           int maxlen=10;
		   public void print(double[] d){
			   //graphbutton.xyd=new XYDrawer(d);
               //graphbutton.data=d;
               int l=d.length;
               if (l>maxlen) l=maxlen;
               String str="array l="+d.length+" ";
               for (int i=0;i<l;i++){
				  str= str.concat(" "+d[i]);
			   }
			   if (d.length>=l) str=str.concat("...");
			   print(str);
			   if (d.length>10){
			    graphbutton.setData(d);
			    showInfoPopup();
			   }

		   }


/*
		   public void print(double[] d){
		   	print(""+JSci.maths.ArrayMath.toString(d));
		   }

		   public void print(double[][] d){
		   	//System.out.println("jython: "+JSci.maths.ArrayMath.toString(d));
		    print(""+JSci.maths.ArrayMath.toString(d));
		   }

		   public void print(int[] d){
		   	 print(""+JSci.maths.ArrayMath.toString(d));
		   }

		   public void print(int[][] d){
		   	print(""+JSci.maths.ArrayMath.toString(d));

	      }
*/

     public String runnable_cmd;

     public Thread jythonthread;
	 public void run(){
         try{
		 System.out.println("sending the following command to interp "+runnable_cmd);
	     interp.exec(runnable_cmd);
	     char achar=runnable_cmd.charAt(runnable_cmd.length()-1);
	     if ((achar!=';')&&(runnable_cmd.startsWith("_="))){
		   interp.exec("w.print(_)");
		 }
	      textField.selectAll();
        // textField.requestFocus();
	      }
		  catch(PyException pye){
			 print(pye.toString());
	   		 last_pye=pye;
	   		 jumptobadline();

		 }
	  }


    public String _raw_input(String text){
	 	  return text = JOptionPane.showInputDialog(this, text);
    }




   public  void processText(){
		String cmd=textField.getText();
        String[] cmds;
	    commandlist.add(cmd);
	    //hist.append(cmd);
	     listModel.update();
	     hist.setSelectedIndex(commandlist.size()-1);
		console.append("("+totalcommands+")>"+cmd+"\n");
		console.setCaretPosition(console.getDocument().getLength());
        totalcommands++;
		historyindex=totalcommands-1;

        if ((cmd.startsWith("help"))||cmd.startsWith("?")){
			Vector stringtokens=new Vector();
			StringTokenizer st = new StringTokenizer(cmd);
			    while (st.hasMoreTokens()) {
			         stringtokens.add(st.nextToken());
                  }
            if (stringtokens.size()>2){
				print("Help function: please querry one word only (i.e help plot)");
			    System.out.println("Sent "+runnable_cmd);
			    return;

			}
			if (stringtokens.size()==1){
				runnable_cmd="_=helptopics()";
				System.out.println("Sent "+runnable_cmd);
			}
			if (stringtokens.size()==2){
				runnable_cmd="_=help(\""+((String)stringtokens.elementAt(1))+"\")";
			     System.out.println("Sent"+runnable_cmd);

			  }
		}
		else
		if (cmd.startsWith(".r")){
			String subs=cmd.substring(2);
			cmds=subs.split(",");
			if (cmds[0].trim().equals("p")){
			  runnable_cmd="_=roiprocess()";
			}else
			if (cmds[0].trim().equals("d")){
			  runnable_cmd="_=roideletelast()";
			}else
			if (cmds[0].trim().equals("a")){
			  runnable_cmd="_=roideleteall()";
			}else
			if (cmds[0].trim().equals("c")){
			  runnable_cmd="_=roicopy()";
			}else
			if (cmds[0].trim().equals("l")){
			  runnable_cmd="_=roiload()";
			}else
			if (cmds[0].trim().equals("s")){
			 runnable_cmd="_=roiscale()";
			}else
			if (cmds.length==4){
			 runnable_cmd="_=makeroi("+subs+")";
			}

		}else
		if (cmd.startsWith(".f")){
	     String subs=cmd.substring(2);
	     String searchstring=subs.trim();
	     find(searchstring);
         return;
		}
		else
		if (cmd.startsWith(".v")){
			String subs=cmd.substring(2);
			cmds=subs.split(",");
			if (cmds[0].trim().equals("b")){
			 runnable_cmd="_=viewersubtract()";
			}else
			if (cmds[0].trim().equals("r")){
			 runnable_cmd="_=viewerraw()";
			}else
			if (cmds[0].trim().equals("n")){
			 runnable_cmd="_=viewernormalize()";
			}else
			if (cmds[0].trim().equals("s")){
			 runnable_cmd="_=viewerscale()";
			}
			else
			if (cmds.length==1){
			 runnable_cmd="viewerframe("+subs+")";
			}else
			if (cmds.length==3){
			 runnable_cmd="_=viewerzoom("+subs+")";
			}
			if (cmds.length==2){
			 runnable_cmd="_=viewervalue("+subs+")";
			}



		}else

        if((!cmd.startsWith("from")) && (!cmd.startsWith("import")) && (!cmd.startsWith("print")))
          runnable_cmd="_="+cmd;

         else runnable_cmd=cmd;

        if (jythonthread == null || !jythonthread.isAlive()) {
			    jythonthread = new Thread(this);
			    jythonthread.start();
	    }


	 }



    public void initInterp() {
        PySystemState.initialize();
      try{
		  FileOutputStream out = new FileOutputStream("outputfromjython.roi");

        //sys.setOut(out);
        this.interp = new PythonInterpreter();
        this.interp.set("w", this);
        this.interp.set("gv",this.gv);
        this.interp.set("jh",this.jh);
        this.interp.set("vwarray",this.jh.vw);
        this.interp.set("t", this.console);
        this.interp.set("g", this.console.getGraphics());
    	this.interp.exec("import sys");
    	this.interp.exec("sys.stdout=w");
    	this.interp.exec("sys.stderr=w");
    	this.interp.execfile("gvdecoder\\scripts\\gview_python_internal.py");

	}catch(IOException e){;}
   }

  public void printWindows(){
	 for (int i=0;i<20;i++){
	    if (jh.vw[i]!=null){
			print(i+" : "+jh.vw[i]);

		}
     }
  }


  public void setShorcuts(Matrix ma){

  }

  public void setShortcuts(int index){
 		try{
 		this.vw=this.jh.vw[index];
 		this.im=this.vw.im;
 		this.jp=this.vw.jp;
 		this.rois=this.jp.rois;

 		//setup all rois so that the pixels are counted
 		if ((rois!=null)&& (rois.size()>0)){
 		for (int i=0;i<rois.size();i++){
 		  ROI roi=(ROI)rois.get(i);
 		  if (roi.arrs==null) roi.setPixels();
 	     }
 	    //arbitrarily set present roi shortcut to last one in the list
 	    this.presentroi=(ROI)this.rois.get(rois.size()-1);
 		this.pixels=this.presentroi.arrs;
 	     }
 	     else{
			 this.presentroi=null;
			 this.pixels=null;

		 }
	    }
 	     catch(Exception e){e.printStackTrace();}

 	    this.interp.set("vw",this.vw);
 	    this.interp.set("im",this.im);
 	    this.interp.set("rois",this.rois);
 	    this.interp.set("presentroi",this.presentroi);
 	    this.interp.set("pixels",this.pixels);

	 }

    public void show() {
        super.show();
        this.initInterp();
        System.out.println("prior to loading the jython command file");
      //  this.interp.execfile(this.startFile);
        System.out.println("after loading the jython command file");

    }

    public static void main(String[] argv) {
    //    PyEmbed embed = new PyEmbed("gview_python.py");
    //    embed.show();
    }

    class GeneratedListModel extends AbstractListModel  {
		JythonViewer demo;

/*
       public Object getSelectedItem(){
           return getElementAt(0);
	   }

	   public void setSelectedItem(Object obj){

	   }

*/
		public GeneratedListModel (JythonViewer demo) {
		    this.demo = demo;
		}


       public void update() {
	   // permuter = new Permuter(getSize());
	    fireContentsChanged(this, 0, getSize());
	   }


		public int getSize() {
		    return demo.commandlist.size();
		}

		public Object getElementAt(int index) {
		 if (demo.commandlist.size()>0)
		   return (String)demo.commandlist.elementAt(index);
		  else return null;

		   }
	    }




}

class jvMenuListener implements ActionListener{
 JythonViewer vi;

 public jvMenuListener(JythonViewer vi){
  this.vi=vi;
  }

 //gets popup events
 public void actionPerformed(ActionEvent e) {
    JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."+ "    Event source: " + source.getText();
        System.out.println(s);
	if (source.getText().equals("run text")){vi.runtext();}
    else if (source.getText().startsWith("find")){vi.findSelectedText();}
    else if (source.getText().startsWith("next")){vi.findNextSelectedText();}
    else if (source.getText().equals("set mark")){vi.setMark();}
    else if (source.getText().equals("goto mark")){vi.gotoMark();}
    else if (source.getText().equals("cancel job")){vi.cancelJob();}
    else if (source.getText().equals("goto line")){vi.gotoLine();}
    else if (source.getText().equals("clear marks")){vi.clearMarks();}



}


}




class jvListener extends MouseInputAdapter {
	JythonViewer vi;
	int x;
	int y;
	boolean dragged=false;
	boolean shifton=false;

	public jvListener(JythonViewer vi){
		   this.vi=vi;
		}

    public void mouseMoved(MouseEvent e){

	}
	public void mousePressed(MouseEvent e) {
	     maybeShowPopup(e);
		}

	  public void mouseDragged(MouseEvent e) {

	   }


  public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
     }


 private void maybeShowPopup(MouseEvent e) {

			  System.out.println("checking popup");
			  if((e.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK){
				 if ( ((JTextArea)e.getSource()).equals(vi.editor)){
				 vi.makeEditorPopup(e.getY()).show(e.getComponent(),e.getX(),e.getY());

			 }else
				 vi.popup.show(e.getComponent(),e.getX(), e.getY());
			  }
			}

    }



