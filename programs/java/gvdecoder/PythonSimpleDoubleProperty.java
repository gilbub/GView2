package gvdecoder;

import org.python.core.*;
import org.python.core.Py.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;



public class PythonSimpleDoubleProperty{

DoubleProperty val=new SimpleDoubleProperty();

public PythonSimpleDoubleProperty(double d){
 val.set(d);
}
public DoubleProperty valueProperty() {return val;}

public void __setitem__(double d){
 val.set(d);
}
public double __getitem__(){
  return val.get();
}
}
