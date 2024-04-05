import React, { useState } from "react";
import {
  AppRegistry,
  StyleSheet,
  View,
  Button,
  BackHandler,
  TextInput,
  NativeModules
} from "react-native";

const App = ({ id, title: initialTitle, text: initialText }) => {
  const [title, setTitle] = useState(initialTitle);
  const [text, setText] = useState(initialText);

  return (
    <View style={styles.container}>
      <View style={styles.topRow}>
      <TextInput style={styles.title} value={title} onChangeText={setTitle}/>

        <Button
          style={styles.backButton}
          title="X"
          onPress={BackHandler.exitApp}
        />
      </View>

      <TextInput style={styles.title} value={text} onChangeText={setText}/>

      <Button title="Save" onPress={() => {
        NativeModules.UpdateNoteModule.updateNote(id, title, text);
      }}/>
    </View>
  );
};
const styles = StyleSheet.create({
  container: {
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    padding: 12,
  },
  topRow: {
    display: "flex",
    flexDirection: "row",
    justifyContent: "space-between",
  },
  title: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#111",
  },
  backButton: {
    display: "flex",
    flexDirection: "column",
    justifyContent: "center"
  },
  text: {
    marginTop: 20,
  },
});

AppRegistry.registerComponent(
  'MyReactNativeApp',
  () => App,
);