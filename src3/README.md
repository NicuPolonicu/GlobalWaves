# Proiect GlobalWaves - Etapa 3

*GlobalWaves* este un simulator de Audio Player (precum Spotify) scris in Java.

# Obiecte Audio

In program trebuiau implementate abordari pentru melodii (`Song`), playlist-uri (`Playlist`),
podcast-uri (`Podcast`), episoade (`Episode`) si (acum in etapa 2) albume (`Album`). Pentru a nu
crea o variabila separata pentru fiecare
intrare posibila, am realizat urmatoarea ierarhie:

* AudioItem
  * AudioFile
    * Song
    * Episode
  * AudioList
    * Album
    * Podcast
    * Playlist

Astfel, incarcarea unui fisier audio in Player se face in obiectul `Queue` de tipul generic
`AudioItem`.

# Biblioteca

In `biblioteca` (`Library`) se regasesc:

* melodiile
* podcast-urile
* user-ii cu toate obiectele lor interioare
* playlist-urile

# User

In obiectele de tip `User` se afla tot ce tine de
utilizatorul specific, cum ar fi:

* `Search Bar`-ul sau
* `Queue`-ul
* melodiile la care a dat Like si playlist-urile la care a dat Follow
* `Podcast Progress` pentru fiecare `Podcast` existent: aici retinem cat
  a ascultat user-ul din fiecare Podcast.

In etapa 2 am adaugat si artisti (`Artist`) si hosti (`Host`). Aceste doua
obiecte mostenesc clasa `User` insa nu folosesc Search Bar-ul, Queue-ul sau
Podcast Progress-ul. In schimb, au o lista de obiecte relevante. (pentru Artist
albume, merch si event-uri, iar pentru Host-uri, podcast-uri si anunturi)


# Search Bar

Dupa cum am mentionat mai sus, fiecare `User` are un `Search Bar` propriu,
deoarece doi useri pot cauta lucruri diferite concomitent. In acesta se pastreaza rezultatele
ultimului search, cat si selectia user-ului din acele rezultate.

# Player

`User`-ii (normali) au, de asemenea, `Player`-i separati. (din moment ce pot asculta melodii diferite, de
exemplu)
In Player avem:

* `queue` : de tip `AudioItem` unde se retine ce am incarcat (Playlist/Podcast/etc.)
* `currentFile` : de tip `AudioFile`: daca avem incarcat in queue un Playlist, Podcast sau Album, aici se
  retine la ce melodie, respectiv episod ne aflam.
* `timeListened` : pentru a retine cat de departe am ajuns in melodie/episod.
* alte variabile precum `shuffle`, `isPlaying`, `repeatMode` pentru a retine starea curenta a
  player-ului (daca avem shuffle, etc.)

# Ad-uri

Acum avem si `Ads`! Acestea sunt pregatite in momentul in care se primeste comanda 
adBreak pentru un user. Daca user-ul asculta o melodie, se pune direct in campul
`currentFile` din Player, insa daca se asculta la un `Playlist`, se retine melodia
la care a ramas user-ul, din moment ce se pierde continuitatea ascultarii playlist-ului.
Dupa sfarsitul ad-ului, se continua de la acea melodie.


# Comenzi

Parsarea comenzilor se face cu ajutorul bibliotecii `Jackson`. Cu ajutorul acesteia,
comenzile, care se dau in format JSON si pot avea campuri lipsa, pot fi citite intr-un obiect
`Command`, ce contine toate campurile posibile intr-o comanda. Cu ajutorul Jackson, in fiecare
instanta pentru o comanda sunt instantiate doar campurile de care avem nevoie intr-o comanda
specifica.
(de exemplu, la comanda `search` avem campurile `command`, `username`, `timestamp`, `type`
si `filters`,
pe cand la comanda `select` aver doar `command`, `username`, `timestamp` si `itemNumber`)

# Design Pattern-uri

- `Singleton` pentru Library
- `Observer` pentru notificari
- `Facade` pentru Command (toate apelurile de functii se fac prin intermediul acesteia,
pentru un Main curat)


# Alte detalii cerute

Am folosit exclusiv implementarea mea la toate etapele.

Am folosit CoPilot pentru generarea sortarilor pentru Wrapped, Recomandari, etc...

# Feedback si Doleanțe

Apreciez că nu a fost chin cu round-urile la calculele pentru monetizări, la unul din
lab-uri a fost jale in privinta asta. Thanks!

Scuze că am un cod destul de C-ish in unele parti (precum Player), am facut tot posibilul
sa fie cat mai POO.

Toate astea fiind zise, sper sa va placa ce am scris si sa nu fie prea greu de citit.


#### Assignment Link: [https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa1](https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa3)


## Skel Structure

* src/
  * checker/ - checker files
  * fileio/ - contains classes used to read data from the json files
  * main/
      * Main - the Main class runs the checker on your implementation. Add the entry point to your implementation in it. Run Main to test your implementation from the IDE or from command line.
      * Test - run the main method from Test class with the name of the input file from the command line and the result will be written
        to the out.txt file. Thus, you can compare this result with ref.
* input/ - contains the tests and library in JSON format
* ref/ - contains all reference output for the tests in JSON format

<div align="center"><img src="https://tenor.com/view/homework-time-gif-24854817.gif" width="500px"></div>
