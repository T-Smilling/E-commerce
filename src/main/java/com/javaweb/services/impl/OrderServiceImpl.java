package com.javaweb.services.impl;

import com.javaweb.entity.*;
import com.javaweb.exception.APIException;
import com.javaweb.exception.ResourceNotFoundException;
import com.javaweb.model.dto.OrderDTO;
import com.javaweb.model.dto.OrderItemDTO;
import com.javaweb.model.response.OrderResponse;
import com.javaweb.repository.CartRepository;
import com.javaweb.repository.OrderItemRepository;
import com.javaweb.repository.OrderRepository;
import com.javaweb.repository.PaymentRepository;
import com.javaweb.services.CartService;
import com.javaweb.services.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartService cartService;

    @Override
    public OrderDTO placeOrder(String email, Long cartId, String paymentMethod) {
        CartEntity cartEntity = cartRepository.findByEmailAndId(email,cartId);

        if (cartEntity == null) {
            throw new ResourceNotFoundException("Cart", "cartId", String.valueOf(cartId));
        }

        OrderEntity order = new OrderEntity();

        order.setEmail(email);
        order.setOrderDate(LocalDate.now());

        order.setTotalAmount(cartEntity.getTotalPrice());
        order.setOrderStatus("Order Accepted !");

        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);

        paymentRepository.save(payment);

        order.setPayment(payment);

        orderRepository.save(order);

        List<CartItemEntity> cartItems = cartEntity.getCartItems();

        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (CartItemEntity cartItem : cartItems) {
            OrderItemEntity orderItem = new OrderItemEntity();

            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(order);

            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        cartEntity.getCartItems().forEach(item -> {
            int quantity = item.getQuantity();

            ProductEntity product = item.getProduct();

            cartService.deleteProductFromCart(cartId, item.getProduct().getProductId());

            product.setQuantity(product.getQuantity() - quantity);
        });

        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));

        return orderDTO;
    }

    @Override
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<OrderEntity> pageOrder = orderRepository.findAll(pageable);
        List<OrderEntity> orderList = pageOrder.getContent();

        if (orderList.isEmpty()) {
            throw new APIException("No orders placed yet by the users");
        }

        List<OrderDTO> orderDTOList = orderList.stream().map(order -> modelMapper.map(order,OrderDTO.class)).collect(Collectors.toList());
        OrderResponse orderResponse = modelMapper.map(pageOrder, OrderResponse.class);

        orderResponse.setContent(orderDTOList);

        return orderResponse;
    }

    @Override
    public List<OrderDTO> getOrdersByUser(String email) {
        List<OrderEntity> orders = orderRepository.findAllByEmail(email);

        List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());

        if (orderDTOs.isEmpty()) {
            throw new APIException("No orders placed yet by the user with email: " + email);
        }

        return orderDTOs;
    }

    @Override
    public OrderDTO getOrderByUserAndOrderId(String email, Long orderId) {
        OrderEntity order = orderRepository.findByEmailAndId(email, orderId);

        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", String.valueOf(orderId));
        }

        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderDTO updateOrder(String email, Long orderId, String orderStatus) {
        OrderEntity order = orderRepository.findByEmailAndId(email, orderId);

        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", email);
        }

        order.setOrderStatus(orderStatus);

        return modelMapper.map(order, OrderDTO.class);
    }
}
