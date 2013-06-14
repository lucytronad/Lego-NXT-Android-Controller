package com.example.lo52_project;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Class of the drawable Robot picture
 * @author Lois Aubree & Lucie Boutou 
 *
 *This class allows to draw the robot linked to the robot on the surfaceView 
 */
public class RobotG extends Drawable{
	private float _X = 0;
	private float _Y = 0;
	private int worldPosX = 0;
	private int worldPosY = 0;
	private float size_X = 0;
	private float size_Y = 0;
	private float scale_X = 0.5f;
	private float scale_Y = 0.5f;
	private boolean isSelected = false;
	private boolean isPlaced = false;
	private Rect Bounds;

	public BitmapDrawable image;
	
	/**
	 * @constructor
	 */
	public RobotG() {
		image = null;
	}
	public RobotG(BitmapDrawable bit){
		image = bit;
		size_X = image.getIntrinsicWidth();
		size_Y = image.getIntrinsicHeight();
		Bounds = new Rect(0,0,(int)size_X,(int)size_Y);
	}

	@Override
	public void draw(Canvas canvas) {image.draw(canvas);}

	@Override
	public int getOpacity() {return 0;}

	@Override
	public void setAlpha(int alpha) {}
	@Override
	public void setColorFilter(ColorFilter cf) {}
	public float getScaleX(){return scale_X;}
	public float getScaleY(){return scale_Y;}
	public void setScaleX(float sca){scale_X = sca;}
	public void setScaleY(float sca){scale_Y = sca;}

	public float getPosX(){return _X;}
	public float getPosY(){return _Y;}
	public void setPosX(float pos){_X = pos;}
	public void setPosY(float pos){_Y = pos;}
	
	public void setWorldPos(int x, int y){worldPosX = x; worldPosY = y;}
	public int getWorldPosX(){return worldPosX;}
	public int getWorldPosY(){return worldPosY;}


	public Rect getImageBounds(){return image.getBounds();}
	public void setImageBounds(Rect rect){image.setBounds(rect);}
	
	/**
	 * Update Robot image bounds when the user moves the robot on the surfaceview
	 */
	public void updateBounds(){Bounds = new Rect((int)(_X-size_X/2*scale_X),
			(int)(_Y-size_Y/2*scale_Y),
			(int)(_X+size_X/2*scale_X),
			(int)(_Y+size_Y/2*scale_Y));
		image.setBounds(Bounds);
	}
	
	public boolean isSelected(){return isSelected;}
	public void setSelected(boolean sel){isSelected = sel;}
	public boolean isPlaced(){return isPlaced;}
	public void setPlaced(boolean pla){isPlaced = pla;}

}

