# 📚 Kütüphane Yönetim Sistemi (Library Management System)

Kütüphaneciler ve öğrencilerin kitapları, ödünç alma işlemlerini, rezervasyonları ve kullanıcı hesaplarını yönetmelerine olanak tanıyan tam kapsamlı bir web tabanlı Kütüphane Yönetim Sistemi.

### 🔐 Yönetici ve Kütüphaneci Hesapları (Test İçin)
Güvenlik standartları gereği, dışarıdan `/api/auth/register` veya arayüz üzerinden kayıt olan her kullanıcı sisteme otomatik olarak **STUDENT (Öğrenci)** yetkisiyle kaydedilir.

Projeyi değerlendirecek kişilerin sistemin farklı yetki seviyelerini anında test edebilmesi için **uygulama başlatıldığında otomatik olarak varsayılan yönetici ve kütüphaneci hesapları oluşturulur**.

Projeyi `git clone` yapıp çalıştıran herkes şu bilgilerle direkt giriş yapıp yetkileri test edebilir:

**Yönetici (Admin) Hesabı:**
- **Kullanıcı Adı:** `admin`
- **Şifre:** `admin123`

**Kütüphaneci (Librarian) Hesabı:**
- **Kullanıcı Adı:** `librarian`
- **Şifre:** `librarian123`

*(Normal bir öğrenci hesabını test etmek isterseniz de Kayıt Ol (Register) ekranından kendiniz bir tane oluşturabilirsiniz.)*

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
# .env dosyasını oluşturun
cp .env.example .env

# Tüm servisleri başlatın
docker-compose up --build

# Arka planda çalıştırmak için
docker-compose up --build -d

# Logları görmek için
docker-compose logs -f

# Durdurmak için
docker-compose down

# Veritabanı verisini de silmek için
docker-compose down -v
```

### Docker Mimarisi
```
┌──────────────────────────────────────────────┐
│                  Docker Network              │
│                                              │
│  ┌──────────┐  ┌──────────┐  ┌───────────┐  │
│  │ Frontend │  │ Backend  │  │ PostgreSQL│  │
│  │  (Nginx) │──│ (Spring) │──│   (DB)    │  │
│  │  :80     │  │  :8080   │  │   :5432   │  │
│  └──────────┘  └──────────┘  └───────────┘  │
│                                              │
└──────────────────────────────────────────────┘
```

**Erişim adresleri:**
- Frontend: http://localhost
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## 📊 Veri Seti
Başlangıç kütüphane envanteri olarak [Kaggle Books Dataset](https://www.kaggle.com/datasets/saurabhbagchi/books-dataset) kullanılmaktadır.

CSV dosyasını `src/main/resources/data/BX-Books.csv` konumuna yerleştirin. Uygulama ilk başlatıldığında veriyi otomatik olarak veritabanına aktarır.

## 📝 API Dokümantasyonu
Uygulama çalışırken Swagger UI'a erişim:
```
http://localhost:8080/swagger-ui.html
```

### Temel API Endpoint'leri
| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | /api/auth/register | Kullanıcı kaydı |
| POST | /api/auth/login | Giriş |
| GET | /api/books | Kitap listesi |
| GET | /api/books/search | Kitap arama |
| POST | /api/borrow | Ödünç al |
| POST | /api/borrow/return/{id} | İade et |
| POST | /api/reservations | Rezervasyon yap |
| GET | /api/reports/summary | Dashboard |

## 👥 Roller
| Rol | Yetkiler |
|-----|----------|
| ADMIN | Tüm yetkiler, kullanıcı yönetimi |
| LIBRARIAN | Kitap yönetimi, ödünç/iade işlemleri, raporlar |
| STUDENT | Kitap arama, ödünç alma, rezervasyon |

## 📄 Lisans
Bu proje IYD 328 - İş Yeri Deneyimi dersi kapsamında geliştirilmiştir.
