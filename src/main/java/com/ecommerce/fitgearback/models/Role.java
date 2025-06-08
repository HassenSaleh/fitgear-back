package com.ecommerce.fitgearback.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "roles")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g. "ADMIN", "CUSTOMER", "VENDOR"

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<Customer> customers;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<Admin> admins;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<Vendor> vendors;
}