package com.paulo.ecommerceX.services;

import com.paulo.ecommerceX.domain.Product;
import com.paulo.ecommerceX.domain.dto.product.ProductRequestDTO;
import com.paulo.ecommerceX.domain.dto.product.ProductResponseDTO;
import com.paulo.ecommerceX.domain.enums.ProductStatus;
import com.paulo.ecommerceX.repositories.ProductRepository;
import com.paulo.ecommerceX.services.exceptions.ResourceNotFoundException;
import com.paulo.ecommerceX.services.exceptions.UniqueConstraintViolationException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponseDTO> findAll(){
        return productRepository.findAll()
                .stream()
                .filter(product -> product.getProductStatus() == ProductStatus.ACTIVE)
                .map(ProductResponseDTO::new)
                .toList();
    }

    public ProductResponseDTO findById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found. Id: " + id));
        return new ProductResponseDTO(product);
    }

    public Product insert(Product product){
        if(productRepository.findByName(product.getName().toUpperCase(Locale.ROOT)).isPresent()) {
            throw new UniqueConstraintViolationException("Product already exists.");
        }
        return productRepository.save(product);
    }

    public void delete(UUID id) {
        try {
            productRepository.deleteById(id);
        }catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Product not found. Id: " + id);
        }catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException(ex.getMessage());
        }
    }

    public ProductResponseDTO update(UUID id, ProductRequestDTO obj) {
        return productRepository.findById(id)
                .map(product -> {
                    updateData(product, obj);
                    Product updateProduct = productRepository.save(product);
                    return new ProductResponseDTO(updateProduct);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Product not found. Id: " + id));
    }

    private void updateData(Product product, ProductRequestDTO obj) {
        product.setName(obj.name().toUpperCase(Locale.ROOT));
        product.setPrice(obj.price());
        product.setStockQuantity(obj.stockQuantity());
        if (product.getStockQuantity() > 0) {
            product.setProductStatus(ProductStatus.ACTIVE);
        }
    }
}
