package org.admin.npapplication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.admin.npapplication.dto.*;
import org.admin.npapplication.model.*;
import org.admin.npapplication.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderDto createOrder(User user, CreateOrderRequest request) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (!product.isActive()) {
                throw new IllegalArgumentException("Product " + product.getName() + " is no longer available");
            }
            if (cartItem.getQuantity() > product.getStock()) {
                throw new IllegalArgumentException("Not enough stock for " + product.getName());
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(toJson(request.getShippingAddress()));
        order.setBillingAddress(request.getBillingAddress() != null ? toJson(request.getBillingAddress()) : null);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.addItem(orderItem);

            subtotal = subtotal.add(orderItem.getTotalPrice());

            // Decrease stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setSubtotal(subtotal);
        order.setShippingCost(BigDecimal.ZERO); // Free shipping for now
        order.setDiscountAmount(BigDecimal.ZERO); // Promo codes later
        order.setTotal(subtotal);

        order = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToDto(order);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid address payload", e);
        }
    }

    public Page<OrderDto> getUserOrders(User user, Pageable pageable) {
        return orderRepository.findByUserId(user.getId(), pageable)
                .map(this::mapToDto);
    }

    public OrderDto getOrderById(User user, Long orderId) {
        Order order = orderRepository.findByUserIdAndId(user.getId(), orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return mapToDto(order);
    }

    // Admin methods
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    public OrderDto getOrderByIdAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return mapToDto(order);
    }

    public OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            order.setStatus(newStatus);
            order = orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + request.getStatus());
        }

        return mapToDto(order);
    }

    public OrderStatsDto getOrderStats() {
        BigDecimal paidRevenue = orderRepository.sumTotalByPaymentStatus(PaymentStatus.PAID);
        if (paidRevenue == null) {
            paidRevenue = BigDecimal.ZERO;
        }

        OrderStatsDto stats = new OrderStatsDto();
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));
        stats.setConfirmedOrders(orderRepository.countByStatus(OrderStatus.CONFIRMED));
        stats.setShippedOrders(orderRepository.countByStatus(OrderStatus.SHIPPED));
        stats.setDeliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED));
        stats.setTotalRevenue(paidRevenue);
        stats.setPaidRevenue(paidRevenue);
        return stats;
    }

    private OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .discountAmount(order.getDiscountAmount())
                .total(order.getTotal())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .paymentReference(order.getPaymentReference())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDtos)
                .build();
    }

    private OrderItemDto mapItemToDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .productPrice(item.getProductPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}