package com.goldtocurrency;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GoldToCurrencyPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GoldToCurrencyPlugin.class);
		RuneLite.main(args);
	}
}
