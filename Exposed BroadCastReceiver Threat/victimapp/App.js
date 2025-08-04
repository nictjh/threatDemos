import React, { useEffect, useState, useRef } from 'react';
import {
  Alert,
  Button,
  Text,
  View,
  StyleSheet,
  DeviceEventEmitter,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
} from 'react-native';

const FAKE_CLEANUP_DURATION_MS = 2500; // 2.5 seconds

export default function App() {
  const [status, setStatus] = useState('🕐 Idle');
  const [lastActionTime, setLastActionTime] = useState('');
  const [isCleaning, setIsCleaning] = useState(false);
  const [progress, setProgress] = useState(0); // 0..1
  const cleanupTimerRef = useRef(null);
  const progressIntervalRef = useRef(null);

  // Utility to update status + timestamp
  const updateStatus = (newStatus) => {
    setStatus(newStatus);
    setLastActionTime(new Date().toLocaleTimeString());
  };

  // Fake cleanup routine
  const startCleanup = () => {
    if (isCleaning) return;
    setIsCleaning(true);
    updateStatus('🧹 Cleaning cache...');
    setProgress(0);

    const start = Date.now();
    // progress updater
    progressIntervalRef.current = setInterval(() => {
      const elapsed = Date.now() - start;
      setProgress(Math.min(1, elapsed / FAKE_CLEANUP_DURATION_MS));
    }, 100);

    // finish after duration
    cleanupTimerRef.current = setTimeout(() => {
      finishCleanup();
    }, FAKE_CLEANUP_DURATION_MS);
  };

  const finishCleanup = () => {
    if (progressIntervalRef.current) clearInterval(progressIntervalRef.current);
    if (cleanupTimerRef.current) clearTimeout(cleanupTimerRef.current);
    setProgress(1);
    setIsCleaning(false);
    updateStatus('✅ Cache cleaned!');
    // short delay to reset progress bar
    setTimeout(() => setProgress(0), 500);
  };

  const cancelCleanup = () => {
    if (progressIntervalRef.current) clearInterval(progressIntervalRef.current);
    if (cleanupTimerRef.current) clearTimeout(cleanupTimerRef.current);
    setIsCleaning(false);
    setProgress(0);
    updateStatus('❌ Cleanup cancelled');
  };

  // Simulate automatic cleanup on start with a small delay
  useEffect(() => {
    const auto = setTimeout(() => {
      startCleanup();
    }, 1000);
    return () => {
      clearTimeout(auto);
      if (progressIntervalRef.current) clearInterval(progressIntervalRef.current);
      if (cleanupTimerRef.current) clearTimeout(cleanupTimerRef.current);
    };
  }, []);

  // (Optional) still listen for broadcasts to retrigger cleanup or show alert
  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      'CleanupTriggered',
      (payload) => {
        // you can repurpose this to trigger a cleanup if desired
        Alert.alert('CleanupTrigger Received', `Payload: ${payload}`);
        if (payload === 'LEGIT_CLEANUP') {
          startCleanup();
        } else if (payload === 'DELETE_ALL_FILES') {
          updateStatus('⚠️ Dangerous cleanup triggered!');
        } else {
          updateStatus(`📨 Unknown command: ${payload}`);
        }
      }
    );
    return () => subscription.remove();
  }, []);

  // Progress bar component
  const ProgressBar = ({ progress }) => {
    return (
      <View style={styles.progressContainer}>
        <View style={[styles.progressBar, { width: `${Math.round(progress * 100)}%` }]} />
      </View>
    );
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>📱 Device Booster</Text>

      <View style={styles.card}>
        <Text style={styles.sectionTitle}>Cleanup Status</Text>
        <Text style={styles.status}>{status}</Text>
        {lastActionTime !== '' && (
          <Text style={styles.timestamp}>🕒 Last updated at {lastActionTime}</Text>
        )}

        {isCleaning && (
          <>
            <ProgressBar progress={progress} />
            <Text style={styles.smallText}>
              {Math.round(progress * 100)}% complete
            </Text>
          </>
        )}

        <View style={styles.buttonRow}>
          {!isCleaning ? (
            <Button title="Start Cleanup" onPress={startCleanup} />
          ) : (
            <Button title="Cancel" onPress={cancelCleanup} color="#d9534f" />
          )}
        </View>
      </View>

      <View style={styles.card}>
        <Text style={styles.sectionTitle}>Tools</Text>
        <View style={styles.buttonWrapper}>
          <Button title="Optimize Memory" onPress={() => updateStatus('✅ Memory optimized!')} />
        </View>
        <View style={styles.buttonWrapper}>
          <Button title="Backup Settings" onPress={() => updateStatus('✅ Settings backed up!')} />
        </View>
        <View style={styles.buttonWrapper}>
          <Button title="Run System Check" onPress={() => updateStatus('✅ System is healthy!')} />
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 24,
    backgroundColor: '#f2f5fa',
    alignItems: 'center',
    justifyContent: 'flex-start',
    flexGrow: 1,
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
    color: '#1f3c88',
    marginBottom: 16,
  },
  card: {
    width: '100%',
    backgroundColor: '#fff',
    padding: 18,
    borderRadius: 14,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOpacity: 0.08,
    shadowRadius: 10,
    elevation: 3,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 6,
    color: '#333',
  },
  status: {
    fontSize: 16,
    color: '#222',
    marginBottom: 4,
  },
  timestamp: {
    fontSize: 12,
    color: '#666',
    marginBottom: 10,
  },
  buttonRow: {
    marginTop: 12,
    flexDirection: 'row',
    justifyContent: 'flex-start',
  },
  buttonWrapper: {
    marginTop: 10,
  },
  progressContainer: {
    height: 8,
    backgroundColor: '#e6e6e6',
    borderRadius: 4,
    overflow: 'hidden',
    marginTop: 8,
  },
  progressBar: {
    height: '100%',
    backgroundColor: '#4f8cff',
  },
  smallText: {
    fontSize: 12,
    color: '#555',
    marginTop: 4,
  },
});