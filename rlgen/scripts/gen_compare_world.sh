#!/bin/bash
# gen_compare_world.sh - generate a block box of an rlgen world on the dev server and copy its region files out,
# as the 1.21 side for compare112.py.
#
# usage: gen_compare_world.sh <seed> <x0> <z0> <x1> <z1> <out dir> [rlgen dir]
#   seed           the 1.12 world seed (compare112.py prints it from level.dat)
#   x0 z0 x1 z1    block box (every chunk touching it is force-loaded to "full")
#   out dir        gets region/, entities/ and level.dat of the generated world
#   rlgen dir      the rlgen project to run (default: the one this script is in)
# Then: python3 compare112.py <1.12 save> <out dir> <report dir> --box x0 z0 x1 z1
#
# Uses the machine's dev-server wrappers (PAR, default the shared scratchpad par/ folder): setup_run.sh copies the run
# dir if missing, withserver.sh waits for the one-server-at-a-time lock, wipes run/world, starts runServer with the seed
# (level-type rlgen:rlcraft_legacy) and stops it afterwards. The box is grown by MARGIN chunks (default 1) so trees and
# other decoration that spill in from neighbour chunks are there on the box edges too. Chunks are force-loaded in batches of at most 15x15,
# each batch is waited for with "execute if loaded" and released again. Build the mod first if the code changed.
set -e
PAR=${PAR:-/tmp/claude-0/-home-user-rlcft1211/981bfb13-b982-50dc-a2f7-80d7a2ebfa64/scratchpad/par}
[ $# -ge 6 ] || { sed -n '2,16p' "$0"; exit 1; }
SEED=$1; X0=$2; Z0=$3; X1=$4; Z1=$5; OUT=$6
MARGIN=${MARGIN:-1}
RL=$(cd "${7:-$(dirname "$0")/..}" && pwd)
mkdir -p "$OUT"; OUT=$(cd "$OUT" && pwd)
[ -d "$RL/run" ] || bash "$PAR/setup_run.sh" "$RL"
P="$RL/run/server.properties"
grep -q '^level-type=' "$P" && sed -i 's/^level-type=.*/level-type=rlgen\\:rlcraft_legacy/' "$P" || echo 'level-type=rlgen\:rlcraft_legacy' >> "$P"

T=$(mktemp -d)
cat > "$T/test.sh" <<EOF
python3 - $X0 $Z0 $X1 $Z1 $MARGIN <<'PY'
import socket, struct, sys, time
def pkt(s, i, t, body):
    d = struct.pack('<ii', i, t) + body.encode() + b'\x00\x00'
    s.sendall(struct.pack('<i', len(d)) + d)
def recv(s):
    ln = struct.unpack('<i', s.recv(4))[0]
    d = b''
    while len(d) < ln: d += s.recv(ln - len(d))
    return d[8:-2].decode(errors='replace')
s = socket.create_connection(('127.0.0.1', 25575), timeout=600)
pkt(s, 1, 3, 'test'); recv(s)
def cmd(c):
    pkt(s, 2, 2, c); return recv(s)
x0, z0, x1, z1, m = map(int, sys.argv[1:6])
cx0, cx1 = (min(x0, x1) >> 4) - m, (max(x0, x1) >> 4) + m
cz0, cz1 = (min(z0, z1) >> 4) - m, (max(z0, z1) >> 4) + m
B = 15
batches = [(a, b, min(a + B - 1, cx1), min(b + B - 1, cz1)) for b in range(cz0, cz1 + 1, B) for a in range(cx0, cx1 + 1, B)]
t0 = time.time()
for n, (a, b, c, d) in enumerate(batches):
    print(cmd(f"forceload add {a * 16} {b * 16} {c * 16 + 15} {d * 16 + 15}"), flush=True)
    todo = [(x, z) for x in range(a, c + 1) for z in range(b, d + 1)]
    for i in range(600):
        todo = [(x, z) for (x, z) in todo if "passed" not in cmd(f"execute if loaded {x * 16} 64 {z * 16}")]
        if not todo: break
        time.sleep(2)
    print(f"batch {n + 1}/{len(batches)} chunks {a},{b}..{c},{d}: {'done' if not todo else str(len(todo)) + ' not loaded'}"
          f" ({time.time() - t0:.0f} s)", flush=True)
    cmd(f"forceload remove {a * 16} {b * 16} {c * 16 + 15} {d * 16 + 15}")
print(cmd("save-all flush"))
PY
EOF
bash "$PAR/withserver.sh" "$RL" "$T/server.log" "$T/test.sh" "$SEED" || { echo "server run failed, log: $T/server.log"; exit 1; }

W="$RL/run/world"
mkdir -p "$OUT/region" "$OUT/entities"
M=$((MARGIN * 16))
for rx in $(seq $(( ((X0<X1?X0:X1) - M) >> 9 )) $(( ((X0>X1?X0:X1) + M) >> 9 ))); do
  for rz in $(seq $(( ((Z0<Z1?Z0:Z1) - M) >> 9 )) $(( ((Z0>Z1?Z0:Z1) + M) >> 9 ))); do
    for d in region entities; do [ -f "$W/$d/r.$rx.$rz.mca" ] && cp "$W/$d/r.$rx.$rz.mca" "$OUT/$d/"; done
  done
done
cp "$W/level.dat" "$OUT/"
echo "region files in $OUT/region (server log $T/server.log)"
ls -la "$OUT/region"
