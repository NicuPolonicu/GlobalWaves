# Proiect GlobalWaves - Etapa 2

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

# Comenzi

Parsarea comenzilor se face cu ajutorul bibliotecii `Jackson`. Cu ajutorul acesteia,
comenzile, care se dau in format JSON si pot avea campuri lipsa, pot fi citite intr-un obiect
`Command`, ce contine toate campurile posibile intr-o comanda. Cu ajutorul Jackson, in fiecare
instanta pentru o comanda sunt instantiate doar campurile de care avem nevoie intr-o comanda
specifica.
(de exemplu, la comanda `search` avem campurile `command`, `username`, `timestamp`, `type`
si `filters`,
pe cand la comanda `select` avem doar `command`, `username`, `timestamp` si `itemNumber`)





<div align="center"><img src="https://tenor.com/view/listening-to-music-spongebob-gif-8009182.gif" width="300px"></div>

#### Assignment Link: [https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa1](https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa1)

## Skel Structure

* src/
    * checker/ - checker files
    * fileio/ - contains classes used to read data from the json files
    * main/
        * Main - the Main class runs the checker on your implementation. Add the entry point to your
          implementation in it. Run Main to test your implementation from the IDE or from command
          line.
        * Test - run the main method from Test class with the name of the input file from the
          command line and the result will be written
          to the out.txt file. Thus, you can compare this result with ref.
* input/ - contains the tests and library in JSON format
* ref/ - contains all reference output for the tests in JSON format

## Tests:

1. test01_searchBar_songs_podcasts - 4p
2. test02_playPause_song - 4p
3. test03_like_create_addRemove - 4p
4. test04_like_create_addRemove_error - 4p
5. test05_playPause_playlist_podcast - 4p
6. test06_playPause_error -4p
7. test07_repeat - 4p
8. test08_repeat_error - 4p
9. test09_shuffle - 4p
10. test10_shuffle_error - 4p
11. test11_next_prev_forward_backward - 4p
12. test12_next_prev_forward_backward_error - 4p
13. test13_searchPlaylist_follow ---  (+4)
14. test14_searchPlaylist_follow_error - 4p
15. test15_statistics - 4p
16. test16_complex - 10p
17. test17_complex - 10p

<div align="center"><img src="https://tenor.com/view/homework-time-gif-24854817.gif" width="500px"></div>
