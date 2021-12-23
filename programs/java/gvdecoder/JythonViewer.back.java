import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import org.python.core.*;
import org.python.core.PySystemState;
import org.python.util.*;
import java.io.*;
import java.awt.font.*;
import java.util.*;

public class JythonViewer extends JPanel implements KeyListener, ListSelectionListener{

    public PythonInterpreter interp;
    JTextArea easel;
    String startFile;
    public JTextField textField;
    public GView gv;
    public JythonHelper2 jh;

	Font command;
	Font result;
    Vector commandlist;
    int totalcommands=0;
    int historyindex=0;
    JList hist;
    GeneratedListModel listModel;
    JScrollPane histPane;
    JPopupMenu popup;
    public float freemem;
    public float usedmem;
    private Runtime r = Runtime.getRuntime();

    public JythonViewer(String startup, GView gv, JythonHelper2 jh) {

		this.startFile = startup;
		this.gv=gv;
		this.jh=jh;

        this.setLayout(new BorderLayout());
        this.easel = new JTextArea();
        this.easel.setDragEnabled(true);
        JScrollPane scrollingarea=new JScrollPane();
        JViewport prt=scrollingarea.getViewport();
        //this.easel.setPreferredSize(new Dimension(200, 600));
        prt.add(easel);
         this.add(scrollingarea, BorderLayout.CENTER);
        this.textField = new JTextField();
        this.textField.setPreferredSize(new Dimension(200, 20));
        this.add(this.textField, BorderLayout.SOUTH);
        this.textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                processText();
                //JythonViewer.this.interp.exec(JythonViewer.this.textField.getText());
                //JythonViewer.this.textField.setText("");
            }});

        this.textField.addKeyListener(this);

        command=new Font("arial",Font.BOLD,12);
        result=new Font("arial",Font.PLAIN,12);
    	commandlist=new Vector();


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
    	//JComboBox jb=new JComboBox(hist);
    	this.add(scroller,BorderLayout.NORTH);

        jvMenuListener myViewMenuListener=new jvMenuListener(this);
		jvListener jvlistener = new jvListener(this);
	    easel.addMouseListener(jvlistener);
	    easel.addMouseMotionListener(jvlistener);
	    popup=new JPopupMenu();
				JMenuItem menuItem = new JMenuItem("run text");
				menuItem.addActionListener(myViewMenuListener);
				popup.add(menuItem);
				menuItem = new JMenuItem("save text");
				menuItem.addActionListener(myViewMenuListener);
		popup.add(menuItem);

    	//this.pack();
        this.show();
    }


     public String mem(){
		 float freeMemory = (float) r.freeMemory();
         float totalMemory = (float) r.totalMemory();
	 	 String str=String.valueOf((int) totalMemory/1024) + "K allocated";
         str += " "+String.valueOf(((int) (totalMemory - freeMemory))/1024)+"K used";
        return str;
	}

     public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {

             JList theList = (JList)e.getSource();
             int index = theList.getSelectedIndex();
             if (index<commandlist.size())
               textField.setText((String)commandlist.elementAt(index));
             }
		 }

     public void keyPressed(KeyEvent e)
	  {
		 int c = e.getKeyCode();

	     if (c==KeyEvent.VK_UP) {history(historyindex-1); e.consume();   }
	     if (c==KeyEvent.VK_DOWN) {history(historyindex+1); e.consume(); }

	  }

	  public void keyTyped(KeyEvent e) {


	  }

	  public void keyReleased(KeyEvent e)
      {}

    public void clear(){
		easel.selectAll();
		easel.cut();
	}

   public void runtext(){
	   String s=easel.getSelectedText();
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
			easel.append(i+")\t"+(String)commandlist.elementAt(i)+"\n");
	     }
	}

     public void processText(){
		String cmd=textField.getText();
	    commandlist.add(cmd);
	    //hist.append(cmd);
	     listModel.update();
	     hist.setSelectedIndex(commandlist.size()-1);
		easel.append("("+totalcommands+")>"+cmd+"\n");
		easel.setCaretPosition(easel.getDocument().getLength());
        totalcommands++;
		historyindex=totalcommands-1;
		interp.exec(cmd);
	//	System.out.println("totalcommands="+totalcommands+" historyindex="+historyindex+" commandlistsize="+commandlist.size()+" last val="+(String)commandlist.elementAt(historyindex-1));
        //PyObject val=interp.eval(textField.getText());
        //textField.setText("");
		//easel.append(val.toString());
	 }


	public void write(String str){
		 easel.append("  "+str+"\n");
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
        this.interp.set("e", this.easel);
        this.interp.set("g", this.easel.getGraphics());
    	this.interp.exec("import sys");
    	this.interp.exec("sys.stdout=w");
    	this.interp.exec("sys.stderr=w");

	}catch(IOException e){;}
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

    class GeneratedListModel extends AbstractListModel {
		JythonViewer demo;




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
		    return (String)demo.commandlist.elementAt(index);

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
	else if (source.getText().equals("toggle ruler|roi")){;}
	else if (source.getText().equals("quicktrace")){;}


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
				  vi.popup.show(e.getComponent(),e.getX(), e.getY());
			  }
			}

    }



