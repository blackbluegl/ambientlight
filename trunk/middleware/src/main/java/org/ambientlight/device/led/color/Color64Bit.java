/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambientlight.device.led.color;

import java.awt.Color;

import org.ambientlight.config.device.led.ColorConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public class Color64Bit {

	public int red = 0;
	public int green = 0;
	public int blue = 0;

	public final float gammaRed;
	public final float gammaGreen;
	public final float gammaBlue;

	public final float levelRed;
	public final float levelGreen;
	public final float levelBlue;


	public Color64Bit(Color color, ColorConfiguration colorConfiguratin) {

		if (colorConfiguratin.levelRed > 1.0f || colorConfiguratin.levelGreen > 1.0f || colorConfiguratin.levelBlue > 1.0f
				|| colorConfiguratin.levelRed <= 0.0f || colorConfiguratin.levelGreen <= 0.0f
				|| colorConfiguratin.levelBlue <= 0.0f)
			throw new IllegalArgumentException("level of color must be greater or equal zeron and not greater than 1.0");

		// store gamma values
		this.gammaRed = colorConfiguratin.gammaRed;
		this.gammaBlue = colorConfiguratin.gammaBlue;
		this.gammaGreen = colorConfiguratin.gammaGreen;

		// store level values
		this.levelRed = colorConfiguratin.levelRed;
		this.levelGreen = colorConfiguratin.levelGreen;
		this.levelBlue = colorConfiguratin.levelBlue;

		// convert rgb values
		double red = color.getRed() / 255.0d;
		double green = color.getGreen() / 255.0d;
		double blue = color.getBlue() / 255.0d;

		// apply gamma correction
		red = Math.pow(red, colorConfiguratin.gammaRed);
		green = (Math.pow(green, colorConfiguratin.gammaGreen));
		blue = (Math.pow(blue, colorConfiguratin.gammaBlue));

		// apply level adjustment - after gamma correction
		red = this.levelRed * red;
		green = this.levelGreen * green;
		blue = this.levelBlue * blue;

		// store and convert to 64 bit integer values
		this.red = (int) (red * Integer.MAX_VALUE);
		this.green = (int) (green * Integer.MAX_VALUE);
		this.blue = (int) (blue * Integer.MAX_VALUE);
	}


	public Color getColor() {
		float red256 = ((float) red / Integer.MAX_VALUE) * 255;
		float green256 = ((float) green / Integer.MAX_VALUE) * 255;
		float blue256 = ((float) blue / Integer.MAX_VALUE) * 255;
		return new Color((int) red256, (int) green256, (int) blue256);
	}
}
