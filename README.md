<h1 align="center">
  <a name="logo" href="https://github.com/ldtteam/PerViamInvenire"><img src="https://github.com/ldtteam/PerViamInvenire/raw/version/latest/logo.png" alt="PerViamInvenire" width="200"></a>
  <br>
  PerViamInvenire Source Code
</h1>
<h4 align="center">Be sure to :star: this repo so you can keep up to date on any progress!</h4>
<div align="center">
  <h4>
    <a href="https://buildsystem.ldtteam.com/buildConfiguration/LetSDevTogether_PerViamInvenire_Alpha_Release?branch=&mode=builds">
        <img alt="TeamCity Alpha Build Status" src="https://img.shields.io/teamcity/build/e/LetSDevTogether_PerViamInvenire_Alpha_Release?label=Alpha&logo=Alpha%20build&server=https%3A%2F%2Fbuildsystem.ldtteam.com&style=plasticr">
    </a>
    <a href="https://buildsystem.ldtteam.com/buildConfiguration/LetSDevTogether_PerViamInvenire_Beta_Release?branch=&mode=builds">
        <img alt="TeamCity Beta Build Status" src="https://img.shields.io/teamcity/build/e/LetSDevTogether_PerViamInvenire_Beta_Release?label=Beta&logo=Beta%20build&server=https%3A%2F%2Fbuildsystem.ldtteam.com&style=plasticr">
    </a>
    <a href="https://buildsystem.ldtteam.com/buildConfiguration/LetSDevTogether_PerViamInvenire_Release_Release?branch=&mode=builds">
        <img alt="TeamCity Release Build Status" src="https://img.shields.io/teamcity/build/e/LetSDevTogether_PerViamInvenire_Release_Release?label=Release&logo=Release%20build&server=https%3A%2F%2Fbuildsystem.ldtteam.com&style=plasticr">
    </a>
    <a href="https://github.com/ldtteam/PerViamInvenire/stargazers">
        <img src="https://img.shields.io/github/stars/ldtteam/PerViamInvenire.svg?style=plasticr"/>
    </a>
    <a href="https://github.com/ldtteam/PerViamInvenire/commits/master">
        <img src="https://img.shields.io/github/last-commit/ldtteam/PerViamInvenire.svg?style=plasticr"/>
    </a>
    <a href="https://github.com/ldtteam/PerViamInvenire/commits/master">
        <img src="https://img.shields.io/github/commit-activity/m/ldtteam/PerViamInvenire.svg?style=plasticr"/>
    </a>
  </h4>
</div>
<hr />
<div align="center"><a name="menu"></a>
  <h4>
    <a href="https://www.curseforge.com/minecraft/mc-mods/perviaminvenire">
      CurseForge
    </a>
    <span> | </span>
    <a href="https://buildsystem.ldtteam.com/project/LetSDevTogether_PerViamInvenire?branch=&mode=builds">
      BuildSystem
    </a>
    <span> | </span>
    <a href="https://github.com/ldtteam/PerViamInvenire/">
      Code
    </a>
    <span> | </span>
    <a href="https://github.com/ldtteam/PerViamInvenire/issues">
      Issues
    </a>
    <span> | </span>
    <a href="https://github.com/ldtteam/PerViamInvenire/pulls">
      Pull Requests
    </a>
  </h4>
</div>
<hr />

### <a name="software"></a>Base implementation:
This library is a derivative of the multi-threaded pathfinder that was originally designed for the Minecraft mod [Minecolonies](https://github.com/ldtteam/minecolonies).
This extraction replaces several of its Citizen specific features with flags which can be managed via callbacks and internal feature registries.

The default navigator creates a cache of the chunks between the source and possible target and then creates a calculation task on a thread pool.
The calculation that is run is an adapted version of the A<sup>*</sup>-Pathfinding algorithm.
Block weights can be adapted for each different entity type using the provided API.

### <a name="software"></a>Vanilla compatibility:
By default, the mod replaces any vanilla mobs GroundPathNavigator.
In other words all entities that travel over or on the ground are currently not supported.
However, as of now no flying or climbing mobs are supported.

Notable supported entities:
- Creeper
- Drowned
- Enderman
- Evoker
- Illusioner
- Iron Golem
- Mooshroom
- Skeleton
- Stray
- Vex
- Villager
- Vindicator
- Wandering Trader
- Witch
- Wither Skeleton
- Zombie
- Zombie Villager

See the following file for more details: [Compatible Vanilla Entity List](https://github.com/ldtteam/PerViamInvenire/blob/version/latest/src/datagen/generated/per-viam-invenire/wiki/per-viam-invenire/tags/entity_types/replace_vanilla_navigator.md).

##### <a name="software"></a>Vanilla compatibility (State):
State of the application:
The current state of this mod is **ALPHA**.
This is important, since even though we do our best to maintain the best compatibility and prevent bugs from appearing,
we do not have the time or stamina to test every single feature/bugged-feature that minecraft holds when it comes to pathfinding.
If you do find a bug or something that behaves differently from the vanilla minecraft please create an issue on the bug tracker [Here](https://github.com/ldtteam/PerViamInvenire/issues).

#### <a name="software"></a>Mod compatibility:
By default, any mob that uses a default GroundPathNavigator can be easily added, by adding it to the data pack tag: `per-viam-invenire:replace_vanilla_navigator`.
This will cause PerViamInvenire to replace the navigator that is used by the mobs in that list and use best guess values when needed.

If a modder wants to support PerViamInvenire directly then they can depend directly on the PerViamInvenire API to make this a reality.