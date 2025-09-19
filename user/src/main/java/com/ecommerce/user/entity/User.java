package com.ecommerce.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
      @UniqueConstraint(name = "uk_user_mobileNumber", columnNames = "mobileNumber")
    })
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private int age;

  @Column(unique = true)
  private String mobileNumber;

  @Column(unique = true)
  private String email;

  private Boolean isEmailSent;
}
