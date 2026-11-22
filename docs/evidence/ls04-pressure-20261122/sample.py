import importlib.util,sys,json
from pathlib import Path
spec=importlib.util.spec_from_file_location('private','/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
r=m.PrivateRun('/mnt/NVME4/launchsystems-runs/ls04-pressure-20261122/run.json','ls04-pressure-foundation-20261122')
label=sys.argv[1];assert label.replace('-','').isalnum()
import time,datetime
with (r.evidence/(label+'-occupied-pull.jsonl')).open('a') as f:
 for i in range(100):
  row={'utc':datetime.datetime.now(datetime.timezone.utc).isoformat()}
  for key,cmd in [('controller','data get block 4 98 8'),('cabinet','data get block -1 98 8'),('native','execute positioned 0.5 100 16 run sable info @e[distance=0..60,limit=5]'),('rider','data get entity Dev RootVehicle')]:row[key]=m.rcon(r.c,cmd)
  f.write(json.dumps(row)+'\n');f.flush();time.sleep(.5)
