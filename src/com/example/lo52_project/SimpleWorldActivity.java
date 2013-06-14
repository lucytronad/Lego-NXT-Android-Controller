package com.example.lo52_project;

import java.util.ArrayList;
import java.util.TimerTask;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ScrollView;

/**
 * SimpleWorld extends WorldActivity to add particular value of the simple world
 * environment
 * 
 * @author Lois Aubree & Lucie Boutou
 */

public class SimpleWorldActivity extends WorldActivity {
	private boolean IS_LIGHT_READY = true;
	private boolean IS_SONAR_READY = true;

	/**
	 * Particular values used in connected mode
	 */
	@SuppressWarnings("unused")
	private int BLACK_LIGHT = 105;
	private int WHITE_LIGHT = 78;
	private int DETECTION_DISTANCE = 15;
	private int WHEELING_DISTANCE = 10;

	private int indX = 0, indY = 0;
	private Robot.Orientation lastOrient = Robot.Orientation.EAST;

	/**
	 * This Handler manages the runnnables which send messages to the bluetooth
	 * thread
	 */
	private Handler mHandler;

	private byte[] piece_config = new byte[5];
	private Piece mCurrentPiece;

	private SimpleSurfaceView mSurfaceView;
	private View mScrollLayout;
	private ScrollView mScrollView;
	ArrayList<ImageButton> mListPieceButton = new ArrayList<ImageButton>();

	/**
	 * runnable permanently reposted to get the last light sensor value
	 */
	private Runnable mLightPerm = new Runnable() {

		@Override
		public void run() {
			mHandler.post(mLightSensor);
			mHandler.post(mReadLight);
			mHandler.postDelayed(this, 100);
		}
	};

	/**
	 * runnable permanently reposted to get the last sonar sensor value
	 */
	private Runnable mSonarPerm = new Runnable() {

		@Override
		public void run() {
			mHandler.post(mSonarSensor);
			mHandler.post(mReadSonarSensor);
			mHandler.postDelayed(this, 100);
		}
	};

	/**
	 * main runnable of the connected mode (post in first place by the handler)
	 */
	private Runnable mStartRunnable = new Runnable() {

		@Override
		public void run() {

			/**
			 * if the robot reaches a white check points the robot is stopped
			 * and control around it
			 */
			if (mRobot.getLightValue() < WHITE_LIGHT) {
				mCurrentPiece = new Piece();
				piece_config[0] = Piece.CROIX;
				piece_config[3] = Piece.MUR_OFF;
				mHandler.post(mRobotStop);
				mHandler.post(mRHeadControl);
			} else {
				if (mRobot.getSonarValue() > WHEELING_DISTANCE) {
					mHandler.post(mRobotForward);
				} else {
					mHandler.post(mRobotStop);
				}
				/**
				 * main runnable is reposted only if the robot is not on a white
				 * check points
				 */
				mHandler.postDelayed(this, 100);
			}
		}
	};

	/**
	 * runnable which control the configuration around the robot when it stops
	 * on a white check points
	 */
	private Runnable mRHeadControl = new Runnable() {

		@Override
		public void run() {

			mHandler.post(mHeadTurnLeft);
			mHandler.postDelayed(mGetValueAtLeft, TURN_HEAD_DELAY);
			mHandler.postDelayed(mHeadTurnFront, TURN_HEAD_DELAY);
			mHandler.postDelayed(mGetValueAtFront, TURN_HEAD_DELAY * 2);
			mHandler.postDelayed(mHeadTurnRight, TURN_HEAD_DELAY * 2);
			mHandler.postDelayed(mGetValueAtRight, TURN_HEAD_DELAY * 3);
			mHandler.postDelayed(mHeadTurnFront, TURN_HEAD_DELAY * 3);
			mHandler.postDelayed(mSavePiece, TURN_HEAD_DELAY * 4);
		}
	};

	/**
	 * runnable which checks if there is a wall on the left of the robot
	 */
	private Runnable mGetValueAtLeft = new Runnable() {

		@Override
		public void run() {

			if (mRobot.getSonarValue() < DETECTION_DISTANCE
					&& mRobot.getSonarValue() > 0)
				piece_config[4] = Piece.MUR_ON;
			else
				piece_config[4] = Piece.MUR_OFF;
			// Log.v("left reading", String.valueOf(mRobot.getSonarValue()));
		}
	};

	/**
	 * runnable which checks if there is a wall on the front of the robot
	 */
	private Runnable mGetValueAtFront = new Runnable() {

		@Override
		public void run() {
			if (mRobot.getSonarValue() < DETECTION_DISTANCE
					&& mRobot.getSonarValue() > 0)
				piece_config[1] = Piece.MUR_ON;
			else
				piece_config[1] = Piece.MUR_OFF;
			// Log.v("front reading", String.valueOf(mRobot.getSonarValue()));
		}
	};

	/**
	 * runnable which checks if there is a wall on the right of the robot
	 */
	private Runnable mGetValueAtRight = new Runnable() {

		@Override
		public void run() {
			if (mRobot.getSonarValue() < DETECTION_DISTANCE
					&& mRobot.getSonarValue() > 0)
				piece_config[2] = Piece.MUR_ON;
			else
				piece_config[2] = Piece.MUR_OFF;
			// Log.v("right reading", String.valueOf(mRobot.getSonarValue()));
		}
	};

	/**
	 * runnable which add the detected piece on the surface view updates the
	 * robot orientation and the indexes of the robot position takes the
	 * decision of the robot's next direction
	 */
	private Runnable mSavePiece = new Runnable() {

		@Override
		public void run() {
			if (mRobot.mCurrentOrientation == lastOrient) {
				BitmapDrawable image = getBitmapByWallConfig();
				mCurrentPiece.setImage(image);
				mSurfaceView.addPieceAt(mCurrentPiece, indX, indY);

				if (lastOrient == Robot.Orientation.EAST) {
					if (piece_config[1] == Piece.MUR_OFF) {
						mHandler.post(mRobotForward);
						mHandler.postDelayed(mStartRunnable, 500);
						indX++;
					} else if (piece_config[2] == Piece.MUR_OFF) {
						mHandler.post(mRobotBackward);
						mHandler.postDelayed(mRobotTurnRight, 800);
						mHandler.postDelayed(mRobotStop, 2500);
						mHandler.postDelayed(mStartRunnable, 3100);
						indY++;
						mRobot.mCurrentOrientation = Robot.Orientation.SOUTH;
					} else if (piece_config[3] == Piece.MUR_OFF) {
						mHandler.post(mRobotBackward);
						mHandler.postDelayed(mRobotTurnLeft, 800);
						mHandler.postDelayed(mRobotStop, 2500);
						mHandler.postDelayed(mStartRunnable, 3100);
						indY--;
						mRobot.mCurrentOrientation = Robot.Orientation.SOUTH;
					} else
						mHandler.post(mRobotStop);

				} else if (lastOrient == Robot.Orientation.NORTH) {
					if (piece_config[1] == Piece.MUR_OFF) {
						mHandler.post(mRobotForward);
						mHandler.postDelayed(mStartRunnable, 500);
						indX--;
					} else if (piece_config[2] == Piece.MUR_OFF) {
						mHandler.post(mRobotBackward);
						mHandler.postDelayed(mRobotTurnRight, 800);
						mHandler.postDelayed(mRobotStop, 2500);
						mHandler.postDelayed(mStartRunnable, 3100);
						indX++;
						mRobot.mCurrentOrientation = Robot.Orientation.EAST;
					} else if (piece_config[4] == Piece.MUR_OFF) {
						mHandler.post(mRobotBackward);
						mHandler.postDelayed(mRobotTurnLeft, 800);
						mHandler.postDelayed(mRobotStop, 2500);
						mHandler.postDelayed(mStartRunnable, 3100);
						indX--;
						mRobot.mCurrentOrientation = Robot.Orientation.WEST;
					} else
						mHandler.post(mRobotStop);

				} else if (lastOrient == Robot.Orientation.SOUTH) {
					if (piece_config[1] == Piece.MUR_OFF) {
						mHandler.post(mRobotForward);
						mHandler.postDelayed(mStartRunnable, 500);
						indY++;
					} else if (piece_config[2] == Piece.MUR_OFF) {
						mHandler.post(mRobotBackward);
						mHandler.postDelayed(mRobotTurnRight, 800);
						mHandler.postDelayed(mRobotStop, 2500);
						mHandler.postDelayed(mStartRunnable, 3100);
						indX--;
						mRobot.mCurrentOrientation = Robot.Orientation.WEST;
					} else if (piece_config[4] == Piece.MUR_OFF) {
						mHandler.post(mRobotBackward);
						mHandler.postDelayed(mRobotTurnLeft, 800);
						mHandler.postDelayed(mRobotStop, 2500);
						mHandler.postDelayed(mStartRunnable, 3100);
						indX++;
						mRobot.mCurrentOrientation = Robot.Orientation.EAST;
					} else
						mHandler.post(mRobotStop);

				} else if (lastOrient == Robot.Orientation.WEST) {
					if (piece_config[1] == Piece.MUR_OFF) {
						mHandler.post(mRobotForward);
						mHandler.postDelayed(mStartRunnable, 500);
						indX--;
					} else if (piece_config[2] == Piece.MUR_OFF) {
						mHandler.post(mRobotBackward);
						mHandler.postDelayed(mRobotTurnRight, 800);
						mHandler.postDelayed(mRobotStop, 2500);
						mHandler.postDelayed(mStartRunnable, 3100);
						indY--;
						mRobot.mCurrentOrientation = Robot.Orientation.NORTH;
					} else if (piece_config[4] == Piece.MUR_OFF) {
						mHandler.post(mRobotBackward);
						mHandler.postDelayed(mRobotTurnLeft, 800);
						mHandler.postDelayed(mRobotStop, 2500);
						mHandler.postDelayed(mStartRunnable, 3100);
						indY++;
						mRobot.mCurrentOrientation = Robot.Orientation.SOUTH;
					} else
						mHandler.post(mRobotStop);
				}
			}
		}
	};

	/**
	 * runnable which make the robot turns on its right
	 */
	private Runnable mRobotTurnRight = new Runnable() {

		@Override
		public void run() {
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_B, 30, 0);
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_C, 5, 0);
			lastOrient = mRobot.mCurrentOrientation;
		}
	};

	/**
	 * runnable which make the robot turns on its left
	 */
	private Runnable mRobotTurnLeft = new Runnable() {

		@Override
		public void run() {
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_B, 5, 0);
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_C, 30, 0);
			lastOrient = mRobot.mCurrentOrientation;
		}
	};

	/**
	 * runnable which make the robot sonar sensor turn to the front (if it is
	 * not already)
	 */
	private Runnable mHeadTurnFront = new Runnable() {

		@Override
		public void run() {
			int head = mRobot.getHeadPosition();
			if (head != 0) {
				if (head == -1) {
					sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
							BluetoothThread.MOTOR_A, 20, 90);
					mRobot.setHeadPosition(head + 1);
				}

				if (head == 1) {
					sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
							BluetoothThread.MOTOR_A, -20, 90);
					mRobot.setHeadPosition(head - 1);
				}
			}
		}
	};

	/**
	 * runnable which make the robot sonar sensor turn to the left (if it is not
	 * already)
	 */
	private Runnable mHeadTurnLeft = new Runnable() {

		@Override
		public void run() {
			int head = mRobot.getHeadPosition();
			if (head != -1) {
				if (head == 0) {
					sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
							BluetoothThread.MOTOR_A, -20, 90);
					mRobot.setHeadPosition(head - 1);
				}

				if (head == 1) {
					sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
							BluetoothThread.MOTOR_A, -20, 180);
					mRobot.setHeadPosition(head - 2);
				}
			}
		}
	};

	/**
	 * runnable which make the robot sonar sensor turn to the right (if it is
	 * not already)
	 */
	private Runnable mHeadTurnRight = new Runnable() {

		@Override
		public void run() {
			int head = mRobot.getHeadPosition();
			if (head != 1) {
				if (head == 0) {
					sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
							BluetoothThread.MOTOR_A, 20, 90);
					mRobot.setHeadPosition(head + 1);
				}

				if (head == -1) {
					sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
							BluetoothThread.MOTOR_A, 20, 180);
					mRobot.setHeadPosition(head + 2);
				}
			}

		}
	};

	/**
	 * runnable which make the robot go forward, a line follower algorithm should
	 * be implemented
	 */
	private Runnable mRobotForward = new Runnable() {

		@Override
		public void run() {
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_B, 20, 0);
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_C, 20, 0);

		}
	};

	/**
	 * runnable which make the robot go backward (used before to turn)
	 */
	private Runnable mRobotBackward = new Runnable() {

		@Override
		public void run() {
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_B, -20, 0);
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_C, -20, 0);
		}
	};

	/**
	 * runnable which make the robot stop
	 */
	private Runnable mRobotStop = new Runnable() {

		@Override
		public void run() {

			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_B, 0, 0);
			sendBTmessage(BluetoothThread.UPDATE_MOTOR_SPEED,
					BluetoothThread.MOTOR_C, 0, 0);
		}
	};

	/**
	 * runnable which updates the light value of the text view in slave mode
	 */
	private int currentLight = 0;
	private Runnable mReadLight = new Runnable() {

		@Override
		public void run() {
			if (IS_LIGHT_READY) {
				currentLight = mRobot.getLightValue();
				lightTextView.setText(String.valueOf(currentLight));
			} else
				mHandler.postDelayed(this, 20);

		}
	};

	/**
	 * runnable which ask the value of the light sensor
	 */
	private Runnable mLightSensor = new Runnable() {

		@Override
		public void run() {
			IS_LIGHT_READY = false;
			sendBTmessage(BluetoothThread.ASK_SENSOR_VALUE,
					BluetoothThread.LIGHT_SENSOR, 0, 0);
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					// Log.v("light sensor", "ready to be read");
					IS_LIGHT_READY = true;
				}
			}, READ_DELAY);
		}
	};

	/**
	 * runnable which updates the sonar value of the text view in slave mode
	 */
	private int currentSonar = 0;
	private Runnable mReadSonarSensor = new Runnable() {

		@Override
		public void run() {
			if (IS_SONAR_READY) {
				currentSonar = mRobot.getSonarValue();
				sonarTextView.setText(String.valueOf(currentSonar));
			} else
				mHandler.postDelayed(this, 20);

		}
	};
	
	/**
	 * runnable which ask the value of the sonar sensor
	 */
	private Runnable mSonarSensor = new Runnable() {

		@Override
		public void run() {
			IS_SONAR_READY = false;
			mHandler.post(mSonnarAsk);
			mSonnarAsk.run();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					mHandler.post(mSonnarRead);
					mSonnarRead.run();
				}
			}, READ_DELAY);
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					IS_SONAR_READY = true;
				}
			}, SONAR_DELAY);
		}
	};
	private Runnable mSonnarAsk = new Runnable() {

		@Override
		public void run() {
			sendBTmessage(BluetoothThread.ASK_I2C_SENSOR_VALUE,
					BluetoothThread.ULTRASONIC_SENSOR, 0, 0);
		}
	};
	private Runnable mSonnarRead = new Runnable() {

		@Override
		public void run() {
			sendBTmessage(BluetoothThread.READ_I2C_SENSOR_VALUE,
					BluetoothThread.ULTRASONIC_SENSOR, 0, 0);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		mMainLayout = (FrameLayout) findViewById(R.id.main_layout);
		mHandler = new Handler();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mSurfaceView = new SimpleSurfaceView(getApplicationContext());
		mSurfaceView.setLayoutParams(params);
		mSurfaceView.setCurrentMode(SelectedMode.MODE_VIRTUAL);
		mSurfaceView.setOnTouchListener(this);
		mMainLayout.addView(mSurfaceView);

		mScrollLayout = (View) getLayoutInflater().inflate(R.layout.piece_view,
				null);
		mScrollView = (ScrollView) mScrollLayout.findViewById(R.id.piece_view);
		mScrollView.setOnTouchListener(this);
		mMainLayout.addView(mScrollLayout);

		mListPieceButton.add((ImageButton) findViewById(R.id.croix_1));
		mListPieceButton.add((ImageButton) findViewById(R.id.croix_2));
		mListPieceButton.add((ImageButton) findViewById(R.id.croix_3));
		mListPieceButton.add((ImageButton) findViewById(R.id.croix_4));
		mListPieceButton.add((ImageButton) findViewById(R.id.croix_5));
		mListPieceButton.add((ImageButton) findViewById(R.id.croix_6));
		mListPieceButton.add((ImageButton) findViewById(R.id.croix_7));
		mListPieceButton.add((ImageButton) findViewById(R.id.croix_8));

		mListPieceButton.add((ImageButton) findViewById(R.id.barreh_1));
		mListPieceButton.add((ImageButton) findViewById(R.id.barreh_2));
		mListPieceButton.add((ImageButton) findViewById(R.id.barreh_3));
		mListPieceButton.add((ImageButton) findViewById(R.id.barreh_4));
		mListPieceButton.add((ImageButton) findViewById(R.id.barreh_5));

		mListPieceButton.add((ImageButton) findViewById(R.id.barrev_1));
		mListPieceButton.add((ImageButton) findViewById(R.id.barrev_2));
		mListPieceButton.add((ImageButton) findViewById(R.id.barrev_3));
		mListPieceButton.add((ImageButton) findViewById(R.id.barrev_4));

		mListPieceButton.add((ImageButton) findViewById(R.id.vide_1));
		mListPieceButton.add((ImageButton) findViewById(R.id.vide_2));

		for (ImageButton but : mListPieceButton)
			but.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_connected:
			mSurfaceView.setRobotAt(indX, indY);
			mHandler.removeCallbacksAndMessages(null);
			mHandler.post(mStartRunnable);
			mHandler.post(mLightPerm);
			mHandler.post(mSonarPerm);
			mLightPerm.run();
			mSonarPerm.run();
			mStartRunnable.run();

			break;
		case R.id.croix_1:
			piece_config[0] = Piece.CROIX;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_OFF;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_OFF;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(0)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.croix_2:
			piece_config[0] = Piece.CROIX;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(1)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.croix_3:
			piece_config[0] = Piece.CROIX;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_OFF;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(2)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.croix_4:
			piece_config[0] = Piece.CROIX;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_OFF;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(3)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.croix_5:
			piece_config[0] = Piece.CROIX;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_OFF;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(4)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.croix_6:
			piece_config[0] = Piece.CROIX;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_OFF;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(5)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.croix_7:
			piece_config[0] = Piece.CROIX;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_OFF;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_OFF;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(6)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.croix_8:
			piece_config[0] = Piece.CROIX;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(7)
					.getDrawable(), piece_config.clone());
			break;

		case R.id.barreh_1:
			piece_config[0] = Piece.BARRE_H;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_OFF;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_OFF;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(8)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.barreh_2:
			piece_config[0] = Piece.BARRE_H;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(9)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.barreh_3:
			piece_config[0] = Piece.BARRE_H;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(10)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.barreh_4:
			piece_config[0] = Piece.BARRE_H;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_OFF;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(11)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.barreh_5:
			piece_config[0] = Piece.BARRE_H;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(12)
					.getDrawable(), piece_config.clone());
			break;

		case R.id.barrev_1:
			piece_config[0] = Piece.BARRE_V;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(13)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.barrev_2:
			piece_config[0] = Piece.BARRE_V;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_OFF;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_OFF;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(14)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.barrev_3:
			piece_config[0] = Piece.BARRE_H;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(15)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.barrev_4:
			piece_config[0] = Piece.BARRE_H;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(16)
					.getDrawable(), piece_config.clone());
			break;

		case R.id.vide_1:
			piece_config[0] = Piece.VIDE;
			piece_config[1] = Piece.MUR_OFF;
			piece_config[2] = Piece.MUR_OFF;
			piece_config[3] = Piece.MUR_OFF;
			piece_config[4] = Piece.MUR_OFF;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(17)
					.getDrawable(), piece_config.clone());
			break;
		case R.id.vide_2:
			piece_config[0] = Piece.VIDE;
			piece_config[1] = Piece.MUR_ON;
			piece_config[2] = Piece.MUR_ON;
			piece_config[3] = Piece.MUR_ON;
			piece_config[4] = Piece.MUR_ON;
			mSurfaceView.addPiece((BitmapDrawable) mListPieceButton.get(18)
					.getDrawable(), piece_config.clone());
			break;

		default:
			break;
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.button_left:
				mHandler.post(mRobotTurnLeft);
				break;
			case R.id.button_right:
				mHandler.post(mRobotTurnRight);
				break;
			case R.id.button_forward:
				mHandler.post(mRobotForward);
				break;
			case R.id.button_backward:
				mHandler.post(mRobotBackward);
				break;
			case R.id.button_left_head:
				mHandler.post(mHeadTurnLeft);
				break;
			case R.id.button_right_head:
				mHandler.post(mHeadTurnRight);
				break;
			case R.id.button_light:
				mHandler.post(mLightSensor);
				mHandler.post(mReadLight);
				break;
			case R.id.button_center:
				mHandler.post(mHeadTurnFront);
				break;
			case R.id.button_ultrasonic:
				mHandler.post(mSonarSensor);
				mHandler.post(mReadSonarSensor);

				break;
			default:
				break;
			}
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.button_left:
			case R.id.button_right:
			case R.id.button_forward:
			case R.id.button_backward:
				mHandler.post(mRobotStop);
				break;
			}
		}
		return mSurfaceView.onTouchEvent(event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SelectedMode last = mCurrentMode;
		super.onOptionsItemSelected(item);
		mSurfaceView.setCurrentMode(mCurrentMode);
		mHandler.removeCallbacksAndMessages(null);
		switch (item.getItemId()) {
		case R.id.SlaveModeItem:
		case R.id.ConnectedModeItem:
		case R.id.MissionModeItem:
			mMainLayout.removeView(mScrollLayout);
			break;
		case R.id.VirtualModeItem:
			if (last != SelectedMode.MODE_VIRTUAL)
				mMainLayout.addView(mScrollLayout);
			break;
		default:
			break;
		}
		return true;
	}

	public BitmapDrawable getBitmapByWallConfig() {
		BitmapDrawable image = (BitmapDrawable) getResources().getDrawable(
				R.drawable.vide_fond);

		if (mRobot.mCurrentOrientation == Robot.Orientation.NORTH) {
			if (piece_config[0] == Piece.CROIX) {
				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hdbg);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_bg);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_db);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hb);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hd);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hg);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_dg);

			}
			if (piece_config[0] == Piece.BARRE_H) {

			}
			if (piece_config[0] == Piece.BARRE_V) {

			}
			if (piece_config[0] == Piece.VIDE) {

			}
		}
		if (mRobot.mCurrentOrientation == Robot.Orientation.EAST) {
			if (piece_config[0] == Piece.CROIX) {
				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hdbg);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hg);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hb);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_dg);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_db);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hd);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hb);

			}
			if (piece_config[0] == Piece.BARRE_H) {

			}
			if (piece_config[0] == Piece.BARRE_V) {

			}
			if (piece_config[0] == Piece.VIDE) {

			}
		}
		if (mRobot.mCurrentOrientation == Robot.Orientation.SOUTH) {
			if (piece_config[0] == Piece.CROIX) {
				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hdbg);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hd);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hg);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hb);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_bg);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_db);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_dg);

			}
			if (piece_config[0] == Piece.BARRE_H) {

			}
			if (piece_config[0] == Piece.BARRE_V) {

			}
			if (piece_config[0] == Piece.VIDE) {

			}
		}
		if (mRobot.mCurrentOrientation == Robot.Orientation.WEST) {
			if (piece_config[0] == Piece.CROIX) {
				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hdbg);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_db);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hd);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_ON
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_dg);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_OFF)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hg);

				if (piece_config[1] == Piece.MUR_ON
						&& piece_config[2] == Piece.MUR_OFF
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_bg);

				if (piece_config[1] == Piece.MUR_OFF
						&& piece_config[2] == Piece.MUR_ON
						&& piece_config[3] == Piece.MUR_OFF
						&& piece_config[4] == Piece.MUR_ON)
					image = (BitmapDrawable) getResources().getDrawable(
							R.drawable.croix_hb);

			}
			if (piece_config[0] == Piece.BARRE_H) {

			}
			if (piece_config[0] == Piece.BARRE_V) {

			}
			if (piece_config[0] == Piece.VIDE) {

			}
		}

		return image;
	}
}
