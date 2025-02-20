package br.com.alelo.consumer.consumerpat.service;

import br.com.alelo.consumer.consumerpat.entity.*;
import br.com.alelo.consumer.consumerpat.repository.ConsumerRepository;
import br.com.alelo.consumer.consumerpat.repository.ExtractRepository;
import br.com.alelo.consumer.consumerpat.strategy.PurchaseStrategy;
import br.com.alelo.consumer.consumerpat.strategy.PurchaseStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final ExtractRepository extractRepository;
    private final PurchaseStrategyFactory purchaseStrategyFactory;

    @Autowired
    public ConsumerService(ConsumerRepository consumerRepository,
                           ExtractRepository extractRepository,
                           PurchaseStrategyFactory purchaseStrategyFactory) {
        this.consumerRepository = consumerRepository;
        this.extractRepository = extractRepository;
        this.purchaseStrategyFactory = purchaseStrategyFactory;
    }

    public Page<Consumer> getAllConsumers(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return consumerRepository.findAll(pageable);
    }

    @Transactional
    public Consumer createConsumer(Consumer consumer) {
        return consumerRepository.save(consumer);
    }

    @Transactional
    public Consumer updateConsumer(Consumer consumer) {
        Consumer existingConsumer = consumerRepository.findById(consumer.getId())
                .orElseThrow(() -> new RuntimeException("Consumer not found"));

        existingConsumer.setName(consumer.getName());
        existingConsumer.setDocumentNumber(consumer.getDocumentNumber());
        existingConsumer.setBirthDate(consumer.getBirthDate());
        existingConsumer.setContact(consumer.getContact());
        existingConsumer.setAddress(consumer.getAddress());

        return consumerRepository.save(existingConsumer);
    }

    @Transactional
    public void creditCard(int cardNumber, double value) {
        Consumer consumer = consumerRepository.findByCardsCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Consumer not found for the given card number"));

        Card card = consumer.getCards().stream()
                .filter(c -> c.getCardNumber().equals(cardNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setBalance(card.getBalance() + value);
        consumerRepository.save(consumer);
    }

    @Transactional
    public void makePurchase(int establishmentType, String establishmentName, int cardNumber, String productDescription, double value) {
        Consumer consumer = consumerRepository.findByCardsCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Consumer not found for the given card number"));

        CardType cardType = getCardTypeFromEstablishmentType(establishmentType);

        Card card = consumer.getCards().stream()
                .filter(c -> c.getType() == cardType && c.getCardNumber().equals(cardNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Card not found for the given type and number"));

        PurchaseStrategy strategy = purchaseStrategyFactory.createStrategy(cardType);
        double finalValue = strategy.execute(card, value);

        // Atualiza o saldo do cartão
        card.setBalance(card.getBalance() - finalValue);

        consumerRepository.save(consumer);

        Extract extract = new Extract();
        extract.setEstablishmentName(establishmentName);
        extract.setProductDescription(productDescription);
        extract.setDateBuy(LocalDateTime.now());
        extract.setCardNumber(cardNumber);
        extract.setAmount(finalValue);
        extract.setCardType(cardType);
        extract.setConsumer(consumer);

        extractRepository.save(extract);
    }

    private CardType getCardTypeFromEstablishmentType(int establishmentType) {
        switch (establishmentType) {
            case 1:
                return CardType.FOOD;
            case 2:
                return CardType.DRUGSTORE;
            case 3:
                return CardType.FUEL;
            default:
                throw new IllegalArgumentException("Invalid establishment type: " + establishmentType);
        }
    }
}
