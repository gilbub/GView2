package gvdecoder;
import java.awt.*;


public class spacer extends Canvas{
int x,y;
public spacer(int xx, int yy){x=xx; y=yy;}
public Dimension getPreferredSize(){
 return new Dimension(x,y);
}



}