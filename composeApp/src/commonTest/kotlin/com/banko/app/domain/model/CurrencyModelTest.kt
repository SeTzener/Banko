package com.banko.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CurrencyModelTest {

    @Test
    fun `getSupportedCurrencies should contain 15 currencies`() {
        val currencies = getSupportedCurrencies()
        assertEquals(15, currencies.size)
    }

    @Test
    fun `getSupportedCurrencies should include EUR as first entry`() {
        val currencies = getSupportedCurrencies()
        assertEquals("EUR", currencies[0].code)
        assertEquals("Euro", currencies[0].name)
        assertEquals("\u20AC", currencies[0].symbol)
    }

    @Test
    fun `getSupportedCurrencies should include all krone currencies`() {
        val codes = getSupportedCurrencies().map { it.code }
        assertTrue("NOK" in codes)
        assertTrue("SEK" in codes)
        assertTrue("DKK" in codes)
        assertTrue("ISK" in codes)
    }

    @Test
    fun `currencyDisplayForCode should return symbol for EUR`() {
        assertEquals("\u20AC", currencyDisplayForCode("EUR"))
    }

    @Test
    fun `currencyDisplayForCode should return symbol for GBP`() {
        assertEquals("\u00A3", currencyDisplayForCode("GBP"))
    }

    @Test
    fun `currencyDisplayForCode should return symbol for JPY`() {
        assertEquals("\u00A5", currencyDisplayForCode("JPY"))
    }

    @Test
    fun `currencyDisplayForCode should return symbol for PLN`() {
        assertEquals("z\u0142", currencyDisplayForCode("PLN"))
    }

    @Test
    fun `currencyDisplayForCode should return symbol for CZK`() {
        assertEquals("K\u010D", currencyDisplayForCode("CZK"))
    }

    @Test
    fun `currencyDisplayForCode should return symbol for HUF`() {
        assertEquals("Ft", currencyDisplayForCode("HUF"))
    }

    @Test
    fun `currencyDisplayForCode should return code for NOK`() {
        assertEquals("NOK", currencyDisplayForCode("NOK"))
    }

    @Test
    fun `currencyDisplayForCode should return code for SEK`() {
        assertEquals("SEK", currencyDisplayForCode("SEK"))
    }

    @Test
    fun `currencyDisplayForCode should return code for DKK`() {
        assertEquals("DKK", currencyDisplayForCode("DKK"))
    }

    @Test
    fun `currencyDisplayForCode should return code for ISK`() {
        assertEquals("ISK", currencyDisplayForCode("ISK"))
    }

    @Test
    fun `currencyDisplayForCode should return code for USD`() {
        assertEquals("USD", currencyDisplayForCode("USD"))
    }

    @Test
    fun `currencyDisplayForCode should return code for CAD`() {
        assertEquals("CAD", currencyDisplayForCode("CAD"))
    }

    @Test
    fun `currencyDisplayForCode should return code for AUD`() {
        assertEquals("AUD", currencyDisplayForCode("AUD"))
    }

    @Test
    fun `currencyDisplayForCode should return code for NZD`() {
        assertEquals("NZD", currencyDisplayForCode("NZD"))
    }

    @Test
    fun `currencyDisplayForCode should return code for CHF`() {
        assertEquals("CHF", currencyDisplayForCode("CHF"))
    }

    @Test
    fun `currencyDisplayForCode should return original code for unknown currency`() {
        assertEquals("XYZ", currencyDisplayForCode("XYZ"))
    }

    @Test
    fun `getSupportedCurrencies should have valid displayLabel for each entry`() {
        getSupportedCurrencies().forEach { currency ->
            val display = currency.displayLabel
            assertNotNull(display)
            assertTrue(display.isNotEmpty())
        }
    }

    @Test
    fun `isCurrencySupported should return true for known currencies`() {
        assertTrue(isCurrencySupported("EUR"))
        assertTrue(isCurrencySupported("NOK"))
        assertTrue(isCurrencySupported("USD"))
    }

    @Test
    fun `isCurrencySupported should return false for unknown currencies`() {
        assertTrue(!isCurrencySupported("XYZ"))
        assertTrue(!isCurrencySupported("ABC"))
    }

    @Test
    fun `CurrencyPreference should default to NOK`() {
        val pref = CurrencyPreference()
        assertEquals("NOK", pref.selectedCurrency)
    }
}
