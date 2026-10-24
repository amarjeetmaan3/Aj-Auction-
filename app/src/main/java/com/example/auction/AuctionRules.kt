package com.example.auction

import com.example.auction.data.AuctionConfig
import com.example.auction.data.AuctionPlayer
import com.example.auction.data.AuctionTeam
import com.example.auction.data.PlayerStatus

/**
 * Shared validation for manual auction actions.
 * Common Base Price is reserved for every unfilled squad slot after the current purchase.
 */
object AuctionRules {
    fun maxNextBid(team: AuctionTeam, config: AuctionConfig): Double {
        if (team.playerCount >= config.playersPerTeam || team.pointsRemaining <= 0.0) return 0.0
        if (config.basePrice <= 0.0) return minOf(100.0, team.pointsRemaining.coerceAtLeast(0.0))
        val slotsAfterPurchase = (config.playersPerTeam - team.playerCount - 1).coerceAtLeast(0)
        val reservePoints = slotsAfterPurchase * config.basePrice
        return (team.pointsRemaining - reservePoints).coerceAtLeast(0.0)
    }

    fun canSell(player: AuctionPlayer, team: AuctionTeam, config: AuctionConfig, bid: Double): Boolean {
        val minimumBid = if (config.basePrice > 0.0) config.basePrice else 1.0
        return player.status in listOf(PlayerStatus.PENDING, PlayerStatus.UNSOLD) &&
            bid.isFinite() && config.basePrice.isFinite() && config.basePrice >= 0.0 &&
            bid >= minimumBid &&
            team.pointsRemaining.isFinite() && team.pointsRemaining >= 0.0 &&
            bid <= maxNextBid(team, config) &&
            team.playerCount >= 0 && team.playerCount < config.playersPerTeam &&
            config.playersPerTeam > 0
    }

    fun canRetain(player: AuctionPlayer, team: AuctionTeam, config: AuctionConfig): Boolean {
        return player.status in listOf(PlayerStatus.PENDING, PlayerStatus.UNSOLD) &&
            config.retentionCost.isFinite() && config.retentionCost >= 0.0 &&
            team.pointsRemaining.isFinite() && team.pointsRemaining >= config.retentionCost &&
            team.playerCount >= 0 && team.playerCount < config.playersPerTeam &&
            config.playersPerTeam > 0 &&
            team.retainedCount >= 0 && team.retainedCount < config.maxRetentions &&
            config.maxRetentions >= 0
    }

    fun canAddTeam(currentTeamCount: Int, config: AuctionConfig): Boolean =
        currentTeamCount >= 0 && config.teamCount > 0 && currentTeamCount < config.teamCount
}
