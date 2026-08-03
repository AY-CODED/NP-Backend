package org.admin.npapplication.service;

import org.admin.npapplication.model.Product;
import org.admin.npapplication.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (updatedProduct.getName() != null && !updatedProduct.getName().isBlank()) {
            existing.setName(updatedProduct.getName());
        }
        if (updatedProduct.getDescription() != null) {
            existing.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getPrice() > 0) {
            existing.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getStock() >= 0) {
            existing.setStock(updatedProduct.getStock());
        }
        if (updatedProduct.getCategory() != null && !updatedProduct.getCategory().isBlank()) {
            existing.setCategory(updatedProduct.getCategory());
        }
        if (updatedProduct.getBadge() != null) {
            existing.setBadge(updatedProduct.getBadge());
        }
        if (updatedProduct.getImage() != null) {
            existing.setImage(updatedProduct.getImage());
        }
        existing.setFeatured(updatedProduct.isFeatured());
        existing.setActive(updatedProduct.isActive());

        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
