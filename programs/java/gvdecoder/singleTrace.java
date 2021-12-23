package gvdecoder;
import java.util.*;
import java.awt.Color;
import java.awt.geom.*;

public class singleTrace{

	public Color col;
	public int x;
	public int y;
	public AffineTransform at;
	public GeneralPath gp;
	public Vector APStarts;
	public Vector APEnds;
	public Vector labels;
    public float[] data;
    public float max;
    public float min;
    public String name;





    public void getGeneralPath(int start, int end, float width, float height){
			int arraylength=(int)(width*2.0f);
			if (end-start<width*2) arraylength=end-start;
			float step=((float)(end-start))/arraylength;
			double[] tmp=new double[arraylength];
			double min=1000000;
			double max=-1000000;
			double c;
		    gp=new GeneralPath(GeneralPath.WIND_EVEN_ODD,tmp.length+1);
			int index=0;
			for (float i=(float)start;i<end;i+=step){
				 c=data[(int)i];
				 index++;
				 if (index<tmp.length)
				   tmp[index]=c;
				 if (min>c) min=c;
				 if (max<c) max=c;
			}
			float h_scale=height/((float)(max-min));
			float w_scale=width/tmp.length;
			gp.moveTo(0.0f,(float)((tmp[0]-min)*h_scale));

			for (int ii=1;ii<tmp.length;ii++){
			    gp.lineTo((float)ii*w_scale,(float)(tmp[ii]-min)*h_scale);
	           }

	    }

       //if (at!=null) gp.transform(at);


}