package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TransactionServiceImpl implements TransactionService {
    private final UsersRepository usersRepository;
    private final AccountsRepository accountsRepository;
    private final TransactionsRepository transactionsRepository;
    List<Consumer<Transaction>> transactionListeners = new LinkedList<>();

    public TransactionServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository, TransactionsRepository transactionsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
    }

    static DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Transaction createTransaction(String created, String userId, String accountId, double amount) throws UseException {
        Date date = getDate(created);
        Account account = getAccountById(accountId, Activity.CREATE_TRANSACTION);
        isAllowedToCreateTransaction(userId, account);
        checkIfEnoughFoundsOnAccount(userId, amount, date, account);
        return createNewTransaction(date, userId, account, amount);
    }


    private Transaction createNewTransaction(Date created, String userId, Account account, double amount) {
        TransactionImpl transaction = new TransactionImpl(UUID.randomUUID().toString(),
                created,
                usersRepository.getEntityById(userId).get(),
                account,
                amount);
        Thread thread = new Thread(() -> addToTransactionListeners(transaction));
        thread.setDaemon(true);
        thread.start();
        return transactionsRepository.save(transaction);
    }

    private void isAllowedToCreateTransaction(String userId, Account account) throws UseException {
        if (!isUserOrOwner(userId, account))
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_ALLOWED);
    }

    private void checkIfEnoughFoundsOnAccount(String userId, double amount, Date date, Account account) throws UseException {
        double sum = sum(date, userId, account);
        synchronized (this) {
            if ((sum + amount) < 0)
                throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_FUNDED);
        }
    }

    private void addToTransactionListeners(TransactionImpl transaction) {
        transactionListeners.forEach(transactionConsumer -> transactionConsumer.accept(transaction));
    }

    @Override
    public double sum(String created, String userId, String accountId) throws UseException {
        Date createdDate = getDate(created);
        Account account = getAccountById(accountId, Activity.SUM_TRANSACTION);
        return sum(createdDate, userId, account);
    }

    private double sum(Date created, String userId, Account account) throws UseException {
        if (isUserOrOwner(userId, account))
            return sumOfFoundTransactions(created, account);
        throw new UseException(Activity.SUM_TRANSACTION, UseExceptionType.NOT_ALLOWED);
    }

    private Stream<Transaction> getTransactions(Date created, Account account) {
        return transactionsRepository.all()
                .filter(t -> t.getAccount().getId().equals(account.getId())
                        && t.getCreated().before(created));
    }

    private double sumOfFoundTransactions(Date created, Account account) {
        return getTransactions(created, account).mapToDouble(Transaction::getAmount).sum();
    }

    private Account getAccountById(String accountId, Activity activity) throws UseException {
        return accountsRepository.getEntityById(accountId)
                .orElseThrow(() -> new UseException(activity, UseExceptionType.ACCOUNT_NOT_FOUND));
    }


    private Boolean isUserOrOwner(String userId, Account account) {
        return account.getOwner().getId().equals(userId)
                || (account.getUsers().anyMatch(u -> u.getId().equals(userId)));
    }

    private Date getDate(String created) {
        return stringToDate(created);
    }

    private Date stringToDate(String created) {
        LocalDateTime localDateTime = LocalDateTime.parse(created, formatter2);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public void addMonitor(Consumer<Transaction> monitor) {
        transactionListeners.add(monitor);
    }
}

