#!/bin/bash
# usage: withclient.sh <rlgen dir> <log> <world dir> <out prefix> [seconds=90] [shots=3]
# Machine-wide lock; copies <world dir> (e.g. a world made with withserver.sh, <rlgen>/run/world, with a datapack whose
# #minecraft:tick function sets up the scene once a player exists) to <rlgen>/run/saves/shot, starts the client on a virtual
# screen joined straight into it, waits, saves <shots> screenshots <out prefix>-N.png every 10 s, then stops the client.
# The game window is 854x480 in the middle of the 1280x720 screenshot. Look at the PNGs with the Read tool.
P=/tmp/claude-0/-home-user-rlcft1211/981bfb13-b982-50dc-a2f7-80d7a2ebfa64/scratchpad/par
D="$1"; LOG="$2"; W="$3"; OUT="$4"; SECS="${5:-90}"; SHOTS="${6:-3}"
exec 9>"$P/server.lock"; flock 9
cpids() { ps -eo pid,args | grep "[c]lientRunProgramArgs" | awk '{print $1}'; }
for p in $(cpids); do kill $p; done
[ -d "$D/run" ] || "$P/setup_run.sh" "$D"
rm -rf "$D/run/saves/shot"; mkdir -p "$D/run/saves"; cp -r "$W" "$D/run/saves/shot"; rm -f "$D/run/saves/shot/session.lock"
O="$D/run/options.txt"; touch "$O"
for kv in onboardAccessibility:false pauseOnLostFocus:false skipMultiplayerWarning:true tutorialStep:none joinedFirstServer:true renderDistance:8 guiScale:2; do
  k=${kv%%:*}; grep -q "^$k:" "$O" && sed -i "s/^$k:.*/$kv/" "$O" || echo "$kv" >> "$O"; done
pgrep -x Xvfb >/dev/null || (Xvfb :99 -screen 0 1280x720x24 >/dev/null 2>&1 9>&- &); sleep 2
(cd "$D" && DISPLAY=:99 nohup timeout $((SECS+700)) ./gradlew runClient --console=plain -PclientArgs="--quickPlaySingleplayer shot" > "$LOG" 2>&1 9>&- &)
for i in $(seq 1 120); do grep -q "joined the game\|Crash\|FAILED\|Exception in" "$LOG" 2>/dev/null && break; sleep 5; done
grep -n "Crash\|FAILED\|Exception in" "$LOG" | head -5
sleep "$SECS"
for n in $(seq 1 $SHOTS); do DISPLAY=:99 import -window root "$OUT-$n.png"; sleep 10; done
for p in $(cpids); do kill $p; done; sleep 5; for p in $(cpids); do kill -9 $p; done
ls "$OUT"-*.png
