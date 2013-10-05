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

package test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.led.colorcorrection.ColorCorrection;


/**
 * @author Florian Bornkessel
 * 
 */
public class TestColorCorrection {

	public static Color c = new Color(4, 4, 4);


	public static void main(String[] args) {
		for (int i = 0; i < 256; i++) {
			System.out.println("color with " + i);
			List<Integer> colors = new ArrayList<Integer>();
			Color c = new Color(i, i, i);
			colors.add(c.getRGB());
			colors.add(c.getRGB());
			colors.add(c.getRGB());
			colors.add(c.getRGB());
			colors.add(c.getRGB());

			ColorCorrection cc = new ColorCorrection(1.6f, 1.6f, 1.6f);
			List<Integer> result = cc.getCorrectedColors(colors);
			for (Integer current : result) {
				System.out.println(new Color(current));
			}
		}
	}
}
