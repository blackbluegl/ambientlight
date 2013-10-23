#ifndef __MAX_RF_22_H
#define __MAX_RF_22_h

#include "RF22.h"
#include <stdint.h>

class MaxRF22 : public RF22 {
public:
  MaxRF22(uint8_t ss = 0, uint8_t interrupt = 0) : RF22(ss, interrupt) {}
  bool init();
};

#endif // __MAX_RF_22_H
//
