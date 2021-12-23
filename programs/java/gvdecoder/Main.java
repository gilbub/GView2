package gvdecoder;
import  eu.hansolo.medusa.*;
import eu.hansolo.medusa.skins.*;
import eu.hansolo.medusa.Gauge.LedType;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
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
	private GridPane pane;
	private Gauge   gauge;

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
    public static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }
    private static Scene createScene() {
	        Group  root  =  new  Group();
	        Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
	        Text  text  =  new  Text();

	        text.setX(40);
	        text.setY(100);
	        text.setFont(new Font(25));
	        text.setText("Welcome JavaFX!");

	        root.getChildren().add(text);
	        gauge = GaugeBuilder.create()
					                          .title("Power")
					                          .subTitle("")
					                          .unit("W")
					                          .minValue(0)
					                          .maxValue(4)
					                          .startAngle(180)
					                          .angleRange(90)
					                          .ledVisible(true)
			                                  .ledBlinking(true)
			                                  .decimals(3)
			                                  .tickLabelDecimals(1)
			                                  .interactive(true)
			                                  .ledType(LedType.FLAT) // STANDARD, FLAT
			                                  .build();

            root.getChildren().add(gauge);
	        return (scene);
    }













        pane = new GridPane();
        pane.setPadding(new Insets(20));
        pane.setHgap(10);
        pane.setVgap(15);
        //pane.setBackground(new Background(new BackgroundFill(Color.rgb(39,44,50), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.add(gauge, 0, 0);


    }

    @Override public void start(Stage stage) {
        Scene scene = new Scene(pane);

        gauge.setValue(3.1);



        stage.setTitle("Medusa Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() { System.exit(0); }




    public static void main(String[] args) { launch(args); }
}