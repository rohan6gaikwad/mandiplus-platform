package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.StorageBookingDTO;
import com.rtech.mandiplus.entity.ColdStorage;
import com.rtech.mandiplus.service.ColdStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ColdStorageController {

    @Autowired
    private ColdStorageService coldStorageService;

    // Public API - Browse cold storage facilities
    @GetMapping("/public/cold-storage")
    public ResponseEntity<List<ColdStorage>> getAllFacilities(@RequestParam(required = false) String location) {
        return ResponseEntity.ok(coldStorageService.getAllFacilities(location));
    }

    // Farmer Private API - View bookings
    @GetMapping("/farmer/cold-storage/bookings")
    public ResponseEntity<List<StorageBookingDTO>> getFarmerBookings(Authentication authentication) {
        return ResponseEntity.ok(coldStorageService.getFarmerBookings(authentication.getName()));
    }

    // Buyer Private API - View bookings
    @GetMapping("/buyer/cold-storage/bookings")
    public ResponseEntity<List<StorageBookingDTO>> getBuyerBookings(Authentication authentication) {
        return ResponseEntity.ok(coldStorageService.getBuyerBookings(authentication.getName()));
    }

    // Storage Manager Private API - View bookings queue
    @GetMapping("/cold-storage/bookings")
    public ResponseEntity<List<StorageBookingDTO>> getManagerBookings(Authentication authentication) {
        return ResponseEntity.ok(coldStorageService.getManagerBookings(authentication.getName()));
    }

    // Farmer Private API - Book storage space
    @PostMapping({"/farmer/cold-storage/bookings", "/farmer/cold-storage/book"})
    public ResponseEntity<?> bookStorage(@RequestBody StorageBookingDTO bookingDTO, Authentication authentication) {
        try {
            StorageBookingDTO completedBooking = coldStorageService.bookStorage(bookingDTO, authentication.getName());
            return ResponseEntity.ok(completedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Buyer Private API - Book storage space
    @PostMapping({"/buyer/cold-storage/bookings", "/buyer/cold-storage/book"})
    public ResponseEntity<?> bookStorageForBuyer(@RequestBody StorageBookingDTO bookingDTO, Authentication authentication) {
        try {
            StorageBookingDTO completedBooking = coldStorageService.bookStorageForBuyer(bookingDTO, authentication.getName());
            return ResponseEntity.ok(completedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Storage Manager Private API - Retrieve managed facility details
    @GetMapping("/cold-storage/my-facility")
    public ResponseEntity<?> getManagerFacility(Authentication authentication) {
        try {
            ColdStorage facility = coldStorageService.getManagerFacility(authentication.getName());
            return ResponseEntity.ok(facility);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Storage Manager Private API - Update managed facility details
    @PutMapping("/cold-storage/my-facility")
    public ResponseEntity<?> updateFacilityDetails(@RequestBody ColdStorage details, Authentication authentication) {
        try {
            ColdStorage updated = coldStorageService.updateFacilityDetails(details, authentication.getName());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Storage Manager Private API - Approve/Reject booking
    @PutMapping("/cold-storage/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason,
            Authentication authentication) {
        try {
            StorageBookingDTO updatedBooking = coldStorageService.updateBookingStatus(id, status, rejectionReason, authentication.getName());
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
