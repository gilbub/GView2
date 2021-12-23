package gvdecoder;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import quicktime.*;
import quicktime.app.*;
import quicktime.app.players.*;
import quicktime.app.display.*;
import quicktime.io.*;
import quicktime.std.*;
import quicktime.std.movies.*;

public class SimpleQTPlayer extends Frame {

  Movie movie;

  public SimpleQTPlayer (String title) {
    super (title);
    try {
      QTSession.open();
      FileDialog fd = new FileDialog (this,
                                      "Select source movie",
                                      FileDialog.LOAD);
      fd.show();
      if (fd.getFile() == null)
          return;

      // get movie from file
      File f = new File (fd.getDirectory(), fd.getFile());
      OpenMovieFile omFile =
        OpenMovieFile.asRead (new QTFile (f));
      movie = Movie.fromFile (omFile);

      // get a Drawable for Movie, put in QTCanvas
      MoviePlayer player = new MoviePlayer (movie);
      QTCanvas canvas    = new QTCanvas();
      canvas.setClient (player, true);
      add (canvas);

      // windows-like close-to-quit
      addWindowListener (new WindowAdapter() {
        public void windowClosing (WindowEvent e) {
          QTSession.close();
          System.exit(0);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main (String[] args) {
    SimpleQTPlayer frame =
      new SimpleQTPlayer ("Simple QTJ Player");
    frame.pack();
    frame.setVisible(true);
    try {
      frame.movie.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}