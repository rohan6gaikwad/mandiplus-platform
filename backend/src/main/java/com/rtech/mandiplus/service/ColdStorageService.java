package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.StorageBookingDTO;
import com.rtech.mandiplus.entity.ColdStorage;
import com.rtech.mandiplus.entity.Farmer;
import com.rtech.mandiplus.entity.StorageBooking;
import com.rtech.mandiplus.entity.Buyer;
import com.rtech.mandiplus.repository.BuyerRepository;
import com.rtech.mandiplus.repository.ColdStorageRepository;
import com.rtech.mandiplus.repository.FarmerRepository;
import com.rtech.mandiplus.repository.StorageBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColdStorageService {

    @Autowired
    private ColdStorageRepository coldStorageRepository;

    @Autowired
    private StorageBookingRepository storageBookingRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private NotificationService notificationService;

    public List<ColdStorage> getAllFacilities(String location) {
        List<ColdStorage> list;
        if (location != null && !location.isEmpty()) {
            list = coldStorageRepository.findByLocationContainingIgnoreCase(location);
        } else {
            list = coldStorageRepository.findAll();
        }
        for (ColdStorage storage : list) {
            BigDecimal occupied = storageBookingRepository.sumOccupiedCapacityByStorageId(storage.getId());
            storage.setAvailableCapacityTons(storage.getTotalCapacityTons().subtract(occupied));
        }
        return list;
    }

    public List<StorageBookingDTO> getFarmerBookings(String farmerEmail) {
        return storageBookingRepository.findByFarmerUserEmail(farmerEmail).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StorageBookingDTO> getManagerBookings(String managerEmail) {
        return storageBookingRepository.findByColdStorageManagerEmail(managerEmail).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StorageBookingDTO bookStorage(StorageBookingDTO dto, String farmerEmail) {
        Farmer farmer = farmerRepository.findByUserEmail(farmerEmail)
                .orElseThrow(() -> new RuntimeException("Error: Farmer profile not found!"));

        ColdStorage storage = coldStorageRepository.findById(dto.getColdStorageId())
                .orElseThrow(() -> new RuntimeException("Error: Cold storage unit not found!"));

        BigDecimal occupied = storageBookingRepository.sumOccupiedCapacityByStorageId(storage.getId());
        BigDecimal dynamicAvailable = storage.getTotalCapacityTons().subtract(occupied);
        if (dynamicAvailable.compareTo(dto.getQuantityTons()) < 0) {
            throw new RuntimeException("Error: Insufficient capacity in this storage unit! No space left.");
        }

        // Calculate lease price (Days * Quantity * Rate)
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate());
        if (days <= 0) {
            throw new RuntimeException("Error: End date must be after start date!");
        }
        BigDecimal totalPrice = dto.getQuantityTons()
                .multiply(BigDecimal.valueOf(days))
                .multiply(storage.getPricePerTonDay());

        StorageBooking booking = new StorageBooking();
        booking.setFarmer(farmer);
        booking.setColdStorage(storage);
        booking.setQuantityTons(dto.getQuantityTons());
        booking.setStartDate(dto.getStartDate());
        booking.setEndDate(dto.getEndDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus("PENDING");

        booking = storageBookingRepository.save(booking);

        // Notify Cold Storage Manager
        notificationService.sendNotification(
            NotificationService.getUserIdSafely(storage.getManager()),
            "New Storage Booking Request",
            String.format("Farmer %s has requested to book %s tons from %s to %s.", farmer.getName(), dto.getQuantityTons().toString(), dto.getStartDate().toString(), dto.getEndDate().toString()),
            "COLD_STORAGE"
        );

        return convertToDTO(booking);
    }

    public List<StorageBookingDTO> getBuyerBookings(String buyerEmail) {
        return storageBookingRepository.findByBuyerUserEmail(buyerEmail).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StorageBookingDTO bookStorageByBuyer(StorageBookingDTO dto, String buyerEmail) {
        return bookStorageForBuyer(dto, buyerEmail);
    }

    @Transactional
    public StorageBookingDTO bookStorageForBuyer(StorageBookingDTO dto, String buyerEmail) {
        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Error: Buyer profile not found!"));

        ColdStorage storage = coldStorageRepository.findById(dto.getColdStorageId())
                .orElseThrow(() -> new RuntimeException("Error: Cold storage unit not found!"));

        BigDecimal occupied = storageBookingRepository.sumOccupiedCapacityByStorageId(storage.getId());
        BigDecimal dynamicAvailable = storage.getTotalCapacityTons().subtract(occupied);
        if (dynamicAvailable.compareTo(dto.getQuantityTons()) < 0) {
            throw new RuntimeException("Error: Insufficient capacity in this storage unit! No space left.");
        }

        // Calculate lease price (Days * Quantity * Rate)
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate());
        if (days <= 0) {
            throw new RuntimeException("Error: End date must be after start date!");
        }
        BigDecimal totalPrice = dto.getQuantityTons()
                .multiply(BigDecimal.valueOf(days))
                .multiply(storage.getPricePerTonDay());

        StorageBooking booking = new StorageBooking();
        booking.setBuyer(buyer);
        booking.setColdStorage(storage);
        booking.setQuantityTons(dto.getQuantityTons());
        booking.setStartDate(dto.getStartDate());
        booking.setEndDate(dto.getEndDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus("PENDING");

        booking = storageBookingRepository.save(booking);

        // Notify Cold Storage Manager
        notificationService.sendNotification(
            NotificationService.getUserIdSafely(storage.getManager()),
            "New Storage Booking Request",
            String.format("Buyer %s has requested to book %s tons from %s to %s.", buyer.getCompanyName(), dto.getQuantityTons().toString(), dto.getStartDate().toString(), dto.getEndDate().toString()),
            "COLD_STORAGE"
        );

        return convertToDTO(booking);
    }

    @Transactional
    public StorageBookingDTO updateBookingStatus(Long bookingId, String status, String rejectionReason, String managerEmail) {
        StorageBooking booking = storageBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Error: Storage booking not found!"));

        if (!booking.getColdStorage().getManager().getEmail().equals(managerEmail)) {
            throw new RuntimeException("Error: Unauthorized to modify this booking!");
        }

        String oldStatus = booking.getStatus();
        String upperStatus = status.toUpperCase();

        if (oldStatus != null && oldStatus.equalsIgnoreCase(upperStatus)) {
            return convertToDTO(booking);
        }

        booking.setStatus(upperStatus);
        if ("REJECTED".equalsIgnoreCase(upperStatus)) {
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                throw new RuntimeException("Error: Rejection reason is required when rejecting a booking!");
            }
            booking.setRejectionReason(rejectionReason);
        } else {
            booking.setRejectionReason(null); // Clear if approved/restored
        }

        ColdStorage storage = booking.getColdStorage();

        // Deduct available capacity if booking transitions to APPROVED
        if ("APPROVED".equalsIgnoreCase(upperStatus) && !"APPROVED".equalsIgnoreCase(oldStatus)) {
            storage.allocateCapacity(booking.getQuantityTons());
            coldStorageRepository.save(storage);
        }

        // Restore available capacity if booking transitions from APPROVED to any other state
        if ("APPROVED".equalsIgnoreCase(oldStatus) && !"APPROVED".equalsIgnoreCase(upperStatus)) {
            storage.restoreCapacity(booking.getQuantityTons());
            coldStorageRepository.save(storage);
        }

        booking = storageBookingRepository.save(booking);

        // Format notification message
        String msg;
        if ("REJECTED".equalsIgnoreCase(upperStatus)) {
            msg = String.format("Your booking request #BK-%d for %s has been REJECTED. Reason: %s", booking.getId(), booking.getColdStorage().getName(), rejectionReason);
        } else {
            msg = String.format("Your booking request #BK-%d for %s has been %s.", booking.getId(), booking.getColdStorage().getName(), status.toUpperCase());
        }

        // Notify Farmer or Buyer
        if (booking.getFarmer() != null) {
            notificationService.sendNotification(
                NotificationService.getUserIdSafely(booking.getFarmer().getUser()),
                "Cold Storage Booking Update",
                msg,
                "COLD_STORAGE"
            );
        } else if (booking.getBuyer() != null) {
            notificationService.sendNotification(
                NotificationService.getUserIdSafely(booking.getBuyer().getUser()),
                "Cold Storage Booking Update",
                msg,
                "COLD_STORAGE"
            );
        }

        return convertToDTO(booking);
    }

    public ColdStorage getManagerFacility(String managerEmail) {
        ColdStorage facility = coldStorageRepository.findByManagerEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Error: Cold storage facility not found for this manager!"));
        BigDecimal occupied = storageBookingRepository.sumOccupiedCapacityByStorageId(facility.getId());
        facility.setAvailableCapacityTons(facility.getTotalCapacityTons().subtract(occupied));
        return facility;
    }

    @Transactional
    public ColdStorage updateFacilityDetails(ColdStorage details, String managerEmail) {
        ColdStorage facility = coldStorageRepository.findByManagerEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Error: Cold storage facility not found for this manager!"));

        facility.updateDetails(
            details.getName(),
            details.getLocation(),
            details.getTotalCapacityTons(),
            details.getPricePerTonDay(),
            details.getDescription()
        );

        ColdStorage saved = coldStorageRepository.save(facility);
        BigDecimal occupied = storageBookingRepository.sumOccupiedCapacityByStorageId(saved.getId());
        saved.setAvailableCapacityTons(saved.getTotalCapacityTons().subtract(occupied));
        return saved;
    }


    public StorageBookingDTO convertToDTO(StorageBooking booking) {
        StorageBookingDTO dto = new StorageBookingDTO();
        dto.setId(booking.getId());
        if (booking.getFarmer() != null) {
            dto.setFarmerId(booking.getFarmer().getId());
            dto.setFarmerName(booking.getFarmer().getName());
            dto.setFarmerPhone(booking.getFarmer().getPhone());
            if (booking.getFarmer().getUser() != null) {
                dto.setFarmerProfilePhoto(booking.getFarmer().getUser().getProfilePhoto());
            }
        }
        if (booking.getBuyer() != null) {
            dto.setBuyerId(booking.getBuyer().getId());
            dto.setBuyerName(booking.getBuyer().getCompanyName());
            dto.setBuyerPhone(booking.getBuyer().getPhone());
            if (booking.getBuyer().getUser() != null) {
                dto.setBuyerProfilePhoto(booking.getBuyer().getUser().getProfilePhoto());
            }
        }
        dto.setColdStorageId(booking.getColdStorage().getId());
        dto.setStorageName(booking.getColdStorage().getName());
        dto.setStorageLocation(booking.getColdStorage().getLocation());
        dto.setQuantityTons(booking.getQuantityTons());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());
        dto.setRejectionReason(booking.getRejectionReason());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }
}
