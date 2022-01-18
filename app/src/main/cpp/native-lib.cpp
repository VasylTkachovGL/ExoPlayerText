#include <jni.h>
#include <string>
#include <libusb.h>
#include "UacDevice.h"
#include "log.h"
#include "Utils.h"

#define TAG "ExoTestJNI"

static const uint16_t RATE = 44100;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_exoplayertext_JniRepository_recordFile(JNIEnv *env, jobject thiz, jstring filePath, jint fd, jint frequency, jint inBytesPerSample, jint inChannels) {
    LOG_D(TAG, "Init. Frequency: %d", frequency);
    UacDevice device(fd);
    LOG_D(TAG, "Preparing audio input");
    device.prepareAudioInput();
    device.setChannelSampleRate(UacDevice::Input, frequency);

    uint16_t inPacketSize = (frequency / 1000) * inBytesPerSample * inChannels;

    LOG_D(TAG, "Recording sample");
    size_t size = 15 * RATE * 3; // 5 second(s) of 44.1kHz audio at 24 bit/sample
    std::unique_ptr<unsigned char[]> pcmData(new unsigned char[size]);
    device.recordPCM(pcmData.get(), size, inPacketSize);
    LOG_D(TAG, "Sample recorded");

    std::unique_ptr<unsigned char[]> pcmData2(new unsigned char[size / 3 * 2]);
    for (size_t sample = 0; sample < size / 3; sample++) {
        pcmData2[sample * 2] = pcmData[sample * 3 + 1];
        pcmData2[sample * 2 + 1] = pcmData[sample * 3 + 2];
    }

    const char *filePathString = env->GetStringUTFChars(filePath, nullptr);
    LOG_D(TAG, "File path: %s", filePathString);

    FILE *pcm = fopen(filePathString, "w+b");
    check(pcm != nullptr, "fopen() pcm file");
    fwrite(pcmData.get(), 1, size, pcm);
    fclose(pcm);
    LOG_D(TAG, "File saved");
}