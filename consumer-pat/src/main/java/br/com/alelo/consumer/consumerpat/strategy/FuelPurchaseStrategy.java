package br.com.alelo.consumer.consumerpat.strategy;

import br.com.alelo.consumer.consumerpat.entity.Card;

public class FuelPurchaseStrategy implements PurchaseStrategy {
    @Override
    public double execute(Card card, double value) {
        double tax = (value / 100) * 35;
        double finalValue = value + tax;
        card.setBalance(card.getBalance() - finalValue);
        return finalValue;
    }
}
