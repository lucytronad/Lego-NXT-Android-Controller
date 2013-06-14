package com.example.lo52_project;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

/**
 *  Piece class  
 * @author Lois Aubree & Lucie Boutou
 *
 */
public class Piece {
	float _X = 0;
	float _Y = 0;
	float size_X = 0;
	float size_Y = 0;
	float scale_X = 1.0f;
	float scale_Y = 1.0f;
	static final int PIECE_WIDTH = 72;
	static final int PIECE_HEIGHT = 66;
	boolean isSelected = false;
	boolean isPlaced = false;

	/**
	 * Wall description
	 */
	final static byte VIDE = 0x00;
	final static byte CROIX = 0x01;
	final static byte BARRE_H = 0x02;
	final static byte BARRE_V = 0x03;
	final static byte MUR_ON = 0x12;
	final static byte MUR_OFF = 0x13;
	
	private boolean isGoal = false;
	
	/**
	 * Configuration of walls 
	 * This a 5 bytes table 
	 * wall_config[0] = VIDE, CROIX, BARRE_H, BARRE_V
	 * wall_config[1-4] = MUR_ON, MUR_OFF
	 */
	public byte[] wall_config;
	
	private int indX = -1 ;
	private int indY = -1 ;
	
	private BitmapDrawable image;
	
	Piece(){}
	
	/**
	 * @constructor
	 * @param Bitmap corresponding of piece's image
	 * @param Configuration of walls
	 */
	Piece(BitmapDrawable bit, byte[] config){
		image = bit;
		wall_config = config;
	}
	public void setImage(BitmapDrawable bit){image = bit;}
	public BitmapDrawable getImage(){return image;};
	public Bitmap getPieceBitmap(){return image.getBitmap();};

	public void draw(Canvas canvas) {image.draw(canvas);}
	public void setPosition(float x, float y){_X = x;_Y = y;}	
	public void setGoal(boolean g){isGoal = g;};
	public boolean isGoal(){return isGoal;};
	
	public void setIndices(int i , int j){indX = i; indY = j;};
	public int getIndX(){return indX;};
	public int getIndY(){return indY;};
	public void setWallConfig(byte[] wall){wall_config = wall;}
	public byte[] getWallConfig(){return wall_config;}
	
};
