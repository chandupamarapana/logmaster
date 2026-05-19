package com.logmaster.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal circumference;

    @Column(name = "length_ft", precision = 4, scale = 1)
    private BigDecimal lengthFt;

    @Column(name = "cubic_feet", precision = 10, scale = 4)
    private BigDecimal cubicFeet;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "entry_order")
    private int entryOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
