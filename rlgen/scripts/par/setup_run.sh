#!/bin/bash
# usage: setup_run.sh <rlgen dir>  -- creates the dev run dir (eula, server.properties with RCON + RLCraft Legacy world type, Dynamic Trees in mods)
D="$1/run"; mkdir -p "$D/mods"
echo "eula=true" > "$D/eula.txt"
cat > "$D/server.properties" <<P
enable-rcon=true
rcon.password=test
rcon.port=25575
broadcast-rcon-to-ops=true
online-mode=false
level-seed=-6377475158484458490
level-type=rlgen\:rlcraft_legacy
spawn-protection=0
view-distance=6
simulation-distance=6
P
J=$(find ~/.gradle -name "dynamictrees-1.7.2*.jar" 2>/dev/null | head -1); [ -n "$J" ] && cp "$J" "$D/mods/dynamictrees-neoforge-1.21.1-1.7.2.jar"
echo "run dir ready: $D"
