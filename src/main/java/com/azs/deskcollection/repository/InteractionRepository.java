package com.azs.deskcollection.repository;

import com.azs.deskcollection.model.Interaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByDebtorId(Long debtorId);

    @org.springframework.data.jpa.repository.Query("SELECT u.username, COUNT(i) FROM Interaction i JOIN i.debtor d JOIN d.user u WHERE i.type = :type GROUP BY u.username")
    List<Object[]> countInteractionsByType(
            @org.springframework.data.repository.query.Param("type") com.azs.deskcollection.model.Interaction.Type type);
}
