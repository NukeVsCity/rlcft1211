#!/usr/bin/env python3
"""Converts the Lycanites Mobs 1.12 blockstates to 1.21.
usage: lyc_blockstates.py <1.12 assets/lycanitesmobs/blockstates> <out blockstates dir> <out models/block dir>
- model names get the 1.21 "block/" folder (1.12 blockstates named models/block/<x> as <modid>:<x>);
- variant "normal" becomes "";
- slabs (1.12 "half=bottom/top,variant=default", the double slab a separate block): 1.21 SlabBlock type=bottom/top/double,
  double = the 1.12 <name>_double block's model (y axis) or the full block the slab was cut from (BlockMaker: the brick and
  tile slabs used their full block as the double slab);
- walls (1.12 multipart up/north... = "true"): 1.21 WallBlock up=true, north=low|tall (1.12 had no tall walls, so both
  use the 1.12 side model);
- the Forge fluid blockstate becomes one blockstate per fluid block with a particle-only model (1.21 draws fluids itself);
- everything else (stairs, fences, pillars, facing blocks, fires, webs) keeps its variants/multipart."""
import json, os, sys

src, out, models = sys.argv[1], sys.argv[2], sys.argv[3]
os.makedirs(out, exist_ok=True)

def fixmodel(m):
   ns, _, path = m.partition(':') if ':' in m else ('minecraft', '', m)
   if not path.startswith('block/'):
      path = 'block/' + path
   return ns + ':' + path

def fixapply(a):
   if isinstance(a, list):
      return [fixapply(x) for x in a]
   a = dict(a)
   if 'model' in a:
      a['model'] = fixmodel(a['model'])
   return a

def first_model(j):
   v = j.get('variants', {})
   for key in ('normal', 'axis=y', ''):
      if key in v:
         a = v[key]
         return (a[0] if isinstance(a, list) else a)['model']
   return None

files = {f[:-5]: json.load(open(os.path.join(src, f))) for f in os.listdir(src) if f.endswith('.json')}
for name, j in files.items():
   if name == 'fluid':
      for fluid in j['variants']:
         json.dump({'variants': {'': {'model': 'lycanitesmobs:block/' + fluid}}}, open(os.path.join(out, fluid + '.json'), 'w'), indent=1)
         json.dump({'textures': {'particle': 'lycanitesmobs:blocks/' + fluid + '_still'}}, open(os.path.join(models, fluid + '.json'), 'w'), indent=1)
      continue
   if name.endswith('_slab') and 'variants' in j:
      v = j['variants']
      bottom = v['half=bottom,variant=default']
      top = v['half=top,variant=default']
      double = None
      if name + '_double' in files:
         double = first_model(files[name + '_double'])
      elif name[:-5] in files:
         double = first_model(files[name[:-5]])
      res = {'type=bottom': fixapply(bottom), 'type=top': fixapply(top)}
      if double:
         res['type=double'] = {'model': fixmodel(double)}
      json.dump({'variants': res}, open(os.path.join(out, name + '.json'), 'w'), indent=1)
      continue
   if name.endswith('_wall') and 'multipart' in j:
      parts = []
      for p in j['multipart']:
         when = p.get('when', {})
         newwhen = {}
         for k, val in when.items():
            if k == 'up':
               newwhen['up'] = str(val).lower()
            else:
               newwhen[k] = 'low|tall'
         parts.append({'when': newwhen, 'apply': fixapply(p['apply'])} if newwhen else {'apply': fixapply(p['apply'])})
      json.dump({'multipart': parts}, open(os.path.join(out, name + '.json'), 'w'), indent=1)
      continue
   if 'variants' in j:
      res = {}
      for k, a in j['variants'].items():
         if k == 'normal' and len(j['variants']) > 1:
            continue  # 1.12 item variant next to the real ones (1.21: overlapping definition)
         res['' if k == 'normal' else k] = fixapply(a)
      j = {'variants': res}
   elif 'multipart' in j:
      parts = []
      for p in j['multipart']:
         q = dict(p)
         q['apply'] = fixapply(p['apply'])
         parts.append(q)
      j = {'multipart': parts}
   json.dump(j, open(os.path.join(out, name + '.json'), 'w'), indent=1)
print(len(files), 'blockstates')
