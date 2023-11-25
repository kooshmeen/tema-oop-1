Programul incepe prin a citi comenzile in lista de Commands reseta niste variabile globale pentru a nu se pastra valorile din rularile anterioare.
Acestea sunt executate apoi prin metoda execute din clasa Command.
Metoda execute incepe prin a realiza logica pentru timpul trecut (remaining time) la o melodie/episod. In primul rand, timpul se scurge, deci se scade din variabilele de duratie, doar daca un flag global ce reprezinta starea de pauza este fals, adica playerul se afla in starea de play.
Aici sunt prezente cazurile pentru o singura melodie, un playlist sau un podcast. In cazul playlistului, se tine cont si de flagul shuffle.
Pentru retinerea timpului initial, am folosit o variabila globala originalDuration (mare greseala).
Calculele pentru timpul trecut sunt realizate in functie de timpul initial, timpul curent si timpul trecut (care este retinut la finalul fiecarei actiuni).
Daca timpul trecut este mai mare decat timpul curent, inseamna ca s-a terminat melodia/episodul, deci se trece la urmatorul, in functie de caz.
Exista situatii in care s-a scurs timpul si playerul nu are ce melodie/episod sa puna in continuare, caz in care se modifica flaguri globale pentru starile de final.
(bucata aceasta de cod a inceput de la o mizerie mica care a fost copiata si lipita + modificata de multe ori si rezulta o mizerie mare)
Dupa ce s-a realizat logica pentru timpul trecut, se verifica comanda de efectuat.
In cazul fiecarei comenzi, am realizat o clasa ce extinde clasa Command.
Astfel, pentru o anumita comanda se creaza o instanta a clasei respective si se apeleaza metoda execute a subclasei.
Search: am pornit de la totalitatea melodiilor/podcasturilor/playlisturilor din librarie si am eliminat elementele ce nu corespund filtrelor date.
Rezultatele se retin in variabile globale pentru a fi folosite in urmatoarele comenzi (select).
Select:  se verifica daca a fost gasit un element de tipul cautat in urma comezii search. Daca da, se retine in variabile globale.
Load: se verifica daca a fost selectat un element corespunzator in urma comenzii select. Daca da, se retine in variabile globale.
Status: se afiseaza informatiile despre melodia/episodul selectat. In functie de flagurile setate anterior in logica pentru timpul trecut, exista corner case uri unde afisarea este aproape lipsita de variabile.
PlayPause: modifica flagul global pentru starea de pauza.
Clasa playlist: pentru a retine melodiile dintr-un playlist, am creat o clasa ce contine o lista de melodii, precum si alte informatii importante pentru viitoarele functii (ownerul, numarul de followeri, vizibilitatea).
CreatePlaylist: creaza un playlist nou (daca nu exista deja) si il adauga in lista cu toate playlisturile.
AddRemoveInPlaylist: adauga sau sterge o melodie dintr-n playlist. Se verifica daca melodia exista deja in playlist, caz in care nu se mai adauga.
ShowPlaylist: afiseaza informatiile despre un playlist (melodiile continute, vizibilitatea, numarul de followeri).
Clasa User_Liked: o clasa auxiliara pentru a retine melodiile/podcasturile like-uite de un user (am vazut ulterior ca se aplica doar la melodii).
ShowPrefferedSongs: se foloseste de clasa user_liked. Afiseaza melodiile like-uite de un utilizator.
Repeat: modifica flagul global pentru starea de repeat, folosit in logica pentru timpul trecut.
Shuffle: modifica flagul global pentru stare de shuffle, folosit in logica pentru timpul trecut.
Follow: adauga un follower unui playlist.
SwitchVisibility: schimba vizibilitatea unui playlist.
GetTop5Playlists: afiseaza cele 5 playlisturi cu cel mai mare numar de followeri.
clasa LikedSongs: clasa auxiliara ce retine numarul unei melodii si numarul de like-uri.
GetTop5Songs: afiseaza cele 5 melodii cu cel mai mare numar de like-uri, folosind obiecet de tip LikedSongs.

