package com.javaweb.controllers;

import com.javaweb.model.dto.OrderDTO;
import com.javaweb.model.response.OrderResponse;
import com.javaweb.services.OrderService;
import com.javaweb.utils.MessageUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/${api.prefix}")
@SecurityRequirement(name = "E-Commerce")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/users/{email}/carts/{cartId}/payments/{paymentMethod}/order")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String email,
                                                  @PathVariable Long cartId,
                                                  @PathVariable String paymentMethod) {
        OrderDTO order = orderService.placeOrder(email, cartId, paymentMethod);

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(
            @RequestParam(name = "pageNumber", defaultValue = MessageUtils.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = MessageUtils.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = MessageUtils.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = MessageUtils.SORT_DIR, required = false) String sortOrder) {

        OrderResponse orderResponse = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);

        return new ResponseEntity<>(orderResponse, HttpStatus.FOUND);
    }

    @GetMapping("/users/{email}/orders")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable String email) {
        List<OrderDTO> orders = orderService.getOrdersByUser(email);

        return new ResponseEntity<>(orders, HttpStatus.FOUND);
    }

    @GetMapping("/users/{email}/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrderByUserAndOrderId(@PathVariable String email, @PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderByUserAndOrderId(email, orderId);

        return new ResponseEntity<>(order, HttpStatus.FOUND);
    }

    @PutMapping("admin/users/{email}/orders/{orderId}/orderStatus/{orderStatus}")
    public ResponseEntity<OrderDTO> updateOrderByUser(@PathVariable String email,
                                                      @PathVariable Long orderId,
                                                      @PathVariable String orderStatus) {
        OrderDTO order = orderService.updateOrder(email, orderId, orderStatus);

        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
