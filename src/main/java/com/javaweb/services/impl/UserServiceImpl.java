package com.javaweb.services.impl;

import com.javaweb.entity.*;
import com.javaweb.exception.APIException;
import com.javaweb.exception.ResourceNotFoundException;
import com.javaweb.model.dto.*;
import com.javaweb.model.response.InfoUserResponse;
import com.javaweb.model.response.UserResponse;
import com.javaweb.repository.AddressRepository;
import com.javaweb.repository.CartRepository;
import com.javaweb.repository.RoleRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.services.CartService;
import com.javaweb.services.UserService;
import com.javaweb.utils.MessageUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
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
    private MailService mailService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


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
            return modelMapper.map(user,UserDTO.class);
        }).collect(Collectors.toList());

        UserResponse userResponse = modelMapper.map(pageUser, UserResponse.class);

        userResponse.setContent(userDTOList);

        return userResponse;
    }

    @Override
    public String deleteUser(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", String.valueOf(userId)));

        List<CartItemEntity> cartItemEntities = userEntity.getCart().getCartItems();
        Long cartId = userEntity.getCart().getId();

        for (CartItemEntity cartItemEntity : cartItemEntities) {
            Long productId = cartItemEntity.getProduct().getProductId();
            cartService.deleteProductFromCart(cartId,productId);
        }
        userEntity.setStatus("0");
        userRepository.save(userEntity);
        return "User with userId " + userId + " deleted successfully!!!";
    }

    @Override
    public void confirmUser(Long userId, String secretCode) {
        log.info("Confirm");
    }

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByName(username).orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
}

