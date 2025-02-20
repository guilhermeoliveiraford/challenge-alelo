package br.com.alelo.consumer.consumerpat;

import br.com.alelo.consumer.consumerpat.entity.*;
import br.com.alelo.consumer.consumerpat.repository.ConsumerRepository;
import br.com.alelo.consumer.consumerpat.repository.ExtractRepository;
import br.com.alelo.consumer.consumerpat.service.ConsumerService;
import br.com.alelo.consumer.consumerpat.strategy.PurchaseStrategy;
import br.com.alelo.consumer.consumerpat.strategy.PurchaseStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ConsumerServiceTests {

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private ExtractRepository extractRepository;

    @Mock
    private PurchaseStrategyFactory purchaseStrategyFactory;

    @InjectMocks
    private ConsumerService consumerService;

    private Consumer testConsumer;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testConsumer = new Consumer();
        testConsumer.setId(1L);
        testConsumer.setName("Test Consumer");
        testConsumer.setDocumentNumber("0123456");
        testConsumer.setBirthDate(LocalDate.of(1990, 1, 1));

        testCard = new Card();
        testCard.setId(1L);
        testCard.setType(CardType.FOOD);
        testCard.setCardNumber(1234);
        testCard.setBalance(1000.0);
        testCard.setConsumer(testConsumer);

        testConsumer.setCards(Arrays.asList(testCard));
    }

    @Test
    void getAllConsumers_ShouldReturnPageOfConsumers() {
        Page<Consumer> expectedPage = new PageImpl<>(Arrays.asList(testConsumer));
        when(consumerRepository.findAll(any(PageRequest.class))).thenReturn(expectedPage);

        Page<Consumer> result = consumerService.getAllConsumers(0, 10, "id", "asc");

        assertEquals(expectedPage, result);
        verify(consumerRepository).findAll(any(PageRequest.class));
    }

    @Test
    void createConsumer_ShouldSaveAndReturnConsumer() {
        when(consumerRepository.save(any(Consumer.class))).thenReturn(testConsumer);

        Consumer result = consumerService.createConsumer(testConsumer);

        assertNotNull(result);
        assertEquals("Test Consumer", result.getName());
        assertEquals("0123456", result.getDocumentNumber());
        assertEquals(LocalDate.of(1990, 1, 1), result.getBirthDate());
        verify(consumerRepository).save(testConsumer);
    }

    @Test
    void updateConsumer_ShouldUpdateAndReturnConsumer() {
        when(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer));
        when(consumerRepository.save(any(Consumer.class))).thenReturn(testConsumer);

        Consumer updatedConsumer = new Consumer();
        updatedConsumer.setId(1L);
        updatedConsumer.setName("Updated Name");

        Consumer result = consumerService.updateConsumer(updatedConsumer);

        assertEquals("Updated Name", result.getName());
        verify(consumerRepository).findById(1L);
        verify(consumerRepository).save(any(Consumer.class));
    }

    @Test
    void creditCard_ShouldIncreaseCardBalance() {
        when(consumerRepository.findByCardsCardNumber(1234)).thenReturn(Optional.of(testConsumer));

        consumerService.creditCard(1234, 500.0);

        assertEquals(1500.0, testCard.getBalance());
        verify(consumerRepository).findByCardsCardNumber(1234);
        verify(consumerRepository).save(testConsumer);
    }

    @Test
    void makePurchase_ShouldDecreaseFoodCardBalanceAndCreateExtract() {
        // Arrange
        when(consumerRepository.findByCardsCardNumber(1234)).thenReturn(Optional.of(testConsumer));

        PurchaseStrategy mockStrategy = mock(PurchaseStrategy.class);
        when(mockStrategy.execute(any(Card.class), eq(100.0))).thenReturn(90.0); // 10% discount
        when(purchaseStrategyFactory.createStrategy(any(CardType.class))).thenReturn(mockStrategy);

        // Act
        consumerService.makePurchase(1, "Restaurant", 1234, "Lunch", 100.0);

        // Assert
        assertEquals(910.0, testCard.getBalance(), 0.01);
        verify(consumerRepository).findByCardsCardNumber(1234);
        verify(consumerRepository).save(testConsumer);
        verify(extractRepository).save(any(Extract.class));
        verify(purchaseStrategyFactory).createStrategy(any(CardType.class));
        verify(mockStrategy).execute(eq(testCard), eq(100.0));
    }

    @Test
    void makePurchase_ShouldThrowExceptionForInvalidCardNumber() {
        when(consumerRepository.findByCardsCardNumber(9999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                consumerService.makePurchase(1, "Restaurant", 9999, "Lunch", 100.0)
        );
    }
}
