package com.rtech.mandiplus.service;

import com.rtech.mandiplus.config.JwtUtils;
import com.rtech.mandiplus.dto.JwtResponse;
import com.rtech.mandiplus.dto.LoginRequest;
import com.rtech.mandiplus.dto.RegisterRequest;
import com.rtech.mandiplus.dto.ProfileDTO;
import com.rtech.mandiplus.dto.ProfileUpdateRequest;
import com.rtech.mandiplus.entity.Buyer;
import com.rtech.mandiplus.entity.ColdStorage;
import com.rtech.mandiplus.entity.DeliveryPartner;
import com.rtech.mandiplus.entity.Farmer;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.BuyerRepository;
import com.rtech.mandiplus.repository.ColdStorageRepository;
import com.rtech.mandiplus.repository.DeliveryPartnerRepository;
import com.rtech.mandiplus.repository.FarmerRepository;
import com.rtech.mandiplus.repository.UserRepository;
import com.rtech.mandiplus.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ColdStorageRepository coldStorageRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.rtech.mandiplus.service.otp.OtpService otpService;

    @Autowired(required = false)
    private com.rtech.mandiplus.service.firebase.FirebaseTokenVerifier firebaseTokenVerifier;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        String email = loginRequest.getEmail() != null ? loginRequest.getEmail().trim().toLowerCase() : "";
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        if ("ADMIN".equalsIgnoreCase(role) && !"admin@gmail.com".equalsIgnoreCase(email)) {
            throw new RuntimeException("Error: Invalid credentials.");
        }

        User dbUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Authenticated user credentials not found in DB!"));

        if (dbUser.getFullName() == null || dbUser.getFullName().trim().isEmpty()) {
            syncUserNameFromProfile(dbUser);
        }

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                role,
                dbUser.getProfilePhoto(),
                dbUser.getFullName(),
                dbUser.getPhone(),
                dbUser.isProfileComplete()
        );
    }

    @Transactional
    public String registerUser(RegisterRequest signUpRequest) {
        if ("ADMIN".equalsIgnoreCase(signUpRequest.getRole())) {
            throw new RuntimeException("Error: Admin accounts cannot be registered publicly. System administrator access is strictly restricted.");
        }
        String email = signUpRequest.getEmail() != null ? signUpRequest.getEmail().trim().toLowerCase() : "";
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        String rawPhone = signUpRequest.getPhone();
        String normalizedPhone = null;
        if (rawPhone != null && !rawPhone.trim().isEmpty()) {
            normalizedPhone = otpService != null ? otpService.normalizePhoneNumber(rawPhone) : rawPhone.replaceAll("[^0-9]", "");
            if (normalizedPhone.length() > 10 && normalizedPhone.startsWith("91")) {
                normalizedPhone = normalizedPhone.substring(2);
            }
            // Prevent duplicate registration with same phone number
            if ("FARMER".equalsIgnoreCase(signUpRequest.getRole())) {
                if (farmerRepository != null && (farmerRepository.findByPhone(normalizedPhone).isPresent()
                        || (userRepository.findByPhone(normalizedPhone).isPresent()
                            && farmerRepository.findByUser(userRepository.findByPhone(normalizedPhone).get()).isPresent()))) {
                    throw new RuntimeException("This mobile number (+91-" + normalizedPhone + ") is already registered to a Farmer profile. Please log in directly with this mobile number to access your account and history.");
                }
            } else if (userRepository.existsByPhone(normalizedPhone)) {
                throw new RuntimeException("This mobile number (+91-" + normalizedPhone + ") is already registered to an account. Please log in directly.");
            }
        }

        // Create new user's account
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(signUpRequest.getRole().toUpperCase());
        user.setActive(true);
        user.setProfilePhoto(signUpRequest.getProfilePhoto());
        if (normalizedPhone != null) {
            user.setPhone(normalizedPhone);
        }
        if (signUpRequest.getName() != null && !signUpRequest.getName().trim().isEmpty()) {
            user.setFullName(signUpRequest.getName().trim());
            user.setProfileCompleted(true);
        }

        user = userRepository.save(user);

        // Based on user role, create respective profile using domain creation validation
        String role = signUpRequest.getRole().toUpperCase();
        if ("FARMER".equals(role)) {
            Farmer farmer = Farmer.createAndValidate(
                user,
                signUpRequest.getName(),
                normalizedPhone != null ? normalizedPhone : signUpRequest.getPhone(),
                signUpRequest.getState(),
                signUpRequest.getDistrict(),
                signUpRequest.getFarmSizeAcres(),
                signUpRequest.getUpiId()
            );
            farmerRepository.save(farmer);
        } else if ("BUYER".equals(role)) {
            Buyer buyer = Buyer.createAndValidate(
                user,
                signUpRequest.getCompanyName(),
                signUpRequest.getName(),
                signUpRequest.getPhone(),
                signUpRequest.getAddress(),
                signUpRequest.getGstin()
            );
            buyerRepository.save(buyer);
        } else if ("COLD_STORAGE_MANAGER".equals(role)) {
            ColdStorage storage = ColdStorage.createAndValidate(
                user,
                signUpRequest.getStorageName(),
                signUpRequest.getStorageLocation(),
                signUpRequest.getStorageCapacityTons(),
                signUpRequest.getStoragePricePerTonDay(),
                signUpRequest.getStorageDescription()
            );
            coldStorageRepository.save(storage);
        } else if ("DELIVERY_PARTNER".equals(role)) {
            DeliveryPartner partner = DeliveryPartner.create(
                user,
                signUpRequest.getName(),
                signUpRequest.getPhone(),
                signUpRequest.getDistrict(),
                signUpRequest.getVehicleType(),
                signUpRequest.getVehicleNumber(),
                signUpRequest.getLicenseNumber(),
                signUpRequest.getPreferredHub(),
                signUpRequest.getUpiId()
            );
            deliveryPartnerRepository.save(partner);
        }

        notificationService.sendToAdmin(
            "New User Registered",
            String.format("A new %s profile (#USR-%d) has registered with email %s.", role, user.getId(), user.getEmail()),
            "SYSTEM"
        );

        return "User registered successfully!";
    }

    public ProfileDTO getUserProfile(String email) {
        return getProfile(email);
    }

    public ProfileDTO getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: User not found!"));

        ProfileDTO dto = new ProfileDTO();
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setProfilePhoto(user.getProfilePhoto());

        String role = user.getRole();
        if ("FARMER".equals(role)) {
            Optional<Farmer> farmerOpt = farmerRepository.findByUserEmail(email);
            if (farmerOpt.isPresent()) {
                Farmer farmer = farmerOpt.get();
                dto.setName(farmer.getName());
                dto.setPhone(farmer.getPhone());
                dto.setState(farmer.getState());
                dto.setDistrict(farmer.getDistrict());
                dto.setFarmSizeAcres(farmer.getFarmSizeAcres());
                dto.setUpiId(farmer.getUpiId());
            }
        } else if ("BUYER".equals(role)) {
            Optional<Buyer> buyerOpt = buyerRepository.findByUserEmail(email);
            if (buyerOpt.isPresent()) {
                Buyer buyer = buyerOpt.get();
                dto.setCompanyName(buyer.getCompanyName());
                dto.setName(buyer.getRepresentativeName());
                dto.setPhone(buyer.getPhone());
                dto.setAddress(buyer.getAddress());
                dto.setGstin(buyer.getGstin());
            }
        } else if ("COLD_STORAGE_MANAGER".equals(role)) {
            Optional<ColdStorage> storageOpt = coldStorageRepository.findByManagerEmail(email);
            if (storageOpt.isPresent()) {
                ColdStorage storage = storageOpt.get();
                dto.setStorageName(storage.getName());
                dto.setStorageLocation(storage.getLocation());
                dto.setStorageCapacityTons(storage.getTotalCapacityTons());
                dto.setStoragePricePerTonDay(storage.getPricePerTonDay());
                dto.setStorageDescription(storage.getDescription());
            }
        } else if ("DELIVERY_PARTNER".equals(role)) {
            Optional<DeliveryPartner> partnerOpt = deliveryPartnerRepository.findByUserEmail(email);
            if (partnerOpt.isPresent()) {
                DeliveryPartner partner = partnerOpt.get();
                dto.setName(partner.getName());
                dto.setPhone(partner.getPhone());
                dto.setDistrict(partner.getDistrict());
                dto.setUpiId(partner.getUpiId());
            }
        }
        return dto;
    }

    @Transactional
    public void updateProfile(String email, ProfileUpdateRequest dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: User not found!"));

        user.setProfilePhoto(dto.getProfilePhoto());
        userRepository.save(user);

        String role = user.getRole();
        if ("FARMER".equals(role)) {
            Farmer farmer = farmerRepository.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Error: Farmer profile not found!"));
            farmer.updateDetails(dto.getName(), dto.getPhone(), dto.getState(), dto.getDistrict(), dto.getFarmSizeAcres(), dto.getUpiId());
            farmerRepository.save(farmer);
        } else if ("BUYER".equals(role)) {
            Buyer buyer = buyerRepository.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Error: Buyer profile not found!"));
            buyer.updateDetails(dto.getCompanyName(), dto.getName(), dto.getPhone(), dto.getAddress(), dto.getGstin());
            buyerRepository.save(buyer);
        } else if ("COLD_STORAGE_MANAGER".equals(role)) {
            ColdStorage storage = coldStorageRepository.findByManagerEmail(email)
                    .orElseThrow(() -> new RuntimeException("Error: Cold storage profile not found!"));
            storage.updateDetails(dto.getStorageName(), dto.getStorageLocation(), dto.getStorageCapacityTons(), dto.getStoragePricePerTonDay(), dto.getStorageDescription());
            coldStorageRepository.save(storage);
        } else if ("DELIVERY_PARTNER".equals(role)) {
            DeliveryPartner partner = deliveryPartnerRepository.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Error: Delivery partner profile not found!"));
            if (dto.getName() != null) partner.setName(dto.getName());
            if (dto.getPhone() != null) partner.setPhone(dto.getPhone());
            if (dto.getDistrict() != null) partner.setDistrict(dto.getDistrict());
            if (dto.getUpiId() != null) partner.setUpiId(dto.getUpiId());
            deliveryPartnerRepository.save(partner);
        }
    }

    public com.rtech.mandiplus.dto.OtpResponseDTO sendMobileOtp(String rawPhone, String role) {
        String phone = otpService.normalizePhoneNumber(rawPhone);
        com.rtech.mandiplus.entity.OtpChallenge challenge = otpService.sendOtp(phone);
        String masked = "+91 ******" + phone.substring(phone.length() - 4);

        String customMsg = "Real SMS OTP dispatched to your mobile number +91-" + phone + " via Fast2SMS. Please check your SMS inbox on your mobile phone.";

        return com.rtech.mandiplus.dto.OtpResponseDTO.success(
                masked,
                otpService.getCooldownSeconds(),
                otpService.getExpirySeconds(),
                null,
                challenge.isRealSmsDelivered(),
                customMsg
        );
    }

    @Transactional
    public JwtResponse verifyFirebaseTokenAndAuthenticate(String firebaseIdToken, String requestedRole) {
        if (firebaseTokenVerifier == null) {
            throw new RuntimeException("Firebase token verifier service is not configured.");
        }
        com.rtech.mandiplus.service.firebase.FirebaseTokenHolder tokenHolder = firebaseTokenVerifier.verifyIdToken(firebaseIdToken);
        String rawPhone = tokenHolder.getPhoneNumber();
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Firebase ID token must contain a verified phone_number claim.");
        }
        String phone = otpService != null ? otpService.normalizePhoneNumber(rawPhone) : rawPhone.replaceAll("[^0-9]", "");
        if (phone.length() > 10 && phone.startsWith("91")) {
            phone = phone.substring(2);
        }

        return authenticateVerifiedPhoneUser(phone, requestedRole, tokenHolder.getName(), tokenHolder.getEmail());
    }

    @Transactional
    public JwtResponse verifyOtpAndAuthenticate(String rawPhone, String otp, String requestedRole) {
        String phone = otpService != null ? otpService.normalizePhoneNumber(rawPhone) : rawPhone.replaceAll("[^0-9]", "");
        if (phone.length() > 10 && phone.startsWith("91")) {
            phone = phone.substring(2);
        }
        if (otpService != null) {
            otpService.verifyOtp(phone, otp);
        }
        return authenticateVerifiedPhoneUser(phone, requestedRole, null, null);
    }

    @Transactional
    public JwtResponse authenticateVerifiedPhoneUser(String phone, String requestedRole, String verifiedName, String verifiedEmail) {
        // Find or provision user
        Optional<User> existingUserOpt = userRepository.findByPhone(phone);
        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            // If requested role is FARMER, ensure the user has their single Farmer entity and history preserved
            if ("FARMER".equalsIgnoreCase(requestedRole)) {
                Optional<Farmer> farmerOpt = farmerRepository != null ? farmerRepository.findByUser(user) : Optional.empty();
                if (farmerOpt == null || farmerOpt.isEmpty()) {
                    farmerOpt = farmerRepository != null ? farmerRepository.findByPhone(phone) : Optional.empty();
                }
                if (farmerOpt != null && farmerOpt.isPresent()) {
                    Farmer f = farmerOpt.get();
                    if (!user.equals(f.getUser())) {
                        f.setUser(user);
                        farmerRepository.save(f);
                    }
                    if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                        user.setFullName(f.getName());
                    }
                    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                        user.setRole("FARMER");
                    }
                    user.setProfileCompleted(true);
                    User saved = userRepository.save(user);
                    if (saved != null) user = saved;
                } else if (!"FARMER".equalsIgnoreCase(user.getRole()) && farmerRepository != null) {
                    Farmer farmer = new Farmer();
                    farmer.setUser(user);
                    farmer.setName(user.getFullName() != null && !user.getFullName().trim().isEmpty() ? user.getFullName() : "Farmer " + phone.substring(phone.length() - 4));
                    farmer.setPhone(phone);
                    farmer.setState("Maharashtra");
                    farmer.setDistrict("Pune");
                    farmer.setFarmSizeAcres(java.math.BigDecimal.valueOf(15.0));
                    farmer.setUpiId(phone + "@upi");
                    farmerRepository.save(farmer);
                    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                        user.setRole("FARMER");
                        User saved = userRepository.save(user);
                        if (saved != null) user = saved;
                    }
                }
            }
        } else {
            // Check if phone matches an existing Farmer, Buyer, or Partner profile
            Optional<Farmer> farmerOpt = farmerRepository != null ? farmerRepository.findByPhone(phone) : Optional.empty();
            if (farmerOpt != null && farmerOpt.isPresent()) {
                User u = farmerOpt.get().getUser();
                u.setPhone(phone);
                u.setFullName(farmerOpt.get().getName());
                u.setProfileCompleted(true);
                User saved = userRepository.save(u);
                user = saved != null ? saved : u;
            } else {
                Optional<Buyer> buyerOpt = buyerRepository != null ? buyerRepository.findByPhone(phone) : Optional.empty();
                if (buyerOpt != null && buyerOpt.isPresent()) {
                    User u = buyerOpt.get().getUser();
                    u.setPhone(phone);
                    u.setFullName(buyerOpt.get().getRepresentativeName());
                    u.setProfileCompleted(true);
                    User saved = userRepository.save(u);
                    user = saved != null ? saved : u;
                } else {
                    Optional<DeliveryPartner> partnerOpt = deliveryPartnerRepository != null ? deliveryPartnerRepository.findByPhone(phone) : Optional.empty();
                    if (partnerOpt != null && partnerOpt.isPresent()) {
                        User u = partnerOpt.get().getUser();
                        u.setPhone(phone);
                        u.setFullName(partnerOpt.get().getName());
                        u.setProfileCompleted(true);
                        User saved = userRepository.save(u);
                        user = saved != null ? saved : u;
                    } else {
                        // New user registration via verified mobile
                        User newUser = new User();
                        String emailToUse = (verifiedEmail != null && !verifiedEmail.trim().isEmpty() && !userRepository.existsByEmail(verifiedEmail.trim()))
                                ? verifiedEmail.trim()
                                : "user_" + phone + "@mandiplus.app";
                        newUser.setEmail(emailToUse);
                        newUser.setPassword(encoder.encode(java.util.UUID.randomUUID().toString()));
                        // TC-ADMIN-008: Phone number or name alone cannot grant ADMIN role!
                        String safeRole = (requestedRole != null && !requestedRole.equalsIgnoreCase("ADMIN"))
                                ? requestedRole.toUpperCase() : "BUYER";
                        newUser.setRole(safeRole);
                        newUser.setPhone(phone);
                        if (verifiedName != null && !verifiedName.trim().isEmpty()) {
                            newUser.setFullName(verifiedName.trim());
                            newUser.setProfileCompleted(true);
                        } else {
                            newUser.setFullName(null);
                            newUser.setProfileCompleted(false);
                        }
                        newUser.setActive(true);
                        User saved = userRepository.save(newUser);
                        user = saved != null ? saved : newUser;

                        if ("FARMER".equalsIgnoreCase(safeRole) && farmerRepository != null) {
                            Farmer farmer = new Farmer();
                            farmer.setUser(user);
                            farmer.setName(user.getFullName() != null && !user.getFullName().trim().isEmpty() ? user.getFullName() : "Farmer " + phone.substring(phone.length() - 4));
                            farmer.setPhone(phone);
                            farmer.setState("Maharashtra");
                            farmer.setDistrict("Pune");
                            farmer.setFarmSizeAcres(java.math.BigDecimal.ZERO);
                            farmer.setUpiId(phone + "@upi");
                            farmerRepository.save(farmer);
                        }
                    }
                }
            }
        }

        // Ensure name is synced if exists in sub-profile
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            syncUserNameFromProfile(user);
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return new JwtResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePhoto(),
                user.getFullName(),
                user.getPhone(),
                user.isProfileComplete()
        );
    }

    @Transactional
    public JwtResponse setupProfileName(String email, String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required and cannot be empty.");
        }
        String trimmedName = fullName.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            throw new IllegalArgumentException("Full name must be between 2 and 100 characters.");
        }
        if (!trimmedName.matches("^[\\p{L}\\s\\.\\-\\']+$")) {
            throw new IllegalArgumentException("Full name contains invalid characters.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: Authenticated user not found in database!"));

        user.setFullName(trimmedName);
        user.setProfileCompleted(true);
        userRepository.save(user);

        // Synchronize name to role-specific profile entity
        String role = user.getRole();
        if ("FARMER".equals(role)) {
            farmerRepository.findByUserEmail(email).ifPresent(f -> {
                f.setName(trimmedName);
                farmerRepository.save(f);
            });
        } else if ("BUYER".equals(role)) {
            buyerRepository.findByUserEmail(email).ifPresent(b -> {
                b.setRepresentativeName(trimmedName);
                buyerRepository.save(b);
            });
        } else if ("DELIVERY_PARTNER".equals(role)) {
            deliveryPartnerRepository.findByUserEmail(email).ifPresent(p -> {
                p.setName(trimmedName);
                deliveryPartnerRepository.save(p);
            });
        } else if ("COLD_STORAGE_MANAGER".equals(role)) {
            coldStorageRepository.findByManagerEmail(email).ifPresent(s -> {
                s.setName(trimmedName);
                coldStorageRepository.save(s);
            });
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return new JwtResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePhoto(),
                user.getFullName(),
                user.getPhone(),
                true
        );
    }

    public void syncUserNameFromProfile(User user) {
        if (user == null || user.getEmail() == null) return;
        String role = user.getRole();
        if ("FARMER".equals(role)) {
            farmerRepository.findByUserEmail(user.getEmail()).ifPresent(f -> {
                if (f.getName() != null && !f.getName().trim().isEmpty()) {
                    user.setFullName(f.getName().trim());
                    user.setProfileCompleted(true);
                    userRepository.save(user);
                }
            });
        } else if ("BUYER".equals(role)) {
            buyerRepository.findByUserEmail(user.getEmail()).ifPresent(b -> {
                if (b.getRepresentativeName() != null && !b.getRepresentativeName().trim().isEmpty()) {
                    user.setFullName(b.getRepresentativeName().trim());
                    user.setProfileCompleted(true);
                    userRepository.save(user);
                }
            });
        } else if ("DELIVERY_PARTNER".equals(role)) {
            deliveryPartnerRepository.findByUserEmail(user.getEmail()).ifPresent(p -> {
                if (p.getName() != null && !p.getName().trim().isEmpty()) {
                    user.setFullName(p.getName().trim());
                    user.setProfileCompleted(true);
                    userRepository.save(user);
                }
            });
        } else if ("COLD_STORAGE_MANAGER".equals(role)) {
            coldStorageRepository.findByManagerEmail(user.getEmail()).ifPresent(s -> {
                if (s.getName() != null && !s.getName().trim().isEmpty()) {
                    user.setFullName(s.getName().trim());
                    user.setProfileCompleted(true);
                    userRepository.save(user);
                }
            });
        }
    }
}
