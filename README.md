# HomesPlugin

A custom Paper plugin for personal homes with a full click-GUI – every
player manages only their own homes, and nobody can access another
player's homes.

## Features

- `/homes` – opens a 4-row GUI. Home boxes sit in a checkerboard pattern
  starting on the second row (one box per home slot you're allowed to have,
  see "Configuring home limits" below):
  - **Empty slot:** a green dye labeled "Click to sethome". Clicking it
    closes the GUI and asks you to type a name in chat; whatever you type
    becomes the new home, set at your current location.
  - **Occupied slot:** a **red bed** labeled with the home's name. Clicking
    it opens a small menu with **"TP to home"** and **"Delete home"**.
  - No coordinates are shown anywhere, neither in the GUI nor in chat
    messages – only the home's name.
- `/home <name>` – teleports you directly to that home, no GUI needed
  (tab-completion included)
- `/sethome <name>` – alternative way to set a home directly from the
  command line (uses your first free slot, or updates the slot if a home
  with that name already exists)
- `/delhome <name>` – deletes a home by name
- `/homes <name>` – same as `/home <name>`, teleports you directly to that home
- `/homes reload` – reloads `config.yml` without restarting the server (permission `homes.admin`, default: OP only)

Homes are stored per player in their own file
(`plugins/HomesPlugin/homes/<UUID>.yml`). Every command and every GUI click
work exclusively with the UUID of the executing player – there is no way
for anyone to view, enter, or delete another player's homes. Names are
freely choosable (e.g. "Base"), the comparison ignores upper/lower case, but
the spelling you chose is what gets displayed.

## Building the JAR on GitHub (no local Maven needed)

This repo includes a GitHub Actions workflow (`.github/workflows/build.yml`)
that builds the JAR for you automatically:

1. Push this project to a GitHub repository (e.g. via GitHub Desktop, see
   below).
2. On GitHub, go to the **Actions** tab of your repository. A workflow run
   called "Build plugin JAR" starts automatically on every push to `main`
   (or click **Run workflow** to trigger it manually).
3. Once it finishes (green checkmark), open that run and scroll down to
   **Artifacts** → download **homes-plugin-jar**. That's a zip containing
   the built `.jar` — drop the `.jar` into your Paper server's `plugins`
   folder and (re)start the server.

**Optional – automatic GitHub Release:** if you tag a commit with a version
like `v1.0.0` and push the tag, the same workflow also creates a GitHub
Release with the `.jar` already attached, so you (or anyone else) can grab
it straight from the Releases page instead of digging through Actions runs:

```bash
git tag v1.0.0
git push origin v1.0.0
```

(In GitHub Desktop: **Repository → Create Tag...**, then push it via
**Repository → Push**, or use the tag button in the History view.)

## Building locally instead

If you'd rather build it yourself on your own machine:

- Java 21 (Paper 1.21.x requires JDK 21 to build against, even if your
  server itself later runs on a newer JDK)
- Maven (with internet access to Maven Central and the PaperMC repository)

```bash
mvn clean package
```

The finished file will be at `target/homes-plugin-1.0.0.jar`.

## Adjusting the version

The project currently targets Paper API `1.21.1-R0.1-SNAPSHOT`
(compatible with Minecraft 1.21.x). If you're running a different
Minecraft version, open `pom.xml` and change the `<paper.version>` value to
whatever's available at https://papermc.io/downloads, e.g.:

```xml
<paper.version>1.20.4-R0.1-SNAPSHOT</paper.version>
```

Then adjust `api-version` in `plugin.yml` accordingly (e.g. `'1.20'`). The
code itself doesn't need to change for this.

## Configuring home limits (e.g. with LuckPerms)

The number of boxes shown in the `/homes` GUI (and the limit for
`/sethome`) is controlled in `plugins/HomesPlugin/config.yml`:

```yaml
default-homes: 4

permission-limits:
  homes.limit.wonder: 7
```

- `default-homes` is the limit for every player without a special
  permission (in the example: **4 homes** for a regular player, so `/homes`
  shows exactly 4 boxes for them).
- Under `permission-limits` you can list as many of your own permission
  nodes as you like, each with its own limit. The player's limit is always
  the **highest** value they have a matching permission for (values are not
  added together).

For a "wonder rank = 7 homes, normal players = 4 homes" setup (already the
default in `config.yml`):

1. `config.yml` already has `default-homes: 4` and `homes.limit.wonder: 7`.
2. In LuckPerms, grant that permission to the `wonder` rank:
   ```
   /lp group wonder permission set homes.limit.wonder true
   ```
   (replace `wonder` if your group is actually named differently.)

Add more ranks the same way, e.g. for an even higher tier:

```yaml
permission-limits:
  homes.limit.wonder: 7
  homes.limit.premium: 10
```

and in LuckPerms: `/lp group premium permission set homes.limit.premium true`.

After changing `config.yml`, `/homes reload` (permission `homes.admin`) is
enough – no server restart needed.

## Permissions

- `homes.use` (default: **every player**) – allows `/sethome`, `/delhome`, `/home`, `/homes`
- `homes.admin` (default: OP only) – allows `/homes reload`
- `homes.limit.<name>` (default: `false`, freely extensible) – see above,
  only controls the number of allowed homes, never access to other players' homes

If you want to restrict access altogether, set `homes.use` to
`default: false` in your permissions manager (e.g. LuckPerms) and grant it
selectively.
