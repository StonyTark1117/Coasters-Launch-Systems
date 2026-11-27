#!/usr/bin/env python3
import importlib.util, sys, os, time, ctypes, ctypes.util, subprocess
spec = importlib.util.spec_from_file_location("fd_private", "/home/braydon/.codex/worktrees/launch-systems-authority/Coasters: Fourth Dimension/scripts/fd-private-client.py")
m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
Base = m.PrivateRun
server = Base("/mnt/NVME4/launchsystems-runs/ls06-route-20261126/run.json", "/root/rides")
class Observer(Base):
    def _input(self, window, args, record):
        seconds = float(os.environ.get("LS_OBSERVER_ATTACK_HOLD", "0"))
        if args.action != "attack" or seconds == 0:
            return super()._input(window, args, record)
        if not 0 < seconds <= 5:
            raise RuntimeError("Attack hold must be bounded to five seconds")
        condition = os.environ["LS_OBSERVER_BREAK_CONDITION"]
        subprocess.run(["xdotool", "windowfocus", "--sync", window], check=True, env=self.env)
        x = ctypes.CDLL(ctypes.util.find_library("X11"))
        t = ctypes.CDLL(ctypes.util.find_library("Xtst"))
        x.XOpenDisplay.argtypes = [ctypes.c_char_p]; x.XOpenDisplay.restype = ctypes.c_void_p
        x.XSync.argtypes = [ctypes.c_void_p, ctypes.c_int]
        x.XCloseDisplay.argtypes = [ctypes.c_void_p]
        t.XTestFakeButtonEvent.argtypes = [ctypes.c_void_p, ctypes.c_uint, ctypes.c_int, ctypes.c_ulong]
        d = x.XOpenDisplay(self.display.encode())
        if not d: raise RuntimeError("Cannot open owned Xvfb")
        started = time.monotonic()
        record["bounded_attack_seconds"] = seconds
        record["break_condition"] = condition
        try:
            t.XTestFakeButtonEvent(d, 1, 1, 0); x.XSync(d, 0)
            while time.monotonic() - started < seconds:
                time.sleep(.1)
                answer = m.rcon(server.c, condition)
                if answer.strip():
                    record["break_readback"] = answer
                    break
        finally:
            t.XTestFakeButtonEvent(d, 1, 0, 0); x.XSync(d, 0)
            x.XCloseDisplay(d)
            record["held_seconds"] = time.monotonic() - started
    def state(self, part):
        return server.state("server") if part == "server" else super().state(part)
    def start(self, part):
        if part == "server": raise RuntimeError("Observer does not own the server")
        return super().start(part)
    def connected(self):
        self.state("server")
        answer = m.rcon(server.c, "list")
        if self.c["player"] not in answer.partition(":")[2].strip().split(", "):
            raise RuntimeError("Observer is not connected: " + answer)
        return answer
m.PrivateRun = Observer
if "console" in sys.argv: raise RuntimeError("Observer has no server mutation authority")
m.main()
