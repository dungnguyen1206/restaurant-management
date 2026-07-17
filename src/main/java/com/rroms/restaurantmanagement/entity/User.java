package com.rroms.restaurantmanagement.entity;

import com.rroms.restaurantmanagement.entity.constant.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = true)
    private String passwordHash;

    @Column(name = "first_name", columnDefinition = "NVARCHAR(20)")
    private String firstName;

    @Column(name = "middle_name", columnDefinition = "NVARCHAR(30)")
    private String middleName;

    @Column(name = "last_name", columnDefinition = "NVARCHAR(20)", nullable = false)
    private String lastName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_role_id")
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();
}
