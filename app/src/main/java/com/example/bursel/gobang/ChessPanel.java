package com.example.bursel.gobang;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

//该应用借鉴于https://www.cnblogs.com/jaycelgb/p/5721661.html 原作者：耕田的老牛
public class ChessPanel extends View {
    private int myPanelWidth;
    private float myLineHeight;
    private int maxLine=10;

    private Paint myPaint;          //画笔
    private Bitmap myWhitePice;
    private Bitmap myBlackPice;
    private float ratioPieceOfLineHeight=3*1.0f/4;  //棋子为行宽的3/4

    private boolean isGameOver;
    public static int WHITE_WIN=0;
    public static int BLACK_WIN=1;
    private boolean isWhite=true;          //判断是否白棋先手

    private List<Point> myWhiteArray=new ArrayList<Point>();       //白棋的位置信息
    private List<Point> myBlackArray=new ArrayList<Point>();       //黑棋的位置信息

    private onGameListener onGameListener;
    private int mUnder;                             //dialog的Y坐标

    public ChessPanel(Context context){
        this(context,null);
    }

    public ChessPanel(Context context, AttributeSet attributeSet){
        super(context , attributeSet);
        init();
    }

    //用于回调的接口
    public interface onGameListener{
        void onGameOver(int i);
    }

    //自定义接口用于显示dialog接口
    public void setOnGameListener(ChessPanel.onGameListener onGameListener){
        this.onGameListener=onGameListener;
    }

    //初始化函数
    private void init(){
        myPaint=new Paint();
        myPaint.setColor(0X44ff0000);
        myPaint.setAntiAlias(true);
        myPaint.setDither(true);
        myPaint.setStyle(Paint.Style.STROKE);

        myWhitePice=BitmapFactory.decodeResource(getResources(),R.drawable.bz);
        myBlackPice=BitmapFactory.decodeResource(getResources(),R.drawable.hz);
    }

    //触发事件
    public boolean onTouchEvent(MotionEvent event){
        if(isGameOver){
            return false;
        }

        int action=event.getAction();
        if(action==MotionEvent.ACTION_UP){                         //单点触摸离开事件
            int x=(int)event.getX();
            int y=(int)event.getY();
            Point p=getValidPoint(x,y);

            if(myWhiteArray.contains(p)||myBlackArray.contains(p)){
                return false;
            }

            if(isWhite){
                myWhiteArray.add(p);
            }
            else{
                myBlackArray.add(p);
            }
            invalidate();                     //刷新View
            isWhite=!isWhite;
        }
        return true;
    }

    private Point getValidPoint(int x,int y){
        return new Point((int)(x/myLineHeight),(int)(y/myLineHeight));
    }

    //计算布局大小
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        int widthMode=View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode=View.MeasureSpec.getMode(heightMeasureSpec);

        int widthSize=View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize=View.MeasureSpec.getSize(heightMeasureSpec);

        int width=Math.min(widthSize,heightSize);

        if(widthMode==View.MeasureSpec.UNSPECIFIED){       //UNSPECIFIED表示未知
            width=heightSize;
        }
        else if(heightMode==View.MeasureSpec.UNSPECIFIED){
            width=widthSize;
        }

        setMeasuredDimension(width, width);
    }

    protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        myPanelWidth=w;
        myLineHeight=myPanelWidth*1.0f/maxLine;
        mUnder=h-(h-myPanelWidth)/2;

        int pieceWidth=(int)(myLineHeight*ratioPieceOfLineHeight);    //棋子大小占行宽3/4
        myWhitePice = Bitmap.createScaledBitmap(myWhitePice, pieceWidth, pieceWidth, false);     //以资源中的图片为原图，创建新的图像，指定新图像的高宽以及是否可变
        myBlackPice = Bitmap.createScaledBitmap(myBlackPice, pieceWidth, pieceWidth, false);
    }

    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        drawBroad(canvas);
        drawPiece(canvas);
        checkGameOver();
    }

    private void drawBroad(Canvas canvas){
        int w=myPanelWidth;
        float lineHeight=myLineHeight;
        int startX=(int)(lineHeight/2);
        int endX=(int)(w-lineHeight/2);

        for(int i=0;i<maxLine;i++){
            int y=(int)((i+1.5)*lineHeight);

            canvas.drawLine(startX,y,endX,y,myPaint);
            canvas.drawLine(y,startX,y,endX,myPaint);
        }
    }

    //画棋子
    private void drawPiece(Canvas canvas){
        int n1=myWhiteArray.size();
        int n2=myBlackArray.size();
        for(int i=0;i<n1;i++){
            Point whitePoint=myWhiteArray.get(i);
            canvas.drawBitmap(myWhitePice, (whitePoint.x+(1-ratioPieceOfLineHeight)/2)*myLineHeight,(whitePoint.y+(1-ratioPieceOfLineHeight)/2)*myLineHeight, null);
        }
        for(int i=0;i<n2;i++){
            Point blackPoint=myBlackArray.get(i);
            canvas.drawBitmap(myBlackPice, (blackPoint.x+(1-ratioPieceOfLineHeight)/2)*myLineHeight,(blackPoint.y+(1-ratioPieceOfLineHeight)/2)*myLineHeight, null);
        }
    }

    //检测游戏是否结束
    private void checkGameOver(){
        boolean whiteWin=checkFiveInLine(myWhiteArray);
        boolean blackWin=checkFiveInLine(myBlackArray);

        if(whiteWin||blackWin){
            isGameOver=true;
            if(onGameListener!=null){
                onGameListener.onGameOver(whiteWin?WHITE_WIN:BLACK_WIN);
            }
        }
    }

    //返回一个数值用于设置dialog的位置
    public int getUnder(){
        return mUnder;
    }

    //检测是否存在五子相连的情况
    private boolean checkFiveInLine(List<Point> myArray){
        for(Point p:myArray){
            int x=p.x;
            int y=p.y;

            boolean win_flag=checkHorizontal(x , y ,myArray)||checkVertical(x,y,myArray)||checkLeftDiagonal(x,y,myArray)||checkRightDiagonal(x,y,myArray);
            if(win_flag){
                return true;
            }
        }
        return false;
    }

    //横向检查是否满足五子相连
    private boolean checkHorizontal(int x,int y,List<Point> myArray){
        int count=1;
        for(int i=1;i<5;i++){
            if(myArray.contains(new Point(x+i,y))){
                count++;
            }
            else{
                break;
            }
        }
        if(count==5){
            return true;
        }
        for(int i=1;i<5;i++){
            if(myArray.contains(new Point(x-i,y))){
                count++;
            }
            else{
                break;
            }

            if(count==5){
                return true;
            }
        }
        return false;
    }

    //纵向检查是否满足五子相连
    private boolean checkVertical(int x,int y,List<Point> myArray){
        int count=1;
        for(int i=1;i<5;i++){
            if(myArray.contains(new Point(x,y+i))){
                count++;
            }
            else{
                break;
            }
        }
        if(count==5){
            return true;
        }
        for(int i=1;i<5;i++){
            if(myArray.contains(new Point(x,y-i))){
                count++;
            }
            else{
                break;
            }

            if(count==5){
                return true;
            }
        }
        return false;
    }

    //左斜检查是否满足五子相连
    private boolean checkLeftDiagonal(int x,int y,List<Point> myArray){
        int count=1;
        for(int i=1;i<5;i++){
            if(myArray.contains(new Point(x-i,y+i))){
                count++;
            }
            else{
                break;
            }
        }
        if(count==5){
            return true;
        }
        for(int i=1;i<5;i++){
            if(myArray.contains(new Point(x+i,y-i))){
                count++;
            }
            else{
                break;
            }

            if(count==5){
                return true;
            }
        }
        return false;
    }

    //右斜检查是否满足五子相连
    private boolean checkRightDiagonal(int x,int y,List<Point> myArray){
        int count=1;
        for(int i=1;i<5;i++){
            if(myArray.contains(new Point(x+i,y+i))){
                count++;
            }
            else{
                break;
            }
        }
        if(count==5){
            return true;
        }
        for(int i=1;i<5;i++){
            if(myArray.contains(new Point(x-i,y-i))){
                count++;
            }
            else{
                break;
            }

            if(count==5){
                return true;
            }
        }
        return false;
    }


    //重新开始游戏
    protected void restartGame(){
        myWhiteArray.clear();
        myBlackArray.clear();;
        isGameOver=false;
        isWhite=false;
        invalidate();
    }
}
