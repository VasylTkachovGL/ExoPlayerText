#ifndef _USB_DEVICE_H
#define _USB_DEVICE_H

#include <stdint.h>
#include <vector>
#include <libusb.h>
#include <jni.h>
#include <thread>
#include <list>
#include <mutex>
#include <condition_variable>
#include <atomic>

struct libusb_device_handle;
struct libusb_transfer;

class UsbDevice
{
    libusb_device_handle * hdev;

    std::vector<libusb_transfer *>  availableXfers;
    std::vector<libusb_transfer *>  availableOutXfers;
    std::vector<uint8_t *> buffers;

    struct TransferQueueEntry
    {
        uint8_t ep;
        uint8_t * data;
        uint16_t len;
    };

    std::list<TransferQueueEntry> transferQueue;
    std::mutex queueMutex;
    std::condition_variable transfer_cv;

    std::thread * transferThread;
    std::atomic<bool> stopping;

public:
    UsbDevice(jint fd);
    ~UsbDevice();

    void openInterface(uint8_t interface);
    void setAltsetting(uint8_t interface, uint8_t altsetting);

    void controlReq(uint8_t requestType, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, uint16_t wLength, unsigned char * data);
    void getControlAttr(bool recepient, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, uint16_t wLength, unsigned char * data);
    void setControlAttr(bool recepient, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, uint16_t wLength, unsigned char * data);

    void receiveIsoData(uint8_t ep, unsigned char * data, size_t size, uint16_t packetSize);

    void closeInterface(uint8_t interface);


protected:
    static void transferCompleteCB(libusb_transfer * xfer);
    virtual void handleTransferCompleteCB(libusb_transfer * xfer);

    std::thread thread;
};

#endif //_USB_DEVICE_H
