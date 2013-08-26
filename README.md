SpyHunter4K
===========

**A Tribute To Spy Hunter, Submitted To The [2006 Java 4K Gaming Competition](http://javaunlimited.net/games/view.php?id=102)**

About
-----

This was our entry into the 2006 Java 4K Gaming Competition.

Our plan was to create a tribute to the Spy Hunter arcade game.

The goal of the competation was to present a game, contained within a single jar file with size less than 4096 bytes, that could be played in the browser.


How We Achieved 4K
------------------

* **Sprites**

We only stored the left-half of the car and tree sprites in the jar.  The code would mirror these to create a full sprite for rendering.  We also reduced the amount of detail in the sprite to create longer runs of the same color for better compression.

* **Drawing & Collision-Detection**

We drew the pixels manually, and performed collision-detection during the drawing process.

* **Post-Compilation Processing**

We ran the class file through a set of tools to strip out unnecessary byte-code and further compress the results.   The names of the tools elude me right now, but we may post more details later.


Playing The Game
----------------

You can view the original submission page (and play the game) here:

http://www.javaunlimited.net/games/view.php?id=102


Contributors
------------

[David Farrell](https://github.com/iNamik) <DavidPFarrell@yahoo.com>

[Chris Jones](https://github.com/cajurabi)
