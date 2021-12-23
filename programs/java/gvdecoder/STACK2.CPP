#include <graphics.h>
#include <iostreams.h>
#include <stdio.h>
#include <stdlib.h>
#include <conio.h>
#include <strstream.h>
#include <dos.h>
#include <strstream.h>
#include <new.h>


#include <c:\models\gparam.h>
#include <c:\models\graphnew.h>
#include <c:\models\giofix.h>
#include <c:\models\linklist.h>


#include "svgautil.h"
#include "svga16.h"
#include "twk16.h"
#include "twk256.h"
#include "svga256.h"
#include "svga32k.h"
#include "svga64k.h"
#include "svgatc.h"
#include "svgas3.h"

#define ARROW_SIZE 10

FILE *stream;
char* inname = "testtext.dat"; // made global for simplicity, though bad style
size_t FrameElemSize=0;   // global to help avoid making whole program a template
		       // is either an unsigned int or a long ints size



int* userframe;
float* userbackground;
int* LastF;
int* LastLastF;
float* userScale;
float* FrameStack;


int TOPGRAPHX=400;
int TOPGRAPHY=1;
int BOTGRAPHX=635;
int BOTGRAPHY=450;
unsigned long int cc3 = 0;
long int UserFrameCount=0;

enum DATATYPE{FLOAT,LONG,INT,UNSIGNED};

DATATYPE framedata=UNSIGNED;


int Y_dim;
int X_dim;


template <class T>
class frame
{
 public:
  void* elem;
  unsigned long TotSize;
  void loadframe();
  frame(unsigned long TotSize);
  ~frame();



};

template <class T>
void frame<T>::loadframe()
{

 //convert
switch(framedata)
{
 case (FLOAT): { fread(elem,sizeof(float), TotSize,stream);
		for (int cc1=0;cc1<TotSize;cc1++)
		userframe[cc1]=(int) ((float*)elem)[cc1];
		break; }
 case (INT): {   fread(elem,sizeof(int), TotSize,stream);
		for (int cc1=0;cc1<TotSize;cc1++)
		userframe[cc1]=(int) ((int*)elem)[cc1];
		break;  }
 case (LONG): {  fread(elem,sizeof(long), TotSize,stream);
		for (int cc1=0;cc1<TotSize;cc1++)
		userframe[cc1]=(long) ((long*)elem)[cc1];
		break;  }
 case (UNSIGNED): {  fread(elem,sizeof(unsigned), TotSize,stream);
		for (int cc1=0;cc1<TotSize;cc1++)
		userframe[cc1]=(unsigned) ((unsigned*)elem)[cc1];
		break;  }



}
}

template <class T>
frame<T>::frame(unsigned long TS)
{

switch(framedata)
{ case (FLOAT)  : {elem = new float[TS+1];FrameElemSize=sizeof(float); break;}
  case (INT)    : {elem = new int[TS+1];FrameElemSize=sizeof(int); break;}
  case (LONG)   : {elem = new long[TS+1];
		   FrameElemSize=sizeof(long);
		   break;
		   }
  case (UNSIGNED):{elem = new unsigned[TS+1];
		   FrameElemSize=sizeof(unsigned);
		   break;}
}
 TotSize=TS;

}


template <class T>
frame<T>::~frame()
{delete [] elem;}



/* Returns the color for white */
long WhitePixel()
{
  switch(getmaxcolor()) {
    case 32768: return 0x7fffL;
    case 65535: return 0xffffL;
    case 16777: return 0xffffffL;
    default   : return 15;
  };
}

int huge DetectVGA256()
{
  int Vid;
/*
  printf("Which video mode would you like to use? \n");
  printf("  0) 320x200x256\n");
  printf("  1) 640x400x256\n");
  printf("  2) 640x480x256\n");
  printf("  3) 800x600x256\n");
  printf("  4) 1024x768x256\n");
  printf("  5) 640x480x256\n");
  printf("  6) 1280x1024x256\n");
  printf("\n> ");
  scanf("%d",&Vid);      */
  Vid=2;
  return Vid;
}



template <class T>
void MaxMinFrame(int& Max, int& Min, T* Frame, int size)
 {

  Max=-5000;
  Min=5000;
  for (int cc1=0;cc1<size;cc1++)
   { if (userframe[cc1]>Max) Max=userframe[cc1];
     if (userframe[cc1]<Min) Min=userframe[cc1];
   }
 }



void outline(int tx, int ty, int bx, int by)
{



int col=getcolor();


{
setcolor(BLACK);
 line(tx,ty,bx,ty);
 line(tx,ty,tx,by);
 line(bx,by,tx,by);
 line(bx,by,bx,ty);


setcolor(YELLOW);
 line(tx,ty,bx,ty);
 line(tx,ty,tx,by);
 line(bx,by,tx,by);
 line(bx,by,bx,ty);

setcolor(LIGHTRED);
 line(tx,ty,bx,ty);
 line(tx,ty,tx,by);
 line(bx,by,tx,by);
 line(bx,by,bx,ty);


setcolor(col);
 line(tx,ty,bx,ty);
 line(tx,ty,tx,by);
 line(bx,by,tx,by);
 line(bx,by,bx,ty);
}

}

void outlinemore(int tx, int ty, int bx, int by)
{
 for (int cc=0;cc<20;cc++)
{
 outline(tx-1,ty-1,bx+1,by+1);
 outline(tx,ty,bx,by);
 outline(tx+1,ty+1,bx-1,by-1);
}
}



int BXNum=0;
int COLINDEX=1;


/*these global variables control the frame average/stack stuff*/

int stackfilenum=1; //declared global, keeps track of number of framestackfiles
long int startstack=0L;
long int endstack=0L;
int dividestack=0;
int framesperstack=1;
int stackon=0;
int stackinc=0;
int stackbacksubtract=0;

/*globals to control contour stuff*/
int contourfilenum=1;
long int startcontour=0L;
long int endcontour=0L;
int dividecontour=0;
int framespercontour=1;
int contouron=0;
int contourinc=0;
int nextcontour=0;
int contourbacksubtract=0;
int newcontourframe=0;
int overwritecontour=6;

void respond(FILE* stream,char* SS, int TotSize)
{
 char*  Text="                                 ";
 long int Fnum=0;
 long int Fnum2=0;
 if(parse(SS,"j",Fnum,0L))
   {
    fseek(stream,4100*sizeof(char)+(Fnum*TotSize*FrameElemSize),SEEK_SET);
    UserFrameCount=Fnum;
    return;
   }

if(parse(SS,"b",startstack,0L))
   {
    stackon=0;

    parse(SS,"e",endstack,0L);
    parse(SS,"d",dividestack,1);
    parse(SS,"s",stackbacksubtract,0);

    if(!(startstack<endstack)) return;
    if (dividestack<1) return;

    framesperstack=(int)(((float)(endstack-startstack))/dividestack);
    if (framesperstack<1) return;

    Fnum=startstack;
    fseek(stream,4100*sizeof(char)+(Fnum*TotSize*FrameElemSize),SEEK_SET);
    UserFrameCount=Fnum;

    stackon=1;
    stackinc=0;

    FrameStack=new float[(X_dim*Y_dim)+1];


    for (int cc9=0;cc9<(X_dim*Y_dim);cc9++) FrameStack[cc9]=0.0;


    return;


   }//end if parse stack

if (parse(SS,"c",startcontour,0L))
   {
    contouron=0;

    parse(SS,"e",endcontour,0L);
    parse(SS,"d",dividecontour,1);
    parse(SS,"s",contourbacksubtract,0);
    parse(SS,"n",newcontourframe,0);
    parse(SS,"o",overwritecontour,6);
    if(!(startcontour<endcontour)) return;
    if (dividecontour<1) return;

    framespercontour=(int)(((float)(endcontour-startcontour))/dividecontour);
    if (framespercontour<1) return;

    Fnum=startcontour;
    fseek(stream,4100*sizeof(char)+(Fnum*TotSize*FrameElemSize),SEEK_SET);
    UserFrameCount=Fnum;

    contouron=1;
    contourinc=0;
    nextcontour=0;
    FrameStack=new float[(X_dim*Y_dim)+1];


    for (int cc9=0;cc9<(X_dim*Y_dim);cc9++) FrameStack[cc9]=0.0;


    return;


   }//end if parse stack



}//end respond

template <class T>
class SubBox{
 public :

   int Xspan, Yspan; // general info on the frame
   int tX,tY,bX,bY;
   long SumBox(float);


   int operator<(SubBox& rhs)
    {return (itsBoxNumber< rhs.itsBoxNumber);}
   Boolean operator ==(SubBox& rhs)
    { return (Boolean)(itsBoxNumber == rhs.itsBoxNumber);}

   graphtext* gt;


   SubBox(int tx, int ty, int bx, int by, int XS, int YS, char* s);
   ~SubBox();
  int itsBoxNumber;
};

template <class T>
long SubBox<T>::SumBox(float Etime)
{
 float sum;
 sum=0;
 if(tX>bX) exit(1);
 if (tY>bY) exit(1);

 for (int cc1=tY; cc1<bY;cc1++)
  {
   for (int cc2=tX; cc2<bX;cc2++)
   {
     sum+=userframe[cc1*Xspan+cc2];
   }
  }

gt->addpoint(Etime, sum);
return sum;
}

template <class T>
SubBox<T>::SubBox(int TX, int TY, int BX,  int BY, int XS, int YS, char* name)
{

 if (BX<TX) {
	    int tmpx;
	    tmpx=BX; BX=TX; TX=tmpx;
	    }
 if (BY<TY) {
	    int tmpy;
	    tmpy=BY; BY=TY; TY=tmpy;
	    }
 tX=TX; tY=TY;  bX=BX; bY=BY; Xspan= XS; Yspan=YS;




 strstream buf;
 buf <<name<<"Z"<<BXNum<<ends;

  gt= new graphtext(TOPGRAPHX,TOPGRAPHY,BOTGRAPHX,BOTGRAPHY,60,0.2,0.2, all, buf.str(), 30);
  if  (!BXNum) LeadGraph=gt; // force x-scaling to match this graphs
  gt->NumberToScale=30;
  gt->pointstyle=Line;
  gt->textmode=none;
  gt->rm=keepYrange;
  gt->curvecol=COLINDEX;
  gt->UserYOffset=0;
  gt->UserYMult=1.0;
  gt->gout<<"#graph from "<<inname<<" pos(tx,ty,bx,by) "<<tX<<","<<tY<<","<<bX<<","<<bY<<" color code: "<<COLINDEX<<endl;
  BXNum++;  COLINDEX++; if (COLINDEX==14) COLINDEX=1;
  if ((COLINDEX==6)||(COLINDEX==7)||(COLINDEX==8))COLINDEX=9;
  itsBoxNumber=BXNum;
  gt->ScaleBarTimePerPoint=tBetweenFrames;
  gt->DrawScaleBar();
//  setfillstyle(SOLID_FILL,BLACK)
//   bar (TOPGRAPHX,BOTGRAPHY,BOTGRAPHX,BOTGRAPHY+20);


}

/*
class designed to 1) allow placement of one box, which contains
			several regions inside it. The reagions are
			summed and saved.
Method. Takes one SumBox already defined, and uses its tx,bx,ty,by
vals. Initialization then requires entry of a patter code-> Allow
Circular, Grid, etc.
Once defined, creates array of points that it runs through
At end of display frame, if initialized, runs through internal list
of regions
*/
/*
enum MultiBoxType{Oval,Array};

class Boundary{
public:
 int  tx,ty,bx,by;
 Boundary();
 Boundary(int, int, int, int);
};


Boundary::Boundary(){}

Boundary::Boundary(int Tx, int Ty, int Bx, int By)
{ tx=Tx; ty=Ty; bx=Bx; by= By;}



class MultiBox
{

public:
 Boundary* BA; //boundary array;
 int Number;  //Number of Boundaries held;
 int Xspan;
 int Yspan;
 void SumAllBoundarys(float Etime);

 MultiBox(int tx, int ty, int bx, int by, int xspan,int yspan, MultiBoxType Type, int Arg=1); //some types may need extra input?
 ~MultiBox();
 void DrawMultiBox();

 ofstream Mout;

};

void MultiBox::DrawMultiBox()
{
 for (int cc=0;cc<Number;cc++)
 {setcolor(WHITE);

  outline(BA[cc].tx, BA[cc].ty, BA[cc].bx, BA[cc].by);
 }
 getch;
 for ( cc=0;cc<Number;cc++)
 {setcolor(BLACK);

  outline(BA[cc].tx, BA[cc].ty, BA[cc].bx, BA[cc].by);
 }
}

MultiBox::~MultiBox()
{delete[]BA;}

void MultiBox::SumAllBoundarys(float Etime)
{
 float sum;
 Mout<<Etime;
 for (int cc=0;cc<Number;cc++)
  {
  sum=0;
  for (int cc1=(BA[cc].ty); cc1<(BA[cc].by);cc1++)
  {
   for (int cc2=(BA[cc].tx); cc2<(BA[cc].bx);cc2++)
   {
     sum+=userframe[cc1*Xspan+cc2];
   }
  }
   Mout<<" "<<sum;
 }
 Mout<<endl;
}

MultiBox::MultiBox(int tx, int ty, int bx, int by, int xsp, int ysp,MultiBoxType Type, int Arg)
{

 Mout.open("Multi.dat");
 Xspan=xsp;
 Yspan=ysp;
 switch(Type)
  {
   case Oval:
    {
     BA=new Boundary[9];
     Number=9;
     BA[0].tx=tx;
     BA[0].ty=ty;
     BA[0].bx=bx;
     BA[0].by=by;

     BA[1].tx= (bx-tx)/2 -Arg;
     BA[1].bx= (bx-tx)/2+Arg;
     BA[1].ty= ty;
     BA[1].by= ty+2*Arg;

     BA[2].tx= (tx+(bx-tx)/5) -Arg;
     BA[2].bx= (tx+(bx-tx)/5) +Arg;
     BA[2].ty= (ty+(by-ty)/5) -Arg;
     BA[2].by= (ty+(by-ty)/5) +Arg;

     BA[3].tx= tx;
     BA[3].bx= tx+2*Arg;
     BA[3].ty= (by-ty)/2 -Arg;
     BA[3].ty= (by-ty)/2 +Arg;

     BA[4].tx= (tx+(bx-tx)/5) -Arg;
     BA[4].bx= (tx+(bx-tx)/5) +Arg;
     BA[4].ty= (by-(by-ty)/5) -Arg;
     BA[4].by= (by-(by-ty)/5) +Arg;

     BA[5].tx= (bx-tx)/2 -Arg;
     BA[5].bx= (bx-tx)/2 +Arg;
     BA[5].ty= by-2*Arg;
     BA[5].ty= by;


     BA[6].tx= (bx-(bx-tx)/5) -Arg;
     BA[6].bx= (bx-(bx-tx)/5) +Arg;
     BA[6].ty= (by-(by-ty)/5) -Arg;
     BA[6].by= (by-(by-ty)/5) +Arg;

     BA[7].tx= bx-2*Arg;
     BA[7].bx= bx;
     BA[7].ty= (by-ty)/2 -Arg;
     BA[7].ty= (by-ty)/2 +Arg;


     BA[8].tx= (bx-(bx-tx)/5) -Arg;
     BA[8].bx= (bx-(bx-tx)/5) +Arg;
     BA[8].ty= (ty+(by-ty)/5) -Arg;
     BA[8].by= (ty+(by-ty)/5) +Arg;





    break;
    }

}
}
*/

template <class T>
SubBox<T>::~SubBox(){
  delete gt;

  }

void mem_warn()
{
 //
 closegraph();
 cout<<"no more memory!"<<endl;
 exit(1);
}

void draw_arrow(int x, int y)
{  x=x+5; y=y+5;
   /* draw an arrow on the screen */
   moveto(x, y);
   setcolor(GREEN);
   circle(x,y,5);
   setcolor(LIGHTRED);
   circle(x,y,3);
   setcolor(WHITE);
   line(x-5,y-5,x+5,y+5);
   line(x+5,y-5,x-5,y+5);
   setcolor(BLACK);
   line(x,y-5,x,y+5);
   line(x-5,y,x+5,y);

}

int LastMouseX, LastMouseY, MouseX, MouseY;
int LEFTMOUSEON=0;
int RIGHTMOUSEON=0;
int PAUSEON=0;
int FULLDISPLAY=1;   //display only image
int SaveAsBPM=0;
int StackFrames=0;
int WriteStackFrame=0;

SubBox<int>* pMM;
float tBetweenFrames=20.0;

int AdjustContrast=0;
int SaveCollapsedFrame=0;
int ReverseVideo=0;

void main(int argc,char *argv[])
{
	  int  Gd = DETECT, Gm;
	  int  Drv;
	  char GrErr,ch;
	  int cc1;
	  char buffer[80];

	struct time t;
	char* str;
	//MultiBox* multi;
	char lastchar;
	int KEYHITTAG=0;
	union REGS orgs,irgs;
	   void *arrow;
	   int x, y, maxx;
	   unsigned int size;


	char header[1026];
//	int  X_dim;
	int  temp_y_dim;
	unsigned long Y_dimension;
//	int  Y_dim;
	unsigned long  Num_frames;
	int  datatype;       /*  108  experiment datatype             */
			     /*         0 =   FLOATING POINT          */
			     /*         1 =   LONG INTEGER            */
			     /*         2 =   INTEGER                 */
			     /*         3 =   UNSIGNED INTEGER        */

	int Max=0;
	int Min=5000;
	int CP=0;
	int cellx=1;
	int celly=1;
	long cc4=4;
	float YSTRETCH;
	int BackGroundSubtract=0;
	int AverageNeighbour=0;
	int DeltaCA=0;
	char* basename="123456789";
	int BMPinc=0;
	int Cinc=0;
	int ColFrameCount=0;
	int SumLine=0;
	int UserOffSet;
	float GreyStretch;
	int BackSubTag;
	int SmoothTag;
	int NormTag;
	char CommandChar=']';

	float* UserScale;

	NormalizeAllXaxis=true; // this is a global var in graphnew.h

	str=argstostr(argc,argv);

	 parse(str,"-in",inname,"st_in.txt");
	 parse(str,"-ystretch",YSTRETCH,1);
	 parse(str,"-t", tBetweenFrames,50.0);

	 parse(str,"-Of",UserOffSet,0);
	 parse(str,"-St",GreyStretch,6.0);

	 parse(str,"-Bg",BackSubTag,0);
	 parse(str,"-Sm",SmoothTag,0);
	 parse(str,"-Nm",NormTag,0);


	if ((stream = fopen(inname,"rb"))==NULL)
	 { cout<< "no file present"<<endl; exit(1);}

	//determine the letters in inname before the '.'
	{int cc1=0;
	 int cc2=0;
	 char achar=' ';
	 //format "d:\name" or "d:name"
	 char* p =" ";
	 while(p=strstr(inname,"\\")) //advance to last \ ie d:\dir1\dir2\filename
	     {if (p) inname=&p[1];}
	 p=strstr(inname,":");
	  if (p) inname=&p[1];
	 while  ((achar!='.')&&(achar!='\x0'))
	 {
	  achar=inname[cc1];
	  basename[cc1]=achar;
	  cc1++;
	 }

	 basename[cc1]='\x0';
	}

/*reading in the header on the spe file. */

	fread(header,sizeof(char),1025,stream);

	X_dim = (int)header[42];

	temp_y_dim  = (int)header[34];
	if( temp_y_dim <0 )
	    Y_dimension = (long)header[664];
	else
	    Y_dimension = (long)temp_y_dim;

	if (Y_dimension<1) Y_dimension=1;
	Y_dim = (int)header[656];
	Num_frames   = (unsigned long)Y_dimension/Y_dim;
	datatype = (int)header[108];
	framedata=datatype;
	cout <<"time between frames : "<<(int)header[3]<<endl;
	cout <<"datatype : "<< datatype <<endl;
	cout <<"X dim : " <<X_dim <<endl;
	cout <<"Y dim max : "<< Y_dimension <<endl;
	cout <<"Y dim "<< Y_dim <<endl;
	cout <<"Num_frames "<< Y_dimension/Y_dim<<endl;
	for (cc1 = 0;cc1 < 3; cc1++)
	 fread(header,sizeof(char),1025,stream);



   set_new_handler(mem_warn);

   doskey_vga inp1(cout.rdbuf(),"input.txt"); //gio.h



  installuserdriver("Svga256",DetectVGA256);

  initgraph(&Gd,&Gm,"");






  GrErr = graphresult();
  if (GrErr != grOk) {
      printf("Graphics error: %s\n",grapherrormsg(GrErr));
      exit(1);
  }


  // blank screen

  setfillstyle(SOLID_FILL,BLACK);
  bar(1,1,639,479);

  //mouse reset
 irgs.x.ax=0;
 int86(0x33,&irgs,&orgs);

   x=100;y=100;

   /* draw the image to be grabbed */
   draw_arrow(x, y);
   /* calculate the size of the image */
   size = imagesize(x, y-ARROW_SIZE, x+(ARROW_SIZE), y+ARROW_SIZE);

   /* allocate memory to hold the image */
   arrow = malloc(size);

   /* grab the image */
   getimage(x, y-ARROW_SIZE, x+(ARROW_SIZE), y+ARROW_SIZE, arrow);


  setfillstyle(SOLID_FILL,BLACK);
  bar(1,1,639,479);



  inp1.setup();

 setviewport(0,0,639,479,1);

int XSCALE=(int)(400/X_dim);
 if (XSCALE<1) XSCALE=1;
int XBOXSIZE=XSCALE-2;
 if (XBOXSIZE<1) XBOXSIZE=1;


int YSCALE=XSCALE*YSTRETCH;
if (YSCALE>(int)(300/Y_dim)) YSCALE=(int)(300/Y_dim);
 if (YSCALE<1) YSCALE=1;
int YBOXSIZE=YSCALE-2;
 if (YBOXSIZE<1) YBOXSIZE=1;



List< SubBox<int> >* sb= new List< SubBox<int> >;



		inp1.letters=BLACK;
		inp1.background=WHITE;
		inp1.bold=LIGHTBLUE;
		inp1.oldtext=20;
		inp1.titlebackground=LIGHTBLUE;
		inp1.titleletters=WHITE;

		inp1.setxy(1,Y_dim*YSCALE+5,398,45);

	 inp1.settitle("Main I/O\n");

	 inp1.drawwin();
	 textwindow stout(1,Y_dim*YSCALE+53,198,Y_dim*YSCALE+53+100,WHITE,BLACK,"general info\x0");
	 textwindow mout (200,Y_dim*YSCALE+53,398,Y_dim*YSCALE+53+100,WHITE,BLACK,"mouse info\x0");
	 stout.show();
	 mout.show();
	 mout.textmode=oneonly;
	 stout.textmode=oneonly;

       // read in 1st frame
       int lastdif=0;
       int max=0;
       int min=1000;
       float Strch=GreyStretch;
       int Ofs=UserOffSet;
       int del=1;
       int totsize;
       totsize=X_dim*Y_dim;


frame<int>* Frame = new frame<int>(totsize);
LastF = new int[totsize+1];

LastLastF= new int[totsize+1];

userframe=new int[totsize+1];
userbackground=new float[totsize+1];

int ValueAtPoint;
Frame->loadframe();

MaxMinFrame(Max,Min,Frame,totsize);

SubBox<int>* sbs=NULL;

ofstream oftemp("P000.pbm");
ofstream ofline("L1.pgm");
ofstream ofstack("S000.dat");
ofstream ofcontour("c0.dat");


if (!ofline) {exit(1);}
if (!oftemp) {exit(1);}

 oftemp<<"P2"<<endl;
 oftemp<<"# generated by gb from" <<inname<<" frame: notknown"<<endl;
 oftemp<<X_dim<<" "<<Y_dim<<endl;
 oftemp<<"255"<<endl;

 for (int cc9=0;cc9<21;cc9++) Frame->loadframe();
 UserFrameCount=21;

    for( cc3=0;cc3<50000;cc3++)
    {



	int ccn=0;
	int lastval=0;
	int col=0;
     if (PAUSEON){
		 cc3--;

		  }

     if (!PAUSEON)
     { UserFrameCount++;
       if(del>0)
       {	gettime(&t);
		while (t.ti_hund<del)gettime(&t);
       }

       //	fread(Frame,sizeof(long int), totsize,stream);
	//fread(Frame,sizeof(long int), totsize,stream);
	 Frame->loadframe();
	 if (SaveAsBPM)
	  {BMPinc++;
	   strstream fname;
	   fname<<"P";
	   if (BMPinc<10) fname<<"00";
	   if ((BMPinc>9)&&(BMPinc<100)) fname<<"0";
	   fname<<BMPinc<<".PBM"<<ends;
	   oftemp.close();
	   oftemp.open(fname.str());
	   if (!oftemp) exit(1);
	    oftemp<<"P2"<<endl;
	    oftemp<<"# generated by gb from " <<inname<<" frame: "<<UserFrameCount<<" "<<BMPinc<<endl;
	    oftemp<<X_dim<<" "<<Y_dim<<endl;
	    oftemp<<"255"<<endl;
	  }




	 if (stackon)
	  {stackinc++;

	   if (stackinc>framesperstack)
	    {
	      //write out old data
	      int cc13=0;
	      for (int cc11=0;cc11<Y_dim;cc11++)
	       {
		for (int cc12=0;cc12<X_dim;cc12++)
		 {

		  ofstack<<cc12<<" "<<cc11<<" "<<FrameStack[cc13]<<"\n";
		  cc13++;
		  }
		 ofstack<<"\n";
		}


	      //start a new file
	      ofstack.close();

	      for (int cc9=0;cc9<(X_dim*Y_dim);cc9++) FrameStack[cc9]=0.0;

	      stackinc=0;
	      strstream fname;
	      fname<<"S";



	      if (stackinc<10) fname<<"00";
	      if ((stackinc>9)&&(stackinc<100)) fname <<"0";

	      fname<<stackfilenum<<".dat"<<ends;

	       stackfilenum++;

	      ofstack.open(fname.str());
	      if (!ofstack) exit(1);

	      ofstack<<"# generated by gb from "<<inname<<" frame: "<<UserFrameCount<<"\n";

	      ofstack<<"# start "<<startstack<<", ends"<<endstack<<", inc"<<stackinc<<", frames per"<<framesperstack<<"\n";

	      if (UserFrameCount>endstack+1){stackon=0;}

	      if (stackbacksubtract)   BackSubTag=1;


	     }

	    }//end of if stackon

	 if (contouron)
	  {contourinc++;
	   nextcontour++;
	   if (nextcontour>framespercontour)
	    {
	      //write out old data
	      int cc13=0;
	      for (int cc11=0;cc11<Y_dim;cc11++)
	       {
		for (int cc12=0;cc12<X_dim;cc12++)
		 {

		  ofcontour<<cc12<<" "<<cc11<<" "<<FrameStack[cc13]<<"\n";
		  cc13++;
		  }
		 ofcontour<<"\n";
		}


	      //start a new file
	      ofcontour.close();
	      if (newcontourframe)
	      {
	      for (int cc9=0;cc9<(X_dim*Y_dim);cc9++) FrameStack[cc9]=0.0;
	      }
	      //contourinc=0;
	      nextcontour=0;
	      strstream fname;
	      fname<<"C";


	     // if (contourinc<10) fname<<"00";
	     // if ((contourinc>9)&&(contourinc<100)) fname <<"0";

	      fname<<contourfilenum<<".dat"<<ends;

	       contourfilenum++;

	      ofcontour.open(fname.str());
	      if (!ofcontour) exit(1);

	      ofcontour<<"# generated by gb from "<<inname<<" frame: "<<UserFrameCount<<"\n";

	      ofcontour<<"# start "<<startcontour<<", ends"<<endcontour<<", inc "<<contourinc<<", frames per"<<framespercontour<<"\n";

	      if (UserFrameCount>endcontour+1){stackon=0;}

	      if (stackbacksubtract)   BackSubTag=1;


	     }

	    }//end of if stackon



	 if (SaveCollapsedFrame)

	 {
	  if (ColFrameCount>99)
	   {Cinc++;
	    strstream fname;
	    fname<<"l"<<Cinc<<".pgm"<<ends;
	    ofline.close();
	    ofline.open(fname.str());
	     if (!ofline) exit(1);
	     ofline<<"P2"<<endl;
	     ofline<<"#generated by gb from "<<inname<<" frame"<<UserFrameCount<<" "<<Cinc<<endl;
	     ofline<<Y_dim<<" 100"<<endl;
	     ofline<<"255"<<endl;
	     ColFrameCount=0;
	   }
	   else ColFrameCount++;
	  }

	//if (*Frame==EOF) exit(1);
       }




       if (NormTag)  {NormTag=0;CommandChar='v';}
	else
       if (SmoothTag){SmoothTag=0;CommandChar='/';}
	else
       if (BackSubTag)  {BackSubTag=0;CommandChar='b';}


	if ((kbhit())||(CommandChar!=']'))
		 { char  ch = getch();
		     if (CommandChar!=']')
		     {ch=CommandChar;
		      CommandChar=']';
		     }
			if (ch=='z') Ofs-=2;
		   else if (ch=='a') Ofs+=2;
		   else if (ch=='s') {Strch+=0.1; }
		   else if (ch=='x') {Strch-=0.1;}
		   else if (ch=='q') cc3=50000;
		   else if (ch=='m') MaxMinFrame(Max,Min,Frame,totsize);
		   else if (ch=='.') del-=1;
		   else if (ch==',') del+=1;
		   else if (ch=='c') CP-=16;
		   else if (ch=='d') CP+=16;
		   else if (ch=='e') {inp1.user_in();
				      respond(stream,inp1.s,totsize);

				      {
				       SubBox<int>* pM;
					sb->ResetFindNext();

					do
					{
					pM=sb->FindNext();
					if (pM)
					 {
					 pM->gt->ResetOutStream();
					 pM->gt->ResetXYBuffer();
					 pM->gt->gout<<"#graph from "<<inname<<" pos(tx,ty,bx,by) "<<pM->tX<<","<<pM->tY<<","<<pM->bX<<","<<pM->bY<<" color code: "<<COLINDEX<<endl;
					 }

					}
					while (pM);
				       }



				      }
		   else if (ch=='r') {
		     sbs->gt->NumberToScale+=sbs->gt->NumberToScale;
		     sbs->gt->drawlines(0,WHITE);
		     sbs->gt->NumberToScale*=0.5;
		     sbs->gt->UserYOffset-=3;
		     sbs->gt->drawlines(0,sbs->gt->curvecol);
		     }

		   else if (ch=='f')
		     {
		     sbs->gt->NumberToScale+=sbs->gt->NumberToScale;
		     sbs->gt->drawlines(0,WHITE);
		     sbs->gt->NumberToScale*=0.5;
		     sbs->gt->UserYOffset+=3;
		     sbs->gt->drawlines(0,sbs->gt->curvecol);
		     }

		   else if (ch=='k')
		    {//toggle reversevideo
		     if (ReverseVideo==1) ReverseVideo=0;
		      else ReverseVideo = 1;
		     }

		   else if (ch=='t')
		     {
		     sbs->gt->NumberToScale+=sbs->gt->NumberToScale;
		     sbs->gt->drawlines(0,WHITE);
		     sbs->gt->NumberToScale*=0.5;
		     sbs->gt->UserYMult+=0.1;
		     sbs->gt->calcscales();
		     sbs->gt->drawlines(0,sbs->gt->curvecol);
		     }
		   else if (ch=='g')
		     {
		     sbs->gt->NumberToScale+=sbs->gt->NumberToScale;
		     sbs->gt->drawlines(0,WHITE);
		     sbs->gt->NumberToScale*=0.5;
		     sbs->gt->UserYMult-=0.1;
		     if (sbs->gt->UserYMult<0.0001) sbs->gt->UserYMult=0.1;
		     sbs->gt->calcscales();
		     sbs->gt->drawlines(0,sbs->gt->curvecol);
		     }
		   else if (ch=='y')
		     {
		     sbs->gt->NumberToScale+=sbs->gt->NumberToScale;
		     sbs->gt->drawlines(0,WHITE);
		     sbs->gt->NumberToScale*=0.5;
		     sbs->gt->UserYMult*=1.5;
		     sbs->gt->calcscales();

		     sbs->gt->drawlines(0,sbs->gt->curvecol);
		      sbs->gt->redraw();
		     }
		   else if (ch=='h')
		     {
		     sbs->gt->NumberToScale+=sbs->gt->NumberToScale;
		     sbs->gt->drawlines(0,WHITE);
		     sbs->gt->NumberToScale*=0.5;
		     sbs->gt->UserYMult*=0.75;
		     if (sbs->gt->UserYMult<0.0001) sbs->gt->UserYMult=0.1;
		     sbs->gt->redraw();
		     sbs->gt->drawlines(0,sbs->gt->curvecol);
		     }


		   else if (ch=='n')

		     { char achar='n';
		       do
			{
			sbs=sb->FindNext();
			if (sbs)
			 {//outline selected area

			  do
			 {

			  setcolor(BLACK);
			  outlinemore(sbs->tX*XSCALE-1,sbs->tY*YSCALE-1,sbs->bX*XSCALE-1,sbs->bY*YSCALE-1);

			  if (achar=='4')
			   {sbs->tX=sbs->tX-1; sbs->bX=sbs->bX-1;}
			  if (achar=='6')
			   {sbs->tX=sbs->tX+1; sbs->bX=sbs->bX+1;}
			  if (achar=='8')
			   {sbs->tY=sbs->tY-1; sbs->bY=sbs->bY-1;}
			  if (achar=='2')
			   {sbs->tY=sbs->tY+1; sbs->bY=sbs->bY+1;}

			  setcolor(sbs->gt->curvecol);
			  outlinemore(sbs->tX*XSCALE-1,sbs->tY*YSCALE-1,sbs->bX*XSCALE-1,sbs->bY*YSCALE-1);


				  achar=getch();
			  }while ((achar=='4')||(achar=='6')||(achar=='8')||(achar=='2'));





			  if (achar=='m')
			   {
			    //multi=new MultiBox(sbs->tX,sbs->tY,sbs->bX,sbs->bY,X_dim, Y_dim, Oval, 2);
			    //multi->DrawMultiBox();
			   }



			  if (!PAUSEON)
			  {
			   setcolor(BLACK);
			   outlinemore(sbs->tX*XSCALE-1,sbs->tY*YSCALE-1,sbs->bX*XSCALE-1,sbs->bY*YSCALE-1);
			   }
			 }
			}
		       while (achar!=' ');

		     }
		   else if (ch=='-')
		     { // remove this graph from the list
		     sbs->gt->NumberToScale+=sbs->gt->NumberToScale;
		     sbs->gt->drawlines(0,WHITE);
		     sbs->gt->NumberToScale*=0.5;
		     SubBox<int>* sbtemp=sbs;
		     setcolor(BLACK);
		     outlinemore(sbs->tX*XSCALE-1,sbs->tY*YSCALE-1,sbs->bX*XSCALE-1,sbs->bY*YSCALE-1);
		     sb->Remove(sbtemp);
		     sb->ResetFindNext();

		     sbs=sb->FindNext();
		     LeadGraph=sbs->gt;
		     sbs->gt->redraw();

		     }
		   else if (ch=='p')
		    {
		     if (PAUSEON) PAUSEON=0;
		      else
		     if (!PAUSEON) PAUSEON=1;
		    }
		    else if (ch=='o')
		     {
		      if (FULLDISPLAY) FULLDISPLAY=0;
			else
		      if (!FULLDISPLAY) FULLDISPLAY = 1;
		      }

		   else if (ch=='v')
		   {
		   fseek(stream,4100*sizeof(char)+((UserFrameCount-20)*totsize*FrameElemSize),SEEK_SET);

		   int* Max=new int[totsize];
		   int* Min=new int[totsize];



		   for (int cc6=0;cc6<totsize;cc6++)
		   { Max[cc6]=-1000;
		     Min[cc6]=1000;

		   }


		   for (int cc7=0;cc7<20;cc7++)
		   {
		    Frame->loadframe();

		    for (int cc6=0;cc6<totsize;cc6++)
		     {
		       if ((userframe[cc6]-userbackground[cc6])>Max[cc6]) Max[cc6]= (userframe[cc6]-userbackground[cc6]);
		       if ((userframe[cc6]-userbackground[cc6])<Min[cc6]) Min[cc6]= (userframe[cc6]-userbackground[cc6]);
		     }


		    }

		    for (cc6=0;cc6<totsize;cc6++)
		     Max[cc6]=Max[cc6]-Min[cc6];

		    delete[]Min;

		    UserScale=new float[totsize];


		    for (cc6=0;cc6<totsize;cc6++)
		     { if (Max[cc6]!=0) UserScale[cc6]=100/(Max[cc6]);
			else
			 UserScale[cc6]=1;
		     }



		    if (!AdjustContrast) AdjustContrast=1;
		     else AdjustContrast=0;

		   }
		   else if (ch=='3')
		   {
		    if (SaveCollapsedFrame) SaveCollapsedFrame=0;
		     else SaveCollapsedFrame=1;
		    }


		   else if (ch=='b')
		     { //copy userframe to userbackground

		       for (int cc6=0;cc6<totsize;cc6++)
			{userbackground[cc6]=0;}

		       //1_ jump back 20 frames

			fseek(stream,4100*sizeof(char)+((UserFrameCount-20)*totsize*FrameElemSize),SEEK_SET);

		for (int cc7=0;cc7<20;cc7++)
		{
		Frame->loadframe();


		 if (!AverageNeighbour)
		   {
			for (int cc6=0;cc6<totsize;cc6++)
			{
			userbackground[cc6]+=userframe[cc6];
			}
		   }
		   else
		   {//must use average routine
		    {int cc6=0;
		    for (int cc1=0;cc1<Y_dim;cc1++)
		    {
		     for (int cc2=0;cc2<X_dim;cc2++)
		     {
		     ValueAtPoint=0;
		     for (int yct=-1;yct<2;yct++)
		      {
			for (int xct=-1;xct<2;xct++)
			{
			int NY=(yct+cc1);
			int NX=(xct+cc2);
			if ((NX>0)&&(NX<X_dim)&&(NY>0)&&(NY<Y_dim))
			ValueAtPoint+=userframe[xct+cc2+((yct+cc1) *  X_dim)];
			else
			ValueAtPoint+=userframe[cc6];
			}
			}
			ValueAtPoint=ValueAtPoint/9;
			userbackground[cc6]+=ValueAtPoint;
			cc6++;
			}
		      }
		     }

		   }

		 }


		 for ( cc6=0;cc6<totsize;cc6++)
			{
			userbackground[cc6]=( (userbackground[cc6])/20);
			}




			BackGroundSubtract=1;

			Max=0;

		 }

		 else if (ch=='0')
		 {
		  if (DeltaCA) DeltaCA=0;
		   else DeltaCA=1;
		 }

		   else if (ch=='/')
		     {
		      if (AverageNeighbour) AverageNeighbour=0;
		       else AverageNeighbour=1;
		     }

		   else if (ch=='1')
		    {
		     if (SaveAsBPM) SaveAsBPM=0;
		      else SaveAsBPM=1;
		     }
		   else if (ch=='5')
		    {
		     if (StackFrames)
		      {
		       StackFrames=0;
		       ofstream ostack("stackfr.dat");
		       int cc13=0;
		       for (int cc11=0;cc11<Y_dim;cc11++)
			{
			 for (int cc12=0;cc12<X_dim;cc12++)
			  {

			   ostack<<cc12<<" "<<cc11<<" "<<FrameStack[cc13]<<"\n";
			   cc13++;
			  }
			  ostack<<"\n";
			 }
		       }
		     else
		      {
		       StackFrames=1;

		       FrameStack=new float[totsize+1];

		      for (int cc9=0;cc9<totsize;cc9++) FrameStack[cc9]=0.0;
		      }


		    }
		   else if (ch=='u')
			{ //inp1.user_in(' ');
			  //respond(inp1.s,array,Xdist,Ydist);
			int cn1x,cn2x,cn1y,cn2y;
			cn1x=-1;cn1y=-1;cn2x=-1;cn2y=-1; // reset the corner
			do
			 { ch=getch();
			   if (cn1x>-1)
			  { setcolor(BLACK);

			    outline(cn1x*XSCALE-1,cn1y*YSCALE-1,cellx*XSCALE-1,celly*YSCALE-1);
			    }
			  setfillstyle(SOLID_FILL,LastF[celly*Y_dim+cellx]);
			   bar(cellx*XSCALE,celly*YSCALE,cellx*XSCALE+XBOXSIZE,celly*YSCALE+YBOXSIZE);
			   if (ch=='4')
			    {if (cellx>0)cellx-=1;}
			   if (ch=='2')
			    {if (celly<Y_dim)celly+=1;}
			   if (ch=='8')
			    {if (celly>0)celly-=1;}
			   if (ch=='6')
			    {if (cellx<X_dim)cellx+=1;}
			    if (ch=='7')
			    {if (cellx>0)cellx-=1;if (celly>0)celly-=1;}
			   if (ch=='9')
			    {if (celly>0)celly-=1;if (cellx<X_dim)cellx+=1;}
			   if (ch=='1')
			    {if (celly<Y_dim)celly+=1;if (cellx>0)cellx-=1;}
			   if (ch=='3')
			    {if (cellx<X_dim)cellx+=1;if (celly<Y_dim)celly+=1;}

			  setfillstyle(SOLID_FILL,LastF[celly*Y_dim+cellx]+24);
			    bar(cellx*XSCALE,celly*YSCALE,cellx*XSCALE+XBOXSIZE,celly*YSCALE+YBOXSIZE);
			   if (cn1x>-1)
			  { setcolor(YELLOW);
			   outline(cn1x*XSCALE-1,cn1y*YSCALE-1,cellx*XSCALE-1,celly*YSCALE-1); }
			  if (ch=='0')
			  {  setcolor(BLACK);
			    outline(cn1x*XSCALE-1,cn1y*YSCALE-1,cellx*XSCALE-1,celly*YSCALE-1);
			    //set corner1
			    cn1x=cellx; cn1y=celly;
			  }
			  if (ch=='.')
			   {int tx,ty,bx,by;
			    if (cellx<cn1x){ tx=cellx;bx=cn1x;}
			     else {tx=cn1x;bx=cellx;}
			    if (celly<cn1y) {ty=celly;by=cn1y;}
			     else {ty=cn1y;by=celly;}
			     if (bx>X_dim) exit(0);
			     if (by>Y_dim) exit(0);

			    sbs=new SubBox <int>(tx,ty,bx,by, X_dim, Y_dim,basename);
			    sb->Insert(*sbs);
			   }


			 } while (ch!='5') ;
			  setcolor(BLACK);
			  outline(cn1x*XSCALE-1,cn1y*YSCALE-1,cellx*XSCALE-1,celly*YSCALE-1);

			}

		  if(ch=='e')
		   {
		    strstream buf;
		    buf<< "User Max : "<<Ofs<<" Strch : " <<Strch<<" fr: "<< UserFrameCount<<" delay :"<<del<<"c: "<<CP<<"\n"<< ends;
		   inp1<<buf.str();
		   }
		   setcolor(30);
		   //outtextxy(200,300,buf.str());
		   }

	irgs.x.ax=3;
	int86(0x33,&irgs,&orgs);
	if(orgs.x.bx==1) LEFTMOUSEON=1;
	if(orgs.x.bx==2) RIGHTMOUSEON=1;


	ccn=0;
    if (!PAUSEON)
    {
     if (FULLDISPLAY)
     {

    //used in contour averaging
    for (int cc20=0;cc20<totsize;cc20++) LastLastF[cc20]=LastF[20];

     for (cc1=0;cc1<Y_dim;cc1++)

	{
       for (int cc2=0;cc2<X_dim;cc2++)
	   {

	    //choose a color  using render topmost
       /*	    int N=(int)(Frame[ccn]-Max+Ofs)/Strch;
		    col=CP+N;    */




    if (AverageNeighbour)
     {ValueAtPoint=0;
      float tempval;
      for (int yct=-1;yct<2;yct++)
       {
       for (int xct=-1;xct<2;xct++)
	{
	 int NY=(yct+cc1);
	 int NX=(xct+cc2);
	 if ((NX>0)&&(NX<X_dim)&&(NY>0)&&(NY<Y_dim))
	 {

	  tempval=userframe[xct+cc2+((yct+cc1) *  X_dim)];

	  //if (!((xct==0)||(yct==0))) tempval*=0.707;

	  ValueAtPoint+=tempval;

	 }
	 else
	  ValueAtPoint+=userframe[ccn];
	}
       }
       ValueAtPoint=ValueAtPoint/9;
      }
     else
       ValueAtPoint=userframe[ccn];


    if (BackGroundSubtract)
     ValueAtPoint -= (int)userbackground[ccn];

    if(AdjustContrast)
     ValueAtPoint*=UserScale[ccn];

    if (ReverseVideo)
     ValueAtPoint=30-ValueAtPoint;

       if(CP<0)
       {
	if ( (ValueAtPoint<(Max- Ofs))) col=0;
	 else col=30;
	}
	else
	{
	     if (ValueAtPoint>(Max- Ofs - Strch  )) col=24;
	else if (ValueAtPoint>(Max- Ofs - Strch*2)) col=44+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*3)) col=68+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*4)) col=92+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*5)) col=31+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*6)) col=30+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*7)) col=29+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*8)) col=28+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*9)) col=27+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*10)) col=26+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*11)) col=25+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*12)) col=24+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*13)) col=23+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*14)) col=22+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*15)) col=21+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*16)) col=20+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*17)) col=19+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*18)) col=18+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*19)) col=17+CP;
	else if (ValueAtPoint>(Max- Ofs - Strch*20)) col=0;
	else if (ValueAtPoint>(Max- Ofs - Strch*21)) col=0;
	else if (ValueAtPoint>(Max- Ofs - Strch*22)) col=0;
	else if (ValueAtPoint>(Max- Ofs - Strch*23)) col=0;



	else col=BLACK;
	 }

	 //DEBUG!

	 if (SaveAsBPM)
	  {
	  int tf;
	  tf = col;
	  if (tf>31) tf=32;
	  tf=tf*8;
	  if (tf <1) tf=1;
	  if (tf >255) tf=255;
	  oftemp<<tf<<" ";
	  if (cc2==X_dim-2) oftemp<<endl;
	  }

	if (stackon)
	 {
	  FrameStack[ccn]+=((float)ValueAtPoint);

	 }




	if (SaveCollapsedFrame)
	 {
	  if (cc2==0) //new xline
	   {
	    ofline<<(SumLine/X_dim)<<"\n";
	    SumLine=0;
	   }
	  else
	    {int tf=2*(-1*Ofs+ValueAtPoint);
	     if (tf<1) tf=1;
	     if (tf>255) tf=255;
	     SumLine+=tf;
	    }
	 }



	    if (LastF[ccn]!=col) {
				   setfillstyle(SOLID_FILL,col);
				   bar(cc2*XSCALE,cc1*YSCALE,cc2*XSCALE+XBOXSIZE,cc1*YSCALE+YBOXSIZE);
				   LastF[ccn]=col;
				   }


	    ccn++;

	    }
	  }

	 if (contouron)
	  {//assumes col is either 0 or 30 (B/W)
	   //lastF is updated with either 0 or 30, run through each point,
	   //if it has a neighbour, mark tag true, and update FrameStack.
	   ccn=0;
	   int prevtag=0;
	   for (int cc10=0;cc10<Y_dim;cc10++)
	    {
	     for (int cc11=0;cc11<X_dim;cc11++)
	      {
	       //if (LastF[ccn]>0)
	       {int ValueAtPoint;
		ValueAtPoint=0;
		prevtag=0;
		for (int yct=-1;yct<2;yct++)
		{
		 for (int xct=-1;xct<2;xct++)
		  {
		   int NY=(yct+cc10);
		   int NX=(xct+cc11);
		   if ((NX>0)&&(NX<X_dim)&&(NY>0)&&(NY<Y_dim))
		    {
		    if (LastF[xct+cc11+((yct+cc10) *  X_dim)]>0) ValueAtPoint+=1;
		    if (LastLastF[xct+cc11+((yct+cc10) *  X_dim)]>0) prevtag=1;

		    }

		  }
		 }

	     //  if (prevtag)
		{
		 int mod;
		 mod =contourinc-overwritecontour;
		 if (mod <0) mod+=framespercontour;

		if (((FrameStack[ccn])<(mod))||(FrameStack[ccn]==0))
		 {
		  if ((ValueAtPoint>1)&&(LastF[ccn]>0)) {FrameStack[ccn]=contourinc;}
		  if ((ValueAtPoint>3))  {FrameStack[ccn]=contourinc;}
		 }
		// if (ValueAtPoint>2) LastF[ccn]=10;

	       }

	      }
	      ccn++;
	      }//cc11

	     }//cc10



	  }//if contouron


	 }
	if (!FULLDISPLAY)
	{



	 sb->ResetFindNext();
	do
	 {
	   pMM=sb->FindNext();
	   if (pMM)
	    {
	     float val = pMM->gt->Ylast+pMM->gt->UserYOffset;
	     val = (1-(val/(BOTGRAPHY - TOPGRAPHY)))*Strch +16+Ofs;
	     setfillstyle(SOLID_FILL, (int)val);

	     bar(pMM->tX*XSCALE-1,pMM->tY*YSCALE-1,pMM->bX*XSCALE-1,pMM->bY*YSCALE-1);
	    }
	 }
	while (pMM);



	}
	t.ti_hour=0;
	t.ti_min=0;
	t.ti_sec=0;
	t.ti_hund=0;
	settime(&t);


       if (cc3>cc4)
	{
	buffer[0]='\x0';
	sprintf(buffer,"\n frame: %ld\n offset %d strch %f\n delay %d\n",UserFrameCount,Ofs,Strch,del);
	stout.display(buffer);
	cc4+=5 ;
	}

    if (DeltaCA)
     {
      for (int ccz=0;ccz<totsize;ccz++)
       userbackground[ccz]=userframe[ccz];
      }





    }  //end if pauseon


SubBox<int> * tmp;
int Yoff;
Yoff=0;
char buffer[80];
buffer[0]='\x0';

if ((MouseX>TOPGRAPHX)&&(MouseX<BOTGRAPHX)&&(MouseY>TOPGRAPHY)&&(MouseY<BOTGRAPHY)&&(LEFTMOUSEON))
    { do
      {
	irgs.x.ax=3;
	int86(0x33,&irgs,&orgs);
	MouseX=orgs.x.cx;
	MouseY=orgs.x.dx;

	putimage(LastMouseX, LastMouseY, arrow, XOR_PUT);


	LastMouseX=MouseX;LastMouseY=MouseY;

       if (orgs.x.bx==1)
	{
       SubBox<int>* pM;
       sb->ResetFindNext();
       do
	{
	  pM=sb->FindNext();
	  if (pM)
	   {
	    setcolor(pM->gt->curvecol);
	    outlinemore(pM->tX*XSCALE-1,pM->tY*YSCALE-1,pM->bX*XSCALE-1,pM->bY*YSCALE-1);
	   }
	}
       while (pM);




	 LEFTMOUSEON=0;
	 int tcol=-1;
	 for (int cc3=0;cc3<5;cc3++)
	 {for (int cc4=0;cc4<5;cc4++)
	  {

	  int tc=getpixel(MouseX+cc3+10,MouseY+cc4+10);

	  if (tc!=WHITE) tcol=tc;
	  }
	 }

	 if (tcol>0)
	 { setfillstyle(SOLID_FILL,tcol);
		       bar(500,1,550,20);


       sb->ResetFindNext();

       do
	{
	  pM=sb->FindNext();
	  if (pM)
	   {
	    if (pM->gt->curvecol==tcol) {tmp=pM;Yoff=MouseY;break;}
	   }
	}
	  while (pM);
	}//end if (tcol>0)

	}
	putimage(MouseX, MouseY, arrow, XOR_PUT);
      }while (orgs.x.bx!=2);

       SubBox<int>* pM;
       sb->ResetFindNext();
       do
	{
	  pM=sb->FindNext();
	  if (pM)
	   {
	    setcolor(BLACK);
	    outlinemore(pM->tX*XSCALE-1,pM->tY*YSCALE-1,pM->bX*XSCALE-1,pM->bY*YSCALE-1);
	   }
	}
       while (pM);


      if (tmp){
       sbs=tmp;
       sbs->gt->NumberToScale+=sbs->gt->NumberToScale;
       sbs->gt->drawlines(0,WHITE);
       sbs->gt->NumberToScale*=0.5;
       sbs->gt->UserYOffset+=MouseY-Yoff;
       sbs->gt->drawlines(0,sbs->gt->curvecol);
       }

     }


      setfillstyle(SOLID_FILL,WHITE);
		       bar(498,0,552,21);

      {

	irgs.x.ax=3;
	int86(0x33,&irgs,&orgs);

	MouseX=orgs.x.cx;
	MouseY=orgs.x.dx;
       if ((LastMouseX!=MouseX)||(LastMouseY!=MouseY))
       {



	/* erase old image */
      if((LastMouseX>=X_dim*XSCALE)||(LastMouseY>=Y_dim*YSCALE))
       {putimage(LastMouseX, LastMouseY, arrow, XOR_PUT);}
      else
       {
       int cc2=LastMouseX/XSCALE;
       int cc1=LastMouseY/YSCALE;
       setfillstyle(SOLID_FILL,LastF[cc1*X_dim+cc2]);
       bar(cc2*XSCALE,cc1*YSCALE,cc2*XSCALE+XBOXSIZE,cc1*YSCALE+YBOXSIZE);

       }

      if ((MouseX<X_dim*XSCALE)&&(MouseY<Y_dim*YSCALE))
      {

       setfillstyle(SOLID_FILL,LIGHTBLUE);
       int cc2=MouseX/XSCALE;
       int cc1=MouseY/YSCALE;
       bar(cc2*XSCALE,cc1*YSCALE,cc2*XSCALE+XBOXSIZE,cc1*YSCALE+YBOXSIZE);
      buffer[0]='\x0';
      sprintf(buffer,"\nmouse:\nx: %d y: %d\n cell x: %d y: %d val %d\n",orgs.x.cx,orgs.x.dx,cc2,cc1,userframe[cc1*X_dim+cc2]);
      mout.display(buffer);
      }
       else
      {
	buffer[0]='\x0';
	sprintf(buffer,"\nmouse:\nx: %d y: %d\n",orgs.x.cx,orgs.x.dx);
	mout.display(buffer);


       putimage(MouseX, MouseY, arrow, XOR_PUT);
      }




	if ((LEFTMOUSEON)&&(MouseX<X_dim*XSCALE)&&(MouseY<Y_dim*YSCALE))
	{
	 KEYHITTAG=0;
	 LEFTMOUSEON=0;
	 setcolor(LIGHTBLUE);
	 int tx=MouseX/XSCALE;
	 int ty=MouseY/YSCALE;
	 int bx=tx;
	 int by=ty;
	 buffer[0]='\x0';

	 do
	 {

	setcolor(BLACK);
	outline(tx*XSCALE-1,ty*YSCALE-1,bx*XSCALE-1,by*YSCALE-1);

	LastMouseX=MouseX; LastMouseY=MouseY;
	irgs.x.ax=3;
	int86(0x33,&irgs,&orgs);

	if (orgs.x.bx==3)
       {
	tx=MouseX/XSCALE;
	ty=MouseY/YSCALE;
       }
	MouseX=orgs.x.cx;
	MouseY=orgs.x.dx;

       int cc2=LastMouseX/XSCALE;
       int cc1=LastMouseY/YSCALE;
       setfillstyle(SOLID_FILL,LastF[cc1*X_dim+cc2]);
       bar(cc2*XSCALE,cc1*YSCALE,cc2*XSCALE+XBOXSIZE,cc1*YSCALE+YBOXSIZE);
       setfillstyle(SOLID_FILL,LIGHTGREEN);
	cc2=MouseX/XSCALE;
	cc1=MouseY/YSCALE;
	if (cc1>Y_dim-1) {cc1=Y_dim; MouseY=cc1*XSCALE;}
	if (cc2>X_dim-1) {cc2=X_dim; MouseX=cc2*YSCALE;}
	bx=cc2;
	by=cc1;
	setcolor(YELLOW);
	outline(tx*XSCALE-1,ty*YSCALE-1,bx*XSCALE-1,by*YSCALE-1);

       bar(cc2*XSCALE,cc1*YSCALE,cc2*XSCALE+XBOXSIZE,cc1*YSCALE+YBOXSIZE);

	if (kbhit()){ lastchar=getch();
		    KEYHITTAG=1;
		    }

	 }while((orgs.x.bx!=2)&&(!KEYHITTAG));
	 setcolor(BLACK);
	 outline(tx*XSCALE-1,ty*YSCALE-1,bx*XSCALE-1,by*YSCALE-1);

	 if (KEYHITTAG)
	  {
	   KEYHITTAG=0;

	    {int anum;
	     anum=(int)lastchar -48;
	     sbs=new SubBox<int>(bx-anum,by-anum,bx+anum,by+anum,X_dim, Y_dim, basename);
	    }
	   }

	  else

	  sbs=new SubBox <int>(tx,ty,bx,by, X_dim, Y_dim, basename);

	  sb->Insert(*sbs);

      buffer[0]='\x0';
      sprintf(buffer,"\nBox :\n tx: %d ty: %d\n bx: %d by %d\n",tx,ty,bx,by);
      stout.display(buffer);

	}

       }
      LastMouseX=MouseX; LastMouseY=MouseY;
      }

      if (!PAUSEON)
      {
       SubBox<int>* pM;
       sb->ResetFindNext();

       do
	{
	  pM=sb->FindNext();
	  if (pM) pM->SumBox((float)UserFrameCount);
	}
	  while (pM);
      }

     // if (sbs!=NULL){err2<<"trying!\n"; sbs->SumBox((float)cc3);}

       }


       //	getch();

	fclose(stream);

	closegraph();
     }



