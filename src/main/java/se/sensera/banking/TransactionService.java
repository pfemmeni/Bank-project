package se.sensera.banking;

import se.sensera.banking.exceptions.UserException;

import java.util.function.Consumer;

public interface TransactionService {
    Transaction createTransaction(String created, String userId, String accountId, double amount) throws UserException;

    double sum(String created, String userId, String accountId);

    void addMonitor(Consumer<Transaction> transactionConsumer);
}
