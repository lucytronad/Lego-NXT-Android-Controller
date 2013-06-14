package com.example.lo52_project;

import java.util.Timer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * WorldActivity is the abstract class which extends Activity
 * SimpleWorldActivity extends WorldActivity
 * 
 * @author Lois Aubree & Lucie Boutou
 */

abstract public class WorldActivity extends Activity implements
		OnTouchListener, OnClickListener {

	/**
	 * Delay values used to send messages with correct timing to the NXT robot
	 */
	public static final int DELAY = 50;
	public static final int READ_DELAY = 100;
	public static final int SONAR_DELAY = 500;
	public static final int TURN_HEAD_DELAY = 1000;
	public static final int TURN_ROBOT_DELAY = 2000;

	protected Timer mTimer = new Timer();
	protected Robot mRobot;

	/**
	 * The application switches between 4 modes
	 */
	protected MenuItem mSlaveMode;
	protected MenuItem mConnectedMode;
	protected MenuItem mMissionMode;
	protected MenuItem mVirtualMode;

	protected Menu mMenu;

	/**
	 * Objects allowing the user to communicate with the application
	 */
	protected ImageButton mSlaveButton_left;
	protected ImageButton mSlaveButton_right;
	protected ImageButton mSlaveButton_forward;
	protected ImageButton mSlaveButton_backward;
	protected ImageButton mSlaveButton_left_head;
	protected ImageButton mSlaveButton_right_head;
	protected ImageButton mSlaveButton_light;
	protected ImageButton mSlaveButton_ultrasonic;
	protected ImageButton mSlaveButton_center;
	protected TextView lightTextView;
	protected TextView sonarTextView;

	/**
	 * Objects allowing the user to run the NXT robot in connected mode
	 */
	protected Button mRunButton;

	protected enum SelectedMode {
		MODE_SLAVE, MODE_CONNECTED, MODE_MISSION, MODE_VIRTUAL
	};

	protected SelectedMode mCurrentMode;

	protected BluetoothThread mBluetoothThread = null;
	protected BluetoothAdapter mBluetoothAdapter = null;
	protected BluetoothDevice mDevice = null;

	protected String NXT_MAC_ADDRESS;
	protected String NXT_NAME;
	protected static final int REQUEST_ENABLE_BT = 1;
	protected static final int REQUEST_GET_DEVICE = 2;

	protected static final String NXT_EXTRA_NAME = "nxt_name";
	protected static final String NXT_EXTRA_ADD = "nxt_address";

	protected FrameLayout mMainLayout;
	protected View mSlaveLayout;
	protected View mConnectedLayout;

	protected boolean IS_NXT_CONNECTED = false;

	protected Handler mBTHandler;

	/**
	 * This Handler handles messages from the bluetooth thread and updates the
	 * variable of the robot object
	 */
	protected Handler mActHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {

			switch (message.getData().getInt("task")) {
			case BluetoothThread.RETURN_SENSOR_VALUE:
				if (message.getData().getInt("device") == BluetoothThread.LIGHT_SENSOR)
					mRobot.setLightValue(message.getData().getInt("param1"));
				break;
			case BluetoothThread.RETURN_I2C_SENSOR_VALUE:
				if (message.getData().getInt("device") == BluetoothThread.ULTRASONIC_SENSOR)
					mRobot.setSonarValue(message.getData().getInt("param1"));
				break;
			case BluetoothThread.RETURN_MOTOR_STATE:
				if (message.getData().getInt("device") == BluetoothThread.MOTOR_A)
					mRobot.setMotorATacho(message.getData().getInt("param1"));
				break;
			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!mBluetoothAdapter.isEnabled()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		} else
			setBluetooth();

		mSlaveLayout = (View) getLayoutInflater().inflate(
				R.layout.slave_layout, null);
		mConnectedLayout = (View) getLayoutInflater().inflate(
				R.layout.connected_layout, null);

		/**
		 * Buttons images
		 */
		mSlaveButton_left = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_left);
		mSlaveButton_right = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_right);
		mSlaveButton_forward = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_forward);
		mSlaveButton_backward = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_backward);
		mSlaveButton_left_head = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_left_head);
		mSlaveButton_right_head = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_right_head);
		mSlaveButton_light = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_light);
		mSlaveButton_ultrasonic = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_ultrasonic);
		mSlaveButton_center = (ImageButton) mSlaveLayout
				.findViewById(R.id.button_center);

		sonarTextView = (TextView) mSlaveLayout.findViewById(R.id.sensorText);
		lightTextView = (TextView) mSlaveLayout.findViewById(R.id.lightText);

		/**
		 * Add of the OnTouch listener to the different buttons
		 */
		mSlaveButton_left.setOnTouchListener(this);
		mSlaveButton_right.setOnTouchListener(this);
		mSlaveButton_forward.setOnTouchListener(this);
		mSlaveButton_backward.setOnTouchListener(this);
		mSlaveButton_left_head.setOnTouchListener(this);
		mSlaveButton_right_head.setOnTouchListener(this);
		mSlaveButton_light.setOnTouchListener(this);
		mSlaveButton_ultrasonic.setOnTouchListener(this);
		mSlaveButton_center.setOnTouchListener(this);

		mRobot = new Robot();

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		mMenu = menu;
		return true;
	}

	/**
	 * Display the layout of selected mode
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.isChecked())
			item.setChecked(false);
		else
			item.setChecked(true);

		switch (item.getItemId()) {
		case R.id.SlaveModeItem:

			if (mCurrentMode != SelectedMode.MODE_SLAVE) {
				mMainLayout.removeView(mConnectedLayout);
				mMainLayout.addView(mSlaveLayout);
				mCurrentMode = SelectedMode.MODE_SLAVE;
			}
			break;

		case R.id.ConnectedModeItem:
			if (mCurrentMode != SelectedMode.MODE_CONNECTED) {
				mMainLayout.removeView(mSlaveLayout);
				mMainLayout.addView(mConnectedLayout);
				mRunButton = (Button) findViewById(R.id.start_connected);

				mRunButton.setOnClickListener(this);
				mCurrentMode = SelectedMode.MODE_CONNECTED;
			}
			break;

		case R.id.MissionModeItem:
			if (mCurrentMode != SelectedMode.MODE_MISSION) {
				mMainLayout.removeView(mSlaveLayout);
				mMainLayout.removeView(mConnectedLayout);
				mCurrentMode = SelectedMode.MODE_MISSION;
			}
			break;

		case R.id.VirtualModeItem:
			if (mCurrentMode != SelectedMode.MODE_VIRTUAL) {
				mMainLayout.removeView(mSlaveLayout);
				mMainLayout.removeView(mConnectedLayout);
				mCurrentMode = SelectedMode.MODE_VIRTUAL;
			}
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Create the connection using the name and MAC address returned by the
	 * ListBluetoothActivity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)
			setBluetooth();
		if (requestCode == REQUEST_GET_DEVICE && resultCode == RESULT_OK) {
			Bundle bdle = data.getExtras();
			NXT_NAME = bdle.getString(NXT_EXTRA_NAME);
			NXT_MAC_ADDRESS = bdle.getString(NXT_EXTRA_ADD);

			mBluetoothAdapter.cancelDiscovery();

			mDevice = mBluetoothAdapter.getRemoteDevice(NXT_MAC_ADDRESS);
			mBluetoothThread = new BluetoothThread(true, mBluetoothAdapter,
					mDevice, mActHandler);
			mBTHandler = mBluetoothThread.getHandler();

			mBluetoothThread.init();

			mBluetoothThread.start();

			/**
			 * Initialization of the sensors as soon as the bluetooth connection
			 * is on
			 */
			sendBTmessage(BluetoothThread.INITIALIZE_SENSOR,
					BluetoothThread.LIGHT_SENSOR, 0, 0);
			sendBTmessage(BluetoothThread.INITIALIZE_SENSOR,
					BluetoothThread.ULTRASONIC_SENSOR, 0, 0);
			sendBTmessage(BluetoothThread.ASK_I2C_SENSOR_VALUE,
					BluetoothThread.ULTRASONIC_SENSOR, 0, 0);
			sendBTmessage(BluetoothThread.ASK_SENSOR_VALUE,
					BluetoothThread.LIGHT_SENSOR, 0, 0);

		}
		if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
			mMenu.findItem(R.id.SlaveModeItem).setEnabled(false);
			mMenu.findItem(R.id.ConnectedModeItem).setEnabled(false);
			mMenu.findItem(R.id.MissionModeItem).setEnabled(false);
		}
		if (requestCode == REQUEST_GET_DEVICE && resultCode == RESULT_CANCELED) {

			mMenu.findItem(R.id.SlaveModeItem).setEnabled(false);
			mMenu.findItem(R.id.ConnectedModeItem).setEnabled(false);
			mMenu.findItem(R.id.MissionModeItem).setEnabled(false);
		}
	};

	protected void setBluetooth() {
		mBluetoothAdapter.startDiscovery();
		Intent intent = new Intent(this, ListBluetoothActivity.class);
		startActivityForResult(intent, REQUEST_GET_DEVICE);
	}

	/**
	 * sends the message to the Handler of the bluetooth thread
	 * 
	 * @param task
	 *            code
	 * @param device
	 *            code (motor or sensor)
	 * @param value1
	 * @param value2
	 */
	public void sendBTmessage(int task, int device, int param1, int param2) {
		Bundle bundle = new Bundle();
		bundle.putInt("task", task);
		bundle.putInt("device", device);
		bundle.putInt("param1", param1);
		bundle.putInt("param2", param2);
		Message message = mActHandler.obtainMessage();
		message.setData(bundle);
		mBTHandler.sendMessage(message);

	}

}
