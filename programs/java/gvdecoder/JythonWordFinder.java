package gvdecoder;
/*
adapted from Core SWING Advanced Programming
By Kim Topley
ISBN: 0 13 083292 8
Publisher: Prentice Hall
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.lang.Comparable;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class JythonWordFinder{
  JTextComponent txt;
  WordSearcher searcher;
  int currentWordIndex;
  JythonViewer jv=null;
  InfoWindow iw=null;
  public ArrayList<Pattern> functionPatterns=null;
  public ArrayList<Integer> foundLocations=null;
  public ArrayList<FunctionLocation> functionLocations;

 public JythonWordFinder(JythonViewer jv, JTextComponent txt){
   this.txt=txt;
   this.jv=jv;
   currentWordIndex=0;
   foundLocations=new ArrayList<Integer>();
   functionPatterns=new ArrayList<Pattern>();
   searcher=new WordSearcher(txt,foundLocations);
   functionLocations=new ArrayList<FunctionLocation>();
   addRegex("[ \t]*def[ \t]");
   addRegex("[ \t]*class[ \t]");
   addRegex("[ \t]\\w*\\(.*\\)\\{");
  }

  public void addRegex(String str){
	  Pattern pattern=Pattern.compile(str);
	  functionPatterns.add(pattern);

}
  public ArrayList findFunctions(){
	functionLocations.clear();
	String content = null;
	Document d=null;
    try {
      d = txt.getDocument();
      content = d.getText(0, d.getLength());
      }catch (BadLocationException e) {
      return null;
      }
     //Pattern pattern =  Pattern.compile("[ \t]*def[ \t]|[ \t]*class[ \t]");
     for (Pattern pattern:functionPatterns){

	  Matcher m = pattern.matcher(content);
	  while (m.find()) {
	 			int i= m.start();
	 			int location=0;
	 			try{
					location=jv.editor.getLineOfOffset(i);
				}catch(Exception e){}
				int end=content.indexOf('\n',i);

	 			String functname=content.substring(i,end);
                FunctionLocation fl=new FunctionLocation(location,functname,i);
                functionLocations.add(fl);
		}
	}
  return functionLocations;

}


public int scoreString(String input, String matchto){
	   int score=15;
	   String lowerc=matchto.toLowerCase();
	   int sz=input.length();

	   if ((matchto.indexOf(input)>0)&&(sz>1)) {score+= sz-matchto.indexOf(input); return score;}
	   else if ((lowerc.indexOf(input)>0)&&(sz>1)) {score+= sz-lowerc.indexOf(input)-5; return score;}
	   else {
		   score=0;

		   for (int k=0;k<input.length();k++){
			   char c=input.charAt(k);
			   if (lowerc.indexOf(c)>-1) score+=1;
		   }

		 return score;
	   }
   }

public void generateInfoWindowText(){
	int hitlength=functionLocations.size();
	if (hitlength>5) hitlength=5;
	if (hitlength>0){
	hits=new String[hitlength];
    for (int i=0;i<hitlength;i++) {
		  hits[i]=getInfoWindowText(i);
	    }//for
   }//hitlength
}

public String getInfoWindowText(int i){
	int j=i+functionOffset;
	if (j<0) return " ";
	if (j>=functionLocations.size()) return " ";
	FunctionLocation f=functionLocations.get(j);
	return ("  "+(i+functionOffset)+") "+f.name+" line = "+f.linenumber+" s"+f.score);
}

int functionOffset=0;
String[] hits;

public void score(String input){
	  for (FunctionLocation f:functionLocations) f.score=scoreString(input,f.name);
	  Collections.sort(functionLocations);
	  Collections.reverse(functionLocations);
	  functionOffset=0;
	  generateInfoWindowText();
	 // if (hits.length>0){
	   if (iw==null)iw=new gvdecoder.InfoWindow(jv,hits,0);
	   else if (iw.lines.length!=hits.length){
		   iw.kill();
		   iw=new gvdecoder.InfoWindow(jv,hits,0);
	   }
	   else{
		  iw.relocate();
		  iw.show();
		  for (int i=0;i<hits.length;i++) iw.setText(i,hits[i]);
	    }//else
}



//only recieves 1 or -1.
public void functionLocationOffset(int ii){
  System.out.println("1) selected ="+iw.selected);
  int i=0;
  if (ii>0) i=1;
  else i=-1;
  if ((iw.selected+i>=0)&&(iw.selected+i<5)){
	  iw.setSelected(iw.selected+i);
	  System.out.println("2) selected ="+iw.selected);
	  return;

}

int v=functionOffset+i;
  if (v<0) v=0;
  if (v>=functionLocations.size()-5) v=functionLocations.size()-5;

  functionOffset=v;
  generateInfoWindowText();
  for (int j=0;j<iw.lines.length;j++){
  	  iw.labels[j].setText(hits[j]);
  }

  if (i==-1){
	iw.setSelected(0);

}else
   iw.setSelected(4);
 iw.window.validate();
}

public int getInfoWindowPick(){
	FunctionLocation f=functionLocations.get(functionOffset+iw.selected);
	return f.location;
}

public int find(String str){

 currentWordIndex=0;

 return searcher.search(str);

}
  public int getNextLocation(){
    if (foundLocations.size()>0){
     int v=foundLocations.get(currentWordIndex);
     currentWordIndex+=1;
     if (currentWordIndex>=foundLocations.size()) currentWordIndex=0;
     return v;
   }
   return 0;

}
}

// A simple class that searches for a word in
// a document and highlights occurrences of that word

class WordSearcher {
  ArrayList foundLocations;
  public WordSearcher(JTextComponent comp, ArrayList f) {
    this.comp = comp;
    this.foundLocations=f;
    this.painter = new UnderlineHighlighter.UnderlineHighlightPainter(
        Color.red);
  }

  // Search for a word and return the offset of the
  // first occurrence. Highlights are added for all
  // occurrences found.
  public int search(String word) {
	foundLocations.clear();
    int firstOffset = -1;
    Highlighter highlighter = comp.getHighlighter();

    // Remove any existing highlights for last word
    Highlighter.Highlight[] highlights = highlighter.getHighlights();
    for (int i = 0; i < highlights.length; i++) {
      Highlighter.Highlight h = highlights[i];
      if (h.getPainter() instanceof UnderlineHighlighter.UnderlineHighlightPainter) {
        highlighter.removeHighlight(h);
      }
    }

    if (word == null || word.equals("")) {
      return -1;
    }

    // Look for the word we are given - insensitive search
    String content = null;
    try {
      Document d = comp.getDocument();
      content = d.getText(0, d.getLength()).toLowerCase();
    } catch (BadLocationException e) {
      // Cannot happen
      return -1;
    }

    word = word.toLowerCase();
    int lastIndex = 0;
    int wordSize = word.length();

    while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
      int endIndex = lastIndex + wordSize;
      foundLocations.add(lastIndex);
      try {
        highlighter.addHighlight(lastIndex, endIndex, painter);
      } catch (BadLocationException e) {
        // Nothing to do
      }
      if (firstOffset == -1) {
        firstOffset = lastIndex;
      }
      lastIndex = endIndex;
    }

    return firstOffset;
  }

  protected JTextComponent comp;

  protected Highlighter.HighlightPainter painter;

}

class UnderlineHighlighter extends DefaultHighlighter {
  public UnderlineHighlighter(Color c) {
    painter = (c == null ? sharedPainter : new UnderlineHighlightPainter(c));
  }

  // Convenience method to add a highlight with
  // the default painter.
  public Object addHighlight(int p0, int p1) throws BadLocationException {
    return addHighlight(p0, p1, painter);
  }

  public void setDrawsLayeredHighlights(boolean newValue) {
    // Illegal if false - we only support layered highlights
    if (newValue == false) {
      throw new IllegalArgumentException(
          "UnderlineHighlighter only draws layered highlights");
    }
    super.setDrawsLayeredHighlights(true);
  }

  // Painter for underlined highlights
  public static class UnderlineHighlightPainter extends
      LayeredHighlighter.LayerPainter {
    public UnderlineHighlightPainter(Color c) {
      color = c;
    }

    public void paint(Graphics g, int offs0, int offs1, Shape bounds,
        JTextComponent c) {
      // Do nothing: this method will never be called
    }

    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
        JTextComponent c, View view) {
      g.setColor(color == null ? c.getSelectionColor() : color);
      String hl=null;
      Rectangle alloc = null;
      if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
        if (bounds instanceof Rectangle) {
          alloc = (Rectangle) bounds;
        } else {
          alloc = bounds.getBounds();
        }
      } else {
        try {
          Shape shape = view.modelToView(offs0,
              Position.Bias.Forward, offs1,
              Position.Bias.Backward, bounds);
          hl=c.getText(offs0,(offs1-offs0));
          alloc = (shape instanceof Rectangle) ? (Rectangle) shape
              : shape.getBounds();
        } catch (BadLocationException e) {
          return null;
        }
      }

      FontMetrics fm = c.getFontMetrics(c.getFont());
      int baseline = alloc.y + alloc.height - fm.getDescent() + 1;

       g.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline);
       g.drawLine(alloc.x, baseline + 1, alloc.x + alloc.width,     baseline + 1);
      //g.drawString(hl,alloc.x,baseline);


      return alloc;
    }

    protected Color color; // The color for the underline
  }

  // Shared painter used for default highlighting
  protected static final Highlighter.HighlightPainter sharedPainter = new UnderlineHighlightPainter(
      null);

  // Painter used for this highlighter
  protected Highlighter.HighlightPainter painter;
}

class FunctionLocation implements Comparable<FunctionLocation>{
	String name;
	int location;
	int linenumber;
	int score=0;
	public FunctionLocation(int line, String n, int loc){
		name=n;
		location=loc;
		linenumber=line;

	}

	public int compareTo(FunctionLocation another) {
	        if (this.score<another.score) return -1;
	        else if (this.score==another.score) return 0;
	        else return 1;
	        }
  }





