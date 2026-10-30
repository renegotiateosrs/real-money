package com.goldtocurrency;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@PluginDescriptor(
	name = "Gold to Currency",
	description = "Shows gold and item values in USD or another real-world currency, priced by the Old School Bond",
	tags = {"gp", "gold", "coins", "usd", "dollar", "currency", "money", "bond", "price", "value"}
)
public class GoldToCurrencyPlugin extends Plugin
{
	static final int COINS = 995;
	static final int COINS_ALT = 617;
	static final int PLATINUM_TOKEN = 13204;
	static final int OLD_SCHOOL_BOND = 13190;
	private static final int INVENTORY_CONTAINER = 93;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private GoldToCurrencyConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private GoldToCurrencyOverlay overlay;

	@Getter
	private long inventoryGp;

	private CoinsInfoBox infoBox;

	@Provides
	GoldToCurrencyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GoldToCurrencyConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		clientThread.invokeLater(() -> updateInventory(client.getItemContainer(INVENTORY_CONTAINER)));
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		removeInfoBox();
		inventoryGp = 0;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == INVENTORY_CONTAINER)
		{
			updateInventory(event.getItemContainer());
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (GoldToCurrencyConfig.GROUP.equals(event.getGroup()))
		{
			clientThread.invokeLater(() -> updateInventory(client.getItemContainer(INVENTORY_CONTAINER)));
		}
	}

	private void updateInventory(ItemContainer container)
	{
		long gp = 0;
		if (container != null)
		{
			for (Item item : container.getItems())
			{
				if (isCurrency(item.getId()))
				{
					gp += getGpValue(item.getId(), item.getQuantity());
				}
			}
		}
		inventoryGp = gp;

		if (config.showInfobox() && gp > 0)
		{
			if (infoBox == null)
			{
				infoBox = new CoinsInfoBox(itemManager.getImage(COINS, 10_000, true), this);
				infoBoxManager.addInfoBox(infoBox);
			}
		}
		else
		{
			removeInfoBox();
		}
	}

	private void removeInfoBox()
	{
		if (infoBox != null)
		{
			infoBoxManager.removeInfoBox(infoBox);
			infoBox = null;
		}
	}

	static boolean isCurrency(int itemId)
	{
		return itemId == COINS || itemId == COINS_ALT || itemId == PLATINUM_TOKEN;
	}

	/**
	 * Grand Exchange value in gp of a stack of items. Returns 0 if the item has no known value
	 * or the user only wants coins converted.
	 */
	long getGpValue(int itemId, int quantity)
	{
		if (itemId == COINS || itemId == COINS_ALT)
		{
			return quantity;
		}
		if (itemId == PLATINUM_TOKEN)
		{
			return quantity * 1000L;
		}
		if (config.coinsOnly())
		{
			return 0;
		}
		return (long) itemManager.getItemPrice(itemManager.canonicalize(itemId)) * quantity;
	}

	/**
	 * Formats a gp amount in the configured currency, or returns null if no rate is available yet
	 * (for example, bond prices have not loaded).
	 */
	String formatGp(long gp)
	{
		double rate = config.rateSource() == RateSource.CUSTOM
			? GoldConverter.rateFromCustom(config.customRate())
			: GoldConverter.rateFromBond(config.bondPrice(), itemManager.getItemPrice(OLD_SCHOOL_BOND));
		if (rate <= 0)
		{
			return null;
		}
		return GoldConverter.format(gp, rate, config.currency());
	}
}
