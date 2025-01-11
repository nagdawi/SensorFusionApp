package com.example.sensorfusionapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private TextView textViewPitch, textViewRoll;
    private float[] accelData = new float[3];
    private float[] gyroData = new float[3];

    private float pitch = 0, roll = 0;
    private long lastTimestamp = 0;
    private final float alpha = 0.98f; // Complementary filter coefficient

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewPitch = findViewById(R.id.textViewPitch);
        textViewRoll = findViewById(R.id.textViewRoll);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelData, 0, event.values.length);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, gyroData, 0, event.values.length);
            if (lastTimestamp != 0) {
                float timeInterval = (event.timestamp - lastTimestamp) * 1e-9f;
                pitch += gyroData[1] * timeInterval;
                roll += gyroData[0] * timeInterval;
                float accelPitch = (float) Math.atan2(accelData[1], Math.sqrt(accelData[0] * accelData[0] + accelData[2] * accelData[2]));
                float accelRoll = (float) Math.atan2(-accelData[0], accelData[2]);
                pitch = alpha * pitch + (1 - alpha) * accelPitch;
                roll = alpha * roll + (1 - alpha) * accelRoll;

                // Update the UI
                textViewPitch.setText(String.format("Pitch: %.0f deg",  Math.toDegrees(pitch)));
                textViewRoll.setText(String.format("Roll: %.0f deg", Math.toDegrees(roll)));
            }
            lastTimestamp = event.timestamp;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}
