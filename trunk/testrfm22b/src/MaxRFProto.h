#ifndef __MAX_RF_PROTO_H
#define __MAX_RF_PROTO_H

#include <stdint.h>


#include "Util.h"
#include <string>


const size_t RF_ADDR_SIZE = 24;
const uint16_t ACTUAL_TEMP_UNKNOWN = 0xffff;
const uint8_t SET_TEMP_UNKNOWN = 0xff;
const uint8_t VALVE_UNKNOWN = 0xff;

// Constant string with external linkage, so it can be passed as a
// template param
const char na[] = "NA";

//typedef HexBits<RF_ADDR_SIZE> Address;
//typedef SpecialValue<Fixed<10, 1>,
//                     TInt<ACTUAL_TEMP_UNKNOWN>,
//                     TStr<na>>
//        ActualTemp;
//typedef SpecialValue<Fixed<2, 1>,
//                     TInt<SET_TEMP_UNKNOWN>,
//                     TStr<na>>
//        SetTemp;
//typedef SpecialValue<Postfix<NoFormat, TChar<'%'>>,
//                     TInt<VALVE_UNKNOWN>,
//                     TStr<na>>
//        ValvePos;

enum class Mode : uint8_t {AUTO, MANUAL, TEMPORARY, BOOST, UNKNOWN};
enum class DisplayMode : uint8_t {SET_TEMP, ACTUAL_TEMP};

enum class DeviceType : uint8_t {UNKNOWN, CUBE, WALL, RADIATOR};

enum class MessageType : uint8_t {
  PAIR_PING                      = 0x00,
  PAIR_PONG                      = 0x01,
  ACK                            = 0x02,
  TIME_INFORMATION               = 0x03,
  CONFIG_WEEK_PROFILE            = 0x10,
  CONFIG_TEMPERATURES            = 0x11,
  CONFIG_VALVE                   = 0x12,
  ADD_LINK_PARTNER               = 0x20,
  REMOVE_LINK_PARTNER            = 0x21,
  SET_GROUP_ID                   = 0x22,
  REMOVE_GROUP_ID                = 0x23,
  SHUTTER_CONTACT_STATE          = 0x30,
  SET_TEMPERATURE                = 0x40,
  WALL_THERMOSTAT_STATE          = 0x42,
  SET_COMFORT_TEMPERATURE        = 0x43,
  SET_ECO_TEMPERATURE            = 0x44,
  PUSH_BUTTON_STATE              = 0x50,
  THERMOSTAT_STATE               = 0x60,
  SET_DISPLAY_ACTUAL_TEMPERATURE = 0x82,
  WAKE_UP                        = 0xF1,
  RESET                          = 0xF0,
};

/**
 * Current state for a specific device.
 */
class Device {
public:
  uint32_t address;
  DeviceType type;
  const char *name;
  uint8_t set_temp; /* In 0.5° increments */
  uint16_t actual_temp; /* In 0.1° increments */
  unsigned long actual_temp_time; /* When was the actual_temp last updated */
  union {
    struct {
      Mode mode;
      uint8_t valve_pos; /* 0-64 (inclusive) */
    } radiator;

    struct {
    } wall;
  } data;
};

/*
 * Known devices, terminated with a NULL entry.
 */
extern Device devices[6];


class UntilTime {
public:
  /* Parse an until time from three bytes from an RF packet */
  UntilTime(const uint8_t *buf);


  uint8_t year, month, day;

  /* In 30-minute increments */
  uint8_t time;
};


class MaxRFMessage  {
public:
  /**
   * Parse a RF message. Buffer should contain only headers and
   * payload (so no length byte and no CRC).
   *
   * Note that the message might keep a reference to the buffer around
   * to prevent unnecessary copies!
   */
  static MaxRFMessage *parse(const uint8_t *buf, size_t len);

  /**
   * Returns a string describing a given message type.
   */
  static const std::string type_to_str(MessageType type);
  static const std::string mode_to_str(Mode mode);
  static const std::string display_mode_to_str(DisplayMode display_mode);


  /**
   * Update any device states that can be derived from this message.
   */
  virtual void updateState();

  uint8_t seqnum;
  uint8_t flags;
  MessageType type;
  uint32_t addr_from;
  uint32_t addr_to;
  uint8_t group_id;


  /* The devices adressed, or NULL for broadcast messages or unknown
   * devices. */
  Device *from;
  Device *to;

  virtual ~MaxRFMessage() {}
private:
  static MaxRFMessage *create_message_from_type(MessageType type);
  static DeviceType message_type_to_sender_type(MessageType type);
  virtual bool parse_payload(const uint8_t *buf, size_t len) = 0;
};

class UnknownMessage : public MaxRFMessage {
public:
  virtual bool parse_payload(const uint8_t *buf, size_t len);

  /**
   * The raw data of the message (excluding headers).
   * Shouldn't be freed, since this is a reference into the buffer
   * passed to parse.
   */
  const uint8_t* payload;
  size_t payload_len;
};

class SetTemperatureMessage : public MaxRFMessage {
public:
  virtual bool parse_payload(const uint8_t *buf, size_t len);

  uint8_t set_temp; /* In 0.5° units */
  Mode mode;

  UntilTime *until; /* Only when mode is MODE_TEMPORARY */

 // virtual ~SetTemperatureMessage() {delete this->until; }

};

class WallThermostatStateMessage : public MaxRFMessage {
public:
  virtual bool parse_payload(const uint8_t *buf, size_t len);
   virtual void updateState();

  uint16_t actual_temp; /* In 0.1° units */
  uint8_t set_temp; /* In 0.5° units */
};

class ThermostatStateMessage : public MaxRFMessage {
public:
  virtual bool parse_payload(const uint8_t *buf, size_t len);
  virtual void updateState();

  bool dst;
  bool locked;
  bool battery_low;
  Mode mode;
  uint8_t valve_pos; /* In percent */
  uint8_t set_temp; /* In 0.5° units */
  uint8_t actual_temp; /* In 0.1° units, 0 when not present */
  UntilTime *until; /* Only when mode is MODE_TEMPORARY */

  virtual ~ThermostatStateMessage() {delete this->until; }
};

class SetDisplayActualTemperatureMessage : public MaxRFMessage {
public:
  virtual bool parse_payload(const uint8_t *buf, size_t len);

  DisplayMode display_mode;
};

class AckMessage : public MaxRFMessage {
public:
  virtual bool parse_payload(const uint8_t *buf, size_t len);
  virtual void updateState();

  bool dst;
  bool locked;
  bool battery_low;
  Mode mode;
  uint8_t valve_pos; /* In percent */
  uint8_t set_temp; /* In 0.5° units */
  UntilTime *until; /* Only when mode is MODE_TEMPORARY */
  virtual ~AckMessage() {delete this->until; }
};

#endif // __MAX_RF_PROTO_H
