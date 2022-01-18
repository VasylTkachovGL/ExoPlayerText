#include "UsbDevice.h"
#include "Utils.h"
#include "log.h"

#include <libusb.h>
#include <cstdio>
#include <jni.h>

#define TAG "UsbDevice"
static const size_t NUM_TRANSFERS = 10;
static const uint8_t NUM_PACKETS = 2;

UsbDevice::UsbDevice(jint fd) {
    // Init the library
    libusb_set_option(nullptr, LIBUSB_OPTION_WEAK_AUTHORITY);
    int ret = libusb_init(nullptr);
    check(ret, "libusb_init()");

    // The Android system owns the USB devices, and we can only ask the OS to do USB operations.
    // to do so, Android gives us a file descriptor we can do I/O operations on to perform USB read/writes.
    // opening and closing of this device must be done by the Java layer. we can call libusb_close(),
    // and in fact we should once we're done with the native handle, but we're not allowed to call
    // libusb_open(). instead, we use libusb_wrap_sys_device() to open the file descriptor that Java
    // gave us.
    libusb_wrap_sys_device(nullptr, fd, &hdev);
    check(hdev != nullptr, "open_device_with_vid_pid");

    // Allocate needed number of transfer objects
    for (size_t i = 0; i < NUM_TRANSFERS; i++) {
        libusb_transfer *xfer = libusb_alloc_transfer(NUM_PACKETS);
        availableXfers.push_back(xfer);

        libusb_transfer *xfer2 = libusb_alloc_transfer(NUM_PACKETS);
        availableOutXfers.push_back(xfer2);

        // TODO: gather in and out transfers, along with associated buffer in a single structure
        auto *buf = new uint8_t[1024 * NUM_PACKETS];  // Size of the buffer shall correspond input and output packet sizes, multiplied by NUM_TRANSFERS
        buffers.push_back(buf);
        // we need double number of buffers to handle simultaneous input and output
        buf = new uint8_t[1024 * NUM_PACKETS];  // Size of the buffer shall correspond input and output packet sizes, multiplied by NUM_TRANSFERS
        buffers.push_back(buf);
    }

    transferThread = nullptr;
}

UsbDevice::~UsbDevice() {
    for (libusb_transfer *xfer : availableXfers)
        libusb_free_transfer(xfer);

    for (libusb_transfer *xfer : availableOutXfers)
        libusb_free_transfer(xfer);

    for (uint8_t *buf : buffers)
        delete[] buf;

    libusb_close(hdev);
    libusb_exit(nullptr);
}

void UsbDevice::openInterface(uint8_t interface) {
    // Detach kernel driver if needed (so that we can operate instead of the driver)
    int ret = libusb_kernel_driver_active(hdev, interface);
    check(ret, "libusb_kernel_driver_active()");
    if (ret == 1) {
        ret = libusb_detach_kernel_driver(hdev, interface);
        check(ret, "libusb_detach_kernel_driver()");
    }

    // Now claim the interface
    ret = libusb_claim_interface(hdev, interface);
    check(ret, "libusb_claim_interface()");
}

void UsbDevice::closeInterface(uint8_t interface) {
    int ret = libusb_release_interface(hdev, interface);
    check(ret, "libusb_release_interface()");
}

void UsbDevice::setAltsetting(uint8_t interface, uint8_t altsetting) {
    int ret = libusb_set_interface_alt_setting(hdev, interface, altsetting);
    check(ret, "libusb_set_interface_alt_setting()");
}

void UsbDevice::controlReq(uint8_t requestType, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, uint16_t wLength, unsigned char *data) {
    int ret = libusb_control_transfer(hdev, requestType, bRequest, wValue, wIndex, data, wLength,
                                      1000);
    check(ret, "libusb_control_transfer()");
}

void UsbDevice::getControlAttr(bool recepient, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, uint16_t wLength, unsigned char *data) {
    uint8_t receiver = recepient ? LIBUSB_RECIPIENT_ENDPOINT : LIBUSB_RECIPIENT_INTERFACE;
    uint8_t bmRequestType = LIBUSB_ENDPOINT_IN | LIBUSB_REQUEST_TYPE_CLASS | receiver;
    controlReq(bmRequestType, bRequest, wValue, wIndex, wLength, data);
}

void UsbDevice::setControlAttr(bool recepient, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, uint16_t wLength, unsigned char *data) {
    uint8_t receiver = recepient ? LIBUSB_RECIPIENT_ENDPOINT : LIBUSB_RECIPIENT_INTERFACE;
    uint8_t bmRequestType = LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_CLASS | receiver;
    controlReq(bmRequestType, bRequest, wValue, wIndex, wLength, data);
}

void UsbDevice::transferCompleteCB(struct libusb_transfer *xfer) {
    auto *device = static_cast<UsbDevice *>(xfer->user_data);
    device->handleTransferCompleteCB(xfer);
}

void UsbDevice::handleTransferCompleteCB(libusb_transfer *xfer) {
    {
        std::lock_guard<std::mutex> guard(queueMutex);
        availableXfers.push_back(xfer);
    }
    transfer_cv.notify_one();
}

void UsbDevice::receiveIsoData(uint8_t ep, unsigned char *data, size_t size, uint16_t packetSize) {
    size_t totalPackets = size / packetSize;
    size_t bytesToGo = size;
    LOG_D(TAG, "Packets: %d", totalPackets);

    while (bytesToGo > 0) {
        // Schedule as many packet transfers as possible
        while (availableXfers.size() > 0) {
            size_t chunkSize = std::min((size_t) packetSize * NUM_PACKETS, bytesToGo);

            libusb_transfer *xfer = availableXfers.back();
            availableXfers.pop_back();

            LOG_D(TAG, "libusb_fill_iso_transfer");
            libusb_fill_iso_transfer(xfer, hdev, ep, data, chunkSize, NUM_PACKETS, transferCompleteCB, this, 1000);
            libusb_set_iso_packet_lengths(xfer, packetSize);
            libusb_submit_transfer(xfer);

            data += chunkSize;
            bytesToGo -= chunkSize;
        }

        int ret = libusb_handle_events(NULL);
        check(ret, "libusb_handle_events()");
    }

    LOG_D(TAG, "Wait for remaining packets to be sent");

    // Wait for remaining packets to be sent
//    while (availableXfers.size() != NUM_TRANSFERS) {
//        int ret = libusb_handle_events(NULL);
//        check(ret, "libusb_handle_events()");
//    }
    LOG_D(TAG, "Remaining packets sent");
}

