package com.alltimes.cartoontime.ui.view

// # Added Imports
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alltimes.cartoontime.data.model.AccelerometerDataModel
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.ui.screen.main.BootScreen
import com.alltimes.cartoontime.ui.screen.main.LoginScreen
import com.alltimes.cartoontime.ui.screen.main.MainScreen
import com.alltimes.cartoontime.ui.viewmodel.MainViewModel
import com.alltimes.cartoontime.utils.NavigationHelper
import com.alltimes.cartoontime.utils.PermissionsHelper

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor

    private lateinit var activity: ComponentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        viewModel = MainViewModel(this) // ViewModel 생성

        // SensorManager 초기화
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        activity = this

        // 권한 요청 부분을 PermissionsHelper로 처리
        if (!PermissionsHelper.hasAllPermissions(this)) {
            PermissionsHelper.requestPermissions(this)
        }

        setContent {
            navController = rememberNavController() // 전역 변수에 저장

            NavHost(navController as NavHostController, startDestination = "mainscreen") {
                composable("mainscreen") { MainScreen(viewModel = viewModel) }
            }
        }

        // ViewModel에서 Activity 전환 요청 처리
        viewModel.activityNavigationTo.observe(this) { navigationTo ->
            navigationTo?.activityType?.let { activityType ->
                NavigationHelper.navigate(this, activityType)
            }
        }

        // ViewModel에서 Screen 전환 요청 처리
        viewModel.screenNavigationTo.observe(this) { navigationTo ->
            navigationTo?.screenType?.let { screenType ->
                navigateToScreen(screenType)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 센서 데이터 업데이트 주기를 설정하여 센서 리스너 등록
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                // ViewModel 업데이트
                viewModel.updateAccelerometerData(AccelerometerDataModel(x, y, z))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed for this example
    }


    // 현재 상태에 따라 올바른 시작 목적지를 반환하는 함수
    private fun getStartDestination(password: String): String {
        // userPassword가 존재하면 메인 스크린 시작 없으면 부트 스크린 시작

        println("password: $password")

        if (password.isNullOrEmpty()) {
            return "bootscreen"
        }
        return "loginscreen"
    }

    // 스크린 전환을 처리하는 함수로 분리하여 처리
    private fun navigateToScreen(screenType: ScreenType) {
        val route = when (screenType) {
            ScreenType.MAIN -> "mainscreen"
            else -> return
        }
        navController.navigate(route)
    }
}