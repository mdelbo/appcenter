package com.example.waybackhome;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This view start with full gray color bitmap and onTouch to make it transparent
 * 
 * @author winsontan520
 */
public class WScratchView extends SurfaceView implements IWScratchView, SurfaceHolder.Callback{
    private static final String TAG = "WScratchView";

    // default value constants
    private final int DEFAULT_COLOR = 0xff444444; // default color is dark gray
    private final int DEFAULT_REVEAL_SIZE = 128;
    
    private Context mContext;
    private WScratchViewThread mThread;
    List<Path> mPathList = new ArrayList<Path>();
    private int mOverlayColor;
    private int mStartColor;
    private int mEndColor;
    private Paint mOverlayPaint;
    private Paint mCutawayPaint;
    private Paint mClearPaint;
    private Rect mFrame;
    private int mRevealSize;
    private boolean mIsScratchable = true;
    private boolean mIsAntiAlias = false;
    private Path path;
	private float startX = 0;
	private float startY = 0;
	private boolean mScratchStart =false;

    public WScratchView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx, attrs); 
    }

	public WScratchView(Context context) {
        super(context);
        init(context, null);
    }
	
    private void init(Context context, AttributeSet attrs) {
    	mContext = context;
        
    	// default value
    	mOverlayColor = DEFAULT_COLOR;
    	mRevealSize = DEFAULT_REVEAL_SIZE;
    	
    	mStartColor = Color.rgb(127, 233, 190);
    	mEndColor = Color.rgb(0, 210, 168);
    	
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WScratchView, 0, 0);

        final int indexCount = ta.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
        	int attr = ta.getIndex(i);
        	switch(attr){
        		case R.styleable.WScratchView_overlayColor:
        			mOverlayColor = ta.getColor(attr, DEFAULT_COLOR);
        			break;
        		case R.styleable.WScratchView_revealSize:
                    mRevealSize = ta.getDimensionPixelSize(attr, DEFAULT_REVEAL_SIZE);
                    break;
        		case R.styleable.WScratchView_antiAlias:
        			mIsAntiAlias = ta.getBoolean(attr, false);
                    break;
        		case R.styleable.WScratchView_scratchable:
        			mIsScratchable = ta.getBoolean(attr, true);
                    break;         
        	}
        }
        
    	setZOrderOnTop(true);   
    	SurfaceHolder holder = getHolder();
    	holder.addCallback(this);
    	holder.setFormat(PixelFormat.TRANSPARENT);

    	
    }

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawRect(mFrame, mClearPaint);
		canvas.drawRect(mFrame, mOverlayPaint);
		
		for(Path path: mPathList){	
			canvas.drawPath(path, mCutawayPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		synchronized (mThread.getSurfaceHolder()) {
			if (!mIsScratchable) {
				return true;
			}

			switch(me.getAction()){
			case MotionEvent.ACTION_DOWN:
				path = new Path();
				path.moveTo(me.getX(), me.getY());
				startX = me.getX();
				startY = me.getY();
				mPathList.add(path);
			    break;
			case MotionEvent.ACTION_MOVE:
				if(mScratchStart){
					path.lineTo(me.getX(), me.getY());
				}else{
					if(isScratch(startX, me.getX(), startY, me.getY())){
						mScratchStart = true;
						path.lineTo(me.getX(), me.getY());
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				mScratchStart = false;
				break;
			}
			return true;
		}
	}

	private boolean isScratch(float oldX, float x, float oldY, float y) {
		float distance = (float) Math.sqrt(Math.pow(oldX - x, 2) + Math.pow(oldY - y, 2));
		if(distance > mRevealSize * 2){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// do nothing
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		mOverlayPaint = new Paint();
		mOverlayPaint.setAlpha(0xD0);
		mOverlayPaint.setShader(new RadialGradient(getWidth(), 0.0f, getWidth()/2, mStartColor, mEndColor, TileMode.CLAMP));
		mOverlayPaint.setStyle(Paint.Style.FILL);
		mFrame = new Rect(0, 0, getWidth(), getHeight());
		
		mClearPaint = new Paint();
		mClearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		mClearPaint.setStyle(Paint.Style.FILL);
		mFrame = new Rect(0, 0, getWidth(), getHeight());
		
		mCutawayPaint = new Paint();
		mCutawayPaint.setAlpha(0xFF);
		mCutawayPaint.setShader(null);
		mCutawayPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		mCutawayPaint.setStyle(Paint.Style.STROKE);
		mCutawayPaint.setStrokeCap(Paint.Cap.ROUND);
		mCutawayPaint.setStrokeJoin(Paint.Join.ROUND);
		mCutawayPaint.setAntiAlias(mIsAntiAlias);
		mCutawayPaint.setStrokeWidth(mRevealSize);
		
		mThread = new WScratchViewThread(getHolder(), this); 
		mThread.setRunning(true);
        mThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
        mThread.setRunning(false);
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // do nothing but keep retry
            }
        }
		
	}

	class WScratchViewThread extends Thread {
		private SurfaceHolder mSurfaceHolder;
		private WScratchView mView;
		private boolean mRun = false;

		public WScratchViewThread(SurfaceHolder surfaceHolder, WScratchView view) {
			mSurfaceHolder = surfaceHolder;
			mView = view;
		}

		public void setRunning(boolean run) {
			mRun = run;
		}

		public SurfaceHolder getSurfaceHolder() {
			return mSurfaceHolder;
		}

		@SuppressLint("WrongCall")
		@Override
		public void run() {
			Canvas c;
			while (mRun) {
				c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						if(c != null){	
							mView.onDraw(c);
						}
					}
				} finally {
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
	}
	
	@Override
	public void resetView(){
		synchronized (mThread.getSurfaceHolder()) {
			mPathList.clear();
		}
	}

	@Override
	public boolean isScratchable() {
		return mIsScratchable;
	}

	@Override
	public void setScratchable(boolean flag) {
		mIsScratchable = flag;
	}

	@Override
	public void setOverlayColor(int ResId) {
		mOverlayColor = ResId;
	}

	@Override
	public void setRevealSize(int size) {
		mRevealSize = size;
	}

	@Override
	public void setAntiAlias(boolean flag) {
		mIsAntiAlias = flag;
	}

}


