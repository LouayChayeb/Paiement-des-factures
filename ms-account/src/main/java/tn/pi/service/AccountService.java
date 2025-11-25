package tn.pi.service;

import tn.pi.entities.Account;

import java.util.List;

public interface AccountService {

    Account getAccountById(Long accountId);
    List<Account> getAllAccounts();
    Account createAccount(Account account);
    void updateAccount(Account account);
    void deleteAccount(Long accountId);
    Account login(String phone, String password);



}
