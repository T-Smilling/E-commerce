package com.javaweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddressEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    public AddressEntity(String country, String state, String city, String pinCode, String street, String buildingName) {
        this.country = country;
        this.state = state;
        this.city = city;
        this.pinCode = pinCode;
        this.street = street;
        this.buildingName = buildingName;
    }

    @NotBlank
    @Size(min = 5, message = "Street name must contain at least 5 characters")
    @Column(name = "street")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must contain at least 5 characters")
    @Column(name = "building_name")
    private String buildingName;

    @NotBlank
    @Size(min = 4, message = "City name must contain at least 4 characters")
    @Column(name = "city")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State name must contain at least 2 characters")
    @Column(name = "state")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must contain at least 2 characters")
    @Column(name = "country")
    private String country;

    @NotBlank
    @Size(min = 6, message = "Pincode must contain at least 6 characters")
    @Column(name = "pin_code")
    private String pinCode;

    @Column(name = "status")
    private String status;

}
