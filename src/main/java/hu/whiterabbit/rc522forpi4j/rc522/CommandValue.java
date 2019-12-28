package hu.whiterabbit.rc522forpi4j.rc522;

public enum CommandValue {
	/**
	 * Places the MFRC522 in Idle mode. The Idle command also terminates itself
	 */
	IDLE(0B0000),

	/**
	 * Transfers 25 bytes from the FIFO buffer to the internal buffer.
	 * To read out the 25 bytes from the internal buffer the Mem command must be started with
	 * an empty FIFO buffer. In this case, the 25 bytes are transferred from the internal buffer to
	 * the FIFO.
	 * During a hard power-down (using pin NRSTPD), the 25 bytes in the internal buffer remain
	 * unchanged and are only lost if the power supply is removed from the MFRC522.
	 * This command automatically terminates when finished and the Idle command becomes
	 * active.
	 */
	MEM(0B0001),

	/**
	 * This command generates a 10-byte random number which is initially stored in the internal
	 * buffer. This then overwrites the 10 bytes in the internal 25-byte buffer. This command
	 * automatically terminates when finished and the MFRC522 returns to Idle mode.
	 */
	GEN_RANDOM_ID(0B0010),

	/**
	 * The FIFO buffer content is transferred to the CRC coprocessor and the CRC calculation is
	 * started. The calculation result is stored in the CRCResultReg register. The CRC
	 * calculation is not limited to a dedicated number of bytes. The calculation is not stopped
	 * when the FIFO buffer is empty during the data stream. The next byte written to the FIFO
	 * buffer is added to the calculation.
	 * The CRC preset value is controlled by the ModeReg register’s CRCPreset[1:0] bits. The
	 * value is loaded in to the CRC coprocessor when the command starts.
	 * This command must be terminated by writing a command to the CommandReg register,
	 * such as, the Idle command.
	 * If the AutoTestReg register’s SelfTest[3:0] bits are set correctly, the MFRC522 enters Self
	 * Test mode. Starting the CalcCRC command initiates a digital self test. The result of the
	 * self test is written to the FIFO buffer.
	 */
	CALC_CRC(0B0011),

	/**
	 * The FIFO buffer content is immediately transmitted after starting this command. Before
	 * transmitting the FIFO buffer content, all relevant registers must be set for data
	 * transmission.
	 * This command automatically terminates when the FIFO buffer is empty. It can be
	 * terminated by another command written to the CommandReg register.
	 */
	TRANSMIT(0B0100),

	/**
	 * This command does not influence any running command in the CommandReg register. It
	 * can be used to manipulate any bit except the CommandReg register Command[3:0] bits,
	 * for example, the RcvOff bit or the PowerDown bit.
	 */
	NO_CMD_CHANGE(0B0111),

	/**
	 * The MFRC522 activates the receiver path and waits for a data stream to be received. The
	 * correct settings must be chosen before starting this command.
	 * This command automatically terminates when the data stream ends. This is indicated
	 * either by the end of frame pattern or by the length byte depending on the selected frame
	 * type and speed.
	 * Remark: If the RxModeReg register’s RxMultiple bit is set to logic 1, the Receive
	 * command will not automatically terminate. It must be terminated by starting another
	 * command in the CommandReg register.
	 */
	RECEIVE(0B1000),

	/**
	 * This command continuously repeats the transmission of data from the FIFO buffer and the
	 * reception of data from the RF field. The first action is transmit and after transmission the
	 * command is changed to receive a data stream.
	 * Each transmit process must be started by setting the BitFramingReg register’s StartSend
	 * bit to logic 1. This command must be cleared by writing any command to the
	 * CommandReg register.
	 * Remark: If the RxModeReg register’s RxMultiple bit is set to logic 1, the Transceive
	 * command never leaves the receive state because this state cannot be cancelled
	 * automatically.
	 */
	TRANSCEIVE(0B1100),

	/**
	 * This command manages MIFARE authentication to enable a secure communication to
	 * any MIFARE Mini, MIFARE 1K and MIFARE 4K card. The following data is written to the
	 * FIFO buffer before the command can be activated:
	 * • Authentication command code (60h, 61h)
	 * • Block address
	 * • Sector key byte 0
	 * • Sector key byte 1
	 * • Sector key byte 2
	 * • Sector key byte 3
	 * • Sector key byte 4
	 * • Sector key byte 5
	 * • Card serial number byte 0
	 * • Card serial number byte 1
	 * • Card serial number byte 2
	 * • Card serial number byte 3
	 * In total 12 bytes are written to the FIFO.
	 * Remark: When the MFAuthent command is active all access to the FIFO buffer is
	 * blocked. However, if there is access to the FIFO buffer, the ErrorReg register’s WrErr bit is
	 * set.
	 * This command automatically terminates when the MIFARE card is authenticated and the
	 * Status2Reg register’s MFCrypto1On bit is set to logic 1.
	 * This command does not terminate automatically if the card does not answer, so the timer
	 * must be initialized to automatic mode. In this case, in addition to the IdleIRq bit, the
	 * TimerIRq bit can be used as the termination criteria. During authentication processing, the
	 * RxIRq bit and TxIRq bit are blocked. The Crypto1On bit is only valid after termination of
	 * the MFAuthent command, either after processing the protocol or writing Idle to the
	 * CommandReg register.
	 * If an error occurs during authentication, the ErrorReg register’s ProtocolErr bit is set to
	 * logic 1 and the Status2Reg register’s Crypto1On bit is set to logic 0.
	 */
	MF_AUTHENT(0B1110),

	/**
	 * This command performs a reset of the device. The configuration data of the internal buffer
	 * remains unchanged. All registers are set to the reset values. This command automatically
	 * terminates when finished.
	 * Remark: The SerialSpeedReg register is reset and therefore the serial data rate is set to
	 * 9.6 kBd.
	 */
	SOFT_RESET(0B1111);

	public final byte code;

	CommandValue(int code) {
		this.code = (byte) code;
	}
}
