package br.com.alelo.consumer.consumerpat.strategy;

import br.com.alelo.consumer.consumerpat.entity.Card;

public class FoodPurchaseStrategy implements PurchaseStrategy {
    @Override
    public double execute(Card card, double value) {
        double discount = value * 0.1; // 10% discount
        double finalValue = value - discount;
        return finalValue;
    }
}
