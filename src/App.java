import com.fxcore2.*;
import globalConstants.UserinfoConstants;
import listeners.ResponseListener;
import listeners.SessionStatusListener;
import models.Account;
import models.Offer;
import models.Order;

import java.util.ArrayList;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        O2GSession session = null;
        Scanner in = new Scanner(System.in);

        try {
            wellcomingMessage();
            System.out.print("Username:" + UserinfoConstants.USERNAME);
            newLine();
            System.out.print("Password:" + UserinfoConstants.PASSWORD);
            newLine();
            System.out.print("Choosen Terminal:Demo");
            String connection = UserinfoConstants.TERMINAL;

            ResponseListener responseListener = new ResponseListener();
            SessionStatusListener statusListener = new SessionStatusListener();
            session = O2GTransport.createSession();

            subsribeSession(session, statusListener, responseListener);
            loginSession(session);

            while (!statusListener.isConnected() && !statusListener.hasError()) {
                Thread.sleep(UserinfoConstants.WAIT_TIME);
            }

            Offer offer = new Offer(session, responseListener);
            Account account = new Account(session);
            Order order = new Order(session, responseListener);

            ArrayList<String> currenciesList = new ArrayList();
            currenciesList.add("EUR/USD");
            currenciesList.add("GBP/USD");
            currenciesList.add("USD/JPY");

            ArrayList<O2GOfferRow> offers = offer.getAllOffers(currenciesList);
            ArrayList<O2GAccountRow> accounts = account.getAccounts();
            ArrayList<O2GTradeRow> trades = order.getTrades();

            O2GAccountRow currentAccount = accounts.get(0);
            boolean isTrading = false;

            while (!isTrading) {
                offer.printer(offers);
                order.printOrders(trades, offers);

                userInstructions();
                String menu = in.nextLine().toLowerCase();

                /*
                If switch case is getting more complex on a later stage
                can be made another class Manager which will be responsible for various of operations
                 */

                switch (menu) {
                    case "open":
                        System.out.print("Choose dealing rates 'index' to create Market models.Order or press '0' to open all orders: ");
                        int inputOpenOrder = Integer.parseInt(in.nextLine());
                        checkIfInRange(inputOpenOrder, offers);

                        System.out.print("----- Press 'b' to BUY and 's' to SELL");
                        String userAction = in.nextLine().toLowerCase();
                        System.out.print("----- Input AMOUNT to " + (userAction.equals("b") ? "BUY" : "SELL") + " : ");

                        int amount = Integer.parseInt(in.nextLine());
                        checkIfAmountIsNegative(amount);

                        System.out.println("CREATING MARKET ORDER");
                        if (inputOpenOrder == 0) {
                            order.createMarderOrder(amount, currentAccount, offers,userAction);
                        } else {
                            int index = inputOpenOrder;
                            order.createSingleOrder(userAction, amount, currentAccount, offers.get(index - 1));
                        }
                        offers = offer.updateOffersAccordingly(currenciesList);
                        trades = order.getTrades();
                        break;

                    case "close":
                        System.out.print("Choose open position 'index' to close Market models.Order or press '0' to close all orders\n");
                        int inputCloseOrder = Integer.parseInt(in.nextLine());
                        checkIfAmountIsNegative(inputCloseOrder);
                        System.out.println("Status: models.Order Closing");
                        if (inputCloseOrder == 0) {
                            order.closeMultiOpenOrders(trades, currentAccount);
                        } else {
                            int index = inputCloseOrder;
                            order.closeSingleOrder(trades.get(index - 1), currentAccount);
                        }
                        offers = offer.updateOffersAccordingly(currenciesList);
                        trades = order.getTrades();
                        break;
                    case "refresh":
                        System.out.println("\nStatus: Refreshing rates and positions");
                        offers = offer.updateOffersAccordingly(currenciesList);
                        trades = order.getTrades();
                        break;

                    case "quit":
                        session.logout();
                        while (!statusListener.isDisconnected()) {
                            Thread.sleep(UserinfoConstants.WAIT_TIME);
                        }
                        closeSession(session, statusListener, responseListener);
                        System.out.println("Have a nice day");
                        isTrading = true;
                        break;

                    default:
                        System.out.println("Invalid Input");
                        break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkIfAmountIsNegative(int amount) throws Exception {
            if (amount < 0) {
                throw new Exception("Invalid rates");
            }
    }

    private static void checkIfInRange(int inputOpenOrder, ArrayList<O2GOfferRow> offers) throws Exception {
            if (inputOpenOrder < 0 || inputOpenOrder > offers.size()) {
                throw new Exception("Invalid rates");
            }
        }

    private static void newLine() {
        System.out.println("\n");
    }

    private static void wellcomingMessage() {
        System.out.println(" Welcome to FXCM App \nPlease enter your login details\n");
    }

    private static void userInstructions() {
        System.out.print("\nType 'open' to open order, 'close' to close order, 'refresh' to refresh rates and open positions, 'quit' to quit the application ");
    }

    private static void closeSession(O2GSession session, SessionStatusListener statusListener, ResponseListener responseListener) {
        session.unsubscribeSessionStatus(statusListener);
        session.unsubscribeResponse(responseListener);
        session.dispose();
    }

    private static void loginSession(O2GSession session) {
        session.login(UserinfoConstants.USERNAME, UserinfoConstants.PASSWORD, UserinfoConstants.SERVER, UserinfoConstants.TERMINAL);
    }

    private static void subsribeSession(O2GSession session, SessionStatusListener statusListener, ResponseListener responseListener) {
        session.subscribeSessionStatus(statusListener);
        session.subscribeResponse(responseListener);
    }
}

