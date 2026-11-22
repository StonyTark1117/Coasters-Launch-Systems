exec(open('/mnt/NVME4/launchsystems-runs/ls01-freespin-20261122/ctl.py').read().split("if sys.argv[1]=='console':")[0])
import datetime
row={'at':datetime.datetime.now(datetime.timezone.utc).isoformat()}
for x,z in ((69,4),(85,28)):
 for key in ('phase','fault','boarding_ready','reservation'):
  row[f'{x}:{key}']=m.rcon(r.c,f'data get block {x} 98 {z} {key}')
row['native']=m.rcon(r.c,'fd status');row['body']=m.rcon(r.c,'sable info 006fd447-22ab-4bb8-801f-aab0e7a8fe20');row['rider']=m.rcon(r.c,'data get entity Dev RootVehicle')
with (r.evidence/'corrected-operation-readbacks.jsonl').open('a') as f:f.write(json.dumps(row)+'\n')
for key,val in row.items():
 if key=='native': val='\n'.join(s for s in val.splitlines() if any(v in s for v in ('513e1676','8edeea19','5a2e11ed','57e0446c')))
 print(key+': '+val)
