# 📚 Kütüphane Yönetim Sistemi (Library Management System)

Kütüphaneciler ve öğrencilerin kitapları, ödünç alma işlemlerini, rezervasyonları ve kullanıcı hesaplarını yönetmelerine olanak tanıyan tam kapsamlı bir web tabanlı Kütüphane Yönetim Sistemi.

## 🛠 Teknoloji Yığını

| Katman | Teknoloji |
|--------|-----------|
| Backend | Java 21, Spring Boot 3.3 |
| Güvenlik | Spring Security 6 + JWT |
| Veritabanı | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Frontend | React 18 (Vite) |
| API Dokümantasyon | SpringDoc OpenAPI (Swagger) |
| Konteyner | Docker + Docker Compose |

## 📁 Proje Yapısı

```
├── src/main/java/com/library/
│   ├── config/          # Konfigürasyon sınıfları
│   ├── controller/      # REST API Controller'ları
│   ├── dto/             # Data Transfer Object'leri
│   ├── entity/          # JPA Entity sınıfları
│   ├── exception/       # Exception handler'lar
│   ├── repository/      # Data access layer
│   ├── security/        # JWT ve güvenlik
│   ├── service/         # İş mantığı katmanı
│   └── util/            # Yardımcı sınıflar
├── src/main/resources/
│   ├── application.yml
│   └── data/            # Kaggle Books Dataset (CSV)
├── frontend/            # React uygulaması
├── docker-compose.yml
└── pom.xml
```

## 🚀 Başlangıç

### Gereksinimler
- Java 21+
- Maven 3.9+
- PostgreSQL 16+
- Node.js 20+ (frontend için)
- Docker & Docker Compose (opsiyonel)

### Veritabanı Kurulumu
```sql
CREATE DATABASE librarydb;
```

### Backend Çalıştırma
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend Çalıştırma
```bash
cd frontend
npm install
npm run dev
```

### Docker ile Çalıştırma
```bash
docker-compose up --build
```

## 📊 Veri Seti
Başlangıç kütüphane envanteri olarak [Kaggle Books Dataset](https://www.kaggle.com/datasets/saurabhbagchi/books-dataset) kullanılmaktadır.

## 📝 API Dokümantasyonu
Uygulama çalışırken Swagger UI'a erişim:
```
http://localhost:8080/swagger-ui.html
```

## 👥 Roller
| Rol | Yetkiler |
|-----|----------|
| ADMIN | Tüm yetkiler, kullanıcı yönetimi |
| LIBRARIAN | Kitap yönetimi, ödünç/iade işlemleri |
| STUDENT | Kitap arama, ödünç alma, rezervasyon |

## 📄 Lisans
Bu proje IYD 328 - İş Yeri Deneyimi dersi kapsamında geliştirilmiştir.
