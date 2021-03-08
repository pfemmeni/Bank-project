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
public class TransactionImpl implements Transaction {
    String id;
    Date created;
    User user;
    Account account;
    double amount;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public TransactionImpl(String id, Date created, User user, Account account, double amount) {
        this.id = id;
        this.created = created; //formatter.format(created);
        this.user = user;
        this.account = account;
        this.amount = amount;
    }

    @Override
    public Date getCreated() {
        return created;
    }

//
//    @Override
//    public String getId() {
//        return this.id;
//    }
//
//    @Override
//    public Date getCreated() {
//        return this.date;
//    }
//
//    @Override
//    public User getUser() {
//        return this.user;
//    }
//
//    @Override
//    public Account getAccount() {
//        return this.account;
//    }
//
//    @Override
//    public double getAmount() {
//        return this.amount;
//    }
}
