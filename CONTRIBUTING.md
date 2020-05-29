Allgemein
=========================

Danke für Ihr Interesse an diesem Projekt. Für Anregungen, Verbesserungsvorschläge oder Bugs können Sie gerne Issues eröffnen, um Code beizusteuern stellen Sie gerne einen Pull Request.
Die Codebasis der App ist über einen langen Zeitraum gewachsen. Verschiedene Entwickler haben mit unterschiedlichen Konzepten daran gearbeitet. Auch technische Neuerungen, die regelmäßig kommen und gehen, haben für unterschiedliche Ansätze in manchen Aspekten der App gesorgt. Wir wollen versuchen, die Diversität in der Codebasis in Grenzen zu halten. Am Ende macht es aber mehr Sinn, dass ein Entwickler eine Lösung umsetzen kann, die ihm sinnvoll erscheint, als auf Biegen und Brechen stumpf Regeln zur Vereinheitlichung zu befolgen. Das gilt im Großen, etwa bei Architektur-Fragen, aber auch im Kleinen, etwa bei der Vorliebe für Zeilenumbrüche und Leerzeichen.
Insofern ist dieses Dokument bitte als Richtlinie zu verstehen und nicht als Gesetz.
 
Prinzipien
=========================

Die Konzepte DRY und KISS sollten beherzigt werden.
Im Zweifel sollte Composition over inheritance angewandt werden.
Reflection sollte möglichst vermieden werden (`instanceof` / `is`).

Architektur
=========================

Wie oben erwähnt ist die Codebasis gewachsen und die Architektur nicht unbedingt einheitlich.
Die aktuell angestrebte Architektur sieht die Verwendung einiger Android Architecture Components vor. ViewModels halten die Daten für die UI in LiveData-Objekten bereit. Die Verwendung von DataBinding ist aktuell nicht erwünscht. Dank Kotlin und den zugehörigen Werkzeugen ist es nicht nötig, UI-Code in die Layout-XMLs zu schreiben.

Code Style
=========================

Der Code Style orientiert sich an gängigen Vorgaben im Android-Bereich und Voreinstellungen von Android Studio. Wer am Projekt mitwirkt, sollte die Projekteinstellungen für Code Style übernehmen.
Automatische Import-Optimierungen sollten nur für Dateien durchgeführt werden, die auch anderweitig angepasst wurden. Eine automatische Code-Formatierung sollte i.d.R. nur für Zeilen bzw. Blöcke erfolgen, die auch anderweitig angepasst wurden. Eine entsprechende Option kann im `Reformat File Dialog` als Voreinstellung für alle Reformat-Vorgänge gesetzt werden. Diese Maßnahmen sollen die Prüfung von Pull Requests übersichtlicher halten.

![Alt-Text](Unbenannt.png "Title")


Sprache
=========================

Der Code sollte in englischer Sprache geschrieben werden. Ausnahmen stellen insbesondere Eigennamen dar. Angezeigte oder gesprochene Texte der App sind deutsch.

Klassen
=========================

Klassennamen sollten Substantive sein, die die wesentliche Rolle der Klasse wiederspiegeln. Sofern das den Namen nicht allzu sehr aufbläht, darf der Name gern die Ableitungshierarchie wiederspiegeln, wenn das zum Verständnis beiträgt. Die Einordnung in Packages erfolgt nach funktionalen und thematischen Aspekten (und soll insbesondere nicht die Vererbungshierarchie kopieren). Eine "korrekte" Einsortierung in Packages ist nicht immer leicht und im Zweifel auch nicht so wichtig, sofern damit keine technischen Ziele verfolgt werden (etwa Sichtbarkeit von Membern).

Methoden
=========================

Methodennamen sollten Verben sein, die die wesentliche Funktion der Methode wiederspiegeln. Übliche Regeln sollten beherzigt werden, wie z.B. dass Getter keine neuen Instanzen erzeugen.


