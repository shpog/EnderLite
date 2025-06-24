# EnderLite
Komunikator umożliwiający tworzenie czatu dla wielu użytkowników, wspierający szyfrowanie wysyłanych wiadomości. Jest to aplikacja graficzna, w której użytkownik tworzy konto i może rozmawiać z innymi użytkownikami. Program napisany w Java z użyciem JSON, Bouncy Castle i JavaFX. Ponadto projekt zawiera testy jednostkowe i dokumentację dostępną w folderze `doc/` dla serwera i klienta, wygenerowaną za pomocą pluginu javadoc do mvn.



Serwer uruchamiamy za pomocą
`java -cp ".\Server\target\EnderLite-1.0-SNAPSHOT.jar;C:\Users\[Username]\.m2\repository\org\json\json\20240303\json-20240303.jar" com.EnderLite.app.App`
Klient uruchamiany za pomocą `mvn clean javafx:run`
