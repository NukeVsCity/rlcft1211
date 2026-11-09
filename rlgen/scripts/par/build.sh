#!/bin/bash
# usage: build.sh <rlgen dir> [gradle args...]   (default: build -q). At most 2 gradle builds run at once on this machine.
P=/tmp/claude-0/-home-user-rlcft1211/981bfb13-b982-50dc-a2f7-80d7a2ebfa64/scratchpad/par
D="$1"; shift; [ $# -eq 0 ] && set -- build -q
while true; do
  for slot in 1 2; do
    exec {fd}>"$P/build$slot.lock"
    if flock -n $fd; then cd "$D" && ./gradlew "$@"; rc=$?; flock -u $fd; exit $rc; fi
    exec {fd}>&-
  done
  sleep 5
done
