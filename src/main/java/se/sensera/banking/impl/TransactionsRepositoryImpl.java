package se.sensera.banking.impl;

import se.sensera.banking.Transaction;
import se.sensera.banking.TransactionsRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionsRepositoryImpl implements TransactionsRepository {
    List<Transaction> transactions = new LinkedList<>();

    @Override
    public Optional<Transaction> getEntityById(String id) {
        return transactions.stream().filter(transaction -> transaction.getId().equals(id)).findAny();
    }

    @Override
    public Stream<Transaction> all() {
        return transactions.stream();
    }

    @Override
    public Transaction save(Transaction entity) {
        transactions.add(entity);
        return entity;
    }

    @Override
    public Transaction delete(Transaction entity) {
        transactions.remove(entity);
        return entity;
    }
}
