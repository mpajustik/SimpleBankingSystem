package banking;

public class Card {
    int balance;
    private final String cardNumber;
    private final String pin;


    public Card() {
        balance = 0;
        cardNumber = numbersGeneration();
        pin = fourNumbersGeneration();
    }

    private String numbersGeneration() {
        String fifteen = "400000".concat(nineNumbersGeneration());
        return fifteen.concat(luhnAlgorithm(fifteen));
    }

    private String nineNumbersGeneration() {
        int x = (int) (Math.random() * 999999999);
        return String.format("%09d", x);
    }

    private String fourNumbersGeneration() {
        int x = (int) (Math.random() * 9999);
        return String.format("%04d", x);
    }

    private String luhnAlgorithm(String s) {
        int[] digits = new int[s.length()];
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            digits[i] = Integer.parseInt(s.split("")[i]);

            if (i % 2 == 0) {
                digits[i] *= 2;
            }
            if (digits[i] > 9) {
                digits[i] -= 9;
            }
            sum += digits[i];
        }

        if (sum % 10 == 0) {
            return "0";
        }
        int sixteen = 10 - (sum % 10);

        return Integer.toString(sixteen);
    }

    public String getPin() {
        return pin;
    }

    public String getCardNumber() {
        return cardNumber;
    }

}