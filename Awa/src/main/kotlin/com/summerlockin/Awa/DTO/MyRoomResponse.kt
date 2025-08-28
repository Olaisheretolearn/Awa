package com.summerlockin.Awa.DTO

data class MyRoomResponse(
    val room: RoomResponse?,
    val members: List<UserResponse>
)
