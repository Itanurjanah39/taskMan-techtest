# TaskMan - Aplikasi Manajemen Tugas (Android)

TaskMan adalah aplikasi Android native yang dirancang untuk mengelola daftar tugas (tasks) harian, mingguan, maupun bulanan. Aplikasi ini dikembangkan menggunakan Java dan mengikuti standar arsitektur modern Android untuk memastikan performa yang cepat, layout yang responsif di berbagai ukuran layar (mobile & tablet/iPad), serta UX yang premium.

---

## 1. Arsitektur & State Management

### Arsitektur: MVVM (Model-View-ViewModel) + Repository Pattern
Aplikasi ini diimplementasikan menggunakan arsitektur **MVVM (Model-View-ViewModel)** yang dipadukan dengan **Repository Pattern**:
*   **Model:** Berkas data class POJO yang terletak di `com.technicaltest.taskman.data.model` (seperti `TaskResponse`, `TaskRequest`, dll) yang merepresentasikan skema data dari server.
*   **View:** Aktivitas (`Activity`) dan Fragmen (`Fragment`) yang terletak di `com.technicaltest.taskman.ui`. Tanggung jawab View murni hanya untuk rendering UI dan menangkap interaksi pengguna.
*   **ViewModel:** Terletak di `com.technicaltest.taskman.data.viewmodel` (seperti `TaskViewModel`, `LoginViewModel`). Bertugas menampung data UI, berinteraksi dengan Repository, serta menjaga status data saat terjadi rotasi layar (*configuration change*).
*   **Repository:** Terletak di `com.technicaltest.taskman.data.repository` (seperti `TaskRepository`, `AuthRepository`). Bertindak sebagai *single source of truth* untuk mengabstraksi sumber data (lokal/remote) dari ViewModel.

**Alasan Pemilihan Arsitektur:**
1.  *Separation of Concerns:* Pemisahan kode logika bisnis (ViewModel/Repository) dari kode tampilan (View) membuat kode lebih rapi, terstruktur, dan mudah dibaca.
2.  *Lifecycle Awareness:* ViewModel menjaga data tetap bertahan meskipun Activity hancur dan dibuat ulang karena rotasi layar.
3.  *Testability:* Kode logika bisnis di dalam ViewModel dan Repository dapat diuji secara terpisah (*Unit Testing*) tanpa harus melibatkan dependensi Android UI.

### State Management: LiveData & Observer Pattern
State management di dalam aplikasi ini menggunakan **LiveData** yang dibungkus dalam generic class helper **`Resource<T>`**:
*   **Resource Class (`com.technicaltest.taskman.data.network.Resource`)** memaketkan data bersama dengan `Status` (`SUCCESS`, `ERROR`, `LOADING`) dan pesan kesalahan (`message`).
*   ViewModel mengekspos data bertipe `LiveData<Resource<T>>`.
*   View mengamati (*observe*) LiveData tersebut. Ketika status berubah, View akan langsung mengupdate UI secara reaktif (misalnya menampilkan shimmer loading saat `LOADING`, merender data saat `SUCCESS`, atau memunculkan dialog kesalahan saat `ERROR`).

---

## 2. API & Endpoint yang Digunakan

Aplikasi ini berinteraksi dengan API Server menggunakan pustaka **Retrofit**. Terdapat dua interface API yang didefinisikan:

### A. Authentication API (`ApiService.java`)
Digunakan untuk mengelola sesi dan akun pengguna.
*   `POST api/auth/sign-in` : Melakukan autentikasi masuk (Login).
*   `GET api/auth/account` : Mengambil informasi profil akun pengguna yang sedang login.
*   `POST api/auth/sign-out` : Melakukan proses keluar (Logout).

### B. Tasks API (`PublicApiService.java`)
Digunakan untuk mengelola data tugas (CRUD).
*   `GET api/tasks` : Mengambil seluruh daftar tugas.
*   `GET api/tasks/{id}` : Mengambil detail informasi satu tugas berdasarkan ID.
*   `POST api/tasks` : Membuat tugas baru.
*   `PUT api/tasks/{id}` : Memperbarui detail tugas yang sudah ada.
*   `DELETE api/tasks/{id}` : Menghapus tugas berdasarkan ID.

---

## 3. Penyimpanan Sesi Pengguna (Session Storage)

Sesi login pengguna disimpan secara lokal menggunakan **`SharedPreferences`** Android yang dibungkus di dalam kelas **`SessionManager`** (`com.technicaltest.taskman.data.auth.SessionManager`).

**Data yang disimpan meliputi:**
*   `is_logged_in` (Boolean): Menandakan status login aktif.
*   `auth_token` (String): Token JWT yang digunakan untuk melakukan autentikasi ke endpoint API yang membutuhkan kredensial.
*   `user_role` (String): Role pengguna (misal: Admin/User).
*   `user_email` (String): Alamat email pengguna.

**Alur Kerja Sesi:**
1.  Saat Login Sukses: Token dan email disimpan menggunakan `editor.apply()`.
2.  Interseptor API: Token disisipkan secara otomatis ke dalam header HTTP setiap kali aplikasi melakukan request API (kecuali endpoint dengan header `No-Authentication: true`).
3.  Saat Logout/Sesi Berakhir: Memanggil `clearSession()` yang membersihkan seluruh data di `SharedPreferences`, lalu mengarahkan pengguna kembali ke `LoginActivity`.

---

## 4. Pengelolaan Loading, Skeleton (Shimmer), dan Error State

Untuk memberikan pengalaman pengguna yang mulus (*seamless UX*), aplikasi menangani 3 state transisi data sebagai berikut:

### A. Loading State (Pembuatan & Pengeditan Task)
Saat menyimpan task baru atau mengedit task di bottom sheet:
*   Aplikasi menampilkan loading selama **3 detik** untuk memberi transisi visual yang jelas kepada pengguna.
*   Selama masa loading, seluruh input field dan tombol di dalam dialog dinonaktifkan (`setEnabled(false)`) dan dialog tidak dapat ditutup (`setCancelable(false)`) untuk menghindari pengiriman data ganda.
*   Teks pada tombol simpan berubah menjadi `"Loading..."`.

### B. Skeleton / Shimmer State (Membaca Daftar Task)
Saat Fragmen Beranda (`HomeFragment`) dan Fragmen Daftar Tugas (`TaskFragment`) sedang memproses pengambilan data dari server:
*   `ProgressBar` melingkar bawaan digantikan dengan efek skeleton animasi mengalir **Facebook Shimmer** (`ShimmerFrameLayout`).
*   Menggunakan layout placeholder khusus `item_task_shimmer.xml` yang memiliki bentuk kartu abu-abu tanpa teks untuk merepresentasikan visual struktur kartu tugas sebelum data asli dimuat.

### C. Error State
Ketika request API gagal (misal karena jaringan terputus atau token kadaluwarsa):
*   Aplikasi menampilkan **Empty State** menggunakan helper `EmptyStateUtils` yang memunculkan ilustrasi gambar disertai pesan kesalahan dan tombol *Retry* untuk memuat ulang data.
*   Kesalahan sistemik lainnya ditampilkan menggunakan dialog error kustom yang atraktif melalui `DialogUtils.showErrorDialog`.

---

## 5. Cara Setup, Menjalankan, Build, dan Testing Aplikasi

### Persyaratan Awal (Prerequisites)
*   **Android Studio** (versi Ladybug atau yang lebih baru direkomendasikan).
*   **JDK 17** (pilih Gradle JDK 17 pada Android Studio Settings).
*   Handphone Android fisik (aktifkan Developer Options & USB Debugging) atau Emulator (AVD) dengan API Level 26+.

### A. Setup Proyek
1.  Clone atau buka folder proyek **TaskMan** ke dalam Android Studio.
2.  Tunggu hingga proses sinkronisasi Gradle (*Gradle Sync*) selesai dilakukan secara otomatis.

### B. Menjalankan Aplikasi (Run)
1.  Hubungkan perangkat fisik Anda melalui kabel USB atau jalankan Emulator Android.
2.  Klik tombol **Run** (ikon segitiga hijau) di toolbar atas Android Studio, atau jalankan perintah CLI berikut:
    ```bash
    ./gradlew installDebug
    ```

### C. Melakukan Build APK
Untuk melakukan kompilasi proyek dan menghasilkan berkas APK Debug:
```bash
./gradlew assembleDebug
```
Berkas APK hasil build akan terletak di direktori:
`app/build/outputs/apk/debug/app-debug.apk`


