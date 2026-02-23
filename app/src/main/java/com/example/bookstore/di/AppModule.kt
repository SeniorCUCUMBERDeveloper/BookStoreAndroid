package com.example.bookstore.di

import androidx.room.Room
import com.example.bookstore.data.firebase.OrdersRepositoryImpl
import com.example.bookstore.data.firebase.UserRepositoryImpl
import com.example.bookstore.data.firebase.ReviewsRepositoryImpl
import com.example.bookstore.data.local.BookStoreDatabase
import com.example.bookstore.data.prefs.ThemeRepositoryImpl
import com.example.bookstore.data.repository.BooksRepositoryImpl
import com.example.bookstore.data.repository.CartRepositoryImpl
import com.example.bookstore.data.repository.FaqRepositoryImpl
import com.example.bookstore.domain.repository.BooksRepository
import com.example.bookstore.domain.repository.CartRepository
import com.example.bookstore.domain.repository.OrdersRepository
import com.example.bookstore.domain.repository.ThemeRepository
import com.example.bookstore.domain.repository.FaqRepository
import com.example.bookstore.domain.repository.ReviewsRepository
import com.example.bookstore.domain.repository.UserRepository
import com.example.bookstore.presentation.viewmodel.AuthViewModel
import com.example.bookstore.presentation.viewmodel.BookDetailViewModel
import com.example.bookstore.presentation.viewmodel.CheckoutViewModel
import com.example.bookstore.presentation.viewmodel.FaqViewModel
import com.example.bookstore.presentation.viewmodel.ProfileViewModel
import com.example.bookstore.presentation.viewmodel.SearchViewModel
import com.example.bookstore.presentation.viewmodel.SessionViewModel
import com.example.bookstore.presentation.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single {
        Room.databaseBuilder(get(), BookStoreDatabase::class.java, "bookstore.db")
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { get<BookStoreDatabase>().viewedDao() }
    single { get<BookStoreDatabase>().faqDao() }

    singleOf(::BooksRepositoryImpl) bind BooksRepository::class
    singleOf(::UserRepositoryImpl) bind UserRepository::class
    singleOf(::OrdersRepositoryImpl) bind OrdersRepository::class
    singleOf(::ThemeRepositoryImpl) bind ThemeRepository::class
    singleOf(::CartRepositoryImpl) bind CartRepository::class
    singleOf(::ReviewsRepositoryImpl) bind ReviewsRepository::class
    singleOf(::FaqRepositoryImpl) bind FaqRepository::class

    viewModelOf(::AuthViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::BookDetailViewModel)
    viewModelOf(::CheckoutViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SessionViewModel)
    viewModelOf(::ThemeViewModel)
    viewModelOf(::FaqViewModel)
}
