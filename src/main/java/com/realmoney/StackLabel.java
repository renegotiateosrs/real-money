package com.realmoney;

public enum StackLabel
{
	GOLD("Gold amount"),
	CURRENCY("Currency value"),
	NONE("None");

	private final String name;

	StackLabel(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
