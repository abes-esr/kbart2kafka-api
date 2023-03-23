package fr.abes.kafkaconvergence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ERREUR_RESULT")
@Getter
@Setter
@NoArgsConstructor
public class ErreurResult {
    @Id
    @Column(name = "ID_ERROR")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ERROR_SEQ")
    @SequenceGenerator(name = "ERROR_SEQ", sequenceName = "ERROR_SEQ")
    private Integer id;

    @ElementCollection
    @Column(name = "PPNS")
    private List<String> ppns = new ArrayList<>();

    @Column(name = "LIGNE_KBART", length = 4000)
    private String ligneKbart;

    @ElementCollection
    @Column(name = "messages")
    private List<String> messages = new ArrayList<>();
}
