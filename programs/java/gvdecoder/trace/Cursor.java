package gvdecoder.trace;

import java.awt.*;
import javax.swing.*;
import gvdecoder.prefs.*;
import java.awt.geom.AffineTransform;


public class Cursor{

 public final static int GENERAL     =0;
 public final static int NOTE		   =1;
 public final static int START_RANGE1=2;
 public final static int END_RANGE1  =3;
 public final static int START_RANGE2=4;
 public final static int END_RANGE2  =5;
 public final static int START_RANGE3=6;
 public final static int END_RANGE3  =7;
 public final static int START_RANGE4=8;
 public final static int END_RANGE4  =9;



 public String filename;
 public Integer number;
 public Double x_value;
 public Double y_value;
 public Double slope_value;
 public Integer position;
 public Color color=Color.black;
 public Boolean visible=new Boolean(true);
 public Rectangle SelectRect=null;
 public Trace trace=null;
 public int cursortype;
 public String note;
 public JLabel label;
 public FontMetrics metrics;
 public AffineTransform at;
 public double scaledposition;

public Cursor(String filename,
			  int num,
			  double x_val,
			  double y_val,
			  double m_val,
			  int pos,
			  Color col,
			  boolean vis){
	this.filename=filename;
	number=new Integer(num);
	x_value=new Double(x_val);
	y_value=new Double(y_val);
	slope_value=new Double(m_val);
	position=new Integer(pos);
	color=col;
	visible=new Boolean(vis);
	SelectRect=new Rectangle(0,0,12,10);
    cursortype=GENERAL;

}



public Cursor(String filename, int num, double x_val, int pos, int cursortype){
	this.filename=filename;
	number=new Integer(num);
	x_value=new Double(x_val);
	y_value=new Double(0.0);
	slope_value=new Double(0.0);
    position=new Integer(pos);
    SelectRect=new Rectangle(0,0,12,10);
    this.cursortype=cursortype;
    switch (cursortype){
		case GENERAL:      color=Color.GREEN; break;
		case NOTE:         color=Color.GREEN.darker(); break;
		case START_RANGE1: color=Color.RED;    break;
		case END_RANGE1:   color=Color.RED.darker();break;
		case START_RANGE2: color=Color.BLUE;break;
		case END_RANGE2:   color=Color.BLUE.darker();break;
		case START_RANGE3: color=Color.MAGENTA;break;
 		case END_RANGE3:   color=Color.MAGENTA.darker();break;
		case START_RANGE4: color=Color.ORANGE;break;
		case END_RANGE4:   color=Color.ORANGE.darker();break;
		default: color=color.GRAY;
	}
    //metrics=getFontMetrics(font);
	}

public Cursor(){
}

public void setNote(String note){
	this.note=note;
}

/*not complete*/
public String toString(){
  return "filename="+filename+",number="+number+",x_value="+x_value+",y_value="+y_value+",slope_value="+slope_value+",position="+position+",color="+color.getRGB()+",type="+cursortype+",note="+note;
}

public void fromString(String str){
 String[] vals=str.split(",");
 filename=get(vals,"filename");
 number=Integer.valueOf(get(vals,"number"));
 x_value=Double.valueOf(get(vals,"x_value"));
 y_value=Double.valueOf(get(vals,"y_value"));
 slope_value=Double.valueOf(get(vals,"slope_value"));
 position=Integer.valueOf(get(vals,"position"));
 int col=Integer.parseInt(get(vals,"color"));
 color=new Color(col);
 cursortype=Integer.parseInt(get(vals,"type"));
 note=get(vals,"note");
  SelectRect=new Rectangle(0,0,12,10);
 label=new JLabel(note);
}

private String get(String[] arr,String key){
	for (int i=0;i<arr.length;i++){
		String[] pairs=arr[i].split("=");
		if (pairs[0].equals(key)) return pairs[1];
	}
	return null;
}


public void paintCursor(Graphics2D g,
						int x_offset,
						int x_range,
						double x_scale,
						Dimension windowSize, //scale to this
						int height, //height of font
						int width, //width of font
						boolean shownote
						){

	     double cursor_position=(double)position.intValue();
	     if ((cursor_position >x_offset)&&(cursor_position <x_offset+x_range))	{
			     scaledposition=(cursor_position-x_offset)/(x_scale);

		 	 	 //int scaledposition=(int)((user_start -x_offset)/x_scale);
		 	 	 g.setColor(color);
		 	 	 g.drawLine((int)scaledposition,0, (int)scaledposition,(int)(windowSize.getHeight()-10));
		 		 String frame=""+cursor_position ;

		 		 g.setColor(Color.lightGray);
		 		 g.fillRect((int)(scaledposition-width/2),(int)(windowSize.getHeight())-height,width,height);
		 		 g.setColor(Color.black);
		 		 //at=new AffineTransform();
				 //g.rotate(90.0*Math.PI/180.0);
                 //g.setTransform(at);
		 		 g.drawString(frame,(int)(scaledposition-width/2),(int)(windowSize.getHeight())-1);
		         //g.rotate(270.0*Math.PI/180.0);

		         if ((shownote)&&(note!=null)&&(!note.equals("null"))){
				    g.setColor(Color.yellow);
				   FontMetrics m=g.getFontMetrics();
				   //int stagger=(int)((number.intValue()*m.getHeight())%(windowSize.getHeight()-20));
                   int stagger=0;
				   g.fillRect(((int)scaledposition-6),stagger+25-m.getHeight(),m.stringWidth(note)+2,m.getHeight());
				   g.setColor(Color.blue);
				   g.drawString(note,(int)(scaledposition-5),23+stagger);
				   //label.paint(g);
				 }
		         SelectRect.setLocation((int)(scaledposition)-5,0);

		         g.setColor(color);
		         g.fill(SelectRect);
		         if ((note!=null)&&(!note.equals("null"))){
			     //g.setColor(Color.white);
			     //g.drawRect(SelectRect.x-1,SelectRect.y-1,SelectRect.width+2,SelectRect.height+2);
                 //g.setColor(Color.black);
                 g.fillOval(SelectRect.x-3,SelectRect.y-3,SelectRect.width+6,SelectRect.height+6);
          		 }
		         g.setColor(Color.white);
		         g.drawString(""+number,(int)(scaledposition)-5,9);
                 if (trace!=null){
                   g.setColor(Color.black);

                   g.drawString("h"+y_value.toString(),(int)(scaledposition)-5,19);
			     }
		}
	   }//end paintCursor


}