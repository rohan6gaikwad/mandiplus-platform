package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.ProductDTO;
import com.rtech.mandiplus.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Public API - Browse marketplace products
    @GetMapping({"/public/products", "/public/products/search"})
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String crop,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location) {
        String searchCrop = crop != null ? crop : query;
        return ResponseEntity.ok(productService.getAllProducts(searchCrop, location));
    }

    // Farmer Private API - View crop listings
    @GetMapping("/farmer/products")
    public ResponseEntity<List<ProductDTO>> getFarmerProducts(Authentication authentication) {
        return ResponseEntity.ok(productService.getFarmerProducts(authentication.getName()));
    }

    // Farmer Private API - List new produce
    @PostMapping("/farmer/products")
    public ResponseEntity<?> addProduct(@RequestBody ProductDTO productDTO, Authentication authentication) {
        try {
            ProductDTO createdProduct = productService.addProduct(productDTO, authentication.getName());
            return ResponseEntity.ok(createdProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Farmer Private API - Update product listing
    @PutMapping("/farmer/products/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id, 
            @RequestBody ProductDTO productDTO, 
            Authentication authentication) {
        try {
            ProductDTO updated = productService.updateProduct(id, productDTO, authentication.getName());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Farmer Private API - Delete product listing
    @DeleteMapping("/farmer/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        try {
            productService.deleteProduct(id, authentication.getName());
            return ResponseEntity.ok("Product deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
