#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cosmic_worldboxloader_MainActivity_nativeLoaderStatus(
        JNIEnv* env, jobject) {
    const std::string status =
        "Native scaffold ready. No game injection or DLL execution is implemented.";
    return env->NewStringUTF(status.c_str());
}
