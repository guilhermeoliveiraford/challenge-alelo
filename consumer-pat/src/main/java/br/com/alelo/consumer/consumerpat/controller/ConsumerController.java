package br.com.alelo.consumer.consumerpat.controller;

import br.com.alelo.consumer.consumerpat.entity.Consumer;
import br.com.alelo.consumer.consumerpat.service.ConsumerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ConsumerService consumerService;

    /* Listar todos os clientes (obs.: tabela possui cerca de 50.000 registros) */
    @GetMapping("/consumerList")
    public ResponseEntity<Page<Consumer>> listAllConsumers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        log.info("Obtendo clientes - página: {}, tamanho: {}, ordenado por: {}, direção: {}", page, size, sortBy, sortDirection);
        Page<Consumer> consumers = consumerService.getAllConsumers(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(consumers);
    }

    /* Cadastrar novos clientes */
    @PostMapping("/createConsumer")
    public ResponseEntity<Consumer> createConsumer(@RequestBody Consumer consumer) {
        Consumer createdConsumer = consumerService.createConsumer(consumer);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdConsumer);
    }

    // Atualizar cliente, lembrando que não deve ser possível alterar o saldo do cartão
    @PostMapping("/updateConsumer")
    public ResponseEntity<Consumer> updateConsumer(@RequestBody Consumer consumer) {
        Consumer updatedConsumer = consumerService.updateConsumer(consumer);
        return ResponseEntity.ok(updatedConsumer);
    }

    /*
     * Credito de valor no cartão
     *
     * cardNumber: número do cartão
     * value: valor a ser creditado (adicionado ao saldo)
     */
    @PostMapping("/setcardbalance")
    public ResponseEntity<Void> setBalance(@RequestParam int cardNumber, @RequestParam double value) {
        consumerService.creditCard(cardNumber, value);
        return ResponseEntity.ok().build();
    }

    /*
     * Débito de valor no cartão (compra)
     *
     * establishmentType: tipo do estabelecimento comercial
     * establishmentName: nome do estabelecimento comercial
     * cardNumber: número do cartão
     * productDescription: descrição do produto
     * value: valor a ser debitado (subtraído)
     */
    @PostMapping("/buy")
    public ResponseEntity<Void> buy(
            @RequestParam int establishmentType,
            @RequestParam String establishmentName,
            @RequestParam int cardNumber,
            @RequestParam String productDescription,
            @RequestParam double value) {
        consumerService.makePurchase(establishmentType, establishmentName, cardNumber, productDescription, value);
        return ResponseEntity.ok().build();
    }
}
