#include <climits>
/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2019 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

#include <climits>
#include "BinderDetector.h"

extern "C" JNIEXPORT jint JNICALL
Java_tk_hack5_treblecheck_BinderDetector_get_1binder_1version(__unused JNIEnv *env, __unused jobject thiz) {
    struct binder_version version{};
    version.protocol_version = INT_MIN;
    int fd;
    int ret;
    fd = open(BINDER_PATH, O_CLOEXEC | O_RDWR); // NOLINT(hicpp-signed-bitwise)
    if (fd < 0) return -errno;
    ret = ioctl(fd, BINDER_VERSION, &version);
    close(fd);
    if (ret < 0) return ret;
    return version.protocol_version;
}
