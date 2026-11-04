package com.ksensor.plugins.states.bluetooth.connections

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.ksensor.core.model.BleDevice
import com.ksensor.core.model.StateData

internal class BleConnectionReceiver(
    private val context: Context,
    private val onData: (StateData.BleConnectionStatus) -> Unit
) : BroadcastReceiver() {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    private val aclConnectedDevices = mutableMapOf<String, BluetoothDevice>()
    private val proxies = mutableMapOf<Int, BluetoothProfile>()

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            synchronized(proxies) {
                proxies[profile] = proxy
            }
            emitCurrentState()
        }

        override fun onServiceDisconnected(profile: Int) {
            synchronized(proxies) {
                proxies.remove(profile)
            }
            emitCurrentState()
        }
    }

    private val monitoredProfiles = listOf(
        BluetoothProfile.A2DP,
        BluetoothProfile.HEADSET,
        4 // BluetoothProfile.HID_HOST
    )

    fun register() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(this, filter)
        }

        monitoredProfiles.forEach { profile ->
            try {
                bluetoothAdapter?.getProfileProxy(context, profileListener, profile)
            } catch (_: Exception) {
            }
        }

        emitCurrentState()
    }

    fun unregister() {
        try {
            context.unregisterReceiver(this)
        } catch (_: Exception) {
        }
        
        val proxiesToClose = synchronized(proxies) {
            val list = proxies.entries.toList()
            proxies.clear()
            list
        }

        proxiesToClose.forEach { (profile, proxy) ->
            try {
                bluetoothAdapter?.closeProfileProxy(profile, proxy)
            } catch (_: Exception) {
            }
        }

        synchronized(aclConnectedDevices) {
            aclConnectedDevices.clear()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                device?.let {
                    synchronized(aclConnectedDevices) {
                        aclConnectedDevices[it.address] = it
                    }
                }
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                device?.let {
                    synchronized(aclConnectedDevices) {
                        aclConnectedDevices.remove(it.address)
                    }
                }
            }
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_OFF) {
                    synchronized(aclConnectedDevices) {
                        aclConnectedDevices.clear()
                    }
                }
            }
        }
        emitCurrentState()
    }

    @SuppressLint("MissingPermission")
    fun emitCurrentState() {
        val connectedDevices = mutableSetOf<BluetoothDevice>()
        val audioDevices = mutableSetOf<String>()

        if (bluetoothAdapter?.isEnabled == true) {
            // 1. Check GATT and GATT_SERVER via BluetoothManager
            try {
                bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).forEach { connectedDevices.add(it) }
            } catch (_: Exception) {}
            try {
                bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER).forEach { connectedDevices.add(it) }
            } catch (_: Exception) {}

            // 2. Check all proxies
            val proxiesCopy = synchronized(proxies) { proxies.entries.toList() }
            proxiesCopy.forEach { (profile, proxy) ->
                try {
                    proxy.connectedDevices.forEach { device ->
                        connectedDevices.add(device)
                        if (profile == BluetoothProfile.A2DP || profile == BluetoothProfile.HEADSET) {
                            audioDevices.add(device.address)
                        }
                    }
                } catch (_: Exception) {}
            }

            // 3. Include devices tracked via ACL broadcasts
            val aclDevicesCopy = synchronized(aclConnectedDevices) { aclConnectedDevices.values.toList() }
            connectedDevices.addAll(aclDevicesCopy)
        } else {
            synchronized(aclConnectedDevices) {
                aclConnectedDevices.clear()
            }
        }

        val bleDevices = connectedDevices.map { device ->
            val name = try {
                device.name
            } catch (_: Exception) {
                null
            } ?: "Unknown Device"
            
            BleDevice(
                id = device.address,
                name = name,
                isAudio = audioDevices.contains(device.address)
            )
        }

        onData(StateData.BleConnectionStatus(bleDevices))
    }
}
