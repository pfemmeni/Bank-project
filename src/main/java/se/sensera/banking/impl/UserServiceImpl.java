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
    private UsersRepository usersRepository;

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
        User user = usersRepository.getEntityById(userId)
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
        User user = usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));
        user.setActive(false);

        return usersRepository.save(user);
    }

    @Override
    public Optional<User> getUser(String userId) {
        Optional<User> user = usersRepository.getEntityById(userId);
        return user;
    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        SortBySortOrder sortBySortOrder = new SortBySortOrder();
        Stream<User> unorderedUsers = sortBySortOrder.unorderedUsers(searchString);

        Stream<User> userStream = sortBySortOrder.usersByOrder(sortOrder, sortBySortOrder, unorderedUsers);

        return ListUtils.applyPage(userStream, pageNumber, pageSize);
    }

    class SortBySortOrder {
        Comparator<User> SORT_BY_NAME = Comparator.comparing(User::getName);
        Comparator<User> SORT_BY_ID = Comparator.comparing(User::getPersonalIdentificationNumber);

        public Stream<User> unorderedUsers(String searchString) {
            return usersRepository.all()
                    .filter(user -> checkIfContains(searchString, user))
                    .collect(Collectors.toList())
                    .stream();
        }

        private boolean checkIfContains(String searchString, User user) {
            if (user.getName().toLowerCase().contains(searchString.toLowerCase()))
                return true;
            if (user.getPersonalIdentificationNumber().contains(searchString))
                return true;
            return false;
        }

        private Stream<User> usersByOrder(SortOrder sortOrder, SortBySortOrder sortBySortOrder, Stream<User> unorderedUsers) {
            if (sortOrder.equals(SortOrder.Name)) {
                return unorderedUsers.sorted(sortBySortOrder.SORT_BY_NAME);
            } else if (sortOrder.equals(SortOrder.PersonalId)) {
                return unorderedUsers.sorted(sortBySortOrder.SORT_BY_ID);
            } else {
                return unorderedUsers.filter(User::isActive);
            }
        }

    }
}
