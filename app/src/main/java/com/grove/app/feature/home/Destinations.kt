package com.grove.app.feature.home

enum class Dest(val route: String, val label: String) {
    Home("home", "Home"),
    History("history", "History"),
    Bills("bills", "Bills"),
    Reports("reports", "Reports"),
    Settings("settings", "Settings"),
    Budget("budget", "Budget"),
}

val BottomTabs = listOf(Dest.Home, Dest.History, Dest.Reports, Dest.Bills)