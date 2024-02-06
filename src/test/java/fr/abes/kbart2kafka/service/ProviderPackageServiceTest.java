package fr.abes.kbart2kafka.service;

import fr.abes.kbart2kafka.entity.Provider;
import fr.abes.kbart2kafka.entity.ProviderPackage;
import fr.abes.kbart2kafka.repository.ProviderPackageRepository;
import fr.abes.kbart2kafka.repository.ProviderRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest(classes = {ProviderPackageService.class})
class ProviderPackageServiceTest {
    @Autowired
    ProviderPackageService service;

    @MockBean
    ProviderPackageRepository providerPackageRepository;

    @MockBean
    ProviderRepository providerRepository;

    @Test
    @DisplayName("test isMoreRecentPackage : cas général")
    void isMoreRecentPackageInBdd() {
        Provider provider = new Provider(1, "provider", "nomContact", "prenomContact", "mail@contact.com", "displayName");
        Mockito.when(providerRepository.findByProvider("provider")).thenReturn(Optional.of(provider));
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, 10, 1);
        Date date1 = calendar.getTime();
        ProviderPackage package1 = new ProviderPackage(1, "package", date1, 1, 'Y');
        calendar.set(2023, 8, 10);
        Date date2 = calendar.getTime();
        ProviderPackage package2 = new ProviderPackage(2, "package", date2, 1, 'Y');
        calendar.set(2023, 7, 5);
        Date date3 = calendar.getTime();
        ProviderPackage package3 = new ProviderPackage(3, "package", date3, 1, 'Y');

        List packageList = Lists.newArrayList(package1, package2, package3);
        Mockito.when(providerPackageRepository.findByPackageNameAndProviderIdtProvider("package", 1)).thenReturn(packageList);

        calendar.set(2023, 4, 20);
        Date inputDate = calendar.getTime();
        Assertions.assertTrue(service.hasMoreRecentPackageInBdd("provider", "package", inputDate));


        calendar.set(2023, 11, 20);
        inputDate = calendar.getTime();
        Assertions.assertFalse(service.hasMoreRecentPackageInBdd("provider", "package", inputDate));

        //cas date égale
        calendar.set(2023, 8, 10);
        inputDate = calendar.getTime();
        Assertions.assertFalse(service.hasMoreRecentPackageInBdd("provider", "package", inputDate));
    }

    @Test
    @DisplayName("test isMoreRecentPacakge : cas pas de provider trouvé")
    void isMoreRecentPacakageInBddNoProvider() {
        Mockito.when(providerRepository.findByProvider("provider")).thenReturn(Optional.empty());
        Assertions.assertFalse(service.hasMoreRecentPackageInBdd("provider", "package", Calendar.getInstance().getTime()));
    }
}