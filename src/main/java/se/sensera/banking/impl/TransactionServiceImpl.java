package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

        if (amount < -100) {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_FUNDED);
        }

        if (account.getOwner().getId().equals(userId) || account.getUsers().anyMatch(user -> user.getId().equals(userId))) {
            return getTransaction(created, userId, accountId, amount);
        } else {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }
    }

    private Transaction getTransaction(String created, String userId, String accountId, double amount) {
        TransactionImpl transaction = new TransactionImpl(UUID.randomUUID().toString(), stringToDate(created),
                usersRepository.getEntityById(userId).get(),
                accountsRepository.getEntityById(accountId).get(),
                amount);

        return transactionsRepository.save(transaction);
    }

    @Override
    public double sum(String created, String userId, String accountId) throws UseException {
        Account account = accountsRepository.getEntityById(accountId)
                .orElseThrow(() -> new UseException(Activity.SUM_TRANSACTION, UseExceptionType.ACCOUNT_NOT_FOUND));

        if (isUserOrOwner(userId, account)) {
            Stream<Transaction> foundTransactions = transactionsRepository.all()
                    .filter(t -> t.getAccount().getId().equals(accountId)
                            && t.getCreated().before(stringToDate(created)));
            return foundTransactions.mapToDouble(Transaction::getAmount).sum();
        } else {
            throw new UseException(Activity.SUM_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }
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
        try {
            return formatter.parse(created);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Could not parse");
    }


    @Override
    public void addMonitor(Consumer<Transaction> monitor) {

    }
}
