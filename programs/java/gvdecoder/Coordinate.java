package gvdecoder;
import java.lang.Math;
import java.util.*;

public class Coordinate{
	public int z=-1;
	public int y=-1;
	public int x=-1;

	public Coordinate(int z, int y, int x){
		this.z=z;
		this.y=y;
		this.x=x;
	}
	public Coordinate(int y, int x){
		this.y=y;
		this.x=x;
		this.z=-1;
	}
	public String toString(){
		if (z==-1) return "y,x = ("+y+","+x+")";
		else return "z,y,x = ("+z+","+y+","+x+")";
	}

    public double distance3D(Coordinate c){
		return Math.sqrt( (c.z-z)*(c.z-z) + (c.y-y)*(c.y-y) + (c.x-x)*(c.x-x) );
	}

	public double distance2D(Coordinate c){
		return Math.sqrt( (c.y-y)*(c.y-y) + (c.x-x)*(c.x-x) );
	}

	public boolean equals(Coordinate c){
		return ((c.z==z) && (c.x==x) && (c.y==y));
	}

}