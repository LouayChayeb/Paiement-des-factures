package tn.pi.service;

import tn.pi.entities.Account;
import org.springframework.stereotype.Service;
import tn.pi.entities.Wallet;
import tn.pi.repositories.AccountRepo;
import tn.pi.repositories.WalletRepo;

@Service
public class WalletService {

    private final WalletRepo walletRepository;
    private final AccountRepo accountRepository;

    public WalletService(WalletRepo walletRepository, AccountRepo accountRepository) {
        this.walletRepository = walletRepository;
        this.accountRepository = accountRepository;
    }


    public Wallet createWalletForUser(Long account_id, Double initialAmount, String bankName, String currency) {

        Account account = accountRepository.findById(account_id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = new Wallet();
        wallet.setAccount(account);
        wallet.setBalance(initialAmount != null ? initialAmount : 0.0);

        // Set bankName and currency from frontend input
        wallet.setBankName(bankName != null ? bankName : "");
        wallet.setCurrency(currency != null ? currency : "");

        return walletRepository.save(wallet);
    }




    public Wallet getWalletByUserId(Long accountId) {
        return walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + accountId));
    }


    public Wallet updateBalance(Long userId, Double amount) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        return walletRepository.save(wallet);
    }
}