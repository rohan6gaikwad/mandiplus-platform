-- Mandi+ Smart Agriculture Marketplace Database Schema
-- Developed by R Tech Solutions
-- MySQL Dialect

CREATE DATABASE IF NOT EXISTS mandiplus_db;
USE mandiplus_db;

-- 1. Users Table (Core Authentication)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- 'FARMER', 'BUYER', 'COLD_STORAGE_MANAGER', 'ADMIN', 'DELIVERY_PARTNER'
    phone VARCHAR(20) DEFAULT NULL,
    full_name VARCHAR(100) DEFAULT NULL,
    profile_completed BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1b. OTP Challenges Table (Cryptographic SMS Phone Verification)
CREATE TABLE IF NOT EXISTS otp_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    attempts INT DEFAULT 0,
    max_attempts INT DEFAULT 3,
    consumed BOOLEAN DEFAULT FALSE,
    consumed_at TIMESTAMP NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_phone (phone),
    INDEX idx_otp_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Farmers Profile Table
CREATE TABLE IF NOT EXISTS farmers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    state VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    farm_size_acres DECIMAL(10, 2) DEFAULT 0.0,
    upi_id VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Buyers Profile Table
CREATE TABLE IF NOT EXISTS buyers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    representative_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    gstin VARCHAR(15) DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Products Table (Produce Listings)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    crop_name VARCHAR(100) NOT NULL,
    variety VARCHAR(100) DEFAULT NULL,
    quantity_quintals DECIMAL(10, 2) NOT NULL, -- Quantity in Quintals (1 Quintal = 100 kg / Kilograms)
    price_per_quintal DECIMAL(10, 2) NOT NULL, -- Price per Quintal (Rate per KG = price_per_quintal / 100)
    suggested_price_per_quintal DECIMAL(10, 2) DEFAULT NULL, -- Suggested Price per Quintal (Rate per KG = suggested_price / 100)
    admin_feedback TEXT DEFAULT NULL,
    description TEXT,
    image_url LONGTEXT DEFAULT NULL,
    location VARCHAR(150) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING_APPROVAL', -- 'PENDING_APPROVAL', 'AVAILABLE', 'REJECTED', 'SOLD'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE,
    INDEX idx_crop_name (crop_name),
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity_quintals DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'SHIPPED', 'DELIVERED', 'CANCELLED'
    payment_method VARCHAR(50) DEFAULT 'RAZORPAY',
    payment_status VARCHAR(50) DEFAULT 'ESCROW_HOLD', -- 'ESCROW_HOLD', 'PAID', 'PENDING_COD', 'FAILED'
    razorpay_order_id VARCHAR(100) DEFAULT NULL,
    razorpay_payment_id VARCHAR(100) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE RESTRICT,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Cold Storage Facilities
CREATE TABLE IF NOT EXISTS cold_storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL, -- Owned/managed by user with COLD_STORAGE_MANAGER role
    name VARCHAR(150) NOT NULL,
    location VARCHAR(150) NOT NULL,
    total_capacity_tons DECIMAL(10, 2) NOT NULL,
    available_capacity_tons DECIMAL(10, 2) NOT NULL,
    price_per_ton_day DECIMAL(10, 2) NOT NULL,
    description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Storage Bookings
CREATE TABLE IF NOT EXISTS storage_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT DEFAULT NULL,
    buyer_id BIGINT DEFAULT NULL,
    cold_storage_id BIGINT NOT NULL,
    quantity_tons DECIMAL(10, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'ACTIVE', 'COMPLETED', 'REJECTED'
    rejection_reason VARCHAR(500) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE CASCADE,
    FOREIGN KEY (cold_storage_id) REFERENCES cold_storage(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Complaints Table (For Admin Panel Management)
CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- 'PENDING', 'RESOLVED', 'CLOSED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Mandi Prices (Live Insights Preview)
CREATE TABLE IF NOT EXISTS mandi_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crop_name VARCHAR(100) NOT NULL,
    variety VARCHAR(100) DEFAULT NULL,
    market_name VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    price_per_quintal DECIMAL(10, 2) NOT NULL,
    price_change_yesterday DECIMAL(10, 2) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Weather (Live Insights Preview)
CREATE TABLE IF NOT EXISTS weather (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(100) NOT NULL,
    temperature_celsius DECIMAL(5, 2) NOT NULL,
    condition_text VARCHAR(100) NOT NULL,
    humidity_percentage INT NOT NULL,
    rain_probability_percentage INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Delivery Partners (Blinkit-style Agri Quick Commerce Fleet)
CREATE TABLE IF NOT EXISTS delivery_partners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    city VARCHAR(50) DEFAULT 'Nashik',
    district VARCHAR(50) DEFAULT 'Nashik',
    vehicle_type VARCHAR(50) DEFAULT 'BIKE',
    vehicle_number VARCHAR(30),
    license_number VARCHAR(50),
    pan_or_aadhaar VARCHAR(50),
    preferred_hub VARCHAR(100) DEFAULT 'Nashik APMC Main Terminal',
    upi_id VARCHAR(100),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    is_online BOOLEAN DEFAULT TRUE,
    rating DECIMAL(3, 2) DEFAULT 4.92,
    total_deliveries INT DEFAULT 0,
    today_deliveries INT DEFAULT 0,
    today_earnings DECIMAL(10, 2) DEFAULT 0.00,
    total_earnings DECIMAL(10, 2) DEFAULT 0.00,
    duty_shift VARCHAR(30) DEFAULT 'FLEXI',
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Delivery Tasks (Dispatch & Trip Workflow)
CREATE TABLE IF NOT EXISTS delivery_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_code VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT DEFAULT NULL,
    delivery_partner_id BIGINT DEFAULT NULL,
    pickup_location VARCHAR(200) NOT NULL,
    pickup_contact_name VARCHAR(100),
    pickup_contact_phone VARCHAR(20),
    drop_location VARCHAR(200) NOT NULL,
    drop_contact_name VARCHAR(100),
    drop_contact_phone VARCHAR(20),
    crop_name VARCHAR(100) NOT NULL,
    variety VARCHAR(100),
    quantity_quintals DECIMAL(10, 2),
    payout_amount DECIMAL(10, 2) NOT NULL,
    surge_bonus DECIMAL(10, 2) DEFAULT 0.00,
    distance_km DECIMAL(6, 2),
    estimated_minutes INT,
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    delivery_otp VARCHAR(10) DEFAULT '5821',
    payment_type VARCHAR(50) DEFAULT 'ESCROW_PAID',
    cash_to_collect DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    FOREIGN KEY (delivery_partner_id) REFERENCES delivery_partners(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. Payments & Escrow Ledger Table (Razorpay Integrated)
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    razorpay_order_id VARCHAR(100) NOT NULL,
    razorpay_payment_id VARCHAR(100) DEFAULT NULL,
    razorpay_signature VARCHAR(255) DEFAULT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    status VARCHAR(50) DEFAULT 'CREATED', -- 'CREATED', 'CAPTURED', 'FAILED', 'REFUNDED'
    payment_method VARCHAR(50) DEFAULT 'RAZORPAY', -- 'RAZORPAY', 'COD', 'UPI', 'CARD', 'NETBANKING'
    escrow_status VARCHAR(50) DEFAULT 'HELD_IN_ESCROW', -- 'HELD_IN_ESCROW', 'RELEASED_TO_FARMER', 'REFUNDED_TO_BUYER', 'NOT_APPLICABLE'
    purpose VARCHAR(100) DEFAULT 'PRODUCE_PURCHASE',
    buyer_id BIGINT DEFAULT NULL,
    order_id BIGINT DEFAULT NULL,
    storage_booking_id BIGINT DEFAULT NULL,
    receipt VARCHAR(100) DEFAULT NULL,
    failure_reason VARCHAR(500) DEFAULT NULL,
    metadata_json TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE SET NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    INDEX idx_rzp_order (razorpay_order_id),
    INDEX idx_rzp_payment (razorpay_payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. Government Mandi Daily Prices (data.gov.in Official Sync)
CREATE TABLE IF NOT EXISTS gov_mandi_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    market VARCHAR(150) NOT NULL,
    commodity VARCHAR(100) NOT NULL,
    variety VARCHAR(100) DEFAULT 'Other',
    grade VARCHAR(50) DEFAULT 'FAQ',
    arrival_date DATE NOT NULL,
    min_price DECIMAL(10, 2) NOT NULL,
    max_price DECIMAL(10, 2) NOT NULL,
    modal_price DECIMAL(10, 2) NOT NULL,
    source VARCHAR(100) DEFAULT 'data.gov.in (Agmarknet)',
    imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_gov_mandi_record (state, district, market, commodity, variety, grade, arrival_date),
    INDEX idx_gov_comm_dist (commodity, district, arrival_date),
    INDEX idx_gov_market (market, arrival_date),
    INDEX idx_gov_arrival (arrival_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. Government Verified Minimum Support Price (MSP) Benchmarks
CREATE TABLE IF NOT EXISTS gov_mandi_msp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    commodity VARCHAR(100) NOT NULL,
    season VARCHAR(50) NOT NULL,
    msp_price DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(30) DEFAULT 'Quintal',
    source_authority VARCHAR(150) DEFAULT 'Ministry of Agriculture & Farmers Welfare, GoI',
    verified BOOLEAN DEFAULT TRUE,
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_msp_commodity (commodity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16. Official Farmer-Facing Mandi Advisories
CREATE TABLE IF NOT EXISTS gov_mandi_advisories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    commodity VARCHAR(100) DEFAULT NULL,
    district VARCHAR(100) DEFAULT NULL,
    severity VARCHAR(30) DEFAULT 'INFO', -- 'INFO', 'ALERT', 'RECOMMENDATION'
    message TEXT NOT NULL,
    source_authority VARCHAR(150) DEFAULT 'Maharashtra State Agricultural Marketing Board (MSAMB)',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_advisory_active (active, commodity, district)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 17. Government Mandi API Synchronization Logs
CREATE TABLE IF NOT EXISTS gov_mandi_sync_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sync_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL, -- 'SUCCESS', 'FAILED', 'PARTIAL'
    records_fetched INT DEFAULT 0,
    records_inserted INT DEFAULT 0,
    records_updated INT DEFAULT 0,
    error_message TEXT DEFAULT NULL,
    triggered_by VARCHAR(100) DEFAULT 'SCHEDULED_JOB',
    execution_time_ms BIGINT DEFAULT 0,
    INDEX idx_sync_time (sync_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 18. Government Mandi Price Management Audit Logs
CREATE TABLE IF NOT EXISTS gov_mandi_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    details TEXT DEFAULT NULL,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_time (performed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 19. Delivery Fleet Partners
CREATE TABLE IF NOT EXISTS delivery_partners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    city VARCHAR(50) DEFAULT 'Nashik',
    district VARCHAR(50) DEFAULT 'Nashik',
    vehicle_type VARCHAR(50) DEFAULT 'BIKE',
    vehicle_number VARCHAR(30) DEFAULT NULL,
    license_number VARCHAR(50) DEFAULT NULL,
    pan_or_aadhaar VARCHAR(50) DEFAULT NULL,
    preferred_hub VARCHAR(100) DEFAULT 'Nashik APMC Main Terminal',
    upi_id VARCHAR(100) DEFAULT NULL,
    emergency_contact_name VARCHAR(100) DEFAULT NULL,
    emergency_contact_phone VARCHAR(20) DEFAULT NULL,
    is_online BOOLEAN NOT NULL DEFAULT TRUE,
    rating DECIMAL(3, 2) DEFAULT 5.00,
    total_trips INT DEFAULT 0,
    completed_trips INT DEFAULT 0,
    today_earnings DECIMAL(10, 2) DEFAULT 0.00,
    total_earnings DECIMAL(12, 2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 20. Farmgate Delivery Consignment Tasks
CREATE TABLE IF NOT EXISTS delivery_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_code VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT DEFAULT NULL,
    delivery_partner_id BIGINT DEFAULT NULL,
    pickup_location VARCHAR(200) NOT NULL,
    pickup_contact_name VARCHAR(100) DEFAULT NULL,
    pickup_contact_phone VARCHAR(20) DEFAULT NULL,
    drop_location VARCHAR(200) NOT NULL,
    drop_contact_name VARCHAR(100) DEFAULT NULL,
    drop_contact_phone VARCHAR(20) DEFAULT NULL,
    crop_name VARCHAR(100) NOT NULL,
    variety VARCHAR(100) DEFAULT NULL,
    quantity_quintals DECIMAL(10, 2) DEFAULT 1.00,
    payout_amount DECIMAL(10, 2) DEFAULT 350.00,
    surge_bonus DECIMAL(10, 2) DEFAULT 0.00,
    distance_km DECIMAL(6, 2) DEFAULT 8.50,
    estimated_minutes INT DEFAULT 25,
    status VARCHAR(50) DEFAULT 'DELIVERY_PENDING',
    delivery_otp VARCHAR(10) DEFAULT '4819',
    payment_type VARCHAR(50) DEFAULT 'ESCROW_PAID',
    cash_to_collect DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP NULL DEFAULT NULL,
    pickup_confirmed_at TIMESTAMP NULL DEFAULT NULL,
    transit_started_at TIMESTAMP NULL DEFAULT NULL,
    delivered_at TIMESTAMP NULL DEFAULT NULL,
    current_latitude DOUBLE DEFAULT NULL,
    current_longitude DOUBLE DEFAULT NULL,
    location_updated_at TIMESTAMP NULL DEFAULT NULL,
    is_live_tracking_active BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    FOREIGN KEY (delivery_partner_id) REFERENCES delivery_partners(id) ON DELETE SET NULL,
    INDEX idx_task_status (status),
    INDEX idx_task_partner (delivery_partner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 21. Delivery Lifecycle Immutable Audit Logs
CREATE TABLE IF NOT EXISTS delivery_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_task_id BIGINT NOT NULL,
    order_id BIGINT DEFAULT NULL,
    actor_email VARCHAR(100) NOT NULL,
    actor_role VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50) DEFAULT NULL,
    new_status VARCHAR(50) NOT NULL,
    remarks TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_delivery (delivery_task_id),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



