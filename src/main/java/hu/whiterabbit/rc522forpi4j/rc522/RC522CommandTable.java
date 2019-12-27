package hu.whiterabbit.rc522forpi4j.rc522;

//@SuppressWarnings("unused")
class RC522CommandTable {

	// reserved for future use?
	static final byte PCD_IDLE = 0x00;

	// starts and stops command execution
	static final byte COMMAND_REG = 0x01;

	// enable and disable interrupt request control bits
	static final byte COMM_I_EN_REG = 0x02;

	// enable and disable interrupt request control bits
	static final byte DIVL_EN_REG = 0x03;

	// interrupt request bits
	static final byte COMM_IRQ_REG = 0x04;

	// interrupt request bits
	static final byte DIV_IRQ_REG = 0x05;

	// error bits showing the error status of the last command executed
	static final byte ERROR_REG = 0x06;

	// communication status bits
	static final byte STATUS_1_REG = 0x07;

	// receiver and transmitter status bits
	static final byte STATUS_2_REG = 0x08;

	// input and output of 64 byte FIFO buffer
	static final byte FIFO_DATA_REG = 0x09;

	// number of bytes stored in the FIFO buffer
	static final byte FIFO_LEVEL_REG = 0x0A;

	// level for FIFO underflow and overflow warning
	static final byte WATER_LEVEL_REG = 0x0B;

	// miscellaneous control registers
	static final byte CONTROL_REG = 0x0C;

	// adjustments for bit-oriented frames
	static final byte BIT_FRAMING_REG = 0x0D;

	// bit position of the first bit-collision detected on the RF interface
	static final byte PCD_AUTHENT = 0x0E;

	// reserved for future use?
	static final byte PCD_RESETPHASE = 0x0F;

	// reserved for future use
	static final byte RESERVED_10 = 0x10;

	// defines general modes for transmitting and receiving
	static final byte MODE_REG = 0x11;

	// defines transmission data rate and framing
	static final byte TX_MODE_REG = 0x12;

	//  defines reception data rate and framing
	static final byte RX_MODE_REG = 0x13;

	//  controls the logical behavior of the antenna driver pins TX1 and TX2
	static final byte TX_CONTROL_REG = 0x14;

	// controls the setting of the transmission modulation
	static final byte TX_ASK_REG = 0x15;

	// selects the internal sources for the antenna driver
	static final byte TX_SEL_REG = 0x16;

	// selects internal receiver settings
	static final byte RX_SEL_REG = 0x17;

	// selects thresholds for the bit decoder
	static final byte RX_THRESHOLD_REG = 0x18;

	// defines demodulator settings
	static final byte DEMOD_REG = 0x19;

	// reserved for future use
	static final byte RESERVED_11 = 0x1A;

	// reserved for future use
	static final byte RESERVED_12 = 0x1B;

	// controls some MIFARE communication transmit parameters
	static final byte MF_TX_REG = 0x1C;

	// controls some MIFARE communication receive parameters
	static final byte MF_RX_REG = 0x1D;

	// reserved for future use
	static final byte RESERVED_14 = 0x1E;

	// selects the speed of the serial UART interface
	static final byte SERIAL_SPEED_REG = 0x1F;

	// reserved for future use
	static final byte RESERVED_20 = 0x20;

	// shows the MSB value of the CRC calculation
	static final byte CRC_RESULT_REG_MSB = 0x21;

	// shows the LSB value of the CRC calculation
	static final byte CRC_RESULT_REG_LSB = 0x22;

	// reserved for future use
	static final byte RESERVED_21 = 0x23;

	// controls the ModWidth setting
	static final byte MOD_WIDTH_REG = 0x24;

	// reserved for future use
	static final byte RESERVED_22 = 0x25;

	// configures the receiver gain
	static final byte RF_CFG_REG = 0x26;

	// selects the conductance of the antenna driver pins TX1 and TX2 for modulation
	static final byte GS_N_REG = 0x27;

	// defines the conductance of the p-driver output during periods of no modulation
	static final byte CW_GS_P_REG = 0x28;

	// defines the conductance of the p-driver output during periods of modulation
	static final byte MOD_GS_P_REG = 0x29;

	// defines settings for the internal timer
	static final byte T_MODE_REG = 0x2A;

	// defines settings for the internal timer
	static final byte T_PRESCALER_REG = 0x2B;

	// defines the 16-bit timer reload value
	static final byte T_RELOAD_REG_H = 0x2C;

	// defines the 16-bit timer reload value
	static final byte T_RELOAD_REG_L = 0x2D;

	// shows the 16-bit timer value
	static final byte T_COUNTER_VALUE_REG_H = 0x2E;

	// shows the 16-bit timer value
	static final byte T_COUNTER_VALUE_REG_L = 0x2F;

	// reserved for future use?
	static final byte PICC_READ = 0x30;

	// general test signal configuration
	static final byte TEST_SEL_1_REG = 0x31;

	// general test signal configuration and PRBS control
	static final byte TEST_SEL_2_REG = 0x32;

	// enables pin output driver on pins D1 to D7
	static final byte TEST_PIN_EN_REG = 0x33;

	//  defines the values for D1 to D7 when it is used as an I/O bus
	static final byte TEST_PIN_VALUE_REG = 0x34;

	// shows the status of the internal test bus
	static final byte TEST_BUS_REG = 0x35;

	// controls the digital self test
	static final byte AUTO_TEST_REG = 0x36;

	// shows the software version
	static final byte VERSION_REG = 0x37;

	// controls the pins AUX1 and AUX2
	static final byte ANALOG_TEST_REG = 0x38;

	// defines the test value for TestDAC1
	static final byte TEST_DAC_1_REG = 0x39;

	// defines the test value for TestDAC2
	static final byte TEST_DAC_2_REG = 0x3A;

	// shows the value of ADC I and Q channels
	static final byte TEST_ADC_REG = 0x3B;

	// reserved for production tests
	static final byte RESERVED_31 = 0x3C;

	// reserved for production tests
	static final byte RESERVED_32 = 0x3D;

	// reserved for production tests
	static final byte RESERVED_33 = 0x3E;

	// reserved for production tests
	static final byte RESERVED_34 = 0x3F;

	/*
		Unknown commands
	 */

	static final byte HALT_1 = 0x50;

	static final byte HALT_2 = 0x00;

	// Wake-up
	static final byte WUPA = 0x52;

	// Authentication with Key A
	static final byte PICC_AUTHENT1A = 0x60;

	// Authentication with Key B
	static final byte PICC_AUTHENT1B = 0x61;

	// Anticollision CL1
	static final byte ANTICOLLISION_CL1_1 = (byte) 0x93;

	static final byte ANTICOLLISION_CL1_2 = 0x20;

	static final byte SELECT_CL1_1 = (byte) 0x93;

	static final byte SELECT_CL1_2 = (byte) 0x70;

	static final byte PICC_WRITE = (byte) 0xA0;

	static final int MI_OK = 0;

	static final int MI_NOTAGERR = 1;

	static final int MI_ERR = 2;

}
