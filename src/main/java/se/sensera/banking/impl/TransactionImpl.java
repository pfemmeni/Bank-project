package se.sensera.banking.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.sensera.banking.Account;
import se.sensera.banking.Transaction;
import se.sensera.banking.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
public class TransactionImpl implements Transaction {
    String id;
    Date created;
    User user;
    Account account;
    double amount;

    @Override
    public Date getCreated() {
        return created;
    }


}
