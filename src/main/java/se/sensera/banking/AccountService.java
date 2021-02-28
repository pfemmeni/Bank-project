package se.sensera.banking;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface AccountService {

    Account createAccount(String userId, String accountName);
    Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer);

    Account addUserToAccount(String userUserId, String accountId, String userIdToBeAssigned);
    Account removeUserFromAccount(String userUserId, String accountId, String userIdToBeAssigned);

    Account inactivateAccount(String userId, String accountId);

    Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder);

    interface ChangeAccount {
        void setName(String name);
    }

    enum SortOrder {
        None,
        AccountName,
    }

}
