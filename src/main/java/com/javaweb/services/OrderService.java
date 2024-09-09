package com.javaweb.services;

import com.javaweb.model.dto.OrderDTO;
import com.javaweb.model.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderDTO placeOrder(String email, Long cartId, String paymentMethod);

    OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    List<OrderDTO> getOrdersByUser(String email);

    OrderDTO getOrderByUserAndOrderId(String email, Long orderId);

    OrderDTO updateOrder(String email, Long orderId, String orderStatus);
}
