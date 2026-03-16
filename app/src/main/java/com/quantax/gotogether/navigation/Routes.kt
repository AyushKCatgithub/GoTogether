package com.quantax.gotogether.navigation

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")
    data object SignIn : Routes("sign_in")
    data object Otp : Routes("otp/{verificationId}/{phoneNumber}") {
        fun createRoute(verificationId: String, phoneNumber: String) = 
            "otp/$verificationId/$phoneNumber"
    }
    data object Main : Routes("main")
    data object Home : Routes("home")
    data object CreateTrip : Routes("create_trip")
    data object Profile : Routes("profile")
    data object EditProfile : Routes("edit_profile")
    data object Notifications : Routes("notifications")
    data object MyTrips : Routes("my_trips")
    data object Messages : Routes("messages")
    data object TripDetail : Routes("trip_detail/{tripId}") {
        fun createRoute(tripId: String) = "trip_detail/$tripId"
    }
    data object Chatroom : Routes("chatroom/{chatroomId}") {
        fun createRoute(chatroomId: String) = "chatroom/$chatroomId"
    }
    data object PublicProfile : Routes("public_profile/{userId}") {
        fun createRoute(userId: String) = "public_profile/$userId"
    }
}
