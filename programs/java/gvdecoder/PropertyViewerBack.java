package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.filechooser.*;

public class PropertyViewer extends JInternalFrame {

	GView ifd; //contains this
	public Properties prp;
	final JFileChooser fc = new JFileChooser(".");
    String ConfigFileName="."+File.separator+"gvdecoder"+File.separator+"properties.cfg";

		PropertyViewer(GView ifd) {
			super("Program properties",
						 true, //resizable
						 true, //closable
						 true, //maximizable
				  true);//iconifiable


			this.ifd=ifd;
			addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosed(InternalFrameEvent e) {
				  saveCFG();
			  }

			});

			//get the properties file...
			prp=new Properties();
			try{
			 readProp(ConfigFileName);
			 }catch(Exception e){System.out.println("can't open configuration file...");}


			JScrollPane sp=new JScrollPane(createCFGPanel());

			getContentPane().add(sp, BorderLayout.CENTER);


			setJMenuBar(createMenuBar());
			setSize(new Dimension(200,200));
			setLocation(5,5);
		}



		public PropertyTextField createPropertyEditField(String name){
			PropertyTextField ptf=new PropertyTextField(name,prp.getProperty(name),10);

			ptf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			changeProperty(e);
			}
			});
			return ptf;
		}

		public void changeProperty(ActionEvent e){
			PropertyTextField ptf=(PropertyTextField)e.getSource();
			System.out.println(ptf.key+" "+ptf.getText());
			prp.setProperty(ptf.key,ptf.getText());
		}

		public void editFile(ActionEvent e){
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			BrowseButton bb=(BrowseButton)e.getSource();
			prp.setProperty(bb.ptf.key,file.getAbsolutePath());
			bb.ptf.setText(file.getAbsolutePath());

			}
		}

		public JPanel createDirEditField(String name){
			BrowseButton bb=new BrowseButton(name,prp.getProperty(name),10);
			//PropertyTextField ptf=createPropertyEditField(name);
			JPanel tmp=new JPanel();
			tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));
			tmp.add(bb.ptf);
			//JButton browse=new JButton(".."));
			bb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			editFile(e);
			}
			});
			tmp.add(bb);
			tmp.add(Box.createHorizontalGlue());
			return tmp;
		}

		protected JPanel createCFGPanel(){
			JPanel jp=new JPanel();
			jp.setLayout(new GridLayout(6,2));


			jp.add(new JLabel(".spe dir"));
			jp.add(createDirEditField("spedir"));

			jp.add(new JLabel(".nav dir"));
			jp.add(createDirEditField("navdir"));

			jp.add(new JLabel("image save dir"));
			jp.add(createDirEditField("imagedir"));

			jp.add(new JLabel("scratch dir"));
			jp.add(createDirEditField("scratchdir"));


            JButton Framerate=new JButton("frame rate          ");
			Framerate.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e){
			  changeFPS();
			 }
			});


			jp.add(createPropertyEditField("framerate"));

			jp.add(new JButton("spe window width    "));
			jp.add(createPropertyEditField("windowwidth"));

			return jp;

		}


		protected JMenuBar createMenuBar() {
			JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("file");

			menu.setMnemonic(KeyEvent.VK_F);
			JMenuItem menuItem = new JMenuItem("load");
			menuItem.setMnemonic(KeyEvent.VK_L);
			menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			loadCFG();
			}
			});

			menu.add(menuItem);

			menuItem=new JMenuItem("save");
			menuItem.setMnemonic(KeyEvent.VK_S);
			menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCFG();
			}
			});

			menu.add(menuItem);
			menuBar.add(menu);
			return menuBar;
		}

	    public void changeFPS(){
		 Integer.parseInt(prp.getProperty("framerate","5"));

		}

		public void saveCFG(){
			SaveProp(ConfigFileName);
		}

		public void loadCFG(){
		}

		public void SaveProp(String filename){
			try{
			FileOutputStream ostream=new FileOutputStream(new File(filename));
			prp.store(ostream,"configuration file for image viewer. do not hand edit");
			}catch (Exception e){e.printStackTrace();}
		}

		public void readProp(String filename){
			try{
			FileInputStream istream=new FileInputStream(new File(filename));
			prp.load(istream);
			}catch(Exception e){e.printStackTrace();}
		}


	   public int getIntProperty(String key, String def){
		  return Integer.parseInt(prp.getProperty(key,def));
		}


		public String getStringProperty(String key, String def){
			return (String)prp.getProperty(key,def);
		}


}


class BrowseButton extends JButton{
PropertyTextField ptf;

public BrowseButton(String key, String val, int num){
 super("..");
 ptf=new PropertyTextField(key,val,num);
 }
}



class PropertyTextField extends JTextField{
String key;

public PropertyTextField(String key, String val, int num){
 super(val,num);
 this.key=key;
 }
}




