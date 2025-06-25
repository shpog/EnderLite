# EnderLite
Komunikator umożliwiający tworzenie czatu dla wielu użytkowników, wspierający szyfrowanie wysyłanych wiadomości. Jest to aplikacja graficzna, w której użytkownik tworzy konto i może rozmawiać z innymi użytkownikami. Program napisany w Java z użyciem JSON, Bouncy Castle i JavaFX. Ponadto projekt zawiera testy jednostkowe i dokumentację dostępną w folderze `doc/` dla serwera i klienta, wygenerowaną za pomocą pluginu javadoc do mvn.

Serwer uruchamiamy w folderze projektu za pomocą `java -cp ".\Server\target\EnderLite-1.0-SNAPSHOT.jar;C:\Users\[Username]\.m2\repository\org\json\json\20240303\json-20240303.jar;C:\Users\[Username]\.m2\repository\org\bouncycastle\bcprov-jdk18on\1.78.1\bcprov-jdk18on-1.78.1.jar" com.EnderLite.app.App`

W miejsce [Username] wpisujemy nazwę uruchamiającego serwer użytkownika

Do uruchomienia potrzebne są pobrane Bouncycastle oraz ORG.JSON

Klienta uruchamiany w folderze .\Client\ za pomocą `mvn clean javafx:run`

Dla najefektywniejszego przetestowania aplikacji polecam otworzyć dwie instancje klienta (tzn uruchomić `mvn clean javafx:run` dwa razy, w dwóch osobnych terminalach). 
Należy mieć na uwadze iż przed uruchomieniem drugiej instancji należy poczekać aż pierwsza uruchomi się całkowicie.

Po uruchomieniu pierwszą rzeczą którą musimy zrobić jest założenie konta. Klikamy przycisk 'Stwórz konto' i wpisujemy login, hasło oraz email; nie musimy zaznaczać checkboxów.

Po założeniu konta w bazie możemy założyć nowy czat grupowy za pomocą przycisku 'Add Chat'. Pokaże nam się okno z polem tekstowym, proszące o nazwę świeżo założonego czatu.
Po wpisaniu nazwy oraz zatwierdzeniu widzimy w panelu 'Chats' nowo utworzony czat. Możemy wejść w niego po kliknięciu w jego nazwę. Od teraz możemy wysyłać wiadomości do naszego czatu.

Kolejną rzeczą jest dodawanie znajomych. Klikamy w 'Add Friend'. Pokaże nam się okno z polem tekstowym, proszące o login lub email uzytkownika którego chcemy zaprosić. 
Po zatwierdzeniu zaproszenie zostanie wysłane. Do uzytkownika którego zapraszamy przyjdzie powiadomienie o zaproszeniu. Możemy je potwierdzić lub odrzucić.
Przy odrzuceniu nie zostanie wysłane żadne powiadomienie. Przy potwierdzeniu w liście znajomych obu uzytkowników pojawi sie nowa osoba. Do zapraszającego zostanie wysłane również powiadomienie.

Znajomych możemy również usunąć. Robimy to za pomocą przycisku '-' przy loginie znajomego. Po usunięciu login zostanie usunięty z listy znajomych, a do usuwanego znajomemgo przyjdzie powiadomienie.

Znajomych możemy zaprosić do utworzonych przez nas czatów. Robimy to za pomoca przycisku '+' znajdującego się przy nazwie czatu. Po kliknięciu pokaże się nam powiadomienie z polem tekstowym proszącym o login zapraszanego użytkownika. Po zatwierdzeniu zaproszony przez nas użytkownik dostanie powiadomienie o dodaniu do czatu. Będzie od teraz miał możliwość pisania na czacie.

Wiadomości na czacie wysyłamy za pomoca pola tesktowego na dole ekranu. Pokaże sie ono po wybraniu czatu. Wiadomości zatwierdzamy do wysłania za pomoca przycisku 'Send Message'