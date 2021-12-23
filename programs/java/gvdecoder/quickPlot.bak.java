import javax.swing.*;
import java.awt.*;
import ptolemy.plot.*;

public class quickPlot extends JInternalFrame {

public EditablePlot p;
public double start;
public double end;
public double inc;
public int dataset=1;


public quickPlot(double[] res, double start, double end){
	super("quick",
							 true, //resizable
							 true, //closable
							 true, //maximizable
			  true);//iconifiable
p=new EditablePlot();
p.setButtons(true);
this.start=start;
this.end=end;
inc=(end-start)/res.length;
init(res);
SetupWindow();
}

public quickLogPlot(double[] res, double start, double end){
	super("quick",
							 true, //resizable
							 true, //closable
							 true, //maximizable
			  true);//iconifiable
p=new EditablePlot();
p.setYLog(true);
p.setButtons(true);
this.start=start;
this.end=end;
inc=(end-start)/res.length;
init(res);
SetupWindow();
}



public quickPlot(float[] res, double start, double end){
	super("quick",
						 true, //resizable
						 true, //closable
						 true, //maximizable
			  true);//iconifiable
p=new EditablePlot();
p.setButtons(true);
this.start=start;
this.end=end;
inc=(end-start)/res.length;
init(res);
SetupWindow();
}


public void init(float[] res){
 boolean first=true;
	for (int i = 0; i < res.length; i++) {
	  p.addPoint(0, start+(double)i*inc,(double)res[i], !first);
	  first = false;
	}

}

public void init(double[] res){
    boolean first=true;
	for (int i = 0; i < res.length; i++) {
	  p.addPoint(0, start+(double)i*inc,(double)res[i], !first);
	  first = false;
	}
}

public void addPlot(double[] res){
	boolean first=true;
		for (int i = 0; i < res.length; i++) {
		  p.addPoint(dataset, start+(double)i*inc,(double)res[i], !first);
		  first = false;
	}
	dataset++;
}

public void addPlot(float[] res){
	boolean first=true;
		for (int i = 0; i < res.length; i++) {
		  p.addPoint(dataset, start+(double)i*inc,(double)res[i], !first);
		  first = false;
	}
	dataset++;
}


public void SetupWindow(){

 JPanel jp=new JPanel();
 jp.add(p);
 this.getContentPane().add(jp,BorderLayout.CENTER);
 setSize(new Dimension(500,350));
 setLocation(10,10);
}



}