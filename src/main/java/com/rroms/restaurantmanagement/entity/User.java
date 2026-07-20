package com.rroms.restaurantmanagement.entity;

import com.rroms.restaurantmanagement.entity.constant.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Transient
    public String getFullName() {
        return Stream.of(firstName, middleName, lastName)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
    }

    @Transient
    public String getInitials() {
        String initials = Stream.of(firstName, middleName, lastName)
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.substring(0, 1).toUpperCase())
                .collect(Collectors.joining());
        return initials.isBlank() ? "U" : initials;
    }
}
