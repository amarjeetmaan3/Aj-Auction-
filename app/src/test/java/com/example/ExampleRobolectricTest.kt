package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.auction.AuctionRules
import com.example.auction.data.AuctionConfig
import com.example.auction.data.AuctionPlayer
import com.example.auction.data.AuctionTeam
import com.example.auction.data.PlayerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AJ Auction", appName)
  }

  @Test
  fun `auction rules bid calculation and selling validation`() {
    val config = AuctionConfig(
      playersPerTeam = 11,
      basePrice = 2.0,
      startingPoints = 100.0,
      maxRetentions = 2,
      retentionCost = 10.0,
      teamCount = 4
    )
    val team = AuctionTeam(
      id = "team-1",
      name = "Warriors",
      shortName = "WAR",
      colorHex = "#0646A8",
      startingPoints = 100.0,
      pointsSpent = 0.0,
      playerCount = 0
    )
    // 11 slots total, 10 slots remaining after purchase, 10 * 2.0 = 20 reserved -> max bid 80.0
    val maxBid = AuctionRules.maxNextBid(team, config)
    assertEquals(80.0, maxBid, 0.001)

    val player = AuctionPlayer(number = 1, name = "Virat")
    assertTrue(AuctionRules.canSell(player, team, config, 10.0))
    assertTrue(AuctionRules.canRetain(player, team, config))
  }
}
