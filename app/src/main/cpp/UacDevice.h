#ifndef _UAC_DEVICE_H_
#define _UAC_DEVICE_H_

#include "UsbDevice.h"

#include <string.h> // for size_t

class UacDevice
{
    UsbDevice device;

public:
    enum Channel
    {
        Output,
        Input
    };

    UacDevice(jint fd);
    ~UacDevice();

    void prepareAudioOutput();
    void prepareAudioInput();

    void setChannelSampleRate(Channel channel, int rate);
    int getChannelSampleRate(Channel channel);

    void recordPCM(unsigned char * data, size_t size, uint16_t inpPacketSize);

private:

    uint8_t getEpForChannel(Channel channel);
    uint8_t getInterfaceForChannel(Channel channel);
};


#endif // _UAC_DEVICE_H_
