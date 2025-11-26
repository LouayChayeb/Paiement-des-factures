package tn.pi.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    private Long customerId;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String address;
}


