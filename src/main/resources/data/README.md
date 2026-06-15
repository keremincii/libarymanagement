# Books Dataset

Bu dizine Kaggle Books Dataset'inden `BX-Books.csv` dosyasını koyun.

## İndirme Adımları

1. https://www.kaggle.com/datasets/saurabhbagchi/books-dataset adresine gidin
2. "Download" butonuna tıklayın (Kaggle hesabı gerekli)
3. İndirilen ZIP dosyasını açın
4. `BX-Books.csv` dosyasını bu dizine kopyalayın

## Beklenen Dosya Yapısı

```
src/main/resources/data/
├── BX-Books.csv      <-- Bu dosyayı buraya koyun
└── README.md
```

## CSV Formatı

- Ayırıcı: Noktalı virgül (;)
- Encoding: ISO-8859-1 (Latin-1)
- Sütunlar: ISBN, Book-Title, Book-Author, Year-Of-Publication, Publisher, Image-URL-S, Image-URL-M, Image-URL-L

Uygulama ilk başlatıldığında bu dosyayı otomatik olarak okuyup veritabanına aktaracaktır.
