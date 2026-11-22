exec(open('/mnt/NVME4/launchsystems-runs/ls02-linear-20261122/ctl.py').read().split("if sys.argv[1]=='console':")[0])
import argparse,time

def command(s):
 out=m.rcon(r.c,s)
 with (r.evidence/'construction-readbacks.jsonl').open('a') as f: f.write(json.dumps({'command':s,'result':out})+'\n')
 return out

def action(expect,check,contains,modifier=None):
 result=r.action(argparse.Namespace(action='use',values=[],expect=expect,menu=False,modifier=modifier))
 readback=command(check)
 print(expect+' => '+readback,flush=True)
 if contains not in readback: raise RuntimeError('Action did not reach expected state; stopped')
 return readback

if sys.argv[1]=='anchors':
 for z in [0,64]:
  command(f'tp Dev 3.5 100 {z+.5} 90 28.369');time.sleep(.2)
  action(f'Place floor-up anchor 0,100,{z}',f'execute if block 0 100 {z} simulatedcoasters:coaster_anchorpoint[facing=up]','Test passed')
elif sys.argv[1]=='track':
 command('item replace entity Dev hotbar.0 with simulatedcoasters:coaster_track 64')
 for i,z in enumerate([0,40]):
  command(f'tp Dev 3.5 100 {z+.5} 90 20.472');time.sleep(.2)
  action('Select start anchor' if i==0 else 'Connect straight40m using Alt', 'data get entity Dev SelectedItem' if i==0 else 'data get block 0 100 0 AnchorPeerCurves','track_connecting_from' if i==0 else 'Bezier',modifier=None if i==0 else 'Alt_L')

elif sys.argv[1]=='modules':
 for z in range(2,40,2):
  kinds=[]
  if 2<=z<=36:kinds.append(('lsm_drive' if z%4==0 else 'lim_drive',-1))
  if z<=10 or z>=30:kinds.append(('fin_brake',1))
  if z==24:kinds.append(('skid_brake',1))
  for block,side in kinds:
   if 'Test passed' in command(f'execute if block {side} 98 {z} coasterslaunchsystems:{block}'): continue
   command(f'item replace entity Dev hotbar.0 with coasterslaunchsystems:{block} 1')
   command(f'setblock {side*2} 97 {z} stone');command(f'setblock {side*2} 98 {z} create:creative_motor[facing={"east" if side<0 else "west"}]{{Speed:64.0f,ScrollValue:64}}')
   command(f'tp Dev .5 97 {z+.5} {90 if side<0 else -90} 4.5739');time.sleep(.25)
   action(f'Normal place {block} {side},98,{z}',f'execute if block {side} 98 {z} coasterslaunchsystems:{block}[axis=x]','Test passed')
elif sys.argv[1]=='controller':
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:ride_controller 1');command('setblock 5 97 8 stone');command('tp Dev 5.5 97 6.5 0 17.223');time.sleep(.25)
 action('Place Survival local swing controller','execute if block 5 98 8 coasterslaunchsystems:ride_controller','Test passed')
elif sys.argv[1]=='bind':
 specs=[(5,8,'controller')]+[(-1,z,'drive') for z in range(2,38,2)]+[(1,z,'fin') for z in list(range(2,12,2))+list(range(30,40,2))]+[(1,24,'skid')]
 for x,z,kind in specs:
  tag='from' if kind=='controller' else 'ls_from'
  if 'has the following block data' in command(f'data get block {x} 98 {z} {tag}'):continue
  command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool 1')
  for i,az in enumerate([0,40]):
   command(f'tp Dev 3.5 100 {az+.5} 90 20.472');time.sleep(.25)
   action(f'Select endpoint for {kind} {x},{z}','data get entity Dev SelectedItem','span_'+('from' if i==0 else 'to'))
  command('tp Dev -.5 97 .5 0 3.433' if x==-1 and z==2 else f'tp Dev {5.5 if kind=="controller" else .5} 97 {6.5 if kind=="controller" else z+.5} {0 if kind=="controller" else 90 if x<0 else -90} {4.5739 if kind=="controller" else 13.4957}');time.sleep(.25)
  action(f'Bind {kind} {x},98,{z}',f'data get block {x} 98 {z} {tag}','has the following block data')
elif sys.argv[1]=='link':
 command('item replace entity Dev hotbar.0 with minecraft:air');command('tp Dev 1.5 97 .5 0 -6.56');time.sleep(.25)
 action('Disarm temporary safety fin before controller linking','data get block 1 98 2 ls_armed','0b',modifier='Shift_L')
 command('setblock 2 98 2 create:creative_motor[facing=west]{Speed:64.0f,ScrollValue:64}')
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool 1');command('tp Dev 5.5 97 6.5 0 -8.717');time.sleep(.25)
 action('Select bound controller for actual module links','data get entity Dev SelectedItem','controller_selection',modifier='Shift_L')
 for x,z in [(-1,z) for z in range(2,38,2)]+[(1,z) for z in list(range(2,12,2))+list(range(30,40,2))]:
  command('tp Dev 1.5 97 .5 0 3.433' if x==1 and z==2 else f'tp Dev .5 97 {z+.5} {90 if x<0 else -90} 13.4957');time.sleep(.25)
  action(f'Link module{x},98,{z}',f'data get block {x} 98 {z} ls_controller','[I;')
elif sys.argv[1]=='swing':
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool 1');command('item replace entity Dev weapon.offhand with minecraft:compass');command('tp Dev 5.5 97 6.5 0 4.5739');time.sleep(.25)
 action('Select forward-backward-forward swing through actual Compass tool interaction','data get block 5 98 8 swing_program','1b')
 command('item replace entity Dev weapon.offhand with minecraft:air')
elif sys.argv[1]=='arm':
 command('item replace entity Dev hotbar.0 with minecraft:air')
 for x,z in [(-1,z) for z in range(2,38,2)]+[(1,z) for z in list(range(2,12,2))+list(range(30,40,2))]:
  if '1b' in command(f'data get block {x} 98 {z} ls_armed'): continue
  command(f'tp Dev {x+.5} 97 .5 0 -6.56' if z==2 else f'tp Dev .5 97 {z+.5} {90 if x<0 else -90} -24.7024');time.sleep(.25)
  action(f'Arm module{x},98,{z}',f'data get block {x} 98 {z} ls_armed','1b',modifier='Shift_L')

elif sys.argv[1]=='link-start':
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool 1');command('tp Dev 5.5 97 6.5 0 -8.717');time.sleep(.25)
 action('Select controller for upstream field extension','data get entity Dev SelectedItem','controller_selection',modifier='Shift_L')
 for z in [2,4,6]:
  command(f'tp Dev -.5 97 {z-1.5} 0 3.433');time.sleep(.25)
  action(f'Link upstream field -1,98,{z}',f'data get block -1 98 {z} ls_controller','[I;')
