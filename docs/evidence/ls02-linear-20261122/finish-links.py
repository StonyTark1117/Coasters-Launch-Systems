exec(open('/mnt/NVME4/launchsystems-runs/ls02-linear-20261122/construct.py').read().split("if sys.argv[1]=='anchors':")[0])
for z in [4,6]:
 command('item replace entity Dev hotbar.0 with minecraft:air');command(f'tp Dev .5 97 {z+.5} 90 -24.7024');time.sleep(.25)
 action(f'Disarm newly added field {z} before linking',f'data get block -1 98 {z} ls_armed','0b',modifier='Shift_L')
command('item replace entity Dev hotbar.0 with coasterslaunchsystems:span_tool 1');command('tp Dev 5.5 97 6.5 0 -8.717');time.sleep(.25)
action('Select controller for remaining upstream links','data get entity Dev SelectedItem','controller_selection',modifier='Shift_L')
for z in [4,6]:
 command(f'tp Dev .5 97 {z+.5} 90 13.4957');time.sleep(.25)
 action(f'Link upstream field {z}',f'data get block -1 98 {z} ls_controller','[I;')
