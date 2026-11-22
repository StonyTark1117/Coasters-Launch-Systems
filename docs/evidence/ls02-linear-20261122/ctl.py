import importlib.util,sys,json
from pathlib import Path
spec=importlib.util.spec_from_file_location('private','/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py'); m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
r=m.PrivateRun('/mnt/NVME4/launchsystems-runs/ls02-linear-20261122/run.json','/root/rides')
if sys.argv[1]=='console':
 for command in sys.argv[2:]:
  result=m.rcon(r.c,command); print(command+' => '+result)
  with (r.evidence/'console.log').open('a') as f: f.write(json.dumps({'command':command,'result':result})+'\n')
elif sys.argv[1]=='snap': print(r.snapshot('current'))
elif sys.argv[1]=='status': print(json.dumps(r.status(),indent=2))
elif sys.argv[1]=='act':
 import argparse
 print(json.dumps(r.action(argparse.Namespace(action=sys.argv[2], values=sys.argv[4:],expect=sys.argv[3],menu=False,modifier=None)),indent=2))
