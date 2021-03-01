package se.sensera.banking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sensera.banking.exceptions.UseException;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class TransactionServiceParallelTest {

    TransactionService transactionService;

    UsersRepository usersRepository;
    AccountsRepository accountsRepository;
    TransactionsRepository transactionsRepository;
    User user;
    User otherUser;
    Account account;
    Account otherAccount;

    @BeforeEach
    void setUp() {
        //TODO must be included in create of AccountService
        usersRepository = mock(UsersRepository.class);
        accountsRepository = mock(AccountsRepository.class);
        transactionsRepository = new TestTransactionsRepository();

        transactionService = null; //TODO create Your implementing class here

        user = createUser("Arne Arnesson", "9283749238472", true);
        otherUser = createUser("Arne Arnesson", "9283749238472", true);
        account = createAccount(user, "default", true);
        otherAccount = createAccount(user, "other", true, otherUser);
    }

    @Test
    void create_parallel_transaction_success() {
        // Given
        String created = "2020-01-01 10:34";
        int count = 1000;
        Object monitorSync = new Object();
        transactionService.addMonitor(waitSync1msec(monitorSync));

        long start = System.currentTimeMillis();
        List<Transaction> transactions = IntStream.range(0, count)
                .boxed()
                .parallel()
                .map(n -> {
                    try {
                        return transactionService.createTransaction(created, user.getId(), account.getId(), (double) n);
                    } catch (UseException e) {
                        throw new RuntimeException("Internal error",e);
                    }
                })
                .collect(Collectors.toList());
        int duration = (int) (System.currentTimeMillis() - start);

        assertThat(transactions.size(), is(count));
        assertThat(transactions, containsInAnyOrder(transactionsRepository.all().toArray(Transaction[]::new)));
        assertThat(duration, is(lessThanOrEqualTo(5000)));
    }

    @Test
    void create_parallel_sum_transaction_success() {
        // Given
        String created = "2020-01-01 10:34";
        int count = 1000;
        Object monitorSync = new Object();
        transactionService.addMonitor(waitSync1msec(monitorSync));

        long start = System.currentTimeMillis();
        long countErrors = IntStream.range(0, count).boxed()
                .map(n -> createAccount(user, UUID.randomUUID().toString(), true))
                //.parallel()
                .map(account -> {
                    try {
                        transactionService.createTransaction(created, user.getId(), account.getId(), 100D);
                    } catch (UseException e) {
                        e.printStackTrace();
                    }
                    return Stream.of(100D,-150D)
                            //.parallel()
                            .anyMatch(amount -> {
                                try {
                                    transactionService.createTransaction(created, user.getId(), account.getId(), amount);
                                    return false;
                                } catch (UseException e) {
                                    return true;
                                }
                            });
                })
                .collect(Collectors.toList())
                .size();
        int duration = (int) (System.currentTimeMillis() - start);

        assertThat(countErrors, is(0));
        assertThat(duration, is(lessThanOrEqualTo(1)));
    }


    @Test
    void monitor_created_transaction_success() throws InterruptedException {
        // Given
        String created = "2020-01-01 10:34";
        final int count = 1000;

        List<Transaction> transactions = new LinkedList<>();
        transactionService.addMonitor(transaction -> {
            synchronized (transactions) {
                transactions.add(transaction);
                if (transactions.size() == count)
                    transactions.notifyAll();
            }
        });

        long start = System.currentTimeMillis();
        int countTransactions = (int) IntStream.range(0, count).boxed()
                .map(n -> createAccount(user, UUID.randomUUID().toString(), true))
                //.parallel()
                .map(account1 -> {
                    try {
                        return transactionService.createTransaction(created, user.getId(), account1.getId(), 100D);
                    } catch (UseException e) {
                        throw new RuntimeException("Internal error!");
                    }
                })
                .collect(Collectors.toList())
                .size();
        int duration = (int) (System.currentTimeMillis() - start);

        synchronized (transactions) {
            if (transactions.size() < count)
                transactions.wait(5000);
        }

        assertThat(countTransactions, is(count));
        assertThat(transactions, is(hasSize(count)));
        assertThat(duration, is(lessThanOrEqualTo(1)));
    }

    private Account createAccount(User owner, String name, boolean active, User... users) {
        Account account = mock(Account.class);
        String accountId = UUID.randomUUID().toString();
        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn(name);
        when(account.getOwner()).thenReturn(owner);
        when(account.isActive()).thenReturn(active);
        when(account.getUsers()).then(invocation -> Stream.of(users));
        when(accountsRepository.getEntityById(accountId)).thenReturn(Optional.of(account));
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

    private static class TestTransactionsRepository implements TransactionsRepository {
        private final List<Transaction> transactions = new LinkedList<>();

        @Override
        public Optional<Transaction> getEntityById(String id) {
            synchronized (transactions) {
                return transactions.stream()
                        .filter(transaction -> transaction.getId().equals(id))
                        .findFirst();
            }
        }

        @Override
        public Stream<Transaction> all() {
            synchronized (transactions) {
                return new ArrayList<>(transactions).stream();
            }
        }

        @Override
        public Transaction save(Transaction entity) {
            synchronized (transactions) {
                transactions.add(entity);
                return entity;
            }
        }

        @Override
        public Transaction delete(Transaction entity) {
            synchronized (transactions) {
                List<Transaction> tmp = transactions.stream()
                        .filter(transaction -> !transaction.getId().equals(transaction.getId()))
                        .collect(Collectors.toList());
                transactions.clear();
                transactions.addAll(tmp);
                return entity;
            }
        }
    }

    private Consumer<Transaction> waitSync1msec(Object monitorSync) {
        return transaction -> {
            synchronized (monitorSync) {
                try {
                    monitorSync.wait(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
