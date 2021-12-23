import processing.core.PApplet as papplet
import java.awt.BorderLayout as BorderLayout
import javax.swing.border.TitledBorder as TitledBorder
import javax.swing.SwingUtilities as SwingUtilities
import javax.swing.JOptionPane as optionpane
import javax.swing.BoxLayout as BoxLayout
import javax.swing.JInternalFrame as JInternalFrame
import javax.swing.JTextArea as JTextArea
import javax.swing.JTextField as JTextField
import javax.swing.JButton as JButton
import javax.swing.JPanel as JPanel
import javax.swing.JLabel as JLabel
import javax.swing.SwingConstants as SwingConstants
import javax.swing.JProgressBar as JProgressBar
import javax.swing.SwingWorker as SwingWorker
import javax.swing.JComboBox as JComboBox
import java.awt.geom.GeneralPath as GeneralPath
import java.awt.geom.AffineTransform as AffineTransform
import javax.swing.JEditorPane as JEditorPane
import javax.swing.event.HyperlinkListener as HyperlinkListener
import javax.swing.JScrollPane as JScrollPane
import javax.swing.JOptionPane as JOptionPane
import javax.swing.ImageIcon as ImageIcon
import javax.swing.JCheckBox as JCheckBox
import org.apache.batik.svggen.SVGGraphics2D as SVGGraphics2D
import org.apache.batik.dom.GenericDOMImplementation as GenericDOMImplementation
import java.io.StringWriter
import gvdecoder.Canvas as canvas
import os
import java.lang.StringBuilder

@java floodfill

public void merge(Matrix m_out, gvdecoder.Viewer2 v1, gvdecoder.Viewer2 v2){
 for (int z=0;z<m_out.zdim;z++){
  System.out.println("merging frame "+z);
  v1.JumpToFrame(z);
  v2.JumpToFrame(z);
  for (int y=0;y<m_out.ydim;y++){
   for (int x=0;x<m_out.xdim;x++){
    double s1=v1.datArray[y*m_out.xdim+x];
    double s2=v2.datArray[y*m_out.xdim+x];
    m_out.dat.set(z,y,x,s1+s2); 
   }
  }
 }
}

public double[] APShape(Matrix ma, int x, int y, int steps, int w){
 double[] res=new double[steps]; 
 double[] tmp=new double[ma.zdim];
 int xx,yy,z,zz;
 int stepsize=(int)((ma.zdim*1.0)/steps);
 //1 make trace
 int count=0;
 if (ma.dat.get(0,y,x)==0) return res;
 for ( yy=y-w;yy<y+w;yy++){
  for (xx=x-w;xx<x+w;xx++){
   if ( (ma.dat.get(0,yy,xx)>0) && (inbounds(ma,yy,xx)) ){
     count++;
     for (z=0;z<ma.zdim;z++) tmp[z]+=ma.dat.get(z,yy,xx);
     }
    }
   }
  for (z =0;z<ma.zdim;z++)tmp[z]=tmp[z]/count;
  count=0;
  for (z =0;z<ma.zdim;z+=stepsize){
   if (z+stepsize<=ma.zdim){
   for (zz=z;zz<z+stepsize;zz++){
      res[count]+=tmp[zz];
    }//zz
   res[count]=(int)(res[count]/stepsize);
   count+=1;
   }//if
 }//z
 return res;
      
 }


public boolean inbounds(Matrix ma, int y, int x){
  return ((y>=0)&&(y<ma.ydim)&&(x>=0)&&(x<ma.xdim));
}

public void replace(Matrix ma, int z, double origvalue, double newvalue){
 for (int y=0;y<ma.ydim;y++){ 
  for (int x=0;x<ma.xdim;x++){
    if (ma.dat.get(z,y,x)==origvalue) ma.dat.set(z,y,x,newvalue);
   }
  }
}


public void median(Matrix ma, int z, Matrix mn){
 double[] vals=new double[9];
 int k=0;
 for (int y=0;y<ma.ydim;y++){
  for (int x=0;x<ma.xdim;x++){
   for ( k=0;k<9;k++) vals[k]=-1;
   k=0; 
   for (int j=-1;j<=1;j++){
    for (int i=-1;i<=1;i++){
     int ny=y+j;
     int nx=x+i;
     if (inbounds(ma,ny,nx)){
      //int v=(int)java.lang.Math.round(ma.dat.get(z,ny,nx));
      double v=ma.dat.get(z,ny,nx);
      vals[k++]=v;
      }//if
      }//i
     }//j
     java.util.Arrays.sort(vals);
     mn.dat.set(z,y,x,vals[4]);
   }
  }
}

public void spatialAverage(Matrix ma, int w, int thr){
    Matrix tmp=new Matrix(1,ma.ydim,ma.xdim);
    for (int z=0;z<ma.zdim;z++){
        System.out.println("z="+z);
        tmp.dat.set(0);
        for (int y=0;y<ma.ydim;y++){
           for (int x=0;x<ma.xdim;x++){
            double sum=0;
            int count=0;
            if (ma.dat.get(z,y,x)>thr){  
              for (int yy=y-w;yy<=y+w;yy++){
                 for (int xx=x-w;xx<=x+w;xx++){
                   if (inbounds(ma,yy,xx)){
                     double v=ma.dat.get(z,yy,xx);
                     if (v>thr){
                      sum+=v;
                      count+=1;
                    }//>thr
                   }//inbounds
                  }//for xx
                 }//for yy
                 if (count>0) tmp.dat.set(0,y,x,sum/count);
                }// if ma.get>thr
               }//for x
              }//for y
            for (int yi=0;yi<tmp.ydim;yi++){
              for (int xi=0;xi<tmp.xdim;xi++){
                 ma.dat.set(z,yi,xi,tmp.dat.get(0,yi,xi));
              }
             } 
            }//for z   
          } 
                   
public void dilate(Matrix ma, int z, double val, double tmpval){
for (int y=0;y<ma.ydim;y++){
 for (int x=0;x<ma.xdim;x++){
  if (ma.dat.get(z,y,x)==val){ 
    if (ma.dat.get(z,y,x+1)<=0) ma.dat.set(z,y,x+1,tmpval);
    if (ma.dat.get(z,y,x-1)<=0) ma.dat.set(z,y,x-1,tmpval);
    if (ma.dat.get(z,y-1,x)<=0) ma.dat.set(z,y-1,x,tmpval);
    if (ma.dat.get(z,y+1,x)<=0) ma.dat.set(z,y+1,x,tmpval);
   }
  }
 } 
 replace(ma,z,tmpval,val);
}

public void erode(Matrix ma, int z, double val, double tmpval){
for (int y=0;y<ma.ydim;y++){
 for (int x=0;x<ma.xdim;x++){
  if (ma.dat.get(z,y,x)==0){ 
    if (ma.dat.get(z,y,x+1)==val) ma.dat.set(z,y,x+1,tmpval);
    if (ma.dat.get(z,y,x-1)==val) ma.dat.set(z,y,x-1,tmpval);
    if (ma.dat.get(z,y-1,x)==val) ma.dat.set(z,y-1,x,tmpval);
    if (ma.dat.get(z,y+1,x)==val) ma.dat.set(z,y+1,x,tmpval);
   }
  }
 } 
 replace(ma,z,tmpval,0);
}


                         
@/java


class activationmap:
 def __init__(self):
      self.marching=gvdecoder.MarchingSquares()
      self.kernel=self.makekernel(5,3)
      self.svgsetup=0
      self.barcolors=[]
      self.makebar=1
      self.zerobackground=0
      self.mb=None
      self.gui()

 def gui(self):
  self.panel=JPanel()
  self.panel.layout=BoxLayout(self.panel, BoxLayout.X_AXIS)
  self.st1panel=JPanel()
  self.st1panel.layout=BoxLayout(self.st1panel, BoxLayout.X_AXIS)
  self.st1panel.setBorder(TitledBorder("Setup: From a Viewer with one processed ROI, select one activation sequence with the Navigator window (~200ms)."))
  self.st1panel.add(JLabel(" a) Get (flipped"))
  self.flipcheckbox=JCheckBox()
  self.st1panel.add(self.flipcheckbox)
  self.st1panel.add(JLabel(") selected data: "))
  self.getnavrangebutton=JButton("get",actionPerformed=self.guicapturenavigator)
  self.st1panel.add(self.getnavrangebutton)
  self.st1panel.add(JLabel(" b) Set AP shape from Navigator (optional): "))
  self.setapshapebutton=JButton("set",actionPerformed=self.guisetshape_roi)
  self.st1panel.add(self.setapshapebutton)
  self.st1panel.add(JLabel(" c) Convolve AP shape with image data (optional): "))
  self.convolvebutton=JButton("go", actionPerformed=self.guiconvolve)
  self.st1panel.add(self.convolvebutton)
  
  self.st2panel=JPanel()
  self.st2panel.layout=BoxLayout(self.st2panel, BoxLayout.X_AXIS)
  self.st2panel.setBorder(TitledBorder("Process: Smooth input data then generate a map. Step e uses convolution from step c as a binary mask, if present. Set map threshold to 0 if not."))
  self.st2panel.add(JLabel(" d) Filter: t:"))
  self.t_field=JTextField(" 5")
  self.xy_field=JTextField(" 3")
  self.st2panel.add(self.t_field)
  self.st2panel.add(JLabel(" xy:"))
  self.st2panel.add(self.xy_field)
  self.st2panel.add(JLabel("norm"))
  self.normalize_after_kernel_checkbox=JCheckBox()
  self.st2panel.add(self.normalize_after_kernel_checkbox)
  self.processbutton=JButton("go",actionPerformed=self.guiprocesskernel)
  self.st2panel.add(self.processbutton)
  self.st2panel.add(JLabel(" "))
  self.undobutton=JButton("undo",actionPerformed=self.guiundo)
  self.st2panel.add(self.undobutton)
  self.st2panel.add(JLabel("  e) shape thresh. (0-1):"))
  self.cthresh_field=JTextField("0")
  self.st2panel.add(self.cthresh_field)
  self.st2panel.add(JLabel(" median?"))
  self.median_shapethreshold_checkbox=JCheckBox()
  self.st2panel.add(self.median_shapethreshold_checkbox)
  self.st2panel.add(JLabel("cross thresh.(0-1000):")) 
  self.vthresh_field=JTextField("500")
  self.st2panel.add(self.vthresh_field)
  self.generate_map_button=JButton("go",actionPerformed=self.guigeneratethresholdmap)
  self.st2panel.add(self.generate_map_button) 

  self.st3panel=JPanel()
  self.st3panel.layout=BoxLayout(self.st3panel, BoxLayout.X_AXIS)
  self.st3panel.setBorder(TitledBorder("Isochrones: Draw isochrones on selected viewer window."))
  self.st3panel.add(JLabel(" f) iso levels: "))
  self.iso_field=JTextField("5,10,15,20,25,30,35,40,45,50,55,60,65")
  self.st3panel.add(self.iso_field)
  self.st3panel.add(JLabel(" scale: "))
  self.scale_field=JTextField("4")
  self.st3panel.add(self.scale_field)
  self.width_field=JTextField(" 2")
  self.st3panel.add(JLabel(" width: "))
  self.st3panel.add(self.width_field)
  self.st3panel.add(JLabel(" line color: "))
  self.isolinecolor_field=JComboBox(["white","black","grey","color"])
  self.st3panel.add(self.isolinecolor_field)
  self.isofillcolor_field=JComboBox(imagejluts)
  self.st3panel.add(JLabel(" fill: "))
  self.st3panel.add(self.isofillcolor_field)
  self.generateisobutton=JButton("go", actionPerformed=self.guigenerateisos)
  self.st3panel.add(JLabel("  "))
  self.st3panel.add(self.generateisobutton)

  self.st4panel=JPanel()
  self.st4panel.layout=BoxLayout(self.st4panel, BoxLayout.X_AXIS)
  self.st4panel.setBorder(TitledBorder("Save: save as svg."))
  self.st4panel.add(JLabel(" g) filename:  "))
  tmpfilename="%s\\ischcrones.svg"%property_system_get("TempImages dir")
  self.directorytextfield=JTextField(tmpfilename)
  self.st4panel.add(self.directorytextfield)
  self.choosepathbutton=JButton(ImageIcon("C:/CD/programs/java/gvdecoder/images/Open16.gif"))
  self.choosepathbutton.actionPerformed=self.guichoosepath
  self.st4panel.add(self.choosepathbutton)
  self.st4panel.add(JLabel("   "))
  self.savesvgbutton=JButton("save",actionPerformed=self.guisavesvg)
  self.st4panel.add(self.savesvgbutton)

  self.iframe=JInternalFrame("isochrones",1,1,1,1, size=(885,240), visible =1)
  cpane=self.iframe.getContentPane()
  tpane=JPanel()
  tpane.layout=BoxLayout(tpane, BoxLayout.Y_AXIS)
  tpane.add(self.st1panel)
  tpane.add(self.st2panel)
  tpane.add(self.st3panel)
  tpane.add(self.st4panel)
  cpane.layout=BorderLayout()
  self.cpanel=canvas()
  cpane.add(tpane,BorderLayout.NORTH)
  cpane.add(self.cpanel,BorderLayout.CENTER)
  gv.desktop.add(self.iframe)
 
 def guiprocesskernel(self,evt):
   self.ma.update_undo()
   fr=self.ma.vw.frameNumber
   t=int(self.t_field.text)
   xy=int(self.xy_field.text)
   k=self.makekernel(t,xy)
   self.ma*k
   if (self.normalize_after_kernel_checkbox.selected==1):   
     self.ma.processByTrace("n")
   self.ma.vw.JumpToFrame(fr)
 
 def guichoosepath(self,evt): 
   path,file=choosefile()
   self.directorytextfield.text=path+file

 def guisavesvg(self,evt):
   txt=self.directorytextfield.text
   self.savesvg(txt)

 def guiundo(self,evt):
   fr=self.ma.vw.frameNumber
   self.ma.undo()
   self.ma.vw.JumpToFrame(fr)
  
 def guigeneratethresholdmap(self,evt):
   cthr=float(self.cthresh_field.text)
   vthr=int(self.vthresh_field.text)
   if self.zerobackground==1:
     self.wf.m_in*=self.mb[0]
   self.mthr=self.wf.getAPTimesThreshold(cthr,vthr)
   if self.median_shapethreshold_checkbox.selected==1:
     mnew=Matrix(self.mthr) 
     mnew.dat.set(-1)
     floodfill.median(self.mthr,0,mnew)
     self.mthr=mnew
   self.mthr.show(gv,"threshold map")
   str=self.findrange(self.mthr)
   self.iso_field.text=str


 def guigenerateisos(self,evt):
 
  vw=jh.presentviewer
  tmp=self.iso_field.text.split(',')
  self.levels=[float(x) for x in tmp]
  self.scale=float(self.scale_field.text)
  self.width=float(self.width_field.text)
  self.linecolor=self.isolinecolor_field.getSelectedIndex()
  self.fillcolor=self.isofillcolor_field.getSelectedIndex()
  #def addIsosColor(self,mact,levels,color,scale,lw,colorscheme):
  self.addIsosColor(self.mthr,self.levels,self.linecolor,self.scale,self.width,self.fillcolor)
  self.cpanel.ip=self.mthr.vw.jp
  self.cpanel.repaint()

   
  
  #vw.addIsosColor(mthr,[28,29,30,31,32,33,34,35],3,3,2,10)

 def capturesegment(self,vw,st,en):
      self.ma=Matrix()
      self.ma.initialize(vw.im,st,en,0,vw.Y_dim,0,vw.X_dim)
      if (self.flipcheckbox.selected==1):
        self.ma=self.ma*-1.0
      self.wf=gvdecoder.WaveFinder(self.ma)
      self.start_t=st
      self.end_t=en
      self.ma.show(gv,"activationmap input")

 def guiconvolve(self,evt):
      self.wf.convolveAll() 
      self.cthresh_field.text="0.8"

               
 def guicapturenavigator(self,evt):
      if jh.presentviewer.nav==None:
        JOptionPane.showMessageDialog(self.iframe,"A navigator window is needed first (processRois)"  ,"warning", JOptionPane.ERROR_MESSAGE)
        return
      nav=jh.presentviewer.nav
      if nav.x_range>300:
       JOptionPane.showMessageDialog(self.iframe,"Select a relatively short activation sequence (max 300 samples)\n using a navigator. This can be overriden by typing:\n 'act.capturenavitator()' in the console."  ,"warning", JOptionPane.ERROR_MESSAGE)
      else:
       self.capturenavigator()
 
 def capturenavigator(self):
      st=jh.presentviewer.nav.x_offset
      self.ap_start_time=st
      en=jh.presentviewer.nav.x_range+st
      self.capturesegment(jh.presentviewer,st,en)

 def guisetshape_roi(self,evt):
     self.setshape_roi()

 def setshape_roi(self):
      tmp=getroidata()[0]
      if (self.flipcheckbox.selected==1):
       self.wf.setShape((tmp*-1).doubles())
      else:
       self.wf.setShape(tmp.doubles())

 def setshape(self,vals):
     self.wf.setShape(vals.doubles())  

 def addIsos(self,mact,levels,color,scale,lw):
      z=mact.vw.frameNumber
      tmp=mact.dat.get2Darray(z)   
      self.isos=self.marching.mkIsos(tmp,levels)
      self.decs=self.addDecorations(color,scale,lw)
      #mact.vw.jp.decorations=self.decs


 def addDecorations(self,color,scale,lw):
       res=[]
       pc=java.awt.Color.WHITE
       if (color==1):
         pc=java.awt.Color(20,20,20)
       if (color==2):
         pc=java.awt.Color.GRAY
       jh.presentviewer.jp.SetScale(scale)
       xf=java.awt.geom.AffineTransform()
       xf.scale(scale,scale)
       c=0
       cinc=int(250/len(self.isos))
       w.print("%s"%cinc)
       for g in self.isos:
         if (color==3):
          pc=java.awt.Color(100,10+c,255-c)
         c+=cinc
         g.transform(xf)
         decoration=gvdecoder.Decoration(pc,-1,-1,"",g,0)
         decoration.linewidth=lw
         res.append(decoration)
       return res  
 
 def addIsosColor(self,mact,levels,color,scale,lw,colorscheme):
       z=mact.vw.frameNumber
       tmp=mact.dat.get2Darray(z)   
       self.isos=self.marching.mkIsos(tmp,levels)
       rs,gs,bs=self.loadlut(colorscheme)
       self.decs=self.addDecorationsColor(color,scale,lw,rs,gs,bs)
       if self.makebar==1:
        generatebar(self.decs,self.barcolors,levels,scale*mact.xdim)
       mact.vw.jp.decorations=self.decs


 def addDecorationsColor(self,color,scale,lw,rs,gs,bs):
        res=[]
        self.barcolors=[]
        pc=java.awt.Color.WHITE
        fillcolor=java.awt.Color.RED
        if (color==1):
              pc=java.awt.Color(20,20,20)
        if (color==2):
              pc=java.awt.Color.GRAY
        jh.presentviewer.jp.SetScale(scale)
        xf=java.awt.geom.AffineTransform()
        xf.scale(scale,scale)
        c=0
        cinc=int(255/len(self.isos))
        w.print("%s"%cinc)
        for g in self.isos:
              fillcolor=java.awt.Color(rs[c],bs[c],gs[c])
              if (color==3):
                  pc=java.awt.Color(100,10+c,255-c)
              c+=cinc
              g.transform(xf)
              decoration=gvdecoder.Decoration(fillcolor,-1,-1,"",g,1)
              res.append(decoration) 
              decoration=gvdecoder.Decoration(pc,-1,-1,"",g,0)
              if (self.makebar==1):
                self.barcolors.append((pc,fillcolor))
              decoration.linewidth=lw
              res.append(decoration)
        return res  

 

 def loadlut(self,n):
      lutname=imagejluts[n]
      f= open((property_system_get("LUTs dir")+"\\"+lutname),"rb")
      r=[]
      g=[]
      b=[]
      for i in range(256): 
       r.append(struct.unpack("B",f.read(1))[0])
      for i in range(256):
       g.append(struct.unpack("B",f.read(1))[0])
      for i in range(256):  
       b.append(struct.unpack("B",f.read(1))[0])
      return r,b,g
 
 def makekernel(self,zd,xyd):
    k2d=[]
    k3d=[]
    for k in range(xyd):
     k2d.append([0]*xyd)
    cxy=xyd/2
    sum2d=0
    for x in range(0,xyd):
     for y in range(0,xyd):
       xx=x-cxy
       yy=y-cxy
       d=(xx*xx+yy*yy)**0.5
       if d==0:
        d=0.75
       k2d[x][y]=1/d
       sum2d+=1/d
    for x in range(0,xyd):
     for y in range(0,xyd):
       val=k2d[x][y]
       k2d[x][y]=val/(sum2d*zd)
    k3d=[k2d]*zd   
    return k3d 
 
 def savesvg(self,fullpath):
  if self.svgsetup==0:
   self.domimp=GenericDOMImplementation.getDOMImplementation()
   self.document=self.domimp.createDocument("http://www.w3.org/2000/svg","svg",None)
   self.svgGenerator=SVGGraphics2D(self.document)
   self.svgsetup=1
  self.mthr.vw.jp.drawOverlay(self.svgGenerator,self.mthr.vw.jp.decorations)
  stw=java.io.StringWriter()
  self.svgGenerator.stream(stw,1)
  file=open(fullpath,"w")
  file.write(stw.toString())
  file.close()

 def findrange(self,ma):
  minv=java.lang.Double.MAX_VALUE
  maxv=-1
  for i in range(len(ma.dat.arr)):
   v=ma.dat.arr[i]
   if v<minv and v>0:
     minv=v
   if v>maxv:
     maxv=v
  minv=int(minv)
  maxv=int(maxv+1)
  r=maxv-minv
  str="[1,2,3,4,5]"
  if r<15:
    str="%s"%(range(minv,maxv,1))
  elif r>30:
    str="%s"%(range(minv,maxv,5))
  else:
    str="%s"%(range(minv,maxv,2))
  return str[1:len(str)-1]

def generatebar(decorations,colors,levels,offset):
  for i in range(len(levels)):
    str="%s"%levels[i]
    xd=int(offset)+10
    yd=10+i*20
    d=gvdecoder.Decoration(colors[i][1],0,0,None,java.awt.Rectangle(xd,yd,20,20),1)
    decorations.append(d)
    d=gvdecoder.Decoration(colors[i][0],0,0,None,java.awt.Rectangle(xd,yd,20,20),0)
    decorations.append(d)
    d=gvdecoder.Decoration(java.awt.Color.black,xd+22,yd+15,str,10,"Arial",None,0,1)
    decorations.append(d)
    w.print("added dec")
  return decorations
 
# k=self.makekernel(t,xy)

#1) generate one iso using the interface so the arrays are setup.
#2) act.makebar=0
#3) kern=act.makekernel(3,3)
#4) ngen_iso(in_vw,56639+5,100,kern,4,"s56345")


def gen_iso(m_in,kernxy,kernt,median_n,vstart):
    m_in.update_undo()
    act.ma = m_in
    act.wf.m_in=act.ma
    act.start_t=1
    act.end_t=m_in.zdim
    act.ma.vw.JumpToFrame(0)
    kern=act.makekernel(kernt,kernxy)
    act.ma*kern
    floodfill.spatialAverage(act.ma,kernxy,0)
    act.ma.vw.JumpToFrame(0)
    act.wf.m_in=act.ma
    act.wf.m_in.vw.JumpToFrame(0)
    act.mthr=act.wf.getAPTimesThreshold(0,500)
    for i in range(median_n):
     act.mthr.processInSpace()
    m_in.undo()
    act.mthr.show(gv,"threshold map")
    str=makeisolevelstring(vstart)
    act.iso_field.setText(str)
    act.guigenerateisos(None) 
    
def makeisolevelstring(vstart):
  vs_0=vstart-79.5
  isolevels=[44,45,46,47,48,49,50,51,52,53,58,63,68,72,77,79.5,80.5,81.5,82.5,83.5,84.5,85.5]
  str=""
  for i in range(len(isolevels)):
   str+="%.1f"%(isolevels[i]+vs_0)
   if i<(len(isolevels)-1):
    str+=","
  return str
 
def redoisos(vstart):
    str=makeisolevelstring(vstart)
    act.iso_field.setText(str)
    act.guigenerateisos(None) 

 
#steps
#1) from 3D_scan.gv, generate a series of tiffs, upload into Fiji, and export the surface as a ascii stl file
#   (the mesh is generated in fiji and loaded using processing (but flipped!), the data is generated here and saved into a series of files that are read by processing.
#2) raw data for the isochronal map and the 3D reconstruction of actvity comes from four_AP_sum_timev5_bin_4.gv
#   this must be multiplied times a mask file based on 3D_scan.gv and a thresholding algorithm to determine which pixels have
#   good APs or else there won't be a nice overlap between the mesh and the data
#   NOTE: the 7 panels are arranged so that the one closes to the surface is camera 7.
#3) based on the original filename of 3D_scan.gv, " aug_19_scan_025mmpersecond_heart2.dat"
#   the scan through the heart was done at 0.025mm/sec. As the record was acquired at either 50 or 20 Hz (check)
#   and there are 366 frames captured through the depth of the heart (corresponding)
#   to 7.32 or 18.3 seconds, this corresponds to a depth of 0.183 mm 0.4575 mm.
#   Each frame in the 3d scan movie is seprrated by 1.25um, there should be 33 or 34 frames per plane
#   We know the cameras are separated by 250um cube 
#    7---6---5---4---3---2---1 (each --- corresponds to 42 or 50 microns
#   Generate mask either by A) Select frames at beginning and end (separated by 192 or 200 frames) that look like the ones in the video and extract the frames
#                 or     by B) setting up a threshold for atrial and ventricular APs and making that a cut-off (better)
#   





def get3Dindex(xi,yi):
 z=int(xi/100.0)
 x=xi-z*100
 y=yi
 return x,y,z

def getMAindex(xi,yi,zi):
  z=zi*100
  x=z+xi
  return x,yi

#old (jfx)
def colorpixels(ma,jfx,fr):
  for zi in range(7):
   for yi in range(99):
    for xi in range(99):
     mx,my=getMAindex(xi,yi,zi)
     v=ma[fr,my,mx]/2.0
     if v>255:
      v=255
     if v<0:
      v=0
     px=zi*100*100+yi*100+xi
     if px<jfx.forground_objects.size():
      jfx.setColor(px,int(v))

#useful
def loadlut(n):
      lutname=imagejluts[n]
      f= open((property_system_get("LUTs dir")+"\\"+lutname),"rb")
      r=[]
      g=[]
      b=[]
      for i in range(256): 
       r.append(struct.unpack("B",f.read(1))[0])
      for i in range(256):
       g.append(struct.unpack("B",f.read(1))[0])
      for i in range(256):  
       b.append(struct.unpack("B",f.read(1))[0])
      return r,g,b

#should be renamed and rewritten - currently useful only if processing not running from jython
def write_test(framenumber,filenumber,thr,ln):
 rc,gc,bc=loadlut(ln)
 file = open("C:\\data\\figure2\\voltage\\f_%d_l_%d.dat"%(framenumber,filenumber),"w")
 for y in range(100):
  for x in range(100):
   r=0
   g=0
   b=0
   a=0
   h=3
   px=filenumber*100+x
   v=ma.dat.get(framenumber,y,px)
   if v>thr:
    n=int( ((v-thr)/(1000.0-thr))*255)
    r=rc[n]
    b=bc[n]
    g=gc[n]
    a=255
    h=n
    
   file.write("%d %d %d %d %d\n"%(r,g,b,a,h))
 file.close()

#old jfx
def setsize(ma,jfx,fr,thr):
 for zi in range(7):
   for yi in range(99):
    for xi in range(99):
     mx,my=getMAindex(xi,yi,zi)
     w=2
     v=ma[fr,my,mx]
     if v<thr:
      w=0
     if v>=thr:
      w=v/thr
     if w>7:
      w=7
     px=zi*100*100+yi*100+xi
     if px<jfx.forground_objects.size():
       jfx.setSize(px,w,w,w*2)


#old JFX
def makeforground(jfx):
 for zi in range(0,7):
  for yi in range(100):
   for xi in range(100):
    jfx.addforground(2,xi*2,yi*2,zi*54,5)



#look at 3d scan, and find the closest image to the camera layer. 
def getclosestlayer(vw,layernumber, offset, skip):
 vw.JumpToFrame(offset+layernumber*skip)



#generates a mask based on the closest layer.  This isn't ideal as it forces the data to fit inside the mesh, when it should do this by itself. 
def makemask(vw_in,mask,thr):
 for z in range(7):
  getclosestlayer(vw_in,z,15,48)
  for y in range(100):
   for x in range(100):
    xi=z*100+x
    v=vw_in.datArray[(y*4)*400+(x*4)]
    if v>thr:
     mask.dat.set(0,y,xi,255)

    
#old
def copytojfx(v_in,jfx,thr):
 nz=0
 for nz in range(0,366,6):
   a=0.1
   ww=1
   w.print(nz)
   v_in.JumpToFrame(nz)
   for y in range(100):
    for x in range(100):
     sx=int(x*4)
     sy=int(y*4)
     v=0
     for ii in range(-2,3):
      for jj in range(-2,3):
       ny=sy+ii
       nx=sx+jj
       if ny>=0 and ny<400 and nx>=0 and nx<400:   
        t=v_in.datArray[(sy+ii)*400+(sx+jj)]
        if t>thr:
         v+=t
     if (v/25.0>thr):
      jfx.addbox(ww,x,y,nz*2,1,0,1,a)
      
#old
def moveforground(jfx):
 i=0
 for zi in range(0,7):
  for yi in range(100):
   for xi in range(100):
    jfx.setLocation(i,xi*2,yi*2,zi*100)
    i+=1

#old   
def setpixs(ma,jfx,fr,thr):
  colorpixels(ma,jfx,fr)
  setsize(ma,jfx,fr,thr)

#old
def makemovie(st,en):
 for fr in range(st,en):
  setpixs(ma,jfx,fr,60)
  java.lang.Thread.sleep(2800)
  imgname="C:/data/batscope/3df/image%d.png"%fr
  jfx.saveimage(imgname)
  java.lang.Thread.sleep(200)




#ro=robot()
#mr=rect(30,30,1100,1100)
#old (pre processing)
def saveimage(ro,mr,filename):
 bi=ro.createScreenCapture(mr)
 fi=java.io.File(filename)
 javax.imageio.ImageIO.write(bi,"png",fi)
 
 #img=jfx.scene.snapshot(None)
 #file = java.io.File("threedee.png");
 #ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

# for calibration - make shapes to export to Fiji 3d viewer to make a surface
def makecuble(x1,x2,y1,y2,z1,z2):
 for z in range(z1,z2):
  for y in range(y1,y2):
   for x in range(x1,x2):
    ma.dat.set(z,y,x,250)

#attemtp to look at data to exclude traces that aren't good before running a normalizing function (which would boost noise)
def isAP(ma,y,x,dmin,mo,mramp):
 cv=[0.1]*10
 ts=(gvdecoder.TimeSeries(ma[y,x].get()))*cv
 li=ts.minima(10,190)
 hi=ts.maxima(10,190)
 if (li>0) and (hi>0):
  lv=ts[li]
  hv=ts[hi]
  amp=(hv-lv) * mramp.dat.get(0,y,x)
  if (li>45) and (li<80):
   if (amp*2>dmin):
     mo.dat.set(0,y,x,li)
  if (li>79) and (li<130):
   if (amp>dmin):
     mo.dat.set(0,y,x,li)

#as above => calls isAP
def find_good_pixels(ma,mok,thr,mramp):
  mok.dat.set(0)
  for y in range(ma.ydim):
   w.print(y)
   for x in range(ma.xdim):
    isAP(ma,y,x,thr,mok,mramp)

#generate a ramp to multiply the data by so that the pixels far from the laser are boosted
def makeramp(mramp):
 for c in range(7):
  for y in range(100):
   for x in range(100):
    xi=x+c*100
    v=100-x+300
    mramp.dat.set(0,y,xi,v)

def makesingleframeramp():
 ma=Matrix(1,400,400)
 for y in range(400):
  for x in range(400):
   v=400-x+300
   ma.dat.set(0,y,x,v)
 return ma

#setup the processing environment 3D Viewer
def processing():
 pr=gvdecoder.Processing_Fig_2()
 globals()["pr"]=pr
 papplet.runSketch(["Procesing_Fig_2"],pr)


def reduce(m_in,m_out):
 for z in range(100):
  w.print(z)
  for x in range(100):
   for y in range(100):
    zi=int(z*3.66)
    xi=x*4
    yi=y*4
    s=0
    for zz in range(3):
     for yy in range(3):
      for xx in range(3):
       s+=m_in[zi+zz,yi+yy,xi+xx]
    m_out.dat.set(z,y,x,s)
   
def i_vs_t(ma, thr):
 res=[]
 for z in range(ma.zdim):
   s=0
   c=0
   for y in range(ma.ydim):
    for x in range(ma.xdim):
      v=ma.dat.get(z,y,x)
      if v>thr: 
       s+=v
       c+=1
   if (c>0):
    res.append(s/c)
 return res   

def make_all_under_255(ma):
 for z in range(ma.zdim):
  for y in range(ma.ydim):
   for x in range(ma.xdim):
    v=ma.dat.get(z,y,x)
    if v>255:
     ma.dat.set(z,y,x,255)

def voltage_to_processing(ma,framenumber,camera,thr,ln,prc):
 rc,gc,bc=loadlut(ln)
 #file = open("C:\\data\\figure2\\voltage\\f_%d_l_%d.dat"%(framenumber,filenumber),"w")
 count=0
 for y in range(100):
  for x in range(100):
   idata=prc.layerdata[camera][count]
   count+=1
   idata.r=0
   idata.g=0
   idata.b=0
   idata.a=0
   idata.h=3
   px=(6-camera)*100+x
   v=ma.dat.get(framenumber,y,px)
   if v>thr:
    n=int( ((v-thr)/(1000.0-thr))*255)
    idata.r=rc[n]
    idata.b=bc[n]
    idata.g=gc[n]
    idata.a=255
    idata.h=n

def showframe(fr,thr):   
  [voltage_to_processing(mvoltage,fr,i,thr,25,pr) for i in range(7)]
  pr.jrotate(0,0,0)
  java.lang.Thread.sleep(500)


#zoffset
def setplane_dist(pr):
  zscreen=-(-13.6-495.6) #max-min z for green bounding box and mesh
  tot_z = ((366*0.05)*0.025)*1000.0 #total number of microns for z stack based on thorlabs 0.025mm/sec
  microns_between_planes = 250.0/6.0 #in microns, assuming first and last planes bound imaging cube
  screen_dbp = microns_between_planes*(zscreen/tot_z)
  pr.zmult=int(round(screen_dbp))
  pr.zoffset=160
  return zscreen, tot_z, screen_dbp

def reload_mesh(pr):
 pr.stlpath="D://data//sacconiscope//figure2//fig_2_adjusted_sm.stl";
 pr.mesh.clear()
 pr.openfile()
 pr.makemesh(5,10,0,0,0)
 pr.reader.close()
 pr.jrotate(0,0,0)
 
def savefront():
 cstate=pr.cam.getState()
 globals()["camfront"]=cstate

# position the camera center so that it can rotate w/o clipping, then type savefront()
def zerocam():
 pr.cam.setState(camfront)
 java.lang.Thread.sleep(500) #not sure why this is needed
 pr.jrotate(0,0,0)

def p45():
 pr.cam.setState(camfront)
 java.lang.Thread.sleep(500) #not sure why this is needed
 pr.jrotate(0,45,0)

def m45():
 pr.cam.setState(camfront)
 java.lang.Thread.sleep(500) #not sure why this is needed
 pr.jrotate(0,-45,0)

def test(ma,c):
 ma.dat.set(0)
 for x in range(100):
  for y in range(100):
    cx=c*100+x
    ma.dat.set(0,y,cx,250)

def signaltonoise(ma,y,x,w):
 tr=gvdecoder.TimeSeries(ma[y,x].presenttrace)
 tr.findRange()
 delta_all=tr.maxvalue-tr.minvalue
 if delta_all==0:
  return -1
 deltas=[]
 for t in range(len(tr)-w):
   tr.findRange(t,t+w)
   deltas.append((1.0*(tr.maxvalue-tr.minvalue))/delta_all)
 c=0
 for i in range(len(deltas)):
   if deltas[i]>0.3:
    c+=1
 return c

def makenoisemask(ma):
  mres=Matrix(1,ma.ydim,ma.xdim)
  for y in range(ma.ydim):
   w.print(y)
   for x in range(ma.xdim):
     v=signaltonoise(ma,y,x,5)
     mres.dat.set(0,y,x,v)
  return mres 

def makebinarymask(noisemask, thr):
  bm=Matrix(1,noisemask.ydim, noisemask.xdim)
  for y in range(noisemask.ydim):
   for x in range(noisemask.xdim):
    v=noisemask.dat.get(0,y,x)
    if (v>0) and (v<thr):
      bm.dat.set(0,y,x,1)
  return bm

def addtrace(pl,xs,ys,i,col):
 pl.add(xs,ys) 
 pl[i].color=col

def makeplot(undecs,decs):
 #undeconvolved are longer as the deconvolved data starts 144 samples after the undeconvolved ones
 #trim undeconvolved to take the last 812 samples 
 ud0=undecs[0][145:957]*-1
 ud0.scale(0,1000)
 ud1=undecs[1][145:957]*-1
 ud1.scale(0,1000)
 ud2=undecs[2][145:957]*-1
 ud2.scale(0,1000)
 d0=decs[0]*-1
 d0.scale(-1200,-200)
 d1=decs[1]*-1
 d1.scale(-1200,-200)
 d2=decs[2]*-1
 d2.scale(-1200,-200)
 xs=range(0,812*2,2)
 pl=gvdecoder.Plot(xs,ud0.arr)
 pl[0].color=(255,0,0)
 pl.add(xs,ud1.arr)
 pl[1].color=(0,220,0)
 pl.add(xs,ud2.arr)
 pl[2].color=(0,0,255)
 globals()["pl"]=pl
 pl.add(xs,d0.arr)
 pl[3].color=(255,0,0)
 pl.add(xs,d1.arr)
 pl[4].color=(0,220,0)
 pl.add(xs,d2.arr)
 pl[5].color=(0,0,255)
 rmd=running_median(d0,11)
 pl.add(xs,rmd)
 rmu=running_median(ud0,11)
 pl.add(xs,rmu)
 for i in range(6):
  pl[i].edgewidth=3

def running_median(_tr,ws):
 tr=_tr.arr
 res=[]
 for j in range(0,ws):
   res.append(tr[j])
 for i in range(ws,len(tr)-ws):
   res.append(median(tr[i-ws:i+ws]))
 for k in range(len(tr)-ws, len(tr)):
   res.append(tr[k])
 return res


def makeblended(fr,path,file):
 pr.drawthemesh=1
 showframe(fr,250)
 wm=pr.deepCopy(pr.bi)
 pr.drawthemesh=0
 showframe(fr,250)
 nm=pr.deepCopy(pr.bi)
 bm=pr.blend_images(nm,wm,0.5)
 pr.writeBufferedImage(bm,path+file,"png")
 

def copy_to_matrix(vi_in, ma_out, camnumber, zstart, zend, shelper):
 for z in range(zstart,zend):
   vi_in.JumpToFrame(z)
   shelper.copy_camera_to_matrix(vi_in,mdest,z,camnumber,ma_out.xdim,ma_out.ydim) 
 

def makekernel(zd,xyd):
    k2d=[]
    k3d=[]
    for k in range(xyd):
     k2d.append([0]*xyd)
    cxy=xyd/2
    sum2d=0
    for x in range(0,xyd):
     for y in range(0,xyd):
       xx=x-cxy
       yy=y-cxy
       d=(xx*xx+yy*yy)**0.5
       if d==0:
        d=0.75
       k2d[x][y]=1/d
       sum2d+=1/d
    for x in range(0,xyd):
     for y in range(0,xyd):
       val=k2d[x][y]
       k2d[x][y]=val/(sum2d*zd)
    k3d=[k2d]*zd   
    return k3d  
 
def genIsoPanel(n):
 path="D:\\data\\sacconiscope\\figure2\\data\\"
 filename="h2_288_cam_%d.gv"%n
 gv.openImageFile(path+filename,"gv")
 w.print(path+filename)
 vw=jh.presentviewer
 w.print("to matrix")
 vw.toMatrix()
 mx=vw.ma
 mx.vw=vw
 w.print("ramp")
 mr=makesingleframeramp()
 mx*mr
 mask=mx[0]
 mask>1200
 mx*mask
 w.print("median filter")
 mx.processByTrace("m",7)
 mx.processInSpace()
 mx-mx[0]
 mx.vw.findScaleOffset()
 mx.vw.binout(2)
 vn=jh.presentviewer
 w.print("saving")
 newfilename="h2_288_cam_%d_processed_new.gv"%n
 gv.writeImageFile(vn.im, path+newfilename)
 #vn.SaveImageFile(path+newfilename)
 w.print("saved")



def genIsoPanel2(n,step):
 camnumber=n
 a_p=globals()["a_p"]
 path=a_p["path"] 
 filename="h2_288_cam_%d.gv"%n
 if (step==1) or (step==0):
  w.print(path+filename)
  gv.openImageFile(path+filename,"gv")
  w.print(path+filename)
  vw=jh.presentviewer
 if (step==1) or (step==0):
  w.print("to matrix")
  mx=Matrix()
  mx.initialize(vw.im,440,540,0,400,0,400,2)
  mx.show(gv,"mx")
  globals()["mx"]=mx
  mx.vw=jh.presentviewer
  #vw=jh.presentviewer
 if (step==2) or (step==0):
  mx=globals()["mx"] 
  a_p=globals()["a_p"]
  w.print("ramp")
  mr=Matrix(1,200,200)
  rc=a_p["ramp_constant"]
  for y in range(200):
   for x in range(200):
    v=(200-x+rc)/rc
    mr.dat.set(0,y,x,v)  
  mx*mr
  mx.vw.findScaleOffset()
  mx.vw.JumpToFrame(0)
  globals()["ramp"]=mr
 if (step==3) or (step==0):
  mx=globals()["mx"]
  a_p=globals()["a_p"]
  w.print("mask")
  mask=mx.getFrameAverage()
  threshold=0
  if (a_p["mask_threshold"])<0: 
   threshold=mask.dat.sumsection(0,1,40,90,100,150)/(50*50)
   tmp="threshold = %f"%threshold
   w.print(tmp)
  else:
   threshold=a_p["mask_threshold"]
  mask>threshold*0.15
  floodfill.dilate(mask, 0, 1, 10)
  floodfill.erode(mask, 0, 1, 10)
  floodfill.erode(mask, 0, 1, 10)
  globals()["mask"]=mask
  mask.show(gv,"mask")
  mx*mask
  mx.vw.findScaleOffset()
  mx.vw.JumpToFrame(0)
 if (step==4) or (step==0):
  mx=globals()["mx"]
  w.print("median filter")
  mx.processByTrace("m",7)
  mx.processInSpace()
  mx-mx[0:10]
  mx.vw.findScaleOffset()
  mx.vw.JumpToFrame(0)
 if (step==5) or (step==0):
  mx=globals()["mx"]
  a_p=globals()["a_p"]
  path=a_p["path"]
  w.print("saving")
  newfilename="h2_288_cam_%d_processed_new.gv"%camnumber
  mx.SaveImageFile(path+newfilename)
  w.print("saved")
 if (step==6) or (step==0):
  w.print("normalize, invert")
  mx=globals()["mx"]
  mx*-1
  mx.processByTrace("n")
  mx.vw.findScaleOffset()
  mx.vw.JumpToFrame(0)
 if (step==7) or (step==0):
  w.print("setup activation map")
  if 'act' in globals()==0:
   act=activationmap()
   globals()["act"]=act
  act=globals()["act"]
  mx=globals()["mx"]
  act.ma=mx
  if hasattr(act,'wf')==0:
   act.wf=gvdecoder.WaveFinder(mx)
  else:
   act.wf.m_in=mx
 if (step==8) or (step==0):
  w.print("gen AP mask")
  mx=globals()["mx"]
  a_p=globals()["a_p"]
  mask=globals()["mask"]
  make_masks_with_AP_shape(mx,mask,a_p["ap_check_width"],a_p["ap_check_mu"])
  mask.vw.JumpToFrame(0)
 if (step==9) or (step==0):
  w.print("gen activation map")
  mx=globals()["mx"]
  act=globals()["act"]
  a_p=globals()["a_p"]
  
  if ('prefilter' in globals())==0:
    w.print("initializing prefilter")
    prefilter=Matrix(mx)
    globals()["prefilter"]=prefilter
  prefilter=globals()["prefilter"]
  prefilter.copy(mx)
  #kern=act.makekernel(a_p["kern_xy"],a_p["kern_t"])
  #mx*kern
  if a_p["kern_t"]>1:
   w.print("temporal averaging")
   mx.processByTrace("a",a_p["kern_t"])
  if a_p["temporal_median_frames"]>1:
   w.print("temporal median")
   mx.processByTrace("m",a_p["temporal_median_frames"])
  if a_p["spatial_average_width"]>1:
   w.print("spatial averaging")  
   floodfill.spatialAverage(act.ma,a_p["spatial_average_width"],0)
  w.print("trying to jump to frame 0")
  mx.vw.JumpToFrame(0)
  w.print("making threshold map")
  act.mthr=act.wf.getAPTimesThreshold(0,a_p["iso_threshold"])
  tmp="threshold map %d"%camnumber
  act.mthr*globals()["mask"]
  act.mthr.show(gv,tmp)
  for i in range(a_p["number_of_spatial_medians"]):
   w.print("spatial median of threshold map")
   act.mthr.processInSpace()
 
  act.mthr.vw.JumpToFrame(0)
  newfilename="h2_288_cam_%d_activationmap.gv"%camnumber
  act.mthr.SaveImageFile(a_p["path"]+newfilename)
  w.print("saved activation map")
  
 if (step==10) or (step==0):
  w.print("gen iso map")
  mx=globals()["mx"]
  act=globals()["act"]
  a_p=globals()["a_p"]
  act.makebar=a_p["make_bar"]
  str=makeisolevelstring(a_p["ap_start_time"])
  act.iso_field.setText(str)
  act.guigenerateisos(None) 

def iso(camnumber,loadlevels,postfix):
  act=globals()["act"]
  a_p=globals()["a_p"]
  newfilename="h2_288_cam_%d_activationmap.gv"%camnumber
  act.mthr.OpenImageFile(a_p["path"]+newfilename)
  act.makebar=a_p["make_bar"]
  if (loadlevels==1):
   str=makeisolevelstring(a_p["ap_start_time"])
   act.iso_field.setText(str)
  if camnumber==6:
   act.makebar=1
  act.guigenerateisos(None) 
  
  actname="C:\\Users\\Gil\\Documents\\GViewFiles\\c%d%s.svg"%(camnumber,postfix)
  act.savesvg(actname)
  

def dvdt(ma,x,y,mo):
 ts=(gvdecoder.TimeSeries(ma[y,x].get()))
 dmax=-1
 ti=-1
 for i in range(5,len(ts)-5):
   d1=ts[i]-ts[i+1]
   d5=ts[i-3]-ts[i+3]
   if (d5>d1) and d5>dmax:
    dmax=d5
    ti=i
 mo.dat.set(0,y,x,ti) 

  
 
def hasAPshape(ma,x,y,w,mu):
  res=floodfill.APShape(ma,x,y,6,w)
  globals()["lastAPshape"]=res
  hi=max(res)
  lo=min(res)
  vAP=0
  aAP=0
  nAP=0
  if hi==lo:
    return 0,0,0
  if hi==0:
    return 0,0,0
  if (hi-lo)/hi<0.25:
   return 0,0,0
  #res 5 and 4 similar, significantly higher than res 1 and 2
  if (java.lang.Math.abs(res[5]-res[4])<100) and (res[5]>res[0]*mu) and (res[5]>res[1]*mu) and (res[5]>res[2]*mu):
    vAP=1
  #
  if (res[2]>res[0]*mu) and (res[2]>res[5]*mu) and (res[2]>res[4]*mu) and (res[1]<res[2]):
    aAP=1
  if (res[3]>res[0]*(mu*0.9)) and (res[3]>res[2]) and (res[3]>res[4]) and (res[4]>res[5]) and (res[3]>res[1]):
    nAP=1
  #if (res[4]>res[0]*(mu*0.9)) and (res[4]>res[2]) and (res[4]>res[3]) and (res[4]>res[5]) and (res[3]>res[1]):
  #  nAP=2

  return aAP,vAP,nAP


def hasPeak(ma,x,y):
 res=floodfill.APShape(ma,x,y,8,3)
 globals()["lastAPshape"]=res
 hi=max(res)
 lo=min(res)
 i=-1
 for t in range(8):
  if res[t]==hi:
   i=t
 if i<4 or i>=6: 
  return i,0
 pa=(res[i]+res[i-1]+res[i+1])/3.0
 pl=(res[i-2]+res[i+2])/2.0
 return i,(pa/pl)

def make_masks_with_peak(ma,mok):
  mok.dat.set(0)
  for y in range(ma.ydim):
   for x in range(ma.xdim):
     p,pm=hasPeak(mx,x,y)
     if (p>0) and (pm>1.05):
       mok.dat.set(0,y,x,1)
  floodfill.dilate(mok,0,1,10)
  floodfill.dilate(mok,0,1,10)
  floodfill.erode(mok,0,1,10)

def makePeakMatrix(m_in,m_out,bins,width):
 for y in range(m_in.ydim):
  for x in range(m_in.xdim):
   res=floodfill.APShape(m_in,x,y,bins,width)
   for z in range(m_out.zdim):
    m_out.dat.set(z,y,x,res[z])
 
def Ptimes(m_in,tolerance):
 m_out=Matrix(1,m_in.ydim,m_in.xdim)
 for y in range(m_out.ydim):
  for x in range(m_out.xdim):
    tmp=m_in.dat.section(y,x)
    p=max(tmp)
    pi=-1
    for i in range(len(tmp)):
     if tmp[i]==p:
      pi=i
    #res=isMonotonicPeak(tmp,pi,tolerance)
    m_out.dat.set(0,y,x,pi)
 return m_out      

#as above => calls isAP
def make_masks_with_AP_shape(ma,mok,w,mu):
  mok.dat.set(0)
  for y in range(ma.ydim):
   for x in range(ma.xdim):
     vAP,aAP,nAP=hasAPshape(mx,x,y,w,mu)
     if (vAP or aAP or nAP):
       mok.dat.set(0,y,x,1)
  floodfill.dilate(mok,0,1,10)
  floodfill.dilate(mok,0,1,10)
  floodfill.erode(mok,0,1,10)

def make_masks_test(ma,mok,w,mu):
  mok.dat.set(0)
  for y in range(ma.ydim):
   for x in range(ma.xdim):
     aAP,vAP,nAP=hasAPshape(mx,x,y,w,mu)
     if vAP:
       mok.dat.set(0,y,x,2)
     if aAP:
       mok.dat.set(0,y,x,1)
     if nAP and vAP:
       mok.dat.set(0,y,x,4)
     if nAP==1 and (vAP==0):
       mok.dat.set(0,y,x,3)
     if nAP==2 and (vAP==0):
       mok.dat.set(0,y,x,5)


  
a_p = {
  "ap_start_time": 54.5,
  "number_of_spatial_medians": 4,
  "temporal_median_frames": -7,
  "spatial_average_width": 9,
  "kern_xy": 3,
  "kern_t" :7,

  "mask_threshold" : 150,
  "iso_threshold" : 600,
 
  "ap_check_width" : 4,
  "ap_check_mu"    : 1.2,
  "ramp_constant"  : 75.0,
  "ap_start_time"  : 55.5,
  "make_bar"       : 0,

  "path"           : "D:\\data\\sacconiscope\\figure2\\data\\"
}    
    

#m0=Matrix(354,400,400)
#mb=Matrix(m0.zdim,100,100)
def preprocess(camnum):
 m0=globals()["m0"]
 mb=globals()["mb"]
 name="D:\\data\\sacconiscope\\figure3\\data\\h15_100_deconv_cam_%d.gv"%camnum
 m0.initialize(name,"gv")
 mb.initialize(m0.im,0,m0.zdim,0,400,0,400,4)
 m0.vw.JumpToFrame(0)
 mb.vw.JumpToFrame(0)
 

def sub1(framenum):
 mb=globals()["mb"]
 mb=mb*-1
 mb.processByTrace("m",5)
 mb.processInSpace()
 mb=mb-mb[framenum:framenum+5]
 mb.processByTrace("n")
 mb=mb-mb[framenum:framenum+5]
 mb.vw.JumpToFrame(framenum-5)
 


def genAverage(m_in,m_out,times):
 for z in range(m_out.zdim):
  for y in range(m_out.ydim):
   for x in range(m_out.xdim):
    s=0
    for t in range(len(times)):
     s+=m_in.dat.get(z+(times[t]-20),y,x)
    m_out.dat.set(z,y,x,s)

def isMonotonicPeak(ar,pi,tolerance):
 mi=1
 tval=ar[pi]*tolerance
 for i in range(1,pi+1):
   d=ar[i]-ar[i-1]
   if d<0:
     mi=0
     if d>-tval:
      mi=1
 for i in range(pi,len(ar)-1):
   d=ar[i]-ar[i+1]
   if d<0:
     mi=0
     if d>-tval:
      mi=1
 return mi

def localSD(m_in,w):
 m_out=Matrix(m_in)
 m_out.dat.set(25)
 for y in range(m_in.ydim):
  for x in range(m_in.xdim):
   res=[]
   if m_in.dat.get(0,y,x)!=7:
    for yy in range (y-w,y+w+1):
     for xx in range (x-w,x+w+1):  
      if (xx>=0) and (xx<m_in.xdim) and (yy>=0) and (yy<m_in.ydim):
        res.append(m_in.dat.get(0,yy,xx))
    m_out.dat.set(0,y,x,stdev(res)*10)
 return m_out  

def fix_zeros(m_in):
 mi=min(m_in.dat.arr)
 ma=max(m_in.dat.arr)
 for z in range(m_in.zdim):
  for y in range(m_in.ydim):
   for x in range(m_in.xdim):
    if m_in.dat.get(z,y,x)==0:
     m_in.dat.set(z,y,x, mi-20)

def clean_within_range(m_in,lowv,highv):
 for y in range(m_in.ydim):
  for x in range(m_in.xdim):
    v=m_in.dat.get(0,y,x)
    if v<lowv or v>highv:
      m_in.dat.set(0,y,x,0)

def saveplotdata(filename,plot,numberoftraces):
  fout = open(filename,"w")
  zd=len(plot[0].y.arr)  
  mytdata=[]  
  for i in range(numberoftraces):
    mydata.append(plot[i].y.arr)
  for t in range(zd):
   for i in range(numberoftraces-1):
    fout.write("%f,"%mydata[i][t])
   fout.write("%f\n"%mydata[numberoftraces-1][t])
  fout.close()

def merge(m_in,v1,v2):
 for z in range(m_in.zdim):
  v1.JumpToFrame(z)
  v2.JumpToFrame(z)
  for y in range(m_in.ydim):
   for x in range(m_in.xdim):

     s1=v1.datArray[y*m_in.xdim+x]
     s2=v2.datArray[y*m_in.xdim+x]
     m_in.dat.set(z,y,x,s1+s2)


def make_average(m_in,tb,t1,t2,w):
  for z in range(tb-w,tb+w):
   for y in range(m_in.ydim):
    for x in range(m_in.xdim):
     v1=m_in.dat.get(z,y,x)
     v2=m_in.dat.get(z+(t1-tb),y,x)
     v3=m_in.dat.get(z+(t2-tb),y,x)
     m_in.dat.set(z,y,x,(v1+v2+v3)/3.0)

def find_bad_aps(m_in,tb,te,th):
 mb=Matrix(m_in[0])
 mb.dat.set(0)
 for z in range(tb,te):
  for y in range(m_in.ydim):
   for x in range(m_in.xdim):
    if m_in.dat.get(z,y,x)>th:
     mb.dat.set(0,y,x,1)
 return mb
 
def  ramp(rc):
 mr=Matrix(1,200,200)
 for y in range(200):
  for x in range(200):
    v=(200-x+rc)/rc
    mr.dat.set(0,y,x,v)
 return mr   

def copyinto(mtarget,m1,m2):
  mtarget.dat.set(0)
  for z in range(mtarget.zdim):
    for y in range(m1.ydim):
     for x in range(m1.xdim):
       mtarget.dat.set(z,y,x,m1.dat.get(z+5,y,x))
  for z in range(mtarget.zdim):
    for y in range(m2.ydim):
     for x in range(m2.xdim):
       mtarget.dat.set(z,y,x+220,m2.dat.get(z,y,x))
 
def zap(m_in,ru):
  m_in.update_undo()
  for i in range(100):
   fr=i/100.0
   pt=ru.pointOnRuler(fr)
   for z in range(m_in.zdim):
    m_in.dat.set(z,pt.y,pt.x,0)

def tshift(m_in,m_sub,ts):
 for z in range(ts):
  for y in range(m_in.ydim):
    for x in range(m_sub.xdim):
       m_in.dat.set(z,y,x+220,0)
 for z in range(ts,32):
  for y in range(m_in.ydim):
    for x in range(m_sub.xdim):
       m_in.dat.set(z,y,x+220,msub.dat.get(z-ts,y,x))
  
 
 
  