package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var manager: SplitInstallManager
    val moduleName = "dynamicfeature"
    val googleMapModule = "googlefeature"
    val interfaceModule = "interfacefeature"
    val hereMapActivity = "com.example.dynamicfeature.BasicMapActivity"
    val googleMapActivity = "com.example.googlefeature.GoogleMapsActivity"

    /** Listener used to handle changes in state for install requests. */
    @SuppressLint("SwitchIntDef")
    private val listener = SplitInstallStateUpdatedListener { state ->
        val multiInstall = state.moduleNames().size > 1
        state.moduleNames().forEach { name ->
            // Handle changes in state.
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    //  In order to see this, the application has to be uploaded to the Play Store.
                    displayLoadingState(state, "Downloading $name")
                }
                SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                    /*
                      This may occur when attempting to download a sufficiently large module.
                      In order to see this, the application has to be uploaded to the Play Store.
                      Then features can be requested until the confirmation path is triggered.
                     */
                    startIntentSender(state.resolutionIntent()?.intentSender, null, 0, 0, 0)
                }
                SplitInstallSessionStatus.INSTALLED -> {
                    launchActivity(hereMapActivity)
                }

                SplitInstallSessionStatus.INSTALLING -> displayLoadingState(state, "Installing $name")
                SplitInstallSessionStatus.FAILED -> {
                    toastAndLog("Error: ${state.errorCode()} for module ${state.moduleNames()}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = SplitInstallManagerFactory.create(this)

        bt_here_map.setOnClickListener {

            if (manager.installedModules.contains(moduleName)) {
                toastAndLog("${moduleName} Already installed")
                launchActivity(hereMapActivity)

            } else {
                toastAndLog("Starting install for $moduleName")

                val request = SplitInstallRequest.newBuilder()
                    .addModule(moduleName)
                    .build()

                manager.startInstall(request)
                    .addOnCompleteListener {toastAndLog("Module $moduleName installed") }
                    .addOnSuccessListener {toastAndLog("Loading $moduleName") }
                    .addOnFailureListener { toastAndLog("Error Loading $moduleName") }
            }
        }

        bt_google_map.setOnClickListener {

            if (manager.installedModules.contains(googleMapModule)) {
                toastAndLog("${googleMapModule} Already installed")
                launchActivity(googleMapActivity)

            } else {
                toastAndLog("Starting install for $googleMapModule")

                val request = SplitInstallRequest.newBuilder()
                    .addModule(googleMapModule)
                    .build()

                manager.startInstall(request)
                    .addOnCompleteListener { toastAndLog("Module $googleMapModule installed") }
                    .addOnSuccessListener { toastAndLog("Loading $googleMapModule") }
                    .addOnFailureListener { toastAndLog("Error Loading $googleMapModule") }
            }
        }

        bt_interface_object.setOnClickListener {

            if (manager.installedModules.contains(interfaceModule)) {
                toastAndLog("${interfaceModule} Already installed")

                val testInterface =
                    Class.forName("com.example.interfacefeature.TestInterfaceImpl").newInstance() as TestInterface
                testInterface.callMe(this)

            } else {
                toastAndLog("Starting install for $interfaceModule")

                val request = SplitInstallRequest.newBuilder()
                    .addModule(interfaceModule)
                    .build()

                manager.startInstall(request)
                    .addOnCompleteListener { toastAndLog("Module $interfaceModule installed") }
                    .addOnSuccessListener { toastAndLog("Loading $interfaceModule") }
                    .addOnFailureListener { toastAndLog("Error Loading $interfaceModule") }
            }
        }

    }

    override fun onResume() {
        // Listener can be registered even without directly triggering a download.
        manager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        // Make sure to dispose of the listener once it's no longer needed.
        manager.unregisterListener(listener)
        super.onPause()
    }

    fun MainActivity.toastAndLog(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        Log.d("toastAndLog :", text)
    }

    /** Launch an activity by its class name. */
    private fun launchActivity(className: String) {
        Intent().setClassName(packageName, className)
            .also {
                startActivity(it)
            }
    }

    private fun displayLoadingState(state: SplitInstallSessionState, message: String) {
        tv_status.setText("$message \n\n BytesToDownload : ${state.totalBytesToDownload().toInt()} :BytesDownloaded : ${state.bytesDownloaded().toInt()}")
    }
}
