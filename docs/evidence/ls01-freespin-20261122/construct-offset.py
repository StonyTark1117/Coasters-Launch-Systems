exec(open('/mnt/NVME4/launchsystems-runs/ls01-freespin-20261122/ctl.py').read().split("if sys.argv[1]=='console':")[0])
import argparse,time,re
# Prepared alternate construction only; not executed until diagnosis authorizes a changed setup.
X_OFFSET = 64

def command(s):
 for prefix in ('tp Dev ', 'setblock ', 'execute if block ', 'data get block '):
  if s.startswith(prefix):
   tail=s[len(prefix):].split(' ',1)
   s=prefix+str(float(tail[0])+X_OFFSET)+' '+tail[1] if prefix=='tp Dev ' else prefix+str(int(tail[0])+X_OFFSET)+' '+tail[1]
   break
 out=m.rcon(r.c,s)
 with (r.evidence/'construction-offset-readbacks.jsonl').open('a') as f: f.write(json.dumps({'command':s,'result':out})+'\n')
 return out

def action(expect,check,contains,modifier=None):
 result=r.action(argparse.Namespace(action='use',values=[],expect=expect,menu=False,modifier=modifier))
 readback=command(check)
 print(expect+' => '+readback,flush=True)
 if contains not in readback: raise RuntimeError('Action did not reach expected state; stopped')
 return readback

if sys.argv[1]=='anchors':
 for x,z in [(0,0),(0,32),(16,32),(16,0)]:
  command(f'tp Dev {x+3.5} 100 {z+.5} 90 28.369');time.sleep(.2)
  action(f'Place floor-up anchor {x},100,{z}',f'execute if block {x} 100 {z} simulatedcoasters:coaster_anchorpoint[facing=up]','Test passed')
elif sys.argv[1]=='connect':
 ax,az,bx,bz=map(int,sys.argv[2:])
 command(f'tp Dev {ax+3.5} 100 {az+.5} 90 20.472');time.sleep(.2)
 action('Select native start anchor', 'data get entity Dev SelectedItem','fourthdimension:track_selection')
 command(f'tp Dev {bx+3.5} 100 {bz+.5} 90 20.472');time.sleep(.2)
 action('Connect native rail with Alt suppressing neighbor smoothing',f'data get block {ax} 100 {az} AnchorPeerCurves','Bezier',modifier='Alt_L')
 print(command('data get entity Dev SelectedItem'),flush=True)
elif sys.argv[1]=='modules':
 lane=int(sys.argv[2]);kind=sys.argv[3]
 distances=sorted(set([4,6,8]+(list(range(11,32,2)) if kind=='tire' else list(range(13,32,2)))))
 block='tire_drive' if kind=='tire' else 'fin_brake'; side=-1 if kind=='tire' else 1
 command(f'item replace entity Dev hotbar.0 with coasterslaunchsystems:{block} 64')
 for distance in distances:
  x=lane+side; z=distance if lane==0 else 32-distance
  if 'Test passed' in command(f'execute if block {x} 98 {z} coasterslaunchsystems:{block}'): continue
  motor=lane+side*2
  command(f'setblock {motor} 97 {z} stone')
  command(f'setblock {motor} 98 {z} create:creative_motor[facing={"east" if side<0 else "west"}]{{Speed:64.0f,ScrollValue:64}}')
  command(f'tp Dev {lane+.5} 97 {z+.5} {90 if side<0 else -90} 4.5739');time.sleep(.2)
  action(f'Place {block} {x},98,{z} against supplied64RPM shaft',f'execute if block {x} 98 {z} coasterslaunchsystems:{block}[axis=x]','Test passed')
  readback=command(f'data get block {x} 98 {z} Speed'); print(readback,flush=True)
  if '64.0f' not in readback: raise RuntimeError('Power not reaching placed module')

elif sys.argv[1]=='controllers':
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:ride_controller 2')
 for x,z in [(5,4),(21,28)]:
  command(f'setblock {x} 97 {z} stone')
  command(f'tp Dev {x+.5} 97 {z-1.5} 0 17.223');time.sleep(.2)
  action(f'Place Survival ride controller {x},98,{z}',f'execute if block {x} 98 {z} coasterslaunchsystems:ride_controller','Test passed')
elif sys.argv[1]=='bind':
 lane=int(sys.argv[2]);kind=sys.argv[3]
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool 1')
 distances=[4] if kind=='controller' else sorted(set([4,6,8]+(list(range(11,32,2)) if kind=='tire' else list(range(13,32,2)))))
 for distance in distances:
  x=lane+(5 if kind=='controller' else -1 if kind=='tire' else 1);z=distance if lane==0 else 32-distance
  tag='from' if kind=='controller' else 'ls_from'
  if 'has the following block data' in command(f'data get block {x} 98 {z} {tag}'): continue
  for i,az in enumerate([0,32] if lane==0 else [32,0]):
   command(f'tp Dev {lane+3.5} 100 {az+.5} 90 20.472');time.sleep(.2)
   action(f'Span Tool selects {"start" if i==0 else "end"} anchor for {kind} {x},{z}', 'data get entity Dev SelectedItem','coasterslaunchsystems:span_'+('from' if i==0 else 'to'))
  if kind=='controller': command(f'tp Dev {x+.5} 97 {z-1.5} 0 4.5739')
  else: command(f'tp Dev {lane+.5} 97 {z+.5} {90 if kind=="tire" else -90} 13.4957')
  time.sleep(.2)
  action(f'Bind disarmed {kind} {x},98,{z} through real Span Tool',f'data get block {x} 98 {z} {tag}','has the following block data')
  print(command(f'data get block {x} 98 {z} '+('fault' if kind=='controller' else 'ls_fault')),flush=True)
elif sys.argv[1]=='link':
 lane=int(sys.argv[2]);cx=lane+5;cz=4 if lane==0 else 28
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool 1')
 command('item replace entity Dev weapon.offhand with minecraft:air')
 command(f'tp Dev {cx+.5} 97 {cz-1.5} 0 -8.717');time.sleep(.2)
 action('Shift-select controller for real module linking','data get entity Dev SelectedItem','coasterslaunchsystems:controller_selection',modifier='Shift_L')
 for kind in ['tire','fin']:
  for distance in sorted(set([4,6,8]+(list(range(11,32,2)) if kind=='tire' else list(range(13,32,2))))):
   x=lane+(-1 if kind=='tire' else 1);z=distance if lane==0 else 32-distance
   command('item replace entity Dev weapon.offhand with '+('minecraft:redstone_torch' if kind=='tire' and distance>=11 else 'minecraft:air'))
   command(f'tp Dev {lane+.5} 97 {z+.5} {90 if kind=="tire" else -90} 13.4957');time.sleep(.2)
   action(f'Link {kind} {x},98,{z} role={"exit" if kind=="tire" and distance>=11 else "inlet/hold"}',f'data get block {x} 98 {z} ls_controller','[I;')
 command('item replace entity Dev weapon.offhand with minecraft:air')
elif sys.argv[1]=='arm':
 lane=int(sys.argv[2]);command('item replace entity Dev hotbar.0 with minecraft:air');command('item replace entity Dev weapon.offhand with minecraft:air')
 for kind in ['tire','fin']:
  for distance in sorted(set([4,6,8]+(list(range(11,32,2)) if kind=='tire' else list(range(13,32,2))))):
   x=lane+(-1 if kind=='tire' else 1);z=distance if lane==0 else 32-distance
   command(f'tp Dev {lane+.5} 97 {z+.5} {90 if kind=="tire" else -90} -24.7024');time.sleep(.2)
   action(f'Empty-hand Shift arms linked {kind} {x},98,{z}',f'data get block {x} 98 {z} ls_armed','1b',modifier='Shift_L')
elif sys.argv[1]=='setup-control':
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool 1')
 for x,z,otherx,otherz in [(5,4,21,28),(21,28,5,4)]:
  command('item replace entity Dev weapon.offhand with minecraft:clock')
  command(f'tp Dev {x+.5} 97 {z-1.5} 0 4.5739');time.sleep(.2)
  action('Enable native station capture via Span Tool and offhand clock',f'data get block {x} 98 {z} station_capture','1b')
  command('item replace entity Dev weapon.offhand with minecraft:air')
  command(f'tp Dev {x+.5} 97 {z-1.5} 0 -8.717');time.sleep(.2)
  action('Select source controller to bind its destination','data get entity Dev SelectedItem','controller_selection',modifier='Shift_L')
  command(f'tp Dev {otherx+.5} 97 {otherz-1.5} 0 4.5739');time.sleep(.2)
  action('Bind downstream receiving controller by normal tool click',f'data get block {x} 98 {z} destination','has the following block data')
elif sys.argv[1]=='levers':
 command('item replace entity Dev hotbar.0 with minecraft:lever 2')
 command('item replace entity Dev weapon.offhand with minecraft:air')
 for x,z in [(5,4),(21,28)]:
  command(f'setblock {x+2} 99 {z} stone')
  command(f'tp Dev {x+2.5} 100 {z+.5} 90 48.6141');time.sleep(.2)
  action(f'Place real Survival input lever on controller at{x},99,{z}',f'execute if block {x} 99 {z} minecraft:lever[face=floor,powered=false]','Test passed',modifier='Shift_L')
elif sys.argv[1]=='arm-controller':
 x,z=map(int,sys.argv[2:]);command('item replace entity Dev hotbar.0 with minecraft:air');command('item replace entity Dev weapon.offhand with minecraft:air')
 command(f'tp Dev {x+.5} 97 {z-1.5} 0 -8.717');time.sleep(.2)
 action('Empty-hand Shift inspects and arms complete controller',f'data get block {x} 98 {z} armed','1b',modifier='Shift_L')
 
 for key in ['phase','fault','boarding_ready','reservation']: print(command(f'data get block {x} 98 {z} {key}'),flush=True)
elif sys.argv[1]=='signal':
 x,z=map(int,sys.argv[2:]);command('item replace entity Dev hotbar.0 with minecraft:air');command('item replace entity Dev weapon.offhand with minecraft:air')
 command(f'tp Dev {x+.5} 97 {z-1.5} 0 -14.8415');time.sleep(.2)
 before=command(f'execute if block {x} 99 {z} minecraft:lever[powered=true]');power='false' if 'Test passed' in before else 'true'
 action('Normal lever input changes power to'+power,f'execute if block {x} 99 {z} minecraft:lever[powered={power}]','Test passed')
 
 for key in ['phase','fault','boarding_ready','reservation']: print(command(f'data get block {x} 98 {z} {key}'),flush=True)
