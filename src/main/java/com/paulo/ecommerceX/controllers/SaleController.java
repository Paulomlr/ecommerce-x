package com.paulo.ecommerceX.controllers;

import com.paulo.ecommerceX.domain.Sale;
import com.paulo.ecommerceX.domain.dto.sale.SaleItemRequestDTO;
import com.paulo.ecommerceX.domain.dto.sale.SaleRequestDTO;
import com.paulo.ecommerceX.domain.dto.sale.SaleResponseDTO;
import com.paulo.ecommerceX.services.SaleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService service;

    @GetMapping
    public ResponseEntity<List<SaleResponseDTO>> findAll(){
        List<SaleResponseDTO> productList = service.findAll();
        return ResponseEntity.ok(productList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> findById(@PathVariable UUID id) {
        SaleResponseDTO sale = service.findById(id);
        return ResponseEntity.ok().body(sale);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<SaleResponseDTO>> findSalesByUser (@PathVariable UUID id) {
        List<SaleResponseDTO> sales = service.getSalesByUser(id);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<SaleResponseDTO>> findSalesByMonth(@RequestParam("date") String date) {
        List<SaleResponseDTO> sales = service.getSalesByMonth(date);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/this-week")
    public ResponseEntity<List<SaleResponseDTO>> findSalesThisWeek() {
        List<SaleResponseDTO> sales = service.getSalesThisWeek();
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/by-day")
    public ResponseEntity<List<SaleResponseDTO>> findSalesByDay(@RequestParam("date") String date) {
        List<SaleResponseDTO> sales = service.getSalesByDate(date);
        return ResponseEntity.ok(sales);
    }

    @PostMapping
    public ResponseEntity<Void> insert(@RequestBody @Valid SaleRequestDTO body){
        Sale sale = service.insert(body);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(sale.getSaleId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid SaleRequestDTO body) {
        SaleResponseDTO saleResponse = service.update(id, body);
        return ResponseEntity.ok().body(saleResponse);
    }

    @PutMapping("/items/{saleId}")
    public ResponseEntity<SaleResponseDTO> deleteSaleItem(@PathVariable UUID saleId, @RequestBody @Valid SaleItemRequestDTO body) {
        SaleResponseDTO saleResponse = service.deleteItem(saleId, body);
        return ResponseEntity.ok(saleResponse);
    }

    @PutMapping("/cancel/{saleId}")
    public ResponseEntity<Void> cancelPurchase (@PathVariable UUID saleId) {
        service.cancelSale(saleId);
        return ResponseEntity.noContent().build();
    }
}
