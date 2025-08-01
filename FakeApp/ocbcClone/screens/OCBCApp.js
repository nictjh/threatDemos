import React from 'react';
import {
  SafeAreaView,
  StatusBar,
  ScrollView,
  View,
  Text,
  TouchableOpacity,
  Image,
  StyleSheet,
  Dimensions,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons, MaterialIcons, Feather } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

const { width, height } = Dimensions.get('window');

export default function OCBCApp() {
    const navigation = useNavigation();
    const handleLogin = () => {
        console.log('Login button pressed');
        navigation.navigate('Login');
    };



  return (
    <SafeAreaView style={styles.container}>
        {/* Header with top icons */}
        <View style={styles.header}>
          <TouchableOpacity style={styles.topIcon}>
            <Feather name="maximize" size={20} color="#666666" />
          </TouchableOpacity>
          <TouchableOpacity style={styles.topIcon}>
            <View style={styles.notificationContainer}>
              <Ionicons name="notifications-outline" size={20} color="#666666" />
              <View style={styles.notificationBadge} />
            </View>
          </TouchableOpacity>
        </View>

        {/* Banner with improved gradient coverage */}
        <View style={styles.bannerContainer}>
          <View style={styles.redAccent} />
          <Image
            source={require('../assets/ocbc_header2.png')}
            style={styles.bannerImage}
            resizeMode="cover"
          />
          {/* Multi-layer gradient for better coverage */}
          <LinearGradient
            colors={[
              'rgba(255,255,255,0.1)', 
              'rgba(248,249,250,0.3)', 
              'rgba(240,242,245,0.6)', 
              'rgba(240,242,245,0.85)', 
              '#f0f2f5'
            ]}
            locations={[0, 0.4, 0.65, 0.85, 1]}
            style={styles.gradientOverlay}
          />
        </View>

        <View style={styles.servicesContainer}>
          <View style={styles.serviceRow}>
            <TouchableOpacity style={styles.serviceItem}>
              <View style={styles.serviceIcon}>
                <MaterialIcons name="currency-exchange" size={22} color="#666666" />
              </View>
              <Text style={styles.serviceText} numberOfLines={1}>Foreign Exchange</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.serviceItem}>
              <View style={styles.serviceIcon}>
                <MaterialIcons name="qr-code-scanner" size={22} color="#666666" />
              </View>
              <Text style={styles.serviceText} numberOfLines={1}>Scan & Pay</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.serviceItem}>
              <View style={styles.serviceIcon}>
                <Text style={styles.payNowText}>PAY{'\n'}NOW</Text>
              </View>
              <Text style={styles.serviceText} numberOfLines={1}>PayNow</Text>
            </TouchableOpacity>
          </View>
          <View style={styles.serviceRow}>
            <TouchableOpacity style={styles.serviceItem}>
              <View style={styles.serviceIcon}>
                <Ionicons name="bulb-outline" size={22} color="#666666" />
              </View>
              <Text style={styles.serviceText} numberOfLines={1}>Wealth Insights</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.serviceItem}>
              <View style={styles.serviceIcon}>
                <MaterialIcons name="card-giftcard" size={22} color="#666666" />
              </View>
              <Text style={styles.serviceText} numberOfLines={1}>Rewards</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.serviceItem}>
              <View style={styles.serviceIcon}>
                <Feather name="more-horizontal" size={22} color="#666666" />
              </View>
              <Text style={styles.serviceText} numberOfLines={1}>More</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Login Button */}
        <TouchableOpacity style={styles.loginButton} onPress={() => handleLogin()}>
          <Text style={styles.loginButtonText}>Log in to OCBC Singapore</Text>
        </TouchableOpacity>

        {/* Maintenance Notice */}
        <View style={styles.maintenanceNotice}>
          <Text style={styles.maintenanceText}>
            <Text style={styles.maintenanceBold}>Scheduled maintenance:</Text>{' '}
            Various services will not be available from 31 Jul (12am) to 4 Aug (1am).{' '}
            <Text style={styles.learnMoreText}>Learn more</Text>
          </Text>
        </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f0f2f5',
  },
  statusBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 45,
    paddingBottom: 8,
    backgroundColor: 'transparent',
  },
  statusLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusTime: {
    fontSize: 16,
    fontWeight: '600',
    color: '#000',
  },
  statusRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  signalBars: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 1,
  },
  bar: {
    width: 3,
    backgroundColor: '#000',
    borderRadius: 1,
  },
  battery: {
    backgroundColor: '#000',
    borderRadius: 3,
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  batteryText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 10, // Reduced padding to move icons to corners
    paddingVertical: 10,
    position: 'absolute',
    top: 55, // Moved up from 75 to 55 (20 pixels higher)
    left: 0,
    right: 0,
    zIndex: 10,
  },
  topIcon: {
    width: 24, // Reduced size since no background
    height: 24,
    justifyContent: 'center',
    alignItems: 'center',
    // Removed all background and shadow styling
  },
  notificationContainer: {
    position: 'relative',
  },
  notificationBadge: {
    position: 'absolute',
    top: 1,
    right: 1,
    width: 6,
    height: 6,
    backgroundColor: '#E71D26',
    borderRadius: 3,
  },
  bannerContainer: {
    height: height * 0.65, // Increased to 65% as requested
    overflow: 'hidden',
    position: 'relative',
  },
  redAccent: {
    position: 'absolute',
    top: -40,
    left: -280, // Brought back to the right to make it visible again
    width: height * 0.35, // Made bigger
    height: height * 0.75, // Made bigger
    backgroundColor: '#E71D26',
    borderTopRightRadius: height * 0.65, // Much rounder
    borderBottomRightRadius: height * 0.65, // Much rounder
    zIndex: 3,
  },
  bannerImage: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    zIndex: 1,
  },
  gradientOverlay: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: '8%', // Increased gradient coverage
    zIndex: 2,
  },
  servicesContainer: {
    marginTop: 10, // Changed from -(height * 0.02) to move down
    backgroundColor: 'transparent',
  },
  serviceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 6, // Reduced from 12 to make more compact
    marginHorizontal: 20,
  },
  serviceItem: {
    width: (width - 40) / 3,
    alignItems: 'center',
  },
  serviceIcon: {
    width: 50, // Reduced from 60 to make smaller
    height: 50, // Reduced from 60 to make smaller
    borderRadius: 25, // Adjusted for new size
    backgroundColor: '#ffffff',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 3,
  },
  payNowText: {
    fontSize: 8, // Reduced slightly for smaller icon
    fontWeight: 'bold',
    color: '#666666',
    textAlign: 'center',
    lineHeight: 10,
  },
  serviceText: {
    fontSize: 12, // Increased from 11
    lineHeight: 15, // Increased to match font size
    color: '#666666',
    textAlign: 'center',
    maxWidth: 100, // Increased from 85 to show full "Foreign Exchange" text
  },
  loginButton: {
    backgroundColor: '#3e5259', // Changed from '#223040'
    height: 48,
    borderRadius: 5, // Reduced from 24 to make more rectangular
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 20,
    marginTop: 20, // Increased from 6 to move down
  },
  loginButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  maintenanceNotice: {
    marginTop: 16, // Increased from 6 to move down
    marginBottom: 20, // Increased from 12 for better spacing
    paddingHorizontal: 20,
  },
  maintenanceText: {
    fontSize: 12,
    color: '#666666',
    lineHeight: 18,
  },
  maintenanceBold: {
    fontWeight: '600',
  },
  learnMoreText: {
    color: '#2947efff',
    fontWeight: '600',
  },
});