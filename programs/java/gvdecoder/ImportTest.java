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
//import com.interactivemesh.jfx.importer;
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import com.interactivemesh.jfx.importer.ImportException;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.DrawMode;

public class ImportTest extends Application
{
    public static void main(String[] args)
    {
        Application.launch(args);
    }


 @Override
    public void start(Stage stage)
    {


StlMeshImporter stlImporter = new StlMeshImporter();

try {
    stlImporter.read(this.getClass().getResource("C:/CD/programs/java/sirface_binary.stl"));
}
catch (ImportException e) {
    e.printStackTrace();
    return;
}
 final PhongMaterial blueMaterial = new PhongMaterial();
    blueMaterial.setDiffuseColor(Color.DARKBLUE);
    blueMaterial.setSpecularColor(Color.BLUE);

TriangleMesh mesh = stlImporter.getImport();
stlImporter.close();
//MeshView mesh=new MeshView(cylinderHeadMesh);
Group root = new Group();
MeshView pyramid=new MeshView(mesh);
pyramid.setDrawMode(DrawMode.FILL);
pyramid.setMaterial(blueMaterial);
root.getChildren().add(pyramid);
Scene scene = new Scene(root, 1024, 800, true);
PerspectiveCamera camera = new PerspectiveCamera();
scene.setCamera(camera);
stage.setScene(scene);
stage.show();


camera.setTranslateZ(-3*100);

}
}