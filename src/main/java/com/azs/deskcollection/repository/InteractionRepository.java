package com.azs.deskcollection.repository;

import com.azs.deskcollection.model.Interaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByDebtorId(Long debtorId);
}
