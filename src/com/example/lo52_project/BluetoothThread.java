package com.example.lo52_project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * BluetoothThread implements the connection from the Android application to the
 * NXT robot.
 * 
 * @author Lois Aubree & Lucie Boutou
 */

public class BluetoothThread extends Thread {

	/**
	 * variable codes used in the BTHandler
	 */

	public static final int MOTOR_A = 0;
	public static final int MOTOR_B = 1;
	public static final int MOTOR_C = 2;

	public static final int LIGHT_SENSOR = 2;
	public static final int ULTRASONIC_SENSOR = 3;

	public static final int INITIALIZE_SENSOR = 1000;
	public static final int ASK_SENSOR_VALUE = 1001;
	public static final int ASK_I2C_SENSOR_VALUE = 1002;
	public static final int READ_I2C_SENSOR_VALUE = 1003;
	public static final int UPDATE_MOTOR_SPEED = 1004;
	public static final int ASK_MOTOR_STATE = 1005;

	public static final int RETURN_SENSOR_VALUE = 2000;
	public static final int RETURN_I2C_SENSOR_VALUE = 2001;
	public static final int RETURN_MOTOR_STATE = 2002;

	private BluetoothDevice mDevice;
	private BluetoothSocket mBluetoothSocket;
	private static final UUID UUID_SSP = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private InputStream mInputStream;
	private OutputStream mOutputStream;

	private boolean isConnected = false;

	private byte[] mReturnMessage;

	private Handler mActHandler;

	/**
	 * This Handler handles messages from the main activity of the application
	 * and send them on the OuputStream.
	 */
	private Handler mBTHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {

			switch (message.getData().getInt("task")) {
			case INITIALIZE_SENSOR:
				try {
					setMessage(LCPMessage.getInputModeMessage(message.getData()
							.getInt("device")));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case ASK_SENSOR_VALUE:
				try {
					setMessage(LCPMessage.getInputStateMessage(message
							.getData().getInt("device")));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case ASK_I2C_SENSOR_VALUE:
				try {
					setMessage(LCPMessage.getI2CCommandMessage(message
							.getData().getInt("device")));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case READ_I2C_SENSOR_VALUE:
				try {
					setMessage(LCPMessage.getI2CStateValues(message.getData()
							.getInt("device")));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			case UPDATE_MOTOR_SPEED:
				try {
					if (message.getData().getInt("param2") == 0)
						setMessage(LCPMessage.getMotorMessage(message.getData()
								.getInt("device"),
								message.getData().getInt("param1")));
					else
						setMessage(LCPMessage.getMotorMessage(message.getData()
								.getInt("device"),
								message.getData().getInt("param1"), message
										.getData().getInt("param2")));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case ASK_MOTOR_STATE:
				try {
					setMessage(LCPMessage.getOutputStateMessage(message
							.getData().getInt("device")));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}

	};

	public BluetoothThread(boolean deviceAlReadyPaired,
			BluetoothAdapter adapter, BluetoothDevice device, Handler actHandler) {
		mDevice = device;
		mActHandler = actHandler;
	}

	public void init() {
		ConnectToNXT();
	}

	@Override
	public void run() {
		while (isConnected) {
			try {
				/**
				 * receiving messages from InputStream
				 */
				mReturnMessage = receiveMessage();
				if ((mReturnMessage.length >= 2)
						&& ((mReturnMessage[0] == LCPMessage.REPLY_COMMAND) || (mReturnMessage[0] == LCPMessage.DIRECT_COMMAND_NOREPLY)))
					decodeMessage(mReturnMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			mBluetoothSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Proceeds to the connection between the Android device and the NXT robot
	 * If the robot has never been paired, correctly catch the exception and ask
	 * to pair the robot.
	 */
	private void ConnectToNXT() {
		BluetoothSocket tmp = null;
		if (mDevice != null) {
			try {
				tmp = mDevice.createRfcommSocketToServiceRecord(UUID_SSP);

				tmp.connect();
				isConnected = true;

				mBluetoothSocket = tmp;
				mOutputStream = mBluetoothSocket.getOutputStream();
				mInputStream = mBluetoothSocket.getInputStream();

			} catch (IOException e) {
				Method m;
				try {
					m = mDevice.getClass().getMethod("createRfcommSocket",
							new Class[] { int.class });
					tmp = (BluetoothSocket) m.invoke(mDevice, 1);
					tmp.connect();
					isConnected = true;

					mBluetoothSocket = tmp;
					mOutputStream = mBluetoothSocket.getOutputStream();
					mInputStream = mBluetoothSocket.getInputStream();

				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}

		}

	}

	/**
	 * writes the message data on the OuputStream
	 * 
	 * @param message
	 *            data
	 * @throws IOException
	 */
	private void setMessage(byte[] message) throws IOException {
		if (mOutputStream == null)
			throw new IOException();

		/**
		 * send message length
		 */
		int messageLength = message.length;
		mOutputStream.write(messageLength);
		mOutputStream.write(messageLength >> 8);
		mOutputStream.write(message, 0, message.length);
	}

	/**
	 * reads the message data on the InputStream
	 * 
	 * @return message data
	 * @throws IOException
	 */
	private byte[] receiveMessage() throws IOException {
		if (mInputStream == null)
			throw new IOException();

		int length = mInputStream.read();
		length = (mInputStream.read() << 8) + length;
		byte[] returnMessage = new byte[length];
		mInputStream.read(returnMessage);
		return returnMessage;
	}

	/**
	 * decodes the received messages and send them the main activity of the
	 * application
	 * 
	 * @param message
	 *            data
	 */
	private void decodeMessage(byte[] message) {
		switch (message[1]) {
		case LCPMessage.GET_INPUT_VALUES:
			if (message[2] == 0)
				sendActMessage(RETURN_SENSOR_VALUE, LIGHT_SENSOR,
						(int) Math.abs(message[10]));
			break;
		case LCPMessage.LS_READ:
			if (message[2] == 0)
				sendActMessage(RETURN_I2C_SENSOR_VALUE, ULTRASONIC_SENSOR,
						(int) message[4]);
			break;
		case LCPMessage.GET_OUTPUT_STATE:
			if (message[2] == 0)
				sendActMessage(RETURN_MOTOR_STATE, MOTOR_A, (int) message[13]);
		default:
			break;
		}

	}

	/**
	 * sends the message to the Handler of the main activity of the application
	 * 
	 * @param task
	 *            code
	 * @param device
	 *            code (motor or sensor)
	 * @param value
	 */
	private void sendActMessage(int task, int device, int param) {
		Bundle bundle = new Bundle();
		bundle.putInt("task", task);
		bundle.putInt("device", device);
		bundle.putInt("param1", param);
		Message message = mBTHandler.obtainMessage();
		message.setData(bundle);
		mActHandler.sendMessage(message);
	}

	public Handler getHandler() {
		return mBTHandler;
	}
}