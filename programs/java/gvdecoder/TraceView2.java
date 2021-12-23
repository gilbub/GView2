package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import gvdecoder.trace.*;
import java.util.*;
import java.awt.geom.GeneralPath;
//import java.awt.geom.GeneralPath;
import java.awt.Graphics2D.*;
import com.sun.image.codec.jpeg.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.geom.AffineTransform;
import java.net.URLConnection;
import javax.swing.event.*;
import java.awt.event.*;

/*
*/
public class TraceView2 extends JPanel{

public static final int MOVE_FIELD_MODE=1;
public static final int SHIFT_TRACE_MODE=2;
public static final int ZOOM_MODE=0;

public int mode=ZOOM_MODE;

public BufferedImage buffer;

String imagefilename;

public int x,y,width,height;

public JythonHelper2 jh=null;


public Vector matrices;


public int xdim=500;
public int ydim=500;

public Color selectcol=Color.black;
public Rectangle selectrect=null;
public Rectangle ruler=null;
public boolean setstartend=false;
public boolean usingscrollpane=false;

public boolean rulermode=false;
public boolean cursormode=false;

JCheckBox showSecondary;
JCheckBox showCalcium;
JCheckBox showVoltage;
JCheckBox showIndex;
JCheckBox showRuler;
JCheckBox showCursor;
JCheckBox showScrollbars;

JButton unzoom;
JButton savepic;

public  Color label_color=Color.yellow;
public  Color xy_color=Color.cyan;
public  Color xy_back_color=Color.gray;
public  Color voltage_color=Color.white;
public  Color calcium_color=Color.gray;
public  Color background_color=Color.black;
public  Color ruler_color=Color.red;
public  Color select_color=Color.yellow;
public  Color border_color=Color.orange;
public  Color apstart_color=Color.blue;
public  Color apend_color=Color.red;
public  Color overlay_color=Color.gray;

AffineTransform at;



public Vector viewables;

public boolean Is_Control_Down=false;

public TraceView2( int width, int height){
this.x=x;
this.y=y;
this.width=width;
this.height=height;
xdim=width;
ydim=height;
//botx=(float)xdim;
//boty=(float)ydim;
setSize(width,height);
trListener trlistener=new trListener(this);
addMouseListener(trlistener);
addMouseMotionListener(trlistener);
addKeyListener(new keyboardListener(this));
}


public Dimension getPreferredSize(){
	return new Dimension(xdim,ydim);
}


public int maxxindex=0;
public int maxyindex=0;
public int maxzindex=0;
public void add(Matrix ma, Color c, String name){
	if ((viewables==null)||(viewables.size()==0)){
		 if (viewables==null) viewables=new Vector();
		 maxxindex=ma.xdim;
		 maxyindex=ma.ydim;
		 maxzindex=ma.zdim;
		 start_x=0;
		 end_x=ma.zdim;
		 botx=maxxindex;
		 boty=maxyindex;
	}
	viewables.add(new Viewable(ma,c,true,name));
}

public BufferedImage getImage(){
 return buffer;
}

public void saveImage(){
	AnalysisHelper.getAnalysisHelper().saveImage(getImage());
}


/*
 topx etc bound the coordinates of the traces viewed
*/
public float topx=0.0f; //controlled by zoom
public float topy=0.0f; //controlled by zoom
public float botx=16.0f;//usually equal to xdim, but controlled by zoom()
public float boty=16.0f;//usually equal to ydim, but controlled by zoom()

public float where_x(int x){
 float xpos=(float)x/(float)xdim;
 return topx+(botx-topx)*xpos;
}


public float where_y(int y){
 float ypos=(float)y/(float)ydim;
 return topy+(boty-topy)*ypos;
}


public int start_x;
public int end_x;
public void setZRange(float begin, float end){
	/*
	 sets the zdim range using mouse input
	*/
	System.out.println("in set zrange");
	begin=begin-(int)begin;
	end=end-(int)end;
    // unzoom range
	if (end<begin) {
		start_x=0;
		end_x=maxzindex;
	}else{

	int newstart_x=(int)(start_x+begin*(end_x-start_x));
	end_x=(int)(start_x+end*(end_x-start_x));
	start_x=newstart_x;
    }
    System.out.println("   zrange="+start_x+","+end_x);
}


float xscale=1.0f;
float yscale=1.0f;
float scaledwidth=32.0f;
float scaledheight=32.0f;

public void zoom(int tx, int ty, int bx, int by){

    if ((tx==0)&&(ty==0)&&(bx==xdim)&&(by==ydim)){
		topx=0;botx=maxxindex;topy=0;boty=maxyindex;
	  }
	  else{
		float newtopx=topx+(botx-topx)*((float)tx/(float)xdim);
		float newtopy=topy+(boty-topy)*((float)ty/(float)ydim);
		float newbotx=topx+(botx-topx)*((float)bx/(float)xdim);
		float newboty=topy+(boty-topy)*((float)by/(float)ydim);
		topx=newtopx;topy=newtopy;botx=newbotx;boty=newboty;

		if (botx<=topx) botx=topx+1;
		if (boty<=topy) boty=topy+1;
		}
    xscale=(float)xdim/(bx-tx);
    yscale=(float)ydim/(by-ty);
    scaledwidth=width*xscale;
    scaledheight=height*yscale;
   // System.out.println("in zoom, entered =("+tx+","+ty+","+bx+","+by+","+tracewindowwidth+","+tracewindowheight+")");
   // System.out.println(" zoom calcs (topx,topy,botx,boty,xscale,yscale,scaledwidth,scaledheight)="+topx+","+topy+","+botx+","+boty+","+xscale+","+yscale+","+scaledwidth+","+scaledheight+")");
}


public double tracexscale;
public double traceyscale;
public float step;
public int arraylength;
double min;
double max;
public Shape getShape(Matrix ma, int y, int x, int start, int end, Rectangle bounds){
	arraylength=(int)(bounds.width*2.0f);
	if (end-start<bounds.width*2) arraylength=end-start;
	step=((float)(end-start))/arraylength;

	GeneralPath gp=new GeneralPath(GeneralPath.WIND_EVEN_ODD,arraylength+1);

	min=Double.POSITIVE_INFINITY;
	max=Double.NEGATIVE_INFINITY;
	double val=0;
	gp.moveTo(0.0f,(float)ma.dat.get(start,y,x));
	int count=0;
	for (float f=(float)start;f<end;f+=step){
		val=ma.dat.get((int)f,y,x);
		if (val>max) max=val;
		if (val<min) min=val;
		gp.lineTo((float)count++,(float)ma.dat.get((int)f,y,x));
	}
	at=new AffineTransform();
	tracexscale=(double)(bounds.width/(double)arraylength);
	traceyscale=(double)(bounds.height/(max-min));
	at.setToScale(tracexscale ,traceyscale);
	at.translate( bounds.x/tracexscale, bounds.y/traceyscale-min);
	return at.createTransformedShape(gp);
	//scale this to fit in bounds.
}

public Vector selectedtraces=null;
public boolean drawselected=true;

public void rendersingle(Graphics2D g, Matrix ma, int y, int x, int zstart, int zend, Rectangle bounds, Color c, boolean drawlabels){

   if ((drawselected)&&(selectedtraces!=null)){
	g.setColor(overlay_color);
	for (int s=0;s<selectedtraces.size();s++){
		SelectedTrace selected=(SelectedTrace)selectedtraces.elementAt(s);
		g.draw(getShape(selected.m,selected.y,selected.x,zstart,zend,bounds));
	}
   }
   g.setColor(c);
   g.draw(getShape(ma,y,x,zstart,zend,bounds));
   g.setColor(border_color);
   g.draw(bounds);
   if (drawlabels){
	 g.setColor(xy_back_color);
	 g.fillRect(bounds.x+1,bounds.y+1,28,10);
	 g.setColor(xy_color);
	 g.drawString((y+","+x),bounds.x+1,bounds.y+9);
   }

}

public void addSelectedTrace(Matrix ma, int y, int x){
	if (selectedtraces==null){ selectedtraces=new Vector();}
	selectedtraces.add(new SelectedTrace(ma,y,x));
}



/** actually draws something.

public void render(Matrix ma, int y, int x, int zstart, int zend, Rectangle bounds, Color c, boolean clear, boolean drawlabels){
if (buffer==null){
			buffer=new BufferedImage(xdim,ydim,BufferedImage.TYPE_INT_RGB);
		}
	Graphics2D g2= buffer.createGraphics();
    if (clear){
    g2.setColor(background_color);
	g2.fillRect(0,0,xdim,ydim);
    }
    rendersingle(g2,ma,y,x,zstart,zend,bounds,c,drawlabels);



}



public void render(Matrix ma, int zstart, int zend, Color c, boolean overplot, boolean drawlabels){
	rect_w=(int)((double)xdim/ma.xdim);
	rect_h=(int)((double)ydim/ma.ydim);
	java.awt.Rectangle r=new Rectangle(0,0,rect_w,rect_h);
	for (int y=0;y<ma.ydim;y++){
		for (int x=0;x<ma.xdim;x++){
   			r.setLocation(x*rect_w,y*rect_h);
   			if ((y==0)&&(x==0)) clear=true;
   			else clear=false;
   			if (overplot) clear=false;
            render(ma,y,x,zstart,zend,r,c,clear,drawlabels);
	}
  }
}
*/


public int rect_w;
public int rect_h;
public boolean clear;
public boolean label;

public int singleview_rect_width=150;
public int singleview_rect_height=75;
public void renderOverplot(float y, float x, int my, int mx){
  if (buffer==null){ buffer=new BufferedImage(xdim,ydim,BufferedImage.TYPE_INT_RGB);}
   Graphics2D g2= buffer.createGraphics();
   g2.setColor(background_color);
   int sx=mx;
   if (mx+singleview_rect_width>xdim) sx=xdim-singleview_rect_width;
   int sy=my;
   if (my+singleview_rect_height>ydim) sy=ydim-singleview_rect_height;

   java.awt.Rectangle r=new java.awt.Rectangle(sx,sy,singleview_rect_width, singleview_rect_height);
   g2.fill(r);
   for (int v=0;v<viewables.size();v++){
	   Viewable viewable=(Viewable)viewables.elementAt(v);
   		rendersingle(g2,viewable.m,(int)y,(int)x,start_x,end_x,r,viewable.c,true);
     }
  g2.dispose();
}

public boolean Show_On_MouseOver=true;
public void mouseMoved(int x, int y){
	if (Show_On_MouseOver){
        render();
		renderOverplot(where_y(y), where_x(x), y,x);

    }
   repaint();
}


public boolean label_xy=true;
public void render(){

    if (buffer==null){
			buffer=new BufferedImage(xdim,ydim,BufferedImage.TYPE_INT_RGB);
		}
	Graphics2D g2= buffer.createGraphics();
    g2.setColor(background_color);
	g2.fillRect(0,0,xdim,ydim);

   rect_w=(int)((float)xdim/(botx-topx));
   rect_h=(int)((float)ydim/(boty-topy));
   float xoffset=topx%1.0f;
   float yoffset=topy%1.0f;
   float botx_1=botx+1.0f;
   if ((int)botx_1>maxxindex) botx_1=botx;
   float boty_1=boty+1.0f;
   if ((int)boty_1>maxyindex) boty_1=boty;

   java.awt.Rectangle r=new Rectangle(0,0,rect_w,rect_h);
   for (int v=0;v<viewables.size();v++){
	   Viewable viewable=(Viewable)viewables.elementAt(v);
	   //render from topx topy to botx boty
       if ((v==viewables.size()-1) && (label_xy)) label=true; else label=false;
	   for (int x=(int)topx;x<(int)botx_1;x++){
		   for (int y=(int)topy;y<(int)boty_1;y++){
			r.setLocation((int)((x-topx)*rect_w),(int)((y-topy)*rect_h));
   			 rendersingle(g2,viewable.m,y,x,start_x,end_x,r,viewable.c,label);
   			//render(Graphics2D g,viewable.m,y,x,start_x,end_x,r,viewable.c,false,label);
		}
	}

  }
  g2.dispose();
  repaint();
 }

   //use topx topy botx boty to calculate the



public void paintComponent(Graphics g){
	Graphics2D g2=(Graphics2D)g;
	if (buffer!=null){
		g2.drawImage(buffer,null,0,0);
	}
	if (selectrect!=null){
	 g2.setColor(selectcol);
	 g2.draw(selectrect);
    }
}


public void setLocationFromDrag(int firstx, int lastx, int firsty, int lasty, float orig_topx, float orig_topy){
	float xrangespan=botx-topx;
	float yrangespan=boty-topy;
	int winwidth=rect_w;
	int winheight=rect_h;
	int xdif=lastx-firstx;
	int ydif=lasty-firsty;
	float x_shift=((float)xdif)/((float)winwidth);
	float y_shift=((float)ydif)/((float)winheight);
	topx=orig_topx-x_shift;
	topy=orig_topy-y_shift;
	if (topx<0)topx=0;
	if (topy<0)topy=0;
	botx=topx+xrangespan;
	boty=topy+yrangespan;
	if (botx>maxxindex){ botx=maxxindex; topx=botx-xrangespan;}
	if (boty>maxyindex){ boty=maxyindex; topy=boty-yrangespan;}
	render();
}

public void setTimeFromDrag(int firstx, int lastx, int orig_start){

	//dragging left to right, corresponds to going backward in time
	//visible time interval
	int interval=end_x-start_x;
	int winwidth=rect_w;
	float onepixeldrag=(float)interval/(float)winwidth;
	int timeshift=(int)(onepixeldrag*(lastx-firstx));
	if (firstx<lastx){
		start_x=orig_start-timeshift;
		if (start_x<0) start_x=0;
		end_x=start_x+interval;
		System.out.println("first<last "+start_x+","+end_x+","+timeshift);
	}
	if (firstx>lastx){
		start_x=orig_start-timeshift;
		if (start_x+interval>maxzindex) start_x=maxzindex-interval;
		end_x=start_x+interval;
		System.out.println("first>last "+start_x+","+end_x+","+timeshift);

	}
	render();
	}





}

class keyboardListener extends KeyAdapter{
    TraceView2 tr;
    public keyboardListener(TraceView2 tr){
		this.tr=tr;
	}

	public void keyReleased(KeyEvent e){
		tr.render();
		}
	}



class trListener extends MouseInputAdapter {
	TraceView2 vi;
	int x;
	int y;
	int tx=0;

	int ty=0;
	int bx=0;
	int by=0;
	boolean dragged=false;
	boolean shifton=false;
	int orig_start_x;
    float orig_start_topx;
    float orig_start_topy;

	public trListener(TraceView2 vi){
		   this.vi=vi;
		}

    public void mouseMoved(MouseEvent e){
	 if (e.isAltDown()){
	      vi.mouseMoved(e.getX(),e.getY());
		 }
	}


	public void mousePressed(MouseEvent e) {
	 	  tx = e.getX();
		  ty = e.getY();
         // System.out.println("x="+x+"+y="+y);
		  orig_start_x=vi.start_x;
		  orig_start_topx=vi.topx;
		  orig_start_topy=vi.topy;

		}

	  public void mouseDragged(MouseEvent e) {
		 bx=e.getX();
		 by=e.getY();

         switch(vi.mode){
		  case 1:
		          vi.setLocationFromDrag(tx,bx,ty,by,orig_start_topx,orig_start_topy);
		          break;
		  case 2:
			 	  vi.setTimeFromDrag(tx,bx,orig_start_x);
			 	  break;
          case 0:
				   if ((bx<tx)||(by<ty)){
				  //dragging right to left
				  vi.selectcol=Color.red;
				  vi.selectrect=new Rectangle(bx,by,(tx-bx),(ty-by));

				 }else{
				 //dragging left to right
				 vi.selectcol=Color.green;
				 vi.selectrect=new Rectangle(tx,ty,(bx-tx),(by-ty));
				  }
				 break;
		        }
		 vi.repaint();
	}


  public void mouseReleased(MouseEvent e) {
    if (vi.mode==vi.ZOOM_MODE){
     vi.selectrect=null;
     //float winwidth=(((float)vi.xdim/(float)vi.maxxindex)); //31
     //float winheight=(((float)vi.ydim/(float)vi.maxyindex)); //31
    // float x=(bx-tx)/winwidth;
    // float y=(by-ty)/winheight;
     if (Math.abs(vi.where_x(bx)-vi.where_x(tx))<1.0f){
		vi.setZRange(vi.where_x(tx), vi.where_x(bx));

	  }
	  else{
        if ((bx<tx)||(by<ty)){
        tx=0;ty=0;bx=vi.xdim;by=vi.ydim;
        }
        vi.zoom(tx,ty,bx,by);
       }
     vi.render();
     }
        vi.requestFocus();

    }
  }

class SelectedTrace{
	public Matrix m;
	public int x;
	public int y;
	public SelectedTrace(Matrix m, int y, int x){
		this.m=m;
		this.y=y;
		this.x=x;
	}
}

class Viewable{
	public Matrix m;
	public Color c;
	public boolean isVisible;
	public String name;
 public Viewable(Matrix m, Color c, boolean isVisible, String name){
	 this.m=m;
	 this.c=c;
	 this.isVisible=isVisible;
	 this.name=name;
 }
}
