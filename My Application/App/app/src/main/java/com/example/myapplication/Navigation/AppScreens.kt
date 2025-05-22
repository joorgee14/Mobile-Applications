package com.example.myapplication.Navigation

sealed class AppScreens(val route: String) {
    object Login: AppScreens("login")
    object Dashboard: AppScreens("dashboard/{userId}")
    object Progress: AppScreens("progress/{userId}")
    object Register : AppScreens("register")
    object MySessions : AppScreens("mysessions/{userId}")
    object GymsMap : AppScreens("gymsMap")
    object EditProfile : AppScreens("editProfile/{userId}")

}