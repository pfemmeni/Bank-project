package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public Transaction createTransaction(String created, String userId, String accountId, double amount) throws UseException {

        Account account = getAccountById(accountId)
                .orElseThrow(() -> new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.ACCOUNT_NOT_FOUND));

        if (!isUserOrOwner(userId, account)) {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }
        Date date = getDate(created);

        synchronized (this) {
            if (sum(date, userId, accountId) + amount < 0) {
                throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_FUNDED);
            }
        }

        return createNewTransaction(date, userId, accountId, amount);

    }

    private Optional<Account> getAccountById(String accountId) {
        return accountsRepository.all().filter(account -> accountId.equals(accountId));
    }

    private Date getDate(String created) {
        return stringToDate(created);
    }

    private Transaction createNewTransaction(Date created, String userId, String accountId, double amount) {
        TransactionImpl transaction = new TransactionImpl(
                UUID.randomUUID().toString(),
                created,
                usersRepository.getEntityById(userId).get(),
                getAccountById(accountId).get(),
                amount);
        Thread thread = new Thread(() -> addToTransactionListeners(transaction));
        thread.setDaemon(true);
        thread.start();

        return transactionsRepository.save(transaction);

    }

    private void addToTransactionListeners(TransactionImpl transaction) {
        transactionListeners.forEach(transactionConsumer -> transactionConsumer.accept(transaction));
    }

    @Override
    public double sum(String created, String userId, String accountId) throws UseException {
        Date createdDate = getDate(created);

        return sum(createdDate, userId, accountId);
    }

    private double sum(Date created, String userId, String accountId) throws UseException {
        Account account = getAccountById(accountId)
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

    static DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Date stringToDate(String created) {
        LocalDateTime localDateTime = LocalDateTime.parse(created, formatter2);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

    }


    @Override
    public void addMonitor(Consumer<Transaction> monitor) {
        transactionListeners.add(monitor);

    }
}

