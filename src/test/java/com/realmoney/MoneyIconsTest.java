package com.realmoney;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MoneyIconsTest
{
	@Test
	public void tiersMatchCoinPiles()
	{
		assertEquals(0, MoneyIcons.tierFor(1));
		assertEquals(3, MoneyIcons.tierFor(4));
		assertEquals(4, MoneyIcons.tierFor(24));
		assertEquals(5, MoneyIcons.tierFor(25));
		assertEquals(6, MoneyIcons.tierFor(100));
		assertEquals(7, MoneyIcons.tierFor(999));
		assertEquals(8, MoneyIcons.tierFor(1000));
		assertEquals(9, MoneyIcons.tierFor(Integer.MAX_VALUE));
	}

	@Test
	public void everyCurrencyRenders()
	{
		for (CurrencyType currency : CurrencyType.values())
		{
			for (int tier = 0; tier < MoneyIcons.tierCount(); tier++)
			{
				assertEquals(MoneyIcons.WIDTH, MoneyIcons.create(tier, currency.getIconSymbol()).getWidth());
			}
		}
	}
}
