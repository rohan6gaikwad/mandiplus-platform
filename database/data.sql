-- Mandi+ Smart Agriculture Marketplace Seed Data (Maharashtra Focused)


USE mandiplus_db;

-- Clear old data in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM delivery_tasks;
DELETE FROM delivery_partners;
DELETE FROM payments;
DELETE FROM orders;
DELETE FROM complaints;
DELETE FROM weather;
DELETE FROM mandi_prices;
DELETE FROM storage_bookings;
DELETE FROM cold_storage;
DELETE FROM products;
DELETE FROM buyers;
DELETE FROM farmers;
DELETE FROM users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Populate Users
INSERT INTO users (id, email, password, role, phone, full_name, profile_completed, active) VALUES
(1, 'farmer@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'FARMER', '9822012345', 'Rohan Gaikwad', TRUE, TRUE),
(2, 'buyer@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'BUYER', '9876543211', 'Krushnal Chaudhari', TRUE, TRUE),
(3, 'storage@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'COLD_STORAGE_MANAGER', '9822054321', 'Storage Manager', TRUE, TRUE),
(4, 'admin@gmail.com', '$2a$10$cUWmnXn9CbziBQKZa.p5NO3Bv/DUnNNb4ZETg1sNndRubeqqiTkVq', 'ADMIN', '9012345678', 'System Administrator', TRUE, TRUE),
(5, 'delivery@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'DELIVERY_PARTNER', '9822099999', 'Rahul Shinde', TRUE, TRUE),
(101, 'suresh.patil@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'FARMER', '9822100001', 'Suresh Patil', TRUE, TRUE),
(102, 'balasaheb.jadhav@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'FARMER', '9822100002', 'Balasaheb Jadhav', TRUE, TRUE),
(103, 'ganesh.kadam@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'FARMER', '9822100003', 'Ganesh Kadam', TRUE, TRUE),
(104, 'sunil.chavan@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'FARMER', '9822100004', 'Sunil Chavan', TRUE, TRUE),
(105, 'vitthal.deshmukh@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'FARMER', '9822100005', 'Vitthal Deshmukh', TRUE, TRUE),
(106, 'dnyaneshwar.pawar@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'FARMER', '9822100006', 'Dnyaneshwar Pawar', TRUE, TRUE),
(107, 'anandrao.shinde@mandiplus.com', '$2a$10$5FZ8Cc8XyfVmjg/criIM/O/h8y1cg2U4fcUekjhzg0iS9/RDe2pDq', 'FARMER', '9822100007', 'Anandrao Shinde', TRUE, TRUE);

-- 2. Populate Farmers Profile (Maharashtra based)
INSERT INTO farmers (id, user_id, name, phone, state, district, farm_size_acres, upi_id, total_earnings) VALUES
(1, 1, 'Rohan Gaikwad', '9822012345', 'Maharashtra', 'Nashik', 15.50, 'rohangaikwad@okaxis', 0.00),
(101, 101, 'Suresh Patil', '9822100001', 'Maharashtra', 'Kolhapur', 18.50, 'sureshpatil@okaxis', 0.00),
(102, 102, 'Balasaheb Jadhav', '9822100002', 'Maharashtra', 'Pune', 22.00, 'balasahebjadhav@okhdfcbank', 0.00),
(103, 103, 'Ganesh Kadam', '9822100003', 'Maharashtra', 'Ratnagiri', 12.00, 'ganeshkadam@okicici', 0.00),
(104, 104, 'Sunil Chavan', '9822100004', 'Maharashtra', 'Jalgaon', 25.00, 'sunilchavan@sbi', 0.00),
(105, 105, 'Vitthal Deshmukh', '9822100005', 'Maharashtra', 'Nagpur', 30.00, 'vitthaldeshmukh@okaxis', 0.00),
(106, 106, 'Dnyaneshwar Pawar', '9822100006', 'Maharashtra', 'Solapur', 16.00, 'dpawar@paytm', 0.00),
(107, 107, 'Anandrao Shinde', '9822100007', 'Maharashtra', 'Sangli', 14.50, 'anandraoshinde@ybl', 0.00);

-- 3. Populate Buyers Profile (Maharashtra based)
INSERT INTO buyers (id, user_id, company_name, representative_name, phone, address, gstin) VALUES
(1, 2, 'Mandi+ Agro Enterprises', 'Krushnal Chaudhari', '9876543211', 'Mandi+ Office, Nashik, Maharashtra, IN', '27AAAAA1111A1Z1');

-- 4. Populate 58 Diverse Showcase Products (Vegetables, Fruits, Grains, Pulses, Spices, Cash Crops)
INSERT INTO products (id, farmer_id, crop_name, variety, quantity_quintals, price_per_quintal, suggested_price_per_quintal, description, image_url, location, status) VALUES
-- Vegetables
(1, 1, 'Onion', 'Nashik Red (Garwa)', 150.00, 1850.00, 1850.00, 'Premium export-quality Grade-A cured red onions from Lasalgaon belt. Low moisture, thin neck, ideal for wholesale and storage.', 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=800&q=80', 'Lasalgaon, Nashik, Maharashtra', 'AVAILABLE'),
(2, 103, 'White Onion', 'Alibag White Special', 90.00, 2200.00, 2200.00, 'GI-tagged sweet white onions of Alibag. Renowned for low sulfur taste, crisp texture, medicinal qualities, and high export demand.', 'https://images.unsplash.com/photo-1580201092675-a0a6a6cafbb1?auto=format&fit=crop&w=800&q=80', 'Alibag, Raigad, Maharashtra', 'AVAILABLE'),
(3, 102, 'Tomato', 'Abhinav Semi-Determinate', 120.00, 1450.00, 1450.00, 'Firm, deep red oval hybrid tomatoes with thick pericarp. Long transit shelf life, juicy pulp, ideal for retail and processing.', 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=800&q=80', 'Narayangaon, Pune, Maharashtra', 'AVAILABLE'),
(4, 102, 'Potato', 'Kufri Jyoti Grade-A', 250.00, 1650.00, 1650.00, 'Clean oval table potatoes with smooth cream skin and shallow eyes. Machine-cleaned, zero mud, excellent cooking texture.', 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=800&q=80', 'Manchar, Pune, Maharashtra', 'AVAILABLE'),
(5, 1, 'Garlic', 'G-2 Bold Clove White', 40.00, 11500.00, 11500.00, 'Cured white garlic bulbs with tight cloves and pungent allicin content. Sun-dried with long storage viability.', 'https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?auto=format&fit=crop&w=800&q=80', 'Dhule, Maharashtra', 'AVAILABLE'),
(6, 107, 'Ginger', 'Mahim Grade-1 Bold', 55.00, 6800.00, 6800.00, 'Freshly dug plump ginger rhizomes with fiber-free flesh and zesty aroma. Thoroughly washed and cured for wholesale.', 'https://images.unsplash.com/photo-1615485500704-8e990f9900f7?auto=format&fit=crop&w=800&q=80', 'Satara, Maharashtra', 'AVAILABLE'),
(7, 104, 'Green Chilli', 'G-4 Spicy Dark Green', 35.00, 3400.00, 3400.00, 'Crisp dark-green spicy chillies with firm stalks. High capsaicin heat, fresh harvest directly from irrigated farms.', 'https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=800&q=80', 'Nandurbar, Maharashtra', 'AVAILABLE'),
(8, 1, 'Cauliflower', 'Snowball White Compact', 70.00, 1750.00, 1750.00, 'Dense, pure white compact curds free from yellowing or spots. Harvested with protective jacket leaves for intact freshness.', 'https://images.unsplash.com/photo-1568584711075-3d021a7c3ca3?auto=format&fit=crop&w=800&q=80', 'Nashik, Maharashtra', 'AVAILABLE'),
(9, 102, 'Cabbage', 'Golden Acre Round Green', 95.00, 1200.00, 1200.00, 'Solid tightly wrapped green cabbage heads. Sweet crisp leaves, 1.2-1.5 kg average head weight, perfect for wholesale.', 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?auto=format&fit=crop&w=800&q=80', 'Sangamner, Ahilyanagar, Maharashtra', 'AVAILABLE'),
(10, 1, 'Carrot', 'Pusa Kesar Deep Red', 85.00, 2100.00, 2100.00, 'Sweet juicy deep-red carrots high in beta-carotene. Machine washed, sorted by uniform length, tender core.', 'https://images.unsplash.com/photo-1590868309235-ea34bed7bd7f?auto=format&fit=crop&w=800&q=80', 'Nashik, Maharashtra', 'AVAILABLE'),
(11, 102, 'Spinach (Palak)', 'All Green Tender Leaf', 30.00, 1800.00, 1800.00, 'Crisp dark green organic palak bundles. Freshly harvested tender leaves rich in dietary iron, pesticide-free water-rinsed.', 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=800&q=80', 'Pune, Maharashtra', 'AVAILABLE'),
(12, 102, 'Lady Finger (Bhindi)', 'Radhika F1 Deep Green', 40.00, 3200.00, 3200.00, 'Tender 5-ridged deep green okra pods with non-fibrous tips. Soft seeds, easy snap, hand-graded into 8-10 cm size.', 'https://images.unsplash.com/photo-1425543103986-22abb7d7e8d2?auto=format&fit=crop&w=800&q=80', 'Baramati, Pune, Maharashtra', 'AVAILABLE'),
(13, 101, 'Brinjal (Baingan)', 'Manjari Gota Purple Round', 50.00, 1950.00, 1950.00, 'Glossy round deep-purple eggplants with fresh green calyx. Creamy seedless white flesh, ideal for bharta and curries.', 'https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=800&q=80', 'Kolhapur, Maharashtra', 'AVAILABLE'),
(14, 1, 'Capsicum', 'Indra F1 Blocky Green', 45.00, 3600.00, 3600.00, 'Greenhouse-grown blocky four-lobed bell peppers. Heavy thick walls, deep lustrous sheen, crunchy sweet texture.', 'https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?auto=format&fit=crop&w=800&q=80', 'Niphad, Nashik, Maharashtra', 'AVAILABLE'),
(15, 107, 'Green Peas (Matar)', 'Arkel Early Sweet Pods', 60.00, 4200.00, 4200.00, 'Plump well-filled sweet green pea pods. Bright green pods packed with 8-9 sweet tender peas, zero blemishes.', 'https://images.unsplash.com/photo-1587735243615-c03f25aaff15?auto=format&fit=crop&w=800&q=80', 'Satara, Maharashtra', 'AVAILABLE'),
(16, 106, 'Bitter Gourd (Karela)', 'Pusa Do Mausami Spiny', 35.00, 2800.00, 2800.00, 'Firm dark green spiny bitter gourds. Evenly tapered with crisp flesh, high medicinal momordicin value.', 'https://images.unsplash.com/photo-1596797038530-2c107229654b?auto=format&fit=crop&w=800&q=80', 'Solapur, Maharashtra', 'AVAILABLE'),
(17, 1, 'Bottle Gourd (Lauki)', 'Pusa Naveen Long Cylindrical', 75.00, 1350.00, 1350.00, 'Smooth tender cylindrical bottle gourds with pale-green skin. High moisture content, tender seeds, ideal for fresh cooking.', 'https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2?auto=format&fit=crop&w=800&q=80', 'Jalna, Maharashtra', 'AVAILABLE'),
(18, 1, 'Cucumber (Kheera)', 'Malini Hybrid Salad Green', 80.00, 1500.00, 1500.00, 'Crisp dark green salad cucumbers with minimal bitterness. Thin skin, refreshing bite, graded by uniform length.', 'https://images.unsplash.com/photo-1604977042946-1eecc30f269e?auto=format&fit=crop&w=800&q=80', 'Nashik, Maharashtra', 'AVAILABLE'),
(19, 102, 'Beetroot', 'Detroit Dark Red Globe', 55.00, 2400.00, 2400.00, 'Uniform globe-shaped beetroots with deep maroon internal rings. High sugar and betanin content, smooth trimmed skin.', 'https://images.unsplash.com/photo-1593105544559-ecb03bf76f82?auto=format&fit=crop&w=800&q=80', 'Manchar, Pune, Maharashtra', 'AVAILABLE'),
(20, 102, 'Radish (Mooli)', 'Pusa Chetki White Long', 60.00, 1400.00, 1400.00, 'Crisp, snow-white pungent long radishes with tender greens. Freshly uprooted, soil-washed, refreshing sharp bite.', 'https://images.unsplash.com/photo-1594282486552-05b4d80fbb9f?auto=format&fit=crop&w=800&q=80', 'Pune, Maharashtra', 'AVAILABLE'),
(21, 102, 'Drumstick (Moringa)', 'PKM-1 Tender Green Pods', 30.00, 4800.00, 4800.00, 'Long, meaty, dark green moringa drumstick pods packed with nutrients. Soft pulpy interior, 60-75 cm uniform length.', 'https://images.unsplash.com/photo-1546793665-c74683f339c1?auto=format&fit=crop&w=800&q=80', 'Baramati, Pune, Maharashtra', 'AVAILABLE'),
(22, 101, 'Sweet Potato', 'Cured Red Skin Cream Flesh', 65.00, 2600.00, 2600.00, 'Nutritious sweet potatoes with dusty red skin and sweet cream pulp. Rich in vitamins and complex carbohydrates.', 'https://images.unsplash.com/photo-1589927986089-35812388d1f4?auto=format&fit=crop&w=800&q=80', 'Kolhapur, Maharashtra', 'AVAILABLE'),

-- Fruits
(23, 103, 'Alphonso Mango', 'Ratnagiri Hapus GI Tag', 35.00, 12500.00, 12500.00, 'Authentic GI-certified Devgad Ratnagiri Alphonso mangoes. Naturally ripened in grass crates without carbide, heavenly aroma.', 'https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=800&q=80', 'Devgad, Ratnagiri, Maharashtra', 'AVAILABLE'),
(24, 104, 'Kesar Mango', 'Marathwada Kesar Royal', 40.00, 9800.00, 9800.00, 'Rich saffron-hued Kesar mangoes famous as the Queen of Mangoes. Extremely sweet 20+ Brix, fragrant bouquet, smooth pulp.', 'https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?auto=format&fit=crop&w=800&q=80', 'Chhatrapati Sambhajinagar, Maharashtra', 'AVAILABLE'),
(25, 1, 'Grapes', 'Thomson Seedless Green', 60.00, 7200.00, 7200.00, 'Export quality sweet seedless green grapes with 18+ Brix sweetness. Handpicked directly from organic-certified Nashik vineyards.', 'https://images.unsplash.com/photo-1596363505729-4190a9506133?auto=format&fit=crop&w=800&q=80', 'Nashik, Maharashtra', 'AVAILABLE'),
(26, 1, 'Red Flame Grapes', 'Flame Seedless Ruby Red', 45.00, 8500.00, 8500.00, 'Ruby-red crunchy seedless table grapes. Tight clusters with sweet-tart berry burst, foam cushioned packaging.', 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?auto=format&fit=crop&w=800&q=80', 'Dindori, Nashik, Maharashtra', 'AVAILABLE'),
(27, 104, 'Banana', 'Grand Naine (G9 Cavendish)', 110.00, 1850.00, 1850.00, 'Fresh export-quality yellow Cavendish bananas. Ethylene chamber ripened, rich in potassium and sweetness, zero spots.', 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=800&q=80', 'Raver, Jalgaon, Maharashtra', 'AVAILABLE'),
(28, 106, 'Pomegranate', 'Bhagwa Sindhuri Export', 50.00, 11000.00, 11000.00, 'Deep red glossy pomegranates with soft seeds and dark ruby arils. High juice yield, 300g+ average fruit weight.', 'https://images.unsplash.com/photo-1527661591475-527312dd65f5?auto=format&fit=crop&w=800&q=80', 'Solapur, Maharashtra', 'AVAILABLE'),
(29, 104, 'Papaya', 'Taiwan 786 Red Lady', 70.00, 2200.00, 2200.00, 'Elongated thick-fleshed sweet papayas with salmon-red interior. Firm texture with high sweetness and digestive enzymes.', 'https://images.unsplash.com/photo-1517282009859-f000ec3b26fe?auto=format&fit=crop&w=800&q=80', 'Nandurbar, Maharashtra', 'AVAILABLE'),
(30, 105, 'Orange (Santra)', 'Nagpur Mandarin GI Santra', 85.00, 4600.00, 4600.00, 'Famous Nagpur GI Santra loose-jacket oranges. Refreshing sweet-sour balance, juicy vesicles, hand-plucked from Vidarbha.', 'https://images.unsplash.com/photo-1547514701-42782101795e?auto=format&fit=crop&w=800&q=80', 'Katol, Nagpur, Maharashtra', 'AVAILABLE'),
(31, 104, 'Sweet Lime (Mosambi)', 'Jalna Citrus Sweet Juicy', 65.00, 3800.00, 3800.00, 'Thin-peeled juicy sweet limes (Mosambi). Low acid, high vitamin C sweet juice content, perfect for juicing and retail.', 'https://images.unsplash.com/photo-1582979512210-99b6a53386f9?auto=format&fit=crop&w=800&q=80', 'Ambad, Jalna, Maharashtra', 'AVAILABLE'),
(32, 102, 'Guava (Amrud)', 'Sardar Lucknow L-49 White', 40.00, 3200.00, 3200.00, 'Large round sweet guavas with creamy white pulp and soft seeds. Rich aroma, high vitamin C and pectin content.', 'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?auto=format&fit=crop&w=800&q=80', 'Rahata, Ahilyanagar, Maharashtra', 'AVAILABLE'),
(33, 102, 'Watermelon', 'Kiran Striped Sugar Baby', 140.00, 1150.00, 1150.00, 'Crisp deep red flesh watermelons with thin rind and small seeds. High Brix sweetness (12%+), 3-5 kg size.', 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?auto=format&fit=crop&w=800&q=80', 'Indapur, Pune, Maharashtra', 'AVAILABLE'),
(34, 106, 'Muskmelon (Kharbuj)', 'Madhumati Netted Orange', 60.00, 2300.00, 2300.00, 'Heavily netted cantaloupe melons with fragrant salmon-orange flesh. Sweet musky perfume, thick tender meat.', 'https://images.unsplash.com/photo-1571575173700-afb9492e6a50?auto=format&fit=crop&w=800&q=80', 'Pandharpur, Solapur, Maharashtra', 'AVAILABLE'),
(35, 105, 'Apple', 'Kinnaur Royal Delicious Red', 50.00, 9500.00, 9500.00, 'Mountain orchard crisp red apples. Sweet aromatic flesh, deep crimson blush, wax-free natural orchard finish.', 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=800&q=80', 'Nagpur Hub, Maharashtra', 'AVAILABLE'),
(36, 102, 'Custard Apple (Sitaphal)', 'Balanagar Golden Eyes', 30.00, 6500.00, 6500.00, 'Plump sugar apple / sitaphal with large creamy eyes and minimal seeds. Heavenly sweet velvety custard pulp.', 'https://images.unsplash.com/photo-1618897996318-5a901fa6ca71?auto=format&fit=crop&w=800&q=80', 'Saswad, Pune, Maharashtra', 'AVAILABLE'),
(37, 107, 'Strawberry', 'Mahabaleshwar Winter Dawn', 20.00, 18000.00, 18000.00, 'GI-tagged Mahabaleshwar strawberries. Bright crimson conical berries with glossy sheen, high sweetness and fragrance.', 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?auto=format&fit=crop&w=800&q=80', 'Mahabaleshwar, Satara, Maharashtra', 'AVAILABLE'),
(38, 103, 'Chikoo (Sapota)', 'Gholvad Kalipatti GI Sweet', 45.00, 3400.00, 3400.00, 'Coastal GI-tagged Gholvad sapota (chikoo). Caramel-sweet grainy texture, thin skin, high natural maltose sugar.', 'https://images.unsplash.com/photo-1589217157232-464b505b197f?auto=format&fit=crop&w=800&q=80', 'Dahanu, Palghar, Maharashtra', 'AVAILABLE'),
(39, 106, 'Lemon (Nimbu)', 'Kagzi Thin Skin Acid Lime', 35.00, 5200.00, 5200.00, 'Thin-skinned round juicy yellow lemons. Extra rich in citric acid and juice percentage (over 48%), long shelf life.', 'https://images.unsplash.com/photo-1590502593747-42a996133562?auto=format&fit=crop&w=800&q=80', 'Solapur, Maharashtra', 'AVAILABLE'),
(40, 103, 'Pineapple', 'Queen Golden Sweet Tropical', 50.00, 3600.00, 3600.00, 'Golden yellow aromatic Queen pineapples. Intensely sweet with crisp core, pleasant tropical scent.', 'https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=800&q=80', 'Ratnagiri, Maharashtra', 'AVAILABLE'),
(41, 103, 'Coconut', 'Coastal Tender & Mature Whole', 120.00, 3200.00, 3200.00, 'Fresh coastal coconuts with sweet water and thick kernel. High oil recovery, ideal for culinary and commercial use.', 'https://images.unsplash.com/photo-1544378730-8b5104b18790?auto=format&fit=crop&w=800&q=80', 'Sindhudurg, Maharashtra', 'AVAILABLE'),

-- Grains, Pulses, Spices & Cash Crops
(42, 105, 'Wheat', 'Sharbati Golden Grain', 220.00, 3200.00, 3200.00, 'Heavy lustrous golden wheat kernels rich in protein and fiber. Machine-cleaned and packed in 50kg bags.', 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?auto=format&fit=crop&w=800&q=80', 'Nagpur, Maharashtra', 'AVAILABLE'),
(43, 102, 'Wheat', 'Lokwan Premium Grain', 180.00, 2950.00, 2950.00, 'Bold hard-grain Lokwan wheat processed through destoners. Moisture under 11%, zero pest infestation, FAQ standard.', 'https://images.unsplash.com/photo-1543257580-7269da773bf5?auto=format&fit=crop&w=800&q=80', 'Kopargaon, Ahilyanagar, Maharashtra', 'AVAILABLE'),
(44, 102, 'Basmati Rice', '1121 Extra Long Steam', 180.00, 5400.00, 5400.00, 'Aged 1121 steam Basmati rice grains with 8.4mm average raw length. Cooks to over 2.2x length, aromatic non-sticky texture.', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=800&q=80', 'Pune, Maharashtra', 'AVAILABLE'),
(45, 102, 'Indrayani Rice', 'Maval Fragrant Sticky Rice', 120.00, 4600.00, 4600.00, 'Traditional sticky aromatic rice of Sahyadri slopes. Pleasant natural aroma, soft mouthfeel upon pressure cooking.', 'https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=800&q=80', 'Maval, Pune, Maharashtra', 'AVAILABLE'),
(46, 103, 'Kolam Rice', 'Wada Kolam Zini Rice', 140.00, 4800.00, 4800.00, 'GI-tagged Wada Kolam tiny grain rice. Light on digestion, smooth delicate texture when boiled, machine sorted.', 'https://images.unsplash.com/photo-1516684732162-798a0062be99?auto=format&fit=crop&w=800&q=80', 'Wada, Palghar, Maharashtra', 'AVAILABLE'),
(47, 105, 'Soybean', 'JS 335 Gold Cleaned', 130.00, 4850.00, 4850.00, 'Grade-A bold yellow soybeans with 19% oil content and 40% protein. Cleaned, graded, moisture below 10%, perfect for processing.', 'https://images.unsplash.com/photo-1599940824399-b87987ceb72a?auto=format&fit=crop&w=800&q=80', 'Latur, Maharashtra', 'AVAILABLE'),
(48, 105, 'Chana (Gram)', 'Vijay Desi Bold Chana', 90.00, 5650.00, 5650.00, 'Brown sun-dried bold Bengal gram chickpeas. High sprouting viability, rich earthy taste, graded FAQ standard.', 'https://images.unsplash.com/photo-1515543237350-b3eea1ec8082?auto=format&fit=crop&w=800&q=80', 'Latur, Maharashtra', 'AVAILABLE'),
(49, 105, 'Tur Dal (Arhar)', 'Marathwada BDN-711 Whole', 80.00, 7800.00, 7800.00, 'Whole unpolished red pigeon pea. High protein content, unadulterated, excellent cooking quality for dal millers.', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=800&q=80', 'Akola, Maharashtra', 'AVAILABLE'),
(50, 102, 'Moong (Green Gram)', 'Kopargaon Shine Moong', 70.00, 8600.00, 8600.00, 'Shiny olive-green whole moong beans. Uniform size, high germinability for healthy sprouts, zero weevils.', 'https://images.unsplash.com/photo-1607672632458-9eb56696346b?auto=format&fit=crop&w=800&q=80', 'Kopargaon, Ahilyanagar, Maharashtra', 'AVAILABLE'),
(51, 107, 'Groundnut (Peanut)', 'Tag-24 Western Bold Pods', 100.00, 6800.00, 6800.00, 'Double-kernel clean dry groundnut pods with pink sweet kernels inside. 48% oil content, snacking and crushing grade.', 'https://images.unsplash.com/photo-1567894340315-735d7c361db0?auto=format&fit=crop&w=800&q=80', 'Karad, Satara, Maharashtra', 'AVAILABLE'),
(52, 107, 'Turmeric', 'Salem / Sangli High Curcumin', 65.00, 9400.00, 9400.00, 'High curcumin content (5.2%+) polished whole turmeric fingers from the spice hub of Sangli. Cleaned, graded, and sun-cured.', 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=800&q=80', 'Sangli, Maharashtra', 'AVAILABLE'),
(53, 101, 'Red Chilli (Dry)', 'Byadagi Wrinkled Deep Red', 45.00, 17200.00, 17200.00, 'Sun-dried deep red chillies known for vibrant natural red color and aromatic mild heat. Zero pesticide residue, stemless cleaned.', 'https://images.unsplash.com/photo-1588252303782-cb80119abd6d?auto=format&fit=crop&w=800&q=80', 'Kolhapur, Maharashtra', 'AVAILABLE'),
(54, 106, 'Jowar (Sorghum)', 'Maldandi M 35-1 (Shalu)', 110.00, 3450.00, 3450.00, 'Pearly white sweet grain Maldandi Jowar. Famous winter harvest grain, produces tender sweet bhakris, gluten-free staple.', 'https://images.unsplash.com/photo-1610484826967-09c5720778c7?auto=format&fit=crop&w=800&q=80', 'Solapur, Maharashtra', 'AVAILABLE'),
(55, 102, 'Pearl Millet (Bajra)', 'Dhanshakti Biofortified Zinc', 95.00, 2650.00, 2650.00, 'Biofortified greyish-green pearl millet rich in natural iron and zinc. High calorie arid grain, cleaned and bagged.', 'https://images.unsplash.com/photo-1600857544200-b2f666a9a2ec?auto=format&fit=crop&w=800&q=80', 'Ahilyanagar, Maharashtra', 'AVAILABLE'),
(56, 1, 'Yellow Maize (Corn)', 'Pioneer Hybrid Starch Bold', 160.00, 2480.00, 2480.00, 'Hard flint yellow corn grains with 70% starch content. High bulk density, moisture below 12%, machine threshed.', 'https://images.unsplash.com/photo-1551754655-cd27e38d2076?auto=format&fit=crop&w=800&q=80', 'Niphad, Nashik, Maharashtra', 'AVAILABLE'),
(57, 101, 'Sugarcane', 'Co 86032 (Nayana High Recovery)', 500.00, 340.00, 340.00, 'Thick, juicy cane stalks with 13.5% sugar recovery. Fresh harvest cutting done immediately upon buyer order confirmation.', 'https://images.unsplash.com/photo-1527842891421-42eec6e703ea?auto=format&fit=crop&w=800&q=80', 'Kolhapur, Maharashtra', 'AVAILABLE'),
(58, 105, 'Cotton', 'MCU-5 White Gold Raw Bolls', 75.00, 7250.00, 7250.00, 'Long-staple clean white cotton bolls with high ginning outturn (36%+). Low trash content, handpicked from Vidarbha.', 'https://images.unsplash.com/photo-1605000797499-95a51c5269ae?auto=format&fit=crop&w=800&q=80', 'Nagpur, Maharashtra', 'AVAILABLE');

-- 5. Populate Cold Storage (Maharashtra facilities)
INSERT INTO cold_storage (id, user_id, name, location, total_capacity_tons, available_capacity_tons, price_per_ton_day, description) VALUES
(1, 3, 'Mandi+ Nashik Cold Chain Hub', 'Mandi+ Office, Nashik, Maharashtra', 1000.00, 850.00, 12.00, 'IoT-enabled high-capacity cold chain terminal specializing in grape and onion storage with humidity controls.');

-- 6. Populate Storage Bookings
INSERT INTO storage_bookings (id, farmer_id, cold_storage_id, quantity_tons, start_date, end_date, total_price, status) VALUES
(1, 1, 1, 15.00, '2026-06-01', '2026-06-30', 5400.00, 'APPROVED');

-- 7. Populate Mandi Prices (Maharashtra Markets)
INSERT INTO mandi_prices (id, crop_name, variety, market_name, state, price_per_quintal, price_change_yesterday) VALUES
(1, 'Onion', 'Nasik Red', 'Lasalgaon Mandi', 'Maharashtra', 1850.00, 45.00),
(2, 'Grapes', 'Thomson Seedless', 'Nashik Mandi', 'Maharashtra', 7500.00, -120.00),
(3, 'Sugarcane', 'Co 86032', 'Pune Mandi', 'Maharashtra', 310.00, 5.00),
(4, 'Soybean', 'JS 335', 'Nagpur Mandi', 'Maharashtra', 4600.00, 30.00),
(5, 'Alphonso Mango', 'Devgad', 'Mumbai APMC', 'Maharashtra', 12500.00, 250.00);

-- 8. Populate Weather Locations (Maharashtra weather points)
INSERT INTO weather (id, location, temperature_celsius, condition_text, humidity_percentage, rain_probability_percentage) VALUES
(1, 'Nashik, Maharashtra', 31.00, 'Sunny', 40, 5),
(2, 'Pune, Maharashtra', 29.00, 'Partly Cloudy', 55, 10),
(3, 'Nagpur, Maharashtra', 38.00, 'Hot', 25, 0);

-- 9. Populate Complaints
INSERT INTO complaints (id, user_id, title, description, status) VALUES
(1, 1, 'Weighment Dispute', 'Mandi digital scale variance found during onion unloading.', 'PENDING');

-- 10. Populate Verified Government Minimum Support Prices (MSP 2024-25)
INSERT INTO gov_mandi_msp (commodity, season, msp_price, unit, source_authority, verified) VALUES
('Wheat', 'Rabi 2024-25', 2275.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Paddy', 'Kharif 2024-25', 2300.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Maize', 'Kharif 2024-25', 2225.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Soyabean', 'Kharif 2024-25', 4892.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Cotton', 'Kharif 2024-25', 7121.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Gram', 'Rabi 2024-25', 5440.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Moong', 'Kharif 2024-25', 8682.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Groundnut', 'Kharif 2024-25', 6783.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Mustard', 'Rabi 2024-25', 5650.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Tur', 'Kharif 2024-25', 7550.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Bajra', 'Kharif 2024-25', 2625.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Jowar', 'Kharif 2024-25', 3371.00, 'Quintal', 'Ministry of Agriculture & Farmers Welfare, GoI (Verified)', TRUE),
('Onion', 'Market Intervention 2024-25', 1800.00, 'Quintal', 'State Market Intervention Reference Scheme', TRUE);

-- 11. Populate Official Agricultural Advisories
INSERT INTO gov_mandi_advisories (title, commodity, district, severity, message, source_authority, active) VALUES
('Peak Harvest Arrival & Moisture Grade Advisory', 'Soyabean', 'Nashik', 'INFO', 'Heavy arrivals observed across Nashik and Niphad APMC mandis. Ensure moisture content is below 12% to prevent 5-8% grade deductions on modal rates.', 'Maharashtra State Agricultural Marketing Board (MSAMB)', TRUE),
('Export Policy & Minimum Realization Advisory', 'Onion', NULL, 'ALERT', 'Export duty rationalization active. Lasalgaon and Pimpalgaon mandis report steady modal prices. Avoid distress selling below modal average.', 'Department of Consumer Affairs & MSAMB', TRUE),
('FAQ Fair Average Quality Standard Reminder', 'Wheat', NULL, 'RECOMMENDATION', 'Ensure FAQ standards (less than 2% foreign matter, undamaged grain) to receive full government MSP of ₹2,275/qtl at designated state procurement centers.', 'Ministry of Agriculture & Farmers Welfare, GoI', TRUE);

