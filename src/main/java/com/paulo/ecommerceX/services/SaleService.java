package com.paulo.ecommerceX.services;

import com.paulo.ecommerceX.domain.Product;
import com.paulo.ecommerceX.domain.ProductSale;
import com.paulo.ecommerceX.domain.Sale;
import com.paulo.ecommerceX.domain.User;
import com.paulo.ecommerceX.domain.dto.productSale.ProductSaleRequestDTO;
import com.paulo.ecommerceX.domain.dto.sale.SaleRequestDTO;
import com.paulo.ecommerceX.domain.dto.sale.SaleResponseDTO;
import com.paulo.ecommerceX.domain.enums.ProductStatus;
import com.paulo.ecommerceX.domain.enums.SaleStatus;
import com.paulo.ecommerceX.repositories.ProductRepository;
import com.paulo.ecommerceX.repositories.SaleRepository;
import com.paulo.ecommerceX.repositories.UserRepository;
import com.paulo.ecommerceX.services.exceptions.InsufficientStockException;
import com.paulo.ecommerceX.services.exceptions.ResourceNotFoundException;
import com.paulo.ecommerceX.services.exceptions.SaleCancellationException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<SaleResponseDTO> findAll() {
        return saleRepository.findAll()
                .stream().map(SaleResponseDTO::new)
                .toList();
    }

    public SaleResponseDTO findById(UUID id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found. Id: " + id));
        return new SaleResponseDTO(sale);
    }

    public List<SaleResponseDTO> getSalesByUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found. Id " + id));

        return saleRepository.findByUser(user)
                .stream()
                .map(SaleResponseDTO::new)
                .toList();
    }

    public void delete(UUID id){
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found. Id: " + id));
        saleRepository.delete(sale);
    }

    @Transactional
    public SaleResponseDTO update(UUID saleId, SaleRequestDTO obj) {
        return saleRepository.findById(saleId)
                .map(sale -> {
                    updateData(sale, obj);
                    Sale updateSale = saleRepository.save(sale);
                    return new SaleResponseDTO(updateSale);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found. Id: " + saleId));
    }

    private void updateData(Sale sale, SaleRequestDTO obj) {
        Set<UUID> existingProductsId = sale.getItems()
                .stream()
                .map(item -> item.getProduct().getProductId())
                .collect(Collectors.toSet());

        for(ProductSaleRequestDTO item : obj.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found. Id: " + item.productId()));

            if(existingProductsId.contains(product.getProductId())) {

                ProductSale existingProductSale = sale.getItems().stream()
                        .filter(p -> p.getProduct().getProductId().equals(product.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("ProductSale not found for productId: " + product.getProductId()));

                int newQuantity = item.quantity();
                int existingQuantity = existingProductSale.getQuantity();

                if(newQuantity > existingQuantity) {
                    int quantityDecreased = newQuantity - existingQuantity;
                    product.setStockQuantity(product.getStockQuantity() - quantityDecreased);
                }
                else if (newQuantity < existingQuantity){
                    int quantityIncrease = existingQuantity - newQuantity;
                    product.setStockQuantity(product.getStockQuantity() + quantityIncrease);
                }

                existingProductSale.setQuantity(newQuantity);
            }
            else {
                ProductSale productSale = new ProductSale(product, sale, item.quantity());
                sale.getItems().add(productSale);

                product.setStockQuantity(product.getStockQuantity() - item.quantity());
            }
        }
        sale.setSaleDate(Instant.now());
        saleRepository.save(sale);
    }

    @Transactional
    public void cancelSale(UUID id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found. Id: " + id));

        if(Instant.now().isAfter(sale.getSaleDate().plusSeconds(24 * 60 * 60))) {
            throw new SaleCancellationException("Cannot cancel the sale after 24 hours.");
        }

        sale.setSaleStatus(SaleStatus.CANCELED);
        sale.setUser(null);
        sale.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

            if(product.getStockQuantity() > 0) {
                product.setProductStatus(ProductStatus.ACTIVE);
            }
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());

        });
        saleRepository.save(sale);
    }

    public List<SaleResponseDTO> getSalesByDate(String date) {
        String[] dateParts = date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);

        LocalDate localDate = LocalDate.of(year, month, day);

        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

        Instant startOfDayInstant = startOfDay.toInstant(ZoneOffset.UTC);
        Instant endOffDayInstant = endOfDay.toInstant(ZoneOffset.UTC);

        return saleRepository.findBySaleDateBetween(startOfDayInstant, endOffDayInstant)
                .stream().map(SaleResponseDTO::new)
                .toList();
    }

    public List<SaleResponseDTO> getSalesByMonth(String date) {
        String[] dateParts = date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        Instant startInstant = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        return saleRepository.findBySaleDateBetween(startInstant, endInstant)
                .stream()
                .map(SaleResponseDTO::new)
                .toList();
    }

    public List<SaleResponseDTO> getSalesThisWeek () {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfWeek = startOfWeek.plusDays(4).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        Instant startInstant = startOfWeek.toInstant(ZoneOffset.UTC);
        Instant endInstant = endOfWeek.toInstant(ZoneOffset.UTC);

        return saleRepository.findBySaleDateBetween(startInstant, endInstant)
                .stream()
                .map(SaleResponseDTO::new)
                .toList();
    }

    private Set<ProductSale> saveSale(Set<ProductSaleRequestDTO> items, SaleRequestDTO saleRequest) {
        return saleRequest.items().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.productId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found. Id: "+ item.productId()));

                    if(item.quantity() > product.getStockQuantity()) {
                        throw new InsufficientStockException("Insufficient stock available for the requested item: " + product.getName());
                    }
                    product.setStockQuantity(product.getStockQuantity() - item.quantity());

                    if(product.getStockQuantity() == 0) {
                        product.setProductStatus(ProductStatus.INACTIVE);
                    }

                    return new ProductSale(product, item.quantity());
                })
                .collect(Collectors.toSet());
    }
}
