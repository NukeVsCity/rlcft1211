#!/bin/bash
# usage: withserver.sh <rlgen dir> <log file> <test script> [seed]
# Waits for the machine-wide lock (one dev server/client at a time), wipes <rlgen dir>/run/world, starts runServer
# (RCON 127.0.0.1:25575 password test), runs <test script> (use python3 $P/rcon.py "cmd" ...), then always stops the server.
P=/tmp/claude-0/-home-user-rlcft1211/981bfb13-b982-50dc-a2f7-80d7a2ebfa64/scratchpad/par
D="$1"; LOG="$2"; T="$3"; SEED="$4"
exec 9>"$P/server.lock"; flock 9
pids() { ps -eo pid,args | grep "[s]erverRunProgramArgs" | awk '{print $1}'; }
stop() { python3 $P/rcon.py stop >/dev/null 2>&1; for i in $(seq 1 30); do [ -z "$(pids)" ] && break; sleep 2; done; for p in $(pids); do kill -9 $p; done; }
stop
[ -d "$D/run" ] || "$P/setup_run.sh" "$D"
[ -n "$SEED" ] && sed -i "s/^level-seed=.*/level-seed=$SEED/" "$D/run/server.properties"
rm -rf "$D/run/world"
(cd "$D" && nohup timeout 2400 ./gradlew runServer --console=plain > "$LOG" 2>&1 9>&- &)
for i in $(seq 1 200); do grep -q "RCON running\|Crash\|FAILED\|Exception in" "$LOG" 2>/dev/null && break; sleep 3; done
grep -n "Crash\|FAILED\|Exception in" "$LOG" | head -5
if grep -q "RCON running" "$LOG"; then bash "$T"; rc=$?; else echo "server did not start, see $LOG"; rc=1; fi
stop
exit $rc
