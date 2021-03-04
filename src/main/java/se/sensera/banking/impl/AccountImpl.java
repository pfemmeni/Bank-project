package se.sensera.banking.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.sensera.banking.Account;
import se.sensera.banking.User;

import java.util.List;
import java.util.stream.Stream;
@Data
@AllArgsConstructor
public class AccountImpl implements Account {
    String id;
    User owner;
    String name;
    boolean isActive;
    List<User> users;

    @Override
    public Stream<User> getUsers() {

        return users.stream();
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public void removeUser(User user) {

    }


}
