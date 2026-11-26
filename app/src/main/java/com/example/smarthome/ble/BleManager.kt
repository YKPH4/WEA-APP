package com.example.smarthome.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter

    private var bluetoothGatt: BluetoothGatt? = null

    // UUIDs as defined in ESP32
    private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHAR_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    suspend fun sendCommand(command: String): Result<String> = suspendCoroutine { continuation ->
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            continuation.resume(Result.failure(Exception("Bluetooth está desactivado")))
            return@suspendCoroutine
        }

        val device = bluetoothAdapter.bondedDevices?.find { it.name == "SMARTHOME_LOCK" }
        if (device == null) {
            continuation.resume(Result.failure(Exception("No se encontró la cerradura emparejada. Ve a los ajustes de Bluetooth y empareja 'SMARTHOME_LOCK' primero.")))
            return@suspendCoroutine
        }

        var isResumed = false
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (!isResumed) {
                isResumed = true
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                continuation.resume(Result.failure(Exception("Tiempo de espera agotado al conectar a la cerradura")))
            }
        }

        timeoutHandler.postDelayed(timeoutRunnable, 10000)

        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (!isResumed) {
                        isResumed = true
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        continuation.resume(Result.failure(Exception("Desconectado de la cerradura")))
                    }
                    gatt.close()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(SERVICE_UUID)
                    val characteristic = service?.getCharacteristic(CHAR_UUID)

                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true)

                        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)

                            Handler(Looper.getMainLooper()).postDelayed({
                                characteristic.value = command.toByteArray()
                                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                                gatt.writeCharacteristic(characteristic)
                            }, 500)
                        } else {
                            characteristic.value = command.toByteArray()
                            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            gatt.writeCharacteristic(characteristic)
                        }
                    } else {
                        if (!isResumed) {
                            isResumed = true
                            timeoutHandler.removeCallbacks(timeoutRunnable)
                            continuation.resume(Result.failure(Exception("Servicio BLE no compatible en el dispositivo")))
                            gatt.disconnect()
                        }
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                val response = String(characteristic.value ?: ByteArray(0))
                if (!isResumed) {
                    isResumed = true
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    if (response == "OK" || response == "OK_OPEN" || response == "OK_CLOSE") {
                        continuation.resume(Result.success(response))
                    } else {
                        continuation.resume(Result.failure(Exception("La cerradura rechazó el cambio: $response")))
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        gatt.disconnect()
                    }, 500)
                }
            }
        })
    }
}
