package tn.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.pi.entities.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {
}
