# KNN vs Decision Tree Classifier

[English](README.md)

Gerçek perakende satış verisini kullanarak bir müşterinin hangi ürün kategorisinden alışveriş yaptığını tahmin eden bir Java masaüstü uygulaması. Herhangi bir makine öğrenmesi kütüphanesi kullanmadan sıfırdan yazdığım iki sınıflandırıcıyı karşılaştırır: **K-En Yakın Komşu (KNN)** ve **Karar Ağacı**.

## Ne yapar?

- **Excel'den veri okur** (Apache POI). Sütunlar başlık adına göre eşleştirilir, sıraları önemli değildir. Virgüllü ondalıklar (`12,5`) ve hatalı satırlar işlenir.
- **Ön işleme:** cinsiyet 0/1 olarak kodlanır, markalara sayısal kimlik verilir (label encoding), miktar, fiyat ve satır toplamı min-max normalizasyonu ile 0-1 arasına çekilir. Min-max değerleri yalnızca eğitim verisinden öğrenilir.
- **KNN:** Öklid uzaklığı ve en yakın k komşunun çoğunluk oyu.
- **Karar Ağacı:** bölmeler bilgi kazancına (entropi) göre seçilir, maksimum derinlik sınırı vardır.
- **Değerlendirme:** rastgele eğitim/test ayrımı, doğruluk ve karışıklık matrisi (confusion matrix).
- **Swing arayüzü:** eğitim oranıyla tek dosya ya da ayrı eğitim ve test dosyaları. Çalıştırma geçmişi, doğruluk ve süre grafikleri (JFreeChart). Veri seti Türkçe olduğu için arayüz dili Türkçedir.

## Sonuçlar

Veri seti: 4.625 satış kaydı, 11 ürün kategorisi, 266 marka. %80 eğitim, %20 test. Her rakam, her seferinde farklı rastgele bölmeyle yapılan 20 çalıştırmanın ortalamasıdır (tüm modeller aynı 20 bölmeyi kullanır).

| Model | Doğruluk (ortalama) | Aralık | Süre (eğitim + test) |
|---|---|---|---|
| KNN, k = 3 | **%89,4** | %87,2 - 91,1 | ~0,2 sn |
| Karar Ağacı, maksimum derinlik = 5 | %63,1 | %58,6 - 69,3 | ~0,7 sn |
| Karar Ağacı, maksimum derinlik = 10 | %80,9 | %77,1 - 84,6 | ~0,8 sn |
| Karar Ağacı, maksimum derinlik = 15 | **%87,8** | %84,0 - 90,8 | ~0,8 sn |

Karşılaştırma için: her zaman en sık görülen kategoriyi tahmin etmek %36,3 doğruluk verir.

## Bulgular: KNN neden daha iyi görünüyor?

İlk bakışta KNN, Karar Ağacı'ndan çok daha iyi görünüyordu (%89'a karşı %63). Nedenini anlamak için birkaç ek deney yaptım:

| Deney | Doğruluk |
|---|---|
| Model yok: müşterinin **markasının** en sık görülen kategorisini tahmin et | %88,8 |
| KNN, marka kimliği diğer özellikler gibi 0-1 arasına ölçeklenmiş | %76,0 |
| KNN, marka hiç kullanılmadan | %53,6 |

- **Sonucu büyük ölçüde marka belirliyor.** 266 markanın 242'si yalnızca tek bir kategoride satılıyor, bu yüzden sadece marka bile %88,8 veriyor.
- **Marka kimliği ölçeklenmiyor.** 0'dan 265'e kadar gidiyor, diğer özellikler ise 0 ile 1 arasında. Bu yüzden Öklid uzaklığını domine ediyor ve KNN çoğunlukla aynı markadan kayıtları buluyor. Marka kimliği diğer özellikler gibi ölçeklenince KNN %76'ya düşüyor.
- **Derinlik 5 ağaç için çok sığ.** 266 marka kimliği olduğu için ağacın daha fazla bölmeye ihtiyacı var. Derinlik 15'te %87,8'e çıkıp KNN'e yaklaşıyor.

Yani aradaki fark, bir algoritmanın genel olarak daha iyi olmasından çok özelliklerin kodlanma şekli ve derinlik ayarından kaynaklanıyor.

**Olası sonraki adımlar:** markalar için one-hot encoding, k-katlı çapraz doğrulama (k-fold cross-validation), k ve maksimum derinlik ayarlarının optimize edilmesi.

## Proje yapısı

```
src/main/java/com/mlproject/
├── Main.java                     giriş noktası
├── core/                         IClassifier (arayüz), BaseAlgorithm (doğruluk, karışıklık matrisi)
├── algorithms/                   KNNClassifier, DecisionTreeClassifier
├── data/                         DataLoader (Excel), UserRecord
├── preprocessing/PreProcessor    kodlama ve min-max normalizasyonu
├── evaluation/Evaluator          eğitim/test ayrımı
└── gui/MainFrame                 Swing arayüzü
src/test/java/com/mlproject/      birim testleri
examples/sample-sales.xlsx        uygulamayı denemek için küçük, uydurma veri seti
```

İki algoritma da `IClassifier` arayüzünü uygular ve `BaseAlgorithm` sınıfından türer, böylece doğruluk ve karışıklık matrisi kodu ortak kullanılır. Yeni bir algoritma eklemek için yalnızca `train()` ve `predict()` yazmak yeterlidir.

## Başlarken

Gereksinimler: JDK 21 veya üstü ve Maven.

1. Projeyi IntelliJ IDEA'da açın. Bağımlılıklar `pom.xml` üzerinden yüklenir.
2. `Main.java` dosyasını çalıştırın.
3. Bir Excel dosyası seçin, algoritmayı ve parametresini belirleyip **Modeli Çalıştır** düğmesine basın.

Excel dosyasında şu sütunlar bulunmalıdır: `CLIENTCODE`, `GENDER`, `AMOUNT`, `PRICE`, `LINENETTOTAL`, `BRAND`, `CATEGORY_NAME1`. Yukarıdaki sonuçlar için kullanılan veri seti gerçek satış verisi olduğu için bu depoda yer almıyor. Uygulamayı denemek için `examples/sample-sales.xlsx` dosyasını kullanabilirsiniz: aynı sütunlara sahip, 600 uydurma satır (kurgusal marka ve müşteriler). Bu dosyadaki doğruluk değerleri yukarıdaki tabloyla karşılaştırılamaz.

## Testler

```
mvn test
```

27 birim testi (JUnit 5); KNN oylamasını, karar ağacını (entropi, bölme, derinlik sınırı), doğruluk ve karışıklık matrisini, ön işlemeyi (cinsiyet kodlama, min-max ölçekleme, marka kimlikleri), eğitim/test ayrımını, Excel okumayı ve Türkçe dil ayarlı sistemlerde doğru çalışmayı kapsar.

## Teknolojiler

Java 21, Maven, Apache POI, JFreeChart, Swing, JUnit 5
