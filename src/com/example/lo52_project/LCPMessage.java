package com.example.lo52_project;

/**
 * LCPMessage implements different functions to transform data into the
 * corresponding byte[] table
 * 
 * @author Lois Aubree & Lucie Boutou
 */

public class LCPMessage {

	/**
	 * command types constants (indicates type of packet being sent or received)
	 */
	public static byte DIRECT_COMMAND_REPLY = 0x00;
	public static byte SYSTEM_COMMAND_REPLY = 0x01;
	public static byte REPLY_COMMAND = 0x02;
	public static byte DIRECT_COMMAND_NOREPLY = (byte) 0x80;
	public static byte SYSTEM_COMMAND_NOREPLY = (byte) 0x81;

	/**
	 * direct Commands
	 */
	public static final byte START_PROGRAM = 0x00;
	public static final byte STOP_PROGRAM = 0x01;
	public static final byte PLAY_SOUND_FILE = 0x02;
	public static final byte PLAY_TONE = 0x03;
	public static final byte SET_OUTPUT_STATE = 0x04;
	public static final byte SET_INPUT_MODE = 0x05;
	public static final byte GET_OUTPUT_STATE = 0x06;
	public static final byte GET_INPUT_VALUES = 0x07;
	public static final byte RESET_SCALED_INPUT_VALUE = 0x08;
	public static final byte MESSAGE_WRITE = 0x09;
	public static final byte RESET_MOTOR_POSITION = 0x0A;
	public static final byte GET_BATTERY_LEVEL = 0x0B;
	public static final byte STOP_SOUND_PLAYBACK = 0x0C;
	public static final byte KEEP_ALIVE = 0x0D;
	public static final byte LS_GET_STATUS = 0x0E;
	public static final byte LS_WRITE = 0x0F;
	public static final byte LS_READ = 0x10;
	public static final byte GET_CURRENT_PROGRAM_NAME = 0x11;
	public static final byte MESSAGE_READ = 0x13;

	/**
	 * sensor types
	 */
	public static final byte NO_SENSOR_TYPE = (byte) 0x00;
	public static final byte SWITCH_TYPE = (byte) 0x01;
	public static final byte TEMPERATURE_TYPE = (byte) 0x02;
	public static final byte REFLECTION_TYPE = (byte) 0x03;
	public static final byte ANGLE_TYPE = (byte) 0x04;
	public static final byte LIGHT_ACTIVE_TYPE = (byte) 0x05;
	public static final byte LIGHT_INACTIVE_TYPE = (byte) 0x06;
	public static final byte SOUND_DB_TYPE = (byte) 0x07;
	public static final byte SOUND_DBA_TYPE = (byte) 0x08;
	public static final byte CUSTOM_TYPE = (byte) 0x09;
	public static final byte LOWSPEED_TYPE = (byte) 0x0A;
	public static final byte LOWSPEED_9V_TYPE = (byte) 0x0B;
	public static final byte NO_OF_SENSOR_TYPES = (byte) 0x0C;

	/**
	 * sensor modes
	 */
	public static final byte RAW_MODE = (byte) 0x00;
	public static final byte BOOLEAN_MODE = (byte) 0x20;
	public static final byte TRANSITION_CNT_MODE = (byte) 0x40;
	public static final byte PERIOD_COUNTER_MODE = (byte) 0x60;
	public static final byte PCT_FULL_SCALE_MODE = (byte) 0x80;
	public static final byte CELSIUS_MODE = (byte) 0xA0;

	/**
	 * @param motor code
	 * @param speed value
	 * @return message to set a particular speed of a motor
	 * the motor will run until a message sets the speed at 0
	 */
	public static byte[] getMotorMessage(int motor, int speed) {
		byte[] message = new byte[12];

		message[0] = DIRECT_COMMAND_NOREPLY;
		message[1] = SET_OUTPUT_STATE;
		/**
		 * output port
		 */
		message[2] = (byte) motor;

		if (speed == 0) {
			message[3] = 0;
			message[4] = 0;
			message[5] = 0;
			message[6] = 0;
			message[7] = 0;

		} else {
			/**
			 * power set option (range: -100 - 100)
			 */
			message[3] = (byte) speed;
			/**
			 * mode byte (bit-field): MOTORON + BREAK
			 */
			message[4] = 0x03;
			/**
			 * regulation mode: REGULATION_MODE_MOTOR_SPEED
			 */
			message[5] = 0x01;
			/**
			 * turn ratio (SBYTE; -100 - 100)
			 */
			message[6] = 0x00;
			/**
			 * runState: MOTOR_RUN_STATE_RUNNING
			 */
			message[7] = 0x20;
		}

		/**
		 * tachoLimit: run forever
		 */
		message[8] = 0;
		message[9] = 0;
		message[10] = 0;
		message[11] = 0;

		return message;

	}

	/**
	 * @param motor code
	 * @param speed value
	 * @param end value
	 * @return message to set the speed and a rotation angle to a motor
	 */
	public static byte[] getMotorMessage(int motor, int speed, int end) {
		byte[] message = getMotorMessage(motor, speed);

		/**
		 * tachoLimit
		 */
		message[8] = (byte) end;
		message[9] = (byte) (end >> 8);
		message[10] = (byte) (end >> 16);
		message[11] = (byte) (end >> 24);

		return message;
	}

	/**
	 * @param motor
	 * @return message to get the state of a motor
	 */
	public static byte[] getOutputStateMessage(int motor) {
		byte[] message = new byte[3];

		message[0] = DIRECT_COMMAND_REPLY;
		message[1] = GET_OUTPUT_STATE;
		/**
		 * output port
		 */
		message[2] = (byte) motor;

		return message;
	}

	/**
	 * @param sensor code
	 * @return message to initialize a sensor
	 */
	public static byte[] getInputModeMessage(int sensor) {
		byte[] message = new byte[5];

		message[0] = DIRECT_COMMAND_NOREPLY;
		message[1] = SET_INPUT_MODE;
		message[2] = (byte) sensor;
		message[4] = RAW_MODE;

		switch (sensor) {
		case 2:
			message[3] = LIGHT_INACTIVE_TYPE;
			break;
		case 3:
			message[3] = LOWSPEED_9V_TYPE;
			break;
		default:
			break;
		}

		return message;
	}

	/**
	 * @param sensor code
	 * @return message to get the value of a sensor
	 */
	public static byte[] getInputStateMessage(int sensor) {
		byte[] message = new byte[3];

		message[0] = DIRECT_COMMAND_REPLY;
		message[1] = GET_INPUT_VALUES;
		/**
		 *  input port
		 */
		message[2] = (byte) sensor;

		return message;
	}

	/**
	 * @param sensor code
	 * @return message to get the NXT robot to ask its I2C sensor
	 * the value of the register which holds the sensor value
	 */
	public static byte[] getI2CCommandMessage(int sensor) {
		byte[] message = new byte[7];

		message[0] = DIRECT_COMMAND_REPLY;
		message[1] = LS_WRITE;
		message[2] = (byte) sensor;
		/**
		 * number of bytes which will be send to the I2C device by the NXT
		 */
		message[3] = (byte) 0x02;
		/**
		 * number of bytes the I2C device will send back
		 */
		message[4] = (byte) 0x01;
		/**
		 * I2C address of the device (usually 0x02 for NXT)
		 */
		message[5] = (byte) 0x02;
		/**
		 * register to write to or to read from
		 */
		message[6] = (byte) 0x42;

		return message;
	}

	/**
	 * @param sensor code
	 * @return message to get the state of the I2C sensor
	 * (if its value is ready to be read or not)
	 */
	public static byte[] getI2CStateMessage(int sensor) {
		byte[] message = new byte[3];

		message[0] = DIRECT_COMMAND_REPLY;
		message[1] = LS_GET_STATUS;
		message[2] = (byte) sensor;

		return message;
	}

	/**
	 * @param sensor code
	 * @return message to get the value of the I2C sensor
	 */
	public static byte[] getI2CStateValues(int sensor) {
		byte[] message = new byte[3];

		message[0] = DIRECT_COMMAND_REPLY;
		message[1] = LS_READ;
		message[2] = (byte) sensor;

		return message;
	}

}