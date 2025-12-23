package tn.pi.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pi.entities.Wallet;
import tn.pi.service.WalletService;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/create/{userId}")
    public ResponseEntity<Wallet> createWallet(
            @PathVariable Long userId,
            @RequestBody Wallet walletBody
    ) {
        Wallet wallet = walletService.createWalletForUser(userId, walletBody.getBalance(),walletBody.getBankName(),walletBody.getCurrency());
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/update")
    public ResponseEntity<Wallet> updateBalance(@RequestParam Long userId, @RequestParam Double amount) {
        Wallet wallet = walletService.updateBalance(userId, amount);
        return ResponseEntity.ok(wallet);
    }
}