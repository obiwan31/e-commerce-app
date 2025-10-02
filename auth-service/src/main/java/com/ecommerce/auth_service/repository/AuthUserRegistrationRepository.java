package com.ecommerce.auth_service.repository;

import com.ecommerce.auth_service.entity.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRegistrationRepository extends JpaRepository<AuthUser, Long> {

  Optional<AuthUser> findByUsername(String userName);

  Optional<AuthUser> findByEmail(String userName);
}
