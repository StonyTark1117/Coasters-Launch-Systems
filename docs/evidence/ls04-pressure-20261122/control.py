import argparse, importlib.util, json, datetime
from pathlib import Path
spec=importlib.util.spec_from_file_location('private_client','/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py')
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
r=Path('/mnt/NVME4/launchsystems-runs/ls04-pressure-20261122')
p=argparse.ArgumentParser();p.add_argument('action');p.add_argument('values',nargs='*');p.add_argument('--expect');p.add_argument('--modifier');p.add_argument('--menu',action='store_true');a=p.parse_args()
if a.action=='rcon':
 command=a.values[0];answer=m.rcon({'root':str(r)},command)
 with (r/'evidence/readbacks.jsonl').open('a') as f:f.write(json.dumps({'at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'command':command,'result':answer})+'\n')
 print(answer)
else:
 if not a.expect:raise ValueError('Expected outcome required')
 result=m.PrivateRun(r/'run.json','ls04-pressure-foundation-20261122').action(a)
 print(json.dumps({k:result[k] for k in ['at','action','after'] if k in result}))
