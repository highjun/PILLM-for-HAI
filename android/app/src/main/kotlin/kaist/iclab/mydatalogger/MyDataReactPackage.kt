package kaist.iclab.mydatalogger

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager
import kaist.iclab.mydatalogger.db.dao.MessageDao
import kaist.iclab.mydatalogger.db.dao.QueryDao
import kaist.iclab.mydatalogger.openai.OpenAIRepository
import kaist.iclab.mydatalogger.react.DataQueryModule
import kaist.iclab.mydatalogger.react.OpenAIModule

class MyDataReactPackage(
    private val queryDao: QueryDao,
    private val messageDao: MessageDao,
    private val openAIRepository: OpenAIRepository
):ReactPackage {
    override fun createNativeModules(reactApplicationContext: ReactApplicationContext): MutableList<NativeModule> {
        val modules = mutableListOf<NativeModule>()

        val dataQueryModule = DataQueryModule(
            reactApplicationContext,
            queryDao
        )
        modules.add(dataQueryModule)

        val openAIModule = OpenAIModule(
            reactApplicationContext,
            openAIRepository,
            messageDao,
            queryDao
        )
        modules.add(openAIModule)
        return modules
    }
    override fun createViewManagers(p0: ReactApplicationContext): MutableList<ViewManager<View, ReactShadowNode<*>>> = mutableListOf()
}