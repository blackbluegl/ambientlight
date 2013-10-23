#include "MaxRF22.h"
#include <stdint.h>

const RF22::ModemConfig config =
{
	.reg_1c = 0x01, //F Filter Coefficient Sets. Defaults are for Rb = 40 kbps and Fd = 20 kHz so Bw = 80 kHz - resetvalue
	.reg_1f = 0x03, //Clock Recovery Slow Gearshift Value.
	.reg_20 = 0x90, //oversampling rate
	.reg_21 = 0x20, //nco1
	.reg_22 = 0x51, //nco2
	.reg_23 = 0xea, //nco3
	.reg_24 = 0x00, //cr loop gain1
	.reg_25 = 0x58, //cr loop gain2
        /* 2c - 2e are only for OOK */
        .reg_2c = 0x00,
        .reg_2d = 0x00,
        .reg_2e = 0x00,
	.reg_58 = 0x80, /* Copied from RF22 defaults */
	.reg_69 = 0x60, // pga gain override 21db


	.reg_6e = 0x08,  //datarate 1 15:8
	.reg_6f = 0x31,//datarate 2 7:0 == 1kbps

	.reg_70 = 0x24, // low datarate and mancheseter inversion... there is a possibility to inverse the preamble?!
	.reg_71 = RF22_DTMOD_FIFO | RF22_MODTYP_FSK, //fifo fsk as it sais. for me: direct mode via spi pin is possible to set here
	.reg_72 = 0x1e,//frequence derivation 18750
};

/* Sync words to send / check for. Don't forget to update RF22_SYNCLEN
 * below if changing the length of this array. */
const uint8_t sync_words[] = {
  0xc6,
  0x26,
  0xc6,
  0x26,
};

bool MaxRF22::init() {
  if (!RF22::init())
    return false;
  setModemRegisters(&config);
  setFrequency(868.3, 0.035);
  /* Disable TX packet control, since the RF22 doesn't do proper
   * whitening so can't read the length header or CRC. We need RX packet
   * control so the RF22 actually sends pkvalid interrupts when the
   * manually set packet length is reached. */
  spiWrite(RF22_REG_30_DATA_ACCESS_CONTROL, RF22_MSBFRST | RF22_ENPACRX);
  /* No packet headers, 4 sync words, fixed packet length */
  spiWrite(RF22_REG_32_HEADER_CONTROL1, RF22_BCEN_NONE | RF22_HDCH_NONE);
  spiWrite(RF22_REG_33_HEADER_CONTROL2, RF22_HDLEN_0 | RF22_FIXPKLEN | RF22_SYNCLEN_4);
  setSyncWords(sync_words, sizeof(sync_words));
  /* Detect preamble after 4 nibbles */
  spiWrite(RF22_REG_35_PREAMBLE_DETECTION_CONTROL1, (0x4 << 3));
  /* Send 8 bytes of preamble */
  setPreambleLength(255); // in nibbles
  spiWrite(RF22_REG_3E_PACKET_LENGTH, 20);
  return true;
}

/* vim: set sw=2 sts=2 expandtab: */
