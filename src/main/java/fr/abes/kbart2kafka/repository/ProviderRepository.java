package fr.abes.kbart2kafka.repository;


import fr.abes.kbart2kafka.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {
    Optional<Provider> findByProvider(String provider);
}
