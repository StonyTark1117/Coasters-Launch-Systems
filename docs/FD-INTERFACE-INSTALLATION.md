# Installing native Fourth Dimension interfaces

Use the native vehicle item and its normal track and station workflow first.
Install LS hardware on the main chassis, never on a rotating seat or cosmetic
body panel. Coordinates below are block offsets from the chassis bearing in
its local orientation; they rotate with the vehicle, not with the world.
Do not remove native controllers, frames, seats, hinges or running gear.

| Hardware | Central mounting | Extended mounting |
| --- | --- | --- |
| Tire strip | (0,-1,0) | (0,-1,-5) |
| Friction fin | (1,-1,0) | (1,-1,-5) |
| Skid shoe | (0,-2,-1) | (0,-2,-6) |
| Conductive fin | (-1,-1,0) | (-1,-1,-5) |
| LSM plate | (0,-1,-1) | (0,-1,-6) |
| LIM plate | (0,-1,1) | (0,-1,-4) |
| Cable catch socket | (0,-2,0) | (0,-2,5) |

Fixed Inverted, Suspended, Conventional Flying, New-generation Flying and
Pipeline use the extended contacts to clear their native components. Build a
rigid chassis spine along (0,0,-1) through (0,0,-5) before mounting these
contacts. Preserve existing native frame blocks where they already occupy
that spine. The certified examples fill remaining cells with iron blocks;
that real mass is included in equipment ratings. The extended LSM plate is face-adjacent to the tire strip. The skid needs
(0,-1,-6) connected: the LSM reaction plate can occupy that cell when installed,
or use an iron connector for a skid-only installation. A supported extended cable
installation also needs the rear spine through (0,0,5), plus a vertical connector at (0,-1,5). The rear catch is
separate from the front brake and motor hardware. Suspended and New-generation
Flying cannot use the physical cable bed: it intersects their native swept
clearance envelope. See the certificate table for those exclusions.

Place the ordinary interface items against the exposed spine/support faces,
using access scaffolding outside the complete seat and running-gear sweep.
Remove construction scaffolding that intersects that sweep before arming.
A visible mounted block alone is insufficient: fixed trackside equipment must
meet the corresponding moving contact, including its longitudinal offset.
Bind each cabinet to the directed rail span with the Span Tool and link it to
the same controller. For hanging families, put motor/brake cabinets above the
rail or outside the native swept envelope, while retaining the permitted
physical binding distance. Native clearance checks remain active.

A five-block rigid arm is certified only while its contact actually meets the
selected span. Equipment projects that contact onto the accepted rail and
refuses an arm that misses a curve or extends beyond an endpoint. Plan straight
working sections and leave enough distance for the whole loaded train to stop.
Cable installations require a straight guide bed and a rear catch that can
release and return without passing through the hanging seats. The cable
cabinet belongs beside the socket's physical home; the launch fin belongs
beside the separate front fin station. Do not copy the bearing distance for
both stations.

Board through the native seat, close the native restraints, complete any
required posture or yaw preparation, then inspect and arm the LS controller.
Flying accessory transitions require a live, non-overstressed shaft supply;
stored launch energy alone does not power those native mechanisms. A fresh
normal request initiates a ready cycle. Native restraint, posture, route,
loaded-body and authority checks remain mandatory.

Plan native boarding-station coverage at the final held position as well as at
launch. Classic Spinning requires its designated dock within twelve blocks to
align the captured carrier for unloading; an LS fin stop alone does not make
native restraints ready to open. Place and bind a suitable native station,
Capture, wait for Ready, then open restraints and exit normally. Keep the
station and its access platform outside the moving body's clearance envelope.

For conventional Flying unloading, after LS HOLD use native Stop to capture.
This pauses external authority and leaves a retained LS fault, so native Set
seated can correctly refuse accessory power. Inspect the stopped train, then
normally Shift-Use the LS controller to disarm and separately Shift-Use to
rearm. The retained train stays in HOLD; a held-high request does not replay.
With fault-free live shaft supply restored, refresh the native editor, choose
Set seated, wait for SEATED and its lock, then open body and leg restraints
separately and exit normally. Any later dispatch requires a fresh request edge.
For New-generation Flying, use the same explicit disarm/rearm inspection after
native Stop, then choose Unwind + seated. Keep the rider retained while native
yaw returns to its dock alignment and the body becomes SEATED; open body and
leg restraints separately only after that transition completes. This service
operation preserves the stopped lease and does not replay the launch request.

Launched Inverting Spinning also requires its selected external controller to
remain available during native capture. If Capture pauses the owner and leaves
the retained LS inspection fault, normally disarm and rearm the local controller
first. With HOLD and no fault restored, use native Capture again to acknowledge
the repaired availability, wait for Ready, then open restraints and exit.
Neither inspection/rearm nor the repeated capture is a dispatch request; a
held-high lever does not replay the launch.
