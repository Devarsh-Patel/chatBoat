package com.example.chatboat.ui.auth

data class Country(
    val name: String,
    val code: String,
    val flag: String
)

val countries = listOf(
    Country("United States", "+1", "🇺🇸"),
    Country("United Kingdom", "+44", "🇬🇧"),
    Country("India", "+91", "🇮🇳"),
    Country("Canada", "+1", "🇨🇦"),
    Country("Australia", "+61", "🇦🇺"),
    Country("Germany", "+49", "🇩🇪"),
    Country("France", "+33", "🇫🇷"),
    Country("Japan", "+81", "🇯🇵"),
    Country("China", "+86", "🇨🇳"),
    Country("Brazil", "+55", "🇧🇷"),
    Country("Mexico", "+52", "🇲🇽"),
    Country("South Africa", "+27", "🇿🇦"),
    Country("Nigeria", "+234", "🇳🇬"),
    Country("United Arab Emirates", "+971", "🇦🇪"),
    Country("Saudi Arabia", "+966", "🇸🇦"),
    Country("Pakistan", "+92", "🇵🇰"),
    Country("Bangladesh", "+880", "🇧🇩"),
    Country("Indonesia", "+62", "🇮🇩"),
    Country("Italy", "+39", "🇮🇹"),
    Country("Spain", "+34", "🇪🇸")
)
