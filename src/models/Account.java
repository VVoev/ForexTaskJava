package models;

import com.fxcore2.*;

import java.util.ArrayList;

public class Account {
    private O2GSession session;
    private final String errorMessage = "No Account for this user";

    public Account(O2GSession session) {
        this.session = session;
    }

    public ArrayList<O2GAccountRow> getAccounts(){

        ArrayList<O2GAccountRow> accounts = new ArrayList<>();

        O2GLoginRules loginRules = session.getLoginRules();
        O2GResponse response = loginRules.getTableRefreshResponse(O2GTableType.ACCOUNTS);
        O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
        O2GAccountsTableResponseReader accountsResponseReader = readerFactory.createAccountsTableReader(response);

        addResponse(accounts,accountsResponseReader);
        checkIfAccountsAreEmpty(accounts);

        return accounts;
    }

    private void checkIfAccountsAreEmpty(ArrayList<O2GAccountRow> accounts) {
        if(accounts.size() == 0){
            try {
                throw new Exception(errorMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addResponse(ArrayList<O2GAccountRow> accounts, O2GAccountsTableResponseReader accountsResponseReader) {
        for (int i = 0; i < accountsResponseReader.size(); i++) {
            accounts.add(accountsResponseReader.getRow(i));
        }
    }
}
