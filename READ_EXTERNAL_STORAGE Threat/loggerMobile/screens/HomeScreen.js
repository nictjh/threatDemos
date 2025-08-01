import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';

export default function HomeScreen() {
    const navigation = useNavigation();
    const [count, setCount] = useState(0);

    const secretPress = () => {
        navigation.navigate('SecretScanner');
    };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>🍪 Cookie Clicker</Text>
      <TouchableOpacity onPress={() => setCount(count + 1)} style={styles.cookieButton}>
        <Text style={styles.cookieText}>Tap to Bake</Text>
      </TouchableOpacity>
      <Text style={styles.count}>Cookies: {count}</Text>

      {/* Secret access */}
      <TouchableOpacity onLongPress={secretPress} style={styles.hiddenZone}>
        <Text style={styles.hiddenText}>IM SUPPOSED TO BE HIDDEN</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
    container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
    title: { fontSize: 26, marginBottom: 20 },
    cookieButton: {
        backgroundColor: '#deb887',
        padding: 20,
        borderRadius: 100,
        marginBottom: 10,
    },
    cookieText: { fontSize: 18, color: '#fff' },
    count: { fontSize: 16 },
    hiddenZone: {
        position: 'absolute',
        bottom: 20,
        right: 20,
        width: 40,
        height: 40,
        backgroundColor: 'transparent',
    },
    hiddenText: { fontSize: 1 },
});