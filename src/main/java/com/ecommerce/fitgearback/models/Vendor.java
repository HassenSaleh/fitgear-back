package com.ecommerce.fitgearback.models;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "vendors")
@PrimaryKeyJoinColumn(name = "id")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Vendor extends User  {
    private String companyName;
    private String companyAddress;
    private String contactNumber;
}
