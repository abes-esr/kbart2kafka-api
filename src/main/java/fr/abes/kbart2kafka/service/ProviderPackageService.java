package fr.abes.kbart2kafka.service;

import fr.abes.kbart2kafka.entity.Provider;
import fr.abes.kbart2kafka.entity.ProviderPackage;
import fr.abes.kbart2kafka.repository.ProviderPackageRepository;
import fr.abes.kbart2kafka.repository.ProviderRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ProviderPackageService {
    private final ProviderPackageRepository repository;

    private final ProviderRepository providerRepository;

    public ProviderPackageService(ProviderPackageRepository repository, ProviderRepository providerRepository) {
        this.repository = repository;
        this.providerRepository = providerRepository;
    }

    public boolean hasMoreRecentPackageInBdd(String provider, String packageName, Date datePackage) {
        Optional<Provider> providerBdd = providerRepository.findByProvider(provider);
        if (providerBdd.isPresent()) {
            List<ProviderPackage> packageList = repository.findByPackageNameAndProviderIdtProvider(packageName, providerBdd.get().getIdtProvider());
            Collections.sort(packageList);
            return packageList.get(0).getDateP().after(datePackage);
        }
        return false;
    }
}
