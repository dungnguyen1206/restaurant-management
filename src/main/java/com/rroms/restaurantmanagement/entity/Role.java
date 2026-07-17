package com.rroms.restaurantmanagement.entity;

import com.rroms.restaurantmanagement.entity.constant.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(length = 500, columnDefinition = "NVARCHAR(255)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false)
    private RoleName roleName;

    @OneToMany(mappedBy = "role")
    private List<User> users = new ArrayList<>();
}