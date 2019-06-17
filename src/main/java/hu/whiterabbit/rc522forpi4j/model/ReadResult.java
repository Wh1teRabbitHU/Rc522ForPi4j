package hu.whiterabbit.rc522forpi4j.model;

import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class ReadResult {

	private final CommunicationStatus status;

	private final CardData cardData;

	public ReadResult(CommunicationStatus status) {
		this.status = status;
		this.cardData = null;
	}

	public ReadResult(RequestResult requestResult) {
		this.status = requestResult.getStatus();

		if (requestResult.getResponseData() == null) {
			this.cardData = null;
		} else {
			this.cardData = new CardData(requestResult.getResponseData());
		}
	}

	public ReadResult(CommunicationStatus status, byte[] cardTag) {
		this.status = status;
		this.cardData = new CardData(cardTag);
	}

	public ReadResult(CommunicationStatus status, CardData cardData) {
		this.status = status;
		this.cardData = cardData;
	}

	public CommunicationStatus getStatus() {
		return status;
	}

	public CardData getCardData() {
		return cardData;
	}

	public boolean isSuccess() {
		return status == CommunicationStatus.SUCCESS;
	}
}
