package tn.pi.clients;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tn.pi.models.Account;
import tn.pi.models.Bill;
import tn.pi.models.Wallet;

@Component
public class BillClient {

    private final RestTemplate restTemplate;

    public BillClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Bill getBillById(Long billId) {
        String url = "http://localhost:8083/api/bill/bill/" + billId;
        return restTemplate.getForObject(url, Bill.class);
    }

    public void updateBillStatus(Long id, String status) {
        String url = "http://localhost:8083/api/bill/bill/update-status/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("\"" + status + "\"", headers);

        restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
    }
    public Wallet updateBalance(Long userId, Double amount) {
        String url = "http://localhost:8083/api/account/wallet/update?userId=" + userId + "&amount=" + amount;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Wallet> response = restTemplate.exchange(url, HttpMethod.POST, entity, Wallet.class);
        return response.getBody();
    }
}
