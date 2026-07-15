# Aksoro - Aplikasi Transliterasi Aksara Jawa 📜

[![Platform](https://img.shields.io/badge/Platform-Android_Native-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![AI](https://img.shields.io/badge/AI_Engine-PyTorch_Mobile-EE4C2C?logo=pytorch&logoColor=white)](https://pytorch.org/mobile/home/)
[![CV](https://img.shields.io/badge/Vision-OpenCV_SDK-5C3EE8?logo=opencv&logoColor=white)](https://opencv.org/)

Aksoro adalah aplikasi *mobile* Android *native* yang dirancang untuk menerjemahkan citra Aksara Jawa ke teks Latin secara *end-to-end*. Proyek ini dikembangkan sebagai implementasi Tugas Akhir (Skripsi) pada Program Studi Informatika, Universitas Pembangunan Jaya, guna memfasilitasi pelestarian aksara tradisional melalui teknologi *Artificial Intelligence*.

Sistem ini beroperasi dengan memadukan pengolahan citra (*Computer Vision*) dan *Deep Learning*, menggunakan arsitektur *hybrid* **MobileNetV2** (klasifikasi karakter) dan **Seq2Seq LSTM** (transliterasi bahasa). Seluruh proses inferensi berjalan langsung di dalam perangkat (*On-Device/Native Engine*).

---

## 🛠️ Arsitektur & Teknologi Utama

- **Platform:** Android Native (Kotlin), Min SDK 24 (Android 7.0)
- **Antarmuka Pengguna (UI):** Jetpack Compose dengan `ActivityResultContracts`
- **Prapemrosesan Citra:** OpenCV SDK (Segmentasi, Morfologi, *Bounding Box*)
- **Mesin Inferensi:** PyTorch Mobile (Lite Interpreter `.ptl`)
- **Modul Tambahan:** `android-image-cropper` (Pemotongan citra dinamis)

---

## 🚀 Fitur & Alur Kerja Sistem (Pipeline)

Sistem `AksoroEngine` memproses masukan gambar hingga menjadi teks melalui tahapan berikut:

1. **Pengambilan & Pemotongan Gambar:**
   Menggunakan integrasi kamera/galeri yang langsung dihubungkan dengan fitur *cropping* bergaris bantu (guidelines) untuk memfokuskan citra pada teks.
2. **Prapemrosesan & Segmentasi (OpenCV):**
   - Konversi Grayscale, Reduksi *Noise* (Gaussian Blur 5x5), dan Binarisasi (OTSU + THRESH_BINARY_INV).
   - Operasi dilatasi morfologi (kernel 3x10) untuk merekatkan elemen aksara.
   - Deteksi kontur dan pembuatan *Bounding Box* dinamis.
   - **Optimasi Padding:** Menggunakan *uniform center padding* (simetris 10 piksel) dengan latar belakang putih pekat (`Color.WHITE`). Metode ini diterapkan untuk mencegah *data leakage* yang sebelumnya terjadi pada teknik *contextual padding*, serta menghilangkan *bug* Alpha-Channel saat masuk ke dalam tensor.
3. **Inferensi Klasifikasi (MobileNetV2):**
   - Normalisasi matriks tensor (Mean: `[0.485, 0.456, 0.406]`, Std: `[0.229, 0.224, 0.225]`).
   - Ekstraksi *logits*, kalkulasi *Softmax*, dan penerapan *Confidence Threshold* (25%).
4. **Pemenggalan Kata & Transliterasi (Seq2Seq LSTM):**
   - Algoritma *Chunking* linguistik Jawa (memisahkan nglegena, pasangan, dan sandhangan dengan aman).
   - *Decoding Tensor* dinamis tanpa *hardcode* limitasi untuk mencegah kalimat terpotong sebelum token `<eos>`.
5. **Visual Debugging (Sinar-X):**
   Sistem mengembalikan struktur ganda (`Pair<List<Bitmap>, Bitmap>`) sehingga antarmuka dapat menampilkan *Bounding Box* deteksi OpenCV secara *real-time* kepada pengguna.

---

## ⚙️ Panduan Instalasi & Kompilasi

### Prasyarat:
- Android Studio (dengan konfigurasi `build.gradle.kts` / Kotlin DSL)
- NDK (Native Development Kit) untuk kompilasi library C++

### Langkah-langkah:
1. **Clone repositori:**
   ```bash
   git clone https://github.com/dikaarnnd/myaksoro.git
   ```
2. **Impor Modul OpenCV:**
   Pastikan modul OpenCV (versi lokal) telah terhubung. Cek berkas `build.gradle (Module :opencv)` dan pastikan `compileSdkVersion` & `targetSdkVersion` diatur ke **36**, dan `minSdkVersion` ke **24**.
3. **Persiapkan Model AI:**
   Letakkan file model PyTorch Mobile hasil konversi (`mobilenetv2_aksago.ptl` dan `seq2seq_aksago.ptl`) ke dalam direktori `app/src/main/assets/`.
4. **Sinkronisasi Gradle:**
   Jalankan *Gradle Sync*. *Project* ini telah dikonfigurasi dengan blok `packaging { jniLibs { pickFirsts.add("**/libc++_shared.so") } }` untuk menyelesaikan konflik pustaka C++ antara OpenCV dan PyTorch.
5. **Jalankan Aplikasi (Run):**
   Sambungkan perangkat Android atau jalankan emulator (direkomendasikan API 26+ untuk dukungan *adaptive-icon*), lalu klik **Run** di Android Studio.

---

## 🔬 Catatan Pengembangan Model (Sim-to-Real Gap)

Untuk pengembang yang ingin melatih ulang model, perhatikan penyesuaian (*troubleshooting*) berikut saat mengekspor model PyTorch ke format Mobile (`.ptl`):

- **Menghindari Korupsi Bobot MobileNetV2:**
  Lewati pemanggilan fungsi `optimize_for_mobile()` pada skrip konversi yang dikonfigurasi secara mandiri saat mengekspor model. Lakukan ekspor langsung menggunakan *Traced Model* (`_save_for_lite_interpreter`). Hal ini menjaga struktur fusi `Conv2d` dan `BatchNorm`, sehingga akurasi *on-device* tetap setara dengan pengujian di *environment* Python.
- **Keselarasan Pemetaan Kelas:**
  Pastikan *array classNames* di Kotlin disusun murni alfabetis (A-Z), mengikuti sifat *default* pustaka `ImageFolder` saat fase pelatihan.
- **Ekspor Seq2Seq LSTM:**
  Gunakan `torch.jit.script` alih-alih *tracing*, karena arsitektur LSTM memiliki perulangan (*looping*) pada komponen Decoder.

---

## 📄 Lisensi

Hak Cipta &copy; 2026. Aplikasi ini dikembangkan untuk tujuan penelitian akademik.
