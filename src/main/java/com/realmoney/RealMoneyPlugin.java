package com.realmoney;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
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
	name = "Real Money",
	description = "Shows gold and item values in USD or another real-world currency, priced by the Old School Bond",
	tags = {"gp", "gold", "coins", "usd", "dollar", "currency", "money", "bond", "price", "value"}
)
public class RealMoneyPlugin extends Plugin
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
	private RealMoneyConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private RealMoneyOverlay overlay;

	@Inject
	private MoneyIconOverlay moneyIconOverlay;

	@Getter
	private long inventoryGp;

	private CoinsInfoBox infoBox;

	@Getter
	private List<CoinSlot> coinSlots = Collections.emptyList();

	// Coin widgets whose sprites are currently made invisible
	private Set<Widget> hiddenWidgets = Collections.newSetFromMap(new IdentityHashMap<>());

	private BufferedImage[] moneyIcons;
	private CurrencyType moneyIconCurrency;

	static final class CoinSlot
	{
		final Rectangle bounds;
		final Rectangle clip;
		final int quantity;

		CoinSlot(Rectangle bounds, Rectangle clip, int quantity)
		{
			this.bounds = bounds;
			this.clip = clip;
			this.quantity = quantity;
		}
	}

	@Provides
	RealMoneyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RealMoneyConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(moneyIconOverlay);
		clientThread.invokeLater(() -> updateInventory(client.getItemContainer(INVENTORY_CONTAINER)));
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(moneyIconOverlay);
		clientThread.invoke(() -> restoreCoinSprites(Collections.emptySet()));
		coinSlots = Collections.emptyList();
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
		if (RealMoneyConfig.GROUP.equals(event.getGroup()))
		{
			// Recreate the infobox so its icon follows the currency
			removeInfoBox();
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
				infoBox = new CoinsInfoBox(config.replaceCoinIcons() ? getMoneyIcon(10_000) : itemManager.getImage(COINS, 10_000, true), this);
				infoBoxManager.addInfoBox(infoBox);
			}
		}
		else
		{
			removeInfoBox();
		}
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		if (!config.replaceCoinIcons())
		{
			if (!hiddenWidgets.isEmpty())
			{
				restoreCoinSprites(Collections.emptySet());
				coinSlots = Collections.emptyList();
			}
			return;
		}

		List<CoinSlot> slots = new ArrayList<>();
		Set<Widget> hidden = Collections.newSetFromMap(new IdentityHashMap<>());
		Rectangle canvas = new Rectangle(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
		findCoins(client.getWidgetRoots(), canvas, slots, hidden);

		restoreCoinSprites(hidden);
		hiddenWidgets = hidden;
		coinSlots = slots;
	}

	/**
	 * Walks the visible widget tree for coin stacks, hiding their sprites and recording where to draw money instead.
	 */
	private void findCoins(Widget[] widgets, Rectangle clip, List<CoinSlot> slots, Set<Widget> hidden)
	{
		if (widgets == null)
		{
			return;
		}

		for (Widget w : widgets)
		{
			if (w == null || w.isSelfHidden())
			{
				continue;
			}

			int itemId = w.getItemId();
			if ((itemId == COINS || itemId == COINS_ALT) && w.getItemQuantity() > 0)
			{
				// Leave a stack being dragged alone so it follows the mouse as normal
				if (client.isDraggingWidget() && client.getDraggedWidget() == w)
				{
					continue;
				}

				Rectangle bounds = w.getBounds();
				Rectangle visible = clip.intersection(bounds);
				if (!visible.isEmpty())
				{
					w.setOpacity(255);
					hidden.add(w);
					slots.add(new CoinSlot(bounds, visible, w.getItemQuantity()));
				}
				continue;
			}

			Rectangle childClip = w.getType() == WidgetType.LAYER ? clip.intersection(w.getBounds()) : clip;
			if (childClip.isEmpty())
			{
				continue;
			}
			findCoins(w.getDynamicChildren(), childClip, slots, hidden);
			findCoins(w.getStaticChildren(), childClip, slots, hidden);
			findCoins(w.getNestedChildren(), childClip, slots, hidden);
		}
	}

	/**
	 * Makes previously hidden coin sprites visible again, except those still in {@code keep}.
	 */
	private void restoreCoinSprites(Set<Widget> keep)
	{
		for (Widget w : hiddenWidgets)
		{
			if (!keep.contains(w))
			{
				w.setOpacity(0);
			}
		}
		if (keep.isEmpty())
		{
			hiddenWidgets = Collections.newSetFromMap(new IdentityHashMap<>());
		}
	}

	BufferedImage getMoneyIcon(long quantity)
	{
		CurrencyType currency = config.currency();
		if (moneyIcons == null || moneyIconCurrency != currency)
		{
			moneyIcons = new BufferedImage[MoneyIcons.tierCount()];
			for (int i = 0; i < moneyIcons.length; i++)
			{
				moneyIcons[i] = MoneyIcons.create(i, currency.getIconSymbol());
			}
			moneyIconCurrency = currency;
		}
		return moneyIcons[MoneyIcons.tierFor(quantity)];
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
