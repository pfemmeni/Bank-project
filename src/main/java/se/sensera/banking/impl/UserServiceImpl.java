package se.sensera.banking.impl;

import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {
    private UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

        @Override
        public User createUser(String name, String personalIdentificationNumber) throws UseException {

            //      innan man skapar ny user kolla om PidN finns i alla sparade users

            if(usersRepository.all().anyMatch(user -> user.getPersonalIdentificationNumber().equals(personalIdentificationNumber))){
                throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE, personalIdentificationNumber);
            }



        User user =  new UserImpl(UUID.randomUUID().toString(), "Arne Gunnarsson", "20011010-1234",true);
        usersRepository.save(user);

        System.out.println(user);
        return user;
    }


    @Override
    public Optional<User> changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
            Optional<User> nameOfUser = getUser(userId);
            if(getUser(userId) == null)
                throw new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND,changeUser);
        usersRepository.save(name;
        return nameOfUser;
    }

    @Override
    public User inactivateUser(String userId) throws UseException {
        return null;
    }

    @Override
    public Optional<User> getUser(String userId) {
        return Optional.empty();
    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        return null;
    }
}