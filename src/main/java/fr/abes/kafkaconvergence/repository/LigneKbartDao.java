package fr.abes.kafkaconvergence.repository;

import fr.abes.kafkaconvergence.entity.LigneKbart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneKbartDao extends JpaRepository<LigneKbart, Integer> {
}
