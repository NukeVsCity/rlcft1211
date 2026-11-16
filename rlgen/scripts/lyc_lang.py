#!/usr/bin/env python3
"""Converts the Lycanites Mobs 1.12 .lang files to 1.21 lang JSON.
usage: lyc_lang.py <1.12 assets/lycanitesmobs/lang dir> <out assets/lycanitesmobs/lang dir> [RLCraft resources/lycanitesmobs/lang dir]
The RLCraft override lang files (ResourceLoader resources, loaded after the jar like a resource pack) win key by key.
Every 1.12 key is kept as is (Lycanites translates its own keys, e.g. entity.troll.description), with 1.12 %d/%f turned
into %s like 1.12 LanguageManager did; the 1.21 registry keys are added: entity.lycanitesmobs.<name>, item.lycanitesmobs.<name>,
block.lycanitesmobs.<name>, effect.lycanitesmobs.<name> from entity/item/tile/effect.<name>.name."""
import json, os, re, sys

src, out = sys.argv[1], sys.argv[2]
over = sys.argv[3] if len(sys.argv) > 3 else None
os.makedirs(out, exist_ok=True)
PAT = re.compile(r'%(\d+\$)?[\d\.]*[df]')
for f in sorted(os.listdir(src)):
   if not f.endswith('.lang'):
      continue
   data = {}
   files = [os.path.join(src, f)]
   if over and os.path.exists(os.path.join(over, f.lower())):
      files.append(os.path.join(over, f.lower()))
   for path in files:
      for line in open(path, encoding='utf-8', errors='replace'):
         line = line.rstrip('\n').rstrip('\r')
         if not line or line[0] == '#' or '=' not in line:
            continue
         k, v = line.split('=', 1)
         v = PAT.sub(lambda m: '%' + (m.group(1) or '') + 's', v)
         data[k] = v
   extra = {}
   for k, v in data.items():
      m = re.match(r'^(entity|item|tile|effect)\.([a-z0-9_]+)\.name$', k)
      if m:
         kind = {'tile': 'block'}.get(m.group(1), m.group(1))
         extra['%s.lycanitesmobs.%s' % (kind, m.group(2))] = v
   for k, v in extra.items():
      data.setdefault(k, v)
   json.dump(data, open(os.path.join(out, f[:-5].lower() + '.json'), 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
   print(f, len(data))
