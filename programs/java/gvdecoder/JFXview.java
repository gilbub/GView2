package gvdecoder;


import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javafx.scene.transform.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.*;
import javafx.scene.image.*;
import javafx.embed.swing.SwingFXUtils;

public class JFXview {

public Group  root;
public Scene  scene;
public PointLight light;
public PerspectiveCamera camera;
public int last_x;
public int last_y;
public int last_z;
public double last_r;
public double last_g;
public double last_b;
public double last_a;
public double last_d;
public int last_w;
public Translate pivot;
public Rotate yRotate,xRotate,zRotate;
public PhongMaterial structurecolor;
public PhongMaterial lut[];
public ArrayList<Box> forground_objects=new ArrayList<Box>();

public void init() {
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("jfx");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(600, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pivot = new Translate();
        yRotate = new Rotate(0, Rotate.Y_AXIS);
        xRotate = new Rotate(0, Rotate.X_AXIS);
        zRotate = new Rotate(0, Rotate.Z_AXIS);

        structurecolor=new PhongMaterial();
        Color c = new Color(0.5,0.5,0.5,0.2);
        structurecolor.setDiffuseColor(c);

        lut =new PhongMaterial[256];
        for (int i=0;i<256; i++){
			PhongMaterial m=new PhongMaterial();
			 Color d = new Color(i/256.0,1.0-i/256.0,0.3+i/512.0,1.0);
             m.setDiffuseColor(d);
             lut[i]=m;
		}

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
       });
    }

    private void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }
    int last_lut;
    public void addforground(int w, int x, int y, int z, int lutvalue){
		last_x=x;
		last_y=y;
        last_z=z;
        last_w=w;
        last_lut=lutvalue;
        Platform.runLater(new Runnable(){ public void run(){_addforground(w,x,y,z,lutvalue);}});
	}
	public void removeallforground(){
		Platform.runLater(new Runnable(){ public void run(){_removeallforground();}});
    }
    public void _removeallforground(){
	 for (int i = 0; i < forground_objects.size(); i++) {
		root.getChildren().remove(forground_objects.get(i));
    }
    forground_objects.clear();
	}

    public void addstructure(int w, int x, int y, int z){
			last_x=x;
			last_y=y;
	        last_z=z;
	        last_w=w;
	        Platform.runLater(new Runnable(){ public void run(){_addstructure(w,x,y,z);}});
	}

    public void setColor(int index, int lutval){
		Platform.runLater(new Runnable(){
			public void run(){
				Box bx=forground_objects.get(index);
				bx.setMaterial(lut[lutval]);
			}
		});
	}
	public void setSize(int index, double w, double h, double d){
		Platform.runLater(new Runnable(){
					public void run(){
						Box bx=forground_objects.get(index);
						bx.setWidth(w);
						bx.setHeight(h);
						bx.setDepth(d);
					}
				});
	}
	public void setLocation(int index, double x, double y, double z){
			Platform.runLater(new Runnable(){
						public void run(){
							Box bx=forground_objects.get(index);
						    bx.setTranslateX(x);
							bx.setTranslateY(y);
		                    bx.setTranslateZ(z);
						}
					});
	}




    public void _addforground(int w, int x, int y, int z, int li){
		Box bx = new Box(w, w, w);
		bx.setTranslateX(x);
		bx.setTranslateY(y);
		bx.setTranslateZ(z);
		bx.setMaterial(lut[li]);
		forground_objects.add(bx);
		root.getChildren().add(bx);
	}
	public void _addstructure(int w, int x, int y, int z){
			Box bx = new Box(w, w, w);
			bx.setTranslateX(x);
			bx.setTranslateY(y);
			bx.setTranslateZ(z);
			bx.setMaterial(structurecolor);
			root.getChildren().add(bx);
	}

    public void addbox(int w, int x, int y, int z, double r, double g, double b, double a){
     last_x=x;
     last_y=y;
     last_z=z;
     last_r=r;
     last_b=b;
     last_g=g;
     last_a=a;
     last_w=w;
     Platform.runLater(new Runnable(){ public void run(){_addBox(w,x,y,z,r,g,b,a);}});
    }

    public void forgroundalpha(double alpha){
		last_a=alpha;
		Platform.runLater(new Runnable(){ public void run(){_forgroundalpha();}});
	}
	public void structurealpha(double alpha){
		last_a=alpha;
	    Platform.runLater(new Runnable(){ public void run(){_structurealpha();}});
	}
	public void _forgroundalpha(){
	 for (int i=0;i<256;i++){
	   Color c=lut[i].getDiffuseColor();
	   last_r=c.getRed();
	   last_b=c.getBlue();
	   last_g=c.getGreen();
	   lut[i].setDiffuseColor(new Color(last_r,last_g,last_b,last_a));
	 }
	}
	public void _structurealpha(){
	  structurecolor.setDiffuseColor(new Color(0.5,0.5,0.5,last_a));
	}

    public void movelight(int x,int y,int z){
     last_x=x;
     last_y=y;
     last_z=z;
     Platform.runLater(new Runnable(){ public void run(){_movelight(x,y,z);}});
   }
   public void _movelight(int last_x, int last_y, int last_z){
	  light.setTranslateX(last_x);
	  light.setTranslateY(last_y);
      light.setTranslateZ(last_z);
   }

   public void rotatecamera_y(double d){
	   last_d=d;
	   Platform.runLater(new Runnable(){ public void run(){_rotatetecamera_y();}});

   }
   public void rotatecamera_z(double d){
   	   last_d=d;
   	   Platform.runLater(new Runnable(){ public void run(){_rotatetecamera_z();}});

   }
   public void rotatecamera_x(double d){
   	   last_d=d;
   	   Platform.runLater(new Runnable(){ public void run(){_rotatetecamera_x();}});

   }
   public void _rotatetecamera_y(){
	   camera.getTransforms().addAll (
	                   pivot,
	                   yRotate,
	                   new Rotate(last_d, Rotate.Y_AXIS),
	                   new Translate(0, 0, 0)
        );
   }
   public void _rotatetecamera_x(){
   	   camera.getTransforms().addAll (
   	                   pivot,
   	                   xRotate,
   	                   new Rotate(last_d, Rotate.X_AXIS),
   	                   new Translate(0, 0, 0)
           );
   }
   public void _rotatetecamera_z(){
   	   camera.getTransforms().addAll (
   	                   pivot,
   	                   zRotate,
   	                   new Rotate(last_d, Rotate.Z_AXIS),
   	                   new Translate(0, 0, 0)
           );
   }
   public void movecamera(int x, int y, int z){
        last_x=x;
        last_y=y;
        last_z=z;
        Platform.runLater(new Runnable(){ public void run(){_movecamera();}});
      }
  public void _movecamera(){
   	  camera.setTranslateX(last_x);
   	  camera.setTranslateY(last_y);
      camera.setTranslateZ(last_z);
   }

  public void _addBox(double last_w, double last_x, double last_y, double last_z, double last_r, double last_g, double last_b, double last_a){
      Box bx = new Box(last_w, last_w, last_w);
      bx.setTranslateX(last_x);
      bx.setTranslateY(last_y);
      bx.setTranslateZ(last_z);
      PhongMaterial m = new PhongMaterial();
      Color c = new Color(last_r,last_g,last_b,last_a);
      m.setDiffuseColor(c);
      bx.setMaterial(m);
      root.getChildren().add(bx);
   }



    public void clear(){
	Platform.runLater(new Runnable(){ public void run(){
		root.getChildren().clear();
		root.getChildren().add(light);
		forground_objects.clear();
		;}});


	}

	WritableImage wim = new WritableImage(600, 600);
	public void saveimage(String filename){
		 Platform.runLater(new Runnable(){ public void run(){_saveimage(filename);}});
	 }

	public void _saveimage(String filename){
		File file=new File(filename);
	    scene.snapshot(wim);
		try{
			ImageIO.write(SwingFXUtils.fromFXImage(wim,null),"png",file);
		}catch (Exception e){e.printStackTrace();}

	}


    private  Scene createScene() {
        root  =  new  Group();
        //scene = new Scene(root, 600, 600, true);
        // Add the Camera to the Scene
        camera = new PerspectiveCamera(false);
	    camera.setTranslateX(200);
	    camera.setTranslateY(-300);
	    camera.setTranslateZ(-500);


        light = new PointLight();
	    light.setTranslateX(350);
	    light.setTranslateY(100);
        light.setTranslateZ(0);

        root.getChildren().add(light);

        Sphere sphere = new Sphere(50);
		sphere.setTranslateX(300);
		sphere.setTranslateY(-5);
        sphere.setTranslateZ(400);
        root.getChildren().add(sphere);

        scene = new Scene(root, 600, 600, true);
        scene.setCamera(camera);


        return (scene);
      }
    }