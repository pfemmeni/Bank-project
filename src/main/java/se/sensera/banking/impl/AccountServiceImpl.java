package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.AccountService;
import se.sensera.banking.AccountsRepository;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.UseException;

import java.util.ArrayList;
import java.util.UUID;
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
        AccountImpl account = new AccountImpl(UUID.randomUUID().toString(),
                                                usersRepository.getEntityById(userId).get(),
                                                accountName,
                                                true,
                                                new ArrayList<>());
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        return null;
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
        return null;
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        return null;
    }
}
