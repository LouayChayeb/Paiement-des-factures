package tn.pi.service;


import tn.pi.entities.Admin;

public interface AdminService {
    Admin login(String email, String password);
}
