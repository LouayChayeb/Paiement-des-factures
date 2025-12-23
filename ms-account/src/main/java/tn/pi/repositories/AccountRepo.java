package tn.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.pi.entities.Account;
import tn.pi.entities.Wallet;

import java.util.Optional;

public interface AccountRepo extends JpaRepository<Account, Long> {

    Optional<Account> findByPhone(String phone);
    Optional<Account> findByName(String name);
    Optional<Account> findById(Long account_id);
}
