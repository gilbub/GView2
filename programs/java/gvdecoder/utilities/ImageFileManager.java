package gvdecoder.utilities;
import java.util.*;

public interface ImageFileManager{


public String[] GetRecordNames();

public Object GetRecordValue(int row, int col);

public void ReadArrayList(String filename);

public int GetRowCount();

public String GetFileType();

public ImageFileRecord[] ReturnSelectedFileNames(int startrow, int endrow);

}
