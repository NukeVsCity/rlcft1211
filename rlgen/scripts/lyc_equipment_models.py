#!/usr/bin/env python3
"""Item models for the Lycanites equipment items: 1.12 gave every ItemEquipmentPart and ItemEquipment the built-in
"lycanitesmobs:equipmentpart" model (a TEISR, no display transforms); 1.21: {"parent": "builtin/entity"}.
usage: lyc_equipment_models.py <out models/item dir> <equipment json dir>..."""
import json, glob, os, sys

out = sys.argv[1]
names = {'equipment'}
for d in sys.argv[2:]:
   for f in glob.glob(os.path.join(d, '*.json')):
      try:
         j = json.load(open(f))
      except Exception:
         continue
      if 'itemName' in j:
         names.add(j['itemName'].lower())
for name in sorted(names):
   json.dump({'parent': 'builtin/entity'}, open(os.path.join(out, name + '.json'), 'w'))
print(len(names), 'equipment item models')
