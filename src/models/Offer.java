package models;

import com.fxcore2.*;
import globalConstants.UserinfoConstants;
import listeners.ResponseListener;

import java.util.ArrayList;

public class Offer {

    private O2GSession session;
    private IO2GResponseListener responseListener;

    public Offer(O2GSession session, IO2GResponseListener responseListener) {
        this.session = session;
        this.responseListener = responseListener;
    }

    public ArrayList<O2GOfferRow> getAllOffers(ArrayList<String> currencies) {
        ArrayList<O2GOfferRow> offers = new ArrayList<O2GOfferRow>();

        O2GLoginRules loginRules = session.getLoginRules();
        O2GResponse response = loginRules.getTableRefreshResponse(O2GTableType.OFFERS);

        O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
        O2GOffersTableResponseReader offersResponseReader = readerFactory.createOffersTableReader(response);

        addEachOfferToOffers(currencies, offersResponseReader, offers);
        return offers;

    }


    public ArrayList<O2GOfferRow> updateOffersAccordingly(ArrayList<String> currencies) throws Exception {
        ArrayList<O2GOfferRow> offers = new ArrayList<O2GOfferRow>();
        O2GRequestFactory requestFactory = session.getRequestFactory();
        O2GRequest request = requestFactory.createRefreshTableRequest(O2GTableType.OFFERS);
        ((ResponseListener) responseListener).setRequestID(request.getRequestId());
        session.sendRequest(request);

        while (!((ResponseListener) responseListener).hasResponse()) {
            Thread.sleep(UserinfoConstants.WAIT_TIME);
        }

        O2GResponse response = ((ResponseListener) responseListener).getResponse();
        O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();

        O2GOffersTableResponseReader offersResponseReader = readerFactory.createOffersTableReader(response);
        addEachOfferToOffers(currencies, offersResponseReader, offers);

        return offers;
    }

    public void printer(ArrayList<O2GOfferRow> offers) {
        System.out.println("\n Rates");
        System.out.format(UserinfoConstants.FORMAT_ACTION, "Index", "Symbol", "Sell", "Buy", "High", "Low");
        int counter = 1;
        for (O2GOfferRow offer : offers) {
            System.out.format(UserinfoConstants.FORMAT, counter, offer.getInstrument(), offer.getBid(),
                    offer.getAsk(), offer.getHigh(), offer.getLow());
            counter++;
        }
    }

    private void addEachOfferToOffers(ArrayList<String> currencies, O2GOffersTableResponseReader offersResponseReader, ArrayList<O2GOfferRow> offers) {
        for (String currency : currencies) {
            for (int i = 0; i < offersResponseReader.size(); i++) {
                O2GOfferRow offer = offersResponseReader.getRow(i);
                if (offer.getInstrument().equals(currency)) {
                    offers.add(offer);
                    break;
                }

            }
        }
    }

}
