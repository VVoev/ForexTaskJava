package listeners;

import com.fxcore2.IO2GResponseListener;
import com.fxcore2.O2GResponse;

public class ResponseListener implements IO2GResponseListener {
    private String requestID;
    private O2GResponse response;


    public void setRequestID(String requestID) {
        response = null;
        this.requestID = requestID;
    }

    public boolean hasResponse() {
        return response != null;
    }

    public O2GResponse getResponse() {
        return response;
    }

    public void onRequestCompleted(String requestID, O2GResponse response) {
        if (this.requestID.equals(response.getRequestId())) {
            this.response = response;
        }
    }

    public void onRequestFailed(String sRequestID, String sError) {
        System.out.println("Request failed: " + sError);
    }

    public void onTablesUpdates(O2GResponse response) { }
}
