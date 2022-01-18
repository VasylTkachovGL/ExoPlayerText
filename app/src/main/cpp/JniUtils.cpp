#include "JniUtils.h"

jclass tryFindClass(JNIEnv* env, const char* className) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        if (strcmp(className, "java/lang/NoClassDefFoundError") == 0) {
            throw std::exception();
        }
        std::string str = "Could not find class ";
        str += className;
        throwJNIException(env, "java/lang/NoClassDefFoundError", str.c_str());
        throw std::exception();
    }
    return clazz;
}

jint throwJNIException(JNIEnv* env, const char* className, const char* message) {
    jclass exceptionClass = env->FindClass(className);
    return env->ThrowNew(exceptionClass, message);
}