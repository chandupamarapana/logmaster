package com.logmaster.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table( name = "price_configs",
uniqueConstraints = @UniqueConstraint(
        name = "uk_delivery_type_category",
        columnNames = {"delivery_id", "log_type", "category"}
        )
    )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceConfig {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", nullable = false)
    private LogType logType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @NotNull
    @Column(name = "price_per_cft", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerCft;

}
