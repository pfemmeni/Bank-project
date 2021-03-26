package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountServiceImpl implements AccountService {
    private final UsersRepository usersRepository;
    private final AccountsRepository accountsRepository;

    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {
        accountNameUnique(accountName, Activity.CREATE_ACCOUNT);
        AccountImpl account = new AccountImpl(UUID.randomUUID().toString(),
                getUserById(userId, Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND),
                accountName,
                true,
                new ArrayList<>());
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = getAccountById(accountId, Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_FOUND);
        AtomicBoolean save = new AtomicBoolean(true);

        isOwnerOfAccountAndAccountIsActive(userId, account);
        setAccountName(changeAccountConsumer, account, save);
        if (!save.get())
            return account;
        return accountsRepository.save(account);
    }

    private void isOwnerOfAccountAndAccountIsActive(String userId, Account account) throws UseException {
        if (!isOwnerOfAccount(userId, account))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (!account.isActive())
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
    }

    private void setAccountName(Consumer<ChangeAccount> changeAccountConsumer, Account account, AtomicBoolean save) {
        changeAccountConsumer.accept(new ChangeAccount() {
            @Override
            public void setName(String name) throws UseException {
                if (checkIfAccountNameEquals(name)) {
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
        Account account = getAccountById(accountId, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND);
        User user = getUserById(userIdToBeAssigned, Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND);

        if (isOwnerOfAccount(user.getId(), account)) {
            if (!account.isActive())
                throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE);
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER);
        }

        if (!isOwnerOfAccount(userId, account))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (account.getUsers().anyMatch(u -> u.getId().equals(user.getId())))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_ALREADY_ASSIGNED_TO_THIS_ACCOUNT);

        account.addUser(user);
        return accountsRepository.save(account);
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = getAccountById(accountId, Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND);
        User user = getUserById(userIdToBeAssigned, Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND);


        if (!isOwnerOfAccount(userId, account))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (account.getUsers().noneMatch(u -> u.getId().equals(user.getId())))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT);

        account.removeUser(user);
        return accountsRepository.save(account);
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        Account account = getAccountById(accountId, Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND);
        User user = getUserById(userId, Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND);


        if (!isOwnerOfAccount(userId, account))
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (!account.isActive() || !user.isActive())
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);

        account.setActive(false);
        return accountsRepository.save(account);
    }

    @Override
    public Stream<Account> findAccounts(String searchValue,
                                        String userId,
                                        Integer pageNumber,
                                        Integer pageSize,
                                        SortOrder sortOrder) throws UseException {
        Stream<Account> accounts;
        accounts = getAccountStream(searchValue, userId);

        switch (sortOrder) {
            case AccountName -> {
                return ListUtils.applyPage(accounts.sorted(Comparator.comparing(Account::getName)), pageNumber, pageSize);
            }
            case None -> {
                return ListUtils.applyPage(accounts, pageNumber, pageSize);
            }
            default -> throw new UseException(Activity.FIND_ACCOUNT, UseExceptionType.NOT_FOUND);
        }
    }

    private void accountNameUnique(String accountName, Activity activity) throws UseException {
        if (accountsRepository.all()
                .anyMatch(account -> account.getName().equals(accountName))) {
            throw new UseException(activity, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
        }
    }

    private boolean checkIfAccountNameEquals(String accountName) {
        return accountsRepository.all()
                .anyMatch(account -> account.getName().equals(accountName));
    }

    private User getUserById(String userId, Activity activity, UseExceptionType useExceptionType) throws UseException {
        return usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(activity, useExceptionType));
    }

    private Account getAccountById(String accountId, Activity activity, UseExceptionType useExceptionType) throws UseException {
        return accountsRepository.getEntityById(accountId)
                .orElseThrow(() -> new UseException(activity, useExceptionType));
    }

    private boolean isOwnerOfAccount(String userId, Account account) {
        return account.getOwner().getId().equals(userId);
    }

    private Stream<Account> getAccountStream(String searchValue, String userId) {
        Stream<Account> accounts;
        if (userId == null) {
            accounts = accountsRepository.all()
                    .filter(account -> account.getName().toLowerCase().contains(searchValue.toLowerCase()))
                    .collect(Collectors.toList())
                    .stream();
        } else {
            accounts = accountsRepository.all()
                    .filter(account -> account.getUsers().anyMatch(user -> user.getId().equals(userId))
                            || isOwnerOfAccount(userId, account))
                    .collect(Collectors.toList())
                    .stream();
        }
        return accounts;
    }

}
