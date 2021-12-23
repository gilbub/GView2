package gvdecoder;
 /*
 * Swing version.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import gvdecoder.trace.*;
/*
 Paints a graph from a file.
 For now, assume the data is a series of ints, not scaled.
 The code should determine the amount of room it has, and
 generate a second x and y array that contains a drawable line.
 It uses drawPolyLine(arrayx, arrayy, length)
 */


class simpleGraph extends JPanel {




    JPopupMenu popup;
    String messageString="";
	int[] xarr;
	int[] yarr;
	int[] datarr;
	int length;
    Dimension preferredSize = new Dimension(300,100);
    int rectWidth = 50;
    int rectHeight = 50;

	double x_scale=1;  //what the xarr array gets divided by
	double y_scale=1;  //what the yarr array gets divided by
	int x_offset=0;    //the first frame shown on the graph
	int y_offset;   //the y_offset for the graph
	int x_range=0;     //the length (in frames) of the graph
	int user_start =0; //user selectable range (sets a frame number) starting point.
	Dimension lastRange;
	boolean flipped=false;

	Dimension windowSize;  //keeps track of the window size for resize events.
	Dimension scaleToSize; //similar to windowSize, but used to scale the data

	int maxY=-32000;
	int minY=32000; //CHANGE

	Rectangle selectionRect;
	boolean showSelection;
	boolean invertSelection; //in case of a zoomout operation
    Font font = new Font("Arial", Font.PLAIN, 10);

    public void setScaleToSize(Dimension d){
	if (scaleToSize==null){
	  scaleToSize=new Dimension();
	 }

	 scaleToSize.setSize(d.getWidth(), d.getHeight()-10);

	}

    public simpleGraph() {

	 getArrayFromFile gaff=new getArrayFromFile("test.dat");
	 datarr=gaff.returnArray();
	 length=datarr.length;
	 x_range=length;

	 xarr=new int[length];
	 yarr=new int[length];

	 windowSize=this.getSize();
     setScaleToSize(windowSize);
	 selectionRect=new Rectangle(0,0,10,10);//not used till later, initialized now for convenience.

	 for (int i=0;i<length;i++){
	  if (datarr[i]>maxY) maxY=datarr[i];
	  if (datarr[i]<minY) minY=datarr[i];
	  yarr[i]=datarr[i];
	  xarr[i]=i;
	  }


	  popup=new JPopupMenu();

	  menuListener myMenuListener=new menuListener(this);
	   sgListener myListener = new sgListener(this);

	  JMenuItem menuItem = new JMenuItem("jump to");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);
	  menuItem = new JMenuItem("range end");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);
	  menuItem = new JMenuItem("flip");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);


	  addMouseListener(myListener);
      addMouseMotionListener(myListener);



    }


    public Dimension getPreferredSize() {
        return preferredSize;
    }

  public void setZoom(int i){
  	//assume compression of 5 not implementied
	}


  public void scale(int offset){
  //set offset and multiplier for y data, in case of flipped tag...
  		int win_height=0;
  		int mult=1;
  		if (flipped){
  		   win_height=(int)scaleToSize.getHeight();
  		   mult=-1;
  		}

  		//rescale
  		for (int i=0;i<length;i++){
  				yarr[i]=(int)(win_height+mult*((double)(datarr[i]+y_offset)/y_scale));
  				xarr[i]=(int)((double)(i-offset)/x_scale);
  		}
	   }

  public void rescale(){
        setScaleToSize(windowSize);
        int windowHeight=(int)scaleToSize.getHeight();
		int windowWidth=(int)scaleToSize.getWidth();
		x_scale=(double)length/(double)windowWidth;
		y_scale=(double)(maxY-minY)/((double)windowHeight);
		y_offset=-1*minY;
		scale(0);
	 lastRange=new Dimension(x_offset,x_range);
	 x_offset=0; //used in zooming.
	 x_range=length;
	}

	public void rescaleToSelection(){
       if (invertSelection){
	    rescale();
	   }else
	   {
		//where in the xrange was the mouse clicked...
	    x_offset=x_offset+(int)(selectionRect.x*x_scale);

		//whats the new x_scale...
		x_scale=x_scale / (scaleToSize.getWidth()/selectionRect.width);
		x_range=(int)(scaleToSize.getWidth()*x_scale);

		scale(x_offset);
	   }

	  repaint();
	}

	public void setPoint(int x, int y){
	// uses x only for now...
	user_start =x_offset+(int)(x*x_scale);
	System.out.println(user_start );
	repaint();
	}


	public void resetSelection(int start_x, int start_y, int end_x, int end_y){
	 //if rectangle is inverted....
	 if (start_x>end_x){int tmp=start_x; start_x=end_x; end_x=tmp; invertSelection=true;}
	 else{
	 invertSelection=false;
	 if (start_y>end_y){int tmp=start_y; start_y=end_y; end_y=tmp;};
	 selectionRect.x=start_x;
	 selectionRect.y=start_y;
	 selectionRect.width=end_x-start_x;
	 selectionRect.height=end_y-start_y;
	  }
	 showSelection=true;
	}

    public void paintComponent(Graphics g) {
        super.paintComponent(g);  //paint background
		g.setColor(Color.white);
		g.fillRect(0,0,(int)windowSize.getWidth(),(int)windowSize.getHeight());

		//lazy way to detect a resize. should redo CHANGE
		Dimension tmp=this.getSize();
		if (!tmp.equals(windowSize)){
			windowSize=tmp;
			rescale();
			}
		g.setColor(Color.lightGray);
		g.fillRect(0,0,(int)windowSize.getWidth(),(int)windowSize.getHeight());
		if (lastRange != null){
		g.setColor(Color.cyan);
		g.drawRect((int)(lastRange.getWidth()/x_scale),0,(int)(lastRange.getHeight()/x_scale),(int)windowSize.getHeight());
		}


        g.setColor(Color.darkGray);
		g.drawPolyline(xarr,yarr,length);
		if ((user_start >x_offset)&&(user_start <x_offset+x_range))	{
		 g.setColor(Color.red);
		 g.drawLine((int)((user_start -x_offset)/x_scale),0, (int)((user_start -x_offset)/x_scale),(int)(windowSize.getHeight()-10));
		 }
		//decorate
		//Graphics2D g2 = (Graphics2D) g;
		g.setColor(Color.blue);
		g.setFont(font);
		String left_x = "<<"+x_offset;
		String right_x = ""+(x_offset+x_range)+">>";
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(left_x);
		int height = metrics.getHeight();
        g.drawString( left_x, 1, (int)(windowSize.getHeight())-1 );
		width = metrics.stringWidth(right_x);
		height = metrics.getHeight();
		g.drawString( right_x, (int)(windowSize.getWidth()-width),(int)(windowSize.getHeight())-1);
	 	if ((user_start >x_offset)&&(user_start <x_offset+x_range))	{
	 	 g.setColor(Color.red);
	 	 g.drawLine((int)((user_start -x_offset)/x_scale),0, (int)((user_start -x_offset)/x_scale),(int)(windowSize.getHeight()-10));

		 String frame=""+user_start ;
		 width=metrics.stringWidth(frame);
		 g.setColor(Color.lightGray);
		 g.fillRect((int)((user_start -x_offset)/x_scale-width/2),(int)(windowSize.getHeight())-height,width,height);
		 g.setColor(Color.white);
		 g.drawString(frame,(int)((user_start -x_offset)/x_scale-width/2),(int)(windowSize.getHeight())-1);
		}


		//Paint a filled rectangle at user's chosen point.
        if (showSelection){
		    g.setColor(Color.yellow);
            g.drawRect(selectionRect.x,selectionRect.y,selectionRect.width,selectionRect.height);
            g.setColor(Color.black);
	        }
	 }
}




class menuListener implements ActionListener{
 simpleGraph sg;
 public menuListener(simpleGraph sg){
  this.sg=sg;
  }

 //gets popup events
 public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."+ "    Event source: " + source.getText();
        System.out.println(s);
		if (source.getText().equals("flip")){
		 if (sg.flipped) sg.flipped=false; else sg.flipped=true;
		 sg.repaint(0,0,(int)sg.windowSize.getWidth(),(int) sg.windowSize.getHeight());
		 sg.rescale();
		}
}

}


class sgListener extends MouseInputAdapter {
	simpleGraph sg;
	int x;
	int y;
	boolean dragged=false;

		public sgListener(simpleGraph sg){
		   this.sg=sg;
		   System.out.println("added");
		}

	              public void mousePressed(MouseEvent e) {
				   if((e.getModifiers() & InputEvent.BUTTON1_MASK)== InputEvent.BUTTON1_MASK){
	                  x = e.getX();
	                  y = e.getY();
	                  sg.resetSelection(x,y,x+1,y+1);
					  System.out.println("pressed");
	                  sg.repaint();
					  }
	                  maybeShowPopup(e);
				  }

	              public void mouseDragged(MouseEvent e) {
					 if((e.getModifiers() & InputEvent.BUTTON1_MASK)== InputEvent.BUTTON1_MASK){
						sg.resetSelection(x,y,e.getX(),e.getY());
	                    sg.repaint();
						dragged=true;
	                 }
				  }

	              public void mouseReleased(MouseEvent e) {

						if((e.getModifiers() & InputEvent.BUTTON1_MASK)== InputEvent.BUTTON1_MASK){
							if (dragged){
							sg.showSelection=false;
							sg.rescaleToSelection();
							}
							else
							{
							sg.setPoint(x,y);
							}
							dragged=false;
	                    }
				        maybeShowPopup(e);

				  }

				 private void maybeShowPopup(MouseEvent e) {
				              System.out.println("checking popup");
				              if((e.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK)
							  {
							      System.out.println("trying popup");
				                  sg.popup.show(e.getComponent(),
				                             e.getX(), e.getY());
				              }
            }

          }