package com.realmoney;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(RealMoneyConfig.GROUP)
public interface RealMoneyConfig extends Config
{
	String GROUP = "realmoney";

	@ConfigSection(
		name = "Exchange rate",
		description = "How gold is converted to real currency",
		position = 0
	)
	String rateSection = "rate";

	@ConfigSection(
		name = "Display",
		description = "Where converted values are shown",
		position = 1
	)
	String displaySection = "display";

	@ConfigItem(
		keyName = "currency",
		name = "Currency",
		description = "The currency values are shown in",
		section = rateSection,
		position = 0
	)
	default CurrencyType currency()
	{
		return CurrencyType.USD;
	}

	@ConfigItem(
		keyName = "rateSource",
		name = "Rate source",
		description = "Old School Bond: uses the live Grand Exchange price of a bond and the bond's store price below.<br>"
			+ "Custom rate: uses the price per million gp entered below.",
		section = rateSection,
		position = 1
	)
	default RateSource rateSource()
	{
		return RateSource.BOND;
	}

	@ConfigItem(
		keyName = "bondPrice",
		name = "Bond store price",
		description = "What one Old School Bond costs in the selected currency on the official store",
		section = rateSection,
		position = 2
	)
	default double bondPrice()
	{
		return 7.99;
	}

	@ConfigItem(
		keyName = "customRate",
		name = "Custom price per 1M gp",
		description = "Value of one million gp in the selected currency (used when rate source is Custom rate)",
		section = rateSection,
		position = 3
	)
	default double customRate()
	{
		return 0.5;
	}

	@ConfigItem(
		keyName = "replaceCoinIcons",
		name = "Replace coin icons",
		description = "Draw bills, bundles and money bags in place of coin piles. Bigger stacks get bigger piles of money.",
		section = displaySection,
		position = -2
	)
	default boolean replaceCoinIcons()
	{
		return true;
	}

	@ConfigItem(
		keyName = "stackLabel",
		name = "Stack label",
		description = "Text drawn on replaced coin icons",
		section = displaySection,
		position = -1
	)
	default StackLabel stackLabel()
	{
		return StackLabel.GOLD;
	}

	@ConfigItem(
		keyName = "showTooltip",
		name = "Show tooltip",
		description = "Show the converted value when hovering items",
		section = displaySection,
		position = 0
	)
	default boolean showTooltip()
	{
		return true;
	}

	@ConfigItem(
		keyName = "coinsOnly",
		name = "Coins only",
		description = "Only show the tooltip for coins and platinum tokens, not other items",
		section = displaySection,
		position = 1
	)
	default boolean coinsOnly()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showInfobox",
		name = "Show inventory infobox",
		description = "Show an infobox with the value of coins in your inventory",
		section = displaySection,
		position = 2
	)
	default boolean showInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tooltipColor",
		name = "Tooltip colour",
		description = "Colour of the converted value in tooltips",
		section = displaySection,
		position = 3
	)
	default Color tooltipColor()
	{
		return new Color(0x4CBB17);
	}
}
