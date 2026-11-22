exec(open('/mnt/NVME4/launchsystems-runs/ls02-linear-20261122/construct.py').read().split("if sys.argv[1]=='anchors':")[0])
command('fill 11 96 -3 22 96 44 stone');command('fill 19 99 -2 19 99 42 stone')
command('item replace entity Dev hotbar.0 with simulatedcoasters:coaster_anchorpoint 2')
for z in [0,40]:
 command(f'fill 16 97 {z} 16 99 {z} stone');command(f'tp Dev 19.5 100 {z+.5} 90 28.369');time.sleep(.25)
 action(f'Place skid-line anchor{z}',f'execute if block 16 100 {z} simulatedcoasters:coaster_anchorpoint[facing=up]','Test passed')
command('item replace entity Dev hotbar.0 with simulatedcoasters:coaster_track 64')
for i,z in enumerate([0,40]):
 command(f'tp Dev 19.5 100 {z+.5} 90 20.472');time.sleep(.25)
 action('Connect skid line' if i else 'Select skid start','data get block 16 100 0 AnchorPeerCurves' if i else 'data get entity Dev SelectedItem','Bezier' if i else 'track_connecting_from',modifier='Alt_L' if i else None)
for kind,x,z in [('tire_drive',15,8),('fin_brake',17,8),('skid_brake',17,24)]:
 command(f'item replace entity Dev hotbar.0 with coasterslaunchsystems:{kind}')
 command(f'setblock {x-1 if x==15 else x+1} 97 {z} stone');command(f'setblock {x-1 if x==15 else x+1} 98 {z} create:creative_motor[facing={"east" if x==15 else "west"}]{{Speed:{64 if kind=="tire_drive" else 0}.0f,ScrollValue:{64 if kind=="tire_drive" else 0}}}')
 command(f'tp Dev 16.5 97 {z+.5} {90 if x==15 else -90} 4.5739');time.sleep(.25)
 action(f'Place skid demonstration {kind}',f'execute if block {x} 98 {z} coasterslaunchsystems:{kind}','Test passed')
 command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool')
 for i,az in enumerate([0,40]):
  command(f'tp Dev 19.5 100 {az+.5} 90 20.472');time.sleep(.25)
  action('Select skid route endpoint','data get entity Dev SelectedItem','span_from' if i==0 else 'span_to')
 command(f'tp Dev 16.5 97 {z+.5} {90 if x==15 else -90} 13.4957');time.sleep(.25)
 action(f'Bind {kind}',f'data get block {x} 98 {z} ls_from','has the following')
 if kind!='tire_drive':
  command('item replace entity Dev hotbar.0 with minecraft:air');command(f'tp Dev 16.5 97 {z+.5} -90 -24.7024');time.sleep(.25)
  action(f'Arm {kind}',f'data get block {x} 98 {z} ls_armed','1b',modifier='Shift_L')
