import React, { useEffect, useState } from 'react';
import { Text, View, StyleSheet, DeviceEventEmitter } from 'react-native';

export default function App() {
  const [status, setStatus] = useState('🕐 Waiting for cleanup intent...');

  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      'CleanupTriggered',
      (payload) => {
        if (payload === 'LEGIT_CLEANUP') {
          setStatus('🧹 Legit cleanup started!');
        } else if (payload === 'DELETE_ALL_FILES') {
          setStatus('⚠️ Dangerous cleanup triggered!');
        } else {
          setStatus(`📨 Received unknown command: ${payload}`);
        }
      }
    );

    return () => subscription.remove();
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Victim App</Text>
      <Text style={styles.status}>{status}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 24,
  },
  status: {
    fontSize: 20,
    color: '#333',
  },
});