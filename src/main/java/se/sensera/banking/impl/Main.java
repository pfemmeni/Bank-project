package se.sensera.banking.impl;

import se.sensera.banking.exceptions.UseException;

import java.util.Scanner;

public class Main {

   /* private static Scanner scanner = new Scanner(System.in);
    private static UsersRepositoryImpl users = new UsersRepositoryImpl();
    private static UserServiceImpl userService = new UserServiceImpl(users);

    public static void main(String[] args) throws UseException {
        boolean quit = false;
        String choice;
        System.out.println("Hello and welcome to Sensera Banking");
        printMainInstructions();
        while (!quit) {
            choice = scanner.nextLine();
            switch (choice) {
                case "0" -> {
                    printUserInstructions();
                    switch (scanner.nextLine()) {
                        case "0" -> createNewUser();
                        case "1" -> {
                            printChangeUserInstructions();
                            switch (scanner.nextLine()) {
                                case "0" -> updateUser(0);
                                case "1" -> updateUser(1);
                            }
                        }
                    }
                }
                case "1" -> printAccountInstructions();
                case "2" -> printTransactionInstructions();
                default -> quit = true;
            }
        }

    }

    public static void createNewUser() throws UseException {
        System.out.println("Write your name: ");
        String name = scanner.nextLine();
        System.out.println("Write your personalIdentificationNumber: ");
        String personalIdentificationNumber = scanner.nextLine();

        userService.createUser(name, personalIdentificationNumber);

        System.out.println("User added");
       users.all().forEach(System.out::println);
        printMainInstructions();
    }

    public static void updateUser(int changeType) throws UseException {
        if (changeType == 0) {
            System.out.println("Enter your id number: ");
            String id = scanner.nextLine();

            System.out.println("Write new name: ");
            String newName = scanner.nextLine();
            userService.changeUser(id, changeUser -> changeUser.setName(newName));
        }
        if (changeType == 1) {
            System.out.println("Enter your id number: ");
            String id = scanner.nextLine();

            System.out.println("Write new personalIdentificationNumber: ");
            String newPersonalIdentificationNumber = scanner.nextLine();
            userService.changeUser(id, changeUser -> {
                try {
                    changeUser.setPersonalIdentificationNumber(newPersonalIdentificationNumber);
                } catch (UseException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    public static void printMainInstructions() {

        System.out.println("\n Enter number to choose, press any other key to cancel");
        System.out.println("\t 0 - User");
        System.out.println("\t 1 - Account");
        System.out.println("\t 2 - Transaction");
        System.out.println("Enter your choice: ");
    }

    public static void printUserInstructions() {

        System.out.println("\t 0 - Add user");
        System.out.println("\t 1 - Update user");
        System.out.println("\t 2 - Inactivate user");
        System.out.println("\t 3 - Find User");
        System.out.println("\t 4 - Go back");
        System.out.println("Enter your choice: ");

    }

    public static void printChangeUserInstructions() {
        System.out.println("\t 0 - Change name");
        System.out.println("\t 1 - Change identification number");
        System.out.println("Enter your choice: ");
    }

    public static void printAccountInstructions() {
        System.out.println("\t 0 - Create account.");
        System.out.println("\t 1 - Update account.");
        System.out.println("\t 2 - Inactivate account");
        System.out.println("\t 3 - Find Account");
        System.out.println("\t 4 - Check founds");
        System.out.println("\t 5 - Go back");
        System.out.println("Enter your choice: ");
    }

    public static void printTransactionInstructions() {
        System.out.println("\t 0 - Create transaction.");
        System.out.println("\t 1 - Check found");
        System.out.println("\t 2 - Go back");
        System.out.println("Enter your choice: ");

    }*/
}
