package gvdecoder;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestJavaFX extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(TestJavaFX.class, args);
    }

    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle("Hello World");
        Group root = new Group();
        Scene scene = new Scene(root, 300, 250, Color.LIGHTGREEN);
        Button btn = new Button();
        btn.setLayoutX(100);
        btn.setLayoutY(80);
        btn.setText("Create stage");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                makeStage();
                primaryStage.toFront();

            }
        });
        root.getChildren().add(btn);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    int stnum=0;
    public void makeStage(){
		new CreateStage(this,stnum++);
	}
    public void sayit(){
		System.out.println("from top");
	}
}

class CreateStage {
    public TestJavaFX tfx;
    public CreateStage(TestJavaFX tfx, int num) {
		this.tfx=tfx;
        Stage stage = new Stage();
        stage.setTitle("Many st "+num);
        Group root=new Group();
        Button btn=new Button();
        btn.setLayoutX(100);
		btn.setLayoutY(80);
        btn.setText("say it");
         btn.setOnAction(new EventHandler<ActionEvent>() {

		            public void handle(ActionEvent event) {
		               System.out.println("it");
                       tfx.sayit();
		            }
        });
        root.getChildren().add(btn);
        stage.setScene(new Scene(root, 260, 230, Color.LIGHTCYAN));
        stage.show();



    }
}