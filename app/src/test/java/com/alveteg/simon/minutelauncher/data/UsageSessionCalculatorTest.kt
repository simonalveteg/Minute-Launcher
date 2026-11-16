package com.alveteg.simon.minutelauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class UsageSessionCalculatorTest {
  @Test
  fun countsCompletedSessions() {
    val calculator = UsageSessionCalculator(true)
    calculator.resume("maps", "MapActivity", 10)
    calculator.pause("maps", "MapActivity", 30)
    calculator.resume("maps", "MapActivity", 40)
    calculator.pause("maps", "MapActivity", 50)

    assertEquals(30L, calculator.totals()["maps"])
  }

  @Test
  fun ignoresUnmatchedPausesAndOverlappingActivitiesAreNotDoubleCounted() {
    val calculator = UsageSessionCalculator(true)
    calculator.resume("maps", "CarActivity", 10)
    calculator.pause("maps", "OtherActivity", 20)
    calculator.resume("maps", "MapActivity", 30)
    calculator.pause("maps", "MapActivity", 40)
    calculator.pause("maps", "CarActivity", 50)

    assertEquals(40L, calculator.totals()["maps"])
  }

  @Test
  fun screenOffEndsSessionsAndIgnoresProjectionWhileNonInteractive() {
    val calculator = UsageSessionCalculator(true)
    calculator.resume("maps", "MapActivity", 10)
    calculator.screenNonInteractive(20)
    calculator.resume("maps", "CarActivity", 30)
    calculator.screenInteractive()
    calculator.pause("maps", "CarActivity", 1000)
    calculator.resume("maps", "MapActivity", 1010)
    calculator.pause("maps", "MapActivity", 1020)

    assertEquals(20L, calculator.totals()["maps"])
  }

  @Test
  fun initiallyNonInteractiveAndMissingResumesAreIgnored() {
    val calculator = UsageSessionCalculator(false)
    calculator.resume("maps", "CarActivity", 10)
    calculator.screenInteractive()
    calculator.pause("maps", "CarActivity", 20)
    calculator.pause("maps", "MapActivity", 30)

    assertEquals(emptyMap<String, Long>(), calculator.totals())
  }

  @Test
  fun shutdownClosesSessionsAndStartupDiscardsUnclosedSessions() {
    val calculator = UsageSessionCalculator(true)
    calculator.resume("maps", "MapActivity", 10)
    calculator.shutdown(20)
    calculator.startup()
    calculator.screenInteractive()
    calculator.pause("maps", "MapActivity", 1000)
    calculator.resume("other", "Activity", 1010)
    calculator.startup()
    calculator.resume("other", "Activity", 1020)
    calculator.screenInteractive()
    calculator.pause("other", "Activity", 2000)

    assertEquals(10L, calculator.totals()["maps"])
    assertEquals(0L, calculator.totals()["other"])
  }
}
