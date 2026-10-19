package mda.session

import io.appium.java_client.android.AndroidDriver
import mda.config.Config

object EmulatorLifecycle {
    fun muteSystemSounds(driver: AndroidDriver) {
        AudioGuard.enforce(driver)
    }

    fun applyFastHarnessTweaks(serial: String?) {
        if (!Config.disableEmulatorAnimations) {
            return
        }
        for (setting in listOf(
            "settings put global window_animation_scale 0",
            "settings put global transition_animation_scale 0",
            "settings put global animator_duration_scale 0",
        )) {
            runCatching { Adb.shellCaptureOn(serial, setting) }
        }
    }

    /**
     * When [Config.muteAndroidSystem] is true, guest audio must stay off for the whole test run.
     * Call before/after each session and after emulator boot (see scripts/apply-mute.cmd).
     */
    fun muteDeviceAudio(serial: String?) {
        if (!Config.muteAndroidSystem) {
            return
        }
        if (serial.isNullOrBlank()) {
            return
        }
        for (stream in 0..10) {
            runCatching { Adb.shellCaptureOn(serial, "cmd media volume --stream $stream --set 0") }
        }
        runCatching { Adb.shellCaptureOn(serial, "cmd media volume --set 0") }
        val settings = listOf(
            "settings put system sound_effects_enabled 0",
            "settings put system haptic_feedback_enabled 0",
            "settings put system vibrate_when_ringing 0",
            "settings put system notification_sound ''",
            "settings put system ringtone ''",
            "settings put system alarm_alert ''",
            "settings put secure notification_sound ''",
            "settings put global zen_mode 3",
            "settings put global apply_ramping_ringer 0",
        )
        for (command in settings) {
            runCatching { Adb.shellCaptureOn(serial, command) }
        }
    }

    /** Best practice: stop emulators this run likely started (empty [Config.udid], Appium `avd` capability). */
    fun killSuiteEmulators() {
        if (!Config.killAfterTestrun) {
            return
        }
        if (Config.udid.isNotBlank()) {
            return
        }
        for (serial in Adb.emulatorSerials()) {
            runCatching { Adb.emuKill(serial) }
        }
    }
}
