import importlib.util,json,sys,datetime
from pathlib import Path
spec=importlib.util.spec_from_file_location('private_client','/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py')
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
root=Path('/mnt/NVME4/launchsystems-runs/ls01-observer-20261122')
command=' '.join(sys.argv[1:])
answer=m.rcon({'root':'/home/braydon/.local/state/launchsystems-runs/ls01-rides-20261122'},command)
record={'at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'server_owner':'/root/rides','command':command,'result':answer}
with (root/'evidence/observer-rcon.jsonl').open('a') as f:f.write(json.dumps(record)+'\n')
print(answer)
