package fr.abes.kbart2kafka.repository;

import fr.abes.kbart2kafka.entity.ProviderPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderPackageRepository extends JpaRepository<ProviderPackage, Integer> {
    List<ProviderPackage> findByPackageNameAndProviderIdtProvider(String packageName, Integer providerIdtProvider);

}
