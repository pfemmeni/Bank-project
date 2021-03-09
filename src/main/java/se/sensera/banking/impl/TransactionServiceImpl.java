package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.text.ParseException;
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

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public Transaction createTransaction(String created, String userId, String accountId, double amount) throws UseException {
        Account account = accountsRepository.getEntityById(accountId)
                .orElseThrow(() -> new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.ACCOUNT_NOT_FOUND));

//        Transaction oldTransaction = transactionsRepository.all().(transaction -> transaction.getUser().equals(userId));
//        if ((oldTransaction.getAmount() + amount) < 0) {
//            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_FUNDED);
//        }

        if (account.getOwner().getId().equals(userId) || account.getUsers().anyMatch(user -> user.getId().equals(userId))) {
            return getTransaction(created, userId, accountId, amount);
        } else {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }
    }

    private Transaction getTransaction(String created, String userId, String accountId, double amount) {
        try {
            TransactionImpl transaction = new TransactionImpl(UUID.randomUUID().toString(), formatter.parse(created),
                    usersRepository.getEntityById(userId).get(),
                    accountsRepository.getEntityById(accountId).get(),
                    amount);
            return transactionsRepository.save(transaction);
        } catch (ParseException e) {
            throw new RuntimeException("Fail in createTransaction because parse failed " + created);
        }
    }

    @Override
    public double sum(String created, String userId, String accountId) throws UseException {
        return 0;
    }


    @Override
    public void addMonitor(Consumer<Transaction> monitor) {

    }
}
