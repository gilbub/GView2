package gvdecoder;
import java.lang.Math;

  public class Rounding
  {

	public static String toString (Double d, int place){
	 return Rounding.toString(d.doubleValue(),place);
	}

    public static String toString (double d, int place)
    {
      if (place <= 0)
        return ""+(int)(d+((d > 0)? 0.5 : -0.5));
      String s = "";
      if (d < 0)
        {
  	s += "-";
  	d = -d;
        }
      d += 0.5*Math.pow(10,-place);
      if (d > 1)
        {
  	int i = (int)d;
  	s += i;
  	d -= i;
        }
      else
        s += "0";
      if (d > 0)
        {
  	d += 1.0;
  	String f = ""+(int)(d*Math.pow(10,place));
  	s += "."+f.substring(1);
        }
      return s;
    }
}