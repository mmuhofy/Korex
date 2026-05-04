#include <jni.h>
#include <stdint.h>
#include <string.h>

// Symbols provided by korex-bootstrap-zip.S
// The bootstrap zip is linked directly into the binary as raw bytes
extern uint8_t _binary_bootstrap_zip_start[];
extern uint8_t _binary_bootstrap_zip_end[];

JNIEXPORT jbyteArray JNICALL
Java_com_muhofy_korex_terminal_BootstrapInstaller_getZip(
        JNIEnv *env, jclass clazz) {
    size_t size = _binary_bootstrap_zip_end - _binary_bootstrap_zip_start;
    jbyteArray result = (*env)->NewByteArray(env, (jsize) size);
    if (result != NULL) {
        (*env)->SetByteArrayRegion(env, result, 0, (jsize) size,
                                   (const jbyte *) _binary_bootstrap_zip_start);
    }
    return result;
}