package gvdecoder;
import java.awt.geom.*;
import java.awt.*;

public class Ruler{

int firstX;
int firstY;
int lastX=-1;
int lastY=-1;
double distance=0.0;

Color color;
Viewer2 vi;


public Ruler(Viewer2 vi,Color col){
   firstX=-1;
   firstY=-1;
   lastX=-1;
   lastY=-1;

   this.color=col;
   this.vi=vi;
  }

public boolean isDrawable(){
return ((firstX>0)&&(firstY>0)&&(lastX>0)&&(lastY>0));
}

 public Shape returnShape(){

   Polygon pg=new Polygon();
   pg.addPoint(firstX,firstY);
   pg.addPoint(lastX,lastY);
   GeneralPath gp=new GeneralPath(pg);
   AffineTransform at=new AffineTransform();
   at.setToScale((double)vi.viewScale,(double)vi.viewScale);
   Shape newpg=gp.createTransformedShape(at);
   return newpg;


 }

public void updateResults(){

	distance=Math.sqrt(Math.pow(lastX-firstX,2)+Math.pow(lastY-firstY,2));
	System.out.println("D="+distance+ " ("+firstX+","+firstY+")("+lastX+","+lastY+")");

}
}