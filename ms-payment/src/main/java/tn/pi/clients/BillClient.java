package tn.pi.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tn.pi.models.Bill;

@Component
public class BillClient {

    private final RestTemplate restTemplate;

    public BillClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Bill getBillById(Long billId) {
        String url = "http://localhost:8100/bill/" + billId;
        return restTemplate.getForObject(url, Bill.class);
    }

    public void updateBillStatus(Long id, String status) {
        String url = "http://localhost:8100/bill/update-status/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("\"" + status + "\"", headers);

        restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
    }
}
