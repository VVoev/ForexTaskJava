package models;

import com.fxcore2.*;
import globalConstants.UserinfoConstants;
import listeners.ResponseListener;

import java.util.ArrayList;

public class Order {
    private O2GSession session;
    private IO2GResponseListener responseListener;

    public Order(O2GSession session, IO2GResponseListener responseListener) {
        this.session = session;
        this.responseListener = responseListener;
    }


    public void createSingleOrder(String buyOrSell, int amount,O2GAccountRow account, O2GOfferRow offer) throws Exception {
        O2GRequestFactory requestFactory = session.getRequestFactory();
        O2GValueMap valueMap = requestFactory.createValueMap();


        valueMap.setString(O2GRequestParamsEnum.ACCOUNT_ID, account.getAccountID());
        valueMap.setString(O2GRequestParamsEnum.OFFER_ID, offer.getOfferID());
        valueMap.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
        valueMap.setString(O2GRequestParamsEnum.ORDER_TYPE, Constants.Orders.TrueMarketOpen);

        if (buyOrSell.equals("b")) {
            valueMap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
        }
        else if (buyOrSell.equals("s")) {
            valueMap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
        }

        valueMap.setInt(O2GRequestParamsEnum.AMOUNT, account.getBaseUnitSize() * amount);
        O2GRequest request = requestFactory.createOrderRequest(valueMap);

        ((ResponseListener) responseListener).setRequestID(request.getRequestId());
        session.sendRequest(request);

        while (!((ResponseListener) responseListener).hasResponse()) {
            Thread.sleep(UserinfoConstants.WAIT_TIME);
        }

    }


    public ArrayList<String> createMarderOrder(int amount, O2GAccountRow account, ArrayList<O2GOfferRow> offers,String buyOrSell) throws Exception {
        ArrayList<String> orderIDs = new ArrayList<>();
        for (O2GOfferRow offer : offers) {
            createSingleOrder(buyOrSell, amount,account, offer);

            O2GResponse response = ((ResponseListener) responseListener).getResponse();
            O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
            O2GOrderResponseReader responseReader = readerFactory.createOrderResponseReader(response);

            orderIDs.add(responseReader.getOrderID());
        }
        return orderIDs;

    }

    public void closeSingleOrder(O2GTradeRow trade, O2GAccountRow account) throws Exception {

        O2GRequestFactory requestFactory = session.getRequestFactory();
        O2GValueMap valueMap = requestFactory.createValueMap();

        valueMap.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
        valueMap.setString(O2GRequestParamsEnum.ORDER_TYPE, Constants.Orders.TrueMarketClose);
        valueMap.setString(O2GRequestParamsEnum.ACCOUNT_ID, account.getAccountID());
        valueMap.setString(O2GRequestParamsEnum.OFFER_ID, trade.getOfferID());
        valueMap.setString(O2GRequestParamsEnum.TRADE_ID, trade.getTradeID());

        if (trade.getBuySell().equals(Constants.Buy)) {
            valueMap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
        }
        if (trade.getBuySell().equals(Constants.Sell)) {
            valueMap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
        }

        valueMap.setInt(O2GRequestParamsEnum.AMOUNT, trade.getAmount());
        O2GRequest request = requestFactory.createOrderRequest(valueMap);
        ((ResponseListener) responseListener).setRequestID(request.getRequestId());
        session.sendRequest(request);

        while (!((ResponseListener) responseListener).hasResponse()) {
            Thread.sleep(UserinfoConstants.WAIT_TIME);
        }

    }


    public ArrayList<String> closeMultiOpenOrders(ArrayList<O2GTradeRow> trades, O2GAccountRow account) throws Exception {
        ArrayList<String> closedOrderIDs = new ArrayList<>();
        for (O2GTradeRow trade : trades) {
            closeSingleOrder(trade,account);

            O2GResponse response = ((ResponseListener) responseListener).getResponse();
            O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
            O2GOrderResponseReader responseReader = readerFactory.createOrderResponseReader(response);

            closedOrderIDs.add(responseReader.getOrderID());
        }
        return closedOrderIDs;
    }


    public ArrayList<O2GTradeRow> getTrades() throws Exception {

        ArrayList<O2GTradeRow> trades = new ArrayList<>();
        O2GRequestFactory requestFactory = session.getRequestFactory();
        O2GRequest request = requestFactory.createRefreshTableRequest(O2GTableType.TRADES);
        ((ResponseListener) responseListener).setRequestID(request.getRequestId());
        session.sendRequest(request);

        while (!((ResponseListener) responseListener).hasResponse()) {
            Thread.sleep(UserinfoConstants.WAIT_TIME);
        }

        O2GResponse response = ((ResponseListener) responseListener).getResponse();
        O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
        O2GTradesTableResponseReader tradesResponseReader = readerFactory.createTradesTableReader(response);

        for (int i = 0; i < tradesResponseReader.size(); i++) {
            trades.add(tradesResponseReader.getRow(i));
        }
        return trades;
    }

    public void printOrders(ArrayList<O2GTradeRow> trades, ArrayList<O2GOfferRow> offers) {
        System.out.println("\nOpen Positions");
        System.out.format("%-8s%-10s%-15s%-15s%-10s%-5s%-15s%-15s\n", "Index", "models.Account", "Symbol", "Usd Mr", "Amount", "S/B", "Open", "Close");

        int index = 1;
        for (O2GTradeRow trade : trades) {
            O2GOfferRow offer = findOffer(offers, trade.getOfferID());

            System.out.format("%-8d%-10s%-15s%-15.2f%-10d%-5s%-15.5f%-15.5f\n", index, trade.getAccountID(),
                    offer.getInstrument(), trade.getUsedMargin(),
                    trade.getAmount() / 1000, trade.getBuySell(),
                    trade.getOpenRate(),
                    trade.getBuySell().equals(Constants.Buy) ? offer.getBid() : offer.getAsk());

            index++;
        }
    }

    private O2GOfferRow findOffer(ArrayList<O2GOfferRow> offers, String offerID) {
        for (O2GOfferRow offer : offers) {
            if (offer.getOfferID().equals(offerID)) {
                return offer;
            }
        }
        return null;
    }
}
