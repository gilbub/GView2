package gvdecoder;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Collections;
import java.util.Enumeration;


public class SortHashtable {

  public static void main(String[] args) {
    // Create and populate hashtable
    Hashtable ht = new Hashtable();
    ht.put("ABC", "abc");
    ht.put("XYZ", "xyz");
    ht.put("MNO", "mno");

    // Sort hashtable.
    Vector v = new Vector(ht.keySet());
    Collections.sort(v);

    // Display (sorted) hashtable.
    for (Enumeration e = v.elements(); e.hasMoreElements();) {
      String key = (String)e.nextElement();
      String val = (String)ht.get(key);
      System.out.println("Key: " + key + "     Val: " + val);
    }
  }
}
