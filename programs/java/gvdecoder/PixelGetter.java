package gvdecoder;
import javafx.beans.property.*;
public interface PixelGetter{

  double getPixelValue(int z, int y, int x);
  int getZDim();
  int getYDim();
  int getXDim();
  double getXMin();
  double getXMax();
  double getYMin();
  double getYMax();
  double getZMin();
  double getZMax();
  void update();
  IntegerProperty getChangedProperty();



}