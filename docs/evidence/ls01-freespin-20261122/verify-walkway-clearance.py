exec(open('/mnt/NVME4/launchsystems-runs/ls01-freespin-20261122/ctl.py').read().split("if sys.argv[1]=='console':")[0])
import datetime
rows=[]
for x in (67,83):
 for z in range(-3,36):
  cmd=f'execute if block {x} 99 {z} minecraft:air'
  result=m.rcon(r.c,cmd); rows.append({'command':cmd,'result':result})
  if 'Test passed' not in result: raise RuntimeError(f'Temporary walkway remains at {x},99,{z}')
(r.evidence/'corrected-walkway-clearance.json').write_text(json.dumps({'at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'purpose':'Both complete temporary strips, including both turn sweeps, are air before dispatch','checks':rows},indent=2))
print(f'PASS: all {len(rows)} temporary walkway cells are air')
