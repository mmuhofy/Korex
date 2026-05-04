LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := korex-bootstrap
LOCAL_SRC_FILES := korex-bootstrap.c korex-bootstrap-zip.S
LOCAL_CFLAGS    := -DBOOTSTRAP_ZIP_PATH=\"$(BOOTSTRAP_ZIP_PATH)\"
include $(BUILD_SHARED_LIBRARY)