package tn.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.pi.entities.Admin;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepo extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmail(String email);
}
