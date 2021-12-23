package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.geom.GeneralPath;
import com.sun.image.codec.jpeg.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.net.URLConnection;

public class _Frame implements InternalFrameListener{
	public JInternalFrame iframe;
	public JFrame oframe;

	public JPanel panel;

	public static int Internal_Frame=0;
	public static int External_Frame=1;

	public _Frame(JPanel,
