# Brief for the survival agent (read fully)

You port RLCraft 2.9.3's survival mods to NeoForge 1.21.1 for one player (NukeClips), personal use only: **Simple Difficulty 0.3.9**
(thirst + temperature), **First Aid 1.6.22** (body-part health), **No Tree Punching 2.0.21**. World gen, Ice and Fire 1.7.1 and (another
agent, in progress) Lycanites are already in this repo (mod `rlgen/`; read `CLAUDE.md` and `AUDIT.md` at the repo root, and skim how Ice and
Fire was ported: `rlgen/src/main/java/com/github/alexthe666/iceandfire`, `net/ilexiconn/llibrary`).

## THE rule (the user insists on it)
Port the OLD 1.12 code and behaviour with RLCraft's config values ($S/pack/x/overrides/config: simpledifficulty.cfg + simpledifficulty/,
firstaid.cfg, notreepunching.cfg; RLCraft CraftTweaker/LootTweaker scripts in $S/pack/x/overrides/scripts). Never copy behaviour, numbers,
GUIs or assets from modern versions of these mods. Use 1.21 code only for how to call the 1.21 API. Keep 1.12 quirks.

## Setup
- `git reset --hard origin/claude/sweet-cannon-iqmaol` first.
- Each mod is its own NeoForge mod in the same jar with its ORIGINAL id (`simpledifficulty`, `firstaid`, `notreepunching`): own `@Mod`
  class, own `[[mods]]` entry in neoforge.mods.toml, own DeferredRegisters, 1.12 package/class names kept (whatever the decompiled jars
  use). Only ADD lines to shared files.
- rlgen already generates No Tree Punching loose rocks as `rlgen:loose_rock_*` blocks (`com.nukevscity.rlgen.ntp`): switch that world gen
  to the real notreepunching rock blocks and remove the stand-ins.
- 1.12 capabilities become 1.21 data attachments; 1.12 Forge events map to NeoForge events.

## Branch
`survival`: push at least every 20-30 minutes and after each mod (`git push -u origin HEAD:survival`). Usage limits or a container restart
can stop you at any moment; unpushed work can be lost. Order: Simple Difficulty, then First Aid, then No Tree Punching.

## Shared resources (read-only, outside your worktree)
S=/tmp/claude-0/-home-user-rlcft1211/981bfb13-b982-50dc-a2f7-80d7a2ebfa64/scratchpad ; P=$S/par
- Decompiled with MCP names + assets: $S/dec/SimpleDifficulty, $S/dec/firstaid, $S/dec/notreepunching (jars in $S/jars). When decompiled
  code looks wrong, check the bytecode with `javap -c -p` (decompilers make mistakes). Other RLCraft jars: $S/pack/names.tsv (projectID,
  fileID, file); download via the CurseForge api/v1/mods/<p>/files/<f>/download redirect, decompile with $S/tools/vineflower.jar, then
  `python3 $S/scripts/srg2mcp.py $S/mc/stable <dir>`.
- 1.12.2 vanilla source (MCP names): $S/mc/src. 1.21.1 + NeoForge source: $S/mcsrc. RLCraft pack: $S/pack/x (configs in overrides/config,
  scripts in overrides/scripts).
- Region tools: `python3 $P/area.py $P <run>/world/region x0 y0 z0 x1 y1 z1`, $P/readmca.py.

## Machine rules
- Build ONLY with `$P/build.sh <your worktree>/rlgen [gradle args]`. Never call ./gradlew directly.
- Dedicated server tests ONLY with `$P/withserver.sh <your worktree>/rlgen <log> <test script> [seed]` (machine-wide lock, one server or
  client at a time; keep sessions under ~10 min; RCON via `python3 $P/rcon.py "cmd" ...`).
- Client tests: `$P/withclient.sh <your worktree>/rlgen <log> <world dir> <out prefix> [secs] [shots]` (real client on a virtual
  screen joined into a copy of <world dir>, e.g. <worktree>/rlgen/run/world made by withserver.sh, with a datapack whose
  #minecraft:tick function sets up the scene once a player exists, guarded by a scoreboard flag). Game window 854x480 in the middle of
  the 1280x720 screenshot. Look at the PNGs with the Read tool.
- Never start servers/clients any other way, never kill java/gradle/Xvfb processes you did not start through these scripts.
- Commit often, messages ending with exactly:
    Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
    Claude-Session: https://claude.ai/code/session_01UGB4AErBBhcoyLTW3XR5EJ
  Do not bump the version, do not edit CLAUDE.md/AUDIT.md. No model names in commits or code. Style: 3-space indent; comments cite the
  1.12 class being ported.

## Report (short, at the end)
What is ported and how faithful (anything approximate or missing), how you tested (server results, screenshots you looked at), stubs
left, branch and last pushed commit.
