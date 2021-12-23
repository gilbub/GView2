package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Viewer extends JInternalFrame implements ActionListener {
    int frameNumber = -1;
    Timer timer;
    boolean frozen = false;
    JLabel label;

    Viewer(int fps, String windowTitle) {
       super(windowTitle,
	                 true, //resizable
	                 true, //closable
	                 true, //maximizable
              true);//iconifiable

        int delay = (fps > 0) ? (1000 / fps) : 100;

        //Set up a timer that calls this object's action handler.
        timer = new Timer(delay, this);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameIconified(WindowEvent e) {
                stopAnimation();
            }
            public void internalFrameDeiconified(WindowEvent e) {
                startAnimation();
            }
            public void internalFrameClosing(WindowEvent e) {
                stopAnimation();
            }
        });

        label = new JLabel("Frame     ", JLabel.CENTER);
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (frozen) {
                    frozen = false;
                    startAnimation();
                } else {
                    frozen = true;
                    stopAnimation();
                }
            }
        });
        getContentPane().add(label, BorderLayout.CENTER);
		setSize(new Dimension(200,200));

		        //Set the window's location.
        setLocation(50,50);
    }


    //Can be invoked by any thread (since timer is thread-safe).
    public void startAnimation() {
        if (frozen) {
            //Do nothing.  The user has requested that we
            //stop changing the image.
        } else {
            //Start animating!
            if (!timer.isRunning()) {
                timer.start();
            }
        }
    }

    //Can be invoked by any thread (since timer is thread-safe).
    public void stopAnimation() {
        //Stop the animating thread.
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    public void actionPerformed(ActionEvent e) {
        //Advance the animation frame.
        frameNumber++;
        label.setText("Frame " + frameNumber);
    }
	}