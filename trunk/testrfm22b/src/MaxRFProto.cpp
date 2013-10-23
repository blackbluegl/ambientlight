#include "MaxRFProto.h"
#include <string>
#include<wiringPi.h>
using namespace std;

/* TStreaming Formatting type to use for the field titles in print
 * output. */
//typedef Align<16> Title;
/**
 * Static list of known devices.
 */

Device devices[] = {
/* Add your devices here, for example: */
//{0x00b825, DeviceType::CUBE, "cube", SET_TEMP_UNKNOWN, ACTUAL_TEMP_UNKNOWN, 0},
//{0x0298e5, DeviceType::WALL, "wall", SET_TEMP_UNKNOWN, ACTUAL_TEMP_UNKNOWN, 0},
//{0x04c8dd, DeviceType::RADIATOR, "up  ", SET_TEMP_UNKNOWN, ACTUAL_TEMP_UNKNOWN, 0, {.radiator = {Mode::UNKNOWN, VALVE_UNKNOWN}}},
//{0x0131b4, DeviceType::RADIATOR, "down", SET_TEMP_UNKNOWN, ACTUAL_TEMP_UNKNOWN, 0, {.radiator = {Mode::UNKNOWN, VALVE_UNKNOWN}}},
		};

/* Find or assign a device struct based on the address */
static Device *get_device(uint32_t addr, DeviceType type) {
	for (unsigned int i = 0; i < lengthof(devices); ++i) {
		/* The address is not in the list yet, assign this empty slot. */
		if (devices[i].address == 0 && addr != 0) {
			devices[i].address = addr;
			devices[i].type = type;
			devices[i].name = NULL;
		}
		/* Found it */
		if (devices[i].address == addr)
			return &devices[i];
	}
	/* Not found and no slots left */
	return NULL;
}

/* MaxRFMessage */
const string MaxRFMessage::mode_to_str(Mode mode) {
	switch (mode) {
	case Mode::AUTO:
		return ("auto");
	case Mode::MANUAL:
		return ("manual");
	case Mode::TEMPORARY:
		return ("temporary");
	case Mode::BOOST:
		return ("boost");
	default:
		return ("");
	}
}

const string MaxRFMessage::display_mode_to_str(DisplayMode display_mode) {
	switch (display_mode) {
	case DisplayMode::SET_TEMP:
		return ("Set temperature");
	case DisplayMode::ACTUAL_TEMP:
		return ("Actual temperature");
	default:
		return ("");
	}
}

const string MaxRFMessage::type_to_str(MessageType type) {
	switch (type) {
	case MessageType::PAIR_PING:
		return ("PairPing");
	case MessageType::PAIR_PONG:
		return ("PairPong");
	case MessageType::ACK:
		return ("Ack");
	case MessageType::TIME_INFORMATION:
		return ("TimeInformation");
	case MessageType::CONFIG_WEEK_PROFILE:
		return ("ConfigWeekProfile");
	case MessageType::CONFIG_TEMPERATURES:
		return ("ConfigTemperatures");
	case MessageType::CONFIG_VALVE:
		return ("ConfigValve");
	case MessageType::ADD_LINK_PARTNER:
		return ("AddLinkPartner");
	case MessageType::REMOVE_LINK_PARTNER:
		return ("RemoveLinkPartner");
	case MessageType::SET_GROUP_ID:
		return ("SetGroupId");
	case MessageType::REMOVE_GROUP_ID:
		return ("RemoveGroupId");
	case MessageType::SHUTTER_CONTACT_STATE:
		return ("ShutterContactState");
	case MessageType::SET_TEMPERATURE:
		return ("SetTemperature");
	case MessageType::WALL_THERMOSTAT_STATE:
		return ("WallThermostatState");
	case MessageType::SET_COMFORT_TEMPERATURE:
		return ("SetComfortTemperature");
	case MessageType::SET_ECO_TEMPERATURE:
		return ("SetEcoTemperature");
	case MessageType::PUSH_BUTTON_STATE:
		return ("PushButtonState");
	case MessageType::THERMOSTAT_STATE:
		return ("ThermostatState");
	case MessageType::SET_DISPLAY_ACTUAL_TEMPERATURE:
		return ("SetDisplayActualTemperature");
	case MessageType::WAKE_UP:
		return ("WakeUp");
	case MessageType::RESET:
		return ("Reset");
	default:
		return ("Unknown");
	}
}

DeviceType MaxRFMessage::message_type_to_sender_type(MessageType type) {
	switch (type) {
	case MessageType::WALL_THERMOSTAT_STATE:
		return DeviceType::WALL;
	case MessageType::THERMOSTAT_STATE:
		return DeviceType::RADIATOR;
	case MessageType::SET_DISPLAY_ACTUAL_TEMPERATURE:
		return DeviceType::CUBE;
	default:
		return DeviceType::UNKNOWN;
	}
}

MaxRFMessage *MaxRFMessage::create_message_from_type(MessageType type) {
	switch (type) {
	case MessageType::SET_TEMPERATURE:
		return new SetTemperatureMessage();
	case MessageType::WALL_THERMOSTAT_STATE:
		return new WallThermostatStateMessage();
	case MessageType::THERMOSTAT_STATE:
		return new ThermostatStateMessage();
	case MessageType::SET_DISPLAY_ACTUAL_TEMPERATURE:
		return new SetDisplayActualTemperatureMessage();
	case MessageType::ACK:
		return new AckMessage();
	default:
		return new UnknownMessage();
	}
}

MaxRFMessage *MaxRFMessage::parse(const uint8_t *buf, size_t len) {
	if (len < 10)
		return NULL;

	MessageType type = (MessageType) buf[2];
	MaxRFMessage *m = create_message_from_type(type);

	m->seqnum = buf[0];
	m->flags = buf[1];
	m->type = type;
	m->addr_from = getBits(buf + 3, 0, RF_ADDR_SIZE);
	m->addr_to = getBits(buf + 6, 0, RF_ADDR_SIZE);
	m->group_id = buf[9];

	m->from = get_device(m->addr_from, message_type_to_sender_type(type));
	m->to = get_device(m->addr_to, DeviceType::UNKNOWN);

	if (m->parse_payload(buf + 10, len - 10))
		return m;
	else
		return NULL;
}

void MaxRFMessage::updateState() {
	/* Nothing to do */
}

/* UnknownMessage */
bool UnknownMessage::parse_payload(const uint8_t *buf, size_t len) {
	this->payload = buf;
	this->payload_len = len;
	return true;
}

/* SetTemperatureMessage */
bool SetTemperatureMessage::parse_payload(const uint8_t *buf, size_t len) {
	if (len < 1)
		return false;

	this->set_temp = buf[0] & 0x3f;
	this->mode = (Mode) ((buf[0] >> 6) & 0x3);

	if (len >= 4)
		this->until = new UntilTime(buf + 1);
	else
		this->until = NULL;

	return true;
}

/* WallThermostatStateMessage */

bool WallThermostatStateMessage::parse_payload(const uint8_t *buf, size_t len) {
	if (len < 2)
		return false;

	this->set_temp = buf[0] & 0x7f;
	this->actual_temp = ((buf[0] & 0x80) << 1) | buf[1];
	/* Note that mode and until time are not in this message */

	return true;
}

void WallThermostatStateMessage::updateState() {
	MaxRFMessage::updateState();
	this->from->set_temp = this->set_temp;
	this->from->actual_temp = this->actual_temp;
	this->from->actual_temp_time = millis();
}

/* ThermostatStateMessage */



bool ThermostatStateMessage::parse_payload(const uint8_t *buf, size_t len) {
	if (len < 3)
		return false;

	this->mode = (Mode) (buf[0] & 0x3);
	this->dst = (buf[0] >> 2) & 0x1;
	this->locked = (buf[0] >> 5) & 0x1;
	this->battery_low = (buf[0] >> 7) & 0x1;
	this->valve_pos = buf[1];
	this->set_temp = buf[2];

	this->actual_temp = 0;
	if (this->mode != Mode::TEMPORARY && len >= 5)
		this->actual_temp = ((buf[3] & 0x1) << 8) + buf[4];

	this->until = NULL;
	if (this->mode == Mode::TEMPORARY && len >= 6)
		this->until = new UntilTime(buf + 3);

	return true;
}

void ThermostatStateMessage::updateState() {
	this->from->set_temp = this->set_temp;
	this->from->data.radiator.valve_pos = this->valve_pos;
	if (this->actual_temp) {
		this->from->actual_temp = this->actual_temp;
		this->from->actual_temp_time = millis();
	}
}

/* SetDisplayActualTemperatureMessage */
bool SetDisplayActualTemperatureMessage::parse_payload(const uint8_t *buf, size_t len) {
	if (len < 1)
		return NULL;
	this->display_mode = (DisplayMode) ((buf[0] >> 2) & 0x1);
	return true;
}



bool AckMessage::parse_payload(const uint8_t *buf, size_t len) {
	if (len < 4)
		return false;

	/* XXX: Perhaps buf[0] == 0x01 can be used here instead? */
	if (this->from && this->from->type == DeviceType::RADIATOR) {
		/* We only know about packet formats sent by radiators yet */

		this->mode = (Mode) (buf[1] & 0x3);
		this->dst = (buf[1] >> 2) & 0x1;
		/* The locked and battery_low bits are unconfirmed, but they probably
		 * match the RadiatorThermostateStateMessage. */
		this->locked = (buf[1] >> 5) & 0x1;
		this->battery_low = (buf[1] >> 7) & 0x1;
		this->valve_pos = buf[2];
		this->set_temp = buf[3];

		this->until = NULL;
		if (this->mode == Mode::TEMPORARY && len >= 7)
			this->until = new UntilTime(buf + 4);
	}

	return true;
}

void AckMessage::updateState() {
	if (this->from && this->from->type == DeviceType::RADIATOR) {
		this->from->set_temp = this->set_temp;
		this->from->data.radiator.valve_pos = this->valve_pos;
	}
}

/* UntilTime */

UntilTime::UntilTime(const uint8_t *buf) {
	this->year = buf[1] & 0x3f;
	this->month = ((buf[0] & 0xE0) >> 4) | (buf[1] >> 7);
	this->day = buf[0] & 0x1f;
	this->time = buf[2] & 0x3f;
}

/*
 Sequence num:   E4
 Flags:          04
 Packet type:    70 (Unknown)
 Packet from:    0298E5
 Packet to:      000000
 Group id:       00
 Payload:        19 04 2A 00 CD

 19: DST switch, mode = auto
 0:1 mode
 2   DST switch
 5   ??
 04: Display mode?
 2A: set temp (21°)
 00 CD: Actual temp (20.5°)

 Sequence num:   9C
 Flags:          04
 Packet type:    70 (Unknown)
 Packet from:    0298E5
 Packet to:      000000
 Group id:       00
 Payload:        12 04 24 48 0D 1B

 19: DST switch, mode = temporary
 0:1 mode
 2   DST switch
 5   ??
 04: Display mode?
 24: set temp (18°)
 48 0D 1B: until time

 Perhaps 70 is really WallThermostatState and the curren WallThermostatState is
 more of a "update temp" message? It seems 70 is sent when the SetTemp of a WT
 changes.

 */

/*
 Set DST adjust
 Sequence num:   E5
 Flags:          00
 Packet type:    81 (Unknown)
 Packet from:    00B825
 Packet to:      0298E5
 Group id:       00
 Payload:        00
 00: Disable
 01: Enable

 Sent to radiator thermostats only?

 */

/*

 Ack from radiator thermostat:

 Sequence num:   2C
 Flags:          02
 Packet type:    02 (Ack)
 Packet from:    0298E5
 Packet to:      04C8DD
 Group id:       00
 Payload:        01 11 00 28
 01: 1 == more data? 0 == no data??
 11: flags, same as 11/19 in type 70?
 00: Valve position / displaymode flags
 28: Set temp

 Sequence num:   1B
 Flags:          02
 Packet type:    02 (Ack)
 Packet from:    0298E5
 Packet to:      00B825
 Group id:       00
 Payload:        01 12 04 24 48 0D 1B

 01: 1 == more data? 0 == no data??
 11: flags, same as 11/19 in type 70? x2 == temporary
 00: Valve position / displaymode flags
 24: Set temp (18.0°)
 48 0D 1B: Until time


 Ack from wall thermostat to SetTemperature:

 Sequence num:   4F
 Flags:          00
 Packet type:    02 (Ack)
 Packet from:    00B825
 Packet to:      0298E5
 Group id:       00
 Payload:        00

 00: ???
 */

/* vim: set sw=2 sts=2 expandtab: */
