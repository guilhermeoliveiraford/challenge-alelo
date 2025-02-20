package br.com.alelo.consumer.consumerpat.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CardType type;

    private Integer cardNumber;
    private Double balance;

    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;
}
