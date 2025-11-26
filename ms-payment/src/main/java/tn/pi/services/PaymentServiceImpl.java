package tn.pi.services;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;
import tn.pi.clients.BillClient;
import tn.pi.entities.Payment;
import tn.pi.models.Bill;
import tn.pi.repositories.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillClient billClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BillClient billClient) {
        this.paymentRepository = paymentRepository;
        this.billClient = billClient;
    }



    public Payment createPayment(Payment payment) throws Exception {

        Bill bill = billClient.getBillById(payment.getBillId());
        if (bill == null) {
            throw new Exception("Bill not found");
        }

        if ("PAID".equalsIgnoreCase(bill.getStatus())) {
            throw new Exception("This bill is already paid");
        }

        payment.setAmount(bill.getAmount());
        payment.setCustomerId(bill.getCustomerId());

        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(bill.getStripePriceId())
                                .setQuantity(1L)
                                .build())
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8100/success")
                .setCancelUrl("http://localhost:8100/cancel")
                .build();

        Session session = Session.create(params);

        payment.setStripePaymentIntentId(session.getId());
        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);


        billClient.updateBillStatus(bill.getId(), "PAID");

        savedPayment.setStripeCheckoutUrl(session.getUrl());

        return savedPayment;
    }
    public Payment getPaymentById(Long id) throws Exception {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new Exception("Payment not found"));
    }


    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }


    public List<Payment> getPaymentsByCustomerId(Long customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }
}