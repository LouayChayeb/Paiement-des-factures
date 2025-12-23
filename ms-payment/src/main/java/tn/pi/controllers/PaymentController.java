package tn.pi.controllers;

import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.pi.entities.Payment;
import tn.pi.services.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/payments")

public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) throws Exception  {
        return paymentService.createPayment(payment);
    }


    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) throws Exception {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Payment> getPaymentsByCustomer(@PathVariable Long customerId) {
        return paymentService.getPaymentsByCustomerId(customerId);
    }
}