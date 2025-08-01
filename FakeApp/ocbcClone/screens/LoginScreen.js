import React, { useState } from 'react';
import { Alert, View, TextInput, Button, StyleSheet, Text, TouchableOpacity } from 'react-native';
import { MaterialCommunityIcons, Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { supabase } from '../lib/supabase';

import { NativeModules } from 'react-native';

export default function LoginScreen() {
  const [accessCode, setAccessCode] = useState('');
  const [pin, setPin] = useState('');
  const navigation = useNavigation();

  const isLoginDisabled = !accessCode || !pin;

//   const handleLogin = async () => {
//     const { data, error } = await supabase
//       .from('yeet')
//       .insert([
//         {
//           accessCode: accessCode,
//           pin: pin,
//         }
//       ]);

//     if (error) {
//       console.error('Insert error:', error);
//     } else {
//       console.log('Data inserted successfully:', data);
//       navigation.navigate('Home');
//     }
//   };

    const handleLogin = async () => {
        try {
            const result = await NativeModules.LoginBridge.submit(accessCode, pin);
            console.log("✅ Native result:", result);
            navigation.navigate('Home');
        } catch (e) {
            console.error("Login error:", e);

            const message = typeof e === 'string' ? e : e?.message || e?.toString();

            if (message.toLowerCase().includes("attacker table")) {
                Alert.alert("⚠️ Breach!", "Your data was stolen. ❌ :<<");
            } else {
                Alert.alert("Login failed", message || "Unknown error");
            }
        }
    };


  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.backButton} onPress={() => navigation.goBack()}>
        <Ionicons name="chevron-back" size={24} color="#333" />
      </TouchableOpacity>
      <TextInput
        style={styles.input}
        placeholder="Access Code"
        value={accessCode}
        onChangeText={setAccessCode}
      />
      <TextInput
        style={styles.input}
        placeholder="PIN"
        secureTextEntry
        value={pin}
        onChangeText={setPin}
      />
      <TouchableOpacity style={styles.faceIcon}>
        <MaterialCommunityIcons name="face-recognition" size={28} color="#888" />
      </TouchableOpacity>
      <Button title="Log in" onPress={handleLogin} disabled={isLoginDisabled} />
      <TouchableOpacity>
        <Text style={styles.forgot}>Forgot your Access Code/PIN?</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingTop: 120, // Increased from 80 to move login form down
    paddingHorizontal: 20,
    backgroundColor: '#fff',
    flex: 1,
  },
  backButton: {
    position: 'absolute',
    top: 50,
    left: 10, // Moved from 20 to 10 to shift more to the left
    zIndex: 1,
    padding: 5,
  },
  input: {
    borderBottomWidth: 1,
    borderBottomColor: '#ccc',
    fontSize: 16,
    paddingVertical: 12,
    marginBottom: 20,
  },
  faceIcon: {
    position: 'absolute',
    top: 125, // Adjusted from 85 to account for moved login form
    right: 30,
  },
  forgot: {
    color: '#007AFF',
    marginTop: 20,
    textAlign: 'center',
  },
});
