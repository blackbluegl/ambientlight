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

package org.ambientlight.rendering.programms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.room.entities.LightObject;


/**
 * @author Florian Bornkessel
 * 
 */
public class Sunset extends RenderingProgramm {

	// position in programm 1 is finished 0 is beginning
	double position = 0;

	// duration of sunset in minutes
	double duration = 0.7;

	double sunStartXPosition = 0.1;

	double sunStartYPosition = 0.7;

	double sunSetXPosition = 0.5;

	double sizeOfSun = 1;

	Color sunStartColor = new Color(255, 255, 255, 255);
	Color sunSetColor = new Color(230, 180, 120, 255);
	Color sunStartCoronaColor = new Color(250, 245, 240, 127);
	Color sunSetCoronaColor = new Color(160, 100, 20, 128);
	Color sunStartCoronaEndColor = new Color(250, 245, 240, 0);
	Color sunSetCoronaEndColor = new Color(160, 100, 20, 0);
	Color borderStartColor = new Color(250, 245, 240, 0);
	Color borderEndColor = new Color(160, 100, 30, 0);

	Color backgroundInnerStart = new Color(250, 250, 250, 0);
	Color backgroundBottomStart = new Color(250, 250, 250);
	Color backgroundBottomEnd = new Color(200, 60, 10);
	Color backgroundTopStart = new Color(210, 220, 230);
	Color backgroundTopEnd = new Color(5, 0, 60);

	Color backgroundStart = new Color(210, 220, 230, 0);
	Color backgroundEnd = new Color(22, 33, 120, 0);


	public Sunset(double duration, double sunStartX, double sunStartY, double sunSetX, double sizeOfSun) {
		this.duration = duration;
		this.sunStartXPosition = sunStartX;
		this.sunStartYPosition = sunStartY;
		this.sunSetXPosition = sunSetX;
		this.sizeOfSun = sizeOfSun;
	}


	private double getStepWidth() {
		return 1 / (duration * 60 * AmbientControlMW.FREQUENCY);
	}


	/*
	 * (non-Javadoc) height
	 * 
	 * @see
	 * org.ambientlight.rendering.programms.RenderingProgramm#renderLightObject
	 * (org.ambientlight.room.entities.LightObject)
	 */
	@Override
	public BufferedImage renderLightObject(LightObject lightObject) {
		if (hasDirtyRegion() == false)
			return lightObject.getPixelMap();
		position = position + getStepWidth();
		renderSunset(lightObject.getPixelMap());
		renderSun(lightObject.getPixelMap());
		renderBackground(lightObject.getPixelMap());
		return lightObject.getPixelMap();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.rendering.programms.RenderingProgramm#hasDirtyRegion()
	 */
	@Override
	public boolean hasDirtyRegion() {
		// program finished. we do not need any updates anymore
		if (position > 1 - getStepWidth())
			return false;
		return true;
	}


	private void renderSun(BufferedImage pixelmap) {

		Color sunColor = getBlendColor(sunStartColor, sunSetColor);
		Color coronaColor = getBlendColor(sunStartCoronaColor, sunSetCoronaColor);
		Color outerCoronaColor = getBlendColor(sunStartCoronaEndColor, sunSetCoronaEndColor);

		Point center = getPositionOfSun(pixelmap);
		center.setLocation(center.x, (center.y + pixelmap.getHeight() * 0.1f));
		Point focus = getPositionOfSun(pixelmap);
		focus.setLocation(focus.x, focus.y + (pixelmap.getHeight() * 0.2) * position);
		int rad = pixelmap.getWidth() < pixelmap.getHeight() ? pixelmap.getWidth() : pixelmap.getHeight();
		float radius = (float) (rad * this.sizeOfSun);

		float[] dist = { 0.04f, 0.05f, 0.06f, 0.28f };
		Color[] colors = { sunColor, sunColor, coronaColor, outerCoronaColor };
		RadialGradientPaint paint = new RadialGradientPaint(center, radius, focus, dist, colors, CycleMethod.NO_CYCLE);
		Graphics2D g = pixelmap.createGraphics();
		g.setPaint(paint);
		g.fillRect(0, 0, pixelmap.getWidth(), pixelmap.getHeight());
	}


	private void renderSunset(BufferedImage pixelmap) {
		Color backgroundTop = getBlendColor(backgroundTopStart, backgroundTopEnd);
		Color backgroundBottom = getBlendColor(backgroundBottomStart, backgroundBottomEnd);
		Point beginning = new Point(pixelmap.getWidth() / 2, 0);
		Point end = new Point(pixelmap.getWidth() / 2, pixelmap.getHeight());
		Point sun = getPositionOfSun(pixelmap);
		float sunPosition = (float) (sun.getY() / pixelmap.getHeight());
		if (sunPosition > 1.0f) {
			sunPosition = 0.9999f;
		}

		float aboveSun = (float) (sunPosition - (0.3f * (1 - position)));
		if (aboveSun <= 0f) {
			aboveSun = 0.0001f;
		}
		float underSun = sunPosition + 0.21f;
		if (underSun >= 1.0f) {
			underSun = 0.9999999f;
		}

		float[] dist = { 0.0f, aboveSun, underSun, 1.0f };
		Color[] colors = { backgroundTop, backgroundTop, backgroundBottom, backgroundBottom };
		LinearGradientPaint paint = new LinearGradientPaint(beginning, end, dist, colors);
		Graphics2D g = pixelmap.createGraphics();
		g.setPaint(paint);
		g.fillRect(0, 0, pixelmap.getWidth(), pixelmap.getHeight());
	}


	private void renderBackground(BufferedImage pixelmap) {
		Point center = getPositionOfSun(pixelmap);
		center.y = center.y + pixelmap.getHeight() / 2;

		Color blendedColor = getBlendColor(this.backgroundStart, this.backgroundEnd);
		blendedColor = new Color(blendedColor.getRed(), blendedColor.getGreen(), blendedColor.getBlue(), 0);

		int maxSize = pixelmap.getWidth() > pixelmap.getHeight() ? pixelmap.getWidth() : pixelmap.getHeight();

		float radius = (float) (2.5 * (maxSize - maxSize * position));

		float[] dist = { 0.0f, 1f };
		Color[] colors = { blendedColor, new Color(0, 0, 0, 200) };
		RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
		Graphics2D g = pixelmap.createGraphics();
		g.setPaint(p);
		g.fillRect(0, 0, pixelmap.getWidth(), pixelmap.getHeight());
	}


	private Color getBlendColor(Color start, Color stop) {
		double weightStart = 1 - position;
		double weightStop = position;

		double r = weightStart * start.getRed() + weightStop * stop.getRed();
		r = r < 0 ? 0 : r;
		double g = weightStart * start.getGreen() + weightStop * stop.getGreen();
		g = g < 0 ? 0 : g;
		double b = weightStart * start.getBlue() + weightStop * stop.getBlue();
		b = b < 0 ? 0 : b;
		double a = weightStart * start.getAlpha() + weightStop * stop.getAlpha();
		a = a < 0 ? 0 : a;
		a = a > 255 ? 255 : a;

		return new Color((int) r, (int) g, (int) b, (int) a);

	}


	private Point getPositionOfSun(BufferedImage pixelMap) {
		int xPosStart = (int) (pixelMap.getWidth() * sunStartXPosition);
		int xPosEnd = (int) (pixelMap.getWidth() * sunSetXPosition);
		int yPosStart = (int) (pixelMap.getHeight() * sunStartYPosition);
		int yPosEnd = pixelMap.getHeight();

		int xPos = (int) (xPosStart + ((xPosEnd - xPosStart) * position));
		int yPos = (int) (yPosStart + ((yPosEnd - yPosStart) * position));

		return new Point(xPos, yPos);
	}
}
