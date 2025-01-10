package com.javaweb.services.impl;

import com.javaweb.entity.*;
import com.javaweb.exception.APIException;
import com.javaweb.exception.InvalidDataException;
import com.javaweb.exception.ResourceNotFoundException;
import com.javaweb.exception.UserNotFoundException;
import com.javaweb.model.dto.*;
import com.javaweb.model.response.InfoUserResponse;
import com.javaweb.model.response.StatusResponse;
import com.javaweb.model.response.TokenResponse;
import com.javaweb.repository.AddressRepository;
import com.javaweb.repository.RoleRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.services.AuthenticationService;
import com.javaweb.services.RedisTokenService;
import com.javaweb.services.TokenService;
import com.javaweb.utils.JWTTokenUtils;
import com.javaweb.utils.MessageUtils;
import com.javaweb.utils.TokenType;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private JWTTokenUtils jwtTokenUtils;
//    @Autowired
//    private TokenService tokenService;
    @Autowired
    private RedisTokenService redisTokenService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public InfoUserResponse getUserById(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", String.valueOf(userId)));

        if (userEntity.getStatus().equals("0")){
            throw new APIException("User is not active!");
        }
        return InfoUserResponse.builder()
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .phoneNumber(String.valueOf(userEntity.getPhoneNumber()))
                .build();
    }

    @Override
    public TokenResponse authenticate(LoginCredentials request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getName(), request.getPassword()));

        UserEntity userEntity = userRepository.findByName(request.getName()).orElseThrow(()-> new UserNotFoundException("Username or Password is incorrect"));

        String accessToken = jwtTokenUtils.generateToken(userEntity,userEntity.getId());
        String refreshToken = jwtTokenUtils.generateRefreshToken(userEntity);
        // save to database
//        tokenService.saveToken(TokenEntity.builder()
//                        .name(userEntity.getName())
//                        .accessToken(accessToken)
//                        .refreshToken(refreshToken)
//                .build());
        //save to redis
        redisTokenService.save(RedisToken.builder()
                        .id(userEntity.getName())
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                .build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userEntity.getId())
                .build();
    }

    @Override
    public TokenResponse refresh(HttpServletRequest request) {
        String refreshToken = request.getHeader("referer");
        if (refreshToken == null) {
            throw new InvalidDataException("Token must be not blank!");
        }

        final String name = jwtTokenUtils.extractUser(refreshToken, TokenType.REFRESH_TOKEN);

        Optional<UserEntity> userEntity = userRepository.findByName(name);

        if (!jwtTokenUtils.validateToken(refreshToken,TokenType.REFRESH_TOKEN, userEntity.get())){
            throw new InvalidDataException("Token is invalid");
        }

        String accessToken = jwtTokenUtils.generateToken(userEntity.get(),userEntity.get().getId());

        // save to database
//        tokenService.saveToken(TokenEntity.builder()
//                        .name(userEntity.get().getName())
//                        .accessToken(accessToken)
//                        .refreshToken(refreshToken)
//                .build());
        //save to redis
        redisTokenService.save(RedisToken.builder()
                .id(userEntity.get().getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userEntity.get().getId())
                .build();
    }

    @Override
    public String logout(HttpServletRequest request) {
        String refreshToken = request.getHeader("referer");
        if (refreshToken == null) {
            throw new InvalidDataException("Token must be not blank!");
        }
        final String name = jwtTokenUtils.extractUser(refreshToken, TokenType.ACCESS_TOKEN);

        RedisToken redisToken = redisTokenService.getById(name);
        redisTokenService.delete(redisToken.getId());

        return "Logout successfully";
    }

    @Override
    public StatusResponse forgotPassword(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!userEntity.isEnabled() || userEntity.getStatus().equals("0")){
            throw new InvalidDataException("User is disabled");
        }

        String resetToken = jwtTokenUtils.generateResetToken(userEntity);

        kafkaTemplate.send("confirm-forgot-password-topic", String.format("email=%s,id=%s,code=%s", email, userEntity.getId(),resetToken));

        return StatusResponse.builder()
                .message("Check your email")
                .status(HttpStatus.OK.value())
                .build();
    }

    @Override
    public StatusResponse resetPassword(String secretKey) {
        UserEntity userEntity = isValidUserByToken(secretKey);
        userEntity.setPassword(passwordEncoder.encode(MessageUtils.DEFAULT_PASSWORD));
        userRepository.save(userEntity);
        return StatusResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Reset successfully")
                .build();
    }

    @Override
    public StatusResponse changePassword(ResetPasswordDTO resetPasswordDTO) {
        UserEntity userEntity = isValidUserByToken(resetPasswordDTO.getSecretKey());
        if (!resetPasswordDTO.getPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new InvalidDataException("Passwords do not match");
        }
        userEntity.setPassword(passwordEncoder.encode(resetPasswordDTO.getPassword()));
        userRepository.save(userEntity);

        return StatusResponse.builder()
                .message("Change password success!")
                .status(HttpStatus.OK.value())
                .build();
    }

    private UserEntity isValidUserByToken(String token) {
        final String name = jwtTokenUtils.extractUser(token,TokenType.RESET_TOKEN);
        UserEntity userEntity = userRepository.findByName(name).orElseThrow(()-> new UserNotFoundException("Username or Password is incorrect"));
        if (!userEntity.isEnabled() || userEntity.getStatus().equals("0")){
            throw new InvalidDataException("User is disabled");
        }
        if (!jwtTokenUtils.validateToken(token,TokenType.RESET_TOKEN, userEntity)){
            throw new InvalidDataException("Token is invalid");
        }
        return userEntity;
    }
    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", String.valueOf(userId)));

        String encodedPass = passwordEncoder.encode(userDTO.getPassword());
        userEntity.setName(userDTO.getName());
        userEntity.setPhoneNumber(userDTO.getPhoneNumber());
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
    public UserDTO registerUser(UserDTO userDTO) throws MessagingException, UnsupportedEncodingException {
        try {
            UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);

            CartEntity cartEntity = new CartEntity();
            userEntity.setCart(cartEntity);
            String encodedPass = passwordEncoder.encode(userDTO.getPassword());
            userEntity.setPassword(encodedPass);

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
            userEntity.setAddresses(List.of(address));

            RoleEntity role = roleRepository.findById(Math.toIntExact(MessageUtils.USER_ID)).get();
            userEntity.getRoles().add(role);
            userEntity.setStatus("1");
            UserEntity registeredUser = new UserEntity();
            try{
                registeredUser = userRepository.save(userEntity);
            } catch (Exception e){
                log.info(e.getMessage());
            }

            if (registeredUser.getId() != null) {
                //mailService.sendConfirmLink(registeredUser.getEmail(),registeredUser.getId(),"secretCode");
                kafkaTemplate.send("confirm-account-topic", String.format("email=%s,id=%s,code=%s", registeredUser.getEmail(), registeredUser.getId(), "code@123"));
            }

            cartEntity.setUser(userEntity);

            userDTO = modelMapper.map(userEntity, UserDTO.class);
            userDTO.setAddress(modelMapper.map(userEntity.getAddresses().stream().findFirst().get(), AddressDTO.class));

            return userDTO;

        } catch (DataIntegrityViolationException e) {
            throw new APIException("User already exists with emailId: " + userDTO.getEmail());
        }

    }
}
