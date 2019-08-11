//
// Created by penn on 11/08/19.
//

#include "BinderDetector.h"

extern "C" JNIEXPORT jint JNICALL
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
Java_tk_hack5_treblecheck_BinderDetector_get_1binder_1version(JNIEnv *env, jobject instance) {
#pragma clang diagnostic pop
    struct binder_version version{};
    version.protocol_version = -1;
    int fd;
    int ret;
    fd = open(BINDER_PATH, O_CLOEXEC | O_RDWR); // NOLINT(hicpp-signed-bitwise)
    if (fd < 0) return -errno;
    ret = ioctl(fd, BINDER_VERSION, &version);
    if (ret < 0) return -abs(ret);
    return version.protocol_version;
}