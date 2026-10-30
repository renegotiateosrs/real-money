package com.goldtocurrency;

import java.util.Currency;

public enum CurrencyType
{
	USD("US Dollar"),
	GBP("British Pound"),
	EUR("Euro"),
	CAD("Canadian Dollar"),
	AUD("Australian Dollar"),
	NZD("New Zealand Dollar"),
	BRL("Brazilian Real"),
	MXN("Mexican Peso"),
	JPY("Japanese Yen"),
	CHF("Swiss Franc"),
	SEK("Swedish Krona"),
	NOK("Norwegian Krone"),
	DKK("Danish Krone"),
	PLN("Polish Zloty"),
	INR("Indian Rupee"),
	PHP("Philippine Peso");

	private final String displayName;

	CurrencyType(String displayName)
	{
		this.displayName = displayName;
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
