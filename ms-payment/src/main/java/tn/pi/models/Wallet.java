package tn.pi.models;

public class Wallet {

    private Long id;
    private Double balance;
    private String currency;
    private Account account; // optional if you need account info

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    // Inner class for Account if needed
    public static class Account {
        private Long account_id;
        private String name;
        private String email;

        // getters and setters
        public Long getAccount_id() {
            return account_id;
        }

        public void setAccount_id(Long account_id) {
            this.account_id = account_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
