package edu.playground.djivln.mini2

import android.graphics.Bitmap
import edu.playground.djivln.vln.AoaUsbProbe
import edu.playground.djivln.vln.InferenceControlClient

class Mini2RemoteInferenceClient(baseUrl: String, usbTransport: AoaUsbProbe?) {
    fun infer(bitmap: Bitmap, prompt: String) =
        InferenceControlClient.Result(false, 501, "Remote inference is not included")
}
