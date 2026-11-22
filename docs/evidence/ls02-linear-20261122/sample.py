exec(open('/mnt/NVME4/launchsystems-runs/ls02-linear-20261122/ctl.py').read().split("if sys.argv[1]=='console':")[0])
import time,datetime
for i in range(100):
 row={'at':datetime.datetime.now(datetime.timezone.utc).isoformat()}
 for key,cmd in {'controller':'data get block 5 98 8','body':'sable info 5eb32f51-6661-4346-9a6e-a6df7ae05793','rider':'data get entity Dev RootVehicle',**{f'drive{z}':f'data get block -1 98 {z}' for z in range(2,38,2)}}.items():row[key]=m.rcon(r.c,cmd)
 with(r.evidence/'occupied-mixed-swing.jsonl').open('a') as f:f.write(json.dumps(row)+'\n')
 time.sleep(1)
