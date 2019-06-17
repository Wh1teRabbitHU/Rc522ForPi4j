package hu.whiterabbit.rc522forpi4j.model;

public class RequestResult {

	private final CommunicationStatus status;

	private final byte[] responseData;

	public RequestResult(CommunicationStatus status) {
		this.status = status;
		this.responseData = null;
	}

	public RequestResult(CommunicationStatus status, byte[] responseData) {
		this.status = status;
		this.responseData = responseData;
	}

	public CommunicationStatus getStatus() {
		return status;
	}

	public byte[] getResponseData() {
		return responseData;
	}

	public boolean isSuccess() {
		return status == CommunicationStatus.SUCCESS;
	}

}
