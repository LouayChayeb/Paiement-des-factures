package tn.pi.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.pi.entities.Account;
import tn.pi.entities.Admin;
import tn.pi.exception.ThisAccountNotFoundException;
import tn.pi.repositories.AdminRepo;

import java.util.Optional;

@Service
public class AdminImplService  implements AdminService{
    private final AdminRepo adminRepos;
    private final PasswordEncoder passwordEncoder;
    public AdminImplService(AdminRepo adminRepos, PasswordEncoder passwordEncoder) {
        this.adminRepos = adminRepos;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public Admin login(String email, String password) {

        Optional<Admin> optionaladmin = adminRepos.findByEmail(email);
        if (optionaladmin.isEmpty()){
            throw new ThisAccountNotFoundException("Wrong creds");
        }
        Admin admin = optionaladmin.get();


        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new ThisAccountNotFoundException("Wrong username or password");
        }
        return admin;
    }
}
