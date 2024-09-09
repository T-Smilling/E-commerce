package com.javaweb.services;

import com.javaweb.model.dto.AddressDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(@Valid AddressDTO addressDTO);

    List<AddressDTO> getAddresses();

    AddressDTO getAddress(Long addressId);

    AddressDTO updateAddress(Long addressId, AddressDTO address);

    String deleteAddress(Long addressId);
}
