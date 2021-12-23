package gvdecoder;
import java.awt.*;
import java.awt.geom.*;
public class polytest{

Polygon pg;
GeneralPath gp;


public polytest(){

pg=new Polygon();
pg.addPoint(10,10);
pg.addPoint(10,20);
pg.addPoint(20,20);
pg.addPoint(20,10);
System.out.println("contains... 15,15? "+pg.contains(15,15));
System.out.println("contains... 5,5? "+pg.contains(5,5));
System.out.println("contains... 20,20? "+pg.contains(20,20));
System.out.println("contains... 10,10? "+pg.contains(10,10));
gp=new GeneralPath(pg);
System.out.println("gp contains... 15,15? "+gp.contains(15,15));
System.out.println("contains... 5,5? "+gp.contains(5,5));
System.out.println("contains... 20,20? "+gp.contains(20,20));
System.out.println("contains... 10,10? "+gp.contains(10,10));
AffineTransform at=new AffineTransform();
at.setToScale(2,2);
Shape newpg=gp.createTransformedShape(at);
System.out.println("new contains... 15,15? "+newpg.contains(15,15));
System.out.println("new contains... 5,5? "+newpg.contains(5,5));
System.out.println("new contains... 20,20? "+newpg.contains(20,20));
System.out.println("new contains... 10,10? "+newpg.contains(10,10));

PathIterator pi=newpg.getPathIterator(null);
double[] cords=new double[6];
while (!pi.isDone()){
 int tp=pi.currentSegment(cords);
 System.out.println(tp+" , "+cords[0]+","+cords[1]);
 pi.next();
}
}
public static void main(String[] arg){

polytest pt=new polytest();
}

}