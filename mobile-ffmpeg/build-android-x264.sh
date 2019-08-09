export ANDROID_HOME=/home/sam/Android/Sdk
export ANDROID_NDK_ROOT=/home/sam/Android/Sdk/ndk-bundle

./android.sh \
    --lts \
    --speed \
    --enable-gpl \
    --enable-android-media-codec \
    --enable-android-zlib \
    --enable-gmp \
    --enable-gnutls \
    --enable-libxml2
