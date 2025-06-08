package com.ecommerce.fitgearback.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "street")
    private String street;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE"))
    @ToString.Exclude
    private Customer customer;
}
