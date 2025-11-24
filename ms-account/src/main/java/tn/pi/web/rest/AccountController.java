package tn.pi.web.rest;


import org.springframework.web.bind.annotation.*;
import tn.pi.entities.Account;
import tn.pi.service.AccountService;

import java.util.List;

@RestController("/account")
public class AccountController {
    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @GetMapping("/accountList/getAll")
    public List<Account> getAllAccounts(){
        return accountService.getAllAccounts();
    }
    @GetMapping("accountList/getOne/{id}")
    public Account getOneAccount(@PathVariable long id){
        return accountService.getAccountById(id);
    }
    @PostMapping("AccountList/signup")
    public Account createAccount(@RequestBody Account account){
        return accountService.createAccount(account);
    }
    @GetMapping("accountList/login")
    public Account login(@RequestParam String username, @RequestParam String password){
        return accountService.login(username, password);

    }
}
