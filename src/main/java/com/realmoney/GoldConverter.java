package com.realmoney;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Pure conversion and formatting logic, kept free of client dependencies so it is easy to test.
 */
final class GoldConverter
{
	private GoldConverter()
	{
	}

	/**
	 * Currency per gp when one bond, worth {@code bondGePrice} gp on the Grand Exchange,
	 * costs {@code bondRealPrice} in real currency. Returns -1 if either price is unknown.
	 */
	static double rateFromBond(double bondRealPrice, long bondGePrice)
	{
		if (bondRealPrice <= 0 || bondGePrice <= 0)
		{
			return -1;
		}
		return bondRealPrice / bondGePrice;
	}

	/**
	 * Currency per gp from a user-entered price for one million gp. Returns -1 if unset.
	 */
	static double rateFromCustom(double pricePerMillion)
	{
		return pricePerMillion > 0 ? pricePerMillion / 1_000_000d : -1;
	}

	static String format(long gp, double ratePerGp, CurrencyType currency)
	{
		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
		nf.setCurrency(currency.getCurrency());
		int digits = Math.max(currency.getCurrency().getDefaultFractionDigits(), 0);
		nf.setMinimumFractionDigits(digits);
		nf.setMaximumFractionDigits(digits);

		double amount = gp * ratePerGp;
		double smallestUnit = Math.pow(10, -digits);
		if (amount > 0 && amount < smallestUnit)
		{
			return "< " + nf.format(smallestUnit);
		}
		return nf.format(amount);
	}
}
