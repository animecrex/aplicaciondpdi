package com.dosis.app.wearable

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Constantes de la Data Layer para sincronización Wear OS */
object WearableConstants {
    const val PATH_MEDICATION_LIST = "/medication/list"
    const val PATH_MARK_TAKEN = "/medication/taken"
    const val PATH_SNOOZE = "/medication/snooze"
    const val PATH_REMINDER_FIRED = "/reminder/fired"

    const val KEY_MEDICATION_ID = "medication_id"
    const val KEY_SCHEDULED_TIME = "scheduled_time"
    const val KEY_MEDICATIONS_JSON = "medications_json"
    const val KEY_TIMESTAMP = "timestamp"
}

/** Interfaz para enviar datos de medicamentos hacia el reloj inteligente */
interface WearableDataManager {
    suspend fun syncMedicationsToWatch(medicationsJson: String): Boolean
    suspend fun sendReminderToWatch(medicationId: Int, name: String, dosage: String, scheduledTime: Long)
    fun isDevicePaired(): Boolean
    fun getPairedDeviceName(): String?
    fun getLastSyncTime(): Long?
    suspend fun pairFromQrCode(qrData: String, medicationsJson: String = ""): Result<String>
    suspend fun unpairDevice()
}

class WearableDataManagerImpl(private val context: Context) : WearableDataManager {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isDevicePaired(): Boolean {
        return prefs.getBoolean(KEY_IS_PAIRED, false)
    }

    override fun getPairedDeviceName(): String? {
        return prefs.getString(KEY_PAIRED_DEVICE_NAME, null)
    }

    override fun getLastSyncTime(): Long? {
        val time = prefs.getLong(KEY_LAST_SYNC_TIME, -1L)
        return if (time != -1L) time else null
    }

    override suspend fun pairFromQrCode(qrData: String, medicationsJson: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject(qrData)
                val app = json.optString("app", "")
                val deviceName = json.optString("deviceName", "Smartwatch Dosis")
                val nodeId = json.optString("nodeId", "watch_wearos")

                if (app != "Dosis" && !qrData.contains("Dosis")) {
                    return@withContext Result.failure(Exception("Código QR no válido para la app Dosis"))
                }

                val syncTime = System.currentTimeMillis()
                prefs.edit()
                    .putBoolean(KEY_IS_PAIRED, true)
                    .putString(KEY_PAIRED_DEVICE_NAME, deviceName)
                    .putString(KEY_PAIRED_NODE_ID, nodeId)
                    .putLong(KEY_LAST_SYNC_TIME, syncTime)
                    .apply()

                if (medicationsJson.isNotEmpty()) {
                    syncMedicationsToWatch(medicationsJson)
                }

                Log.d(TAG, "Smartwatch $deviceName vinculado exitosamente mediante QR")
                Result.success(deviceName)
            } catch (e: Exception) {
                Log.e(TAG, "Error al vincular con el código QR", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun unpairDevice() {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .remove(KEY_IS_PAIRED)
                .remove(KEY_PAIRED_DEVICE_NAME)
                .remove(KEY_PAIRED_NODE_ID)
                .remove(KEY_LAST_SYNC_TIME)
                .apply()
            Log.d(TAG, "Smartwatch desvinculado")
        }
    }

    override suspend fun syncMedicationsToWatch(medicationsJson: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = PutDataMapRequest.create(WearableConstants.PATH_MEDICATION_LIST).apply {
                    dataMap.putString(WearableConstants.KEY_MEDICATIONS_JSON, medicationsJson)
                    dataMap.putLong(WearableConstants.KEY_TIMESTAMP, System.currentTimeMillis())
                }.asPutDataRequest().setUrgent()

                Tasks.await(Wearable.getDataClient(context).putDataItem(request))
                val syncTime = System.currentTimeMillis()
                prefs.edit().putLong(KEY_LAST_SYNC_TIME, syncTime).apply()
                Log.d(TAG, "Lista de medicamentos enviada exitosamente al smartwatch")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar medicamentos con el smartwatch", e)
                false
            }
        }
    }

    override suspend fun sendReminderToWatch(
        medicationId: Int,
        name: String,
        dosage: String,
        scheduledTime: Long
    ) {
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put(WearableConstants.KEY_MEDICATION_ID, medicationId)
                    put("name", name)
                    put("dosage", dosage)
                    put(WearableConstants.KEY_SCHEDULED_TIME, scheduledTime)
                }.toString().toByteArray(Charsets.UTF_8)

                val nodes = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
                for (node in nodes) {
                    Wearable.getMessageClient(context).sendMessage(
                        node.id,
                        WearableConstants.PATH_REMINDER_FIRED,
                        payload
                    )
                }
                Log.d(TAG, "Recordatorio enviado a ${nodes.size} relojes conectados")
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar recordatorio al smartwatch", e)
            }
        }
    }

    companion object {
        private const val TAG = "WearableDataManager"
        private const val PREFS_NAME = "dosis_wearable_prefs"
        private const val KEY_IS_PAIRED = "is_paired"
        private const val KEY_PAIRED_DEVICE_NAME = "paired_device_name"
        private const val KEY_PAIRED_NODE_ID = "paired_node_id"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }
}

data class WearableConnectionState(
    val isConnected: Boolean = false,
    val deviceName: String? = null,
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false
)
