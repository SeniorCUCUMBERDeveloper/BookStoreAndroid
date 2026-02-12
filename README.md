# BookStore

## О проекте
**BookStore** — это Android-приложение книжного магазина, в котором пользователь может:
- зарегистрироваться и войти по email/password;
- просматривать каталог книг из Firestore;
- искать книги по названию и автору;
- открывать карточку книги, читать описание и отзывы;
- добавлять книги в корзину и оформлять заказ;
- просматривать историю заказов;
- редактировать профиль (имя, телефон, адрес доставки);
- переключать светлую/тёмную тему.

Проект построен как клиентское приложение на Jetpack Compose с облачным backend-слоем в Firebase. Каталог и пользовательские данные живут в Cloud Firestore, а локально сохраняется история просмотренных книг для текущего пользователя.

## Технологический стек
- **Язык и платформа:** Kotlin, Android SDK (minSdk 26, targetSdk 35)
- **UI:** Jetpack Compose, Material 3, Navigation Compose
- **Архитектура UI:** MVVM (ViewModel + StateFlow)
- **DI:** Koin
- **Асинхронность:** Kotlin Coroutines + Flow
- **Локальное хранение:** Room (`viewed_books`)
- **Настройки приложения:** DataStore Preferences (настройка темы)
- **Облачные сервисы:** Firebase Authentication, Cloud Firestore
- **Фоновая работа:** WorkManager (периодический worker-напоминание)
- **Загрузка изображений:** Coil 3
- **Сборка и инструменты:** Gradle Kotlin DSL, KSP, Google Services plugin

## Архитектура
Проект имеет слоистую структуру с разделением ответственности по пакетам:

### 1) `presentation`
Слой интерфейса на Compose:
- `screen` — экраны (`Auth`, `Search`, `SearchResults`, `BookDetail`, `Checkout`, `Profile`, `OrderHistory`, `Settings`, `About`, `FAQ`);
- `viewmodel` — состояние и логика экранов;
- `navigation` — граф навигации и маршруты;
- `theme` — тема, типографика, размеры.

Особенности слоя:
- root-навигация зависит от сессии пользователя;
- есть реакция на офлайн-состояние (snackbar о входе по сохранённой сессии);
- формы авторизации, профиля и checkout валидируются на уровне ViewModel.

### 2) `domain`
Слой контрактов и моделей:
- модели: `Book`, `BookReview`, `UserProfile`, `Order`, `OrderItem`, `UserSession`;
- интерфейсы репозиториев: `BooksRepository`, `UserRepository`, `OrdersRepository`, `ReviewsRepository`, `CartRepository`, `ThemeRepository`.

Это независимый от инфраструктуры слой: presentation работает через интерфейсы, а не через Firebase/Room напрямую.

### 3) `data`
Слой реализаций репозиториев и источников данных:
- `firebase`:
  - `UserRepositoryImpl` — auth-сессия, login/register/reset, профиль;
  - `OrdersRepositoryImpl` — создание/чтение/отмена заказов;
  - `ReviewsRepositoryImpl` — чтение/добавление отзывов;
- `repository`:
  - `BooksRepositoryImpl` — чтение каталога из `catalog`, поиск, подборки, просмотренные;
  - `CartRepositoryImpl` — корзина в `users/{uid}/cart`;
- `local`:
  - Room БД `BookStoreDatabase` + `ViewedDao` + `ViewedBookEntity`;
- `prefs`:
  - `ThemeRepositoryImpl` — хранение настройки темы в DataStore.

### 4) `di`
- `AppModule` регистрирует зависимости Koin;
- связывает интерфейсы domain-слоя с реализациями data-слоя;
- конфигурирует Firebase, Room и ViewModel.

### 5) `worker`
- `FeaturedSyncWorker` + `SyncScheduler`;
- периодическая задача каждые 12 часов;
- отправляет reminder-уведомление о магазине (с учётом разрешения на уведомления).

---

### Поток данных
Основной поток данных в приложении:

**Compose UI → ViewModel → domain-интерфейс репозитория → data-реализация → Firebase/Room/DataStore → обратно в UI через Flow/StateFlow.**

