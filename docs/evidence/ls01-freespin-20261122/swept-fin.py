import json,math
from pathlib import Path
p=Path('/mnt/NVME4/launchsystems-runs/ls01-freespin-20261122/evidence')
rows=list(map(json.loads,(p/'fault-geometry.jsonl').read_text().splitlines()))
body=rows[1]; motion=rows[-1]; t0=motion['contactT']; origin=motion['contactPoint']; q=body['orientation']; yaw=2*math.atan2(q[1],q[3])
boxes=[b for b in body['boxes'] if b['type']=='coasterslaunchsystems:brake_fin' and b['worldAabb'][1]<100]
assert len(boxes)==1
points=boxes[0]['worldCorners']
def path(t): return (.5+16*(3*t*t-2*t*t*t),32.5+16*t*(1-t))
def angle(t): return math.atan2(96*t*(1-t),16*(1-2*t))
def overlap(points,x,z):
 # Exact 2D oriented rectangle versus unit square, with unchanged vertical overlap.
 fixed=[(x,z),(x+1,z),(x,z+1),(x+1,z+1)]
 axes=[(1,0),(0,1)]
 for i in [1,4]:
  dx=points[i][0]-points[0][0]; dz=points[i][1]-points[0][1]; n=math.hypot(dx,dz)
  if n>1e-8: axes.append((dx/n,dz/n))
 for ax,az in axes:
  a=[px*ax+pz*az for px,pz in points];b=[px*ax+pz*az for px,pz in fixed]
  if min(max(a),max(b))-max(min(a),min(b))<=1e-6:return False
 return True
out={'method':'Geometric prediction only: rigid stopped fin moved along measured native cubic and yaw tangent, no physics or gameplay input; current negligible pitch/roll retained in local points','currentT':t0,'currentFin':boxes[0],'samples':[]}
for i in range(0,101):
 t=t0+i*.0001;cx,cz=path(t); delta=angle(t)-yaw;c,s=math.cos(delta),math.sin(delta)
 pts=[(cx+c*(p[0]-origin[0])+s*(p[2]-origin[2]),cz-s*(p[0]-origin[0])+c*(p[2]-origin[2])) for p in points]
 hit=[(3,99,z) for z in range(32,36) if overlap(pts,3,z)]
 if i==0 or hit:
  out['samples'].append({'t':t,'contactXZ':[cx,cz],'finMaxX':max(p[0] for p in pts),'intersectedTemporaryStones':hit})
 if hit:break
(p/'swept-fin-prediction.json').write_text(json.dumps(out,indent=2))
print(json.dumps(out['samples']))
