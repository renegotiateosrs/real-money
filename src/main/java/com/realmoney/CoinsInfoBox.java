package com.realmoney;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.QuantityFormatter;

class CoinsInfoBox extends InfoBox
{
	private final RealMoneyPlugin plugin;

	CoinsInfoBox(BufferedImage image, RealMoneyPlugin plugin)
	{
		super(image, plugin);
		this.plugin = plugin;
	}

	@Override
	public String getText()
	{
		String value = plugin.formatGp(plugin.getInventoryGp());
		return value != null ? value : "?";
	}

	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		String value = plugin.formatGp(plugin.getInventoryGp());
		return QuantityFormatter.formatNumber(plugin.getInventoryGp()) + " gp in inventory"
			+ (value != null ? "</br>" + value : "</br>Waiting for bond price");
	}
}
