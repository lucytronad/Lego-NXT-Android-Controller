package com.example.lo52_project;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ComplexeSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

	private Paint mPaint;
	private Rect mRectMap;
	
	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			invalidate();
			postDelayed(this, 0);
		}
	};
	
	
	public ComplexeSurfaceView(Context context,AttributeSet attributeSet) {
		super(context,attributeSet);
		mPaint = new Paint();
		getHolder().addCallback(this);
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setColor(Color.WHITE);
		mRectMap = new Rect(0,0, (int)getWidth(), (int)getHeight());
		canvas.drawRect(mRectMap, mPaint);
		canvas.save();
		canvas.restore();
	};
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	
	    mRunnable.run();
	    post(mRunnable);
	    setWillNotDraw(false);
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}
