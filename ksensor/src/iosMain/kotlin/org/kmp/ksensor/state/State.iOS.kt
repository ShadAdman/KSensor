package org.kmp.ksensor.state

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Network.nw_path_monitor_create
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationProtectedDataDidBecomeAvailable
import platform.UIKit.UIApplicationProtectedDataWillBecomeUnavailable
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.darwin.NSObject
import org.kmp.ksensor.state.StateUpdate.Data
import org.kmp.ksensor.state.StateUpdate.Error

actual fun createController(): StateController = IOSStateHandler()
internal class IOSStateHandler : StateController {
    private var foregroundObserver: NSObject? = null
    private var backgroundObserver: NSObject? = null
    private var lockObserver: NSObject? = null
    private var unlockObserver: NSObject? = null
    private val monitor = nw_path_monitor_create()

    private lateinit var locationProviderReceiver: LocationProviderReceiver
    private val connectivityMonitor = ConnectivityMonitor
    private val volumeReceiver = VolumeReceiver()
    private val batteryStateReceiver = BatteryStateReceiver()
    private var bleConnectionReceiver: BleConnectionReceiver? = null
    private var bleDiscoversReceiver: BleDiscoversReceiver? = null
    private var localeReceiver: LocaleReceiver? = null
    private var screenStateReceiver: ScreenStateReceiver? = null
    override fun addObserver(types: List<StateType>): Flow<StateUpdate> = callbackFlow {
        types.forEach { stateType ->
            when (stateType) {
                StateType.SCREEN -> observeScreenState { trySend(it).isSuccess }
                StateType.APP_VISIBILITY -> observerAppVisibility { trySend(it).isSuccess }
                StateType.CONNECTIVITY, StateType.ACTIVE_NETWORK -> observeConnectivity { trySend(it).isSuccess }
                StateType.LOCATION -> observeLocation { trySend(it).isSuccess }
                StateType.VOLUME -> observeVolume { trySend(it).isSuccess }
                StateType.LOCALE -> observeLocale { trySend(it).isSuccess }
                StateType.BATTERY -> observeBattery { trySend(it) }
                StateType.LOCK -> observeLockState { trySend(it).isSuccess }
                StateType.BLE_CONNECTIONS -> observeBleConnection { trySend(it).isSuccess }
                StateType.BLE_DISCOVERS -> observeBleDiscovers { trySend(it).isSuccess }
            }.also {
                println("Observer added for $stateType on iOS")
            }
        }
        awaitClose { removeObserver(types) }
    }

    override fun removeObserver(types: List<StateType>) {
        types.forEach { stateType ->
            when (stateType) {
                StateType.SCREEN -> screenStateReceiver?.unregister()
                StateType.APP_VISIBILITY -> {
                    foregroundObserver?.let {
                        NSNotificationCenter.defaultCenter.removeObserver(it)
                    }
                    backgroundObserver?.let {
                        NSNotificationCenter.defaultCenter.removeObserver(it)
                    }
                }

                StateType.CONNECTIVITY, StateType.ACTIVE_NETWORK -> connectivityMonitor.unregister(
                    monitor
                )

                StateType.LOCATION -> locationProviderReceiver.dispose()
                StateType.VOLUME -> volumeReceiver.removeObserver()
                StateType.LOCALE -> localeReceiver?.removeObserver()
                StateType.BATTERY -> batteryStateReceiver.removeObserver()
                StateType.LOCK -> {
                    lockObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                    unlockObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                }

                StateType.BLE_CONNECTIONS -> {
                    bleConnectionReceiver?.unregister()
                    bleConnectionReceiver = null
                }

                StateType.BLE_DISCOVERS -> {
                    bleDiscoversReceiver?.unregister()
                    bleDiscoversReceiver = null
                }
            }.also {
                println("Observer removed for $stateType on iOS")
            }
        }
    }

    private fun observeBattery(onData: (StateUpdate) -> Unit) {
        batteryStateReceiver.registerObserver(onData)
    }

    private fun observeScreenState(onData: (StateUpdate) -> Unit) {
        val receiver = ScreenStateReceiver { isScreenOn ->
            onData(
                Data(
                    type = StateType.SCREEN,
                    data = StateData.ScreenStatus(isScreenOn),
                    platformType = PlatformType.iOS
                )
            )
        }
        receiver.register()
        screenStateReceiver = receiver
    }

    private fun observeLocale(onData: (StateUpdate) -> Unit) {
        val receiver = LocaleReceiver { localeInfo ->
            onData(
                Data(
                    type = StateType.LOCALE,
                    data = localeInfo,
                    platformType = PlatformType.iOS
                )
            )
        }

        onData(
            Data(
                type = StateType.LOCALE,
                data = receiver.getCurrentLocale(),
                platformType = PlatformType.iOS
            )
        )

        receiver.registerObserver()
        localeReceiver = receiver
    }

    private fun observeLocation(onData: (StateUpdate) -> Boolean) {
        locationProviderReceiver = LocationProviderReceiver {
            onData(
                Data(
                    type = StateType.LOCATION, data = StateData.LocationStatus(it),
                    PlatformType.iOS
                )
            )
        }
    }

    private fun observeVolume(onData: (StateUpdate) -> Unit) {
        onData(
            Data(
                type = StateType.VOLUME,
                data = StateData.VolumeStatus(volumeReceiver.getCurrentVolume()),
                platformType = PlatformType.iOS
            )
        )

        volumeReceiver.registerObserver {
            Data(
                type = StateType.VOLUME,
                data = StateData.VolumeStatus(it),
                platformType = PlatformType.iOS
            )
        }
    }

    private fun observeConnectivity(onData: (StateUpdate) -> Boolean) {
        connectivityMonitor.register(monitor, isConnected = {
            onData(
                Data(
                    type = StateType.CONNECTIVITY,
                    data = StateData.ConnectivityStatus(isConnected = it),
                    platformType = PlatformType.iOS
                )
            )
        }, currentActiveNetwork = {
            onData(
                Data(
                    type = StateType.ACTIVE_NETWORK,
                    data = StateData.CurrentActiveNetwork(it),
                    platformType = PlatformType.iOS
                )
            )
        })
    }

    private fun observerAppVisibility(onData: (StateUpdate) -> Boolean) {
        onData(
            Data(
                type = StateType.APP_VISIBILITY, StateData.AppVisibilityStatus(
                    false
                ),
                PlatformType.iOS
            )
        )

        foregroundObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue()
        ) {
            onData(
                StateUpdate.Data(
                    type = StateType.APP_VISIBILITY, StateData.AppVisibilityStatus(
                        true
                    ),
                    PlatformType.iOS
                )
            )
        } as NSObject?

        backgroundObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue()
        ) {
            onData(
                Data(
                    type = StateType.APP_VISIBILITY, StateData.AppVisibilityStatus(
                        false
                    ),
                    PlatformType.iOS
                )
            )
        } as NSObject?
    }

    /**
     * on iOS, the most reliable way to detect when a device is locked or unlocked
     * is by observing the "protected data" availability.
     */
    private fun observeLockState(onData: (StateUpdate) -> Unit) {
        onData(
            Data(
                type = StateType.LOCK,
                data = StateData.LockStatus(!UIApplication.sharedApplication.isProtectedDataAvailable()),
                platformType = PlatformType.iOS
            )
        )

        lockObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationProtectedDataWillBecomeUnavailable,
            `object` = null,
            queue = NSOperationQueue.mainQueue()
        ) {
            onData(
                Data(
                    type = StateType.LOCK,
                    data = StateData.LockStatus(true),
                    platformType = PlatformType.iOS
                )
            )
        } as NSObject?

        unlockObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationProtectedDataDidBecomeAvailable,
            `object` = null,
            queue = NSOperationQueue.mainQueue()
        ) {
            onData(
                Data(
                    type = StateType.LOCK,
                    data = StateData.LockStatus(false),
                    platformType = PlatformType.iOS
                )
            )
        } as NSObject?
    }

    private fun observeBleConnection(onData: (StateUpdate) -> Boolean) {
        val receiver = BleConnectionReceiver { onData(it) }
        receiver.register()
        bleConnectionReceiver = receiver
    }

    private fun observeBleDiscovers(onData: (StateUpdate) -> Boolean) {
        val receiver = BleDiscoversReceiver { onData(it) }
        receiver.register()
        bleDiscoversReceiver = receiver
    }
}