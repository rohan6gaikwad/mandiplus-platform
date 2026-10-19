USE mandiplus_db;

-- 1. Ensure any conflicting admin user is updated or removed so ONLY admin@gmail.com is ADMIN
UPDATE users SET role = 'FARMER' WHERE role = 'ADMIN' AND email != 'admin@gmail.com';

-- 2. Insert or Update admin@gmail.com with password 'admin@gmail.com'
-- BCrypt for 'admin@gmail.com': $2a$10$cUWmnXn9CbziBQKZa.p5NO3Bv/DUnNNb4ZETg1sNndRubeqqiTkVq
INSERT INTO users (id, email, password, role, phone, full_name, profile_completed, active)
VALUES (4, 'admin@gmail.com', '$2a$10$cUWmnXn9CbziBQKZa.p5NO3Bv/DUnNNb4ZETg1sNndRubeqqiTkVq', 'ADMIN', '9012345678', 'System Administrator', TRUE, TRUE)
ON DUPLICATE KEY UPDATE
    email = 'admin@gmail.com',
    password = '$2a$10$cUWmnXn9CbziBQKZa.p5NO3Bv/DUnNNb4ZETg1sNndRubeqqiTkVq',
    role = 'ADMIN',
    phone = '9012345678',
    full_name = 'System Administrator',
    profile_completed = TRUE,
    active = TRUE;

-- 3. Verify admin record in users table
SELECT id, email, role, phone, full_name, active FROM users WHERE email = 'admin@gmail.com';
