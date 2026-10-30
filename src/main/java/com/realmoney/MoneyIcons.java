package com.realmoney;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Draws item-sized (36x32) money icons that replace the coin sprites. The art grows with the
 * stack size using the same thresholds the game uses for its coin piles.
 */
final class MoneyIcons
{
	static final int WIDTH = 36;
	static final int HEIGHT = 32;

	private static final Color BILL = new Color(0x8CC266);
	private static final Color BILL_DARK = new Color(0x3F7A2C);
	private static final Color BILL_EDGE = new Color(0x2A5220);
	private static final Color BILL_LIGHT = new Color(0xC8E6A8);
	private static final Color BAND = new Color(0xF0DFA0);
	private static final Color BAND_DARK = new Color(0xB89A48);
	private static final Color BAG = new Color(0xA8763E);
	private static final Color BAG_DARK = new Color(0x6B4520);
	private static final Color OUTLINE = new Color(0x000001);
	private static final Color SHADOW = new Color(0x302020);

	private static final int[] THRESHOLDS = {1, 2, 3, 4, 5, 25, 100, 250, 1000, 10000};

	private MoneyIcons()
	{
	}

	/**
	 * Index into {@link #THRESHOLDS} for a stack size, mirroring the coin pile sprites.
	 */
	static int tierFor(long quantity)
	{
		int tier = 0;
		for (int i = 0; i < THRESHOLDS.length; i++)
		{
			if (quantity >= THRESHOLDS[i])
			{
				tier = i;
			}
		}
		return tier;
	}

	static int tierCount()
	{
		return THRESHOLDS.length;
	}

	static BufferedImage create(int tier, String symbol)
	{
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		switch (tier)
		{
			case 0:
				bill(g, 18, 18, 0, symbol);
				break;
			case 1:
			case 2:
			case 3:
			case 4:
				fan(g, tier + 1, symbol);
				break;
			case 5:
				stack(g, 18, 25, 8, false, symbol);
				bill(g, 20, 13, Math.toRadians(-12), symbol);
				break;
			case 6:
				stack(g, 18, 20, 6, true, symbol);
				break;
			case 7:
				stack(g, 11, 24, 5, true, symbol);
				stack(g, 24, 20, 7, true, symbol);
				break;
			case 8:
				stack(g, 10, 26, 5, true, symbol);
				stack(g, 26, 26, 5, true, symbol);
				stack(g, 18, 18, 5, true, symbol);
				break;
			default:
				bag(g, 18, 15, symbol);
				stack(g, 9, 31, 16, 3, true, symbol);
				stack(g, 27, 31, 16, 3, true, symbol);
				break;
		}
		g.dispose();
		return outline(img);
	}

	/** Bills fanned out from a common corner. */
	private static void fan(Graphics2D g, int count, String symbol)
	{
		double spread = Math.toRadians(14);
		double start = -spread * (count - 1) / 2;
		for (int i = 0; i < count; i++)
		{
			bill(g, 18 + (i - (count - 1) / 2.0), 19 - i * 0.5, start + spread * i, symbol);
		}
	}

	/** A single bill centred on (cx, cy). */
	private static void bill(Graphics2D g, double cx, double cy, double angle, String symbol)
	{
		AffineTransform saved = g.getTransform();
		g.translate(cx, cy);
		g.rotate(angle);

		double w = 28, h = 14;
		RoundRectangle2D body = new RoundRectangle2D.Double(-w / 2, -h / 2, w, h, 2, 2);
		g.setPaint(new GradientPaint(0, (float) (-h / 2), BILL_LIGHT, 0, (float) (h / 2), BILL));
		g.fill(body);
		g.setColor(BILL_DARK);
		g.setStroke(new BasicStroke(1f));
		g.draw(new RoundRectangle2D.Double(-w / 2 + 2, -h / 2 + 2, w - 4, h - 4, 2, 2));
		g.setColor(BILL_EDGE);
		g.draw(body);

		g.setColor(BILL_LIGHT);
		g.fill(new Ellipse2D.Double(-5, -5, 10, 10));
		g.setColor(BILL_DARK);
		g.draw(new Ellipse2D.Double(-5, -5, 10, 10));
		symbol(g, 0, 0, symbol, 9f, BILL_EDGE);

		g.setTransform(saved);
	}

	/** A pile of bills seen from slightly above, optionally strapped with a paper band. */
	private static void stack(Graphics2D g, double cx, double bottom, int layers, boolean banded, String symbol)
	{
		stack(g, cx, bottom, 22, layers, banded, symbol);
	}

	private static void stack(Graphics2D g, double cx, double bottom, double w, int layers, boolean banded, String symbol)
	{
		double h = w / 2;
		for (int i = 0; i < layers; i++)
		{
			double y = bottom - h - i;
			g.setColor(i % 2 == 0 ? BILL_DARK : BILL);
			g.fill(new RoundRectangle2D.Double(cx - w / 2, y, w, h, 2, 2));
		}

		double top = bottom - h - layers;
		RoundRectangle2D face = new RoundRectangle2D.Double(cx - w / 2, top, w, h, 2, 2);
		g.setPaint(new GradientPaint(0, (float) top, BILL_LIGHT, 0, (float) (top + h), BILL));
		g.fill(face);
		g.setColor(BILL_DARK);
		g.setStroke(new BasicStroke(1f));
		g.draw(new RoundRectangle2D.Double(cx - w / 2 + 2, top + 2, w - 4, h - 4, 1, 1));
		g.setColor(BILL_EDGE);
		g.draw(face);

		if (banded)
		{
			double bw = 6;
			g.setColor(BAND);
			g.fill(new java.awt.geom.Rectangle2D.Double(cx - bw / 2, top, bw, h + layers));
			g.setColor(BAND_DARK);
			g.draw(new java.awt.geom.Rectangle2D.Double(cx - bw / 2, top, bw, h + layers));
			symbol(g, cx, top + h / 2, symbol, 7f, BILL_EDGE);
		}
		else
		{
			symbol(g, cx, top + h / 2, symbol, 8f, BILL_EDGE);
		}
	}

	/** A tied money sack. */
	private static void bag(Graphics2D g, double cx, double cy, String symbol)
	{
		GeneralPath sack = new GeneralPath();
		sack.moveTo(cx - 3, cy - 9);
		sack.curveTo(cx - 13, cy - 4, cx - 14, cy + 11, cx, cy + 11);
		sack.curveTo(cx + 14, cy + 11, cx + 13, cy - 4, cx + 3, cy - 9);
		sack.closePath();
		g.setPaint(new GradientPaint((float) (cx - 10), 0, BAG, (float) (cx + 12), 0, BAG_DARK));
		g.fill(sack);
		g.setColor(BAG_DARK);
		g.setStroke(new BasicStroke(1f));
		g.draw(sack);

		GeneralPath neck = new GeneralPath();
		neck.moveTo(cx - 3, cy - 9);
		neck.lineTo(cx - 6, cy - 14);
		neck.lineTo(cx + 6, cy - 14);
		neck.lineTo(cx + 3, cy - 9);
		neck.closePath();
		g.setColor(BAG);
		g.fill(neck);
		g.setColor(BAG_DARK);
		g.draw(neck);
		g.setColor(BAND_DARK);
		g.fillRect((int) cx - 4, (int) cy - 10, 8, 2);

		symbol(g, cx, cy + 1, symbol, 11f, BAND);
	}

	private static void symbol(Graphics2D g, double cx, double cy, String symbol, float size, Color color)
	{
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, 1).deriveFont(size);
		if (font.canDisplayUpTo(symbol) != -1)
		{
			symbol = "$";
		}
		if (symbol.length() > 1)
		{
			font = font.deriveFont(size * 0.75f);
		}
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		float x = (float) (cx - fm.stringWidth(symbol) / 2.0);
		float y = (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0);
		g.setColor(color);
		g.drawString(symbol, x, y);
	}

	/** Adds the black outline and drop shadow that in-game item sprites have. */
	private static BufferedImage outline(BufferedImage src)
	{
		BufferedImage out = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int argb = src.getRGB(x, y);
				if ((argb >>> 24) >= 0x80)
				{
					out.setRGB(x, y, argb | 0xFF000000);
				}
				else if (opaque(src, x - 1, y) || opaque(src, x + 1, y) || opaque(src, x, y - 1) || opaque(src, x, y + 1))
				{
					out.setRGB(x, y, OUTLINE.getRGB());
				}
				else if (opaque(src, x - 1, y - 1) || opaque(src, x - 2, y - 2))
				{
					out.setRGB(x, y, SHADOW.getRGB());
				}
			}
		}
		return out;
	}

	private static boolean opaque(BufferedImage img, int x, int y)
	{
		return x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT && (img.getRGB(x, y) >>> 24) >= 0x80;
	}
}
