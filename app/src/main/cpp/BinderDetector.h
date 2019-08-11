//
// Created by penn on 11/08/19.
//

#ifndef TREBLECHECK_BINDERDETECTOR_H
#define TREBLECHECK_BINDERDETECTOR_H

#include <jni.h>
#include <sys/ioctl.h>
#include <stdlib.h>
#include <cerrno>
#include <fcntl.h>

struct binder_version {
    __s32 protocol_version;
};

const unsigned int BINDER_VERSION = (1U | 2U) << 30 | sizeof(struct binder_version) << 16 | 'b' << 8 | 9U;
// https://github.com/xylophone21/android-binder-standalone/blob/a92595efee6c28ea1a9b7f8892d90062abdc6718/androidtoolsets/include/bionic/linux/binder.h
#define BINDER_PATH "/dev/binder"

#endif //TREBLECHECK_BINDERDETECTOR_H
