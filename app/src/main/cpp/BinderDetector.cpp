/*
 *     Treble Info
 *     Copyright (C) 2019-2022 Hackintosh Five
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
// SPDX-License-Identifier: GPL-3.0-or-later

#include <climits>
#include "BinderDetector.h"

extern "C" JNIEXPORT jint JNICALL
Java_tk_hack5_treblecheck_data_BinderDetector_get_1binder_1version(__unused JNIEnv *env, __unused jobject thiz) {
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
