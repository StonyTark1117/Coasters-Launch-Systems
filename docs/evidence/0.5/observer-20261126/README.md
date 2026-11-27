# 0.5 independent operator and observer receipts

Owner `/root`; curator `/root/foundation`. This is a bounded copy of the
append-only evidence under
`/mnt/NVME4/launchsystems-runs/ls05-fixed-20261122/observer/evidence`.
The run used private display `:399`, distinct player `Observer`, Java 21,
1536M game heap and a 6GiB Minecraft-only limit. The original Wing endpoint
and unit names are in [run identity](wing-run.json). The same owned observer
was subsequently handed to the LS502 server for Suspended and Axis.
Selected FD family artifact remains `3dbbc7a77e1b0cd02b3e5fa3a69e4b4823c5c5a3befca6f0e1e35d72381b7fba`.
Each family owner's record identifies its exact LS artifact and world.

[Input](input.jsonl) preserves completed original action records, including
expected result, connected player list, window geometry and default right-use
binding. [Readbacks](readbacks.jsonl) preserve original Unix timestamps,
setup commands and responses. The [snapshot manifest](snapshot-manifest.json)
records the finite selection and file hashes. No live script, runtime or world
was changed by curation. Original screenshot paths in JSON identify the source;
selected images copied here retain their original basenames.

| Case | Normal operator action and observed result (UTC) |
| --- | --- |
| Wing, November 22 | Empty-hand Shift-Use armed the flywheel, launch fin, eleven stop fins and controller; each had a readback. Controller was READY at 22:10:29.344. Normal lever Use receipt is 22:11:00.802; RUNNING readback 22:11:01.612, HOLD 22:11:31.927. At 22:11:47.660 Dev remained on `create:seat` UUID `[-902163043,688736994,-1705034905,-1951089939]`, matching the rider owner's record. |
| Suspended, November 22 | Normal lever Use at 22:13:41.864; RUNNING at 22:13:42.648, HOLD at 22:13:50.768. Dev remained seated, UUID `[603707230,-1468775240,-1633392693,-1877351635]`. External image shows the occupied native cabin. |
| Axis, November 27 | On the November 26 local-date continuation, normal lever Use at 02:24:42.713 UTC; RUNNING at 02:24:43.345, HOLD at 02:24:51.447. Dev remained seated, UUID `[1155506399,1838629581,-1266654259,-52743476]`. The rider owner supplies the matching boarding identity; the observer independently reads the held seat. |
| Pipeline, November 27 | Normal lever Use at 02:28:51.199 UTC; RUNNING at 02:28:51.852, HOLD at 02:28:58.409. Held seat UUID `[-369247900,-1751431134,-1985651198,-913913647]`; [held exterior](20261127T022913.458492Z-state.png) shows seated Dev and carrier. |
| Dive, November 27 | Normal lever Use at 02:31:01.454 UTC; RUNNING at 02:31:02.104, HOLD at 02:31:09.354. Held seat UUID `[-192738559,-816492939,-2079220642,1814297079]`; [held exterior](20261127T023109.876487Z-state.png). |
| Conventional Flying, November 27 | Normal lever Use at 02:36:15.762 UTC; HOLD at 02:36:23.302, fault empty. Held seat UUID `[-412790132,660554985,-1231641578,1800787459]`; [occupied exterior](20261127T023623.408457Z-after.png). |
| New-generation Flying, November 27 | Normal lever Use at 02:39:43.707 UTC; RUNNING at 02:39:44.367, HOLD at 02:39:53.757, fault empty. Same seat UUID `[-616422523,-56540571,-1914028514,1187728054]` was read during RUNNING and HOLD; [occupied posture/yaw exterior](20261127T023949.119073Z-state.png). |

The Wing rider record's earlier narrated dispatch time is not substituted for
the original operator timestamp above. The action and following state readback
are the precise operator receipt.

Flying service also has separate normal controller Shift-Use actions: disarm
at 02:37:57.401 produced INSPECTION/armed0; rearm at 02:38:02.564 produced
HOLD/armed1/fault empty. This restored native accessory supply under the
retained lease without a new dispatch edge. The rider record supplies the
subsequent native seated return, restraint opening and normal exit. New-generation
Flying used the same explicit sequence: disarm at 02:40:54.099 produced
INSPECTION/armed0; rearm at 02:40:59.154 produced HOLD/armed1/fault empty.
The rider record confirms native Unwind/seated return, body/leg opening and
normal exit without a held-high replay.

Positioning, empty-hand selection and spectator viewpoints are disclosed setup.
The observer's supplied Wing exterior position lacked a floor: the observer
fell during that view setup at 22:11:05.748 UTC, then normally clicked Respawn.
The [client excerpt](observer-setup-fall.txt) and death image preserve this
failure. Dev remained on the ride; after respawn, the observer obtained the
held-seat readback and occupied exterior image. This does not claim continuous
observer video through that interruption or treat an observer teleport as
normal player camera operation. The same live client separately proved a
camera turn and reachable vanilla stone placement before operating equipment.

Selected images: [Wing cabinet arm](20261122T220229.802963Z-after.png),
[Wing lever target](20261122T221100.802166Z-before.png),
[observer setup fall](20261122T221132.036235Z-state.png),
[Wing held rider](20261122T221201.586288Z-after.png),
[Suspended occupied exterior](20261122T221343.059779Z-after.png),
[Axis lever target](20261127T022442.712945Z-before.png), and
[Axis held exterior](20261127T022501.106771Z-state.png).

These receipts support [LS501](../../ls05-fixed-20261122/README.md) and
[LS502](../../ls05-articulated-20261122/README.md); they do not replace the
rider owners' native restraint/posture, physical HOME, service and saved-world
records. This final snapshot includes all six articulated occupied cases and the Wing
operator receipt. After normal menu Disconnect at 02:42:14.743, the server list
contained only Dev. The [final cleanup receipt](runtime-cleanup.txt) confirms
both root-owned client and display inactive, MainPID0, Result success, with
original PIDs absent at 02:42:25 UTC. No observer runtime remains.
