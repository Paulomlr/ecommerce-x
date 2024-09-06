package com.paulo.ecommerceX.services;

import com.paulo.ecommerceX.domain.Product;
import com.paulo.ecommerceX.domain.ProductSale;
import com.paulo.ecommerceX.domain.Sale;
import com.paulo.ecommerceX.domain.User;
import com.paulo.ecommerceX.domain.dto.productSale.DeleteProductSaleRequestDTO;
import com.paulo.ecommerceX.domain.dto.productSale.ProductSaleRequestDTO;
import com.paulo.ecommerceX.domain.dto.sale.SaleItemRequestDTO;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Cacheable(value = "sales")
    public List<SaleResponseDTO> findAll() {
        System.out.println("Fetching sales from database...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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

        return saleRepository.findByUser(user)  // retornar todas as vendas de um usuário
                .stream()
                .map(SaleResponseDTO::new) // converter a venda para um SaleResponseDTO
                .toList();
    }

    @Transactional
    @CacheEvict(value = "sales", allEntries = true)
    public Sale insert(SaleRequestDTO saleRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // pegar o login do usuário que vai fazer a venda

        User user = (User) userRepository.findByLogin(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Set<ProductSale> productSales = saveSale(saleRequest);  // chamar o método de salvar a venda passando o request do usuário

        Sale sale = new Sale(Instant.now(), user, productSales, SaleStatus.COMPLETED); // se não lançar nenhuma exceção, eu crio a venda
                                                                                       // passando o set de ProductSale
        productSales.forEach(productSale -> productSale.getId().setSale(sale));

        productRepository.saveAll(
                productSales.stream()
                        .map(ProductSale::getProduct)
                        .toList()
        );
        return saleRepository.save(sale);
    }

    @CacheEvict(value = "sales", allEntries = true)
    public void delete(UUID id){
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found. Id: " + id));
        saleRepository.delete(sale);
    }

    @Transactional
    @CacheEvict(value = "sales", allEntries = true)
    public SaleResponseDTO update(UUID saleId, SaleRequestDTO obj) {
        return saleRepository.findById(saleId)// verifico se a venda existe
                .map(sale -> {
                    updateData(sale, obj); // chamo updateData passando a venda encontrada e os itens para atualizar
                    Sale updateSale = saleRepository.save(sale);
                    return new SaleResponseDTO(updateSale);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found. Id: " + saleId));
    }

    private void updateData(Sale sale, SaleRequestDTO obj) {
        Set<UUID> existingProductsId = sale.getItems() // para cada item da venda eu guardo o id
                .stream()
                .map(item -> item.getProduct().getProductId())
                .collect(Collectors.toSet());

        for(ProductSaleRequestDTO item : obj.items()) {
            Product product = productRepository.findById(item.productId()) // verifca se os ids passados existem
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found. Id: " + item.productId()));

            if(existingProductsId.contains(product.getProductId())) { // verifica se um id de um produto passado no request já existe na venda

                ProductSale existingProductSale = sale.getItems().stream() // busco um ProductSale da venda pelo o id do produto passado no request
                        .filter(p -> p.getProduct().getProductId().equals(product.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("ProductSale not found for productId: " + product.getProductId()));

                int newQuantity = item.quantity(); // nova quantidade do item na venda
                int existingQuantity = existingProductSale.getQuantity(); // quantidade antiiga do item na venda

                if(newQuantity > existingQuantity) { // verifico se a nova quantidade é maior que a antiga
                    int quantityDecreased = newQuantity - existingQuantity; // pegea a diferença dessas quantidades
                    product.setStockQuantity(product.getStockQuantity() - quantityDecreased); // diminuo a quantidade do estoque do produto
                }
                else if (newQuantity < existingQuantity){ // verifico se a nova quantidade é menor que a antiga
                    int quantityIncrease = existingQuantity - newQuantity;
                    product.setStockQuantity(product.getStockQuantity() + quantityIncrease); // aumento a quantidade do estoque do produto
                }

                existingProductSale.setQuantity(newQuantity); // modifico o campo quantidade no produtSale
            }
            else { // se o id passado não existe na venda, significa que vai ser adicionado na mesma
                ProductSale productSale = new ProductSale(product, sale, item.quantity()); // crio um novo productSale e adiciono a venda
                sale.getItems().add(productSale);

                product.setStockQuantity(product.getStockQuantity() - item.quantity()); // diminuo a quantidade do produto no estoque
            }
        }
        sale.setSaleDate(Instant.now());
        saleRepository.save(sale);
    }

    @Transactional
    @CacheEvict(value = "sales", allEntries = true)
    public SaleResponseDTO deleteItem(UUID saleId, SaleItemRequestDTO body) {
        Sale sale = saleRepository.findById(saleId) // procura a venda
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found. Id: " + saleId));

        for(DeleteProductSaleRequestDTO item : body.items()) {
            Product product = productRepository.findById(item.productId()) // verifico se o produto existe
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found. Id: " + item.productId()));

            sale.getItems().removeIf(prod -> { // remove o produto se o id do produto passado na requisição existe na venda
                if(prod.getProduct().getProductId().equals(product.getProductId())){
                    int quantityRemoved = prod.getQuantity(); // guarda a quantidade removida

                    product.setStockQuantity(product.getStockQuantity() + quantityRemoved); // devolve a quantidade removida do produto pro estoque
                    productRepository.save(product);
                    return true; // retorna true se isso acontecer e aplica as alterações
                }
                return false; // se não acontecer, se o produto não existe na venda, retorna false
            });
        }
        saleRepository.save(sale);
        return new SaleResponseDTO(sale);
    }

    @Transactional
    @CacheEvict(value = "sales", allEntries = true)
    public void cancelSale(UUID id) { // método pro usuário cancelar a venda do usuário mas não apagar do banco de dados
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found. Id: " + id));

        if(Instant.now().isAfter(sale.getSaleDate().plusSeconds(24 * 60 * 60))) {
            throw new SaleCancellationException("Cannot cancel the sale after 24 hours."); // caso a venda passe de mais de 24h criada, não vai se possivel cancelar mais
        }

        sale.setSaleStatus(SaleStatus.CANCELED);
        sale.setUser(null);
        sale.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

            if(product.getStockQuantity() == 0) {   // se o produto não tiver nenhuma quantidade disponivel no estoque
                product.setProductStatus(ProductStatus.ACTIVE); // muda o status para ativo já que a quantidade do produto na venda retornará ao estoque
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
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0); // ajustando a data para o inicio da semana atual
        LocalDateTime endOfWeek = startOfWeek.plusDays(4).withHour(23).withMinute(59).withSecond(59).withNano(999999999); // adiciona 4 dias a data de inicio da semana

        Instant startInstant = startOfWeek.toInstant(ZoneOffset.UTC);
        Instant endInstant = endOfWeek.toInstant(ZoneOffset.UTC);

        return saleRepository.findBySaleDateBetween(startInstant, endInstant)
                .stream()
                .map(SaleResponseDTO::new)
                .toList();
    }

    private Set<ProductSale> saveSale(SaleRequestDTO saleRequest) {
        return saleRequest.items().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.productId()) // verifico se cada id do produto existe
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found. Id: "+ item.productId()));

                    if(item.quantity() > product.getStockQuantity()) { // se a quantidade do item for maior do que a do estoque, lança uma exceção
                        throw new InsufficientStockException("Insufficient stock available for the requested item: " + product.getName());
                    }
                    product.setStockQuantity(product.getStockQuantity() - item.quantity()); // diminuo a quantidade do produto no estoque

                    if(product.getStockQuantity() == 0) { // já verifico se quando diminui o estoque do produto, se o estoque é igual a zero
                        product.setProductStatus(ProductStatus.INACTIVE); // se for, o produto fica inativo
                    }

                    return new ProductSale(product, item.quantity());
                })
                .collect(Collectors.toSet());
    }
}
