package fr.abes.kafkaconvergence.service;

import fr.abes.kafkaconvergence.entity.LigneKbart;
import fr.abes.kafkaconvergence.repository.LigneKbartDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LigneKbartService {
    private final LigneKbartDao dao;

    public LigneKbart save(LigneKbart ligneKbart) {
        return dao.save(ligneKbart);
    }

}
