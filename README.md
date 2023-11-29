### PILLM

Our app is implemented by using React Native (for UI) and Android Native (for business logic include API hanlding and utilziing DB).

#### Android Native

* /android/app/src/main/kotlin/kaist/iclab/mydatalogger/ is the main directory for Android Native.
    * /db: Subpackage for handling DB including DAO(Data Access Object), Data entities, and RoomDB builder 
    * /openai: Subpackage for OpenAI API handling
    * /react: 
    * Others: 
        * MyDataReactPackage.kt: Package for communicate with ReactPackage
        * MyDataApplication.kt: The class that alive for whole AppLifecycle
        * MainActivity.kt: The main activity for application
        * KoinModules.kt: Dependency Injection using Koin.

#### React Native

* /react-native/ is the main directory for React Native
    * /openAI: Subpackage for handling Chat/ 
    * /components: Subpackage for React component including MessageComponent, Graphics Visualization 

* Note: OpenAI API Token should be incldued on the below file for proper use: android\app\src\main\kotlin\kaist\iclab\mydatalogger\openai\OpenAIRepository.kt