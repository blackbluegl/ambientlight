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

package org.ambientlight.device.led.colorcorrection;

import java.awt.Color;


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


	public Color64Bit(Color color, float gammaRed, float gammaGreen, float gammaBlue) {

		float factorRed = (float) color.getRed() / 255;
		float factorGreen = (float) color.getGreen() / 255;
		float factorBlue = (float) color.getBlue() / 255;

		int red = (int) (factorRed * Integer.MAX_VALUE);
		int green = (int) (factorGreen * Integer.MAX_VALUE);
		int blue = (int) (factorBlue * Integer.MAX_VALUE);

		this.red = (int) (Integer.MAX_VALUE * Math.pow((float) red / Integer.MAX_VALUE, gammaRed));
		this.green = (int) (Integer.MAX_VALUE * (Math.pow((float) green / Integer.MAX_VALUE, gammaGreen)));
		this.blue = (int) (Integer.MAX_VALUE * (Math.pow((float) blue / Integer.MAX_VALUE, gammaBlue)));

		this.gammaRed = gammaRed;
		this.gammaBlue = gammaBlue;
		this.gammaGreen = gammaGreen;

	}


	public Color getColor() {
		float red256 = ((float) red / Integer.MAX_VALUE) * 255;
		float green256 = ((float) green / Integer.MAX_VALUE) * 255;
		float blue256 = ((float) blue / Integer.MAX_VALUE) * 255;
		return new Color((int) red256, (int) green256, (int) blue256);
	}
}
