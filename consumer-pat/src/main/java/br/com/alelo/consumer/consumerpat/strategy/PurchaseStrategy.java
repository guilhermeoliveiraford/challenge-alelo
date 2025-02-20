package br.com.alelo.consumer.consumerpat.strategy;

import br.com.alelo.consumer.consumerpat.entity.Card;

public interface PurchaseStrategy {
    double execute(Card card, double value);
}
