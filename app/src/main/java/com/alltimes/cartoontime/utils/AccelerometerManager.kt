package com.alltimes.cartoontime.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alltimes.cartoontime.data.model.AccelerometerDataModel

class AccelerometerManager(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerSensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

    private val _accelerometerData = MutableLiveData<AccelerometerDataModel>()
    val accelerometerData: LiveData<AccelerometerDataModel> = _accelerometerData

    init {
        // Register sensor listener when AccelerometerManager is created
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                _accelerometerData.value = AccelerometerDataModel(x, y, z)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed for this example
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }
}
