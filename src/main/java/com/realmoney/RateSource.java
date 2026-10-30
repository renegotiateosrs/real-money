package com.realmoney;

public enum RateSource
{
	BOND("Old School Bond"),
	CUSTOM("Custom rate");

	private final String name;

	RateSource(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
