package com.ecommerce.fitgearback.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "vendors")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Vendor implements JwtUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,name = "first_name")
    private String firstName;

    @Column(nullable = false,name = "last_name")
    private String lastName;

    @Column(nullable = false, unique = true,name = "email")
    private String email;

    @Column(nullable = false,name = "password")
    private String password;

    @Column(nullable = false,name = "company_name")
    private String companyName;
    @Column(name = "company_ddress")
    private String companyAddress;
    @Column(name = "company_number")
    private String contactNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "vendor_roles",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonIgnore
    private Set<Role> roles;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getRole() {
        return "";
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    public List<String> getRoleNames() {
        return roles.stream().map(Role::getName).toList();
    }
}
