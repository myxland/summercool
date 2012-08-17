package org.summercool.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.summercool.image.AnimatedGifEncoder;
import org.summercool.image.GifDecoder;
import org.summercool.image.Scalr;
import org.summercool.image.Scalr.Method;
import org.summercool.image.Scalr.Mode;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageUtil {

	static Font FONT = new Font("微软雅黑", Font.BOLD, 18);
	static final Color COLOR = Color.WHITE;
	static final Color FONT_COLOR = new Color(255, 255, 255, 150);
	static final Color FONT_SHADOW_COLOR = new Color(170, 170, 170, 77);

	private static Map<Font, Map<String, int[]>> FONT_REC_MAP = new HashMap<Font, Map<String, int[]>>();

	public static boolean isJpg(String str) {
		return isEndWid(str, "jpg");
	}

	public static boolean isPng(String str) {
		return isEndWid(str, "png");
	}

	public static boolean isGif(String str) {
		return isEndWid(str, "gif");
	}

	private static boolean isEndWid(String str, String ext) {
		if (str == null || "".equals(str.trim())) {
			return false;
		}

		int position = str.lastIndexOf(".");
		if (position == -1 || (position == str.length() - 1)) {
			return false;
		}
		String suffix = str.substring(position + 1);
		if (ext.equalsIgnoreCase(suffix)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isJpg(InputStream in) throws IOException {
		InputStream iis = in;

		if (!in.markSupported()) {
			throw new IllegalArgumentException("Input stream must support mark");
		}

		iis.mark(30);
		// If the first two bytes are a JPEG SOI marker, it's probably
		// a JPEG file. If they aren't, it definitely isn't a JPEG file.
		try {
			int byte1 = iis.read();
			int byte2 = iis.read();
			if ((byte1 == 0xFF) && (byte2 == 0xD8)) {
				return true;
			}
		} finally {
			iis.reset();
		}

		return false;
	}

	public static boolean isPng(InputStream in) throws IOException {
		if (!in.markSupported()) {
			throw new IllegalArgumentException("Input stream must support mark");
		}

		byte[] b = new byte[8];
		try {
			in.mark(30);
			in.read(b);
		} finally {
			in.reset();
		}

		return (b[0] == (byte) 137 && b[1] == (byte) 80 && b[2] == (byte) 78 && b[3] == (byte) 71 && b[4] == (byte) 13
				&& b[5] == (byte) 10 && b[6] == (byte) 26 && b[7] == (byte) 10);
	}

	public static boolean isGif(InputStream in) throws IOException {
		if (!in.markSupported()) {
			throw new IllegalArgumentException("Input stream must support mark");
		}

		byte[] b = new byte[6];

		try {
			in.mark(30);
			in.read(b);
		} finally {
			in.reset();
		}

		return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8' && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
	}

	/**
	 * 压缩图片
	 * 
	 * @param in
	 * @param out
	 * @param maxWidth
	 * @param maxHeight
	 * @param type
	 *            1: jpg 2: png 4: gif 3: jpg+png 5: jpg+gif 6: png+gif 7:
	 *            jpg+png+gif
	 * @throws IOException
	 */
	public static void resize(InputStream in, OutputStream out, int maxWidth, int maxHeight, int type, float quality,
			String[] watermark, Font font, Color fontColor) throws IOException {
		if (!(type >= 1 && type <= 7)) {
			throw new IOException("can not support type: " + type + ", type must be in [1-7] ");
		}
		if (type == 1) {
			if (!isJpg(in)) {
				throw new IOException("image format is not jpg ");
			}
			resizeJpg(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
			return;
		} else if (type == 2) {
			if (!isPng(in)) {
				throw new IOException("image format is not png ");
			}
			resizePng(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
			return;
		} else if (type == 3) {
			if (isJpg(in)) {
				resizeJpg(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			} else if (isPng(in)) {
				resizePng(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			}
			throw new IOException("image format is not jpg or png ");
		} else if (type == 4) {
			if (!isGif(in)) {
				throw new IOException("image format is not gif ");
			}
			resizeGif(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
			return;
		} else if (type == 5) {
			if (isJpg(in)) {
				resizeJpg(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			} else if (isGif(in)) {
				resizeGif(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			}
			throw new IOException("image format is not jpg or gif ");
		} else if (type == 6) {
			if (isPng(in)) {
				resizePng(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			} else if (isGif(in)) {
				resizeGif(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			}
			throw new IOException("image format is not png or gif ");
		} else if (type == 7) {
			if (isJpg(in)) {
				resizeJpg(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			} else if (isPng(in)) {
				resizePng(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			} else if (isGif(in)) {
				resizeGif(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor);
				return;
			}
			throw new IOException("image format is not jpg or png or gif ");
		}

	}

	public static void resizeJpg(InputStream in, OutputStream out, int maxWidth, int maxHeight, float quality,
			String[] watermark, Font font, Color fontColor) throws IOException {
		checkParams(in, out, maxWidth, maxHeight, quality);
		//
		BufferedImage image = ImageIO.read(in);
		image = Scalr.resize(image, Method.AUTOMATIC, Mode.AUTOMATIC, maxWidth, maxHeight);
		// create new image with right size/format
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bufferedImage.createGraphics();
		// 因为有的图片背景是透明色，所以用白色填充 FIXED
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1));
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.drawImage(image, 0, 0, null);
		image = bufferedImage;
		//
		if (watermark != null && watermark.length > 0) {
			makeWatermark(watermark, image, font, fontColor);
		}
		//
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
		param.setQuality(quality, false);
		encoder.setJPEGEncodeParam(param);
		encoder.encode(image);
	}

	public static void resizePng(InputStream in, OutputStream out, int maxWidth, int maxHeight, float quality,
			String[] watermark, Font font, Color fontColor) throws IOException {
		checkParams(in, out, maxWidth, maxHeight, quality);
		//
		BufferedImage image = ImageIO.read(in);
		image = Scalr.resize(image, Method.AUTOMATIC, Mode.AUTOMATIC, maxWidth, maxHeight);
		if (watermark != null && watermark.length > 0) {
			makeWatermark(watermark, image, font, fontColor);
		}
		ImageIO.write(image, "png", out);
	}

	public static void resizeGif(InputStream in, OutputStream out, int maxWidth, int maxHeight, float quality,
			String[] watermark, Font font, Color fontColor) throws IOException {
		checkParams(in, out, maxWidth, maxHeight, quality);
		//
		GifDecoder gd = new GifDecoder();
		int status = gd.read(in);
		if (status != GifDecoder.STATUS_OK) {
			return;
		}
		//
		AnimatedGifEncoder ge = new AnimatedGifEncoder();
		ge.start(out);
		ge.setRepeat(0);

		for (int i = 0; i < gd.getFrameCount(); i++) {
			BufferedImage frame = gd.getFrame(i);
			BufferedImage rescaled = Scalr.resize(frame, Method.AUTOMATIC, Mode.AUTOMATIC, maxWidth, maxHeight);
			if (watermark != null && watermark.length > 0) {
				makeWatermark(watermark, rescaled, font, fontColor);
			}
			//
			int delay = gd.getDelay(i);
			ge.setDelay(delay);
			ge.addFrame(rescaled);
		}

		ge.finish();
	}

	public static void makeWatermark(String[] text, BufferedImage image, Font font, Color fontColor) {
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (font != null) {
			graphics.setFont(font);
		} else {
			graphics.setFont(FONT);
		}
		if (fontColor == null) {
			fontColor = COLOR;
		}
		//
		graphics.setColor(fontColor);
		for (int i = 0; i < text.length; i++) {
			if ("".equals(text[i].trim())) {
				continue;
			}
			FontRenderContext context = graphics.getFontRenderContext();
			Rectangle2D fontRectangle = font.getStringBounds(text[i], context);
			int sw = (int) fontRectangle.getWidth();
			int sh = (int) fontRectangle.getHeight();
			if (text.length - i == 1) {
				graphics.drawString(text[i], image.getWidth() - sw - 6, image.getHeight() - 8);
			} else {
				graphics.drawString(text[i], image.getWidth() - sw - 6, image.getHeight() - sh * (text.length - 1) - 8);
			}
		}
		graphics.dispose();
	}

	private static void checkParams(InputStream in, OutputStream out, int maxWidth, int maxHeight, float quality)
			throws IOException {
		if (in == null) {
			throw new IOException("InputStream can not be null ");
		}
		if (out == null) {
			throw new IOException("OutputStream can not be null ");
		}
		if (maxWidth < 1 || maxHeight < 1) {
			throw new IOException("maxWidth or maxHeight can not be less than 1 ");
		}
		if (quality < 0f || quality > 1f) {
			throw new IOException("quality must be in [0-1] ");
		}
	}

	public static void makePng(String text, OutputStream out, int maxWidth, int cHeight, Font font, Color fontColor)
			throws ImageFormatException, IOException {
		int xM = 8;
		int yM = 25;
		cHeight = cHeight <= 0 ? 0 : cHeight;
		int maxHeight = 0;
		int line;
		int spaceWidth;
		int spaceHeight;
		if (font == null) {
			font = FONT;
		}

		buildFontRec(font);
		{
			spaceWidth = FONT_REC_MAP.get(font).get("　")[0];
			spaceHeight = FONT_REC_MAP.get(font).get("　")[1];

			line = 0;
			int lineWidth = 0;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				int sw = spaceWidth;
				int sh = spaceHeight;

				if (cHeight == 0) {
					cHeight = sh;
				}
				if (c == '\n') {
					line = line + 1;
					lineWidth = 0;
					continue;
				}
				if (c == '\t') {
					sw = spaceWidth * 2;
				} else if (c >= 0 && c < 128) {
					sw = FONT_REC_MAP.get(font).get(String.valueOf(c))[0];
				}
				//
				lineWidth = lineWidth + sw;
				if (lineWidth > maxWidth - 10 * 2) {
					line = line + 1;
					lineWidth = 0 + sw;
				} else if (lineWidth == maxWidth - 10 * 2) {
					line = line + 1;
					lineWidth = 0;
				}
			}
		}
		maxHeight = yM * 2 + cHeight * line;

		// create new image with right size/format
		BufferedImage image;
		BufferedImage bufferedImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		// ---------- 增加下面的代码使得背景透明 -----------------
		image = g2d.getDeviceConfiguration().createCompatibleImage(maxWidth, maxHeight, Transparency.TRANSLUCENT);
		g2d.dispose();
		//
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(font);
		if (fontColor != null) {
			graphics.setColor(fontColor);
		} else {
			graphics.setColor(FONT_COLOR);
		}
		//
		line = 0;
		int lineWidth = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			String str = String.valueOf(c);
			int sw = spaceWidth;
			// int sh = (int) fontRectangle.getHeight();
			int sh = cHeight;

			if (c == '\n') {
				graphics.drawString(sb.toString(), xM, yM + (line * sh));
				sb = new StringBuilder();
				line = line + 1;
				lineWidth = 0;
				continue;
			}
			if (c == '\t') {
				str = "　　";
				sw = spaceWidth * 2;
			} else if (c > 00 && c < 128) {
				sw = FONT_REC_MAP.get(font).get(String.valueOf(c))[0];
			}
			lineWidth = lineWidth + sw;
			if (lineWidth > maxWidth - 10 * 2) {
				graphics.drawString(sb.toString(), xM, yM + (line * sh));
				sb = new StringBuilder();
				sb.append(str);
				line = line + 1;
				lineWidth = 0 + sw;
			} else if (lineWidth == maxWidth - 10 * 2) {
				sb.append(str);
				graphics.drawString(sb.toString(), xM, yM + (line * sh));
				sb = new StringBuilder();
				line = line + 1;
				lineWidth = 0;
			} else {
				sb.append(str);
				if (i == text.length() - 1) {
					graphics.drawString(sb.toString(), xM, yM + (line * sh));
				}
			}
		}
		graphics.dispose();
		//
		ImageIO.write(image, "png", out);
	}

	public static synchronized void buildFontRec(Font font) {
		if (FONT_REC_MAP.get(font) != null) {
			return;
		}
		{
			Map<String, int[]> map = new HashMap<String, int[]>();
			BufferedImage nImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			Graphics2D ng = nImage.createGraphics();
			FontRenderContext context = ng.getFontRenderContext();

			Rectangle2D rect = font.getStringBounds("　", context);
			int spaceWidth = (int) rect.getWidth();
			int spaceHeight = (int) rect.getHeight();
			map.put("　", new int[] { spaceWidth, spaceHeight });
			//
			for (int i = 0; i < 128; i++) {
				rect = font.getStringBounds(String.valueOf((char) i), context);
				spaceWidth = (int) rect.getWidth();
				spaceHeight = (int) rect.getHeight();
				map.put(String.valueOf((char) i), new int[] { spaceWidth, spaceHeight });
			}
			FONT_REC_MAP.put(font, map);
		}
	}
}
