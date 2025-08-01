import 'react-native-get-random-values';
import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import LoginScreen from './screens/LoginScreen';
import HomeScreen from './screens/HomeScreen';
import { generateAndStoreEncryptionKey, getStoredKey } from './utils/cryptoHelper';

const Stack = createNativeStackNavigator();

export default function App() {

    useEffect(() => {
        const ensureEncryptionKey = async () => {
        try {
            await getStoredKey(); // if not found, throw
        } catch {
            console.log('Key not found — generating new one');
            await generateAndStoreEncryptionKey();
        }
        };
        ensureEncryptionKey();
    }, []);

    return (
        <NavigationContainer>
            <Stack.Navigator initialRouteName="Login">
                <Stack.Screen name="Login" component={LoginScreen} />
                <Stack.Screen name="Home" component={HomeScreen} />
            </Stack.Navigator>
        </NavigationContainer>
    );
}