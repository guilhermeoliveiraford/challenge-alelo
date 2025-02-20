package br.com.alelo.consumer.consumerpat.entity;

import lombok.Data;
import javax.persistence.Embeddable;

@Data
@Embeddable
public class Contact {
    private Integer mobilePhoneNumber;
    private Integer residencePhoneNumber;
    private Integer phoneNumber;
    private String email;
}
