package br.com.alelo.consumer.consumerpat.strategy;
import org.springframework.stereotype.Component;
import br.com.alelo.consumer.consumerpat.entity.CardType;

@Component
public class PurchaseStrategyFactory {
    public PurchaseStrategy createStrategy(CardType cardType) {
        switch (cardType) {
            case FOOD:
                return new FoodPurchaseStrategy();
            case DRUGSTORE:
                return new DrugstorePurchaseStrategy();
            case FUEL:
                return new FuelPurchaseStrategy();
            default:
                throw new IllegalArgumentException("Invalid card type: " + cardType);
        }
    }
}
