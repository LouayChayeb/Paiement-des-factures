package tn.pi.services;

import com.stripe.exception.StripeException;
import tn.pi.entities.Payment;

import java.util.List;

public interface PaymentService {
    Payment createPayment(Payment payment) throws Exception ;
    Payment getPaymentById(Long id) throws Exception;
    List<Payment> getAllPayments();
    List<Payment> getPaymentsByCustomerId(Long customerId);
}
