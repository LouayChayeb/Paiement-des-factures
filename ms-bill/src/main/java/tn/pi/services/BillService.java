package tn.pi.services;

import tn.pi.entities.Bill;
import tn.pi.entities.BillStatus;

import java.util.List;

public interface BillService {
    Bill addBill(Bill bill) throws Exception;
    List<Bill> getAllBills();
    Bill getBillById(Long id);
    Bill updateBillStatus(Long id, BillStatus status);
}
