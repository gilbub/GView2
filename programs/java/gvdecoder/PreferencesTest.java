package gvdecoder;
import  java.util.prefs.*;

public class PreferencesTest {
    // Preference keys for this package
    private static final String NUM_ROWS = "num_rows";
    private static final String NUM_COLS = "num_cols";

    void foo() {
        Preferences prefs = Preferences.userNodeForPackage(NavigatorGraph.class);
        try{
        System.out.println("checking navigator");
        String[] k=prefs.keys();
        for (int i=0;i<k.length;i++){
			System.out.println(k[i]);
		}

       System.out.println("\n children ");
		String[] o=prefs.childrenNames();
		        for (int i=0;i<k.length;i++){
					System.out.println(o[i]);
		}
	   }catch(BackingStoreException e){}
	    catch(Exception e){}


	    prefs = Preferences.userNodeForPackage(tester.class);
	           try{
				System.out.println("checking tester");
	           String[] k=prefs.keys();
	           for (int i=0;i<k.length;i++){
	   			System.out.println(k[i]);
	   		}
	   }catch(BackingStoreException e){}

      prefs = Preferences.userNodeForPackage(trace.Trace.class);
	 	           try{
	 				System.out.println("checking Trace");
	 	           String[] k=prefs.keys();
	 	           for (int i=0;i<k.length;i++){
	 	   			System.out.println(k[i]);
	 	   		}
	   }catch(BackingStoreException e){}


    }
public static void main(String[] args){

PreferencesTest pt=new PreferencesTest();
pt.foo();
}

}
