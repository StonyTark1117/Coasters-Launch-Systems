import importlib.util,sys,json
from pathlib import Path
spec=importlib.util.spec_from_file_location('private','/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
r=m.PrivateRun('/mnt/NVME4/launchsystems-runs/ls04-pressure-20261122/run.json','ls04-pressure-foundation-20261122')
import argparse,time

def command(s):
 out=m.rcon(r.c,s)
 with (r.evidence/'construction-readbacks.jsonl').open('a') as f: f.write(json.dumps({'command':s,'result':out})+'\n')
 return out

def action(expect,check,contains,modifier=None):
 result=r.action(argparse.Namespace(action='use',values=[],expect=expect,menu=False,modifier=modifier))
 readback=command(check);print(expect+' => '+readback,flush=True)
 if contains not in readback: raise RuntimeError('Action did not reach expected state; stopped')
 return readback

def pose(s): command('tp Dev '+s);time.sleep(.2)

def aim(x,y,z,px,py,pz):
 command(f'execute positioned {x} {y} {z} anchored eyes run tp Dev {px} {py} {pz} facing ~ ~ ~');time.sleep(.2)

def item(s): command('item replace entity Dev hotbar.0 with '+s)

if sys.argv[1]=='pressure-bind':
 item('coasterslaunchsystems:span_tool 1');command('item replace entity Dev weapon.offhand with air')
 for i,az in enumerate([0,44]):
  pose(f'3.5 100 {az+.5} 90 20.472')
  action('Select directed anchor for pressure cabinet','data get entity Dev SelectedItem','coasterslaunchsystems:span_'+('from' if i==0 else 'to'))
 aim(-.5,98.5,8.5,-.5,96,6.5)
 action('Bind new pressure cabinet','data get block -1 98 8 ls_from','has the following block data')
 aim(4.5,98.7,8.5,4.5,98,6.5)
 action('Select disarmed controller','data get entity Dev SelectedItem','coasterslaunchsystems:controller_selection',modifier='Shift_L')
 aim(-.5,98.5,8.5,-.5,96,6.5)
 action('Link new pressure cabinet identity','data get block -1 98 8 ls_controller','[I;')
elif sys.argv[1]=='anchors':
 item('simulatedcoasters:coaster_anchorpoint 2')
 for z in [0,44]:
  pose(f'3.5 100 {z+.5} 90 28.369')
  action('Place floor-up anchor',f'execute if block 0 100 {z} simulatedcoasters:coaster_anchorpoint[facing=up]','Test passed')
elif sys.argv[1]=='track':
 item('simulatedcoasters:coaster_track 64')
 for z,tag in [(0,'track_connecting_from'),(44,'')]:
  pose(f'3.5 100 {z+.5} 90 20.472')
  action('Select start/finish straight rail','data get entity Dev SelectedItem' if z==0 else 'data get block 0 100 0 AnchorPeerCurves',tag if z==0 else 'Bezier',modifier='Alt_L' if z else None)
elif sys.argv[1]=='guides':
 item('coasterslaunchsystems:cable_guide 64')
 for z in range(7,30):
  if 'Test passed' in command(f'execute if block 0 96 {z} coasterslaunchsystems:cable_guide'):continue
  pose(f'2.5 96 {z+.5} 90 39.007')
  action(f'Place guide bed {z}',f'execute if block 0 96 {z} coasterslaunchsystems:cable_guide','Test passed')
elif sys.argv[1]=='modules':
 for x,z,block in [(-1,8,'flywheel_cabinet')]+[(1,z,'fin_brake') for z in [8]+list(range(23,44,2))]:
  if 'Test passed' in command(f'execute if block {x} 98 {z} coasterslaunchsystems:{block}'):continue
  item('coasterslaunchsystems:'+block+' 1')
  command(f'setblock {x*2} 97 {z} stone')
  command(f'setblock {x*2} 98 {z} create:creative_motor[facing={"east" if x<0 else "west"}]{{Speed:64.0f,ScrollValue:64}}')
  if not 7<=z<=29: command(f'setblock 0 96 {z} stone')
  pose(f'.5 {96.1875 if 7<=z<=29 else 97} {z+.5} {90 if x<0 else -90} {-24.78 if 7<=z<=29 else 4.5739}')
  action(f'Place {block} on supplied64RPM shaft',f'execute if block {x} 98 {z} coasterslaunchsystems:{block}[axis=x]','Test passed')
elif sys.argv[1]=='controller':
 item('coasterslaunchsystems:ride_controller 1');aim(4.5,98,8.5,4.5,98,6.5)
 action('Place ride controller','execute if block 4 98 8 coasterslaunchsystems:ride_controller','Test passed')
elif sys.argv[1]=='bind':
 item('coasterslaunchsystems:span_tool 1')
 for x,z,ctl in [(-1,8,False)]+[(1,z,False) for z in [8]+list(range(23,44,2))]+[(4,8,True)]:
  tag='from' if ctl else 'ls_from'
  if 'has the following block data' in command(f'data get block {x} 98 {z} {tag}'):continue
  for i,az in enumerate([0,44]):
   pose(f'3.5 100 {az+.5} 90 20.472')
   action('Select directed anchor', 'data get entity Dev SelectedItem','coasterslaunchsystems:span_'+('from' if i==0 else 'to'))
  aim(x+.5,98.5,z+.5,x+.5,98 if ctl else 96,z-1.5)
  action('Bind module/controller',f'data get block {x} 98 {z} {tag}','has the following block data')
elif sys.argv[1]=='link':
 item('coasterslaunchsystems:span_tool 1');command('item replace entity Dev weapon.offhand with air');aim(4.5,98.7,8.5,4.5,98,6.5)
 action('Shift-select controller','data get entity Dev SelectedItem','coasterslaunchsystems:controller_selection',modifier='Shift_L')
 for x,z in [(-1,8)]+[(1,z) for z in [8]+list(range(23,44,2))]:
  aim(x+.5,98.5,z+.5,x+.5,96,z-1.5)
  action('Link cabinet or fin',f'data get block {x} 98 {z} ls_controller','[I;')
elif sys.argv[1]=='arm':
 item('minecraft:air');command('item replace entity Dev weapon.offhand with air')
 for x,z in [(-1,8)]+[(1,z) for z in [8]+list(range(23,44,2))]:
  aim(x+.5,98.7,z+.5,x+.5,96,z-1.5)
  action('Empty-hand Shift arms linked module',f'data get block {x} 98 {z} ls_armed','1b',modifier='Shift_L')
elif sys.argv[1]=='arm-controller':
 item('air');aim(4.5,98.7,8.5,4.5,98,6.5)
 action('Inspect and arm controller','data get block 4 98 8 armed','1b',modifier='Shift_L')
 print(command('data get block 4 98 8'))
