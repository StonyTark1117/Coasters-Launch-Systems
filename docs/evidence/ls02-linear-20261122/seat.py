exec(open('/mnt/NVME4/launchsystems-runs/ls02-linear-20261122/construct.py').read().split("if sys.argv[1]=='anchors':")[0])
command('setblock 3 101 0 stone');command('item replace entity Dev hotbar.0 with create:red_seat');command('tp Dev 3.5 102 .90010592 90 33.637');time.sleep(.25)
action('Place native Create seat on measured stopped bearing top','execute positioned 20481032 129 20481032 if block ~ ~ ~ create:red_seat','Test passed')
