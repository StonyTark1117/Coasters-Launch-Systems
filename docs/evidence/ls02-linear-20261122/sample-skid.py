exec(open('/mnt/NVME4/launchsystems-runs/ls02-linear-20261122/ctl.py').read().split("if sys.argv[1]=='console':")[0])
import time,datetime
for i in range(120):
 row={'at':datetime.datetime.now(datetime.timezone.utc).isoformat()}
 for key,cmd in {'skid':'data get block 17 98 24','tire':'data get block 15 98 8','body':'sable info 273b2cd9-e266-4cd7-8a23-90cbd7ab3d93','rider':'data get entity Dev RootVehicle'}.items():row[key]=m.rcon(r.c,cmd)
 with(r.evidence/'occupied-skid.jsonl').open('a') as f:f.write(json.dumps(row)+'\n')
 time.sleep(.5)
