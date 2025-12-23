package tn.pi.web.rest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pi.dtos.LoginRequest;
import tn.pi.entities.Account;
import tn.pi.service.AccountService;

import java.util.List;
import java.util.Map;


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
    @PostMapping("accountList/signup")
    public Account createAccount(@RequestBody Account account){
        return accountService.createAccount(account);
    }
    @PostMapping("/accountList/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {

        Account account = accountService.login(request.getPhone(), request.getPassword());


        HttpSession session = servletRequest.getSession();
        session.setAttribute("user", account);


        String sessionId = session.getId();


        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "account", account,
                        "sessionId", sessionId
                )
        );
    }

    @GetMapping("/accountList/me")
    public ResponseEntity<?> me(HttpSession session) {

        Account loggedUser = (Account) session.getAttribute("user");

        if (loggedUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not logged in"));
        }

        return ResponseEntity.ok(loggedUser);
    }
    @PostMapping("/accountList/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }




}
