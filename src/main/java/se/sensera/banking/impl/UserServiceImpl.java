package se.sensera.banking.impl;

import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {
    private final UsersRepository usersRepository;


    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public User createUser(String name, String personalIdentificationNumber) throws UseException {
        if (usersRepository.all()
                .anyMatch(user -> user.getPersonalIdentificationNumber()
                        .equals(personalIdentificationNumber))) {
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        }
        UserImpl user = new UserImpl(UUID.randomUUID().toString(), name, personalIdentificationNumber, true);
        return usersRepository.save(user);
    }

    @Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
        User user = getUser(userId)
                .orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));

        AtomicBoolean save = new AtomicBoolean(true);

        changeUser.accept(new ChangeUser() {
            @Override
            public void setName(String name) {
                user.setName(name);
            }

            @Override
            public void setPersonalIdentificationNumber(String personalIdentificationNumber) throws UseException {
                if (usersRepository.all()
                        .anyMatch(user -> user.getPersonalIdentificationNumber()
                                .equals(personalIdentificationNumber))) {
                    save.set(false);
                    throw new UseException(Activity.UPDATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
                }
                user.setPersonalIdentificationNumber(personalIdentificationNumber);
            }
        });

        if (!save.get()) {
            return user;
        }
        return usersRepository.save(user);
    }

    @Override
    public User inactivateUser(String userId) throws UseException {
        User user = getUser(userId)
                .orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));
        user.setActive(false);
        return usersRepository.save(user);
    }

    @Override
    public Optional<User> getUser(String userId) {
        return usersRepository.getEntityById(userId);
    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        Stream<User> allFoundUsers = usersRepository.all()
                .filter(user -> user.getName().toLowerCase().contains(searchString.toLowerCase())
                        || user.getPersonalIdentificationNumber().contains(searchString))
                .filter(User::isActive)
                .collect(Collectors.toList())
                .stream();

        switch (sortOrder) {
            case Name -> allFoundUsers = allFoundUsers.sorted(Comparator.comparing(User::getName));
            case PersonalId -> allFoundUsers = allFoundUsers.sorted(Comparator.comparing(User::getPersonalIdentificationNumber));
        }
        return ListUtils.applyPage(allFoundUsers, pageNumber, pageSize);
    }


}
