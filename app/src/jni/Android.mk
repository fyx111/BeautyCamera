LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
include D:/SDK/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
LOCAL_MODULE := face_detection
LOCAL_SRC_FILES := haar_detect.cpp
LOCAL_LDLIBS += -llog -ldl
include $(BUILD_SHARED_LIBRARY)