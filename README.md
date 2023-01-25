# [Pokémon GO Clone][pokemon-go-repo]

## Table of Contents

* __[Introduction](#introduction)__
* __[Resources applied in pokemon go clone](#resources-applied-in-pokemon-go-clone)__
* __[Application features](#application-features)__
  * [Screenshots](#screenshots)
  * [Demonstration Video](#demonstration-video)
* __[Technical Documentation](#technical-documentation)__
* __[About](#about)__

## Introduction

In order to teach my students the fundamentals of programming for mobile devices, in 2017 I took advantage of the then-recent release of the Pokémon GO game (Android, iOS) and developed this Android version of the game similar to the one originally created by Niantic.

I used this application to demonstrate in the classroom how to program using the main features of a mobile device and I provided students with all the [documentation](#technical-documentation) for the application, without giving the source code at first. The students were divided into groups and each group had to implement the same project, which had already been previously analyzed and documented by me. In addition to practicing development technologies for mobile devices, this work aimed to provide students with the experience of participating in a ``software engineering laboratory``, implementing a backlog of features as a team, following a pre-defined schedule and quality standards, all this using the SCRUM agile method to manage the project.

After the end of the course in 2017, I used this public GitHub repository to make the source code of this didactic experience available, thus allowing new collaborators to contribute to the evolution of this project.

In 2020, many additional features were collaboratively implemented by another group of students. These novel features are documented as [issues](#issues-doc).

[▲ back to Index](#table-of-contents)

## Resources applied in Pokemon GO clone

To develop this project, I used native application development resources for Android. More specifically, these were the main resources used and demonstrated to students through this app:

* Basic Android Concepts
* Navigation between screens
* Activity Lifecycle
* GUI - layout managers
* GUI - different types of views
* GUI - Fragments
* Threads, Handler and AsyncTask
* Client-server communication with HTTP and JSON
* Data persistence with SQLite
* Intents and Intent Filters
* Multimedia – audio and camera
* Use of Maps and Routes (Google Maps API, Places SDK and Directions API)
* Geolocation
* Communication via network (Bluetooth)
* Support for different screen sizes
* Moving an image around the screen using touch
* Gyroscope sensor
* Use of design and architectural patterns

[▲ back to Index](#table-of-contents)

## Application features

As my goal with this application was purely didactic, not all features of the original game were implemented. I selected the following features for implementation:

* __Register user__ (trainer avatar defined according to gender)
* __Login/Logout__
* __Search for Pokémon__ (walk around the map, seeing random Pokémon scattered within a dynamic radius updated according to the trainer's geolocation. Pokémon respawn every 3 minutes.)
* __Catch Pokémon__ (after clicking on a Pokémon on the map, an augmented reality simulation is presented using the camera, gyroscope, sound effects, and moving objects on the screen via touch)
* __Pokémon trainer profile__ (Catch history and more)
* __Pokédex__ (View all pokemon ever caught among the 151 pokemon of the first generation)
* __Pokédex details__ (See types of all pokemon and locations on the map where they have already been found/captured)
* __PokéStops__ (Specific points marked on the map, usually buildings and historic places where the main character can find Pokémon eggs.)
* __Hatch Eggs__ (Eggs are available at PokéStops around the map. When the player gets close enough to a PokéStop and interacts with it, he gains items, which are usually eggs. To hatch an egg, simply walk the distance indicated by the egg (two, five, or ten kilometers) for the egg to hatch and you get a new Pokémon.)
* __Trace a route to a distant Pokémon__ (To try to catch an existing Pokémon on the map, the player must be within a minimum distance of it. Whenever the player interacts with a Pokémon that is not within this minimum distance, the game shows the player a route on the map between the Pokémon and the trainer. After the route is traced and the trainer starts moving, it is warned when the pokémon is within the minimum distance. Feature implemented using the Google Directions API)
* __Pokémon Trading__ (Allow nearby users to use a Bluetooth connection to trade Pokemon they've caught before)
* __Pokémon Evolution__ (When capturing a Pokémon, it comes with specific candies of that species. When a certain amount of candies from an evolutionary line is accumulated, the trainer can evolve a Pokémon of this belonging to that evolutionary line)
* __Trainer experience points__ (Whenever a trainer catch or evolves a Pokémon, interacts with a PokéStop, or hatches an egg, the trainer gains experience points and can level up.)

### Screenshots

The next screens implement some of the main features mentioned above:

<div align="center">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350632-7ce44635-1762-44e8-99ad-f754919128c6.png?raw=true">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350620-09ac0e90-939b-4a1d-8a01-e0a8efd280d0.png?raw=true">
</div>
<div align="center">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350606-9d7a72c8-f3cb-4551-83fd-6203e71fef8b.png?raw=true">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350229-586166d0-74c7-4d1e-b2bd-d1feae85cc14.png?raw=true">
</div>
<div align="center">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350614-f0929f90-0615-476d-a6d1-cfe979594a2e.png?raw=true">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350628-bd363f66-8c66-405a-a7b4-7aa08c86d5e7.png?raw=true">
</div>
<div align="center">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350630-9f6a32ae-8e0f-4d89-b3ef-06c1fcee542b.png?raw=true">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350617-31da4875-c4cc-423b-a545-eeb57625a084.png?raw=true">
</div>
<div align="center">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350626-ecd60447-2fa3-4e32-830a-a3e9c8e44705.png?raw=true">
  <img width="35%" src="https://user-images.githubusercontent.com/17458700/214350623-f12051e5-23a4-4bd0-a723-b8fb29ff2fe2.png?raw=true">
  <br><br>
</div>

The navigation flow of the screens follows the storyboard presented below:

<div align="center">
  <img width="100%" src="https://user-images.githubusercontent.com/17458700/214377843-4772c956-9136-4d8c-97f3-43d91bd96e19.png?raw=true">
  <br><br>
</div>

[▲ back to Index](#table-of-contents)
___

### Demonstration Video

The video below (pt-BR) shows the complete app in action. 

[![Pokémon GO Clone presentation](https://user-images.githubusercontent.com/17458700/214458048-0630f62b-dcef-42ec-9943-c4d17a2be21a.png)](https://www.youtube.com/embed/akgI7fmcn4E)

[▲ back to Index](#table-of-contents)

## Technical Documentation

The complete technical documentation of this project (pt-BR), including its functional requirements, non-functional requirements, structural and behavioral modeling can be found at this [link][pokemon-go-documentation].

[▲ back to Index](#table-of-contents)

## About

This project was developed during the course "Programming for Mobile Devices" of the undergraduate in Computer Science at Federal University of Viçosa (Brazil). It sought to reproduce the main features of the original game, with a purely didactic and pedagogical purpose. This game is not marketed or distributed to end users.

The UFV Computer Science undergraduate course website published a note detailing this experience a little more. This note is in Brazilian Portuguese and can be accessed at this [link][pokemon-go-site-ccp].

* __Pokémon is a brand owned by Nintendo Co. Ltd., Creatures Inc., and Game Freak Inc.__
* __Pokémon GO was originally developed by Niantic Inc., and Nintendo Co. Ltd.__

[▲ back to Index](#table-of-contents)

<!-- Links -->
[pokemon-go-repo]: https://github.com/labd2m/PokemonGoCloneOffline
[pokemon-go-documentation]: https://github.com/INF311/PokemonGoCloneOffline/files/5277308/projeto_pokemon_go_clone.pdf
[pokemon-go-site-ccp]: https://ccp.ufv.br/informativo/alunos-de-ciencia-da-computacao-desenvolvem-jogo-similar-ao-pokemon-go-em-uma-disciplina/
[issues-doc]: https://github.com/labd2m/PokemonGoCloneOffline/issues
