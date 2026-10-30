package com.goldtocurrency;

import java.util.Currency;

public enum CurrencyType
{
	USD("US Dollar", "$"),
	GBP("British Pound", "\u00A3"),
	EUR("Euro", "\u20AC"),
	CAD("Canadian Dollar", "$"),
	AUD("Australian Dollar", "$"),
	NZD("New Zealand Dollar", "$"),
	BRL("Brazilian Real", "$"),
	MXN("Mexican Peso", "$"),
	JPY("Japanese Yen", "\u00A5"),
	CHF("Swiss Franc", "Fr"),
	SEK("Swedish Krona", "kr"),
	NOK("Norwegian Krone", "kr"),
	DKK("Danish Krone", "kr"),
	PLN("Polish Zloty", "z\u0142"),
	INR("Indian Rupee", "\u20B9"),
	PHP("Philippine Peso", "\u20B1");

	private final String displayName;
	// Short symbol drawn on the money icons
	private final String iconSymbol;

	CurrencyType(String displayName, String iconSymbol)
	{
		this.displayName = displayName;
		this.iconSymbol = iconSymbol;
	}

	public String getIconSymbol()
	{
		return iconSymbol;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public Currency getCurrency()
	{
		return Currency.getInstance(name());
	}

	@Override
	public String toString()
	{
		return name() + " - " + displayName;
	}
}
