package com.finadem.repository;


import com.finadem.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query("FROM Account a WHERE a.accountNumber = :accountNumber")
    Account findAccountInformationByAccountNumber(@Param("accountNumber") String accountNumber);
}
