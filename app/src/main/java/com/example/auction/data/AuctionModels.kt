package com.example.auction.data

/**
 * Domain models for the native Auction Pro / AJ Auction app.
 */

enum class PlayerStatus {
    PENDING,
    SOLD,
    UNSOLD,
    RETAINED
}

data class AuctionConfig(
    val playersPerTeam: Int = 11,
    val basePrice: Double = 2.0,
    val startingPoints: Double = 100.0,
    val maxRetentions: Int = 2,
    val retentionCost: Double = 10.0,
    val teamCount: Int = 4,
    val enablePlayerGrades: Boolean = false
)

data class AuctionPlayer(
    val number: Int,
    val name: String,
    val grade: String? = null,
    val role: String? = null,
    val phone: String? = null,
    val photoUrl: String? = null,
    val status: PlayerStatus = PlayerStatus.PENDING,
    val teamId: String? = null,
    val price: Double = 0.0,
    val auctionTurn: Int? = null,
    val auctionRound: Int? = null
)

data class AuctionTeam(
    val id: String,
    val name: String,
    val shortName: String,
    val colorHex: String,
    val startingPoints: Double,
    val pointsSpent: Double = 0.0,
    val playerCount: Int = 0,
    val retainedCount: Int = 0
) {
    val pointsRemaining: Double
        get() = startingPoints - pointsSpent
}

data class BidRecord(
    val teamId: String,
    val amount: Double,
    val timestampMillis: Long
)

data class AuctionSnapshot(
    val players: List<AuctionPlayer>,
    val teams: List<AuctionTeam>,
    val bidCounter: Int,
    val retentionCounter: Int,
    val roundNumber: Int
)
