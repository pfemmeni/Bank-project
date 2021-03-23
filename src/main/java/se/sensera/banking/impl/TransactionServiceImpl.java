package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TransactionServiceImpl implements TransactionService {
    private UsersRepository usersRepository;
    private AccountsRepository accountsRepository;
    private TransactionsRepository transactionsRepository;
    List<Consumer<Transaction>> transactionListeners = new LinkedList<>();

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

        if (!isUserOrOwner(userId, account)) {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }
        Date createdDate = stringToDate(created);
        synchronized (this) {
            if (sum(createdDate, userId, accountId) + amount < 0) {
                throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_FUNDED);
            }
        }
        return createNewTransaction(created, userId, accountId, amount);


    }

    private Transaction createNewTransaction(String created, String userId, String accountId, double amount) {
        TransactionImpl transaction = new TransactionImpl(
                UUID.randomUUID().toString(),
                stringToDate(created),
                usersRepository.getEntityById(userId).get(),
                accountsRepository.getEntityById(accountId).get(),
                amount);
        Thread thread = new Thread(() -> addToTransactionListeners(transaction));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return transactionsRepository.save(transaction);

    }

    private void addToTransactionListeners(TransactionImpl transaction) {
        transactionListeners.forEach(transactionConsumer -> transactionConsumer.accept(transaction));
    }

    @Override
    public double sum(String created, String userId, String accountId) throws UseException {
        return sum(stringToDate(created), userId, accountId);
    }

    private double sum(Date created, String userId, String accountId) throws UseException {
        Account account = accountsRepository.getEntityById(accountId)
                .orElseThrow(() -> new UseException(Activity.SUM_TRANSACTION, UseExceptionType.ACCOUNT_NOT_FOUND));
        if (isUserOrOwner(userId, account)) {
            return sumOfFoundTransactions(created, accountId);
        } else {
            throw new UseException(Activity.SUM_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }
    }

    private double sumOfFoundTransactions(Date created, String accountId) {
        Stream<Transaction> foundTransactions = getTransactions(created, accountId);

        return foundTransactions.mapToDouble(Transaction::getAmount).sum();
    }


    private Stream<Transaction> getTransactions(Date created, String accountId) {
        return transactionsRepository.all()
                .filter(t -> t.getAccount().getId().equals(accountId)
                        && t.getCreated().before(created));
    }

    private Boolean isUserOrOwner(String userId, Account account) {
        if (account.getOwner().getId().equals(userId)) {
            return true;
        }
        if (account.getUsers().anyMatch(u -> u.getId().equals(userId))) {
            return true;
        }
        return false;
    }

    private Date stringToDate(String created) {
        synchronized (this) {
            try {
                return formatter.parse(created);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("Could not parse");
        }
    }


    @Override
    public void addMonitor(Consumer<Transaction> monitor) {
        transactionListeners.add(monitor);
    }
}
