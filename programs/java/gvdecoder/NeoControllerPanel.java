/*
 * ExportPanel.java
 *
 * Created on 09 January 2007, 15:11
 */

package gvdecoder;

import java.io.*;
import java.util.*;
import java.text.NumberFormat;
/**
 *
 * @author  Gil
 */
public class NeoControllerPanel extends javax.swing.JPanel {
    Viewer2 vw;
    Vector createddirectories=new Vector();
    //QTCodecs qtc;

    /** Creates new form ExportPanel */
    public ExportPanel(Viewer2 vw) {
        this.vw=vw;
        initComponents();
    }





    private void initComponents() {

		NumberFormat numberFormat=NumberFormat.getInstance();
		//numberFormat.setParseIntegerOnly(true);

        //qtc=new QTCodecs();
        //String[] codecmodel=QuickTimeEncoder.getQuickTimeEncoder().qtcodecs.names;
        jPanel1 = new javax.swing.JPanel();
        movieName = new javax.swing.JTextField();
        autoName = new javax.swing.JButton();
        chooseName = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        startRange = new javax.swing.JFormattedTextField(numberFormat);
        endRange = new javax.swing.JFormattedTextField(numberFormat);
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        skipFrames = new javax.swing.JFormattedTextField(numberFormat);
        sumFrames = new javax.swing.JFormattedTextField(numberFormat);
        jPanel3 = new javax.swing.JPanel();
        sizeToRealPixels = new javax.swing.JCheckBox();
        showDecorations = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        imageTypes = new javax.swing.JComboBox();
        addViewer = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        AVIFPS = new javax.swing.JFormattedTextField(numberFormat);
        AVIUnfilteredCheckBox = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        movQuality = new javax.swing.JComboBox();
        movCodec = new javax.swing.JComboBox();
        MovFPS = new javax.swing.JFormattedTextField(numberFormat);
        jLabel6 = new javax.swing.JLabel();
        startEncoding = new javax.swing.JButton();
        cancelEncoding = new javax.swing.JButton();
        saveAsDefaults = new javax.swing.JButton();
        progressbar = new javax.swing.JProgressBar();

        setLayout(null);

        jPanel1.setLayout(null);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("File"));
        movieName.setText("enter name");
        jPanel1.add(movieName);
        movieName.setBounds(10, 20, 100, 20);

        autoName.setFont(new java.awt.Font("Arial", 0, 10));
        autoName.setLabel("auto");
        autoName.setMargin(new java.awt.Insets(2, 5, 2, 5));
        autoName.setMinimumSize(new java.awt.Dimension(35, 19));
        autoName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoNameActionPerformed(evt);
            }
        });

        jPanel1.add(autoName);
        autoName.setBounds(120, 20, 37, 20);

        chooseName.setFont(new java.awt.Font("Arial", 0, 10));
        chooseName.setText("..");
        chooseName.setMargin(new java.awt.Insets(2, 5, 2, 5));
        chooseName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseNameActionPerformed(evt);
            }
        });

        jPanel1.add(chooseName);
        chooseName.setBounds(160, 20, 30, 20);

        add(jPanel1);
        jPanel1.setBounds(0, 0, 200, 50);

        jPanel2.setLayout(null);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Range"));
        jLabel1.setFont(new java.awt.Font("Arial", 0, 10));
        jLabel1.setText("start");
        jPanel2.add(jLabel1);
        jLabel1.setBounds(10, 20, 30, 13);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 10));
        jLabel2.setText("stop");
        jPanel2.add(jLabel2);
        jLabel2.setBounds(10, 50, 30, 13);

        startRange.setColumns(5);
        startRange.setText("0");
        startRange.setToolTipText("Navigator auto sets range to zoom.");
        startRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startRangeActionPerformed(evt);
            }
        });

        jPanel2.add(startRange);
        startRange.setBounds(50, 20, 51, 19);

        endRange.setColumns(5);
        endRange.setText(""+vw.MaxFrames);
       // endRange.setText("-1");
        endRange.setToolTipText("Note: navigator sets range to zoom");
        endRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endRangeActionPerformed(evt);
            }
        });

        jPanel2.add(endRange);
        endRange.setBounds(50, 50, 51, 19);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 10));
        jLabel3.setText("skip");
        jPanel2.add(jLabel3);
        jLabel3.setBounds(120, 20, 30, 13);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 10));
        sumFrames.setText("1");

        jLabel4.setText("sum");
        jPanel2.add(jLabel4);
        jLabel4.setBounds(120, 50, 30, 13);

	    sumFrames.setColumns(3);
		        sumFrames.setText("1");
		        sumFrames.setToolTipText("sum over N frames (not always supported)");
		        sumFrames.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                sumFramesActionPerformed(evt);
		            }
		        });

		        jPanel2.add(sumFrames);

        sumFrames.setBounds(160, 50, 30, 19);
       if ((vw.im.ReturnSupportedFilters()).indexOf("FRAMESTEP")==-1){
         sumFrames.setEnabled(false);
         jLabel4.setEnabled(false);
        }
        skipFrames.setColumns(3);
        skipFrames.setText("1");
        skipFrames.setToolTipText("show every Nth frame");
        skipFrames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipFramesActionPerformed(evt);
            }
        });

        jPanel2.add(skipFrames);
        skipFrames.setBounds(160, 20, 30, 19);



        add(jPanel2);
        jPanel2.setBounds(0, 50, 200, 80);

        jPanel3.setLayout(null);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Appearance"));
        sizeToRealPixels.setFont(new java.awt.Font("Arial", 0, 10));
        sizeToRealPixels.setText("Min. Pixels");
        sizeToRealPixels.setToolTipText("Uses viewer dimensions unless checked.");
        sizeToRealPixels.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sizeToRealPixels.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel3.add(sizeToRealPixels);
        sizeToRealPixels.setBounds(10, 20, 80, 13);

        showDecorations.setFont(new java.awt.Font("Arial", 0, 10));
        showDecorations.setText("Show extras");
        showDecorations.setToolTipText("Show decorations (rois, rulers, etc)");
        showDecorations.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showDecorations.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel3.add(showDecorations);
        showDecorations.setBounds(110, 20, 80, 15);

        add(jPanel3);
        jPanel3.setBounds(0, 130, 200, 40);

        jPanel4.setLayout(null);

        jPanel5.setLayout(null);

        imageTypes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "jpg", "tif", "bmp", "gif", "png" }));
        jPanel5.add(imageTypes);
        imageTypes.setBounds(10, 10, 60, 22);

        addViewer.setFont(new java.awt.Font("Arial", 0, 10));
        addViewer.setText("add viewer");
        addViewer.setToolTipText("include javascript flipbook viewer");
        addViewer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addViewer.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel5.add(addViewer);
        addViewer.setBounds(80, 10, 80, 20);

        jTabbedPane1.addTab("Images", jPanel5);
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener(){
			public void stateChanged(javax.swing.event.ChangeEvent evt){
				javax.swing.JTabbedPane pane=(javax.swing.JTabbedPane)evt.getSource();
				checkIfQuickTimeTab(pane.getSelectedIndex());
			}
		});

        jPanel6.setLayout(null);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 10));
        jLabel5.setText("FPS");
        jPanel6.add(jLabel5);
        jLabel5.setBounds(0, 10, 20, 20);

        AVIFPS.setText("20");
        AVIFPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AVIFPSActionPerformed(evt);
            }
        });

        jPanel6.add(AVIFPS);
        AVIFPS.setBounds(20, 10, 30, 19);

        AVIUnfilteredCheckBox.setFont(new java.awt.Font("Arial", 0, 10));
        AVIUnfilteredCheckBox.setText("unfiltered");
        AVIUnfilteredCheckBox.setToolTipText("save as small undecorated greyscale avi");
        AVIUnfilteredCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        AVIUnfilteredCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel6.add(AVIUnfilteredCheckBox);
        AVIUnfilteredCheckBox.setBounds(90, 10, 60, 20);

        jTabbedPane1.addTab("AVI", jPanel6);

        jPanel7.setLayout(null);

        movQuality.setFont(new java.awt.Font("Arial", 0, 10));
        movQuality.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "low", "med", "hi", "max" }));
        movQuality.setToolTipText("select encoding quality");
        movQuality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movQualityActionPerformed(evt);
            }
        });

        jPanel7.add(movQuality);
        movQuality.setBounds(50, 10, 50, 22);

        movCodec.setFont(new java.awt.Font("Arial", 0, 9));
        movCodec.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"qt","not","loaded"})); /*new String[] { "cinepak", "animation", "H.263", "Sorensen", "Sorensen3", "mpeg-4" })); */
        movCodec.setToolTipText("select encoder");
        movCodec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movCodecActionPerformed(evt);
            }
        });

        jPanel7.add(movCodec);
        movCodec.setBounds(102, 10, 95, 22);

        MovFPS.setText("20");
        MovFPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MovFPSActionPerformed(evt);
            }
        });

        jPanel7.add(MovFPS);
        MovFPS.setBounds(20, 10, 30, 20);

        jLabel6.setFont(new java.awt.Font("Arial", 0, 10));
        jLabel6.setText("FPS");
        jPanel7.add(jLabel6);
        jLabel6.setBounds(0, 10, 20, 20);
        jPanel7.setEnabled(false);
        jTabbedPane1.addTab("mov", jPanel7);

        jPanel4.add(jTabbedPane1);
        jTabbedPane1.setBounds(0, 0, 200, 70);

        startEncoding.setBackground(new java.awt.Color(102, 255, 102));
        startEncoding.setFont(new java.awt.Font("Arial", 0, 10));
        startEncoding.setText("Go!");
        startEncoding.setToolTipText("start encoding");
        startEncoding.setMargin(new java.awt.Insets(2, 3, 2, 3));
        startEncoding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startEncodingActionPerformed(evt);
            }
        });

        jPanel4.add(startEncoding);
        startEncoding.setBounds(0, 80, 50, 21);

        cancelEncoding.setBackground(new java.awt.Color(255, 153, 153));
        cancelEncoding.setFont(new java.awt.Font("Arial", 0, 10));
        cancelEncoding.setText("Cancel");
        cancelEncoding.setToolTipText("stop encoding");
        cancelEncoding.setMargin(new java.awt.Insets(2, 5, 2, 5));
        cancelEncoding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelEncodingActionPerformed(evt);
            }
        });

        jPanel4.add(cancelEncoding);
        cancelEncoding.setBounds(50, 80, 50, 21);

        saveAsDefaults.setFont(new java.awt.Font("Arial", 0, 10));
        saveAsDefaults.setText("save default");
        saveAsDefaults.setToolTipText("save settings as defaults");
        saveAsDefaults.setMargin(new java.awt.Insets(2, 5, 2, 5));
        saveAsDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsDefaultsActionPerformed(evt);
            }
        });

        jPanel4.add(saveAsDefaults);
        saveAsDefaults.setBounds(120, 80, 80, 20);

        jPanel4.add(progressbar);
        progressbar.setBounds(0, 110, 200, 20);

        add(jPanel4);
        jPanel4.setBounds(0, 170, 200, 140);
        setPreferredSize(new java.awt.Dimension(215,340));
    }


   public String getPostFix(){
	 String res="";
	 int i=jTabbedPane1.getSelectedIndex();
     if (i==0){
      res=(String)imageTypes.getSelectedItem();
     }
      else
      if (i==1) res="avi";
      else
      if (i==2) res="mov";

      return res;
   }

    private void AVIFPSActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }

    private void MovFPSActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }

    private void sumFramesActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }

    private void skipFramesActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }

    private void endRangeActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }

    private void startRangeActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }

    private void saveAsDefaultsActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }

    public boolean cancelled=false;
    private void cancelEncodingActionPerformed(java.awt.event.ActionEvent evt) {
     cancelled=true;
    }


    public boolean QTPanelOpened=false;
    public void checkIfQuickTimeTab(int i){
		if (i==2) {
			if (QTPanelOpened) return;

			QTPanelOpened=true;
			try{
			//String[] codecmodel=QuickTimeEncoder.getQuickTimeEncoder().qtcodecs.names;
			//movCodec.setModel(new javax.swing.DefaultComboBoxModel(codecmodel)); /*new String[] { "cinepak", "animation", "H.263", "Sorensen", "Sorensen3", "mpeg-4" })); */
		    }catch(Exception e){
				QTPanelOpened=false;
				javax.swing.JOptionPane.showInternalMessageDialog(vw,"QuickTime (QTJava.zip) must be installed before saving .mov files.\n\n"+
						                                                     "Please go to:\n"+
						                                                     "developer.apple.com/quicktime/qtjava/installation.html\n"+
						                                                     "and follow the directions (but if the 'custom' option does\n"+
						                                                     "not appear, its still OK)\n"+
						                                                     "Please note, if you use more than one jre, you will have\n"+
						                                                     "to make sure QTJava.zip is in your JAVAHOME/lib/ext directory.\n\n");
			    e.printStackTrace();
			}
		}
      QTPanelOpened=false;
	}

    private void startEncodingActionPerformed(java.awt.event.ActionEvent evt) {
    // TODO add your handling code here:
    //1 determine what type of data to encode.
    cancelled=false;
    int i=jTabbedPane1.getSelectedIndex();
    //validate all input
    try{
     startRange.commitEdit();
     endRange.commitEdit();
     sumFrames.commitEdit();
     skipFrames.commitEdit();
     AVIFPS.commitEdit();
     MovFPS.commitEdit();
     }catch (Exception e){
        java.awt.Toolkit.getDefaultToolkit().beep();
        e.printStackTrace();
		errormsg("Please check the numbers entered...\nthere is a format problem");
	    return;
     }

     //check to make sure movieName!="enter name"
     java.io.File tmp=new java.io.File(movieName.getText());
     if ((movieName.getText().equals("enter name") )||
         ( (i==0)&&(!tmp.isDirectory())) ||
         ( (i>0)&& (tmp.isDirectory())) ) {
		errormsg("Please enter a valid file\nor directory name.\n(or click on the [auto] button)");
	    return;
	 }

	 int start=((Number)startRange.getValue()).intValue();
	 int end=((Number)endRange.getValue()).intValue();
	 int avifps=((Number)AVIFPS.getValue()).intValue();
     int movfps=((Number)MovFPS.getValue()).intValue();
     int skipframes=((Number)skipFrames.getValue()).intValue();
     int sumframes=((Number)sumFrames.getValue()).intValue();
     boolean corrected=false;
     if (start<0) {start=0; corrected=true;}
     if (end<start) {end=start+1;corrected=true;}
     if (end>vw.MaxFrames) {end=vw.MaxFrames;corrected=true;}
     if (skipframes<1) {skipframes=1;corrected=true;}
     if (sumframes<1) {sumframes=1;corrected=true;}
     if (corrected){
       startRange.setValue(new Integer(start));
       endRange.setValue(new Integer(end));
       skipFrames.setValue(new Integer(skipframes));
       sumFrames.setValue(new Integer(sumframes));
       java.awt.Toolkit.getDefaultToolkit().beep();
	   errormsg("One of the numbers entered has been autocorrected\nPlease recheck the values and hit\n[go] if they are correct.");
	   return;
	  }
	   if (i==0){

		System.out.println("images saved");
		System.out.println("prefix = "+movieName.getText());
		System.out.println("start="+startRange.getText());
		System.out.println("end="+endRange.getText());
		System.out.println("sumframes="+sumFrames.getText());
		System.out.println("skipframes="+skipFrames.getText());
		System.out.println("size to real="+sizeToRealPixels.isSelected());
        AnalysisHelper ah=AnalysisHelper.getAnalysisHelper();
        ah.saveImageSeriesThread(vw, start, end, getPostFix(), movieName.getText(), showDecorations.isSelected(), this);

       }

      if (i==1) {
        AnalysisHelper ah=AnalysisHelper.getAnalysisHelper();
        //(Viewer2 vw, String filename, int start, int end, int fps, boolean filtered, StatusMonitor monitor)
        ah.saveAVIThread(vw, movieName.getText(),start,end, avifps, AVIUnfilteredCheckBox.isSelected(), showDecorations.isSelected(), this);

    	}

	if (i==2) {
		if (QTPanelOpened){
		AnalysisHelper ah=AnalysisHelper.getAnalysisHelper();
       // ah.saveMovThread(vw,movieName.getText(),start,end,movfps,movCodec.getSelectedIndex(),movQuality.getSelectedIndex(),showDecorations.isSelected(),this);
	    }
      }

    }

    private void chooseNameActionPerformed(java.awt.event.ActionEvent evt) {
         int i=jTabbedPane1.getSelectedIndex();
         String old=movieName.getText();
         String res=null;
         if (i==0) res=vw.ifd.fc.saveDirectory(vw.ifd.fc.TempImagesDir);
         else res=vw.ifd.fc.saveFile(vw.ifd.fc.TempImagesDir);
         if (res!=null) movieName.setText(res);
    }


    public String filepath=null;
    public String filename=null;
    public boolean save_series=false;
    public boolean name_ok=false;
    private void autoNameActionPerformed(java.awt.event.ActionEvent evt) {
     // TODO add your handling code here:
     //AnalysisHelper.getAnalysisHelper().unique_name(getImage());
     //what is the postfix?
     name_ok=false;

     String post=getPostFix();
     String mid=vw.filename;

     String[] newpath=AnalysisHelper.getAnalysisHelper().create_unique_image_name(mid,post,true);
     if ((newpath==null)||(newpath[0]==null)) {
		  movieName.setText("FAILED - try choosing own");
		  name_ok=false;
		  return;
		  }
     if (newpath[1]==null){
		 filepath=newpath[0];
	     movieName.setText(filepath);
	     save_series=true;
	     name_ok=true;
	     createddirectories.add(filepath);

	 }else{
		filepath=newpath[0];
		filename=newpath[1];
		movieName.setText(filepath+File.separator+filename);
	    save_series=false;
	    name_ok=true;
	 }
    }

    private void movCodecActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }

    private void movQualityActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    }


    // Variables declaration - do not modify
    private javax.swing.JCheckBox addViewer;
    private javax.swing.JButton autoName;
    private javax.swing.JButton cancelEncoding;
    private javax.swing.JButton chooseName;
    public javax.swing.JFormattedTextField endRange;
    private javax.swing.JComboBox imageTypes;
    private javax.swing.JButton startEncoding;
    private javax.swing.JCheckBox AVIUnfilteredCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JProgressBar progressbar;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JFormattedTextField AVIFPS;
    private javax.swing.JFormattedTextField MovFPS;
    private javax.swing.JComboBox movCodec;
    private javax.swing.JComboBox movQuality;
    private javax.swing.JTextField movieName;
    private javax.swing.JButton saveAsDefaults;
    private javax.swing.JCheckBox showDecorations;
    private javax.swing.JCheckBox sizeToRealPixels;
    private javax.swing.JFormattedTextField skipFrames;
    public javax.swing.JFormattedTextField startRange;
    private javax.swing.JFormattedTextField sumFrames;


    public void errormsg(String error){
		 javax.swing.JOptionPane.showInternalMessageDialog(vw,error);
	}

}
