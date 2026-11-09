#!/bin/bash
# Rebuilds the scratch workspace after a container reset: RLCraft 2.9.3 pack (configs, manifest), 1.12 mod jars from the manifest,
# the 1.12.2 client decompiled with MCP stable_39 names, Ice and Fire + llibrary decompiled, and the par/ test scripts.
# usage: setup_workspace.sh <scratch dir> [projectID ...]   (default projects: Ice and Fire 264231, llibrary 243298)
set -e
S="$1"; shift; IDS="${*:-264231 243298}"; R=$(cd "$(dirname "$0")" && pwd)
mkdir -p $S/pack/x $S/jars $S/tools $S/mc/stable $S/mc/srg $S/dec $S/par $S/scripts
cp $R/par/* $S/par/; cp $R/srg2mcp.py $S/scripts/; sed -i "s#^P=.*#P=$S/par#" $S/par/*.sh
cd $S/pack; [ -f rl.zip ] || curl -sSL -o rl.zip "https://mediafilez.forgecdn.net/files/4612/979/RLCraft%201.12.2%20-%20Release%20v2.9.3.zip"
(cd x && unzip -q -o ../rl.zip)
python3 -c "import json;[print(f['projectID'],f['fileID']) for f in json.load(open('x/manifest.json'))['files']]" > ids.txt
cd $S/jars; for p in $IDS; do f=$(grep "^$p " $S/pack/ids.txt | cut -d' ' -f2); u=$(curl -sSIL "https://www.curseforge.com/api/v1/mods/$p/files/$f/download" | grep -i '^location' | tail -1 | tr -d '\r' | awk '{print $2}'); curl -sSL -o "$(basename "$u")" "$u"; done
cd $S/tools; [ -f vineflower.jar ] || curl -sSL -o vineflower.jar https://repo1.maven.org/maven2/org/vineflower/vineflower/1.10.1/vineflower-1.10.1.jar
[ -f specialsource.jar ] || curl -sSL -o specialsource.jar https://repo1.maven.org/maven2/net/md-5/SpecialSource/1.11.4/SpecialSource-1.11.4-shaded.jar
cd $S/mc; curl -sSL -o stable.zip https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/39-1.12/mcp_stable-39-1.12.zip; unzip -q -o stable.zip -d stable
curl -sSL -o srg.zip https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.12.2/mcp-1.12.2-srg.zip; unzip -q -o srg.zip -d srg
U=$(curl -sS https://piston-meta.mojang.com/mc/game/version_manifest_v2.json | python3 -c "import json,sys;print([v['url'] for v in json.load(sys.stdin)['versions'] if v['id']=='1.12.2'][0])")
curl -sS "$U" -o 1.12.2.json; curl -sSL -o client.jar $(python3 -c "import json;print(json.load(open('1.12.2.json'))['downloads']['client']['url'])")
java -jar $S/tools/specialsource.jar --in-jar client.jar --out-jar client-srg.jar --srg-in srg/joined.srg -q
python3 -c "
import zipfile
z=zipfile.ZipFile('client-srg.jar'); o=zipfile.ZipFile('client-srg-mc.jar','w')
[o.writestr(n,z.read(n)) for n in z.namelist() if n.startswith('net/minecraft/') and n.endswith('.class')]"
java -Xmx3g -jar $S/tools/vineflower.jar -log=WARN client-srg-mc.jar src >/dev/null; python3 $S/scripts/srg2mcp.py stable src
cd $S/dec; for j in $S/jars/*.jar; do n=$(basename $j .jar); java -Xmx2g -jar $S/tools/vineflower.jar -log=WARN $j $n >/dev/null; python3 $S/scripts/srg2mcp.py $S/mc/stable $n; done
pip install -q nbtlib pillow numpy
echo "workspace ready in $S"
