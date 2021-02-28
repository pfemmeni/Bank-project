package se.sensera.banking;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface UserService {

    User createUser(String name, String personalIdentificationNumber);

    User changeUser(String userId, Consumer<ChangeUser> changeUser);

    User inactivateUser(String userId);

    Optional<User> getUser(String userId);

    Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder);

    interface ChangeUser {
        void setName(String name);
        void setPersonalIdentificationNumber(String personalIdentificationNumber);
    }

    enum SortOrder {
        None,
        Name,
        PersonalId
    }
}
