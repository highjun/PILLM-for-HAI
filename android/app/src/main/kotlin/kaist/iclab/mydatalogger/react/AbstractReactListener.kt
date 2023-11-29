package kaist.iclab.mydatalogger.react

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule

abstract class AbstractReactListener(
    reactApplicationContext: ReactApplicationContext
): ReactContextBaseJavaModule(reactApplicationContext) {

    private var listenerCount = 0

    fun listenerExist():Boolean{
        return listenerCount > 0
    }

    @ReactMethod
    open fun addListener(eventName: String) {
        if(listenerCount == 0){
            listenerCount += 1
        }
    }

    @ReactMethod
    open fun removeListeners(count:Int){
        listenerCount = if(listenerCount< count) 0 else listenerCount-count
    }

    fun sendEvent(reactContext: ReactContext, eventName: String, params: String) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

}