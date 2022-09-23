package com.comeon.userservice.domain.user.service.config;

import com.comeon.userservice.domain.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<UserAccount, Long> {
}
