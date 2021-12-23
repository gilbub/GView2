package gvdecoder;
import javax.swing.*;
public class GeneralDialogChoices{
 public String text;
 public String [] choices;
 public boolean showFileDialog;
 public JTextField textfield;
 public JComboBox combobox;
 public String chosenfile;
 public String chosenselection;
 public GeneralDialogChoices(String text, String[] choices, boolean showFileDialog){
	 this.text=text;
	 this.choices=choices;
	 this.showFileDialog=showFileDialog;

 }

 }