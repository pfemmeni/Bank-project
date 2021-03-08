package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.UseException;

import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.function.Consumer;

public class TransactionServiceImpl implements TransactionService {
    private UsersRepository usersRepository;
    private AccountsRepository accountsRepository;
    private TransactionsRepository transactionsRepository;

    public TransactionServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository, TransactionsRepository transactionsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @Override
    public Transaction createTransaction(String created, String userId, String accountId, double amount) throws UseException {
       TransactionImpl transaction = new TransactionImpl(UUID.randomUUID().toString(), created,
               usersRepository.getEntityById(userId).get(),
               accountsRepository.getEntityById(accountId).get(),
               amount);
        return transactionsRepository.save(transaction);
    }

    @Override
    public double sum(String created, String userId, String accountId) throws UseException {
        return 0;
    }

    @Override
    public void addMonitor(Consumer<Transaction> monitor) {

    }
}
