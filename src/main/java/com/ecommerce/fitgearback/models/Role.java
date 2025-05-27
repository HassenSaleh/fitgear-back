package com.ecommerce.fitgearback.models;

import jakarta.persistence.*;
import lombok.*;

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
    private String name;  // e.g. "ADMIN", "CUSTOMER", "VENDOR"

    // If you want bidirectional mapping, uncomment below
    /*
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
    */
}
