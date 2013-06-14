package com.example.lo52_project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;

/**
 * 
 * Class of the background grid
 * This class is important because it gives the indexes values of pieces.
 * Cannot dissociated to SimpleSurfaceView that implement it
 * @author Lois Aubree & Lucie Boutou
 *
 */
public class MapDrawable {

	public final static int NB_DEFAULT_ROWS = 8;
	public final static int NB_DEFAULT_COLS = 5;
	private int mRows = 8;
	private int mCols = 5;
	public final static int _X = 200;
	public final static int _Y = 200;
	private float centerX = 0;
	private float centerY = 0;
	public Paint mPaint = new Paint();

	private Rect[][] mTabCase = new Rect[NB_DEFAULT_ROWS][NB_DEFAULT_COLS];

	/**
	 * @constructor
	 * @param Context of the application
	 */
	public MapDrawable(Context context) {
		/**
		 * get Screen size
		 */
		DisplayMetrics disp = context.getResources().getDisplayMetrics();
		centerX = disp.widthPixels/2;
		centerY = disp.heightPixels/2;
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		float startX = centerX-(NB_DEFAULT_ROWS*Piece.PIECE_WIDTH)/2;
		float startY = centerY-(NB_DEFAULT_COLS*Piece.PIECE_HEIGHT)/2;
		for (int i = 0 ; i < NB_DEFAULT_ROWS ; i++ )
			for(int j = 0; j < NB_DEFAULT_COLS ; j++)
			{	
				mTabCase[i][j] = new Rect((int)startX+i*Piece.PIECE_WIDTH,(int)startY+j*Piece.PIECE_HEIGHT,
						(int)startX+(i+1)*Piece.PIECE_WIDTH,(int)startY+(j+1)*Piece.PIECE_HEIGHT);
			}
	}

	public Rect getCase(int i,int j){return mTabCase[i][j];}


	/**
	 * Drawing function of the grid
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		for (int i = 0 ; i < mRows ; i++ )
			for(int j = 0; j < mCols ; j++)
			{
				canvas.drawRect(mTabCase[i][j], mPaint);
				//canvas.drawCircle(centerX, centerY, 3, mPaint);
			}
	}

	public Rect getBoundCase(int i, int j)
	{
		return mTabCase[i][j];
	}	
	
	/**
	 * Resize the grid to allow adding of more pieces to the world
	 * @param new width value
	 * @param new height value
	 */
	public void resize(int x , int y)
	{
		mRows = x;
		mCols = y;
		mTabCase = new Rect[mRows][mCols]; 
		float startX = centerX-(x*Piece.PIECE_WIDTH)/2;
		float startY = centerY-(y*Piece.PIECE_HEIGHT)/2;
		for (int i = 0 ; i < mRows ; i++ )
			for(int j = 0; j < mCols ; j++)
			{	
				mTabCase[i][j] = new Rect((int)startX+i*Piece.PIECE_WIDTH,(int)startY+j*Piece.PIECE_HEIGHT,
						(int)startX+(i+1)*Piece.PIECE_WIDTH,(int)startY+(j+1)*Piece.PIECE_HEIGHT);
			}
	}

}
