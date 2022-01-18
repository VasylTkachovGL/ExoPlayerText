package com.example.exoplayertext


/*
* @author Tkachov Vasyl
* @since 17.01.2022
*/
class JniRepository {

    external fun recordFile(filePath: String, fileDescriptor: Int, frequency: Int, inBytesPerSample: Int,  inChannels: Int)

    companion object {
        // Used to load the 'native-libs' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}