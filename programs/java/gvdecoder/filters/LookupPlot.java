//package filters;


import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JFrame;

// The java.io imports are only necessary for the right hand plot.
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

 
public class LookupPlot extends JFrame implements EditListener{

    int[] lookup;
	 
	 
	public void editDataModified(EditablePlot source, int dataset){
	 System.out.println("modify dataset");
	 
	}
    LookupPlot(int[] dataset) {
        
		lookup=dataset;
        
		EditablePlot plot = new EditablePlot();
        
		plot.addEditListener(this); 
		plot.setEditable(1);
		
        // Set the size of the toplevel window.
        setSize(400, 300);

        // Create the left plot by calling methods.
	    plot.setSize(350,250);
        plot.setButtons(true);
		plot.setXLabel("level");
		plot.setYLabel("val");
		 
		
       
        boolean first = true;
        for (int i = 0; i < lookup.length; i++) {
		    
            plot.addPoint(0, (double)i,(double)lookup[i], !first);
              
            first = false;

        }

        // Layout the two plots
        
        getContentPane().add(plot);
		JButton updatelookup= new JButton("update");
		LookupListener lookupListener=new LookupListener(this);
		updatelookup.addActionListener(lookupListener);
		getContentPane().add(updatelookup);

        show();
    }


  
}
class LookupListener implements ActionListener{
LookupPlot lp;
public LookupListener(LookupPlot lp){
 this.lp=lp;
 }
public void actionPerformed(ActionEvent e){
  double[][] tmp=lp.getData(0);
  for (int i=0;i<lp.lookup.length;lp++){
   lookup[i]=(int)tmp[1][i];
   System.out.println(i+" "+lookup[i]);
  }

 }
}

}
 
