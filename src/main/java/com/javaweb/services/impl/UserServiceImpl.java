package com.javaweb.services.impl;

import com.javaweb.entity.*;
import com.javaweb.exception.APIException;
import com.javaweb.exception.ResourceNotFoundException;
import com.javaweb.model.dto.*;
import com.javaweb.model.response.UserResponse;
import com.javaweb.repository.AddressRepository;
import com.javaweb.repository.CartRepository;
import com.javaweb.repository.RoleRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.services.CartService;
import com.javaweb.services.UserService;
import com.javaweb.utils.MessageUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<UserEntity> pageUser = userRepository.findAll(pageable);
        List<UserEntity> listUser = pageUser.getContent();

        if (listUser.isEmpty()) {
            throw new APIException("No User exists!");
        }

        List<UserDTO> userDTOList = listUser.stream().map(user ->{
            UserDTO userDTO = new UserDTO();
            if (user.getStatus().equals("1")){
                userDTO = modelMapper.map(user, UserDTO.class);
                if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
                    userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
                }

                CartDTO cartDTO = modelMapper.map(user.getCart(), CartDTO.class);

                List<ProductDTO> products = user.getCart().getCartItems().stream().map(
                        product -> modelMapper.map(product.getProduct(), ProductDTO.class)
                ).collect(Collectors.toList());

                userDTO.setCart(cartDTO);
                userDTO.getCart().setProducts(products);
            }
            return userDTO;
        }).collect(Collectors.toList());

        UserResponse userResponse = modelMapper.map(pageUser, UserResponse.class);

        userResponse.setContent(userDTOList);

        return userResponse;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", String.valueOf(userId)));

        if (userEntity.getStatus().equals("0")){
            throw new APIException("User is not active!");
        }
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);

        userDTO.setAddress(modelMapper.map(userEntity.getAddresses().stream().findFirst().get(), AddressDTO.class));

        CartDTO cartDTO = modelMapper.map(userEntity.getCart(), CartDTO.class);

        List<ProductDTO> products = userEntity.getCart().getCartItems().stream()
                .map(user -> modelMapper.map(user.getProduct(), ProductDTO.class)).collect(Collectors.toList());

        userDTO.setCart(cartDTO);

        userDTO.getCart().setProducts(products);

        return userDTO;
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", String.valueOf(userId)));

        String encodedPass = passwordEncoder.encode(userDTO.getPassword());
        userEntity.setName(userDTO.getName());
        userEntity.setPhoneNumber(userDTO.getMobileNumber());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPassword(encodedPass);

        if (userDTO.getAddress() != null) {
            String country = userDTO.getAddress().getCountry();
            String state = userDTO.getAddress().getState();
            String city = userDTO.getAddress().getCity();
            String pinCode = userDTO.getAddress().getPinCode();
            String street = userDTO.getAddress().getStreet();
            String buildingName = userDTO.getAddress().getBuildingName();

            AddressEntity addressEntity = addressRepository.findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(country, state,
                    city, pinCode, street, buildingName);

            if (addressEntity == null) {
                addressEntity = new AddressEntity(country, state, city, pinCode, street, buildingName);

                addressRepository.save(addressEntity);

                userEntity.setAddresses(Arrays.asList(addressEntity));
            }
        }
        userRepository.save(userEntity);

        userDTO = modelMapper.map(userEntity, UserDTO.class);

        userDTO.setAddress(modelMapper.map(userEntity.getAddresses().stream().findFirst().get(), AddressDTO.class));

        CartDTO cart = modelMapper.map(userEntity.getCart(), CartDTO.class);

        List<ProductDTO> products = userEntity.getCart().getCartItems().stream()
                .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).collect(Collectors.toList());

        userDTO.setCart(cart);

        userDTO.getCart().setProducts(products);

        return userDTO;
    }

    @Override
    public String deleteUser(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", String.valueOf(userId)));

        List<CartItemEntity> cartItemEntities = userEntity.getCart().getCartItems();
        Long cartId = userEntity.getCart().getId();

        for (CartItemEntity cartItemEntity : cartItemEntities) {
            Long productId = cartItemEntity.getProduct().getId();
            cartService.deleteProductFromCart(cartId,productId);
        }
        userEntity.setStatus("0");

        return "User with userId " + userId + " deleted successfully!!!";
    }

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        try {
            UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);

            CartEntity cartEntity = new CartEntity();
            userEntity.setCart(cartEntity);
            String encodedPass = passwordEncoder.encode(userDTO.getPassword());
            userEntity.setPassword(encodedPass);

            RoleEntity role = roleRepository.findById(Math.toIntExact(MessageUtils.USER_ID)).get();
            userEntity.getRoles().add(role);

            String country = userDTO.getAddress().getCountry();
            String state = userDTO.getAddress().getState();
            String city = userDTO.getAddress().getCity();
            String pinCode = userDTO.getAddress().getPinCode();
            String street = userDTO.getAddress().getStreet();
            String buildingName = userDTO.getAddress().getBuildingName();

            AddressEntity address = addressRepository.findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(country, state,
                    city, pinCode, street, buildingName);

            if (address == null) {
                address = new AddressEntity(country, state, city, pinCode, street, buildingName);

                addressRepository.save(address);
            }

            userEntity.setAddresses(Arrays.asList(address));
            UserEntity registeredUser = userRepository.save(userEntity);

            cartEntity.setUser(registeredUser);

            userDTO = modelMapper.map(registeredUser, UserDTO.class);

            userDTO.setAddress(modelMapper.map(userEntity.getAddresses().stream().findFirst().get(), AddressDTO.class));

            return userDTO;

        } catch (DataIntegrityViolationException e) {
            throw new APIException("User already exists with emailId: " + userDTO.getEmail());
        }

    }
}

