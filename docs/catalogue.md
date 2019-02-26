# Ministerstvo financí ČR

Datové instance MFČR se updatují automaticky z JSON API na data.mfcr.cz. Pokud se nezmení formát, není potřeba je přidávat ručně.

## Objednávky
Stahují se v souhrnném dokumentu obsahujícím všechno od roku 2007.

Pro jednu objednávku může existovat více záznamů:
* Pokud objednávka patří do více rozpočtových kategorií, pak je uvedena pro každou zvlášť.
* Oproti smlouvám a fakturám jsou ale na každém řádku objednávány často úplně jiné věci, proto je aplikace nespojuje.
* Záznamy patřící pod stejnou objednávku mají stejný authorityIdentifier a masterId

Vyplněná pole (hvězdička znamená, že je pole v drtivé většině případů nenulové):
+ recordType*, vždy ORDER
+ subject* ve formátu Předmět: předmět_řádku
+ dateCreated*, ze sloupce "Datum_vystavení"
+ currency*
+ originalCurrencyAmount* ze sloupce "Částka_v_měně"
+ amountCzk - chybí, pokud se částka neliší od originalCurrencyAmount a měna není CZK. Vychází ze sloupce "Částka za řádek".
+ partner* - může chybět v ojedinělých případech
+ authorityIdentifier*, ze sloupce "Číslo_objednávky"
+ masterId* - náhodně vygenerované UUID, stejné přes objednávky sdílející authorityIdentifier

## Faktury 2010 - 2014
Každý rok má vlastní dokument, roky < 2015 mají jiný formát. Záznamy rozpadlé přes několik řádků aplikace spojuje do jednoho.

Dokumenty neobsahují IČO partnera, pouze jméno. Proto se před zpracováním každý měsíc stahuje ještě seznam partnerů, který má vlastní DataInstanci, ale té je zabráněno automatické zpracování. Stahuje a zpracovává ji rovnou MFCRHandler. Během zpracování dokumentu se pouze vytváří UnresolvedRelationships, partneři se naváží až po běhu RelationshipResolveru.

Vyplněná pole (hvězdička znamená, že je pole v drtivé většině případů nenulové):
+ recordType*, vždy INVOICE
+ subject ze sloupce "Předmět"
+ variableSymbol ze sloupce "Variabilní symbol"
+ dateCreated*, ze sloupce "Datum přijetí"
+ dueDate*, ze sloupce "Datum splatnosti"
+ dateOfPayment, ze sloupce "Datum úhrady"
+ currency*
+ originalCurrencyAmount* ze sloupce "Úhrada cizí měna"
+ amountCzk - chybí, pokud se částka neliší od originalCurrencyAmount a měna není CZK. Vychází ze sloupce "Částka".
+ partner - pouze pokud je už partner s daným kódem v databázi.
+ authorityIdentifier*, ze sloupce "Číslo_faktury"
+ masterId* - náhodně vygenerované UUID


## Faktury od 2015
Nový formát dokumentu (opět co dokument, to rok) obsahue i "Ostatní platby" kromě faktur, proto může být typ záznamu různý. Partneři se nastavují přímo, záznamy nejsou nikdy rozpadlé.

Vyplněná pole (hvězdička znamená, že je pole v drtivé většině případů nenulové):
+ recordType*, INVOICE nebo PAYMENT, podle sloupce [ROZLISENI]
+ subject ze sloupce "[UCELPLATBY]"
+ budgetCategory ze sloupce "[NAZEVPOLOZKYROZPOCTU]"
+ variableSymbol ze sloucpe "[VARIABILNISYMBOL]"
+ dateCreated*, ze sloupce "[DATUMPRIJETI]"
+ dueDate*, ze sloupce "[DATUMSPLATNOSTI]"
+ dateOfPayment, ze sloupce "[DATUMUHRADY]"
+ currency*
+ originalCurrencyAmount* ze sloupce "[CELKOVACASTKACIZIMENA]"
+ amountCzk - chybí, pokud se částka neliší od originalCurrencyAmount a měna není CZK. Vychází ze sloupce "[CELKOVACASTKA]".
+ partner*
+ authorityIdentifier*, ze sloupce "[CISLO]"
+ masterId* - náhodně vygenerované UUID
+ parentRecord - po spojení se smlouvami


## Smlouvy
Všechny smlouvy jsou v jediném dokumentu, který obsahuje jak platné, tak neplatné smlouvy. Jednotlivé smlouvy nejsou rozpadlé na více záznamů, ale dodatek ke smlouvě má vlastní a je spojen s rodičem přes parentRecord. Toto je jediný dokument, který obsahuje vazby na jiné (na faktury), takže při zpracování jsou vytvářeny UnresolvedRelationships. Datová instance smluv není inkrementální, takže se při každém stažení zpracovává celá, nejen od posledního zpracovaného řádku. Není totiž záruka, že se nové záznamy vloží na konec dokumentu a je možné, že bude nějaká starší smlouva zveřejněna a zařazena mezi smlouvy ze stejné doby.

Dokument může obsahovat (a obsahuje) záznamy s nullovými hodnotami u neplatných smluv. Dokument také obsahuje patičku jako celkovou sumu částek za všechny smlouvy. Tyto věci je nutné ošetřit pro úspěšné zpracování.

+ recordType*, vždy CONTRACT,
+ authorityRole*, ze sloupce "Povaha_smlouvy"
+ subject* ve formátu "Popis_smluvního_typu": "Předmět"
+ inEffect*, ze sloupce "Kód stavu"
+ dateCreated*, ze sloupce "Platnost_od"
+ dateOfExpiry*, ze sloupce "Platnost_do"
+ currency*
+ originalCurrencyAmount* je kombinací sloupců "Částka", který se váže ke smlouvě, a "Částka", který se váže k faktuře. Často mají smlouvy vyplněný jenom jeden z nich. Částky jednotlivých faktur se posčítají přes jednotlivé řádky u stejné smlouvy, částka za celou smlouvu je všude stejná.
+ partner*
+ authorityIdentifier*, ze sloupce "Číslo_smlouvy"
+ parentRecord v případě, že jde o dodatek
+ masterId* - náhodně vygenerované UUID

# Ministerstvo spravedlnosti ČR

## Smlouvy

MSp publikuje všechny aktuálně platné smlouvy v jednom dokumentu (naposledy byl ale aktualizován v červnu 2015). Datová instance je do databáze vložená manuálně a není inkrementální, tedy zpracovává se vždy celá. Zneplatnění uložených, které v dokumentu nejsou, zatím není implementováno. Mezi fakturami a smlouvami neprobíhá žádné spojování.

+ authorityIdentifier*, ze sloupce "Číslo smlouvy"
+ recordType, vždy CONTRACT
+ partner*
+ subject*, ze sloupce "Stručný předmět smlouvy"
+ currency*
+ originalCurrencyAmount*, pokud je měna CZK, pak i amountCZK
+ masterId* - náhodně vygenerované UUID
+ dateCreated* - zdrojový dokument neobsahuje datum uzavření, pouze rok jako součást čísla smlouvy. Proto je datum vždy 1. 1. daného roku.
+ dateOfExpiry, ze sloupce "Platnost". Pokud je platnost na dobu neurčitou, hodnota je null.
+ periodicity, u některých smluv DAILY

## Faktury

Faktury se publikují od roku 2009 v odděleném dokumentu pro každý rok. DataInstance jsou generovány automaticky, v URL se liší jenom rokem.

Formát dokumentů se od sebe drobně liší (překlepy ve sloupcích, IČ místo IČO apod.), proto jsou argumenty v mapování často přetížené. Informace jsou ale ve všech stejné. Instance nejsou inkrementální - zpracování probíhá vždy od prvního řádku, protože záznamy nejsou seřazené podle času. Duplicitní záznamy by se ale neměly objevovat díky Retrieveru. Mezi fakturami a smlouvami neprobíhá žádné spojování.

Aktuálně nejnovější dokument je za rok 2015, který je aktuální k 30. 9. 2015.

+ authorityIdentifier*, spojení sloupců "Dokladová řada" a "Číslo dokladu"
+ recordType, vždy INVOICE
+ authorityRole, vždy CUSTOMER
+ partner*
+ subject*, ze sloupce "Důvod úhrady, nebo PARAMETR4 (faktury 2012)"
+ variableSymbol*, ze sloupce "Variabilní symbol"
+ currency*
+ originalCurrencyAmount*, pokud je měna CZK, pak i amountCZK
+ masterId* - náhodně vygenerované UUID
+ dateCreated*, ze sloupce "Datum zápisu"
+ dueDate, ze sloupce "Splatnost dne"
+ dateOfPayment, ze sloupce "Posl. úhr. dne"

# Ministerstvo životního prostředí ČR

MŽP publikuje faktury, smlouvy i objednávky v HTML tabulce s odkazem pro vygenerování souhrnného XLS dokumentu. Aplikace volá přímo tuto URL pro všechny tři datové zdroje. Odpovídající datové instance jsou do databáze vloženy ručně. Záznamy jsou seřazené od nejnovějšího, proto instance nejsou inkrementální a zpracovávají se celé.

Generování XLS dokumentů trvá webu MŽP poměrně dlouho, u faktur jsou to desítky sekund.

## Objednávky

Objednávky jsou sice na webu MŽP zveřejněné, ale z nějakého důvodu pouze 1000 záznamů končících v květnu 2015. I tak je ale aplikace zpracuje.

+ recordType*, vždy ORDER
+ authorityRole*, vždy CUSTOMER
+ subject* ve formátu Předmět: předmět_řádku
+ dateCreated*, ze sloupce "Datum objednání"
+ currency*, vždy CZK
+ originalCurrencyAmount* a amountCzk*, ze sloupce "Celková částka"
+ partner* - dokument neobsahuje IČO, takže pouze podle jména a tím pádem se mohou snadněji vyskytnout duplicitní partneři.
+ authorityIdentifier*, ze sloupce "Číslo objednávky"
+ masterId* - náhodně vygenerované UUID

## Smlouvy

Dokument obsahuje pouze aktuálně platné smlouvy.

+ recordType*, vždy CONTRACT,
+ authorityRole*, ze sloupce "Povaha smlouvy"
+ subject*, ze sloupce "Předmět smlouvy"
+ dateCreated*, ze sloupce "Datum podpisu"
+ dateOfExpiry*, ze sloupce "Datum ukončení smlouvy"
+ currency*, vždy CZK
+ periodicity, u některých záznamů MONTHLY (měsíční platba) nebo YEARLY (roční limit)
+ originalCurrencyAmount* a amountCzk*, ze sloupce "Celková částka"
+ partner* - dokument neobsahuje IČO, takže pouze podle jména a tím pádem se mohou snadněji vyskytnout duplicitní partneři.
+ authorityIdentifier*, ze sloupce "Číslo smlouvy"
+ masterId* - náhodně vygenerované UUID

## Faktury

Dokument obsahuje vazbu na smlouvy, takže se při zpracování vytvářejí UnresolvedRelationships

+ recordType*, vždy INVOICE,
+ authorityRole*, vždy CUSTOMER
+ subject*, ze sloupce "Popis"
+ dateCreated*, ze sloupce "Datum platby"
+ currency*, vždy CZK
+ originalCurrencyAmount* a amountCzk*, ze sloupce "Celková částka"
+ partner* - tento dokument IČO obsahuje.
+ authorityIdentifier*, ze sloupce "Číslo faktury"
+ masterId* - náhodně vygenerované UUID
+ parentRecord - po spojení se smlouvami

# Ministerstvo kultury ČR

Nejméně kvalitní ze všech datových zdrojů. Všechny dokumenty jsou jednorázové (k určitému datu) a zdá se, že má každá aktualizace jinou URL. Je proto potřeba periodicky manuálně kontrolovat a přidávat datové instance, pokud nějaké přibudou. Poslední aktualizace je k lednu 2015.

## Smlouvy

Smlouvy nad 50 000 kč se publikují v jednom dokumentu, k lednu 2015 jich bylo pouze 41. Dokument se zpracovává vždy celý. Nejedná se nejspíš o aktuálně platné smlouvy, protože některé jsou už po datu vypršení.

+ recordType*, vždy CONTRACT,
+ authorityRole*, vždy CUSTOMER
+ subject*, ze sloupce "Předmět smlouvy"
+ dateCreated*, ze sloupce "Platnost od"
+ dateOfExpiry*, ze sloupce "Platnost do"
+ currency*, vždy CZK
+ originalCurrencyAmount* a amountCzk*, ze sloupce "Celková částka"
+ partner* - dokument neobsahuje IČO, takže pouze podle jména a tím pádem se mohou snadněji vyskytnout duplicitní partneři.
+ authorityIdentifier, ze sloupce "Číslo smlouvy", u většiny smluv chybí
+ masterId* - náhodně vygenerované UUID

## Faktury

Faktury jsou publikovány podle měsíce proplacení, momentálně je k dispozici pouze dokument za leden 2015. Jakékoli nové je potřeba přidat ručně nebo zjistit nějaký rozumný URL pattern (s jedním dokumentem to nelze zjistit) a připsat handler, který je bude přidávat automaticky. Faktury nejsou nijak navázány na smlouvy.

V dokumentu jsou celkem čtyři listy, které se liší jenom typem záznamu:

+ Zálohové faktury, typ INVOICE
+ Došlé faktury, typ INVOICE
+ Platební poukazy, typ PAYMENT
+ Dobropisy, typ PAYMENT

Vyplněná pole jsou:

+ recordType*, viz. výše,
+ authorityRole*, vždy CUSTOMER
+ subject*, z bezejmenného sloupce s popisem
+ variableSymbol*, ze sloupce "Variabilní symbol"
+ dateCreated*, ze sloupce "Datum přijetí",
+ dueDate*, ze sloupce "Datum splatnosti",
+ dateOfPayment*, ze sloupce "Datum úhrady",
+ currency*, vždy CZK
+ originalCurrencyAmount* a amountCzk*, ze sloupce "Celková částka"
+ partner* - dokument neobsahuje IČO, snadněji vznikají duplicitní partneři.
+ authorityIdentifier*, ze sloupce "Evidenční číslo"
+ masterId* - náhodně vygenerované UUID

# Ministerstvo pro místní rozvoj

Stejně jako u Ministerstva kultury, datové instance se musí přidávat ručně. Teoreticky by šlo odkazy na XLS soubory získávat z HTML kódu webu, ale k tomu je napřed potřeba zjistit, jak probíhá aktualizace - jestli se mění URL apod. Do té doby jsou přidány pouze tři datové instance (zda jsou periodické, je taky potřeba zjistit - zatím je jejich periodicita nastavena na měsíční aktualizaci, ale je dost nepravděpodobné, že adresy zůstanou stejné).

## Smlouvy

V jednom dokumentu jsou publikovány smlouvy uzavřené od 1. 1. 2015, poslední aktualizace proběhla 1. 1. 2016. Dokument obsahuje celkem pouze 51 záznamů. Hodně smluv je periodických, částka je za nějaké období. Bohužel tato informace se nachází v popisu a bez pokročilého textového rozpoznávání je neextrahovatelná.

+ recordType*, vždy CONTRACT,
+ authorityRole*, vždy CUSTOMER
+ subject*, ze sloupce "Popis"
+ dateCreated*, ze sloupce "Datum uzavření"
+ currency*, vždy CZK
+ originalCurrencyAmount* a amountCzk*, ze sloupce "Celková částka"
+ partner - dokument obsahuje IČO, ale u některých smluv partner úplně chybí. Název se bere ze sloupce "Název/příjmení".
+ authorityIdentifier*, ze sloupce "Číslo smlouvy.
+ masterId* - náhodně vygenerované UUID

## Faktury

V jednom dokumentu jsou publikovány faktury od 1. 1. 2015, dokument je aktuální k 1. 12. 2015.

+ recordType*, vždy INVOICE,
+ authorityRole*, vždy CUSTOMER
+ subject*, ze sloupce "Předmět fakturace"
+ dateCreated*, ze sloupce "Datum příchodu"
+ dateOfPayment*, ze sloupce "Datum úhrady"
+ currency*, ze sloupce "Měna"
+ originalCurrencyAmount* a amountCzk*, ze sloupce "Částka"
+ partner* - včetně IČO
+ authorityIdentifier*, ve formátu "Dokladová řada"-"Číslo"
+ masterId* - náhodně vygenerované UUID


## Objednávky

V jednom dokumentu jsou publikovány objednávky od 1. 1. 2015, dokument je aktuální k 1. 12. 2015.

+ recordType*, vždy ORDER,
+ authorityRole*, vždy CUSTOMER
+ subject, ze sloupce "Popis"
+ dateCreated*, ze sloupce "Datum objednání"
+ currency*, vždy CZK
+ originalCurrencyAmount* a amountCzk*, ze sloupce "Částka v Kč"
+ partner* - včetně IČO, název se bere ze sloupce "Název/příjmení"
+ authorityIdentifier*, ze sloupce "Číslo objednávky"
+ masterId* - náhodně vygenerované UUID

# Ministerstvo dopravy a Státní fond dopravní infrastruktury

MD a SFDI jsou dva různé datové zdroje, ale jejich soubory mají stejný formát, takže používají stejná mapování. Aktualizace datových instancí je složitá, protože při každé aktualizaci je dokumentu vygenerováno nové URL. Musí se provádět ručně.

## Smlouvy

+ recordType*, vždy CONTRACT,
+ authorityRole*, vždy CUSTOMER
+ subject, ze sloupce PREDMET
+ dateCreated*, ze sloupců PLATOD, DATUCIN, DATPOD. Může se stát, že všechny tři jsou prázdné. V takovém případě je dateCreated 1. leden roku, ke kterému se smlouva vztahuje (ze sloupce CISLO).
+ dateOfExpiry, ze sloupce PLATDO
+ currency*, vždy CZK
+ originalCurrencyAmount* a amountCzk*, ze sloupce CENA
+ partner - dokument obsahuje IČO, ale u některých smluv partner úplně chybí. Kombinace sloupců ICDOD a DODAVATEL
+ authorityIdentifier*, ze sloupce CISLO.
+ masterId* - náhodně vygenerované UUID

## Faktury

+ recordType*, vždy INVOICE,
+ authorityRole*, vždy CUSTOMER
+ subject, ze sloupce UCELPLATBY
+ dateCreated*, ze sloupce DATVYST.
+ dueDate, ze sloupce DATSPLAT.
+ budgetCategory, ze sloupce POLOZKA.
+ currency*, vždy CZK
+ originalCurrencyAmount* a amountCzk*, ze sloupce CASTKA
+ partner - dokument obsahuje IČO, ale u některých smluv partner úplně chybí. Kombinace sloupců ICDOD a DODAVATEL
+ authorityIdentifier*, ze sloupce CISLOFA.
+ masterId* - náhodně vygenerované UUID

# Ministerstvo obrany České republiky

MOČR vystavuje API poskytující list dostupných dokumentů faktur a smluv. Přístup k API vyžaduje bezpečnostní protokol TLS 1.2. Fetcher se snaží položky tohoto listu zpracovat, avšak zpracování je implementováno jen pro Excel soubory. Faktury jsou však vystavovány pouze v csv formátu, pro který zpracování není řešeno.

## Smlouvy

TODO

## Faktury

TODO
