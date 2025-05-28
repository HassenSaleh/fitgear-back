package com.ecommerce.fitgearback.models;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "id")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class Customer extends User{

    private String shippingAddress;

    private String phoneNumber;
}
