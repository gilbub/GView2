package gvdecoder;
   import JSci.maths.Complex;

   public class dft1d{
    static public  Complex[] go(double[] source){
      int sourceLength=source.length;
      Complex[] buffer=new Complex[sourceLength];
        for ( int i = 0; i < sourceLength; i++ ){
            double arg = - 2d * Math.PI * i / sourceLength;
            double r=0;
            double im=0;
            double cosarg;
            double sinarg;
            for ( int k = 0; k < sourceLength; k++ ){
                cosarg = Math.cos(k*arg);
                sinarg = Math.sin(k*arg);
                r+=source[k]*cosarg;
                im+=source[k]*sinarg;
                //buffer[i].addValue( source[ k ].getRealValue() * cosarg - source[ k ].getImagValue() * sinarg,source[ k ].getRealValue() * sinarg + source[ k ].getImagValue() * cosarg );
            }
            buffer[i]=new Complex(r,im);
        }

        for ( int i = 0; i < sourceLength; i++ )
              buffer[i]=buffer[i].divide(sourceLength);
        return buffer;
    }
    }
