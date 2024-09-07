package com.paulo.ecommerceX.config;

import com.paulo.ecommerceX.domain.Product;
import com.paulo.ecommerceX.domain.ProductSale;
import com.paulo.ecommerceX.domain.Sale;
import com.paulo.ecommerceX.domain.User;
import com.paulo.ecommerceX.domain.enums.SaleStatus;
import com.paulo.ecommerceX.domain.enums.UserRole;
import com.paulo.ecommerceX.repositories.ProductRepository;
import com.paulo.ecommerceX.repositories.SaleRepository;
import com.paulo.ecommerceX.repositories.UserRepository;
import com.paulo.ecommerceX.services.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createDefaultUser() {
        return args -> {

            String username = "admin";
            String password = "admin123";

            if (userRepository.findByLogin(username).isEmpty()) {
                var user = new User(username, passwordEncoder.encode(password), UserRole.ADMIN);
                userRepository.save(user);
            }

            User user = userRepository.findById(UUID.fromString("1c6245e9-f066-4f35-8824-081b11e6dc77"))
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Product product1 = productRepository.findById(UUID.fromString("a5aca643-ecf2-4ab0-99ff-11c7d84a09ca"))
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

            Product product2 = productRepository.findById(UUID.fromString("a8d288d3-07dd-4e36-971a-3c1a8bc24bd3"))
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

            ProductSale p1Sale = new ProductSale(product1, 15);
            ProductSale p2Sale = new ProductSale(product2, 15);

            Set<ProductSale> productSales = new HashSet<>();
            productSales.add(p1Sale);
            productSales.add(p2Sale);


            LocalDateTime now = LocalDateTime.now();
            LocalDateTime saleDate = now.minus(Period.of(0, 1, 5));
            Instant date = saleDate.toInstant(ZoneOffset.UTC);

            if (saleRepository.findById(UUID.fromString("3e854472-a0fc-4c58-95a7-a8c2a7326aad")).isEmpty()) {
                Sale sale = new Sale(date, user, productSales, SaleStatus.COMPLETED);

                p1Sale.getId().setSale(sale);
                p2Sale.getId().setSale(sale);

                saleRepository.save(sale);
            }
        };
    }
}
