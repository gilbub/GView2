//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public interface GeneralFilter{
 public RenderedOp getOp();
 public RenderedOp process(Object source);
 public RenderedOp reCreate(Object source);
 
}