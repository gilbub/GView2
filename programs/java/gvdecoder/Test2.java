package gvdecoder;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.HTMLEditor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Test2 {
	public HTMLEditor htmlEditor;
	String _html;

	public void setHTML(String str){
		    Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		                htmlEditor.setHtmlText(str);
		            }
		       });
    }


    private  void initAndShowGUI() {
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("Swing and JavaFX");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(300, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

    private Scene createScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
        htmlEditor = new HTMLEditor();
        htmlEditor.setPrefHeight(245);
        root.getChildren().add(htmlEditor);
        return (scene);
    }
/*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initAndShowGUI();
            }
        });
    }
    */
}