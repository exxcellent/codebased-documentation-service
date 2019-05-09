# codebased-documentation-service
Bachelorarbeit: "Code-basierte Dokumentation evolutionärer Software-Architekturen"

## Titel

> Code-basierte Dokumentation evolutionärer Software-Architekturen

## Motivation

**_Software-Systeme_** unterliegen durch wechselnde Anforderungen einem stetigem Wandel. Die Software-Entwicklung steht vor der Herausforderung einer [**_evolutionären Software-Architektur_**](https://de.wikipedia.org/wiki/Evolution%C3%A4res_Design), die die Anforderungen erfüllen kann.

Das [**_Domain-Driven-Design_**](https://de.wikipedia.org/wiki/Domain-driven_Design) mit dem Microservice-Ansatz und ein [agiles Projektvorgehen](https://de.wikipedia.org/wiki/Agile_Softwareentwicklung) addressieren diese Herausforderung. Die Kapselung von Domänen-Funktionalitäten in [Bounded Contexts](https://martinfowler.com/bliki/BoundedContext.html) erlauben eine inkrementelle und unabhängige Weiterentwicklung eines Software-Systems.

**_Microservice-Architekturen erschweren allerdings die Dokumentation_**, wie die Erfassung der Beziehungen zwischen den beteiligten Komponenten innerhalb des [verteilten Systems](https://de.wikipedia.org/wiki/Verteiltes_System). Microservices abstraiieren Funktionalitäten und kommunizieren über das Netzwerk per REST und Messaging.

Auf Ebene der Microservices erlaubt die [statische Code-Analyse](https://de.wikipedia.org/wiki/Statische_Code-Analyse) die **_Auflösung der internen Abhängigkeiten_** zwischen Modulen, Packages und Klassen. Eine [Zyklenfreiheit](https://en.wikipedia.org/wiki/Circular_dependency) kann mit bewähreten Werkzeugen zuverlässig festgestellt und visualisiert werden ([JDepend](https://github.com/clarkware/jdepend), [Sonar](https://www.sonarqube.org/), etc.).

**_Die Beziehungen zwischen Microservices sind allerdings nur indirekt über Schnittstellen-Aufrufe im Code oder anhand des Kommunikationsverhaltens zur Laufzeit sichtbar._**

Zum Auflösen dieser Problematik versucht diese Arbeit die **_Informationen einzelner Microservices zusammenzuführen_** und (teil-)automatisiert eine Dokumentation der gesamten Architektur zu erzeugen. **_Meta-Daten zur Dokumentation sollen daher zusammen mit dem Code versioniert_** werden und auf bereits bekannten Informationen basieren.

### Aufgabe

Diese Arbeit soll Möglichkeiten zur code-basierten Dokumentation von Software-Architekturen eruieren und prototypisch eine Lösung implementieren.

### Ansatz

Folgende Ansätze sollen beleuchtet werden:

Jeder Microservice stellt die Dokumentation zur inneren Struktur und der genutzten Services einheitlich in einem definierten Format bereit. Eine strukturierte Dokumentation gestattet eine maschinelle Analyse und Darstellung der Informationen.

Über den Build-Prozess sind allgemeine Informationen wie der Name, das
Repository, die Build-Version und die verwendete Bibliotheken (z.B. Maven Dependencies) bekannt.

Mit Hilfe der metadaten-basierten Schnittstellen-Dokumentation, wie Swagger (Open API Specification), liegt zur Kompilierzeit eine strukturierte Dokumenation des Service-Angebots vor.

Über zusätzliche Meta-Daten werden die Bereitstellung und das Konsumierung von Service-Schnittstellen identifiziert. Diese Daten werden beim Build-Prozess erzeugt und bereitstellt.

_Option_:<br>
Die Kommunikationsinfrastruktur, wie die Service Discovery, der Load-Balancer oder die Firewall, kann das Konsumieren der Service-Angebote, also die Kommunikationsbeziehungen zwischen den Microservices auflösen.

Ein Service zum Architektur-Management aggregiert und visualisiert die bereitgestellten Informationen.
Wünschenswert ist eine automatische Erzeugung der Software-Architektur auf Ebene der Kontext- und Struktursicht.
Bei Option 2 werden zusätzlich die Beziehungen zu anderen Microservices aufgelöst.

Siehe [original README](https://github.com/lehnert-andre/codebased-sw-architecture-documentation/blob/master/README.md)
