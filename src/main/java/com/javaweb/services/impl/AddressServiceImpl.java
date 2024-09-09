package com.javaweb.services.impl;

import com.javaweb.entity.AddressEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.exception.APIException;
import com.javaweb.exception.ResourceNotFoundException;
import com.javaweb.model.dto.AddressDTO;
import com.javaweb.repository.AddressRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.services.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        String country = addressDTO.getCountry();
        String state = addressDTO.getState();
        String city = addressDTO.getCity();
        String pinCode = addressDTO.getPinCode();
        String street = addressDTO.getStreet();
        String buildingName = addressDTO.getBuildingName();

        AddressEntity addressEntity = addressRepository.findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(country,
                state, city, pinCode, street, buildingName);

        if (addressEntity != null) {
            throw new APIException("Address already exists with addressId: " + addressEntity.getId());
        }
        AddressEntity newAddress = modelMapper.map(addressDTO, AddressEntity.class);
        newAddress.setStatus("1");
        addressRepository.save(newAddress);
        return modelMapper.map(newAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<AddressEntity> addressEntities = addressRepository.findAll();

        List<AddressDTO> addressDTOS = addressEntities.stream().
                map(address -> modelMapper.map(address, AddressDTO.class)).collect(Collectors.toList());

        return addressDTOS;
    }

    @Override
    public AddressDTO getAddress(Long addressId) {
        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", String.valueOf(addressId)));

        if (address.getStatus().equals("0")){
            throw new APIException("Address is not active.");
        }
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO address) {
        AddressEntity addressEntity = addressRepository.findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(
                address.getCountry(), address.getState(), address.getCity(), address.getPinCode(), address.getStreet(),
                address.getBuildingName()
        );

        if (addressEntity == null) {
            AddressEntity newAddress = modelMapper.map(address, AddressEntity.class);
            addressRepository.save(newAddress);
            return modelMapper.map(newAddress, AddressDTO.class);
        } else {
            List<UserEntity> userEntities = userRepository.findByAddress(addressId);

            for (UserEntity userEntity : userEntities) {
                if (!userEntity.getAddresses().contains(addressEntity)) {
                    userEntity.getAddresses().add(addressEntity);
                }
            }
            userRepository.saveAll(userEntities);
            return modelMapper.map(addressEntity, AddressDTO.class);
        }
    }

    @Override
    public String deleteAddress(Long addressId) {
        AddressEntity addressEntity = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", String.valueOf(addressId)));

        List<UserEntity> userEntities = userRepository.findByAddress(addressId);

        for (UserEntity userEntity : userEntities) {
            userEntity.getAddresses().remove(addressEntity);
            userRepository.save(userEntity);
        }

        addressEntity.setStatus("0");

        return "Address deleted successfully with addressId: " + addressId;
    }
}
