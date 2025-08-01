import React, { useEffect, useState } from 'react';
import {
    Alert,
    View,
    Text,
    TextInput,
    Button,
    StyleSheet,
    ScrollView,
    Switch,
    PermissionsAndroid,
    Platform,
} from 'react-native';
import RNFS from 'react-native-fs';
import { useNavigation } from '@react-navigation/native';
import { encryptText, decryptText, getStoredKey, evpKDF, extractSaltAndCiphertext } from '../utils/cryptoHelper';
import CryptoJS from 'crypto-js';


export default function LoginScreen() {
    const navigation = useNavigation();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [status, setStatus] = useState('');
    const [permissionGranted, setPermissionGranted] = useState(false);
    const [encrypt, setEncrypt] = useState(false);
    const [debugInfo, setDebugInfo] = useState(null);

    useEffect(() => {
        const requestStoragePermission = async () => { // Android 11+ doesn't require it for MediaStore or SAF
        if (Platform.OS === 'android' && Platform.Version < 30) {
            try {
                const granted = await PermissionsAndroid.request(
                    PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
                    {
                        title: 'Storage Permission',
                        message: 'This app logs data to your Downloads folder.',
                        buttonNeutral: 'Ask Me Later',
                        buttonNegative: 'Cancel',
                        buttonPositive: 'OK',
                    }
                );
            setPermissionGranted(granted === PermissionsAndroid.RESULTS.GRANTED);
            } catch (err) {
                console.warn(err);
            }
        } else {
            setPermissionGranted(true);
        }
        };
        requestStoragePermission();
    }, []);


    const handleLogin = async () => {

        if (!permissionGranted) {
            setStatus('Storage permission not granted');
            return;
        }

        const token = 'abc12345xyz';
        const timestamp = new Date().toISOString().replace('T', ' ').split('.')[0];
        let logData = `[${timestamp}] User: ${username} | Token: ${token}\n`;

        try {

            if (encrypt) {
                const passphrase = await getStoredKey();
                const { encryptedString, debugInfo } = await encryptText(logData);

                logData = encryptedString; // Store the encrypted log data
                setDebugInfo({
                    passphrase,
                    salt: debugInfo.salt ? debugInfo.salt.toString(CryptoJS.enc.Hex) : 'N/A',
                    iv: debugInfo.iv ? debugInfo.iv.toString(CryptoJS.enc.Hex) : 'N/A',
                    derivedKey: debugInfo.derivedKey ? debugInfo.derivedKey.toString(CryptoJS.enc.Hex) : 'N/A',
                    ciphertext: debugInfo.ciphertext ? debugInfo.ciphertext.toString(CryptoJS.enc.Hex) : 'N/A',
                });
            } else {
                setDebugInfo(null);
            }

            const path = `${RNFS.DownloadDirectoryPath}/log.txt`;
            await RNFS.appendFile(path, logData + '\n', 'utf8');
            setStatus(`Login success — logged to /Download/log.txt`);
            setTimeout(() => navigation.navigate('Home'), 5000);
        } catch (e) {
            console.error(e);
            setStatus('Logging failed');
        }
    };


    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.title}>Victim App - Login</Text>
            <TextInput
                placeholder="Username"
                value={username}
                onChangeText={setUsername}
                style={styles.input}
            />
            <TextInput
                placeholder="Password"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                style={styles.input}
            />
            <View style={styles.toggleContainer}>
                <Text>Encrypt Logs</Text>
                <Switch value={encrypt} onValueChange={setEncrypt} />
            </View>
            <Button title="Login" onPress={handleLogin} />
            <Text style={styles.status}>{status}</Text>

            {debugInfo && (
                <View style={styles.debugContainer}>
                    <Text style={styles.debugHeader}>🔐 Encryption Details</Text>
                    <Text selectable>Passphrase: {debugInfo.passphrase}</Text>
                    <Text selectable>Salt: {debugInfo.salt}</Text>
                    <Text selectable>IV: {debugInfo.iv}</Text>
                    <Text selectable>Derived Key: {debugInfo.derivedKey}</Text>
                    <Text selectable>Ciphertext: {debugInfo.ciphertext}</Text>
                </View>
            )}
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: { padding: 20, marginTop: 50 },
    title: { fontSize: 22, marginBottom: 20 },
    input: { borderWidth: 1, padding: 10, marginBottom: 10 },
    toggleContainer: { flexDirection: 'row', alignItems: 'center', marginBottom: 20 },
    status: { marginTop: 20, color: 'blue' },
    debugContainer: { marginTop: 30, padding: 10, backgroundColor: '#f0f0f0', borderRadius: 8 },
    debugHeader: { fontWeight: 'bold', marginBottom: 10 },
});