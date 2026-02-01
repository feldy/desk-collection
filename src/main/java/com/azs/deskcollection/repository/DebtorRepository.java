package com.azs.deskcollection.repository;

import com.azs.deskcollection.model.Debtor;
import com.azs.deskcollection.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebtorRepository extends JpaRepository<Debtor, Long> {
    List<Debtor> findByUser(User user);

    List<Debtor> findByUserId(Long userId);

    Optional<Debtor> findByIdAndUser(Long id, User user);
}
