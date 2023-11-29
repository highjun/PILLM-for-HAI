import React, { useEffect } from 'react';
import {
  Button,
  Keyboard,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  StyleSheet,
  SafeAreaView,
  NativeModules,
  NativeEventEmitter
} from 'react-native';


const { OpenAIModule } = NativeModules;
const openAIEventEmitter = new NativeEventEmitter(OpenAIModule);
const openAIEventName = "OpenAIModule"
const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  inputContainer: {
    flexDirection: 'row',
    paddingHorizontal: 10,
    backgroundColor: '#E6E0E9', // Background color for the input area
    borderTopWidth: 1,
    borderTopColor: '#6750A4', // Border color to match your design
  },
  input: {
    flex: 1, // Take up all available space except for button
    borderWidth: 1,
    borderColor: '#E6E0E9', // Border color for the input
    padding: 10,
    backgroundColor: '#E6E0E9', // White background for the input
  },
  button: {
    position: 'absolute', // Position the button absolutely to overlap the input
    right: 10, // Position the button to the right
    top: 10, // Center it vertically
    width: 0,
    height: 0,
    borderStyle: 'solid',
    borderTopWidth: 15, 
    borderBottomWidth: 15,
    borderLeftWidth: 30,
    borderTopColor: 'transparent',
    borderBottomColor: 'transparent',
    borderLeftColor: '#6750A4', // Replace with the desired triangle color
  },
});

interface Message {
  role: "user" | "system" | "assistant" | "tool",
  content: string
}

interface Response {
  status: "message" | "response" | "responseEnd",
  content: Message[] | string
}

const QuestionView: React.FC = () => {

  const [messages, setMessages] = React.useState<Message[]>([]);
  const [inputValue, setInputValue] = React.useState('');
  const [outputValue, setOutputValue] = React.useState('');


  const sendMessage = () => {
    console.log("sendMessage");
    if (inputValue.trim() !== '') {
      const newMessage: Message = { role: "user", content: inputValue };
      const updatedMessages = [...messages, newMessage]
      OpenAIModule.request(inputValue, );
      setMessages(updatedMessages);
      setInputValue('');
      Keyboard.dismiss();

    }
  };

  useEffect(() => {
    const eventListener = openAIEventEmitter.addListener(openAIEventName, (event: string) => {
      const parsedEvent = JSON.parse(event)
      if (parsedEvent.status === "message") {
        setMessages(JSON.parse(parsedEvent.content))
      } else if (parsedEvent.status === "response") {
        setOutputValue(parsedEvent.content)
      } else if (parsedEvent.status === "responseEnd") {
        setOutputValue("")
      }

    });
    OpenAIModule.initMessageEmitter();

    return () => {
      eventListener?.remove();
    }
  }, [])

  return (

    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'android' ? 'padding' : 'height'} 
        keyboardVerticalOffset={-200}
        style={styles.container}
      >
        <ScrollView style={{ padding: 20 }}>
          {
            messages.map((message) => {
              const isUserMessage = message.role === 'user';
              const messageRoleLabel = isUserMessage ? "USER" : (message.role === 'system' ? "GPT" : message.role);

              return <Text key={message.content}>
                <Text style={{fontWeight: "bold"}}>{messageRoleLabel}</Text>
                <Text>{"\n"}</Text>
                <Text>{message.content}</Text>
                <Text>{"\n"}</Text>
              </Text>
            })
          }
          <Text>{`${outputValue}`}</Text>
        </ScrollView>
        <View style={styles.inputContainer}>
          <TextInput
            style={styles.input}
            value={inputValue}
            onChangeText={setInputValue}
            placeholder="Ask GPT..."
          />
          <TouchableOpacity onPress={sendMessage} style={styles.button}>

          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

export default QuestionView;