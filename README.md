# Retail Insight Agent

![CI](https://github.com/Auticad/retail-insight-agent/actions/workflows/ci.yml/badge.svg)

Applicazione Java per l'analisi delle vendite di una catena di cinque negozi di abbigliamento.
Cresce settimana dopo settimana durante il corso «Sviluppatore Java e Agentic AI» (28 ottobre – 11 dicembre 2026):
dai KPI calcolati da console fino a un agente AI che risponde in linguaggio naturale interrogando il database
e i documenti interni, con le fonti citate.

**Stato:** scheletro iniziale (infrastruttura e dati). Il codice Java arriva tappa dopo tappa, seguendo la roadmap.

## Roadmap

| Versione | Settimana | Contenuto |
|---|---|---|
| `v0.1.0` | 28–30 ott | Progetto Maven, KPI di un prodotto da console |
| `v0.2.0` | 2–6 nov | Menu, validazione, metodi, matrice delle vendite, prime classi di dominio |
| `v0.3.0` | 9–13 nov | Package, record, test JUnit, CI, gerarchia prodotti, collezioni, repository generico |
| `v0.4.0` | 16–20 nov | Eccezioni, import/export CSV, Stream API, client meteo HTTP/JSON, PostgreSQL e JDBC |
| `v0.5.0` | 23–27 nov | LLM locale con Ollama, client OpenAI-compatible, commento ai KPI, primo ciclo agentico |
| `v0.6.0` | 30 nov–4 dic | Schema normalizzato, tool calling, agente sui dati, memoria, ReAct |
| `v1.0.0` | 9–11 dic | RAG con pgvector, orchestrazione, valutazione su 20 domande |

## Requisiti

- JDK 21 (es. Eclipse Temurin)
- Maven 3.9+
- Docker Desktop (backend WSL2 su Windows)
- Ollama con `llama3.1:8b` e `nomic-embed-text` (dalla settimana 5)

## Avvio del database

```bash
cp .env.example .env
docker compose up -d
docker compose exec db psql -U retail -d retail -c "SELECT COUNT(*), SUM(quantity * unit_price) FROM sales"
```

Il risultato atteso è `5019 | 866593.63`. Gli script in `db/init` vengono eseguiti solo alla prima creazione del volume:
per ripartire da zero usa `docker compose down -v`.

## Struttura

```text
.github/workflows/ci.yml   build e test a ogni push
db/init/                   schema iniziale, anagrafiche, caricamento vendite
data/sales.csv             vendite sintetiche set–nov 2026 (5.019 righe)
docs/company/              procedure aziendali usate dal RAG (guida F)
src/main/java/io/github/auticad/retail/
src/test/java/io/github/auticad/retail/
```

## Dati

Tutti i dati sono **sintetici**: negozi, prodotti e vendite sono inventati e generati con seme fisso (2026),
quindi chiunque cloni il repository ottiene gli stessi numeri. Nessun dato personale o aziendale reale.

## Licenza

MIT, vedi [LICENSE](LICENSE).
