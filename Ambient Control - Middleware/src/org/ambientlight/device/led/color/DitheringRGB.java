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
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.led.ColorConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public class DitheringRGB {


	public List<Color> getDitheredRGB(List<Color64Bit> input) {
		if (input == null)
			return new ArrayList<Color>();
		return ditherAndDownSample(input);
	}


	private List<Color> ditherAndDownSample(List<Color64Bit> input) {

		if(input ==null||input.isEmpty())
			return new ArrayList<Color>();

		List<Color> result = new ArrayList<Color>();

		// fill to dither correctly
		int fillCount = input.size() % 6;
		if (fillCount > 0) {
			fillCount = 6 - fillCount;
		}
		ColorConfiguration config =new ColorConfiguration();
		config.gammaRed=input.get(0).gammaRed;
		config.gammaGreen=input.get(0).gammaGreen;
		config.gammaBlue=input.get(0).gammaBlue;
		config.levelRed = input.get(0).levelRed;
		config.levelGreen = input.get(0).levelGreen;
		config.levelBlue = input.get(0).levelBlue;

		for (int i = 0; i < fillCount; i++) {
			input.add(0, new Color64Bit(new Color(0, 0, 0), config));
		}

		for (int i = 0; i < input.size(); i = i + 6) {
			List<Integer> red = ditherAndDownsampleValue(new int[] { input.get(i).red, input.get(i + 1).red,
					input.get(i + 2).red, input.get(i + 3).red, input.get(i + 4).red, input.get(i + 5).red });
			List<Integer> green = ditherAndDownsampleValue(new int[] { input.get(i).green, input.get(i + 1).green,
					input.get(i + 2).green, input.get(i + 3).green, input.get(i + 4).green, input.get(i + 5).green });
			List<Integer> blue = ditherAndDownsampleValue(new int[] { input.get(i).blue, input.get(i + 1).blue,
					input.get(i + 2).blue, input.get(i + 3).blue, input.get(i + 4).blue, input.get(i + 5).blue });
			for (int y = 0; y < 6; y++) {
				Color color = new Color(red.get(y), green.get(y), blue.get(y));
				result.add(color);
			}
		}

		// restore original size array
		for (int i = 0; i < fillCount; i++) {
			result.remove(0);
		}
		return result;
	}


	private List<Integer> ditherAndDownsampleValue(int[] values) {

		int quantInHigh = Integer.MAX_VALUE / 255;

		List<Integer> result = new ArrayList<Integer>();

		int quantError = 0;

		for (Integer value : values) {
			float currentValueIn256 = ((float) value / Integer.MAX_VALUE) * 255;
			float errorIn256 = currentValueIn256 % 1;
			int currentQuantIn256 = (int) (currentValueIn256 - errorIn256);
			int errorInHigh = (int) (quantInHigh * errorIn256);

			result.add(currentQuantIn256);
			quantError += errorInHigh;
		}
		quantError = quantError / 6;

		int[] patternErrorDifuse1 = new int[] { 0, 0, 0, 0, 0, 1 };
		int[] patternErrorDifuse2 = new int[] { 0, 0, 0, 1, 0, 1 };
		int[] patternErrorDifuse3 = new int[] { 0, 1, 0, 1, 0, 1 };
		int[] patternErrorDifuse4 = new int[] { 0, 1, 0, 1, 1, 1 };
		int[] patternErrorDifuse5 = new int[] { 0, 1, 1, 1, 1, 1 };
		if (quantError > 0.8333 * quantInHigh) {
			addError(result, patternErrorDifuse5);
		} else if (quantError > 0.6666 * quantInHigh) {
			addError(result, patternErrorDifuse4);
		} else if (quantError > 0.5 * quantInHigh) {
			addError(result, patternErrorDifuse3);
		} else if (quantError > 0.3333 * quantInHigh) {
			addError(result, patternErrorDifuse2);
		} else if (quantError > 0.1666 * quantInHigh) {
			addError(result, patternErrorDifuse1);
		}

		return result;
	}


	private void addError(List<Integer> result, int[] patternErrorDifuse1) {
		for (int i = 0; i < result.size(); i++) {
			result.set(i, result.get(i) + patternErrorDifuse1[i]);
		}
	}
}
