package com.rroms.restaurantmanagement.entity;

import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(length = 500, columnDefinition = "NVARCHAR(50)")
    private String note;

    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @Column(name = "full_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(50)")
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ReservationTable> reservationTables = new HashSet<>();

    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL
    )
    private Set<Payment> payments = new HashSet<>();
}