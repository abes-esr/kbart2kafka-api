package fr.abes.kafkaconvergence.repository;

import fr.abes.kafkaconvergence.entity.ErreurResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErreurResultDao extends JpaRepository<ErreurResult, Integer> {
}
