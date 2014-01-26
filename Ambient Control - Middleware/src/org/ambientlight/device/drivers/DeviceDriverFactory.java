package org.ambientlight.device.drivers;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.config.device.drivers.DeviceConfiguration;
import org.ambientlight.config.device.drivers.DummyLedStripeDeviceConfiguration;
import org.ambientlight.config.device.drivers.DummySwitchDeviceConfiguration;
import org.ambientlight.config.device.drivers.LK35CLientDeviceConfiguration;
import org.ambientlight.config.device.drivers.MultiStripeOverEthernetClientDeviceConfiguration;
import org.ambientlight.config.device.drivers.RemoteSwitchBridgeConfiguration;
import org.ambientlight.config.device.led.LedPointConfiguration;
import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.config.device.led.StripePartConfiguration;
import org.ambientlight.device.drivers.ledpoint.LK35ClientDeviceDriver;
import org.ambientlight.device.drivers.ledstripes.DummyLedStripeDeviceDriver;
import org.ambientlight.device.drivers.ledstripes.MultistripeOverEthernetClientDeviceDriver;
import org.ambientlight.device.drivers.remoteswitches.DummySwitchingDeviceDriver;
import org.ambientlight.device.drivers.remoteswitches.SwitchDeviceOverEthernetDriver;
import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.Stripe;
import org.ambientlight.device.led.StripePart;


public class DeviceDriverFactory {

	public RemoteSwtichDeviceDriver createRemoteSwitchDevice(DeviceConfiguration dc) {
		if (dc instanceof DummySwitchDeviceConfiguration) {
			System.out.println("DeviceDriverFactory: init DummySwitchingDeviceDriver device");

			return new DummySwitchingDeviceDriver();
		}

		if (dc instanceof RemoteSwitchBridgeConfiguration) {
			System.out.println("DeviceDriverFactory: init RemoteSwitchBridgeConfiguration device");
			RemoteSwitchBridgeConfiguration config = (RemoteSwitchBridgeConfiguration) dc;
			SwitchDeviceOverEthernetDriver device = new SwitchDeviceOverEthernetDriver();
			device.setConfiguration(config);

			return device;
		}

		return null;
	}


	public DeviceDriver createLedDevice(DeviceConfiguration dc) {

		if (dc instanceof DummyLedStripeDeviceConfiguration) {
			System.out.println("DeviceDriverFactory: init DummyLedDeviceDriver device");
			DummyLedStripeDeviceDriver device = new DummyLedStripeDeviceDriver();
			DummyLedStripeDeviceConfiguration configuration = (DummyLedStripeDeviceConfiguration) dc;

			for (StripeConfiguration currentStripeConfig : configuration.configuredStripes) {
				Stripe currentStripe = this.initializeStripeForDevice(currentStripeConfig);
				device.attachStripe(currentStripe);
			}

			device.connect();

			return device;
		}

		if (dc instanceof MultiStripeOverEthernetClientDeviceConfiguration) {
			System.out.println("DeviceDriverFactory: init MultistripeOverEthernetClientDeviceDriver device");

			MultiStripeOverEthernetClientDeviceConfiguration configuration = (MultiStripeOverEthernetClientDeviceConfiguration) dc;

			MultistripeOverEthernetClientDeviceDriver device = new MultistripeOverEthernetClientDeviceDriver();
			device.setConfiguration(configuration);

			for (StripeConfiguration currentStripeConfig : configuration.configuredStripes) {
				Stripe currentStripe = this.initializeStripeForDevice(currentStripeConfig);
				device.attachStripe(currentStripe);
			}
			try {
				device.connect();
			} catch (Exception e) {
				System.out
				.println("connect of MultistripeOverEthernetClientDeviceDriver device failed. Maybe the device comes up later: "
						+ e.getMessage());
			}
			return device;
		}

		if (dc instanceof LK35CLientDeviceConfiguration) {
			System.out.println("DeviceDriverFactory: init LK35CLientDeviceConfiguration device");
			LK35CLientDeviceConfiguration configuration = (LK35CLientDeviceConfiguration) dc;
			LK35ClientDeviceDriver device = new LK35ClientDeviceDriver();
			device.setConfiguration(configuration);
			for (LedPointConfiguration currentLedPointConfig : configuration.configuredLeds) {
				LedPoint ledPoint = new LedPoint();
				ledPoint.configuration = currentLedPointConfig;
				ledPoint.clear();
				device.attachLedPoint(ledPoint);
			}

			try {
				device.connect();
			} catch (Exception e) {
				System.out.println("connect of LK35CLientDeviceDriver device failed. Maybe the device comes up later: "
						+ e.getMessage());
			}
			return device;
		}

		// default if config name unknown
		return null;
	}


	private Stripe initializeStripeForDevice(StripeConfiguration stripeConfig) {

		Stripe stripe = new Stripe(stripeConfig);

		List<StripePart> stripeParts = new ArrayList<StripePart>();
		for (StripePartConfiguration currentStripePartConfig : stripeConfig.stripeParts) {
			StripePart currentStripePart = this.initializeStripePartForStripe(currentStripePartConfig, stripe);
			stripeParts.add(currentStripePart);
		}

		stripe.setStripeParts(stripeParts);

		return stripe;
	}


	private StripePart initializeStripePartForStripe(StripePartConfiguration stripePartConfig, Stripe stripe) {
		StripePart stripePart = new StripePart();
		stripePart.configuration = stripePartConfig;
		stripePart.stripe = stripe;

		return stripePart;
	}
}
