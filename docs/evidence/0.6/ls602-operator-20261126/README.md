# LS602 shared operator status and fresh requests

Accepted operator selector `coasterslaunchsystems_operator.shared_status_requests`, run by `/root/rides` on Java 21 with the frozen FD route/operator API1 artifact identified in [artifact.json](artifact.json). LS was loaded from development classes; this record does not claim a packaged LS run. Exact focused compiled-class and source hashes are retained alongside the full log.

The actual Arrow chassis, native passenger seat and installed LS controller reached READY. The native panel's nested operator snapshot equaled the local LS adapter snapshot, including the controller owner and charge/reason/request data. Dispatch queued one transient request without releasing native authority. A replayed revision was denied and the request consumed exactly once. Hold disarmed into inspection without losing the reservation; Resume rearmed without dispatch. Controller NBT reload disarmed, cleared pending input and invalidated the earlier revision. The delayed check confirmed no automatic resume.

The explicit pass marker is in [the full combined log](native-operator-pass-and-route-diagnostic.txt). The other selector failed on a separate route-bank projection tolerance issue, so the combined process exited 1. This does not invalidate the operator pass; subsequent route-only runs exclude this closed selector. See the [route evidence](../ls601-route-delegation-20261126/README.md) and [physical/occupied integration record](../../ls06-route-20261126/README.md) for their independent outcomes.

## Fixture corrections and scope

The initial selection found no generated tests because the reused empty template belonged to another namespace. A matching `coasterslaunchsystems_operator:empty` template fixed selection. The test timeout is 300 ticks, including the shared FD helper's 120-tick offset. The first selected operator setup used scene slot 26, yielding absolute x1280 outside native physics bounds. Slot 10 yields x512/y240/z−896..−832, separate from the route fixture near x320/y160/z−640. These setup failures tested no operator behavior; the corrected selector passed on its first behavioral run.

This checks server-side status and request semantics with actual LS/native objects and a fake operator player. It does not claim rendered button input or an occupied human ride. The separate FD native panel/admission receipt is `docs/evidence/launch-systems-route-20261126/` in the Fourth Dimension overlay; its final frozen artifact is the same one used here. Real occupied route, restart and rejoin evidence is owned by the route integration record.

## Shutdown and ownership

The combined headless server saved all dimensions and shut down at 2026-11-27 04:25:35 UTC (log lines 176–177); Gradle reported the unrelated route selector's exit 1 afterward. Runtime ownership remained with `/root/rides`; this subtask launched no client, display, server service or scope. The continuing route task may reuse its own persistent build/world paths. No runtime was retained by this operator subtask.

The separately owned [occupied observer receipt](occupied-observer/README.md) records normal native panel input, displayed owner/charge/denial, fresh dispatch, actual multi-train conflict, and saved-world continuation. Its packaged artifact timeline and runtime lifecycle are independent of this closed native selector.
