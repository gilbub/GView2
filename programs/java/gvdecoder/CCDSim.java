package gvdecoder;


/****
def shutter(m_in,m_out,N):
  c=0
  for z in range(m_out.zdim/(N*N)):
   w.print("frame")
   for p in range(N):
     for q in range(N):
       for y in range(m_out.ydim/N):
        for x in range(m_out.xdim/N):
         m_out.dat.set(z,y*N+p,x*N+q,m_in.dat.get(z*N*N+p*N+q,y*N+p,x*N+q))
  return m_in,m_out

def normal(m_in,m_out,N):
  for z in range(m_out.zdim/(N*N)):
   w.print("frame")
   for y in range(m_out.ydim):
    for x in range(m_out.xdim):
     val=0
     for p in range(N*N):
      val+=m_in.dat.get(z*(N*N)+p,y,x)
     m_out.dat.set(z,y,x,val)
  return m_in, m_out

def extract(m_in, m_out,N):
  for z in range(m_in.zdim):
  w.print("frame")
   for x in range(m_out.xdim):
    for y in range(m_out.ydim):
     for p in range(N):
      for q in range(N):

       m_out.dat.set(z*(N*N)+p*N+q,y,x, m_in.dat.get(z,y*N+p,x*N+q))
  return m_in, m_out


	One thing to get working is the 4 equations 4 unknown


****/

import gvdecoder.Matrix;

public class CCDSim{
 public int zreduction;
 public int xreduction;
 public int yreduction;
 public Matrix mo; //original data set
 public Matrix ms; //shutter, direct exposure
 public Matrix mse; //shutter, expose all else
 public Matrix mse_s; //solved version of mse

 public int[] sequence;
 public solver solve=new solver();

public CCDSim(Matrix m_in, int N){
 zreduction=(int)((double)m_in.zdim)/(N*N);
 xreduction=(int)((double)m_in.xdim)/N;
 yreduction=(int)((double)m_in.ydim)/N;
 mo=m_in;
}


public void shuffle(int len, int N){
	sequence=new int[len];
	for (int k=0; k<len; k+=(N*N)){
     for (int i=0;i<N*N;i++){
		 sequence[k+i]=i;
	 }
    }
    for (int k=0;k<len;k+=(N*N)){


	for (int i=0; i<N*N; i++){
	  int j=(int)(Math.random()*(N*N));
	  if(i!=j){
	    int temp=sequence[k+i];
	    sequence[k+i]=sequence[k+j];
	    sequence[k+j]=temp;
	    }
	  }
    }


}





public void shutter(Matrix m_in, Matrix m_out, int N){

 for (int z=0;z<zreduction;z++){
  for (int p=0;p<N;p++){
    for (int q=0;q<N;q++){
     for (int y=0;y<yreduction;y++){
      for (int x=0;x<xreduction;x++){
        m_out.dat.set(z,y*N+p,x*N+q,m_in.dat.get(z*N*N+p*N+q,y*N+p,x*N+q));
        }
        }
        }
        }
        }

    }




public void shutter_snap(Matrix m_in, Matrix m_out, int[][] timing, int N){
/*
divide up into two fields.
*/




	for (int z=0;z<zreduction;z++){

	    for (int x=0;x<xreduction;x++){
	    for (int y=0;y<yreduction;y++){

	    for (int p=0;p<N;p++){
	    for (int q=0;q<N;q++){
			double val=m_in.dat.get(z*N*N+timing[p][q],y*N+p,x*N+q);

	        m_out.dat.set(z,y*N+p,x*N+q,val);
	        }
	        }
	        }
	        }
	        }
}

/*

public void extract_snapHR(Matrix m_in, Matrix m_snap,  int[][] timing, int N){
  for (int z=0;z<m_out.zdim;z++){
   for (int x=0;x<xreduction;x++){
    for (int y=0;y<yreduction;y++){
      for (int p=0;p<N;p++){
        for (int q=0;q<N;q++){

		 double val=m_in.dat.get(z,y*N+p,x*N+q);
		 for (int a=-1;a<=N;a++){
		   for (int b=-1;b<=N;b++){
		    m_out.dat.set(z*N*N+p*N+q,y*N+a+p,x*N+b+q,val);
		 			}
		}

         }
         }
         }
         }
     }
}

*/


public void shutter_expose(Matrix m_in, Matrix m_out, int N){
normal(m_in,m_out,N); // fully expose m_out.
for (int z=0;z<zreduction;z++){
for (int x=0;x<xreduction;x++){
 for (int y=0;y<yreduction;y++){

  for (int p=0;p<N;p++){
    for (int q=0;q<N;q++){
		double val=m_out.dat.get(z,y*N+p,x*N+q);
		val-=m_in.dat.get(z*N*N+p*N+q,y*N+p,x*N+q);
        m_out.dat.set(z,y*N+p,x*N+q,val);
        }
        }
        }
        }
        }

    }

public Matrix solve4x4(Matrix m_in, int N){
double[] in_arr=new double[N*N];
Matrix solution=new Matrix(m_in.zdim,m_in.ydim,m_in.xdim);
for (int z=0;z<zreduction;z++){
 for (int x=0;x<xreduction;x++){
 for (int y=0;y<yreduction;y++){

  for (int p=0;p<N;p++){
    for (int q=0;q<N;q++){
     in_arr[p*N+q]=m_in.dat.get(z,y*N+p,x*N+q);
    }
   }
  double[] res=solve.solve4(in_arr[0],in_arr[1],in_arr[2], in_arr[3]);
  for (int p=0;p<N;p++){
    for (int q=0;q<N;q++){
     solution.dat.set(z,y*N+p,x*N+q,res[p*N+q]);//[p*N+q]=m_in.dat.get(z,y*N+p,x*N+q);
    }
   }

}
}
}
return solution;
}


public Matrix solve4x4v2(Matrix m_in, int N){
double[] in_arr=new double[N*N];
Matrix solution=new Matrix(m_in.zdim,m_in.ydim,m_in.xdim);
for (int z=0;z<zreduction;z++){
 for (int x=0;x<xreduction;x++){
 for (int y=0;y<yreduction;y++){

  for (int p=0;p<N;p++){
    for (int q=0;q<N;q++){
     in_arr[p*N+q]=m_in.dat.get(z,y*N+p,x*N+q);
    }
   }
  double[] res=solve.solveN(in_arr);
  for (int p=0;p<N;p++){
    for (int q=0;q<N;q++){
     solution.dat.set(z,y*N+p,x*N+q,res[p*N+q]);//[p*N+q]=m_in.dat.get(z,y*N+p,x*N+q);
    }
   }

}
}
}
return solution;
}




public void shutter_noise(Matrix m_in, Matrix m_out, int N, double noise){

 for (int z=0;z<zreduction;z++){
  for (int y=0;y<yreduction;y++){
  for (int x=0;x<xreduction;x++){

   for (int p=0;p<N;p++){
   for (int q=0;q<N;q++){
	    double val=m_in.dat.get(z*N*N+p*N+q,y*N+p,x*N+q);
    	for (int i=-1;i<2;i++){
		 for (int j=-1;j<2;j++){
		  if (!((j==0)&&(i==0)))
		  val+=noise*m_in.dat.get(z*N*N+p*N+q,y*N+p+i,x*N+q+j);
	      }
	     }
        m_out.dat.set(z,y*N+p,x*N+q,val);
        }
       }
      }
     }
    }
   }


public void normal(Matrix m_in, Matrix m_out, int N){
 for (int z=0;z<zreduction;z++){
   for (int x=0;x<m_out.xdim;x++){
  for (int y=0;y<m_out.ydim;y++){

     double val=0;
     for (int p=0;p<N*N;p++){
      val+=m_in.dat.get(z*(N*N)+p,y,x);
      }
     m_out.dat.set(z,y,x,val);
     }
     }
     }
   }

public void extract(Matrix m_in, Matrix m_out, int N){
  for (int z=0;z<m_out.zdim;z++){
   for (int x=0;x<m_out.xdim;x++){
    for (int y=0;y<m_out.ydim;y++){
     for (int p=0;p<N;p++){
      for (int q=0;q<N;q++){
         m_out.dat.set(z*(N*N)+p*N+q,y,x,m_in.dat.get(z,y*N+p,x*N+q));
         }
         }
         }
         }
     }
}


public void shutter_rand(Matrix m_in, Matrix m_out, int N){
 int cnt=0;
 for (int z=0;z<zreduction;z++){

  //for (int p=0;p<N;p++)
  //  for (int q=0;q<N;q++)
   for (int x=0;x<xreduction;x++){
     for (int y=0;y<yreduction;y++){

		for (int k=0;k<N*N;k++){
        int val=sequence[cnt];
        int q=val%N;
        int p=(val-q)/N;
        double level=m_in.dat.get(z*N*N+k,y*N+p,x*N+q);
        m_out.dat.set(z,y*N+p,x*N+q,level);
	    cnt++;
	    if (cnt>=sequence.length) cnt=0;
	    }

        }
        }


        }

    }


public void extract_rand(Matrix m_in, Matrix m_out, int N){
  int cnt=0;
  for (int z=0;z<m_in.zdim;z++){
   for (int x=0;x<m_out.xdim;x++){
    for (int y=0;y<m_out.ydim;y++){
    for (int k=0;k<N*N;k++){
	        int val=sequence[cnt];
	        int q=val%N;
	        int p=(val-q)/N;
	        m_out.dat.set(z*(N*N)+k,y,x,m_in.dat.get(z,y*N+p,x*N+q));

	        //m_out.dat.set(z,y*N+p,x*N+q,m_in.dat.get(z*N*N+p*N+q,y*N+p,x*N+q));
		    cnt++;
		    if (cnt>=sequence.length) cnt=0;
	    }


         }
         }
     }
}

public void extract2_rand(Matrix m_in, Matrix m_out, int N, int thresh){
	//if t!=t+1,then assumes superpixel moves,and replace whole with the pixel
	int cnt=0;
	for (int z=0;z<m_in.zdim;z++){
	  for (int x=0;x<xreduction;x++){
		  for (int y=0;y<yreduction;y++){
		  boolean changed=false;
		  for (int p=0;p<N;p++){
			for (int q=0;q<N;q++){
			 double t1=m_in.dat.get(z,y*N+p,x*N+q);
			 double t2=m_in.dat.get(z+1,y*N+p,x*N+q);
			 if (Math.abs(t1-t2)>thresh){
				 changed=true;
			    }
			}
		}
		if (!changed){
	     //use high rez data
	      for (int p=0;p<N;p++){
			for (int q=0;q<N;q++){
			  double v=m_in.dat.get(z,y*N+p,x*N+q);

			  cnt++;
			  if (cnt>=sequence.length) cnt=0;

			  for (int zz=0;zz<N*N;zz++){
                m_out.dat.set(z*N*N+zz,y*N+p,x*N+q,v);
			}
	       }
		  }
	  }else{
		 //replace w high temp data
	    for (int k=0;k<N*N;k++){
	        int v=sequence[cnt];
	        cnt++;
		    if (cnt>=sequence.length) cnt=0;
	        int q=v%N;
	        int p=(v-q)/N;
			double val=m_in.dat.get(z,y*N+p,x*N+q);
			  for (int a=0;a<N;a++){
			   for (int b=0;b<N;b++){
			    m_out.dat.set(z*N*N+k,y*N+a,x*N+b,val);
			}
		}
	   }//k

     }//else
}
}
}

}




public void extract_shift(Matrix m_in, Matrix m_out, int N){
  for (int z=0;z<m_out.zdim;z++){
   for (int x=0;x<xreduction;x++){
    for (int y=0;y<yreduction;y++){
      for (int p=0;p<N;p++){
        for (int q=0;q<N;q++){
		 double val=m_in.dat.get(z,y*N+p,x*N+q);
		 for (int a=-1;a<=N;a++){
		   for (int b=-1;b<=N;b++){
		    m_out.dat.set(z*N*N+p*N+q,y*N+a+p,x*N+b+q,val);
		 			}
		}

         }
         }
         }
         }
     }
}


public void extract_shift2(Matrix m_in, Matrix m_out, int N, int pm, int qm){
  for (int z=0;z<m_out.zdim;z++){
   for (int x=0;x<xreduction;x++){
    for (int y=0;y<yreduction;y++){
      for (int p=0;p<N;p++){
        for (int q=0;q<N;q++){
		 double val=m_in.dat.get(z,y*N+p,x*N+q);
		 for (int a=-1;a<=N;a++){
		   for (int b=-1;b<=N;b++){
		    m_out.dat.set(z*N*N+p*N+q,y*N+a+p*pm,x*N+b+q*qm,val);
		 			}
		}

         }
         }
         }
         }
     }
}


public double[] calcnoise(Matrix m1, Matrix m2){
	double maxval=0;
	double mserr=0;
	double psnr=0;
	for (int z=0;z<m1.zdim;z++){
     for (int x=0;x<m1.xdim;x++){
	  for (int y=0;y<m1.ydim;y++){
	   if (m1.dat.get(z,y,x)>maxval) maxval=m1.dat.get(z,y,x);
	   if (m2.dat.get(z,y,x)>maxval) maxval=m2.dat.get(z,y,x);

	   double dif=(m1.dat.get(z,y,x)-m2.dat.get(z,y,x));
	   mserr+=dif*dif;
	  }
  }
}
 mserr=mserr/(m1.zdim*m1.ydim*m1.xdim);
 psnr=20*(Math.log(maxval/Math.sqrt(mserr))/Math.log(10));
 double[] res=new double[2];
 res[0]=mserr;
 res[1]=psnr;
 return res;
}




public void extract2(Matrix m_in, Matrix m_out, int N, int thresh){
	//if t!=t+1,then assumes superpixel moves,and replace whole with the pixel
	for (int z=0;z<m_in.zdim;z++){
	 for (int y=0;y<yreduction;y++){
	  for (int x=0;x<xreduction;x++){
		  boolean changed=false;
		  for (int p=0;p<N;p++){
			for (int q=0;q<N;q++){
			 double t1=m_in.dat.get(z,y*N+p,x*N+q);
			 double t2=m_in.dat.get(z+1,y*N+p,x*N+q);
			 if (Math.abs(t1-t2)>thresh){
				 changed=true;
			    }
			}
		}
		if (!changed)
		{
	     //use high rez data
	      for (int p=0;p<N;p++){
			for (int q=0;q<N;q++){
			  double val=m_in.dat.get(z,y*N+p,x*N+q);
			  for (int zz=0;zz<N*N;zz++){
                m_out.dat.set(z*N*N+zz,y*N+p,x*N+q,val);
			}
	       }
		  }
	  }
	  else
	  {
		 //replace w high temp data
		 for (int p=0;p<N;p++){
			for (int q=0;q<N;q++){
			  double val=m_in.dat.get(z,y*N+p,x*N+q);
			  for (int a=0;a<N;a++){
			   for (int b=0;b<N;b++){
			    m_out.dat.set(z*N*N+p*N+q,y*N+a+p,x*N+b+q,val);
			}
		}
	   }
      }
     }//else
}
}
}

}
public void extract3(Matrix m_in, Matrix m_out, int N, int thresh){
	//if t!=t+1,then assumes superpixel moves,and replace whole with the pixel
	for (int z=0;z<m_in.zdim;z++){
	 for (int y=0;y<yreduction;y++){
	  for (int x=0;x<xreduction;x++){
		  boolean changed=false;
		  for (int p=0;p<N;p++){
			for (int q=0;q<N;q++){
			 double t1=m_in.dat.get(z,y*N+p,x*N+q);
			 double t2=m_in.dat.get(z+1,y*N+p,x*N+q);
			 if (Math.abs(t1-t2)>thresh){
				 changed=true;
			    }
			}
		}
		/*if (!changed)*/
		{
	     //use high rez data
	      for (int p=0;p<N;p++){
			for (int q=0;q<N;q++){
			  double val=m_in.dat.get(z,y*N+p,x*N+q);
			  for (int zz=0;zz<N*N;zz++){
                m_out.dat.set(z*N*N+zz,y*N+p,x*N+q,val);
			}
	       }
		  }
	  }
	  if (changed)
	  {
		 //replace w high temp data
		 for (int p=0;p<N;p++){
			for (int q=0;q<N;q++){
			  double val=m_in.dat.get(z,y*N+p,x*N+q);
			  for (int a=0;a<N;a++){
			   for (int b=0;b<N;b++){
			    m_out.dat.set(z*N*N+p*N+q,y*N+a+p,x*N+b+q,val);
			}
		}
	   }
      }
     }//else
}
}
}

}


}






