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

package org.ambientlight.room.entities.lightobject.programms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.ambientlight.room.entities.lightobject.RenderObject;


/**
 * @author Florian Bornkessel
 * 
 */
public class Sunset extends RenderingProgramm {

	private final int FREQUENCY;

	// position in programm 1 is finished 0 is beginning
	double position = 0;

	double gamma = 3;

	// duration of sunset in minutes
	double duration = 0.7;

	double sunStartXPosition = 0.1;

	double sunStartYPosition = 0.7;

	double sunSetXPosition = 0.5;

	double sizeOfSun = 1;

	int oldSubSampleStep = -1;
	int largestSide = 0;

	Color sunStartColor = new Color(255, 255, 255, 255);
	Color sunSetColor = new Color(255, 230, 200, 255);
	Color sunStartCoronaColor = new Color(255, 245, 240, 127);
	Color sunSetCoronaColor = new Color(255, 190, 20, 128);
	Color sunStartCoronaEndColor = new Color(255, 245, 240, 0);
	Color sunSetCoronaEndColor = new Color(180, 100, 20, 0);
	Color borderStartColor = new Color(250, 245, 240, 0);
	Color borderEndColor = new Color(180, 100, 30, 0);

	Color backgroundBottomStart = new Color(250, 250, 250);
	Color backgroundBottomEnd = new Color(255, 128, 0);
	Color backgroundTopStart = new Color(250, 250, 250);
	Color backgroundTopEnd = new Color(40, 80, 100);

	Color backgroundStart = new Color(0, 70, 230, 0);
	Color backgroundEnd = new Color(0, 128, 255, 0);


	public Sunset(double duration, double position, double sunStartX, double sunStartY, double sunSetX, double sizeOfSun,
			double gamma, int frequency) {
		this.FREQUENCY = frequency;
		this.duration = duration;
		this.position = position;
		this.sunStartXPosition = sunStartX;
		this.sunStartYPosition = sunStartY;
		this.sunSetXPosition = sunSetX;
		this.sizeOfSun = sizeOfSun;
		this.gamma = gamma;
	}


	private double getStepWidth() {
		return 1 / (duration * 60 * FREQUENCY);
	}


	/*
	 * (non-Javadoc) height
	 * 
	 * @see
	 * org.ambientlight.rendering.programms.RenderingProgramm#renderLightObject
	 * (org.ambientlight.room.entities.LightObject)
	 */
	@Override
	public BufferedImage renderLightObject(RenderObject lightObject) {

		if (hasDirtyRegion() == false)
			return lightObject.getPixelMap();

		largestSide = lightObject.getPixelMap().getHeight() > lightObject.getPixelMap().getWidth() ? lightObject.getPixelMap()
				.getHeight() : lightObject.getPixelMap().getWidth();

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
		if (position > (1 - getStepWidth()))
			return false;
		else {
			boolean render = false;
			float subampling = (float) (largestSide * position) % 1;

			int subsamplingStep = (int) (subampling / 0.02);
			if (this.oldSubSampleStep != subsamplingStep) {
				oldSubSampleStep = subsamplingStep;
				render = true;
			}

			position = position + getStepWidth();
			// program finished. we do not need any updates anymore

			if (render)
				return true;
			else
				return false;

		}
	}


	private void renderSun(BufferedImage pixelmap) {

		Color sunColor = getBlendColor(sunStartColor, sunSetColor);
		Color coronaColor = getBlendColor(sunStartCoronaColor, sunSetCoronaColor);
		Color outerCoronaColor = getBlendColor(sunStartCoronaEndColor, sunSetCoronaEndColor);

		Point2D center = getPositionOfSun(pixelmap);
		center.setLocation(center.getX(), (center.getY() + pixelmap.getHeight() * 0.25f));
		Point2D focus = getPositionOfSun(pixelmap);
		focus.setLocation(focus.getX(), focus.getY() + pixelmap.getHeight() * 0.25f + (pixelmap.getHeight() * 0.1) * position);
		float rad = pixelmap.getWidth() < pixelmap.getHeight() ? pixelmap.getWidth() : pixelmap.getHeight();
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
		Point2D beginning = new Point2D.Float(pixelmap.getWidth() / 2, 0);
		Point2D end = new Point2D.Float(pixelmap.getWidth() / 2, pixelmap.getHeight());
		Point2D sun = getPositionOfSun(pixelmap);
		float sunPosition = (float) (sun.getY() / pixelmap.getHeight());
		if (sunPosition > 1.0f) {
			sunPosition = 0.9999f;
		}

		float aboveSun = (float) (sunPosition + 0.0f - (0.5f * (1 - position)));
		if (aboveSun <= 0f) {
			aboveSun = 0.0001f;
		}

		float underSun = sunPosition + 0.35f;
		if (underSun >= 1.0f) {
			underSun = 0.9999999f;
		}

		float[] dist = { 0.0f, aboveSun, underSun, 1.0f };
		Color[] colors = { backgroundTop, backgroundTop, backgroundBottom, backgroundBottom };
		LinearGradientPaint paint = new LinearGradientPaint(beginning, end, dist, colors);
		Graphics2D g = pixelmap.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(paint);
		g.fillRect(0, 0, pixelmap.getWidth(), pixelmap.getHeight());
	}


	private void renderBackground(BufferedImage pixelmap) {
		Point2D center = getPositionOfSun(pixelmap);
		center.setLocation(center.getX(), center.getY() + (pixelmap.getHeight() / 1.5) + 0.0f * pixelmap.getHeight() * position);

		Color blendedColor = getBlendColor(this.backgroundStart, this.backgroundEnd);
		Color outerColor = getBlendColor(this.backgroundStart, Color.BLACK);
		// blendedColor = new Color(blendedColor.getRed(),
		// blendedColor.getGreen(), blendedColor.getBlue(), 0);

		int maxSize = pixelmap.getWidth() > pixelmap.getHeight() ? pixelmap.getWidth() : pixelmap.getHeight();

		float radius = (float) (2.5 * (maxSize - maxSize * position));

		float[] dist = { 0.0f, 1f };
		Color[] colors = { blendedColor, outerColor };
		RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
		Graphics2D g = pixelmap.createGraphics();
		// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
		// RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(p);
		g.fillRect(0, 0, pixelmap.getWidth(), pixelmap.getHeight());
	}


	private Color getBlendColor(Color start, Color stop) {

		double weightStart = 1 - position;
		double weightStop = position;

		double r = weightStart * start.getRed() + weightStop * stop.getRed();
		r = (255 * (Math.pow(r / 255, gamma)));
		r = r < 0 ? 0 : r;

		double g = weightStart * start.getGreen() + weightStop * stop.getGreen();
		g = (255 * (Math.pow(g / 255, gamma)));
		g = g < 0 ? 0 : g;

		double b = weightStart * start.getBlue() + weightStop * stop.getBlue();
		b = b < 0 ? 0 : b;
		b = (255 * (Math.pow(b / 255, gamma)));

		double a = weightStart * start.getAlpha() + weightStop * stop.getAlpha();
		a = a < 0 ? 0 : a;
		a = a > 255 ? 255 : a;

		return new Color((int) r, (int) g, (int) b, (int) a);

	}


	private Point2D getPositionOfSun(BufferedImage pixelMap) {
		float xPosStart = (float) (pixelMap.getWidth() * sunStartXPosition);
		float xPosEnd = (float) (pixelMap.getWidth() * sunSetXPosition);
		float yPosStart = (float) (pixelMap.getHeight() * sunStartYPosition);
		float yPosEnd = pixelMap.getHeight();

		float xPos = (float) (xPosStart + ((xPosEnd - xPosStart) * position));
		float yPos = (float) (yPosStart + ((yPosEnd - yPosStart) * position));

		return new Point2D.Float(xPos, yPos);
	}
}
