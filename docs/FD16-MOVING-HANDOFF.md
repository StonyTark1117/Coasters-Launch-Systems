# Fourth Dimension 1.6 moving handoff

Acceptance complete, 2026-11-27; companion remains unpublished.
[Integration evidence](evidence/fd16-integration-20261127/README.md). Source owner for this adapter: `/root` in the existing
`codex/launch-systems-0-4-0` checkout, starting from published source `041e21d`
and publication receipt commit `be7f4f6`. Version 1.0.1 is an unpublished
integration candidate; published 1.0.0 remains unchanged.

The schema-8/API1 fixed-track adapter remains available. Schema 9 requires
Fourth Dimension's explicit `EXTERNAL_MOVING_API = 1` and exact FD 1.6.0 loader
metadata; a schema number alone does not grant compatibility. Native LS
standalone behavior and the previously qualified family/mechanism certificates
are unchanged by the moving opt-in.

Moving support is finite: original Arrow or Wing on the corresponding native
FD bed/insert, within accepted FD bed/train/mechanism limits, and a fixed local
Tire/LSM/LIM controller with fin brakes. Cable and swing operation, active
section transfers, managed FD circuits and other families are not moving
certificates. A configured fixed Tire exit may run after docking under the
existing paired section transfer guards.

The moving controller captures the complete held train and suspends its old
launch token. LS reports no propulsion or braking permission while native
moving retention owns the bodies. No LS equipment is installed on the bed.
At a physically locked destination, Release through FD, inspect the real fixed
LS modules, then explicitly arm the receiving LS controller. That fresh arm
acknowledges the retained native handoff; it does not launch. Issue a separate
fresh launch request when READY. Original LS reservation cleanup occurs only
after FD has accepted the new owner and only for the exact old controller UUID
and complete reservation. Missing old hardware cannot become a substitute owner.

The receiving identity persists during released service so another controller
cannot repeatedly take the same handoff. On restart, FD native recovery and LS
inspection/re-arm are both required; no launch request or drive permit survives.
Missing FD/LS/provider, changed controllers or bodies and unavailable dock/support
remain held. The two native bridge anchors alone are exempted from LS running
clearance after live FD dock authentication; unrelated obstructions stay denied.

Native Arrow/Wing consumer, moving dependency-loss and occupied checks pass. Existing 1.0 publication and qualification records remain historical.

## Mounting and normal operation

For original Arrow, keep fixed cabinets/motors below the full bed/carrier/rider
sweep. The live FD dock exception permits eligible Tire/Fin/LSM/LIM cabinets
below the contact up to four blocks total distance and 1.025 blocks horizontally.
The extra 25 mm follows FD's native socket alignment envelope, not a general
mounting increase. Ordinary mounting stays three blocks. Resolve, restore and
cached binds reauthenticate the actual dock; absent FD and non-dock rails cannot
borrow the exception. [Mounting evidence](evidence/fd16-mounting-20261127/README.md).

Normal sequence: source hold/closed restraints → FD Capture → Move → locked
Release → explicit receiving LS arm → a separate launch request. A receiver may
report READY_EXIT directly when the stopped train is already in its fin zone.
A redstone request comes from the block **above** the controller. Use a new low
then high edge for the paired Tire exit. A side lever does not request launch.
See the sister [player procedure](<../../Coasters: Fourth Dimension/docs/MOVING-TRACK.md#launch-systems-handoff-16>).

The representative two-client Wing cycle now passes physical handoff, power loss,
cold restart, receiving arm, actual drive, whole-train downstream clearance and
normal dismount. All owned runtimes stopped successfully. This combines with
native force/loss checks; it is not a new all-family qualification campaign.
