package com.realmoney;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.QuantityFormatter;

/**
 * Draws money icons over coin stacks whose own sprites the plugin has made invisible.
 */
class MoneyIconOverlay extends Overlay
{
	private static final Color YELLOW = new Color(255, 255, 0);
	private static final Color GREEN = new Color(0, 255, 128);

	private final RealMoneyPlugin plugin;
	private final RealMoneyConfig config;

	@Inject
	MoneyIconOverlay(RealMoneyPlugin plugin, RealMoneyConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.replaceCoinIcons())
		{
			return null;
		}

		Shape oldClip = graphics.getClip();
		graphics.setFont(FontManager.getRunescapeSmallFont());
		for (RealMoneyPlugin.CoinSlot slot : plugin.getCoinSlots())
		{
			Rectangle b = slot.bounds;
			graphics.setClip(slot.clip);
			graphics.drawImage(plugin.getMoneyIcon(slot.quantity), b.x, b.y, null);
			drawLabel(graphics, slot.quantity, b.x, b.y + 10);
		}
		graphics.setClip(oldClip);
		return null;
	}

	private void drawLabel(Graphics2D graphics, int quantity, int x, int y)
	{
		String text = null;
		Color color = YELLOW;
		switch (config.stackLabel())
		{
			case NONE:
				return;
			case CURRENCY:
				text = plugin.formatGp(quantity);
				break;
		}

		if (text == null)
		{
			// Matches the game's own stack colours
			text = QuantityFormatter.quantityToStackSize(quantity);
			color = quantity >= 10_000_000 ? GREEN : quantity >= 100_000 ? Color.WHITE : YELLOW;
		}

		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);
		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}
}
