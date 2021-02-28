package se.sensera.banking;

import java.util.Date;

public interface Transaction {

    String getId();
    Date getCreated();
    User getUser();
    Account getAccount();
    double getAmount();
}
