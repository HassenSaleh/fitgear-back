package com.ecommerce.fitgearback.dtos.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter@Getter
public class VendorDashboardResponse {
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
    private BigDecimal totalRevenue;

}
