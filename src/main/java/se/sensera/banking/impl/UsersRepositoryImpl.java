package se.sensera.banking.impl;

import se.sensera.banking.User;
import se.sensera.banking.UsersRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class UsersRepositoryImpl implements se.sensera.banking.UsersRepository {
    List<User> users = new LinkedList<>();

    @Override
    public Optional<User> getEntityById(String id) {
        return users.stream().filter(user -> user.getId().equals(id)).findAny();
    }

    @Override
    public Stream<User> all() {
        return users.stream();
    }

    @Override
    public User save(User entity) {
        users.add(entity);
        return entity;
    }

    @Override
    public User delete(User entity) {
        users.remove(entity);
        return entity;
    }

    @Override
    public String toString() {
        return "UsersRepositoryImpl{" +
                "users=" + users +
                '}';
    }
}
