# LS601 route delegation implementation

The selected FD baseline is `dc1c83a543256a5ffeab6d7958e9a3656c132f74`.
The additive route API is developed in the existing FD task worktree; the newer
primary 1.5 checkout is not a runtime input. LS equipment and occupied route acceptance are complete in the
[integration record](../integration-20261126/README.md). The implementation
chronology below retains the scope of its original inputs.

The focused native route and operator tests passed on the frozen additive
artifact (reported by its operator/API test owner):
`/mnt/NVME4/launchsystems-runs/ls06-operator-20261126/artifacts/fourthdimension-route-operator-final-api1.jar`,
SHA256 `97772d1d9cba7a843ecb845a442841fe0d553edacd1dae951aef8e1777c8e010`.
That artifact was the initial consumer input, superseded by the readiness
artifact below. Native
passing coverage includes exact reservation pair, changed admitted consist,
replaced authority owner, opposing direction and missing native body denial.
The FD repository receipt is `docs/evidence/launch-systems-route-20261126/README.md`,
with `route-final.txt`, `panel-pass-and-initial-route-setup.txt`, source hashes and
verified clean server shutdown. Its simple registered controller is a prerequisite
seam test; the LS-loaded physical cases remain separate consumer evidence.

LS consumes `RideAuthority.EXTERNAL_ROUTE_API = 1`. FD keeps the admitted train
UUID and its current/next reservations; the existing native authority token
remains the longitudinal grant. LS retains its physical equipment/stopping lease.
There is no new reservation manager or persisted permission cache.

Initial stopped admission uses `requestRoute` before native claim. Explicit
controller rearm uses `resumeRoute` before native resume. Normal physics snapshots
continue through native `holds`/`permits`, so native route validation is required
for each propulsion decision. Older API1 artifacts retain the managed-route veto.
The provider declares its directed current span, checks saved geometry and loaded
support, and refuses swing direction changes on the one-way bounded route.

Denied route service retains the same controller, complete consist and native
owner checks. Stopped capture may use the current retained token; this never
authorizes propulsion, clears route suspension, or dispatches. Native and local
operator controls share the `OperatorStatus` adapter and FD route status.

The additive API keeps `EXTERNAL_API = 1` and introduces:

- `ExternalProvider.supportsManagedRoutes()` defaults to false; the existing
  unmanaged API1 contract remains supported. Older reflection proxies that do
  not recognize the new method are treated as not opted in.
- `routeDirection(level, controller, vehicle)` declares the current equipment
  span direction relative to the native rail. It grants no reservation.
- `requestRoute(level, controller, chassis, provider)` requests stopped admission
  before initial native claim; `resumeRoute(level, controller, chassis, token)`
  performs explicit stopped recovery before native resume.
- `routeStatus(level, chassis)` returns managed/permitted, the native train
  owner, current/next blocks, blocked reason and fresh-request requirement.
  It refreshes opted-in route safety; inspecting a native or unknown provider
  does not change that train's native reservations or braking state.
- `operatorStatus` and `operatorAction` delegate the existing native panel to
  LS's shared status/request adapter. Dispatch queues a request; it does not
  directly release native authority.

Captured managed handoff keeps the FD train reservations and transfers only the
existing longitudinal controller token. The receiver remains held/suspended and
requires explicit rearm plus a new request. Denied or restored route state never
automatically resumes. Exact saved consist and ownership checks precede recovery
or service mutations. Route progression uses the observed segment index, not only
the six-block modulo mapping, and verifies the current/next owner pair before
native reconciliation can advance it.

Review checked missing-carrier/chassis failure, stale controller fallback and
compatibility boundaries. Missing native bodies deny LS force; native emergency
hold acts on known bodies without granting a replacement controller's equipment
permission. Only expensive curve fingerprints are cached by graph/curve revision
and declared rail metadata. Live body, loaded rail, owner, direction and velocity
checks remain per permission decision.

Route-capable eligibility uses native `hasRouteInterest`, including retained
admission after a route configuration change. This keeps stopped service managed;
it does not bypass direction/support validation or the explicit resume request.
Older API1 continues to use `isManaged`.

The first physical consumer run found touching fin intervals on the 36 m native
straight separated numerically by 1.49–2.01 micrometres. Local forward/backward
and transfer-bank coverage now share a 0.0001 m geometry seam tolerance. No bank
length, force, stopping envelope or missing-pad allowance changed. The four
`StoppingEnvelopeTest` tests passed, including both observed seams, overlap,
rejection of a 1 mm and 2 m gap, and nonfinite endpoints. See `coverage-test.txt`.
The affected native consumer cases subsequently passed; the already passing
shared operator selector carried forward.

The next physical run exposed a readiness mismatch: initial claim resets native
authority's stopped-tick count, while LS previously queried only family dispatch
guards. An immediate request at first READY could therefore fail native release's
five-stopped-tick guard. Route-owned LS readiness now additionally queries
`RideAuthority.dispatchReady(level, chassis, cachedToken)`. FD remains the sole
timer and repeats its release guards at dispatch; LS does not queue retries or
advance the native counter. Unowned inspection remains resolvable so initial
admission is possible. Local READY, station boarding readiness, transfer READY_EXIT
and the shared operator status consume the same motion snapshot. Compilation
passed (`compile-readiness.txt`); the subsequent physical retry used the updated
FD artifact and passed immediate first-READY dispatch without a fixture delay.
The route capability probe requires the exact boolean readiness method as well as
the API marker. An earlier transient route artifact without that method gets a
single startup warning and no managed-route opt-in, rather than repeated snapshot
exceptions. Foundation's focused native route test passed with readiness false
immediately after claim and a successful native dispatch at tick 70, preserving
all previous denial assertions. It did not assert a positive readiness query at
tick 70; the consumer's first-READY assertion supplies that check. The replacement frozen input is
`/mnt/NVME4/launchsystems-runs/ls06-operator-20261126/artifacts/fourthdimension-route-operator-readiness-api1.jar`,
SHA256 `fd267b3d627c8db3a4d4b71d70ecb465bcf9a54768405d7ffb72af607884a167`.
The earlier operator pass carries forward; the LS physical consumer retry passed
the first-READY dispatch check.

The actual occupied saved-world recovery exposed a separate missing call: active
paired transfers bypass the ordinary local evaluation that consumes the explicitly
queued provider rearm. Both controllers could arm while native restored authority
remained suspended indefinitely. The active source's preparation branch now invokes
the existing authority reservation/refresh only after exact retained-pair, module,
route, complete-train and occupancy validation, plus stopped native chassis/point
velocity and an actual spring clamp or native captured hold. It refreshes the train
snapshot before the existing preparation guards. Moving transfer and receiver paths
do not resume authority. No request is created; only the provider's already queued
explicit source rearm can resume a suspended token. Compilation passed in
`compile-active-recovery.txt`. The preserved same-world recovery and held-high
no-replay checks passed on the preserved world, followed by a separate normal
low/high input producing 1.312 m/s occupied motion. The independent
[recovery receipt](../integration-20261126/recovery-verification.json) confirms
identities, input ordering, stopped-world save and all five inactive runtime units.

Java21 `compileJava` passed with an isolated build and project cache on NVME4.
See `compile.txt` and the post-handoff `compile-handoff.txt`. No runtime was launched for these compilation checks. Targeted
native route/fault and occupied recovery evidence is owned by the route fixture
and operator agents; the linked integration record reconciles their completed
results without repeating unchanged cases.

Persistent build/evidence root:
`/mnt/NVME4/launchsystems-runs/ls06-lease-20261126`.
