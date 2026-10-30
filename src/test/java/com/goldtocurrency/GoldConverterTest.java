package com.goldtocurrency;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GoldConverterTest
{
	@Test
	public void bondRate()
	{
		// A bond selling for 10M gp and costing $8 makes 10M gp worth $8
		double rate = GoldConverter.rateFromBond(8.0, 10_000_000);
		assertEquals("$8.00", GoldConverter.format(10_000_000, rate, CurrencyType.USD));
		assertEquals("$0.80", GoldConverter.format(1_000_000, rate, CurrencyType.USD));
	}

	@Test
	public void unknownBondPrice()
	{
		assertEquals(-1, GoldConverter.rateFromBond(8.0, 0), 0);
	}

	@Test
	public void tinyAmounts()
	{
		double rate = GoldConverter.rateFromCustom(0.5);
		assertEquals("< $0.01", GoldConverter.format(100, rate, CurrencyType.USD));
		assertEquals("$0.00", GoldConverter.format(0, rate, CurrencyType.USD));
	}

	@Test
	public void otherCurrencies()
	{
		double rate = GoldConverter.rateFromCustom(1000);
		assertEquals("£1,000.00", GoldConverter.format(1_000_000, rate, CurrencyType.GBP));
		assertEquals("¥1,000", GoldConverter.format(1_000_000, rate, CurrencyType.JPY));
		assertEquals("CA$1,000.00", GoldConverter.format(1_000_000, rate, CurrencyType.CAD));
	}
}
