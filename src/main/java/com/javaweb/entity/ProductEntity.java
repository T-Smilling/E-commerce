package com.javaweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class ProductEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @NotBlank
    @Size(min = 3, message = "Product name must contain atleast 3 characters")
    @Column(name = "product_name")
    private String productName;

    @Column(name = "image")
    private String image;

    @NotBlank
    @Size(min = 6, message = "Product description must contain atleast 6 characters")
    @Column(name = "description")
    private String description;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private double price;
    @Column(name = "discount")
    private double discount;
    @Column(name = "special_price")
    private double specialPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @OneToMany(mappedBy = "product", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private List<CartItemEntity> products = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<OrderItemEntity> orderItems = new ArrayList<>();
}
