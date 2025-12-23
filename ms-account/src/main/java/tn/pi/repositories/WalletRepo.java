package tn.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.pi.entities.Wallet;

import java.util.Optional;

public interface WalletRepo extends JpaRepository<Wallet, Integer> {
    @Query("SELECT w FROM Wallet w WHERE w.account.account_id = :accountId")
    Optional<Wallet> findByAccountId(@Param("accountId") Long accountId);
}
