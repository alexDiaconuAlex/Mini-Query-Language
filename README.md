**# Tema 2 PP 2025 – Mini Query Language în Scala

## 📚 Scop

În această temă vei construi, în stil **100 % funcțional**, un mini‑limbaj de interogare (inspirat de SQL) peste o bază de date simplă implementată de tine. Vei lucra atât cu operații pe tabele individuale, cât și cu operații între tabele pe care le poți compune.

*Reguli stricte: fără efecte laterale, fără `var` – doar `val`, funcții pure și colecții imutabile.*

---

## 🗄️ Model de date

```scala
// O linie dintr‑un tabel
type Row     = Map[String, String]      // numeColoană -> valoare
// Un tabel ca listă de linii
type Tabular = List[Row]
```

### Clasa `Table`

```scala
case class Table(tableName: String, tableData: Tabular) {
  def header: List[String]                                   // numele coloanelor
  def data:   Tabular                                        // toate liniile
  def name:   String                                         // alias pentru tableName

  // ↔️ CSV
  override def toString: String                              // → CSV
  def fromCSV(csv: String): Table                            // CSV → Table

  // 📝 CRUD de bază
  def insert(row: Row): Table                                // adaugă linie
  def delete(row: Row): Table                                // șterge linii egale
  def sort(column: String, ascending: Boolean = true): Table // ordonează
  def select(columns: List[String]): Table                   // alege coloane

  // 🧩 Operații între tabele
  def cartesianProduct(other: Table): Table
  def join(other: Table)(col1: String, col2: String): Table
}

object Table {
  def apply(name: String, s: String): Table                  // parsează CSV
}
```

---

## 🔍 Filtre (DSL pentru WHERE‑uri)

Se definește un TDA expresiv:

```scala
trait FilterCond { def eval(r: Row): Option[Boolean] }
case class Field(col: String, pred: String => Boolean)            extends FilterCond
case class Compound(op: (Boolean, Boolean) => Boolean,
                    conditions: List[FilterCond])                extends FilterCond
case class Not(f: FilterCond)                                    extends FilterCond
case class Any(fs: List[FilterCond])                            extends FilterCond
case class All(fs: List[FilterCond])                            extends FilterCond

// Helpers
def And(f1: FilterCond, f2: FilterCond): FilterCond = ???
def Or (f1: FilterCond, f2: FilterCond): FilterCond = ???
def Equal(f1: FilterCond, f2: FilterCond): FilterCond = ???
```

Operatori infix și unari pentru sintaxă fluentă:

```scala
f1 && f2   // And
f1 || f2   // Or
!f1        // Not
f1 == f2   // Equal
```

`Table.filter` și `Table.update` folosesc aceste condiții.

---

## 🗂️ Clasa `Database`

```scala
case class Database(tables: List[Table]) {
  override def toString: String

  def insert(tableName: String): Database                     // adaugă tabel gol
  def update(tableName: String, newTable: Table): Option[Database]
  def delete(tableName: String): Database                     // șterge tabel
  def selectTables(names: List[String]): Option[Database]     // subset

  // Sugestie bonus: acces prin index:  db(0)  ⇒ primul tabel
}
```

---

## 🏗️ Sarcini principale (pe scurt)

| Nr | Tema                                   | Indicii                               |
| -- | -------------------------------------- | ------------------------------------- |
| 1  | Implementă toate metodele din `Table`  | conversie CSV, sort, select, join     |
| 2  | Construiește DSL‑ul de filtre          | `FilterCond`, operatori, evaluare     |
| 3  | Implementează operațiile pe `Database` | insert/update/delete/selectTables     |
| 4  | Suport `apply` pentru indexare rapidă  | atât pe `Table`, cât și pe `Database` |
| 5  | Scrie 3 query‑uri demonstrative        | vezi `query_1` … `query_3`            |

> **Hint:** păstrează codul modular; testează incremental.

---

## 🧪 Testare

Proiectul include teste unitare ScalaTest + Scalactic.

```bash
sbt test          # rulează toate testele
```

* Toate testele trebuie să treacă pentru a primi punctaj complet.

---

## 🚀 Rulare rapidă

```bash
# 1. Clonează / dezarhivează scheletul
unzip skel_tema2.zip -d tema2
cd tema2

# 2. Compilează și rulează un REPL sbt pentru experimente
sbt console
```

