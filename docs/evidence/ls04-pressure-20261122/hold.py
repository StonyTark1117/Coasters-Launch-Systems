import importlib.util,subprocess,time,json,sys,datetime
from pathlib import Path
spec=importlib.util.spec_from_file_location('p','/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
r=m.PrivateRun('/mnt/NVME4/launchsystems-runs/ls04-pressure-20261122/run.json','ls04-pressure-foundation-20261122');w=r.window();r.options();r.connected();key=sys.argv[1];duration=float(sys.argv[2]);expect=sys.argv[3];assert 0<duration<=3
stamp=datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%S.%fZ');record={'at':stamp,'action':'hold '+key,'seconds':duration,'expected':expect,'before':r.snapshot(stamp+'-before')}
subprocess.run(['xdotool','windowfocus','--sync',w],env=r.env,check=True)
down=['mousedown','1'] if key=='attack' else ['keydown','--window',w,key];up=['mouseup','1'] if key=='attack' else ['keyup','--window',w,key]
try:subprocess.run(['xdotool',*down],env=r.env,check=True);time.sleep(duration)
finally:subprocess.run(['xdotool',*up],env=r.env,check=True)
time.sleep(.35);record['after']=r.snapshot(stamp+'-after');r.log_input(record);print(json.dumps(record))
