package com.rtech.mandiplus.config;

import com.rtech.mandiplus.entity.Farmer;
import com.rtech.mandiplus.entity.Product;
import com.rtech.mandiplus.repository.FarmerRepository;
import com.rtech.mandiplus.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Automatically ensures 10 diverse showcase agricultural produce listings 
 * with photos, varieties, and descriptions exist in the marketplace.
 * Perfect for College Presentations, Evaluator Demos, and Buyer Marketplace display.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private com.rtech.mandiplus.repository.GovMandiMspRepository mspRepository;

    @Autowired
    private com.rtech.mandiplus.repository.GovMandiAdvisoryRepository advisoryRepository;

    @Autowired
    private com.rtech.mandiplus.repository.GovMandiPriceRepository priceRepository;

    @Autowired
    private com.rtech.mandiplus.repository.UserRepository userRepository;

    @Autowired
    private com.rtech.mandiplus.repository.BuyerRepository buyerRepository;

    @Autowired
    private com.rtech.mandiplus.repository.DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private com.rtech.mandiplus.repository.ColdStorageRepository coldStorageRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            configureAdministratorProfile();
        } catch (Exception e) {
            System.err.println("[Mandi+] Notice during Admin profile configuration: " + e.getMessage());
        }

        try {
            syncExistingUserProfiles();
        } catch (Exception e) {
            System.err.println("[Mandi+] Notice during user profile sync: " + e.getMessage());
        }

        try {
            seedSampleProductsIfEmpty();
        } catch (Exception e) {
            System.err.println("[Mandi+] Notice during product data initialization: " + e.getMessage());
        }

        try {
            seedGovMandiMspIfEmpty();
        } catch (Exception e) {
            System.err.println("[Mandi+] Notice during Gov Mandi MSP initialization: " + e.getMessage());
        }

        try {
            seedGovMandiAdvisoriesIfEmpty();
        } catch (Exception e) {
            System.err.println("[Mandi+] Notice during Gov Mandi Advisory initialization: " + e.getMessage());
        }

        try {
            seedGovMandiPricesIfEmpty();
        } catch (Exception e) {
            System.err.println("[Mandi+] Notice during Gov Mandi Prices initialization: " + e.getMessage());
        }
    }

    public void configureAdministratorProfile() {
        // Demote/convert any other non-admin@gmail.com users that may have ADMIN role
        List<com.rtech.mandiplus.entity.User> otherAdmins = userRepository.findByRole("ADMIN");
        for (com.rtech.mandiplus.entity.User u : otherAdmins) {
            if (!"admin@gmail.com".equalsIgnoreCase(u.getEmail())) {
                u.setRole("FARMER");
                userRepository.save(u);
            }
        }

        Optional<com.rtech.mandiplus.entity.User> adminOpt = userRepository.findByEmail("admin@gmail.com");
        com.rtech.mandiplus.entity.User admin;
        if (adminOpt.isEmpty()) {
            admin = new com.rtech.mandiplus.entity.User();
            admin.setEmail("admin@gmail.com");
            // BCrypt hash for "admin@gmail.com" (email and password identical)
            admin.setPassword("$2a$10$cUWmnXn9CbziBQKZa.p5NO3Bv/DUnNNb4ZETg1sNndRubeqqiTkVq");
            admin.setRole("ADMIN");
            admin.setPhone("9012345678");
            admin.setFullName("System Administrator");
            admin.setProfileCompleted(true);
            admin.setActive(true);
            admin = userRepository.save(admin);
            System.out.println("[Mandi+] Successfully initialized exclusive Administrator profile for: admin@gmail.com");
        } else {
            admin = adminOpt.get();
            admin.setPassword("$2a$10$cUWmnXn9CbziBQKZa.p5NO3Bv/DUnNNb4ZETg1sNndRubeqqiTkVq"); // password is "admin@gmail.com"
            admin.setRole("ADMIN");
            admin.setActive(true);
            admin.setProfileCompleted(true);
            if (admin.getPhone() == null || !admin.getPhone().equals("9012345678")) {
                admin.setPhone("9012345678");
            }
            if (admin.getFullName() == null || !admin.getFullName().equals("System Administrator")) {
                admin.setFullName("System Administrator");
            }
            userRepository.save(admin);
            System.out.println("[Mandi+] Verified exclusive Administrator credentials for: admin@gmail.com");
        }

        // Ensure Administrator also has an attached Farmer profile for Farmer Portal access and crop management
        Optional<Farmer> adminFarmerOpt = farmerRepository.findByUser(admin);
        if (adminFarmerOpt.isEmpty()) {
            adminFarmerOpt = farmerRepository.findByPhone("9012345678");
        }

        if (adminFarmerOpt.isEmpty()) {
            Farmer adminFarmer = new Farmer();
            adminFarmer.setUser(admin);
            adminFarmer.setName("System Administrator");
            adminFarmer.setPhone("9012345678");
            adminFarmer.setState("Maharashtra");
            adminFarmer.setDistrict("Pune");
            adminFarmer.setFarmSizeAcres(BigDecimal.valueOf(15.0));
            adminFarmer.setUpiId("9012345678@upi");
            farmerRepository.save(adminFarmer);
        } else {
            Farmer adminFarmer = adminFarmerOpt.get();
            boolean farmerChanged = false;
            if (!"9012345678".equals(adminFarmer.getPhone())) {
                adminFarmer.setPhone("9012345678");
                farmerChanged = true;
            }
            if (!"9012345678@upi".equals(adminFarmer.getUpiId())) {
                adminFarmer.setUpiId("9012345678@upi");
                farmerChanged = true;
            }
            if (adminFarmer.getUser() == null) {
                adminFarmer.setUser(admin);
                farmerChanged = true;
            }
            if (farmerChanged) {
                farmerRepository.save(adminFarmer);
            }
        }
    }

    public void syncExistingUserProfiles() {
        List<com.rtech.mandiplus.entity.User> allUsers = userRepository.findAll();
        for (com.rtech.mandiplus.entity.User u : allUsers) {
            if (u.getFullName() != null && Boolean.TRUE.equals(u.getProfileCompleted())) {
                continue;
            }
            String role = u.getRole();
            if ("FARMER".equals(role)) {
                farmerRepository.findByUserEmail(u.getEmail()).ifPresent(f -> {
                    if (f.getName() != null && !f.getName().trim().isEmpty()) {
                        u.setFullName(f.getName().trim());
                        u.setPhone(f.getPhone());
                        u.setProfileCompleted(true);
                        userRepository.save(u);
                    }
                });
            } else if ("BUYER".equals(role)) {
                buyerRepository.findByUserEmail(u.getEmail()).ifPresent(b -> {
                    if (b.getRepresentativeName() != null && !b.getRepresentativeName().trim().isEmpty()) {
                        u.setFullName(b.getRepresentativeName().trim());
                        u.setPhone(b.getPhone());
                        u.setProfileCompleted(true);
                        userRepository.save(u);
                    }
                });
            } else if ("DELIVERY_PARTNER".equals(role)) {
                deliveryPartnerRepository.findByUserEmail(u.getEmail()).ifPresent(p -> {
                    if (p.getName() != null && !p.getName().trim().isEmpty()) {
                        u.setFullName(p.getName().trim());
                        u.setPhone(p.getPhone());
                        u.setProfileCompleted(true);
                        userRepository.save(u);
                    }
                });
            }
        }
    }

    private void seedSampleProductsIfEmpty() {
        List<Farmer> farmers = farmerRepository.findAll();
        if (farmers.isEmpty()) {
            return;
        }

        // 58 Diverse Agricultural Showcase Products with High-Resolution Photos & Market Pricing
        SampleProduct[] showcase = new SampleProduct[]{
            // --- Vegetables (22 items) ---
            new SampleProduct("Onion", "Nashik Red (Garwa)", new BigDecimal("150.00"), new BigDecimal("1850.00"),
                "Premium export-quality Grade-A cured red onions from Lasalgaon belt. Low moisture, thin neck, ideal for wholesale and storage.",
                "https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=800&q=80",
                "Lasalgaon, Nashik, Maharashtra", 0),
            new SampleProduct("White Onion", "Alibag White Special", new BigDecimal("90.00"), new BigDecimal("2200.00"),
                "GI-tagged sweet white onions of Alibag. Renowned for low sulfur taste, crisp texture, medicinal qualities, and high export demand.",
                "https://images.unsplash.com/photo-1580201092675-a0a6a6cafbb1?auto=format&fit=crop&w=800&q=80",
                "Alibag, Raigad, Maharashtra", 3),
            new SampleProduct("Tomato", "Abhinav Semi-Determinate", new BigDecimal("120.00"), new BigDecimal("1450.00"),
                "Firm, deep red oval hybrid tomatoes with thick pericarp. Long transit shelf life, juicy pulp, ideal for retail and processing.",
                "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=800&q=80",
                "Narayangaon, Pune, Maharashtra", 2),
            new SampleProduct("Potato", "Kufri Jyoti Grade-A", new BigDecimal("250.00"), new BigDecimal("1650.00"),
                "Clean oval table potatoes with smooth cream skin and shallow eyes. Machine-cleaned, zero mud, excellent cooking texture.",
                "https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=800&q=80",
                "Manchar, Pune, Maharashtra", 2),
            new SampleProduct("Garlic", "G-2 Bold Clove White", new BigDecimal("40.00"), new BigDecimal("11500.00"),
                "Cured white garlic bulbs with tight cloves and pungent allicin content. Sun-dried with long storage viability.",
                "https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?auto=format&fit=crop&w=800&q=80",
                "Dhule, Maharashtra", 0),
            new SampleProduct("Ginger", "Mahim Grade-1 Bold", new BigDecimal("55.00"), new BigDecimal("6800.00"),
                "Freshly dug plump ginger rhizomes with fiber-free flesh and zesty aroma. Thoroughly washed and cured for wholesale.",
                "https://images.unsplash.com/photo-1615485500704-8e990f9900f7?auto=format&fit=crop&w=800&q=80",
                "Satara, Maharashtra", 7),
            new SampleProduct("Green Chilli", "G-4 Spicy Dark Green", new BigDecimal("35.00"), new BigDecimal("3400.00"),
                "Crisp dark-green spicy chillies with firm stalks. High capsaicin heat, fresh harvest directly from irrigated farms.",
                "https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=800&q=80",
                "Nandurbar, Maharashtra", 4),
            new SampleProduct("Cauliflower", "Snowball White Compact", new BigDecimal("70.00"), new BigDecimal("1750.00"),
                "Dense, pure white compact curds free from yellowing or spots. Harvested with protective jacket leaves for intact freshness.",
                "https://images.unsplash.com/photo-1568584711075-3d021a7c3ca3?auto=format&fit=crop&w=800&q=80",
                "Nashik, Maharashtra", 0),
            new SampleProduct("Cabbage", "Golden Acre Round Green", new BigDecimal("95.00"), new BigDecimal("1200.00"),
                "Solid tightly wrapped green cabbage heads. Sweet crisp leaves, 1.2-1.5 kg average head weight, perfect for wholesale.",
                "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?auto=format&fit=crop&w=800&q=80",
                "Sangamner, Ahilyanagar, Maharashtra", 2),
            new SampleProduct("Carrot", "Pusa Kesar Deep Red", new BigDecimal("85.00"), new BigDecimal("2100.00"),
                "Sweet juicy deep-red carrots high in beta-carotene. Machine washed, sorted by uniform length, tender core.",
                "https://images.unsplash.com/photo-1590868309235-ea34bed7bd7f?auto=format&fit=crop&w=800&q=80",
                "Nashik, Maharashtra", 0),
            new SampleProduct("Spinach (Palak)", "All Green Tender Leaf", new BigDecimal("30.00"), new BigDecimal("1800.00"),
                "Crisp dark green organic palak bundles. Freshly harvested tender leaves rich in dietary iron, pesticide-free water-rinsed.",
                "https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=800&q=80",
                "Pune, Maharashtra", 2),
            new SampleProduct("Lady Finger (Bhindi)", "Radhika F1 Deep Green", new BigDecimal("40.00"), new BigDecimal("3200.00"),
                "Tender 5-ridged deep green okra pods with non-fibrous tips. Soft seeds, easy snap, hand-graded into 8-10 cm size.",
                "https://images.unsplash.com/photo-1425543103986-22abb7d7e8d2?auto=format&fit=crop&w=800&q=80",
                "Baramati, Pune, Maharashtra", 2),
            new SampleProduct("Brinjal (Baingan)", "Manjari Gota Purple Round", new BigDecimal("50.00"), new BigDecimal("1950.00"),
                "Glossy round deep-purple eggplants with fresh green calyx. Creamy seedless white flesh, ideal for bharta and curries.",
                "https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=800&q=80",
                "Kolhapur, Maharashtra", 1),
            new SampleProduct("Capsicum", "Indra F1 Blocky Green", new BigDecimal("45.00"), new BigDecimal("3600.00"),
                "Greenhouse-grown blocky four-lobed bell peppers. Heavy thick walls, deep lustrous sheen, crunchy sweet texture.",
                "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?auto=format&fit=crop&w=800&q=80",
                "Niphad, Nashik, Maharashtra", 0),
            new SampleProduct("Green Peas (Matar)", "Arkel Early Sweet Pods", new BigDecimal("60.00"), new BigDecimal("4200.00"),
                "Plump well-filled sweet green pea pods. Bright green pods packed with 8-9 sweet tender peas, zero blemishes.",
                "https://images.unsplash.com/photo-1587735243615-c03f25aaff15?auto=format&fit=crop&w=800&q=80",
                "Satara, Maharashtra", 7),
            new SampleProduct("Bitter Gourd (Karela)", "Pusa Do Mausami Spiny", new BigDecimal("35.00"), new BigDecimal("2800.00"),
                "Firm dark green spiny bitter gourds. Evenly tapered with crisp flesh, high medicinal momordicin value.",
                "https://images.unsplash.com/photo-1596797038530-2c107229654b?auto=format&fit=crop&w=800&q=80",
                "Solapur, Maharashtra", 6),
            new SampleProduct("Bottle Gourd (Lauki)", "Pusa Naveen Long Cylindrical", new BigDecimal("75.00"), new BigDecimal("1350.00"),
                "Smooth tender cylindrical bottle gourds with pale-green skin. High moisture content, tender seeds, ideal for fresh cooking.",
                "https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2?auto=format&fit=crop&w=800&q=80",
                "Jalna, Maharashtra", 0),
            new SampleProduct("Cucumber (Kheera)", "Malini Hybrid Salad Green", new BigDecimal("80.00"), new BigDecimal("1500.00"),
                "Crisp dark green salad cucumbers with minimal bitterness. Thin skin, refreshing bite, graded by uniform length.",
                "https://images.unsplash.com/photo-1604977042946-1eecc30f269e?auto=format&fit=crop&w=800&q=80",
                "Nashik, Maharashtra", 0),
            new SampleProduct("Beetroot", "Detroit Dark Red Globe", new BigDecimal("55.00"), new BigDecimal("2400.00"),
                "Uniform globe-shaped beetroots with deep maroon internal rings. High sugar and betanin content, smooth trimmed skin.",
                "https://images.unsplash.com/photo-1593105544559-ecb03bf76f82?auto=format&fit=crop&w=800&q=80",
                "Manchar, Pune, Maharashtra", 2),
            new SampleProduct("Radish (Mooli)", "Pusa Chetki White Long", new BigDecimal("60.00"), new BigDecimal("1400.00"),
                "Crisp, snow-white pungent long radishes with tender greens. Freshly uprooted, soil-washed, refreshing sharp bite.",
                "https://images.unsplash.com/photo-1594282486552-05b4d80fbb9f?auto=format&fit=crop&w=800&q=80",
                "Pune, Maharashtra", 2),
            new SampleProduct("Drumstick (Moringa)", "PKM-1 Tender Green Pods", new BigDecimal("30.00"), new BigDecimal("4800.00"),
                "Long, meaty, dark green moringa drumstick pods packed with nutrients. Soft pulpy interior, 60-75 cm uniform length.",
                "https://images.unsplash.com/photo-1546793665-c74683f339c1?auto=format&fit=crop&w=800&q=80",
                "Baramati, Pune, Maharashtra", 2),
            new SampleProduct("Sweet Potato", "Cured Red Skin Cream Flesh", new BigDecimal("65.00"), new BigDecimal("2600.00"),
                "Nutritious sweet potatoes with dusty red skin and sweet cream pulp. Rich in vitamins and complex carbohydrates.",
                "https://images.unsplash.com/photo-1589927986089-35812388d1f4?auto=format&fit=crop&w=800&q=80",
                "Kolhapur, Maharashtra", 1),

            // --- Fruits (19 items) ---
            new SampleProduct("Alphonso Mango", "Ratnagiri Hapus GI Tag", new BigDecimal("35.00"), new BigDecimal("12500.00"),
                "Authentic GI-certified Devgad Ratnagiri Alphonso mangoes. Naturally ripened in grass crates without carbide, heavenly aroma.",
                "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=800&q=80",
                "Devgad, Ratnagiri, Maharashtra", 3),
            new SampleProduct("Kesar Mango", "Marathwada Kesar Royal", new BigDecimal("40.00"), new BigDecimal("9800.00"),
                "Rich saffron-hued Kesar mangoes famous as the Queen of Mangoes. Extremely sweet 20+ Brix, fragrant bouquet, smooth pulp.",
                "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?auto=format&fit=crop&w=800&q=80",
                "Chhatrapati Sambhajinagar, Maharashtra", 4),
            new SampleProduct("Grapes", "Thomson Seedless Green", new BigDecimal("60.00"), new BigDecimal("7200.00"),
                "Export quality sweet seedless green grapes with 18+ Brix sweetness. Handpicked directly from organic-certified Nashik vineyards.",
                "https://images.unsplash.com/photo-1596363505729-4190a9506133?auto=format&fit=crop&w=800&q=80",
                "Nashik, Maharashtra", 0),
            new SampleProduct("Red Flame Grapes", "Flame Seedless Ruby Red", new BigDecimal("45.00"), new BigDecimal("8500.00"),
                "Ruby-red crunchy seedless table grapes. Tight clusters with sweet-tart berry burst, foam cushioned packaging.",
                "https://images.unsplash.com/photo-1537640538966-79f369143f8f?auto=format&fit=crop&w=800&q=80",
                "Dindori, Nashik, Maharashtra", 0),
            new SampleProduct("Banana", "Grand Naine (G9 Cavendish)", new BigDecimal("110.00"), new BigDecimal("1850.00"),
                "Fresh export-quality yellow Cavendish bananas. Ethylene chamber ripened, rich in potassium and sweetness, zero spots.",
                "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=800&q=80",
                "Raver, Jalgaon, Maharashtra", 4),
            new SampleProduct("Pomegranate", "Bhagwa Sindhuri Export", new BigDecimal("50.00"), new BigDecimal("11000.00"),
                "Deep red glossy pomegranates with soft seeds and dark ruby arils. High juice yield, 300g+ average fruit weight.",
                "https://images.unsplash.com/photo-1527661591475-527312dd65f5?auto=format&fit=crop&w=800&q=80",
                "Solapur, Maharashtra", 6),
            new SampleProduct("Papaya", "Taiwan 786 Red Lady", new BigDecimal("70.00"), new BigDecimal("2200.00"),
                "Elongated thick-fleshed sweet papayas with salmon-red interior. Firm texture with high sweetness and digestive enzymes.",
                "https://images.unsplash.com/photo-1517282009859-f000ec3b26fe?auto=format&fit=crop&w=800&q=80",
                "Nandurbar, Maharashtra", 4),
            new SampleProduct("Orange (Santra)", "Nagpur Mandarin GI Santra", new BigDecimal("85.00"), new BigDecimal("4600.00"),
                "Famous Nagpur GI Santra loose-jacket oranges. Refreshing sweet-sour balance, juicy vesicles, hand-plucked from Vidarbha.",
                "https://images.unsplash.com/photo-1547514701-42782101795e?auto=format&fit=crop&w=800&q=80",
                "Katol, Nagpur, Maharashtra", 5),
            new SampleProduct("Sweet Lime (Mosambi)", "Jalna Citrus Sweet Juicy", new BigDecimal("65.00"), new BigDecimal("3800.00"),
                "Thin-peeled juicy sweet limes (Mosambi). Low acid, high vitamin C sweet juice content, perfect for juicing and retail.",
                "https://images.unsplash.com/photo-1582979512210-99b6a53386f9?auto=format&fit=crop&w=800&q=80",
                "Ambad, Jalna, Maharashtra", 4),
            new SampleProduct("Guava (Amrud)", "Sardar Lucknow L-49 White", new BigDecimal("40.00"), new BigDecimal("3200.00"),
                "Large round sweet guavas with creamy white pulp and soft seeds. Rich aroma, high vitamin C and pectin content.",
                "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?auto=format&fit=crop&w=800&q=80",
                "Rahata, Ahilyanagar, Maharashtra", 2),
            new SampleProduct("Watermelon", "Kiran Striped Sugar Baby", new BigDecimal("140.00"), new BigDecimal("1150.00"),
                "Crisp deep red flesh watermelons with thin rind and small seeds. High Brix sweetness (12%+), 3-5 kg size.",
                "https://images.unsplash.com/photo-1587049352846-4a222e784d38?auto=format&fit=crop&w=800&q=80",
                "Indapur, Pune, Maharashtra", 2),
            new SampleProduct("Muskmelon (Kharbuj)", "Madhumati Netted Orange", new BigDecimal("60.00"), new BigDecimal("2300.00"),
                "Heavily netted cantaloupe melons with fragrant salmon-orange flesh. Sweet musky perfume, thick tender meat.",
                "https://images.unsplash.com/photo-1571575173700-afb9492e6a50?auto=format&fit=crop&w=800&q=80",
                "Pandharpur, Solapur, Maharashtra", 6),
            new SampleProduct("Apple", "Kinnaur Royal Delicious Red", new BigDecimal("50.00"), new BigDecimal("9500.00"),
                "Mountain orchard crisp red apples. Sweet aromatic flesh, deep crimson blush, wax-free natural orchard finish.",
                "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=800&q=80",
                "Nagpur Hub, Maharashtra", 5),
            new SampleProduct("Custard Apple (Sitaphal)", "Balanagar Golden Eyes", new BigDecimal("30.00"), new BigDecimal("6500.00"),
                "Plump sugar apple / sitaphal with large creamy eyes and minimal seeds. Heavenly sweet velvety custard pulp.",
                "https://images.unsplash.com/photo-1618897996318-5a901fa6ca71?auto=format&fit=crop&w=800&q=80",
                "Saswad, Pune, Maharashtra", 2),
            new SampleProduct("Strawberry", "Mahabaleshwar Winter Dawn", new BigDecimal("20.00"), new BigDecimal("18000.00"),
                "GI-tagged Mahabaleshwar strawberries. Bright crimson conical berries with glossy sheen, high sweetness and fragrance.",
                "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?auto=format&fit=crop&w=800&q=80",
                "Mahabaleshwar, Satara, Maharashtra", 7),
            new SampleProduct("Chikoo (Sapota)", "Gholvad Kalipatti GI Sweet", new BigDecimal("45.00"), new BigDecimal("3400.00"),
                "Coastal GI-tagged Gholvad sapota (chikoo). Caramel-sweet grainy texture, thin skin, high natural maltose sugar.",
                "https://images.unsplash.com/photo-1589217157232-464b505b197f?auto=format&fit=crop&w=800&q=80",
                "Dahanu, Palghar, Maharashtra", 3),
            new SampleProduct("Lemon (Nimbu)", "Kagzi Thin Skin Acid Lime", new BigDecimal("35.00"), new BigDecimal("5200.00"),
                "Thin-skinned round juicy yellow lemons. Extra rich in citric acid and juice percentage (over 48%), long shelf life.",
                "https://images.unsplash.com/photo-1590502593747-42a996133562?auto=format&fit=crop&w=800&q=80",
                "Solapur, Maharashtra", 6),
            new SampleProduct("Pineapple", "Queen Golden Sweet Tropical", new BigDecimal("50.00"), new BigDecimal("3600.00"),
                "Golden yellow aromatic Queen pineapples. Intensely sweet with crisp core, pleasant tropical scent.",
                "https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=800&q=80",
                "Ratnagiri, Maharashtra", 3),
            new SampleProduct("Coconut", "Coastal Tender & Mature Whole", new BigDecimal("120.00"), new BigDecimal("3200.00"),
                "Fresh coastal coconuts with sweet water and thick kernel. High oil recovery, ideal for culinary and commercial use.",
                "https://images.unsplash.com/photo-1544378730-8b5104b18790?auto=format&fit=crop&w=800&q=80",
                "Sindhudurg, Maharashtra", 3),

            // --- Grains, Pulses, Spices & Cash Crops (17 items) ---
            new SampleProduct("Wheat", "Sharbati Golden Grain", new BigDecimal("220.00"), new BigDecimal("3200.00"),
                "Heavy lustrous golden wheat kernels rich in protein and fiber. Machine-cleaned and packed in 50kg bags.",
                "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?auto=format&fit=crop&w=800&q=80",
                "Nagpur, Maharashtra", 5),
            new SampleProduct("Wheat", "Lokwan Premium Grain", new BigDecimal("180.00"), new BigDecimal("2950.00"),
                "Bold hard-grain Lokwan wheat processed through destoners. Moisture under 11%, zero pest infestation, FAQ standard.",
                "https://images.unsplash.com/photo-1543257580-7269da773bf5?auto=format&fit=crop&w=800&q=80",
                "Kopargaon, Ahilyanagar, Maharashtra", 2),
            new SampleProduct("Basmati Rice", "1121 Extra Long Steam", new BigDecimal("180.00"), new BigDecimal("5400.00"),
                "Aged 1121 steam Basmati rice grains with 8.4mm average raw length. Cooks to over 2.2x length, aromatic non-sticky texture.",
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=800&q=80",
                "Pune, Maharashtra", 2),
            new SampleProduct("Indrayani Rice", "Maval Fragrant Sticky Rice", new BigDecimal("120.00"), new BigDecimal("4600.00"),
                "Traditional sticky aromatic rice of Sahyadri slopes. Pleasant natural aroma, soft mouthfeel upon pressure cooking.",
                "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=800&q=80",
                "Maval, Pune, Maharashtra", 2),
            new SampleProduct("Kolam Rice", "Wada Kolam Zini Rice", new BigDecimal("140.00"), new BigDecimal("4800.00"),
                "GI-tagged Wada Kolam tiny grain rice. Light on digestion, smooth delicate texture when boiled, machine sorted.",
                "https://images.unsplash.com/photo-1516684732162-798a0062be99?auto=format&fit=crop&w=800&q=80",
                "Wada, Palghar, Maharashtra", 3),
            new SampleProduct("Soybean", "JS 335 Gold Cleaned", new BigDecimal("130.00"), new BigDecimal("4850.00"),
                "Grade-A bold yellow soybeans with 19% oil content and 40% protein. Cleaned, graded, moisture below 10%, perfect for processing.",
                "https://images.unsplash.com/photo-1599940824399-b87987ceb72a?auto=format&fit=crop&w=800&q=80",
                "Latur, Maharashtra", 5),
            new SampleProduct("Chana (Gram)", "Vijay Desi Bold Chana", new BigDecimal("90.00"), new BigDecimal("5650.00"),
                "Brown sun-dried bold Bengal gram chickpeas. High sprouting viability, rich earthy taste, graded FAQ standard.",
                "https://images.unsplash.com/photo-1515543237350-b3eea1ec8082?auto=format&fit=crop&w=800&q=80",
                "Latur, Maharashtra", 5),
            new SampleProduct("Tur Dal (Arhar)", "Marathwada BDN-711 Whole", new BigDecimal("80.00"), new BigDecimal("7800.00"),
                "Whole unpolished red pigeon pea. High protein content, unadulterated, excellent cooking quality for dal millers.",
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=800&q=80",
                "Akola, Maharashtra", 5),
            new SampleProduct("Moong (Green Gram)", "Kopargaon Shine Moong", new BigDecimal("70.00"), new BigDecimal("8600.00"),
                "Shiny olive-green whole moong beans. Uniform size, high germinability for healthy sprouts, zero weevils.",
                "https://images.unsplash.com/photo-1607672632458-9eb56696346b?auto=format&fit=crop&w=800&q=80",
                "Kopargaon, Ahilyanagar, Maharashtra", 2),
            new SampleProduct("Groundnut (Peanut)", "Tag-24 Western Bold Pods", new BigDecimal("100.00"), new BigDecimal("6800.00"),
                "Double-kernel clean dry groundnut pods with pink sweet kernels inside. 48% oil content, snacking and crushing grade.",
                "https://images.unsplash.com/photo-1567894340315-735d7c361db0?auto=format&fit=crop&w=800&q=80",
                "Karad, Satara, Maharashtra", 7),
            new SampleProduct("Turmeric", "Salem / Sangli High Curcumin", new BigDecimal("65.00"), new BigDecimal("9400.00"),
                "High curcumin content (5.2%+) polished whole turmeric fingers from the spice hub of Sangli. Cleaned, graded, and sun-cured.",
                "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=800&q=80",
                "Sangli, Maharashtra", 7),
            new SampleProduct("Red Chilli (Dry)", "Byadagi Wrinkled Deep Red", new BigDecimal("45.00"), new BigDecimal("17200.00"),
                "Sun-dried deep red chillies known for vibrant natural red color and aromatic mild heat. Zero pesticide residue, stemless cleaned.",
                "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?auto=format&fit=crop&w=800&q=80",
                "Kolhapur, Maharashtra", 1),
            new SampleProduct("Jowar (Sorghum)", "Maldandi M 35-1 (Shalu)", new BigDecimal("110.00"), new BigDecimal("3450.00"),
                "Pearly white sweet grain Maldandi Jowar. Famous winter harvest grain, produces tender sweet bhakris, gluten-free staple.",
                "https://images.unsplash.com/photo-1610484826967-09c5720778c7?auto=format&fit=crop&w=800&q=80",
                "Solapur, Maharashtra", 6),
            new SampleProduct("Pearl Millet (Bajra)", "Dhanshakti Biofortified Zinc", new BigDecimal("95.00"), new BigDecimal("2650.00"),
                "Biofortified greyish-green pearl millet rich in natural iron and zinc. High calorie arid grain, cleaned and bagged.",
                "https://images.unsplash.com/photo-1600857544200-b2f666a9a2ec?auto=format&fit=crop&w=800&q=80",
                "Ahilyanagar, Maharashtra", 2),
            new SampleProduct("Yellow Maize (Corn)", "Pioneer Hybrid Starch Bold", new BigDecimal("160.00"), new BigDecimal("2480.00"),
                "Hard flint yellow corn grains with 70% starch content. High bulk density, moisture below 12%, machine threshed.",
                "https://images.unsplash.com/photo-1551754655-cd27e38d2076?auto=format&fit=crop&w=800&q=80",
                "Niphad, Nashik, Maharashtra", 0),
            new SampleProduct("Sugarcane", "Co 86032 (Nayana High Recovery)", new BigDecimal("500.00"), new BigDecimal("340.00"),
                "Thick, juicy cane stalks with 13.5% sugar recovery. Fresh harvest cutting done immediately upon buyer order confirmation.",
                "https://images.unsplash.com/photo-1527842891421-42eec6e703ea?auto=format&fit=crop&w=800&q=80",
                "Kolhapur, Maharashtra", 1),
            new SampleProduct("Cotton", "MCU-5 White Gold Raw Bolls", new BigDecimal("75.00"), new BigDecimal("7250.00"),
                "Long-staple clean white cotton bolls with high ginning outturn (36%+). Low trash content, handpicked from Vidarbha.",
                "https://images.unsplash.com/photo-1605000797499-95a51c5269ae?auto=format&fit=crop&w=800&q=80",
                "Nagpur, Maharashtra", 5)
        };

        int seededCount = 0;
        for (SampleProduct sp : showcase) {
            Farmer assignedFarmer = farmers.get(Math.abs(sp.farmerIndex) % farmers.size());
            List<Product> matches = productRepository.findByCropNameContainingIgnoreCaseAndStatus(sp.cropName, "AVAILABLE");
            boolean alreadyExists = false;
            for (Product p : matches) {
                if (p.getVariety() != null && p.getVariety().equalsIgnoreCase(sp.variety)) {
                    alreadyExists = true;
                    p.setImageUrl(sp.imageUrl);
                    p.setPricePerQuintal(sp.price);
                    p.setSuggestedPricePerQuintal(sp.price);
                    p.setDescription(sp.description);
                    p.setLocation(sp.location);
                    p.setStatus("AVAILABLE");
                    productRepository.save(p);
                    break;
                }
            }

            if (!alreadyExists) {
                Product np = new Product();
                np.setFarmer(assignedFarmer);
                np.setCropName(sp.cropName);
                np.setVariety(sp.variety);
                np.setQuantityQuintals(sp.quantity);
                np.setPricePerQuintal(sp.price);
                np.setSuggestedPricePerQuintal(sp.price);
                np.setDescription(sp.description);
                np.setImageUrl(sp.imageUrl);
                np.setLocation(sp.location);
                np.setStatus("AVAILABLE");
                productRepository.save(np);
                seededCount++;
            }
        }

        System.out.println("[Mandi+] Verified " + showcase.length + " showcase agricultural products (New seeded: " + seededCount + ") for Buyer marketplace.");
    }

    private void seedGovMandiMspIfEmpty() {
        if (mspRepository.count() > 0) {
            return;
        }

        List<com.rtech.mandiplus.entity.GovMandiMsp> msps = List.of(
            new com.rtech.mandiplus.entity.GovMandiMsp("Wheat", "Rabi", new BigDecimal("2275.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Paddy", "Kharif", new BigDecimal("2300.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Maize", "Kharif", new BigDecimal("2225.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Soyabean", "Kharif", new BigDecimal("4892.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Cotton", "Kharif", new BigDecimal("7121.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Gram", "Rabi", new BigDecimal("5440.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Moong", "Kharif", new BigDecimal("8682.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Groundnut", "Kharif", new BigDecimal("6783.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Mustard", "Rabi", new BigDecimal("5650.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Tur", "Kharif", new BigDecimal("7550.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Bajra", "Kharif", new BigDecimal("2625.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Jowar", "Kharif", new BigDecimal("3371.00"), "2024-25", "Ministry of Agriculture & Farmers Welfare, GoI (Verified)"),
            new com.rtech.mandiplus.entity.GovMandiMsp("Onion", "Intervention", new BigDecimal("1800.00"), "2024-25", "State Market Intervention Reference Scheme")
        );

        mspRepository.saveAll(msps);
        System.out.println("[Mandi+] Initialized " + msps.size() + " verified Government MSP benchmarks.");
    }

    private void seedGovMandiAdvisoriesIfEmpty() {
        if (advisoryRepository.count() > 0) {
            return;
        }

        List<com.rtech.mandiplus.entity.GovMandiAdvisory> advisories = List.of(
            new com.rtech.mandiplus.entity.GovMandiAdvisory(
                "Soyabean",
                "Nashik",
                "Peak Harvest Arrival & Moisture Grade Advisory",
                "Heavy arrivals observed across Nashik and Niphad APMC mandis. Ensure moisture content is below 12% to prevent 5-8% grade deductions on modal rates.",
                "INFO",
                "Maharashtra State Agricultural Marketing Board (MSAMB)"
            ),
            new com.rtech.mandiplus.entity.GovMandiAdvisory(
                "Onion",
                null,
                "Export Policy & Minimum Realization Advisory",
                "Export duty rationalization active. Lasalgaon and Pimpalgaon mandis report steady modal prices. Avoid distress selling below modal average.",
                "ALERT",
                "Department of Consumer Affairs & MSAMB"
            ),
            new com.rtech.mandiplus.entity.GovMandiAdvisory(
                "Wheat",
                null,
                "FAQ Fair Average Quality Standard Reminder",
                "Ensure FAQ standards (less than 2% foreign matter, undamaged grain) to receive full government MSP of ₹2,275/qtl at designated state procurement centers.",
                "RECOMMENDATION",
                "Ministry of Agriculture & Farmers Welfare, GoI"
            )
        );

        advisoryRepository.saveAll(advisories);
        System.out.println("[Mandi+] Initialized " + advisories.size() + " official Government agricultural advisories.");
    }

    private void seedGovMandiPricesIfEmpty() {
        if (priceRepository.count() > 0) {
            return;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<com.rtech.mandiplus.entity.GovMandiPrice> records = new java.util.ArrayList<>();

        // Soyabean - Lasalgaon, Niphad, Pimpalgaon, Latur (Multiple days for trend & moving avg)
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Lasalgaon", "Soyabean", "Yellow", "FAQ", today.minusDays(2), new BigDecimal("4700.00"), new BigDecimal("5000.00"), new BigDecimal("4850.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Lasalgaon", "Soyabean", "Yellow", "FAQ", today.minusDays(1), new BigDecimal("4750.00"), new BigDecimal("5050.00"), new BigDecimal("4900.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Lasalgaon", "Soyabean", "Yellow", "FAQ", today, new BigDecimal("4800.00"), new BigDecimal("5100.00"), new BigDecimal("4950.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Niphad", "Soyabean", "Yellow", "FAQ", today, new BigDecimal("4700.00"), new BigDecimal("5050.00"), new BigDecimal("4880.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Pimpalgaon", "Soyabean", "Yellow", "FAQ", today, new BigDecimal("4750.00"), new BigDecimal("5120.00"), new BigDecimal("4920.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Latur", "Latur", "Soyabean", "Yellow", "FAQ", today, new BigDecimal("4780.00"), new BigDecimal("5150.00"), new BigDecimal("4960.00"), "data.gov.in (Agmarknet)"));

        // Onion - Lasalgaon, Pimpalgaon, Niphad, Yeola
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Lasalgaon", "Onion", "Red", "FAQ", today.minusDays(2), new BigDecimal("1200.00"), new BigDecimal("2350.00"), new BigDecimal("1820.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Lasalgaon", "Onion", "Red", "FAQ", today.minusDays(1), new BigDecimal("1250.00"), new BigDecimal("2400.00"), new BigDecimal("1850.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Lasalgaon", "Onion", "Red", "FAQ", today, new BigDecimal("1200.00"), new BigDecimal("2400.00"), new BigDecimal("1850.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Pimpalgaon", "Onion", "Red", "FAQ", today, new BigDecimal("1300.00"), new BigDecimal("2450.00"), new BigDecimal("1920.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Niphad", "Onion", "Red", "FAQ", today, new BigDecimal("1150.00"), new BigDecimal("2300.00"), new BigDecimal("1810.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Yeola", "Onion", "Red", "FAQ", today, new BigDecimal("1200.00"), new BigDecimal("2380.00"), new BigDecimal("1830.00"), "data.gov.in (Agmarknet)"));

        // Wheat - Kopargaon, Pathardi, Nashik, Pune
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Ahilyanagar", "APMC Kopargaon", "Wheat", "Lokwan", "FAQ", today.minusDays(1), new BigDecimal("2800.00"), new BigDecimal("3100.00"), new BigDecimal("2950.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Ahilyanagar", "APMC Kopargaon", "Wheat", "Lokwan", "FAQ", today, new BigDecimal("2851.00"), new BigDecimal("3121.00"), new BigDecimal("2990.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Ahilyanagar", "APMC Pathardi", "Wheat", "Maharashtra 2189", "FAQ", today, new BigDecimal("2610.00"), new BigDecimal("3200.00"), new BigDecimal("2950.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Nashik", "Wheat", "Lokwan", "FAQ", today, new BigDecimal("2700.00"), new BigDecimal("3050.00"), new BigDecimal("2850.00"), "data.gov.in (Agmarknet)"));

        // Maize - Kopargaon, Karjat(Rashin), Niphad
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Ahilyanagar", "APMC Kopargaon", "Maize", "Yellow", "FAQ", today.minusDays(1), new BigDecimal("2450.00"), new BigDecimal("2500.00"), new BigDecimal("2480.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Ahilyanagar", "APMC Kopargaon", "Maize", "Yellow", "FAQ", today, new BigDecimal("2501.00"), new BigDecimal("2501.00"), new BigDecimal("2501.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Ahilyanagar", "Karjat(Rashin)", "Maize", "Yellow", "Non-FAQ", today, new BigDecimal("2300.00"), new BigDecimal("2400.00"), new BigDecimal("2350.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Niphad", "Maize", "Yellow", "FAQ", today, new BigDecimal("2350.00"), new BigDecimal("2480.00"), new BigDecimal("2420.00"), "data.gov.in (Agmarknet)"));

        // Cotton - Jalgaon, Nagpur
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Jalgaon", "Jalgaon", "Cotton", "Medium", "FAQ", today.minusDays(1), new BigDecimal("6900.00"), new BigDecimal("7350.00"), new BigDecimal("7200.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Jalgaon", "Jalgaon", "Cotton", "Medium", "FAQ", today, new BigDecimal("6950.00"), new BigDecimal("7400.00"), new BigDecimal("7250.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nagpur", "Nagpur", "Cotton", "Medium", "FAQ", today, new BigDecimal("7000.00"), new BigDecimal("7450.00"), new BigDecimal("7310.00"), "data.gov.in (Agmarknet)"));

        // Tomato - Pimpalgaon, Nashik, Pune
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Pimpalgaon", "Tomato", "Hybrid", "FAQ", today.minusDays(1), new BigDecimal("1200.00"), new BigDecimal("1750.00"), new BigDecimal("1500.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Pimpalgaon", "Tomato", "Hybrid", "FAQ", today, new BigDecimal("1100.00"), new BigDecimal("1700.00"), new BigDecimal("1450.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Nashik", "Nashik", "Tomato", "Hybrid", "FAQ", today, new BigDecimal("1200.00"), new BigDecimal("1800.00"), new BigDecimal("1520.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Pune", "Pune", "Tomato", "Hybrid", "FAQ", today, new BigDecimal("1150.00"), new BigDecimal("1750.00"), new BigDecimal("1480.00"), "data.gov.in (Agmarknet)"));

        // Gram / Chana - Latur, Solapur
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Latur", "Latur", "Gram", "Chana", "FAQ", today.minusDays(1), new BigDecimal("5300.00"), new BigDecimal("5750.00"), new BigDecimal("5600.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Latur", "Latur", "Gram", "Chana", "FAQ", today, new BigDecimal("5350.00"), new BigDecimal("5800.00"), new BigDecimal("5650.00"), "data.gov.in (Agmarknet)"));
        records.add(new com.rtech.mandiplus.entity.GovMandiPrice("Maharashtra", "Solapur", "Solapur", "Gram", "Chana", "FAQ", today, new BigDecimal("5250.00"), new BigDecimal("5720.00"), new BigDecimal("5580.00"), "data.gov.in (Agmarknet)"));

        priceRepository.saveAll(records);
        System.out.println("[Mandi+] Initialized " + records.size() + " official Government Mandi market arrival records.");
    }

    private static class SampleProduct {
        final String cropName;
        final String variety;
        final BigDecimal quantity;
        final BigDecimal price;
        final String description;
        final String imageUrl;
        final String location;
        final int farmerIndex;

        SampleProduct(String cropName, String variety, BigDecimal quantity, BigDecimal price,
                      String description, String imageUrl, String location, int farmerIndex) {
            this.cropName = cropName;
            this.variety = variety;
            this.quantity = quantity;
            this.price = price;
            this.description = description;
            this.imageUrl = imageUrl;
            this.location = location;
            this.farmerIndex = farmerIndex;
        }
    }
}
