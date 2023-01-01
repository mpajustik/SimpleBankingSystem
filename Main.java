package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    ArrayList<Card> listOfClients = new ArrayList<>();
    Scanner sc = new Scanner(System.in);
    SQLiteDataSource sds;

    public static void main(String[] args) {
        new Main().init(args);
    }

    void init(String[] args) {
        String url = "jdbc:sqlite:".concat(args[1]);
        sds = new SQLiteDataSource();
        sds.setUrl(url);

        try (Connection cn = sds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS card (id INTEGER PRIMARY KEY, number TEXT NOT NULL, pin TEXT NOT NULL, balance INTEGER DEFAULT 0)");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            welcomeText();
            chooseAction(sc.next());
        }
    }


    private void welcomeText() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    private void chooseAction(String input) {
        switch (input) {
            case "1":
                createAccount();
                break;
            case "2":
                logInAccount();
                break;
            case "0":
                exit();
        }
    }

    private void createAccount() {
        Card cd = new Card();
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cd.getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(cd.getPin());
        listOfClients.add(cd);

        addCardToDb(cd.getCardNumber(), cd.getPin());
    }

    private void addCardToDb(String cardNumber, String pin) {

        try (Connection cn = sds.getConnection()) {
            try (Statement st = cn.createStatement()) {

                st.executeUpdate("INSERT INTO card (number, pin) VALUES (" + "'" + cardNumber + "', '" + pin + "')");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logInAccount() {
        String accountNumber = "";
        System.out.println("Enter your card number:");
        String cardNumber = sc.next();
        System.out.println("Enter your PIN:");
        String cardPin = sc.next();


        accountNumber = authentication(cardNumber, cardPin);
        if (!accountNumber.equals("")) {
            System.out.println("You have successfully logged in!");
            insideAccount(cardNumber);
        }

    }

    private String authentication(String cardNumber, String cardPin) {
        String getCardNumber = "";
        try (Connection cn = sds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                try (ResultSet currentUser = st.executeQuery("SELECT number FROM card WHERE number = " + cardNumber + " AND pin = " + cardPin)) {
                    getCardNumber = currentUser.getString("number");
                    System.out.println(getCardNumber);
                } catch (Exception e) {
                    System.out.println("Wrong card number or PIN!");
                    ;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getCardNumber;
    }


    private void insideAccount(String cardNumber) {
        boolean insideAccountContinue = true;
        while (insideAccountContinue) {
            printInsideAccountActions();
            insideAccountContinue = actionsToDoInsideAccount(cardNumber);
        }

    }

    private boolean actionsToDoInsideAccount(String cardNumber) {
        switch (sc.next()) {
            case "1":
                System.out.println("Balance: " + getBalance(cardNumber));
                return true;
            case "2":
                addIncome(cardNumber);
                return true;
            case "3":
                System.out.println("Do transfer: ");
                doTransfer(cardNumber);
                return true;
            case "4":
                System.out.println("Close account: ");
                closeAccount(cardNumber);
                return false;
            case "5":
                System.out.println("You have successfully logged out!");
                return false;
            case "0":
                exit();
            default:
                return true;
        }
    }

    private static void printInsideAccountActions() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    private void doTransfer(String cardNumber) {
        Scanner scanner = new Scanner(System.in);
        int currentBalance = getBalance(cardNumber);
        System.out.println("Enter card number:");
        String otherCardNumber = scanner.nextLine();
        boolean isLuhnNumber = testLuhnNumberAlgoritm(otherCardNumber);
        if (!isLuhnNumber) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return;
        }
        boolean cardExistsInDatabase = doesCardExistInDatabase(otherCardNumber);
        if (!cardExistsInDatabase) {
            System.out.println("Such a card does not exist.");
        }
        System.out.println("Enter how much money you want to transfer:");
        int moneyToTransfer = scanner.nextInt();

        if (currentBalance < moneyToTransfer) {
            System.out.println("Not enough money!");
        } else {
            addMoneyToOtherAccount(cardNumber, otherCardNumber, moneyToTransfer);
            System.out.println("Success!");
        }
    }

    private boolean doesCardExistInDatabase(String otherCardNumber) {
        int howManyCardNumberExist = 0;
        ResultSet resultSet = null;
        try (Connection cn = sds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                resultSet = st.executeQuery("SELECT  * FROM card WHERE number = " + otherCardNumber);
                while (resultSet.next()) {
                    howManyCardNumberExist++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return howManyCardNumberExist > 0;
    }

    private void addMoneyToOtherAccount(String cardNumber, String otherCardNumber, int moneyToTransfer) {
        try (Connection cn = sds.getConnection()) {
            try (Statement st = cn.createStatement()) {

                st.executeUpdate("UPDATE card SET balance = balance + " + moneyToTransfer + " WHERE number=" + otherCardNumber);
                st.executeUpdate("UPDATE card SET balance = balance - " + moneyToTransfer + " WHERE number=" + cardNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeAccount(String cardNumber) {
        try (Connection cn = sds.getConnection()) {
            try (Statement st = cn.createStatement()) {

                st.executeUpdate("DELETE FROM card WHERE number=" + cardNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("The account has been closed!");
    }

    private int getBalance(String cardNumber) {
        int balance = 0;
        try (Connection cn = sds.getConnection()) {
            try (Statement st = cn.createStatement()) {
                try (ResultSet currentUser = st.executeQuery("SELECT balance FROM card WHERE number = " + cardNumber)) {
                    balance = currentUser.getInt("balance");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance;
    }

    private void addIncome(String cardNumber) {
        System.out.println("Enter income:");
        int addedSum = sc.nextInt();
        try (Connection cn = sds.getConnection()) {
            try (Statement st = cn.createStatement()) {

                st.executeUpdate("UPDATE card SET balance = balance + " + addedSum + " WHERE number=" + cardNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Income was added!");
    }

    private static int[] getIntegerArrayFromString(String cardNumber) {
        int[] cardNumberArray = new int[16];
        for (int i = 0; i < cardNumber.length(); i++) {
            cardNumberArray[i] = Integer.parseInt(String.valueOf(cardNumber.charAt(i)));
        }
        return cardNumberArray;
    }

    private static boolean testLuhnNumberAlgoritm(String cardNumber) {
        int[] cardNumberArray = getIntegerArrayFromString(cardNumber);
        int[] tempArray = new int[16];
        int tempNum = 0;
        int sum = 0;
        for (int i = 0; i < tempArray.length - 1; i++) {
            if (i % 2 != 0) {
                tempArray[i] = cardNumberArray[i];
            } else {
                tempNum = 2 * cardNumberArray[i];
                if (tempNum > 9) {
                    tempArray[i] = tempNum - 9;
                } else {
                    tempArray[i] = tempNum;
                }
            }
            sum += tempArray[i];
        }

        return (sum + cardNumberArray[15]) % 10 == 0;
    }

    private void exit() {
        System.out.println("Bye!");
        System.exit(0);
    }
}