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

#ifndef TREBLECHECK_BINDERDETECTOR_H
#define TREBLECHECK_BINDERDETECTOR_H

#include <jni.h>
#include <sys/ioctl.h>
#include <stdlib.h>
#include <cerrno>
#include <fcntl.h>
#include <unistd.h>

struct binder_version {
    __s32 protocol_version;
};

const unsigned int BINDER_VERSION = (1U | 2U) << 30 | sizeof(struct binder_version) << 16 | 'b' << 8 | 9U;
// https://github.com/zhaodm/android-binder-standalone/blob/a92595efee6c28ea1a9b7f8892d90062abdc6718/androidtoolsets/include/bionic/linux/binder.h
#define BINDER_PATH "/dev/binder"

#endif //TREBLECHECK_BINDERDETECTOR_H
