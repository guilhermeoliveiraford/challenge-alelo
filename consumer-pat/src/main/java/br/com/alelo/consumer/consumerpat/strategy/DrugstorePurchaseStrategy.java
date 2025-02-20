package br.com.alelo.consumer.consumerpat.strategy;

import br.com.alelo.consumer.consumerpat.entity.Card;

public class DrugstorePurchaseStrategy implements PurchaseStrategy {
    @Override
    public double execute(Card card, double value) {
        card.setBalance(card.getBalance() - value);
        return value;
    }
}
