import argparse, importlib.util, json
from pathlib import Path
spec = importlib.util.spec_from_file_location('private_client', '/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py')
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)
ROOT = Path('/mnt/NVME4/launchsystems-runs/ls01-fd-observer-20261122')
SERVER = Path('/mnt/NVME4/launchsystems-runs/bridge-20261122/arrow')
class ObserverRun(m.PrivateRun):
    def connected(self):
        state = m.unit_state('fd-ls-arrow-20261122-server.service')
        if state['ActiveState'] != 'active' or int(state['MainPID']) <= 0 or state['WorkingDirectory'] != str(SERVER):
            raise RuntimeError('Named bridge-owned server is not active in its recorded directory')
        if not m.owns_group(Path('/proc', state['MainPID'], 'cgroup').read_text(), state['ControlGroup']):
            raise RuntimeError('Server PID/cgroup mismatch')
        answer = m.rcon({'root': str(SERVER)}, 'list')
        if self.c['player'] not in answer.partition(':')[2].strip().split(', '):
            raise RuntimeError('Observer not connected: ' + answer)
        return answer
p = argparse.ArgumentParser()
p.add_argument('action')
p.add_argument('values', nargs='*')
p.add_argument('--expect', required=True)
p.add_argument('--modifier')
p.add_argument('--menu', action='store_true')
a = p.parse_args()
r = ObserverRun(ROOT / 'run.json', 'ls-foundation-fd-observer-20261122')
print(json.dumps(r.action(a), indent=2))
