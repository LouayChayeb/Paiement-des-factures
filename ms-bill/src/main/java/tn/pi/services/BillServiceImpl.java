package tn.pi.services;

import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tn.pi.entities.Bill;
import tn.pi.entities.BillStatus;
import tn.pi.repositories.BillRepository;

import java.util.List;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    public BillServiceImpl(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Override
    public Bill addBill(Bill bill) throws Exception {
        Bill savedBill = billRepository.save(bill);


        ProductCreateParams productParams = ProductCreateParams.builder()
                .setName("Bill " + savedBill.getBillNumber())
                .setDescription(savedBill.getDescription())
                .build();
        Product stripeProduct = Product.create(productParams);


        PriceCreateParams priceParams = PriceCreateParams.builder()
                .setProduct(stripeProduct.getId())
                .setUnitAmount((long)(savedBill.getAmount() * 100))
                .setCurrency("usd")
                .build();
        Price stripePrice = Price.create(priceParams);


        savedBill.setStripeProductId(stripeProduct.getId());
        savedBill.setStripePriceId(stripePrice.getId());

        return billRepository.save(savedBill);
    }

    @Override
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    @Override
    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
    }
    @Override
    public Bill updateBillStatus(Long id, BillStatus status) {
        Bill existing = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));

        existing.setStatus(status);
        return billRepository.save(existing);
    }
    @Override
    public List<Bill> getBillsByCustomerId(Long customerId) {
        return billRepository.findByCustomerId(customerId);
    }

}
