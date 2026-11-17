package com.example.contar.wearable

import android.util.Log
import com.example.contar.data.DosisItem
import com.example.contar.data.DosisRepository
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONArray

/**
 * Servicio en el smartwatch que escucha actualizaciones de datos desde el teléfono
 * mediante la Google Play Services Wearable Data Layer API.
 */
class WatchWearableListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (PATH_MEDICATION_LIST == dataItem.uri.path) {
                    val dataMapItem = DataMapItem.fromDataItem(dataItem)
                    val jsonStr = dataMapItem.dataMap.getString(KEY_MEDICATIONS_JSON)
                    if (!jsonStr.isNullOrEmpty()) {
                        updateWatchRepository(jsonStr)
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Mensaje recibido en smartwatch: ${messageEvent.path}")
    }

    private fun updateWatchRepository(jsonStr: String) {
        try {
            val repository = DosisRepository(applicationContext)
            val jsonArray = JSONArray(jsonStr)
            val newList = mutableListOf<DosisItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", i.toString())
                val name = obj.optString("name", "Medicamento")
                val dosage = obj.optString("dosage", "1 dosis")
                val scheduleTimes = obj.optJSONArray("scheduleTimes")
                val timeStr = if ((scheduleTimes != null) && (scheduleTimes.length() > 0)) {
                    scheduleTimes.getString(0)
                } else {
                    "08:00"
                }

                newList.add(
                    DosisItem(
                        id = id,
                        nombre = name,
                        dosis = dosage,
                        hora = timeStr,
                        tomada = false,
                    ),
                )
            }
            if (newList.isNotEmpty()) {
                repository.saveDosisList(newList)
                Log.d(TAG, "Dosis actualizadas desde el teléfono: ${newList.size} ítems")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar datos recibidos del teléfono", e)
        }
    }

    companion object {
        private const val TAG = "WatchWearableListener"
        private const val PATH_MEDICATION_LIST = "/medication/list"
        private const val KEY_MEDICATIONS_JSON = "medications_json"
    }
}
