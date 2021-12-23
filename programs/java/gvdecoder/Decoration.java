package gvdecoder;

import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.*;

import java.awt.image.*;
import java.awt.image.renderable.*;

public class Decoration{
  public Color color;
  public int xloc;
  public int yloc;
  public String str=null;
  public int fontsize=0;
  public Shape shape;
  public boolean fill;
  public String fontname="Arial";
  public Ruler ruler;
  public float linewidth=1.0f;
  public boolean absolute=false; //absolute position

public Decoration(Color color, int x, int y, String str, Shape sh, boolean fill){
	this.color=color;
	this.xloc=x;
	this.yloc=y;
	this.str=str;
	this.shape=sh;
	this.fill=fill;
	this.fontsize=0;
	this.absolute=false;
}

public Decoration(Color color, int x, int y, String str, int fontsize, String fontname, Shape sh, boolean fill){
	this.color=color;
	this.xloc=x;
	this.yloc=y;
	this.str=str;
	this.shape=sh;
	this.fill=fill;
	this.fontsize=fontsize;
	this.fontname=fontname;
	this.absolute=false;
}

public Decoration(Color color, int x, int y, String str, int fontsize, String fontname, Shape sh, boolean fill,boolean absolute){
	this.color=color;
	this.xloc=x;
	this.yloc=y;
	this.str=str;
	this.shape=sh;
	this.fill=fill;
	this.fontsize=fontsize;
	this.fontname=fontname;
	this.absolute=absolute;
}


public Decoration(){

}

}