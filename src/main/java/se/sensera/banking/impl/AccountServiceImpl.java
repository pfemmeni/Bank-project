package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountServiceImpl implements AccountService {
    private UsersRepository usersRepository;
    private AccountsRepository accountsRepository;

    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {
        if (usersRepository.getEntityById(userId)
                .isEmpty()) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND);
        }
        if (accountsRepository.all()
                .anyMatch(account -> account.getName().equals(accountName))) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
        }

        AccountImpl account = new AccountImpl(UUID.randomUUID().toString(),
                usersRepository.getEntityById(userId).get(),
                accountName,
                true,
                new ArrayList<>());
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = accountsRepository.getEntityById(accountId)
                .orElseThrow(() -> new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_FOUND));

        AtomicBoolean save = new AtomicBoolean(true);

        if (!account.getOwner().getId().equals(userId))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (!account.isActive())
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        changeAccountName(changeAccountConsumer, account, save);

        if (!save.get())
            return account;
        return accountsRepository.save(account);
    }

    private void changeAccountName(Consumer<ChangeAccount> changeAccountConsumer, Account account, AtomicBoolean save) {
        changeAccountConsumer.accept(new ChangeAccount() {
            @Override
            public void setName(String name) throws UseException {
                if (accountsRepository.all().anyMatch(account -> account.getName().equals(name))) {
                    save.set(false);
                    throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
                }
                if (account.getName().equals(name)) {
                    save.set(false);
                } else {
                    account.setName(name);
                }
            }
        });
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        Account account = accountsRepository.getEntityById(accountId)
                .orElseThrow(() -> new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND));
        User user = usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND));

        if (!account.getOwner().getId().equals(userId))
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (!account.isActive() || !user.isActive())
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        account.setActive(false);

        return accountsRepository.save(account);
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        return null;
    }
}
