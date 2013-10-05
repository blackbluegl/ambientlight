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

package org.ambientlight.device.led;

import java.awt.Color;

import org.ambientlight.device.led.color.Color64Bit;


/**
 * @author Florian Bornkessel
 * 
 */
public class LedPoint {

	public LedPointConfiguration configuration;

	Color64Bit rgbValue;


	public void setPixel(int rgbValue) {
		this.rgbValue = new Color64Bit(new Color(rgbValue), configuration.gammaRed, configuration.gammaGreen,
				configuration.gammaBlue);
	}


	public Integer getOutputResult() {
		return rgbValue.getColor().getRGB();
	}


	public void clear() {
		this.rgbValue = new Color64Bit(Color.BLACK, configuration.gammaRed, configuration.gammaGreen, configuration.gammaBlue);
	}
}
