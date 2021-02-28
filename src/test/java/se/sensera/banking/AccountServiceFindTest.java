package se.sensera.banking;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountServiceFindTest {

    static User LISA;
    static User ARNE;
    static User GULLBRITT;
    static User BEDA;
    static User SVEN;

    AccountService accountService;

    UsersRepository usersRepository;
    AccountsRepository accountsRepository;

    static Account lisaDefaultAccount;
    static Account lisaExtrAccount;
    static Account arneDefaultAccount;
    static Account gullbrittDefaultAccount;
    static Account bedaDefaultAccount;
    static Account svenDefaultAccount;
    static Account svenExtraAccount;

    @BeforeEach
    void setUp() {
        //TODO must be included in create of AccountService
        usersRepository = mock(UsersRepository.class);
        accountsRepository = mock(AccountsRepository.class);

        accountService = null; //TODO create Your implementing class here

        LISA = createUser("Lisa", "9384538975", true);
        ARNE = createUser("Arne", "9384538976", true);
        GULLBRITT = createUser("Gullbritt", "9384538977", true);
        BEDA = createUser("Beda", "9384538978", true);
        SVEN = createUser("Sven", "9384538979", true);

        lisaDefaultAccount = createAccount(LISA, "default a", true);
        lisaExtrAccount = createAccount(LISA, "extra a", true);
        arneDefaultAccount = createAccount(ARNE, "default b", true);
        gullbrittDefaultAccount = createAccount(GULLBRITT, "default c", true);
        bedaDefaultAccount = createAccount(BEDA, "default d", true);
        svenDefaultAccount = createAccount(SVEN, "default e", true);
        svenExtraAccount = createAccount(SVEN, "extra b", true, LISA, ARNE);

        when(accountsRepository.all()).thenReturn(Stream.of(
                lisaDefaultAccount,
                lisaExtrAccount,
                arneDefaultAccount,
                gullbrittDefaultAccount,
                bedaDefaultAccount,
                svenDefaultAccount,
                svenExtraAccount
        ));
    }

    @ParameterizedTest
    @MethodSource("provideSearchTestData")
    void find_accounts_success(String searchValue, String userId, Integer pageNumber, Integer pageSize, AccountService.SortOrder sortOrder, Matcher<Iterable<Account>> matcher) {
        // When
        Stream<Account> searchResult = accountService.findAccounts(searchValue, userId, pageNumber, pageSize, sortOrder);

        // Then
        assertThat(searchResult.collect(Collectors.toList()), matcher);

    }

    private static Stream<Arguments> provideSearchTestData() {
        return Stream.of(
                // All accounts unsorted
                Arguments.of("", null, null, null, AccountService.SortOrder.None, containsInAnyOrder(
                        lisaDefaultAccount,
                        lisaExtrAccount,
                        arneDefaultAccount,
                        gullbrittDefaultAccount,
                        bedaDefaultAccount,
                        svenDefaultAccount,
                        svenExtraAccount
                )),
                // All accounts with default in the name unsorted
                Arguments.of("default", null, null, null, AccountService.SortOrder.None, containsInAnyOrder(
                        lisaDefaultAccount,
                        lisaExtrAccount,
                        arneDefaultAccount,
                        gullbrittDefaultAccount,
                        bedaDefaultAccount,
                        svenDefaultAccount,
                        svenExtraAccount
                )),
                // All accounts sorted by name
                Arguments.of("", null, null, null, AccountService.SortOrder.AccountName, contains(
                        lisaDefaultAccount,
                        arneDefaultAccount,
                        gullbrittDefaultAccount,
                        bedaDefaultAccount,
                        svenDefaultAccount,
                        lisaExtrAccount,
                        svenExtraAccount
                )),
                // All accounts of user (and associated accounts) unsorted
                Arguments.of("", LISA, null, null, AccountService.SortOrder.None, containsInAnyOrder(
                        lisaDefaultAccount,
                        lisaExtrAccount,
                        svenExtraAccount
                )),
                // All accounts from pageNumber 0 unsorted
                Arguments.of("", null, 0, null, AccountService.SortOrder.None, containsInAnyOrder(
                        lisaDefaultAccount,
                        lisaExtrAccount,
                        arneDefaultAccount,
                        gullbrittDefaultAccount,
                        bedaDefaultAccount,
                        svenDefaultAccount,
                        svenExtraAccount
                )),
                // All accounts from pageNumber 0 & pageSize 10 unsorted
                Arguments.of("", null, 0, 10, AccountService.SortOrder.None, containsInAnyOrder(
                        lisaDefaultAccount,
                        lisaExtrAccount,
                        arneDefaultAccount,
                        gullbrittDefaultAccount,
                        bedaDefaultAccount,
                        svenDefaultAccount,
                        svenExtraAccount
                )),
                // All accounts from pageNumber 0 & pageSize 3 sorted by name
                Arguments.of("", null, 0, 3, AccountService.SortOrder.AccountName, contains(
                        lisaDefaultAccount,
                        lisaExtrAccount
                )),
                // All accounts from pageNumber 1 & pageSize 10 unsorted
                Arguments.of("", null, 0, 10, AccountService.SortOrder.None, empty())
        );
    }

    private static Account createAccount(User owner, String name, boolean active, User... users) {
        Account account = mock(Account.class);
        when(account.getName()).thenReturn(name);
        when(account.getOwner()).thenReturn(owner);
        when(account.isActive()).thenReturn(active);
        when(account.getUsers()).thenReturn(Stream.of(users));
        return account;
    }

    private User createUser(String name, String pid, boolean active) {
        User user = mock(User.class);
        String userId = UUID.randomUUID().toString();
        when(user.getId()).thenReturn(userId);
        when(user.getPersonalIdentificationNumber()).thenReturn(pid);
        when(user.getName()).thenReturn(name);
        when(user.isActive()).thenReturn(active);
        when(usersRepository.getEntityById(eq(userId))).thenReturn(Optional.of(user));
        return user;
    }

}
