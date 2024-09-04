package com.paulo.ecommerceX.domain;

import com.paulo.ecommerceX.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity(name = "tb_user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "userId" )
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Setter
    @NotBlank
    @Size(min = 5, max = 35)
    @Column(unique = true)
    private String login;

    @Setter
    @NotNull
    @Size(min = 8)
    private String password;

    private UserRole role;
}
