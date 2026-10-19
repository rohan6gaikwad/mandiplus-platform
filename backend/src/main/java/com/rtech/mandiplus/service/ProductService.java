package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.ProductDTO;
import com.rtech.mandiplus.entity.Farmer;
import com.rtech.mandiplus.entity.Product;
import com.rtech.mandiplus.repository.FarmerRepository;
import com.rtech.mandiplus.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public ProductDTO addProduct(ProductDTO dto, String farmerEmail) {
        Farmer farmer = farmerRepository.findByUserEmail(farmerEmail)
                .orElseThrow(() -> new RuntimeException("Error: Farmer profile not found!"));

        Product product = new Product();
        product.setFarmer(farmer);
        product.setCropName(dto.getCropName());
        product.setVariety(dto.getVariety());
        product.setQuantityQuintals(dto.getQuantityQuintals());
        
        java.math.BigDecimal suggestedPrice = dto.getSuggestedPricePerQuintal() != null 
                ? dto.getSuggestedPricePerQuintal() 
                : dto.getPricePerQuintal();
        product.setSuggestedPricePerQuintal(suggestedPrice);
        product.setPricePerQuintal(suggestedPrice);
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setLocation(dto.getLocation() != null ? dto.getLocation() : farmer.getDistrict() + ", " + farmer.getState());
        product.setStatus("PENDING_APPROVAL");
        product.setAdminFeedback("Awaiting Admin moderation review");

        product = productRepository.save(product);

        // Targeted alert to Admins that a new listing needs review
        notificationService.sendToAdmin(
            "New Produce Awaiting Approval",
            String.format("Farmer %s submitted %s (%s) - %s qtl with suggested rate ₹%s/qtl. Review photos & price to approve.", 
                farmer.getName(), product.getCropName(), product.getVariety(), 
                product.getQuantityQuintals().toString(), product.getSuggestedPricePerQuintal().toString()),
            "MARKETPLACE"
        );

        // Targeted alert to the farmer confirming submission
        if (farmer.getUser() != null) {
            notificationService.sendNotification(
                farmer.getUser().getId(),
                "Crop Submitted for Review",
                String.format("Your listing for %s (%s) at suggested ₹%s/qtl has been sent to Admin for inspection.",
                    product.getCropName(), product.getVariety(), product.getSuggestedPricePerQuintal()),
                "MARKETPLACE"
            );
        }

        return convertToDTO(product);
    }

    public List<ProductDTO> getAllProducts(String crop, String location) {
        List<Product> products;
        if (crop != null && !crop.isEmpty() && location != null && !location.isEmpty()) {
            products = productRepository.findByCropNameContainingIgnoreCaseAndLocationContainingIgnoreCaseAndStatus(crop, location, "AVAILABLE");
        } else if (crop != null && !crop.isEmpty()) {
            products = productRepository.findByCropNameContainingIgnoreCaseAndStatus(crop, "AVAILABLE");
        } else if (location != null && !location.isEmpty()) {
            products = productRepository.findByLocationContainingIgnoreCaseAndStatus(location, "AVAILABLE");
        } else {
            products = productRepository.findByStatusOrderByCreatedAtDesc("AVAILABLE");
        }

        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getApprovedProducts() {
        return getAllProducts(null, null);
    }

    public List<ProductDTO> searchProducts(String crop, String location) {
        return getAllProducts(crop, location);
    }

    public ProductDTO createProduct(ProductDTO dto, String email) {
        return addProduct(dto, email);
    }

    public List<ProductDTO> getFarmerProducts(String email) {
        return productRepository.findByFarmerUserEmailOrderByCreatedAtDesc(email).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto, String farmerEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found!"));

        if (!product.getFarmer().getUser().getEmail().equals(farmerEmail)) {
            throw new RuntimeException("Error: Unauthorized to edit this product!");
        }

        java.math.BigDecimal suggestedPrice = dto.getSuggestedPricePerQuintal() != null 
                ? dto.getSuggestedPricePerQuintal() 
                : dto.getPricePerQuintal();

        product.setCropName(dto.getCropName());
        product.setVariety(dto.getVariety());
        product.setQuantityQuintals(dto.getQuantityQuintals());
        product.setSuggestedPricePerQuintal(suggestedPrice);
        product.setPricePerQuintal(suggestedPrice);
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        if (dto.getLocation() != null) {
            product.setLocation(dto.getLocation());
        }
        // Resubmitting resets status to PENDING_APPROVAL so admin inspects updated price/photos
        product.setStatus("PENDING_APPROVAL");
        product.setAdminFeedback("Resubmitted by farmer for review");

        product = productRepository.save(product);

        notificationService.sendToAdmin(
            "Produce Resubmitted for Approval",
            String.format("Farmer %s updated %s (%s). Please review updated photo/price.", 
                product.getFarmer().getName(), product.getCropName(), product.getVariety()),
            "MARKETPLACE"
        );

        return convertToDTO(product);
    }

    @Transactional
    public void deleteProduct(Long id, String email) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found!"));

        if (!product.getFarmer().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Error: Unauthorized to delete this product!");
        }

        productRepository.delete(product);
    }

    public ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setFarmerId(product.getFarmer().getId());
        dto.setFarmerName(product.getFarmer().getName());
        dto.setFarmerPhone(product.getFarmer().getPhone());
        if (product.getFarmer().getUser() != null) {
            dto.setFarmerProfilePhoto(product.getFarmer().getUser().getProfilePhoto());
        }
        dto.setCropName(product.getCropName());
        dto.setVariety(product.getVariety());
        dto.setQuantityQuintals(product.getQuantityQuintals());
        dto.setPricePerQuintal(product.getPricePerQuintal());
        dto.setSuggestedPricePerQuintal(product.getSuggestedPricePerQuintal());
        dto.setAdminFeedback(product.getAdminFeedback());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setLocation(product.getLocation());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
}
