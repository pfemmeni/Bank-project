
package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.UseException;


import java.util.Scanner;
import java.util.stream.Collectors;


import static se.sensera.banking.impl.Colors.*;

public class ClientInterface {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UsersRepositoryImpl users = new UsersRepositoryImpl();
    private static final UserServiceImpl userService = new UserServiceImpl(users);
    private static final AccountsRepositoryImpl accounts = new AccountsRepositoryImpl();
    private static final AccountServiceImpl accountService = new AccountServiceImpl(users, accounts);
    private static final TransactionsRepositoryImpl transactions = new TransactionsRepositoryImpl();
    private static final TransactionServiceImpl transactionService = new TransactionServiceImpl(users, accounts, transactions);


    public static void main(String[] args) throws UseException {
        boolean quit = false;
        int choice;
        System.out.println(ANSI_RED + "<><><><><><><><><><><><><><><><><><><><><>");
        System.out.println(ANSI_RED + "<> Hello and welcome to Sensera Banking <>");
        while (!quit) {
            printInstructions();
            System.out.println(ANSI_RED + "<><><><><><><><><><><><><><><><><><><><><>");
            System.out.println(ANSI_GREEN + "Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> createNewUser();
                case 2 -> updateUser();
                case 3 -> inactivateUser();
                case 4 -> findUser();
                case 5 -> createAccount();
                case 6 -> updateExistingAccount();
                case 7 -> inactivateAccount();
                case 8 -> findAccount();
                case 9 -> checkFunds();
                case 10 -> createTransaction();
                default -> quit = true;
            }
        }
    }


    public static void createNewUser() throws UseException {
        System.out.println(ANSI_CYAN + "Write your name: ");
        String name = scanner.nextLine();
        System.out.println("Write your personal Identification Number: ");
        String personalIdentificationNumber = scanner.nextLine();
        userService.createUser(name, personalIdentificationNumber);
        System.out.println("User added");
        String userId = users.all().filter(user -> user.getName().equals(name)
                && user.getPersonalIdentificationNumber().equals(personalIdentificationNumber))
                .findAny()
                .get()
                .getId();
        System.out.println("Your User Id is: " + userId);
    }

    public static void updateUser() throws UseException {
        String changeType = "";
        System.out.println(ANSI_CYAN + "To change/update user info");
        System.out.println("To update your name press 1");
        System.out.println("To update your person identification number press 2");
        changeType = scanner.nextLine();

        if (changeType.equals("1")) {
            System.out.println(ANSI_CYAN + "Enter your User Id: ");
            String id = scanner.nextLine();
            System.out.println("Write new Name: ");
            String newName = scanner.nextLine();
            userService.changeUser(id, changeUser -> changeUser.setName(newName));
        } else if (changeType.equals("2")) {
            System.out.println(ANSI_CYAN + "Enter your User Id: ");
            String id = scanner.nextLine();
            System.out.println("Write new Personal Identification Number: ");
            String newPersonalIdentificationNumber = scanner.nextLine();
            userService.changeUser(id, changeUser -> {
                try {
                    changeUser.setPersonalIdentificationNumber(newPersonalIdentificationNumber);
                } catch (UseException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println("User updated");
    }

    private static void inactivateUser() throws UseException {
        System.out.println(ANSI_CYAN + "To inactivate user");
        System.out.println("Enter your userid");
        String userId = scanner.nextLine();
        userService.inactivateUser(userId);
        System.out.println("User inactivated");
    }

    private static void findUser() {
        System.out.println(ANSI_CYAN + "To search for a user");
        System.out.println("Enter search text");
        String searchString = scanner.nextLine();
        String foundUsers = userService.find(searchString, null, null, UserService.SortOrder.None)
                .collect(Collectors.toList())
                .toString();
        System.out.println("User found is " + foundUsers);
    }

    private static void createAccount() throws UseException {
        System.out.println(ANSI_PURPLE + "To create an account");
        System.out.println("Enter your userId: ");
        String userId = scanner.nextLine();
        System.out.println("Enter your account name: ");
        String accountName = scanner.nextLine();

        accountService.createAccount(userId, accountName);
        String accountId = accounts.all()
                .filter(account -> account.getOwner().getId().equals(userId)
                        && account.getName().equals(accountName))
                .findAny()
                .get()
                .getId();
        System.out.println("Account created, your account id is: " + accountId);

    }

    private static void updateExistingAccount() throws UseException {
        System.out.println(ANSI_PURPLE + "To change your account name");
        System.out.println("Enter your userid: ");
        String userId = scanner.nextLine();
        System.out.println("Enter your account id");
        String accountId = scanner.nextLine();
        System.out.println("Enter your new account name: ");
        String newAccountName = scanner.nextLine();
        accountService.changeAccount(userId, accountId, changeAccount -> {
            try {
                changeAccount.setName(newAccountName);
            } catch (UseException e) {
                e.printStackTrace();
            }
        });
        System.out.println("Account updated ");
    }

    private static void inactivateAccount() throws UseException {
        System.out.println(ANSI_PURPLE + "To inactivate your account");
        System.out.println("Enter your userid");
        String userId = scanner.nextLine();
        System.out.println("Enter your account id");
        String accountId = scanner.nextLine();

        accountService.inactivateAccount(userId, accountId);
    }

    private static void findAccount() throws UseException {
        System.out.println(ANSI_PURPLE + "Search for an account");
        System.out.println("Enter your search");
        String searchValue = scanner.nextLine();
        String searchResult = accountService.findAccounts(searchValue, null, null, null, AccountService.SortOrder.None)
                .map(Account::getName)
                .collect(Collectors.toList())
                .toString();


        System.out.println("Account(s) found was : " + searchResult);
    }

    private static void checkFunds() throws UseException {
        System.out.println(ANSI_BLUE + "Check your funds");
        System.out.println("Enter your userid");
        String userId = scanner.nextLine();
        System.out.println("Enter your account id");
        String accountId = scanner.nextLine();

//        Date date = Calendar.getInstance().getTime();
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String created = dateFormat.format(date);
        String created = "2021-01-01 10:34";
        double funds = transactionService.sum(created, userId, accountId);
        System.out.println("Your account has: " + funds + " kr");
    }

    private static void createTransaction() throws UseException {
        System.out.println(ANSI_BLUE + "Create an transaction");
        String created = "2020-01-01 10:34";
        System.out.println("Enter your user id:");
        String userId = scanner.nextLine();
        System.out.println("Enter your account id:");
        String accountId = scanner.nextLine();
        System.out.println("Enter the amount");
        double amount = scanner.nextInt();
        scanner.nextLine();

        transactionService.createTransaction(created, userId, accountId, amount);
        System.out.println("Transaction completed with: " + amount + " kr");
    }

    public static void printInstructions() {
        System.out.println(ANSI_RED + "<><><><><><><><><><><><><><><><><><><><><>");
        System.out.println("\t 0 - To show instructions");
        System.out.println(ANSI_CYAN + "\t 1 - To create a user");
        System.out.println("\t 2 - To update an existing user");
        System.out.println("\t 3 - To inactivate a user");
        System.out.println("\t 4 - To find a user");
        System.out.println(ANSI_PURPLE + "\t 5 - To create an account.");
        System.out.println("\t 6 - To update an existing account.");
        System.out.println("\t 7 - To inactivate an account.");
        System.out.println("\t 8 - To find an account.");
        System.out.println(ANSI_BLUE + "\t 9 - To check funds.");
        System.out.println("\t 10 - To create a transaction.");
    }

}
