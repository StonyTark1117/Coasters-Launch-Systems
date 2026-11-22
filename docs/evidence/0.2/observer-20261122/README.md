# 0.2 independent operator evidence — 2026-11-22

Owner `/root`, Observer on private display `:399`, 1536M Java 21 heap and 6 GiB Minecraft-only unit. Connected to `/root/rides` standalone server `127.0.0.1:25771`; exact candidate and paths in [run.json](run.json). This candidate predates the later cable housing/Ponder update; linear/skid behavior is unchanged.

Normal camera turning and reachable vanilla cobblestone placement established input before ride actions. Disclosed operator poses, supplies and a temporary standing block are setup, not player-construction proof. Default raw input and mouse bindings remained fixed.

- Normal lever placement at `(5,99,8)` read back unpowered. At **21:12:48.909641 UTC**, one normal use powered it and dispatched the occupied mixed LSM/LIM swing. [Before](occupied-dispatch-ready.png) and [after](20261122T211248.909641Z-after.png). Rides telemetry confirms three passes, HOLD and unchanged rider attachment.
- After confirming crouched F3 hit on tire `(15,98,8)`, one empty-hand Shift-use at **21:17:26.539747 UTC** armed it. [Target](20261122T211712.577646Z-after.png), [input](20261122T211726.539747Z-after.png). Rides telemetry confirms occupied coast into the independent passive skid, heating and near-zero final speed without fin clamping.

Full input/readback records are [input.jsonl](input.jsonl) and [observer-rcon.jsonl](observer-rcon.jsonl). The rider video, clear occupied still and numeric results are in [linear/skid evidence](../../ls02-linear-20261122/README.md). Temporary operator standing block removed after dispatch.

Observer disconnected; server list then showed only Dev. Both owned units are inactive, MainPID 0, Result success ([shutdown](shutdown.txt)). The rides owner retains responsibility for saving and stopping its server; root did not stop that shared server.
