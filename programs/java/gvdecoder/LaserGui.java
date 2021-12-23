package gvdecoder;
import  eu.hansolo.medusa.*;
import eu.hansolo.medusa.skins.*;
import eu.hansolo.medusa.Gauge.LedType;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.input.MouseEvent;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class LaserGui{
	public LaserProperties laserProperties;

	private GridPane pane;

	static private Gauge   powerGauge;
    static private Gauge   wavelengthGauge;
    static private ToggleButton laserOnOff;
    static private ToggleButton shutterOpenClose;
    static private ComboBox enterWavelength;
    static double mousex, mousey, tmpthr;
    static double mousescalefactor=3.3;//this is very bad and lazy.

	public static void initAndShowGui(){
	JFrame frame = new JFrame("Swing and JavaFX");
	final JFXPanel fxPanel = new JFXPanel();
	frame.add(fxPanel);
	frame.setSize(600, 600);
	frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Platform.runLater(new Runnable() {
	            @Override
	            public void run() {
	                initFX(fxPanel);
	            }
       });
    }
    public LaserGui(LaserProperties p){
	 this.laserProperties=p;
	 initAndShowGui();
	 laserProperties.shutterOpenProperty().addListener(new ChangeListener(){
        @Override
        public void changed(ObservableValue o,Object oldVal, Object newVal){
             System.out.println("shutter open!");
        }
      });
     laserProperties.laserWavelengthProperty().addListener(new ChangeListener(){
	          @Override
	          public void changed(ObservableValue o,Object oldVal, Object newVal){
	               System.out.println("wavelength change!");
	               wavelengthGauge.setValue(((Integer)newVal).doubleValue());
	          }
      });
	}
    public static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }
    private static Scene createScene() {
		    Pane root=new Pane();

			wavelengthGauge=new Gauge();
			//wavelengthGauge.setSkin(new DashboardSkin(wavelengthGauge));
			wavelengthGauge.setBarColor(Color.BLUE);
			wavelengthGauge.setMinValue(690);
			wavelengthGauge.setMaxValue(1200);
			wavelengthGauge.setStartAngle(270);
			wavelengthGauge.setAngleRange(90);
			wavelengthGauge.setForegroundBaseColor(Color.WHITE);
			wavelengthGauge.setUnit("nm");
			wavelengthGauge.setTitle("wavelength");
			wavelengthGauge.setThresholdVisible(true);
			wavelengthGauge.setThreshold(1000);
			wavelengthGauge.setThresholdColor(Color.RED);

            wavelengthGauge.setOnMousePressed(new EventHandler<MouseEvent>(){
				@Override
				public void handle(MouseEvent event){
					mousex=event.getX();
					mousey=event.getY();
					Gauge g=(Gauge)event.getSource();
					tmpthr=g.getThreshold();
				}
		    });
			wavelengthGauge.setOnMouseDragged(new EventHandler<MouseEvent>() {
			        @Override
			        public void handle(MouseEvent e) {
						Gauge g=(Gauge)e.getSource();

						double dx=e.getX()-mousex;
						double dy=e.getY()-mousey;
						double di=Math.sqrt(dx*dx+dy*dy);
						if (dx<0) di=di*-1;
						double newt=tmpthr+(di)*mousescalefactor;
						if (newt<690) newt=690;
						if (newt>1200)newt=1200;
						g.setThreshold(newt);
			            System.out.println("mouse click detected! "+e.getSource());
			        }
			    });


	        powerGauge = GaugeBuilder.create()
					                          .title("Power")
					                          .subTitle("")
					                          .unit("W")
					                          .minValue(0)
					                          .maxValue(4)
					                          .startAngle(180)
					                          .angleRange(90)
					                          .ledVisible(true)
			                                  .ledBlinking(false)
			                                  .decimals(3)
			                                  .tickLabelDecimals(1)
			                                  .interactive(true)
			                                  .ledType(LedType.FLAT)
			                                  .foregroundBaseColor(Color.WHITE)
			                                  .minorTickMarkColor(Color.CYAN)
			                                  .majorTickMarkColor(Color.CYAN)
			                                  .build();
            powerGauge.setValue(3.1);
            wavelengthGauge.setValue(800);

            root.setStyle("-fx-background-color:#333333;");
            root.setPrefSize(1000,200);
            root.getChildren().add(wavelengthGauge);
            powerGauge.relocate(100,0);
            root.getChildren().add(powerGauge);
            laserOnOff=new ToggleButton("Laser On ");
            laserOnOff.relocate(400,75);//las.laserOnOff.relocate(400,75)
            shutterOpenClose=new ToggleButton("Shutter Open");
            enterWavelength=new ComboBox();
            enterWavelength.getItems().addAll("690","700","750","800","850","900","1000","1100");
            enterWavelength.setEditable(true);
            enterWavelength.relocate(400,35);

            shutterOpenClose.relocate(400,135);
            root.getChildren().add(shutterOpenClose);
            root.getChildren().add(enterWavelength);
            root.getChildren().add(laserOnOff);

            Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
            //root.getChildren().add(gauge);
	        return (scene);
    }




  }

