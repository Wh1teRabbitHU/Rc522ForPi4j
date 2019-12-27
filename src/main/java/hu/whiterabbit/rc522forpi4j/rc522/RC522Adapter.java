package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationResult;

public interface RC522Adapter {

	/**
	 * Initializing the RC522Adapter. It creates and initialize the raspberryPiAdapter instance and resets the RC522
	 * module to it's default state
	 *
	 * @param speed      Speed of the communication
	 * @param resetPin   The connected reset pin's number
	 * @param spiChannel The channel number used for SPI communication
	 */
	void init(int speed, int resetPin, int spiChannel);

	/**
	 * Resets your RC522 module to it's default state
	 */
	void reset();

	/**
	 * It selects a card and returns with the tagId. First it send a request, then it set up the anti-collision and
	 * finally it select the card for further operations
	 *
	 * @return The CommunicationResult object with the tagId array
	 */
	CommunicationResult selectCard();

	/**
	 * Authenticates a given block on your already selected card. You must select your card before this process!
	 *
	 * @param authMode     Either it can be PICC_AUTHENT1A or PICC_AUTHENT1B, depends on the key type, 1 byte long
	 * @param blockAddress Target block address, 1 byte long
	 * @param key          The authentication's key, 6 bytes long
	 * @param uid          The selected cards tagId (uid), 4 bytes long
	 * @return The result of the authentication process
	 */
	CommunicationResult authCard(byte authMode, byte blockAddress, byte[] key, byte[] uid);

	/**
	 * Reads data from block. You must authenticate this block before reading from it! The returned CommunicationResult
	 * object contains the block data, which is 16 bytes long!
	 *
	 * @param blockAddress The address of your target block
	 * @return The result object with the block data
	 */
	CommunicationResult read(byte blockAddress);

	/**
	 * Writes data to the target block. You must authenticate this block before writing to it!
	 *
	 * @param blockAddress The target block's address
	 * @param data         A 16 bytes long data array
	 * @return The result of the communication
	 */
	CommunicationResult write(byte blockAddress, byte[] data);

}
