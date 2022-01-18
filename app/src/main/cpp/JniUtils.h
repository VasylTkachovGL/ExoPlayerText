#ifndef TESTNATIVEAPP_JNIUTILS_H
#define TESTNATIVEAPP_JNIUTILS_H

#include <jni.h>
#include <string>

jclass tryFindClass(JNIEnv* env, const char* className);

jint throwJNIException(JNIEnv* env, const char* className, const char* message);

#endif //TESTNATIVEAPP_JNIUTILS_H
