package tn.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.pi.entities.Account;
import tn.pi.exception.AccountWithPhoneNumberExists;
import tn.pi.exception.ThisAccountNotFoundException;
import tn.pi.repositories.AccountRepo;

import java.util.List;
import java.util.Optional;

@Service
public class AccountImplementationService implements AccountService {
    private final AccountRepo accountRepo;
    private final PasswordEncoder passwordEncoder;
    public AccountImplementationService(AccountRepo accountRepo, PasswordEncoder passwordEncoder) {
        this.accountRepo = accountRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Account getAccountById(Long accountId) {
        Optional<Account> OptionalAccount = accountRepo.findById(accountId);
        if (OptionalAccount.isEmpty()) {
            throw new ThisAccountNotFoundException("account not found exception");
        }
        return OptionalAccount.get();
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> OptionalAccounts = accountRepo.findAll();
        if (OptionalAccounts.isEmpty()) {
            throw new ThisAccountNotFoundException("account list is empty");

        }

        return OptionalAccounts;
    }
    @Override
    public Account createAccount(Account account) {
        List<Account> accountList = accountRepo.findAll();
        if (accountRepo.findByPhone(account.getPhone()).isPresent()){
            throw new AccountWithPhoneNumberExists("account with this phone number already exisits");
        }
        String hashedPassword = passwordEncoder.encode(account.getPassword());
        account.setPassword(hashedPassword);
        return accountRepo.save(account);
    }
    @Override
    public void updateAccount(Account account) {


    }
    @Override
    public void deleteAccount(Long accountId) {

    }
    @Override
    public Account login(String name, String password) {

        Optional<Account> optionalAccount = accountRepo.findByName(name);

        if (optionalAccount.isEmpty()) {
            throw new ThisAccountNotFoundException("Wrong username or password");
        }

        Account account = optionalAccount.get();

        // CHECK HASHED PASSWORD
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new ThisAccountNotFoundException("Wrong username or password");
        }

        return account;
    }


}
