# External Plugins for BlueLite

This is my GitHub repository that hosts the source code and builds for all my plugins on BlueLite.

You can join their Discord via the following URL: https://discord.gg/mezdbNZ

Please direct any inquires there as well, inside the support channel. Feel free to tag me if necessary. 

# Table of Contents

## Socket

Socket is a peer-to-peer content sharing plugin that allows other plugins to send and receive data between members in the same party. All socket plugins listed below are included in the single installation file and can be enabled or disabled as necessary.

- **Repository:** [Click Here](./socket)
- **Extended Plugins:** [Click Here](./socket/plugins)
- **Download:** [Click Here](https://github.com/kThisIsCvpv/bexternalplugins/releases/download/1.0.7/unclelitetob.jar)

### Sotetseg

Socket plugin extension for Sotetseg in the Theatre of Blood. Send and receive mazes to guarentee the safety of your party.

### Special Attack Counter

Socket plugin extension for special attack counting. Track DWH, Arclight, Darklight, and BGS special attacks used on NPCs and share with those in the same party.

### Player Status

Socket plugin extension for displaying player status to members in your party.

## Socket Server

Socket server is the middleman for socket plugins. Individuals using the socket plugin must establish a connection to a common socket server in order to broadcast data to amongst each other.

- **Repository:** [Click Here](./socket-server-standalone)
- **Download:** [Click Here](https://github.com/kThisIsCvpv/bexternalplugins/releases/download/1.0.7/unclelitetob.jar)

## UncleLite+ Theatre of Blood

**Disclaimer:** This plugin was made for jokes. It only edits the default vanilla text on the client side.

Ever wanted your own Speedrun Record but just aren't good enough? Or maybe you can't find the right team...? Don't worry, UncleLite+ has got your back! Here, we make it easy.

- **Repository:** [Click Here](./unclelitetob)
- **Download:** [Click Here](https://github.com/kThisIsCvpv/bexternalplugins/releases/download/1.0.7/unclelitetob.jar)

# Plugin Installation

## Windows

1. Press ``Win + R``
2. Type in ``%USERPROFILE%/.runelite``
3. Create a folder named ``bexternalplugins`` if it does not already exist.
4. Drag plugins into that folder.
5. Restart your BlueLite.

## Mac

1. Open Terminal.
2. Execute ``cd ~/.runelite``
3. Execute ``mkdir -p bexternalplugins``
4. Execute ``open .``
5. Drag plugins into ``bexternalplugins``
6. Restart your BlueLite.

## Linux

1. Open Terminal.
2. Execute ``cd ~/.runelite ; mkdir -p bexternalplugins ; xdg-open ./bexternalplugins``
3. Drag plugins into this folder.
4. Restart your BlueLite.

## OpenOSRS

A version of the socket plugins will most likely be ported to [xKylee's repository on GitHub](https://github.com/xKylee/plugins-release). You can import his repository by following the instructions listed on either his repository markdown or Discord support channel.
