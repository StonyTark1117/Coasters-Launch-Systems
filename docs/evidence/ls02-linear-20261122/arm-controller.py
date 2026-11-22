exec(open('/mnt/NVME4/launchsystems-runs/ls02-linear-20261122/construct.py').read().split("if sys.argv[1]=='anchors':")[0])
command('item replace entity Dev hotbar.0 with minecraft:air');command('tp Dev 5.5 97 6.5 0 -8.717');time.sleep(.25)
action('Arm completed mixed swing controller','data get block 5 98 8 armed','1b',modifier='Shift_L')
print(command('data get block 5 98 8'))
