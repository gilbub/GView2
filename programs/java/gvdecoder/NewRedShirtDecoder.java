package gvdecoder;

public class NewRedShirtDecoder extends GenericDecoder{




public int OpenImageFile(String fpath){
	 if (super.OpenImageFile(fpath)!=-1) return parseFITS();
	return -1;

}

}