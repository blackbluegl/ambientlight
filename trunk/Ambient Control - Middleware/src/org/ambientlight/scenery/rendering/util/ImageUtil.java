package org.ambientlight.scenery.rendering.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class ImageUtil {
	
	//if g2d performance on rp is faster then look in simpleFade howto accelerate this
	public static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		}

	
	public static BufferedImage getPaintedImage(BufferedImage input, Color color){
		Graphics2D    graphics = input.createGraphics();
		graphics.setPaint (color);
		graphics.fillRect ( 0, 0, input.getWidth(), input.getHeight() );
		return input;
	}
	
	
	public static BufferedImage blend(BufferedImage source, BufferedImage target,
			double weight) {

		int width = source.getWidth();
		int height = source.getHeight();

		BufferedImage bi3 = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi3.createGraphics();
		g2d.drawImage(source, null, 0, 0);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				(float) (1.0 - weight)));
		g2d.drawImage(target, null, 0, 0);
		g2d.dispose();

		return bi3;
	}
	
	
	public static BufferedImage crop(BufferedImage src, int xOffset, int yOffset, int width, int height)
	{
	    BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    Graphics g = dest.getGraphics();
	    g.drawImage(src, 0, 0, width, height, xOffset, yOffset, xOffset + width, yOffset + height, null);
	    g.dispose();
	    return dest;
	}
	
}
