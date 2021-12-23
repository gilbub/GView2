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
import javax.swing.Box;

/**
s

**/

public class JythonViewer extends JPanel implements Runnable, ItemListener, KeyListener, ListSelectionListener{
    public PythonPreprocessor preprocessor=new PythonPreprocessor();
    public PythonInterpreter interp;
    public JTextArea console;
    public ColoredTextArea editor;
    public JTextArea linenumbers;

    public JTextArea parametereditor;
    public JTextArea parameterlinenumbers;
    public JTextField textField;

    String startFile;

    public JPanel textPanel;
    public GView gv;
    public JythonHelper2 jh;
    JythonWordFinder jythonWordFinder;

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
    public JPanel parametereditorPanel;
    public JPanel inputPanel;
    private Vector nullvector=new Vector();
    public final java.util.Timer timer=new java.util.Timer();
    public JPopupMenu infopop;
    public JPopupMenu notifypop;
    public JLabel notifylabel;
    public GraphButton graphbutton;
    public JLabel statuslabel;
    public JLabel paramlabel;
    public JButton ViewerMenuButton;
    public boolean USEMINIGRAPH=false;
    public JPanel javabuttons=new JPanel();
    public JPanel userbuttons=new JPanel();
    private Highlighter.HighlightPainter[] hilitecolors;

    public WatchDir dirwatcher;

    public JPanel cardPanel;
    public FindField findField;

    public REPLServer replserver; //this is for communication with sublime text.

    //public gvdecoder.fx.Logger logger;
    public boolean doLog=false;



    public JythonViewer(String startup, GView gv, JythonHelper2 jh) {
        System.out.println("JythonViewer constructor");
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
        this.editor = new ColoredTextArea(this);
        editor.add("def ",0,255,0);
        editor.add("class ",0,255,255);
        editor.add("@java ",255,255,200);

        this.jythonWordFinder=new JythonWordFinder(this,editor);
		this.editor.setDragEnabled(true);
		this.editor.setTabSize(4);
		String lnumbers=" ";
        this.linenumbers=new JTextArea(lnumbers,20,5);
        this.linenumbers.setForeground(new Color(220,200,180));
        this.linenumbers.setBackground(new Color(120,120,120));
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
        JButton cleartext=new JButton("cancel");
        cleartext.addActionListener(new ActionListener(){
					 public void actionPerformed(ActionEvent evt){
					   //editor_clear();
					   editor_cancel();
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

		this.parametereditor = new JTextArea();
		this.parametereditor.setDragEnabled(true);
		this.parametereditor.setTabSize(4);
		String javalnumbers=" ";
		this.parameterlinenumbers=new JTextArea(javalnumbers,20,5);
		this.parameterlinenumbers.setForeground(new Color(180,180,200));
		this.parameterlinenumbers.setBackground(new Color(220,220,240));
		JPanel javalineplusedit=new JPanel();
		javalineplusedit.setLayout(new BorderLayout());
		javalineplusedit.add(parameterlinenumbers,BorderLayout.WEST);
		javalineplusedit.add(parametereditor,BorderLayout.CENTER);
		//JScrollPane
		javascrollingarea2=new JScrollPane();
		JViewport javaprt2=javascrollingarea2.getViewport();

		javaprt2.add(javalineplusedit);
		parametereditorPanel=new JPanel();
		parametereditorPanel.setLayout(new BorderLayout());
		parametereditorPanel.add(javascrollingarea2,BorderLayout.CENTER);

		javabuttons.setLayout(new BoxLayout(javabuttons, BoxLayout.X_AXIS));
		userbuttons.setLayout(new BoxLayout(userbuttons,BoxLayout.Y_AXIS));
		userbuttons.add(Box.createRigidArea(new Dimension(20,20)));
		userbuttons.add(new JLabel("user buttons"));
		javabuttons.add(Box.createRigidArea(new Dimension(10,1)));
		//EmptyBorder eb = new EmptyBorder(5,5,5,5);
		SoftBevelBorder javasbb = new SoftBevelBorder(SoftBevelBorder.RAISED);
		buttons.setBorder(javasbb);

		JButton parambutton=new JButton("parse");
		parambutton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent evt){
		 parametereditor_parse();
		}});


        paramlabel=new JLabel(" ");

		javabuttons.add(parambutton);
		javabuttons.add(new JLabel("  "));
		javabuttons.add(paramlabel);
		//javabuttons.add(javarunselected);


		parametereditorPanel.add(javabuttons,BorderLayout.SOUTH);
		parametereditorPanel.add(userbuttons,BorderLayout.EAST);
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
	   this.textField.setPreferredSize(new Dimension(200, 40));
	   //textPanel=new JPanel();
	   //textPanel.add(textField);

        //botpanel.add(textField,BorderLayout.SOUTH);
       cardPanel=new JPanel(new CardLayout());
       //cardPanel.setLayout(new BoxLayout(cardPanel,BoxLayout.LINE_AXIS));

       inputPanel=new JPanel();
       inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
       inputPanel.add(textField);
       inputPanel.add(Box.createHorizontalGlue());
       ViewerMenuButton=new JButton(new String(Character.toChars(0x25b2)));

       inputPanel.add(ViewerMenuButton);
       ViewerMenuButton.addActionListener(new ActionListener() {
	     public void actionPerformed(ActionEvent e) {
	       showInputPopup(e);
	     }
         } );

       cardPanel.add(inputPanel,"input");
       findField=new FindField(this);
       cardPanel.add(findField.panel,"finder");

       this.add(cardPanel,BorderLayout.SOUTH);
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
         tabbedPane.add("vars",parametereditorPanel);

        //jvMenuListener
        myViewMenuListener=new jvMenuListener(this);
		jvListener jvlistener = new jvListener(this);
	    console.addMouseListener(jvlistener);
	    console.addMouseMotionListener(jvlistener);

	    editor.addMouseListener(jvlistener);
	    editor.addMouseMotionListener(jvlistener);

	    console.setFont(result);
	    editor.setFont(result);
	    parametereditor.setFont(result);
	    linenumbers.setFont(result);
	    parameterlinenumbers.setFont(result);



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
        startWatchDir();
        replserver=new REPLServer(gv);

    }

  /*
    else if (source.getText().startsWith("find")){vi.findSelectedText();}
      else if (source.getText().startsWith("next")){vi.findNextSelectedText();}
      else if (source.getText().equals("set mark")){vi.setMark();}
      else if (source.getText().equals("goto mark")){vi.gotoMark();}
    else if (source.getText().equals("clear marks")){vi.clearMarks();}
    */


  /*
  public void setLogger(gvdecoder.fx.Logger logger){
	  this.logger=logger;
	  doLog=true;
  }

  public void unsetLogger(){
	  this.logger=null;
	  doLog=false;
  }
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

  public String fontname="monospaced";
  public int fontsize=12;
  public void setfontsize(int newfontsize){
	  Font f=new Font(fontname,Font.PLAIN,fontsize);
	  fontsize=newfontsize;
	  console.setFont(f);
	  editor.setFont(f);
	  textField.setFont(f);
	  findField.textfield.setFont(f);
	  linenumbers.setFont(f);
	  parametereditor.setFont(f);
	  parameterlinenumbers.setFont(f);

  }

  public void setfontname(String name){
	  fontname=name;
 	  Font f=new Font(fontname,Font.PLAIN,fontsize);
 	  console.setFont(f);
 	  editor.setFont(f);
 	  textField.setFont(f);
 	  findField.textfield.setFont(f);
 	  linenumbers.setFont(f);
 	  parametereditor.setFont(f);
 	  parameterlinenumbers.setFont(f);
  }


public void fontButtonPressed(){
	if (fontsize==12) normalfont();
	else if (fontsize==16) bigfont();
	else if (fontsize==20) hugefont();
	else smallfont();
}

 public void hugefont(){
	 fontsize=24;
	 editor.stagger=-3;
     setfontname("consolas");

}


 public void bigfont(){
   fontsize=20;
   editor.stagger=-3;
   setfontname("consolas");
}

 public void normalfont(){
   fontsize=16;
   editor.stagger=-3;
   setfontname("consolas");


}
 public void smallfont(){
   fontsize=12;
   editor.stagger=-3;
   setfontname("consolas");

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
  public void showInputPopup(ActionEvent e){
	  JButton source=(JButton)e.getSource();
	  makeInputPopup().show(source,-45,-90);
  }
  public JPopupMenu makeInputPopup(){
	  JPopupMenu tmp=new JPopupMenu();
	  JMenuItem menu4;
	  if (fontsize<24) menu4=new JMenuItem("Aa+");
	  else menu4=new JMenuItem("Aa-");
	  menu4.addActionListener(myViewMenuListener);
	  tmp.add(menu4);
	  tmp.addSeparator();
	  JMenuItem menu6;
	  if (USEMINIGRAPH) menu6=new JMenuItem("plot off");
	  else menu6= new JMenuItem("plot on");
	  menu6.addActionListener(myViewMenuListener);
	  tmp.add(menu6);
	  tmp.addSeparator();
	  JMenuItem menu7=new JMenuItem("find (cnt-f)");
	  menu7.addActionListener(myViewMenuListener);
	  tmp.add(menu7);
	  JMenuItem menu8=new JMenuItem("goto (cnt-g)");
	  menu8.addActionListener(myViewMenuListener);
	  tmp.add(menu8);

	  return tmp;

  }

   public String searchtext=null;
   int lastpopupypos=0;
   public JPopupMenu makeEditorPopup(int ypos){
       lastpopupypos=ypos;
	   JPopupMenu tmp=new JPopupMenu();

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
public void parametereditor_parse(){
	 StringBuilder pb=new StringBuilder();
	 pb.append("class _p:\n");
     pb.append("  _base_=0\n");
     String[] tmp=(parametereditor.getText()).split("\n");
     for (int i=0;i<tmp.length;i++){
		 if (tmp[i].indexOf("=")!=-1){
			 pb.append("  ");
			 pb.append(tmp[i].replace("\t"," "));
			 pb.append("\n");
	       }
	}
 interp.exec(pb.toString());
 System.out.println(pb.toString());
}


public String translatePySyntaxError(PyException e){
          String str=e.toString();
          int v=str.indexOf("<string>");
          int c1=str.indexOf(',',v);
          int c2=str.indexOf(',',c1+1);
          int c3=str.indexOf(',',c2+1);
          int a1=str.indexOf("'",c3+1);
          int a2=str.indexOf("'",a1+1);
          if ((c1<0) || (c2<0) || (c3<0) || (a1<0) || (a2<0)) return "unable to parse:"+str;
          String postfix= "("+str.substring(c1+1,c2)+","+str.substring(c2+1,c3)+"): "+str.substring(a1+1,a2);
	      if (currentfilename==null) return postfix;
	      else return currentfilename+" "+postfix;
	  }

public void parametereditor_runSelected(){;}
public void parametereditor_clear(){;}
public void editor_runAll(){
	  // editor_findMethodNames();
       formatEditor();
       try{
	   		PrintWriter out = new PrintWriter("compilationerrors.txt");
	        out.println("no errors");
	   	    out.close();
		}catch(IOException e){e.printStackTrace();}
	   try{
		   String sc=editor.getText();
		   preprocessor.params.clear();
		   preprocessor.processParameters(sc);
		   interp.exec(preprocessor.python_param_class);
		   String paramlines="";
		   String paramtxt="";
		   for (Parameter param:preprocessor.params){
			  if (param.ok){
			    paramlines+=param.linenumber+1+"\n";
			    paramtxt+=param.paramname+" = "+param.paramvalue;
			    if (param.comment!=null) paramtxt+=" \t\t# "+param.comment;
			    paramtxt+="\n";
			   }
		   }
           parametereditor.setText(paramtxt);
           parameterlinenumbers.setText(paramlines);

		   String s=sc.replace("@p","##");
		   preprocessor.processJava(s);
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

			  // String str=code.basename+" = gvdecoder.scripts.anonclasses."+code.classname+"()";
			   String str=code.basename+" = gvdecoder.anonclasses."+code.classname+"()";
			   console.append("   setting python variable to java class: "+str+" \n");
			   interp.exec(str);

		   }

		   interp.exec(preprocessor.pythonstring);
		   console.append("\n\n in command box, ctrl-shift-{ and ctrl-shift-( highlight, ctrl-space clears.\n");
		   console.append("\n python script loaded\n");

	       }catch(PyException pye){
			last_pye=pye;
           /*
			JOptionPane.showMessageDialog(
			   			  		    this,
			   			  		    pye.toString(),
			   			  		    "python script error",
			  		                 JOptionPane.WARNING_MESSAGE);
			*/
			String errmessage=translatePySyntaxError(pye);
		    console.append("\n script window err:\n"+errmessage);
		    try{
							PrintWriter out = new PrintWriter("compilationerrors.txt");
					        out.println(errmessage);
					        out.close();
		    }catch(IOException e){e.printStackTrace();}

		    //console.setCaretPosition(editor.getDocument().getLength());

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
	   editor.setCaretPosition(0);
	   try{
       methods.setListData(nullvector);
       }catch(Exception e){;}
   }

  public void editor_cancel(){
 if (jythonthread != null || jythonthread.isAlive())  jythonthread.stop();

  }


  public void startWatchDir(){
	  String dir = "C:\\CD\\programs\\java\\gvdecoder\\scripts";
	  try{
	  dirwatcher=new WatchDir(this,dir, false);
	  Thread thr=new Thread(dirwatcher);
      thr.start();
     }catch(Exception e){ print("can't do it, see console");
		                 System.out.println("can't start watchdir");
                         e.printStackTrace();
					 }
  }

  public void loadandrun(String filename){

	  if (filename.endsWith(".py")){
	   print("in loadandrun");
	   try{java.lang.Thread.sleep(500);
	   System.out.println("loading "+filename);
	   opentextfile( filename);
	   java.lang.Thread.sleep(500);
	   editor_runAll();
       }catch(Exception e){
		   System.out.println("problem in loadandrun in jythonviewer");
		   e.printStackTrace();}
      }
  }
   String currentfilename=null;
   public void opentextfile(String filename){
	   editor_clear();
	   try{
	   currentfilename=filename;
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
     public void FindMode(){
		   ((CardLayout)this.cardPanel.getLayout()).show(this.cardPanel,"finder");
		   findField.textfield.requestFocus();
           findField.setFindMode();
	 }
	 public void GotoMode(){

		((CardLayout)this.cardPanel.getLayout()).show(this.cardPanel,"finder");
		findField.textfield.requestFocus();
		findField.setGotoMode();
		jythonWordFinder.findFunctions();
	 }

     public void keyPressed(KeyEvent e)
	  {
		 int c = e.getKeyCode();

	     if (c==KeyEvent.VK_UP) {history(historyindex-1); e.consume();   }
	     if (c==KeyEvent.VK_DOWN) {history(historyindex+1); e.consume(); }
	     if (c==KeyEvent.VK_TAB)
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
		 if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (c == KeyEvent.VK_F)){

		 		 		  FindMode();
		 		 		  e.consume();
		 }
		  if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (c == KeyEvent.VK_G)){

		 		 		GotoMode();
		 		        e.consume();
		 }
		 if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (c == KeyEvent.VK_M)){
			  makeInputPopup().show(ViewerMenuButton,-45,-90);
		 }
		 if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (c == KeyEvent.VK_N)){

		 	if (jythonWordFinder.foundLocations.size()>0) {

				editor.setCaretPosition(jythonWordFinder.getNextLocation());
		 		e.consume();
		    }
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
		paramlabel.setText(s);
   	}

	public void print(String str){
		//checkconsolelength();
		String s=format(str);
		console.append(">\t"+s+"\n");
		console.setCaretPosition(console.getDocument().getLength());
		statuslabel.setText(s);
		paramlabel.setText(s);
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

	public String REPLResponse;
	public String REPL(String input){
	 System.out.println("in repl");
	 //String command="_="+input;
	 System.out.println("sending "+command);
	 try{
	  textField.setText(input);
      String c=generateCommand(input);
      run(); // this processes the command in the current thread (blocking)
      if (c.indexOf("_=")==0)
       interp.exec("w.assign(_)");
    else REPLResponse="OK.";

    }catch(Exception e){e.printStackTrace(); REPLResponse="jython syntax error:\n"+e.toString();}

	 System.out.println("returning "+REPLResponse);
	 return REPLResponse;
	}

    public void assign(Object o){
		REPLResponse=o.toString();
	}
    public void assign(int o){
		REPLResponse=""+o;
	}
	public void assign(double o){
			REPLResponse=""+o;
	}
    public void assign(double[] d){
	  if (d==null) {REPLResponse="null"; return;}
	  int l=d.length;
	  if (l>20) l=20;
	  String str="array l="+d.length+" ";
	  for (int i=0;i<l;i++) str= str.concat(" "+d[i]);
	  if (d.length>=l) str=str.concat("...");
	  REPLResponse=str;
	}

    public String _raw_input(String text){
	 	  return text = JOptionPane.showInputDialog(this, text);
    }


   public String generateCommand(String cmd){
    String[] cmds;
    if ((cmd.startsWith("help"))||cmd.startsWith("?")){
            Vector stringtokens=new Vector();
            StringTokenizer st = new StringTokenizer(cmd);
                while (st.hasMoreTokens()) {
                     stringtokens.add(st.nextToken());
                  }
            if (stringtokens.size()>2){
                print("Help function: please querry one word only (i.e help plot)");
                System.out.println("Sent "+runnable_cmd);
                return null;

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
         return null;
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
        if ((cmd.indexOf("*=")>0) || (cmd.indexOf("+=")>0) || (cmd.indexOf("-=")>0)){
            int itemp=cmd.indexOf("=");
            String varname=cmd.substring(0,itemp-1).trim();
            runnable_cmd=cmd+="\n_="+varname+"\nw.print(_)\n";
        }
        else
        if((!cmd.startsWith("from")) && (!cmd.startsWith("import")) && (!cmd.startsWith("print")))
          runnable_cmd="_="+cmd;

        else runnable_cmd=cmd;
     return runnable_cmd;

   }

   public  void processText(){
		String cmd=textField.getText();
       //if ((doLog)&&(logger!=null)) logger.log(true,"code",cmd);
	    commandlist.add(cmd);
	    //hist.append(cmd);
	     listModel.update();
	     hist.setSelectedIndex(commandlist.size()-1);
		console.append("("+totalcommands+")>"+cmd+"\n");
		console.setCaretPosition(console.getDocument().getLength());
        totalcommands++;
		historyindex=totalcommands-1;

        runnable_cmd=generateCommand(cmd);
       if (runnable_cmd!=null){
        if (jythonthread == null || !jythonthread.isAlive()) {
			    jythonthread = new Thread(this);
			    jythonthread.start();
	    }
      }

	 }



    public void initInterp() {


      try{
		System.out.println("before PySystemState.initialize()");
		PySystemState.initialize();
		System.out.println("after PySystemState.initialize()");

		FileOutputStream out = new FileOutputStream("outputfromjython.roi");
        //sys.setOut(out);

        System.out.println("before new  PythonInterpreter()");
        this.interp = new PythonInterpreter();
        System.out.println("after new  PythonInterpreter()");
        this.interp.set("w", this);
        System.out.println("after interp set");

        this.interp.set("gv",this.gv);
         System.out.println("after gv set");
        this.interp.set("jh",this.jh);
         System.out.println("after jh set");
        this.interp.set("vwarray",this.jh.vw);
         System.out.println("after vwarray set");
        this.interp.set("t", this.console);
         System.out.println("after t set");
        //this.interp.set("g", this.console.getGraphics());
    	this.interp.exec("import sys");
    	 System.out.println("after import sys set");
    	//this.interp.exec("sys.stdout=w");
    	 System.out.println("after sys.stdout=w");
    	//this.interp.exec("sys.stderr=w");
    	System.out.println("before execute script");
    	this.interp.execfile("gvdecoder\\scripts\\gview_python_internal.py");
        System.out.println("after execute script");
	}catch(Exception e){e.printStackTrace();}
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
        System.out.println("before initInterp()");
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
		JythonViewer jv;

		public GeneratedListModel (JythonViewer jv) {
		    this.jv = jv;
		}


       public void update() {
	   // permuter = new Permuter(getSize());
	    fireContentsChanged(this, 0, getSize());
	   }


		public int getSize() {
		    return jv.commandlist.size();
		}

		public Object getElementAt(int index) {
		 if (jv.commandlist.size()>0)
		   return (String)jv.commandlist.elementAt(index);
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

    else if (source.getText().equals("set mark")){vi.setMark();}
    else if (source.getText().equals("goto mark")){vi.gotoMark();}
    else if (source.getText().equals("cancel job")){vi.cancelJob();}
    else if (source.getText().equals("goto line")){vi.gotoLine();}
    else if (source.getText().equals("clear marks")){vi.clearMarks();}
    else if (source.getText().equals("Aa+")){vi.fontButtonPressed();}
    else if (source.getText().equals("Aa-")){vi.fontButtonPressed();}
    else if (source.getText().equals("plot off")){ vi.USEMINIGRAPH=false;}
    else if (source.getText().equals("plot on")){ vi.USEMINIGRAPH=true;}
    else if (source.getText().equals("find (cnt-f)")) {vi.FindMode();}
    else if (source.getText().equals("goto (cnt-g)")){vi.GotoMode();}



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



