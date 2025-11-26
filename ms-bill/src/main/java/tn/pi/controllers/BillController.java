package tn.pi.controllers;

import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tn.pi.entities.Bill;
import tn.pi.entities.BillStatus;
import tn.pi.services.BillService;

import java.util.List;

@RestController
@RequestMapping("/bill")
public class BillController {
    private BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping("/addbill")
    public ResponseEntity<?> createBill(@RequestBody Bill bill) {
        try {
            Bill savedBill = billService.addBill(bill);
            return ResponseEntity.ok(savedBill);
        } catch (StripeException e) {
            // You can return a custom error DTO or message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Stripe error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/all")
    public List<Bill> getAllBills() {
        return billService.getAllBills();
    }


    @GetMapping("/{id}")
    public Bill getBillById(@PathVariable Long id) {
        return billService.getBillById(id);
    }


    @PatchMapping("/update-status/{id}")
    public ResponseEntity<Bill> updateBillStatus(@PathVariable Long id, @RequestBody BillStatus status) {
        Bill updatedBill = billService.updateBillStatus(id, status);
        return ResponseEntity.ok(updatedBill);
    }
}
