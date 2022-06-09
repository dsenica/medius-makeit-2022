# Enostavna gradnja podatkovnih tokov za usmerjanje, analizo ter učenje v "BigData" svetu
## MakeIT 2022


### Struktura
#### makeit-entity
Vsebuje entitete, ki se uporabljajo v demu.

#### produce-data
Service, ki ustvari začetno "bazo" podatkov.

#### makeit-depo
Logika za skladišče.

#### makeit-invoice-splitter
Razdeli vsak "invoice" (na katerem je več izdelkov) v sploščen seznam, kjer vsak element nosi informacijo o id uporabnika, id računa ter id izdelka.

#### makeit-recommender
Priporočilni sistem, ki deluje na podlagi elastica.

### Demo
Najprej zgradimo vse mikrostoritve, tako da zaženemo `mvn clean package`.
Nato zaženemo `docker-compose up -d`. (v nekaterih primerih je predhodno potrebno nastaviti `vm.max_map_count` na vsaj 262144, na linuxu to storimo z ukazom `sysctl -w vm.max_map_count=262144`). 
Počakamo, da se vsi docker containerji zaženejo nato pa v naslednjem vrstnem redu zaženemo mikrostoritve:
1. `java -jar produce-data/target/quarkus-app/quarkus-run.jar` zažene generator podatkov, ki ob zagonu naloži nekaj začetnih podatkov na prvi topic kafke. 
Prav tako izpostavi API na `localhost:8081/produce`, kamor je mogoče polsati nov "Invoice".
2. `java -jar makeit-depo/target/quarkus-app/quarkus-run.jar` zažene API za skladišče. API je izpostavljen na `localhost:8082/invoices`, kjer se pridobijo vsa naročila v statusu PENDING.
3. `java -jar makeit-invoice-splitter/target/quarkus-app/quarkus-run.jar` zažene program, ki "Invoice" razdeli na "Segment". Ne izpostavlja nobenega APIja.
4. `java -jar makeit-recommender/target/quarkus-app/quarkus-run.jar` zažene API za priporočilni sistem. API izpostavi na `localhost:8084/recommend/{customerID}`. Ta v ozadju pokliče elasticsearch.

#### UI
Kiana UI je dosegljiv na `localhost:5601`, AKHQ UI pa na `localhost:28080`.

Za prikaz vizualizacij v Kibani v meniju pod Management -> StackManagement -> Saved Objects,
naredite import datoteke [export.ndjson](kibana_dashboard/export.ndjson).

