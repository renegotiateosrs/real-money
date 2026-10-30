package com.realmoney;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RealMoneyPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RealMoneyPlugin.class);
		RuneLite.main(args);
	}
}
