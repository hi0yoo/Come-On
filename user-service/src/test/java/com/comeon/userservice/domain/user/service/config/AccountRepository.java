package com.comeon.userservice.domain.user.service.config;

import com.comeon.userservice.domain.user.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
