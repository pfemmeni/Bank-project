package se.sensera.banking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UserException;
import se.sensera.banking.exceptions.UserExceptionType;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    AccountService accountService;

    UsersRepository usersRepository;
    AccountsRepository accountsRepository;

    Account account;
    String accountId;
    String accountName;

    String userId;
    String otherUserId;
    User user;
    User otherUser;

    @BeforeEach
    void setUp() {
        //TODO must be included in create of AccountService
        usersRepository = mock(UsersRepository.class);
        accountsRepository = mock(AccountsRepository.class);

        accountService = null; //TODO create Your implementing class here

        user = mock(User.class);
        userId = UUID.randomUUID().toString();

        otherUser = mock(User.class);
        otherUserId = UUID.randomUUID().toString();

        when(user.getId()).thenReturn(userId);
        when(otherUser.getId()).thenReturn(otherUserId);
        when(usersRepository.getEntityById(anyString())).thenReturn(Optional.empty());
        when(usersRepository.getEntityById(eq(userId))).thenReturn(Optional.of(user));
        when(usersRepository.getEntityById(eq(otherUserId))).thenReturn(Optional.of(otherUser));

        account = mock(Account.class);
        accountId = UUID.randomUUID().toString();
        accountName = "default";
        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn(accountName);
        when(account.getOwner()).thenReturn(user);
        when(accountsRepository.getEntityById(anyString())).thenReturn(Optional.empty());
        when(accountsRepository.getEntityById(eq(accountId))).thenReturn(Optional.of(account));
    }

    @Test
    void create_account_success() {
        // When
        Account account = accountService.createAccount(userId, accountName);

        // Then
        verify(accountsRepository).save(this.account);
        assertThat(account.getOwner().getId(), is(userId));
        assertThat(account.getName(), is(accountName));
        assertThat(account.getBalance(), is(0D));
        assertThat(account.isActive(), is(true));
        assertThat(account.getUsers().collect(Collectors.toList()), is(empty()));
    }

    @Test
    void create_account_failed_because_duplicate_name_for_owner_user() {
        // Given
        Account otherAccount = mock(Account.class);
        when(otherAccount.getOwner()).thenReturn(user);
        when(otherAccount.getName()).thenReturn(accountName);
        when(accountsRepository.all()).thenReturn(Stream.of(otherAccount));

        // when
        UserException userException = assertThrows(UserException.class, () -> {
            accountService.createAccount(userId, accountName);
        });

        // Then
        verify(accountsRepository,never()).save(anyObject());
        assertThat(userException.getUserExceptionType(), is(UserExceptionType.ACCOUNT_NAME_NOT_UNIQUE));
        assertThat(userException.getActivity(), is(Activity.CREATE_ACCOUNT));
    }

    @Test
    void change_account_name_success() {
        // Given
        String otherAccountName = "other";

        // When
        Account account = accountService.changeAccount(userId, accountId, changeAccount -> changeAccount.setName(otherAccountName));

        // Then
        verify(accountsRepository).save(this.account);
        verify(this.account).setName(otherAccountName);
        assertThat(account.getOwner().getId(), is(userId));
        assertThat(account.getName(), is(accountName));
        assertThat(account.getBalance(), is(0D));
        assertThat(account.isActive(), is(true));
        assertThat(account.getUsers().collect(Collectors.toList()), is(empty()));
    }

    @Test
    void change_account_name_to_same_name_success() {
        // When
        Account account = accountService.changeAccount(userId, accountId, changeAccount -> changeAccount.setName(accountName));

        // Then
        verify(accountsRepository, never()).save(anyObject());
        verify(this.account, never()).setName(anyString());
    }

    @Test
    void change_account_name_failed_because_duplicate_name() {
        // Given
        String otherAccountName = "other";
        Account otherAccount = mock(Account.class);
        when(otherAccount.getOwner()).thenReturn(user);
        when(otherAccount.getName()).thenReturn(otherAccountName);
        when(accountsRepository.all()).thenReturn(Stream.of(this.account, otherAccount));

        // When
        UserException userException = assertThrows(UserException.class, () -> {
            accountService.changeAccount(userId, accountId, changeAccount -> changeAccount.setName(otherAccountName));
        });

        // Then
        verify(accountsRepository, never()).save(anyObject());
        assertThat(userException.getUserExceptionType(), is(UserExceptionType.ACCOUNT_NAME_NOT_UNIQUE));
        assertThat(userException.getActivity(), is(Activity.UPDATE_ACCOUNT));
    }

    @Test
    void change_account_name_failed_because_not_owner() {
        // Given
        String otherAccountName = "other";
        Account otherAccount = mock(Account.class);
        when(otherAccount.getOwner()).thenReturn(user);
        when(accountsRepository.all()).thenReturn(Stream.of(this.account, otherAccount));

        // When
        UserException userException = assertThrows(UserException.class, () -> {
            accountService.changeAccount(otherUserId, accountId, changeAccount -> changeAccount.setName(otherAccountName));
        });

        // Then
        verify(accountsRepository, never()).save(anyObject());
        assertThat(userException.getUserExceptionType(), is(UserExceptionType.NOT_OWNER));
        assertThat(userException.getActivity(), is(Activity.UPDATE_ACCOUNT));
    }

    @Test
    void change_account_name_failed_because_account_inactive() {
        // Given
        String otherAccountName = "other";
        when(accountsRepository.all()).thenReturn(Stream.of(account));
        when(account.isActive()).thenReturn(false);

        // When
        UserException userException = assertThrows(UserException.class, () -> {
            accountService.changeAccount(userId, accountId, changeAccount -> changeAccount.setName(otherAccountName));
        });

        // Then
        verify(accountsRepository, never()).save(anyObject());
        assertThat(userException.getUserExceptionType(), is(UserExceptionType.NOT_ACTIVE));
        assertThat(userException.getActivity(), is(Activity.UPDATE_ACCOUNT));
    }

    @Test
    void inactivate_account_success() {
        // Given
        when(accountsRepository.all()).thenReturn(Stream.of(account));

        // When
        Account account = accountService.inactivateAccount(userId, accountId);

        // Then
        verify(accountsRepository).save(this.account);
        verify(account).setActive(false);
    }

    @Test
    void inactivate_account_failed_because_not_owner() {
        // Given
        when(accountsRepository.all()).thenReturn(Stream.of(account));

        // When
        UserException userException = assertThrows(UserException.class, () -> {
            accountService.inactivateAccount(otherUserId, accountId);
        });

        // Then
        assertThat(userException.getUserExceptionType(), is(UserExceptionType.NOT_OWNER));
        assertThat(userException.getActivity(), is(Activity.INACTIVATE_ACCOUNT));
    }

    @Test
    void inactivate_account_failed_because_not_active() {
        // Given
        when(accountsRepository.all()).thenReturn(Stream.of(account));
        when(account.isActive()).thenReturn(false);

        // When
        UserException userException = assertThrows(UserException.class, () -> {
            accountService.inactivateAccount(userId, accountId);
        });

        // Then
        assertThat(userException.getUserExceptionType(), is(UserExceptionType.NOT_ACTIVE));
        assertThat(userException.getActivity(), is(Activity.INACTIVATE_ACCOUNT));
    }
}
