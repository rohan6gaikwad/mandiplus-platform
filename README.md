# Mandi+: Smart Agriculture Marketplace, Quick-Commerce Logistics & Cold Chain Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React Vite](https://img.shields.io/badge/Frontend-React%2018%20%2B%20Vite-blue.svg)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/Database-MySQL%208.x-orange.svg)](https://www.mysql.com/)
[![Razorpay](https://img.shields.io/badge/Payments-Razorpay%20Sandbox-02042B.svg)](https://razorpay.com/)
[![WebSockets](https://img.shields.io/badge/Real--Time-Spring%20WebSockets-blueviolet.svg)](https://spring.io/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](#)

**Mandi+** is a full-stack smart agriculture marketplace and logistics ecosystem designed to bridge the gap between **Farmers, Commercial Buyers, Quick-Commerce Delivery Fleets, Cold Storage Operators, and Market Administrators**. 

The platform modernizes agricultural commerce through end-to-end crop trade, automated stock reservation, cold storage slot leasing, quick-commerce agricultural dispatch (Blinkit/Zepto-style fleet workflows), simulated and live Razorpay escrow payments, APMC Mandi rate ticker updates, dispute resolution desks, multi-media WhatsApp Web chat, and tri-lingual localization (English, Hindi, Marathi).

---

## 🌟 What's New & Recent Major Updates

1. **Agri Quick-Commerce Delivery Fleet Module (`/delivery` & `/apply-delivery`)**:
   - **Dedicated Delivery Partner Role (`DELIVERY_PARTNER`)**: Full operational lifecycle for agricultural delivery riders and cargo drivers.
   - **3-Step Partner Onboarding**: Self-service KYC application with vehicle selection (Bike, Electric Scooter, Auto Tempo, Mini Truck Tata Ace), Maharashtra APMC hub assignment, PAN/Aadhaar validation, and UPI payout linking.
   - **Dispatch Operations Dashboard**: Real-time incoming task radar with Web Audio API chime notifications, online/offline duty toggle, and trip assignment.
   - **Secure Milestone Delivery with OTP**: Trip progression (`AVAILABLE` → `ACCEPTED` → `REACHED_PICKUP` → `PICKED_UP` → `IN_TRANSIT` → `DELIVERED`) safeguarded by mandatory 4-digit customer delivery OTP verification.
   - **Earnings & Instant Cashout**: Real-time earnings ledger (trip rates + surge bonuses) with instant on-demand UPI payout withdrawal.
   - **Driver SOS Emergency System**: Instant one-click SOS alert modal with emergency contact dispatch.

2. **Produce Quality Moderation & Catalog Approval Desk**:
   - **Two-Tier Produce Listing**: Farmer listings default to `PENDING_APPROVAL` to prevent fraudulent listings or erratic pricing.
   - **Admin Moderation Console**: Administrators can inspect produce photos in a high-resolution lightbox, suggest market-aligned price adjustments, approve listings directly to the live marketplace (`AVAILABLE`), or reject with feedback reasons visible to the farmer.
   - **Farmer Feedback Loop**: Farmers receive real-time admin feedback badges and suggested price recommendations directly in their inventory tab to adjust and re-submit immediately.

3. **Multi-Option Checkout & Escrow Payment Engine**:
   - **Razorpay Sandbox Integration**: Seamless pop-up checkout handling cards, UPI apps, QR codes, and Netbanking with HMAC-SHA256 signature verification.
   - **Multi-Method Checkout Modal (`CheckoutFlowModal`)**: Supports Razorpay gateway, simulated Instant UPI/Netbanking, and Cash on Delivery (COD) with escrow hold semantics.
   - **Animated Dispatch Transition**: Interactive driving truck animation celebrating confirmed consignments with detailed downloadable escrow receipts.
   - **Automated Public Webhook (`/api/public/payment/webhook`)**: Asynchronous gateway verification with signature authentication to reconcile transactions independently.
   - **Buyer Payments & Escrow Ledger (`/buyer/payments`)**: Complete transparent audit log showing transaction IDs, payment methods, and escrow statuses (`HELD_IN_ESCROW`, `RELEASED_TO_FARMER`).

4. **Automated Database Seeder (`DataInitializer.java`)**:
   - Spring Boot startup runner that verifies and populates 10 high-resolution showcase agricultural produce items (Nashik Red Onions, Thomson Seedless Grapes, Ratnagiri Alphonso Mangoes, Sharbati Wheat, Byadagi Chillies, 1121 Basmati Rice, Waigaon Turmeric, Sugarcane Co 86032, Abhinav Fresh Tomatoes, and Grand Naine Bananas) out-of-the-box for evaluators and college presentations.

5. **WhatsApp Web Real-Time Multi-Media Chat System**:
   - Authentic WhatsApp Web UI with doodle pattern background, speech bubbles, audio chimes, and double blue read ticks (`✓✓`).
   - High-resolution photo sharing with lightbox preview and download.
   - Microphone live voice note recording with audio waveform visualizer and custom WhatsApp voice note player.
   - Native video sharing with embedded video playback controls.
   - Pinned Admin Helpdesk banner with quick action templates (*Escrow Verified*, *Inspection Approved*, *Logistics Cleared*, *Dispute Resolved*).

6. **Targeted Tri-Lingual Localization**:
   - Native toggle and translation support restricted cleanly to **English**, **Hindi (हिंदी)**, and **Marathi (मराठी)**.

---

## 🚀 Key Modules & Functional Capabilities

### 1. Farmer Module
* **Crop Catalog Management**: List crops with variety, quantity in quintals, price per quintal, location, and photos.
* **Moderation Feedback Tracking**: Real-time status badges (`PENDING_APPROVAL`, `AVAILABLE`, `REJECTED`) with admin review suggestions.
* **Order Fulfillment**: Approve or reject incoming buyer orders with automated stock deduction.
* **Cold Storage Leases**: Search nearby cold chains and book storage slots with date ranges.
* **Analytical Financial Ledger**: Live earnings summaries, active orders, and sales breakdown.

### 2. Buyer Module
* **Marketplace Discovery**: Filter produce by dynamic search keywords, location, price ranges, and categorized chips (**Vegetables**, **Fruits**, **Grains**).
* **Interactive Shopping Cart & Buy Now**: Flexible quantity unit conversions (Kilograms and Quintals) with real-time stock ceiling validation.
* **Multi-Method Checkout**: Razorpay gateway, simulated UPI, or Cash on Delivery with delivery address selection.
* **Consignment Tracking**: Monitor order shipment milestones (`PENDING`, `APPROVED`, `SHIPPED`, `DELIVERED`).
* **Transaction & Escrow History**: Dedicated audit log detailing payments, escrow holds, and gateway payment IDs.

### 3. Delivery Partner Quick-Commerce Module (Agri Fleet)
* **Blinkit-Style Driver Hub**: Built specifically for rural-to-urban and mandi-to-buyer quick transit.
* **Fleet Dashboard**: Toggle online/offline status, view dispatch tasks, and accept consignments.
* **Delivery Workflow**: Step-by-step navigation triggers: Reached Pickup → Picked Up → In Transit → Deliver with OTP.
* **Earnings & Instant UPI Payout**: Detailed trip earnings, daily milestones, and on-demand UPI withdrawals.
* **Safety & Support**: Emergency SOS trigger and direct grievances filing desk.

### 4. Cold Storage Operator Module
* **Facility Configuration**: Manage total capacity in metric tons, pricing per ton/day, warehouse location, and facility features.
* **Reservation Queue**: Approve or reject slot lease applications with mandatory rejection justifications.
* **Capacity Utilization Visualizers**: Real-time circular SVG gauges displaying leased vs available space.

### 5. Administrator Control Desk
* **Global Ecosystem Telemetry**: Total users, farmers, buyers, delivery partners, listed crops, pending approvals, total orders, and open grievances.
* **Produce Moderation Desk**: Review pending crops, adjust suggested prices, inspect produce imagery, and approve or reject listings.
* **Live APMC Mandi Desk**: Add, update, or remove commodity market prices with instant WebSocket broadcasts to all connected clients.
* **User Governance**: Search and toggle user account statuses (Block / Unblock toggle).
* **Dispute Resolution Desk**: Review complaints submitted by farmers, buyers, or drivers and update resolution states (`PENDING`, `RESOLVED`, `CLOSED`).
* **Platform Financial Parameters**: Configure escrow service fees (%) and transit insurance coverage limits.

### 6. WhatsApp Web Peer-to-Peer Chat
* Real-time Spring Boot WebSocket chat (`ws-mandiplus`) backed by MySQL message persistence.
* Multimedia communication: Photos, Audio voice notes with live recording, and Videos.
* Pinned Mandi+ Admin Helpdesk across all dashboards with pre-built one-click response templates.

---

## 🛠️ Technology Stack

| Layer | Technologies Used |
| :--- | :--- |
| **Frontend** | React 18 (Vite), Vanilla CSS, Tailwind CSS, Lucide Icons, Web Audio API |
| **Backend** | Spring Boot 3.3.0, Spring Security 6 (JWT Authentication), Spring WebSocket |
| **Database** | MySQL 8.x, Spring Data JPA, Hibernate ORM |
| **Payment Gateway** | Razorpay Java SDK, Razorpay Web Checkout, HMAC-SHA256 Webhook Verification |
| **Real-time Engine** | Spring WebSocket Text Protocol, Native Browser WebSockets |
| **Localization** | Context API Tri-Lingual Dictionary (English, Hindi, Marathi) |

---

## 📂 Project Directory Structure

```
MandiPlus/
├── .env.example                               # Root template for environment variables (tracked)
├── .env                                       # Local environment variables (ignored by Git)
├── .gitignore                                 # Git rules ignoring private configs and artifacts
│
├── backend/                                   # Spring Boot REST API & WebSocket Backend
│   ├── src/main/java/com/rtech/mandiplus/
│   │   ├── config/                            # Security, WebSockets & Startup Initializers
│   │   │   ├── SecurityConfig.java            # JWT filters, role path security & CORS
│   │   │   ├── JwtUtils.java                  # JWT token generator, parser & validator (no hardcoded secret)
│   │   │   ├── JwtAuthenticationFilter.java   # Bearer JWT extraction filter
│   │   │   ├── LiveFeedWebSocketHandler.java  # APMC ticker & notification socket handler
│   │   │   ├── WebSocketConfig.java           # Endpoint registration (/ws-mandiplus)
│   │   │   └── DataInitializer.java           # Startup seeder for showcase produce items
│   │   ├── controller/                        # REST Controllers (Auth, Product, Order, Payment, etc.)
│   │   ├── dto/                               # Data Transfer Objects
│   │   ├── entity/                            # JPA Relational Database Entities
│   │   ├── repository/                        # Spring Data JPA Repositories
│   │   ├── service/                           # Core Business Services
│   │   └── MandiPlusApplication.java          # Main Spring Boot Application Bootstrap
│   ├── src/main/resources/
│   │   └── application.properties             # Externalized Spring properties (reads .env / system env)
│   └── pom.xml                                # Maven build descriptors & dependencies
│
├── frontend/                                  # React Client Application (Vite)
│   ├── .env.example                           # Frontend template for public variables (tracked)
│   ├── .env                                   # Local frontend variables (ignored by Git)
│   ├── src/
│   │   ├── components/                        # Reusable UI Components (Checkout, Live Map, Weather, etc.)
│   │   ├── context/                           # AuthContext, LanguageContext
│   │   ├── pages/                             # Role dashboards and marketplace pages
│   │   ├── services/api.js                    # Central Axios client with JWT interceptor
│   │   ├── App.jsx                            # Root application component
│   │   └── index.css                          # Global design system & theme utilities
│   ├── package.json                           # Frontend scripts & dependencies
│   └── vite.config.js                         # Vite build configurations
│
└── database/                                  # Database SQL Scripts
    ├── schema.sql                             # 13 Relational tables with constraints & indexes
    └── data.sql                               # Seed accounts, cold chains, mandi rates & weather
```

---

## 🔒 Security & Environment Configuration Guide

All sensitive credentials, database passwords, JWT signing keys, payment secrets, and third-party tokens are **fully externalized** and decoupled from version control.

### Required Environment Variables

| Variable Name | Required By | Scope | Description & Example Format |
| :--- | :--- | :--- | :--- |
| `SERVER_PORT` | Backend | Backend | Port on which Spring Boot listens (default: `8082`). |
| `DB_URL` | Backend | **Backend Only** | JDBC connection string, e.g. `jdbc:mysql://localhost:3306/mandiplus_db`. |
| `DB_USERNAME` | Backend | **Backend Only** | Database user, e.g. `root`. |
| `DB_PASSWORD` | Backend | **Backend Only** | Database password. Never commit this value. |
| `JWT_SECRET` | Backend | **Backend Only** | Cryptographic HMAC-SHA256 signing key (min 256 bits / 32 chars). |
| `JWT_EXPIRATION_MS` | Backend | **Backend Only** | JWT token lifespan in milliseconds (default: `86400000` = 24 hours). |
| `RAZORPAY_KEY_ID` | Backend & Frontend | Public ID | Razorpay API key ID (e.g. `rzp_test_...`). |
| `RAZORPAY_KEY_SECRET` | Backend | **Backend Only** | Razorpay API secret key. Never expose to client! |
| `RAZORPAY_WEBHOOK_SECRET` | Backend | **Backend Only** | Webhook verification secret configured in Razorpay dashboard. |
| `DATA_GOV_IN_API_KEY` | Backend | **Backend Only** | Official data.gov.in API key for Mandi rates. |
| `MANDIPLUS_OTP_PROVIDER` | Backend | **Backend Only** | SMS gateway provider (`mock` or `fast2sms`). |
| `MANDIPLUS_OTP_API_KEY` | Backend | **Backend Only** | API key for Fast2SMS Indian SMS gateway. |
| `FIREBASE_PROJECT_ID` | Backend & Frontend | Public ID | Firebase project ID (e.g. `mandiplus`). |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | Backend | **Backend Only** | Absolute local file path to private service account JSON. |
| `VITE_API_URL` | Frontend | Public Client | Backend REST API base URL (e.g. `http://localhost:8082/api`). |
| `VITE_WS_PORT` | Frontend | Public Client | WebSocket port (e.g. `8082`). |
| `VITE_GEOAPIFY_API_KEY` | Frontend | Public Client | Geoapify map & geocoding browser key. |
| `VITE_OPENWEATHER_API_KEY` | Frontend | Public Client | OpenWeatherMap API key (optional; defaults to Open-Meteo). |
| `VITE_RAZORPAY_KEY_ID` | Frontend | Public Client | Razorpay key ID for client checkout modal. |
| `VITE_FIREBASE_API_KEY` | Frontend | Public Client | Firebase client SDK API key. |

> [!IMPORTANT]
> **Client vs. Backend Boundary**:
> Variables starting with `VITE_` are compiled directly into the client-side JavaScript bundle and are visible to anyone opening DevTools in their browser.
> **Never** prefix database credentials, JWT secrets, or payment secret keys with `VITE_`.

---

## ⚙️ Step-by-Step Installation & Local Setup (Windows)

### 1. Prerequisites Setup
Verify that the following tools are installed and accessible in your system `Path`:
* **Java JDK 17+**: `java -version`
* **Apache Maven 3.8+**: `mvn -version` (or use the included local Maven in `maven/bin/mvn.cmd`)
* **Node.js (v18+) & NPM**: `node -version` and `npm -version`
* **MySQL Server 8.x**: `mysql -version`

---

### 2. MySQL Database Setup

1. Start your local MySQL service (via Windows Services or terminal `net start MySQL80`).
2. Log into the MySQL terminal:
   ```bash
   mysql -u root -p
   ```
3. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS mandiplus_db;
   EXIT;
   ```
4. Import the table schema and initial seed data:
   ```bash
   mysql -u root -p mandiplus_db < database/schema.sql
   mysql -u root -p mandiplus_db < database/data.sql
   ```

---

### 3. One-Time Environment Configuration

1. **Copy the Root Environment Template**:
   In PowerShell (from the project root):
   ```powershell
   Copy-Item .env.example .env
   ```
   Or in Windows Command Prompt:
   ```cmd
   copy .env.example .env
   ```

2. **Edit `.env` with Your Local Values**:
   Open `.env` in your editor and enter your local configuration:
   ```env
   DB_URL=jdbc:mysql://localhost:3306/mandiplus_db
   DB_USERNAME=root
   DB_PASSWORD=your_actual_mysql_password
   JWT_SECRET=your_secure_random_jwt_secret_min_32_characters
   RAZORPAY_KEY_ID=rzp_test_your_key_id
   RAZORPAY_KEY_SECRET=your_razorpay_secret
   VITE_GEOAPIFY_API_KEY=your_geoapify_key
   ```

3. **Copy the Frontend Template**:
   ```powershell
   Copy-Item frontend\.env.example frontend\.env
   ```
   Configure `frontend\.env` with your public URLs and client keys.

> [!TIP]
> **Preserving Configuration across Git Updates**:
> Both `.env` and `frontend\.env` are ignored by `.gitignore`. You can pull updates, switch branches, or rebuild without losing your local configuration or re-entering keys.

---

### 4. Running the Spring Boot Backend

Spring Boot automatically reads your `.env` file via `spring.config.import`:

```powershell
cd backend
mvn spring-boot:run
```
*(If Maven is not in your system Path, use: `..\maven\bin\mvn.cmd spring-boot:run`)*

#### Running via IntelliJ IDEA:
1. Open the project in IntelliJ IDEA.
2. Navigate to `MandiPlusApplication.java`.
3. Open **Run/Debug Configurations** → **Environment variables**.
4. (Optional) You can define overrides directly in the Run Configuration, or let Spring Boot automatically import `.env`.

*The backend will boot up and listen at `http://localhost:8082`.*

---

### 5. Running the React Frontend

Open a separate terminal window:

```powershell
cd frontend
npm install
npm run dev
```

*Access the application in your browser at `http://localhost:5173`.*

---



## 🛡️ Git & GitHub Publishing Safety

Before publishing this repository to GitHub or any public remote:

1. **Verify Ignored Files**:
   Ensure `.env` and local properties are ignored:
   ```bash
   git status --ignored
   ```
   Verify that `.env` appears under `Ignored files` (`!! .env`).

2. **Check for Uncommitted Secrets**:
   Run a check to confirm no secret file is staged:
   ```bash
   git check-ignore -v .env frontend/.env
   ```

3. **Security Warning & Credential Rotation**:
   > [!WARNING]
   > Adding files to `.gitignore` does **not** erase secrets that were committed in past Git history.
   > If any real API key, Razorpay secret, or production database password was previously committed or pushed to a public remote:
   > - **Rotate the credential immediately** in the respective provider console (Razorpay Dashboard, Geoapify Console, MySQL, etc.).
   > - Do not reuse compromised passwords or signing secrets.
   > - To permanently remove sensitive files from historical commits before publishing, use tools like `git-filter-repo` or BFG Repo-Cleaner after creating a full repository backup.

---

## 👥 Core Team & Leadership

* **Rohan Gaikwad** — Founder & Lead Architect
* **Krushnal Chaudhari** — Co-Founder & Operations Lead
* **Sanjana Bagul** — Co-Founder & Product Strategist

---
