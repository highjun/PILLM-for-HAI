import { EmitterSubscription, NativeEventEmitter, NativeModules } from "react-native";

const { OpenAIModule } = NativeModules;
const openAIEventEmitter = new NativeEventEmitter(OpenAIModule);
const openAIEventName = "OpenAIModule"

const initMessageListener = (callback: (event:string)=> void):EmitterSubscription => {
    OpenAIModule.initMessageEmitter();
    const eventListener = openAIEventEmitter.addListener(openAIEventName, callback)
    return eventListener
}


const sendMessage = (prevMessages: Message[], message: string) => {
    console.log("sendMessage");
    if (message.trim() !== '') {
        const newMessage: Message = { role: "user", content: message };
        const updatedMessages = [...prevMessages, newMessage]
        OpenAIModule.request(message);  
        return updatedMessages
    }
}

export {initMessageListener, sendMessage};