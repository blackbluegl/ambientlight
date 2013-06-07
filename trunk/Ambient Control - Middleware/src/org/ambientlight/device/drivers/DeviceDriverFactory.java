package org.ambientlight.device.drivers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.dummy.DummyDeviceDriver;
import org.ambientlight.device.drivers.dummyswitching.DummySwitchingDeviceDriver;
import org.ambientlight.device.drivers.multistripeoverethernet.MultistripeOverEthernetClientDeviceDriver;
import org.ambientlight.device.drivers.switchoverethernet.SwitchDeviceOverEthernetDriver;
import org.ambientlight.device.stripe.Stripe;
import org.ambientlight.device.stripe.StripeConfiguration;
import org.ambientlight.device.stripe.StripePartConfiguration;
import org.ambientlight.room.entities.StripePart;


public class DeviceDriverFactory {

	public DeviceDriver createByName(DeviceConfiguration dc) throws UnknownHostException, IOException {

		if (dc instanceof DummyLedStripeDeviceConfiguration) {
			System.out.println("init DummyLedDeviceDriver device");
			DummyDeviceDriver device = new DummyDeviceDriver();
			DummyLedStripeDeviceConfiguration configuration = (DummyLedStripeDeviceConfiguration) dc;

			for (StripeConfiguration currentStripeConfig : configuration.configuredStripes) {
				Stripe currentStripe = this.initializeStripeForDevice(currentStripeConfig);
				device.attachStripe(currentStripe);
			}

			device.connect();

			return device;
		}

		if (dc instanceof DummySwitchDeviceConfiguration) {
			System.out.println("init DummySwitchingDeviceDriver device");

			return new DummySwitchingDeviceDriver();
		}

		if (dc instanceof SwitchDeviceOverEthernetConfiguration) {
			System.out.println("init SwitchDeviceOverEthernetDriver device");
			SwitchDeviceOverEthernetConfiguration config = (SwitchDeviceOverEthernetConfiguration) dc;
			SwitchDeviceOverEthernetDriver device = new SwitchDeviceOverEthernetDriver();
			device.setConfiguration(config);

			return device;
		}

		if (dc instanceof MultiStripeOverEthernetClientDeviceConfiguration) {
			System.out.println("init MultistripeOverEthernetClientDeviceDriver device");

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
