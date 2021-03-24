package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.AccountsRepository;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class AccountsRepositoryImpl implements AccountsRepository {
    List<Account> accounts = new LinkedList<>();

    @Override
    public Optional<Account> getEntityById(String id) {
        return accounts.stream().filter(account -> account.getId().equals(id)).findAny();
    }

    @Override
    public Stream<Account> all() {
        return accounts.stream();
    }

    @Override
    public Account save(Account entity) {
        accounts.add(entity);
        return entity;
    }

    @Override
    public Account delete(Account entity) {
        accounts.remove(entity);
        return entity;
    }
}
