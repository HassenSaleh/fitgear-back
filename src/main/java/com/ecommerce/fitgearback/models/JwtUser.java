package com.ecommerce.fitgearback.models;


public interface JwtUser {
    Long getId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPassword();
    String getRole();
}
