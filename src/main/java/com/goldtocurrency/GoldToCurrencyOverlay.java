package com.goldtocurrency;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

class GoldToCurrencyOverlay extends Overlay
{
	private final Client client;
	private final GoldToCurrencyPlugin plugin;
	private final GoldToCurrencyConfig config;
	private final TooltipManager tooltipManager;

	@Inject
	GoldToCurrencyOverlay(Client client, GoldToCurrencyPlugin plugin, GoldToCurrencyConfig config, TooltipManager tooltipManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.tooltipManager = tooltipManager;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showTooltip() || client.isMenuOpen())
		{
			return null;
		}

		MenuEntry[] entries = client.getMenuEntries();
		if (entries.length == 0)
		{
			return null;
		}

		// The last entry is the left-click option for whatever the mouse is over
		Widget widget = entries[entries.length - 1].getWidget();
		if (widget == null)
		{
			return null;
		}

		int itemId = widget.getItemId();
		int quantity = widget.getItemQuantity();
		if (itemId <= 0 || quantity <= 0)
		{
			return null;
		}

		long gp = plugin.getGpValue(itemId, quantity);
		if (gp <= 0)
		{
			return null;
		}

		String value = plugin.formatGp(gp);
		if (value != null)
		{
			tooltipManager.add(new Tooltip("Value: " + ColorUtil.wrapWithColorTag(value, config.tooltipColor())));
		}
		return null;
	}
}
