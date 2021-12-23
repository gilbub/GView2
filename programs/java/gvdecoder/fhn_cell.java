package gvdecoder;


public class fhn_cell{
 double U;
 double V;

 double dv;
 double du;

 double tau=0.08;
 double a=0.7;
 double b=0.8;


 public void solve(double dt){
   dv=dt*(V-(V*V*V)/3.0-U);
   du=dt*(tau*(V+a-b*U));
 }

 public void setParameters(double _tau, double _a, double _b){
	 tau=_tau;
	 a=_a;
	 b=_b;
 }

 public void update(){
    V+=dv;
    U+=du;
 }
}




