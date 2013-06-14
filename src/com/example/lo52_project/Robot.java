package com.example.lo52_project;


import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

/**
 * Class of the physical Robot
 * @author Lois Aubree & Lucie Boutou
 *
 */
public class Robot {
	
	/**
	 *  The picture associated to the robot 
	 */
	private RobotG mRobotG;
	private int mHeadPosition=0;
	/**
	 * Value of two sensor: light and ultrasonic
	 */
	private int mLightValue;
	private int mSonarValue;
	private int mMotorATacho=0;
	/**
	 * All orientation of the robot that are possible
	 */
	public enum Orientation{NORTH,EAST,SOUTH,WEST};
	
	/**
	 * Current physical robot orientation
	 */
	public Orientation mCurrentOrientation;
	
	/**
	 * @constructor
	 */
	public Robot() {mRobotG = new RobotG(); mCurrentOrientation = Orientation.EAST;}
	public Robot(BitmapDrawable bit){mRobotG = new RobotG(bit); mCurrentOrientation = Orientation.EAST;}
	
	public RobotG getRobotGraphic(){return mRobotG;}
	
	public void drawOnPA(Canvas canvas){mRobotG.draw(canvas);}
	
	public int getLightValue(){return mLightValue;}
	public void setLightValue(int value){mLightValue=value;}
	public int getSonarValue(){return mSonarValue;}
	public void setSonarValue(int value){mSonarValue=value;}
	public int getHeadPosition(){return mHeadPosition;}
	public void setHeadPosition(int position){mHeadPosition=position;}
	public int getMotorATacho(){return mMotorATacho;}
	public void setMotorATacho(int tacho){mMotorATacho=tacho;}
};