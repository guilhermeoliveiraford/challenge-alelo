package br.com.alelo.consumer.consumerpat.entity;

import lombok.Data;
import javax.persistence.Embeddable;

@Data
@Embeddable
public class Address {
    private String street;
    private Integer number;
    private String city;
    private String country;
    private Integer postalCode;
}
