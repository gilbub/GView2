package gvdecoder;

public class fhn_array{

fhn_cell[] cells;
int xdim;
int ydim;
double dt;
double D;

 public fhn_array(int xdim, int ydim, double dt, double D){
  this.xdim=xdim;
  this.ydim=ydim;
  this.dt=dt;
  this.D=D;
  cells=new fhn_cell[xdim*ydim];
  for (int i=0;i<xdim*ydim;i++){cells[i]=new fhn_cell();}
 }

 public int get_index(int x, int y){
  return y*xdim+x;
 }

 public void solve(){
  for (int i=0;i<cells.length;i++)cells[i].solve(dt);
  for (int i=0;i<cells.length;i++)cells[i].update();

 }

 public void diffuse(){
 for (int i=1;i<xdim-1;i++){
  for (int j=1;j<ydim-1;j++){
    int index=get_index(i,j);
    double sum=0;
    for (int p=-1;p<=1;p++){
     for (int q=-1;q<=1;q++){
      int xx=i+p;
      int yy=j+q;
      sum+=(cells[get_index(xx,yy)].V-cells[index].V);
      }
      }
      cells[index].V+=sum*D;
      }
      }
   }

public void stimulate(int x_s, int y_s, int x_e, int y_e, double s){
  for (int x=x_s; x<x_e; x++){
   for (int y=y_s; y<y_e;y++){
     cells[get_index(x,y)].V+=s*dt;
     }
     }
    }

public void stimulate(Matrix m_in){
	for (int x=0;x<m_in.xdim;x++){
		for (int y=0;y<m_in.ydim;y++){
			cells[get_index(x,y)].V+=m_in.dat.get(0,y,x)*dt;
		}
	}
}

public void copy(Matrix ma, int z, boolean update_view){
  for (int x=0;x<xdim;x++){
	  for (int y=0;y<ydim;y++){
		  ma.dat.set(z,y,x,cells[get_index(x,y)].V*100.0);
	  }
  }
  if (update_view) ma.vw.JumpToFrame(z);
}

public void reset(double v, double u){
	for (int i=0;i<cells.length;i++){
		cells[i].U=u;
		cells[i].V=v;
		cells[i].dv=0;
		cells[i].du=0;
	}
}

public void setParameters(double tau, double a, double b){
	for (int i=0;i<cells.length;i++){
		cells[i].setParameters(tau,a,b);
	}
}


}