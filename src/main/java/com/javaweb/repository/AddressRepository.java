package com.javaweb.repository;

import com.javaweb.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
     AddressEntity findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(String country, String state, String city, String pinCode, String street, String buildingName);
}
