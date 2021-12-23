package gvdecoder;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;

public class Fx3DShapeExample1 extends Application
{
    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        // Create a Box
        Box box = new Box(10, 10, 10);
        box.setTranslateX(150);
        box.setTranslateY(0);
        box.setTranslateZ(400);

        // Create a Sphere
        Sphere sphere = new Sphere(50);
        sphere.setTranslateX(300);
        sphere.setTranslateY(-5);
        sphere.setTranslateZ(400);

        // Create a Cylinder
        Cylinder cylinder = new Cylinder(40, 120);
        cylinder.setTranslateX(500);
        cylinder.setTranslateY(-25);
        cylinder.setTranslateZ(600);

        // Create a Light
        PointLight light = new PointLight();
        light.setTranslateX(350);
        light.setTranslateY(100);
        light.setTranslateZ(0);

        // Create a Camera to view the 3D Shapes
        PerspectiveCamera camera = new PerspectiveCamera(false);
        camera.setTranslateX(200);
        camera.setTranslateY(-300);
        camera.setTranslateZ(-500);

        PhongMaterial material6 = new PhongMaterial();

		        //setting the specular color map to the material
	    material6.setDiffuseColor(Color.GREEN);

        //spher.setMaterial(material6);

        // Add the Shapes and the Light to the Group
        Group root = new Group();
        root.getChildren().add(box);
         root.getChildren().add(light);
        //root.getChildren().add(light);
        for (int x=0;x<500000;x++){
			Box bx = new Box(1, 1, 1);
			bx.setTranslateX(Math.random()*500+100);
			bx.setTranslateY(Math.random()*500+100);
            bx.setTranslateZ(Math.random()*500);
            PhongMaterial m = new PhongMaterial();

					        //setting the specular color map to the material
	       // material6.setDiffuseColor(Color.GREEN);
           Color c = new Color(Math.random(),Math.random(),Math.random(),Math.random());
           m.setDiffuseColor(c);
           bx.setMaterial(m);
           root.getChildren().add(bx);
	  }
	  // root.getChildren().add(light);

        // Create a Scene with depth buffer enabled
        Scene scene = new Scene(root, 600, 600, true);
        // Add the Camera to the Scene
        scene.setCamera(camera);

        // Add the Scene to the Stage
        stage.setScene(scene);
        // Set the Title of the Stage
        stage.setTitle("An Example with Predefined 3D Shapes");
        // Display the Stage
        stage.show();
    }
}