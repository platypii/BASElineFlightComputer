# Bluetooth Laser Rangefinders

BASEline talks to BLE laser rangefinders to capture `LaserMeasurement(horiz, vert)` pairs (meters) for visualizing jumps. `RangefinderService` instantiates one `BleProtocol` per vendor and hands them to `BleService`, which scans and dispatches to whichever protocol's `canParse()` claims the peripheral.

All four protocols listen for BLE notifications and post `LaserMeasurement` events on the EventBus. Total slant range and inclination are converted to horizontal / vertical components via `horiz = total * cos(pitch)`, `vert = total * sin(pitch)`.

## ATN (`ATN-LD99`)

- Identification: device name `ATN-LD99`.
- Service / characteristic: `0000ffe0…` / `0000ffe1…` (notify).
- Frame: 8 bytes starting with `10 01`. Byte 2 is a flag bitfield (`0x80` = failure, `0x20` = normal, `0x10` = fog, `0x01` = yards). Bytes 3–4 are range in half-units; bytes 5–6 are pitch in tenths of a degree.
- No outbound commands; the unit pushes a frame per trigger pull.

## Sig Sauer (Kilo BDX)

- Identification: manufacturer ID `1179` with manufacturer data `02-00-ff-ff-ff-ff-…`, or a device name containing `BDX`.
- Service: `49535343-fe7d-…`. Write on `fff1`, notify on `fff2`.
- ASCII sentence protocol. On connect the app sends `:DU,<code>,<xor>\r` where `<code>` is a name-derived unlock (required on newer units, silently ignored by old ones). Measurements arrive as `:AB,…,range,…,incl,xor`.
- Range is yards (×0.9144 → m); pitch is degrees. Checksum is XOR of the body with extra constants 13/138/171 (see `validate` / `appendChecksum`).

## Vectronix Terrapin-X

- Identification: manufacturer ID `1164` with data `01-a0-ff-ff-ff-ff-00`, or name starting with `Terrapin` / `FastM`, or advertises the Terrapin service UUID.
- Service: `81480000-…`. Notify on `…0100`, write on `…0200`.
- Binary framed protocol delimited by `0x7e` with `0x7d` byte-stuffing (see `TerraSentenceIterator`, `escape`/`unescape`). Each frame is `packetType(2) | dataLen(2) | command(2) | data | crc16(2)` with the CRC16 reflected polynomial `0x8408` (`Crc16.java`).
- Flow: rangefinder sends `commandNewMeasurementAvailable` (0x1000); app requests `GetLastRange` (0x1002, cm) then `GetLastInclination` (0x1003, millidegrees) to reconstruct x/y. Ack/Nack packets (types 4/5) are logged only.

## Uineye / Hawkeye

- Identification: five known manufacturer ID/data tuples (IDs `21881`, `19784`, `42841`), or name suffix `BT05` / prefix `Rangefinder` / `Uineye`, or the advertised service UUID.
- Service / characteristic: `0000ffe0…` / `0000ffe1…` (same UUIDs as ATN — disambiguated by scan record).
- Binary frames wrapped by `ae-a7 … bc-b7` (see `RfSentenceIterator`). On connect the app sends `appHello` and then replies to periodic `heartbeat` frames with `appHeartbeatAck`. Pre-2023 firmware rejects `WriteType.WITH_RESPONSE`; the code falls back to `WITHOUT_RESPONSE` on `IllegalArgumentException`.
- Measurement frame (`value[0]==23, value[1]==0`): byte 21 selects units (1=m, 2=yd, 3=ft); bytes 3–4 pitch (0.1°), 7–8 vertical, 9–10 horizontal (all ×0.1×units). Sign of `vert` is forced to match pitch.
