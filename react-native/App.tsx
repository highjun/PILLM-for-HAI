import React, { useEffect } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';
import { initMessageListener, sendMessage } from './openAI/repository';
import MessageComponent from './components/MessageComponent';
import Icon from 'react-native-vector-icons/MaterialIcons';


// App
const App: React.FC = () => {
  const [messages, setMessages] = React.useState<Message[]>([]);
  const [inputValue, setInputValue] = React.useState('');
  // const [outputValue, setOutputValue] = React.useState('');
  const isEmpty = (input_: string) => {
    return input_.trim() === ""
  }

  useEffect(() => {
    const eventListener = initMessageListener((event: string) => {
      const parsedEvent = JSON.parse(event);
      setMessages(parsedEvent);
    });
    return () => {
      eventListener?.remove();
    }
  }, []);

  // main view
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={{ fontSize: 20, fontWeight: "500", color: "white" }}>PILLM</Text>
      </View>
      <ScrollView >
        {messages.filter((message)=> (message.role !=="system" && !message.isToolCall)).map((message, idx) => <MessageComponent key={`message-${idx}`} message={message} />)}
      </ScrollView>
      <View style={styles.inputContainer}>
        <TextInput
          style={styles.input}
          value={inputValue}
          onChangeText={setInputValue}
          placeholder="Message PILLM..."
        />
        <TouchableOpacity disabled={isEmpty(inputValue)} onPress={() => { sendMessage(messages, inputValue); setInputValue("")}} style={[styles.button, isEmpty(inputValue) ? styles.empty : styles.activeButton]}>
          <Icon name={"arrow-upward"} size={24} color="white" />
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}


const styles = StyleSheet.create({
  header: {
    backgroundColor: "#777",
    paddingVertical: 18,
    paddingHorizontal: 15,
  },
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  inputContainer: {
    flexDirection: 'row',
    marginHorizontal: 10,
    marginVertical: 3,
    paddingHorizontal: 15,
    alignItems: 'center',
    gap: 10,
    borderWidth: 1,
    borderColor: '#aaa',
    borderRadius: 18,
    backgroundColor: "#fff",
    opacity: 0.8
  },
  input: {
    flex: 1,
    padding: 10,
  },
  button: {
    padding: 4,
    borderRadius: 8,
    backgroundColor: "#aaa"
  },
  activeButton: {
    backgroundColor: "#018374"
  },
  empty: {}
});

export default App;