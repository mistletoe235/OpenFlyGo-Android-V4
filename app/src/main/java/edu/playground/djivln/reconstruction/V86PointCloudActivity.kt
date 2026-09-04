package edu.playground.djivln.reconstruction

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.playground.djivln.mini2.R
import java.io.File
import java.util.concurrent.Executors

class V86PointCloudActivity : AppCompatActivity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var viewer: V86PointCloudView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private var candidatesVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(7, 11, 16); window.navigationBarColor = Color.rgb(7, 11, 16)
        viewer = V86PointCloudView(this)
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(7, 11, 16))
            addView(viewer, FrameLayout.LayoutParams(-1, -1))
        }
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8)); setBackgroundColor(0xdd111827.toInt())
        }
        status = TextView(this).apply {
            text = getString(R.string.point_cloud_loading); textSize = 11f
            setTextColor(Color.WHITE); maxLines = 2
        }
        bar.addView(status, LinearLayout.LayoutParams(0, -2, 1f))
        fun action(label: String, click: () -> Unit) = Button(this).apply {
            text = label; textSize = 9f; minWidth = 0; minHeight = 0
            setOnClickListener { click() }
            bar.addView(this, LinearLayout.LayoutParams(dp(70), dp(38)).apply { marginStart = dp(5) })
        }
        action(getString(R.string.point_cloud_fit)) { viewer.setPreset(V86PointCloudView.ViewPreset.ISOMETRIC) }
        action(getString(R.string.point_cloud_top)) { viewer.setPreset(V86PointCloudView.ViewPreset.TOP) }
        action(getString(R.string.point_cloud_front)) { viewer.setPreset(V86PointCloudView.ViewPreset.FRONT) }
        val risk = action(getString(R.string.point_cloud_risk_on)) {}
        risk.setOnClickListener {
            candidatesVisible = !candidatesVisible; viewer.setCandidatesVisible(candidatesVisible)
            risk.text = getString(if (candidatesVisible) R.string.point_cloud_risk_on else R.string.point_cloud_risk_off)
        }
        action(getString(R.string.action_close)) { finish() }
        root.addView(bar, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { isIndeterminate = true }
        root.addView(progress, FrameLayout.LayoutParams(-1, dp(4), Gravity.BOTTOM))
        setContentView(root)
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
        }
        load()
    }

    private fun load() {
        val ply = internalFile(intent.getStringExtra(EXTRA_PLY_PATH))
        val candidatesFile = internalFile(intent.getStringExtra(EXTRA_CANDIDATES_PATH), false)
        if (ply == null || !ply.isFile) {
            status.text = getString(R.string.point_cloud_missing); progress.visibility = View.GONE; return
        }
        worker.execute {
            val result = runCatching {
                val candidates = candidatesFile?.takeIf(File::isFile)?.readText()
                    ?.let(::decodeV86Candidates).orEmpty()
                V86PlyDecoder.decode(ply, candidates, 40_000) to candidates.size
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                progress.visibility = View.GONE
                result.onFailure { status.text = getString(R.string.point_cloud_parse_failed, it.message) }
                result.onSuccess { (cloud, count) ->
                    viewer.setPointCloud(cloud)
                    status.text = getString(R.string.point_cloud_status, ply.name,
                        formatBytes(ply.length()), cloud.size, count)
                }
            }
        }
    }

    private fun internalFile(path: String?, required: Boolean = true): File? {
        if (path.isNullOrBlank()) return null.also {
            if (required) status.text = getString(R.string.point_cloud_path_missing)
        }
        val file = File(path).canonicalFile
        val root = filesDir.canonicalFile
        return file.takeIf { it.path.startsWith(root.path + File.separator) }
    }
    override fun onDestroy() { worker.shutdownNow(); super.onDestroy() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_PLY_PATH = "v86_ply_path"
        const val EXTRA_CANDIDATES_PATH = "v86_candidates_path"
        private fun formatBytes(bytes: Long) = when {
            bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
