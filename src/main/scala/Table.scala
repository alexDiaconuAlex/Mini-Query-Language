
type Row = Map[String, String]
type Tabular = List[Row]

case class Table (tableName: String, tableData: Tabular) {
  
  // TODO 1.0
//  def header: List[String] = tableData.headOption.map(_.keys.toList).getOrElse(List.empty)
  def header: List[String] = tableData.headOption match {
    case Some(row) => row.keys.toList
    case None => List.empty
  }
  def data: Tabular = tableData
  def name: String = tableName


  // TODO 1.1
  override def toString: String = {
    val dataHeader = header.mkString(",")
    val dataBody = tableData.map{_.map{
      (key, value) => value
    }.mkString(",")
    }.mkString("\n")
    s"$dataHeader\n$dataBody"
  }

  // TODO 1.3
  def insert(row: Row): Table = {
    if (tableData.contains(row)) {
      this
    } else {
      val newTableData = tableData :+ row
      this.copy(tableData = newTableData)
    }
  }

  // TODO 1.4
  def delete(row: Row): Table = {
    val newTableData = tableData.filterNot(_ == row)
    this.copy(tableData = newTableData)
  }

  // TODO 1.5
  def sort(column: String, ascending: Boolean = true): Table = {
    val ascendingSortData = tableData.sortBy(row => row.get(column).getOrElse(""))
    if (ascending) {
      this.copy(tableData = ascendingSortData)
    } else {
      val reverseSort = ascendingSortData.reverse
      this.copy(tableData = reverseSort)
    }
  }

  // TODO 1.6
  def select(columns: List[String ]): Table = {
//    val newTable: Tabular = this.tableData.map {
//      row =>
//        row.filter{
//          (key, value) =>
//            columns.contains(key)
//        }
//    }
//    this.copy(tableData = newTable)
  val newTableData: Tabular = tableData.map(_.filter(
    (key, value) => columns.contains(key)
  ))

      this.copy(tableData = newTableData)
  }

  // TODO 1.7
  def cartesianProduct(otherTable: Table): Table = {
    val thisName = this.name
    val otherName = otherTable.name

    val newDataTable = this.tableData.flatMap{ rowA =>
      otherTable.tableData.map { rowB =>
        val renamedRowA = rowA.map { (key, value) =>
          (s"$thisName.$key", value)
        }
        val renamedRowB = rowB.map { (key, value) =>
          (s"$otherName.$key", value)
        }

        renamedRowA ++ renamedRowB
      }
    }
    this.copy(tableData = newDataTable)
  }
  
  // TODO 1.8
  def join(other: Table)(col1: String, col2: String): Table = {
    def mergeValues(v1: String, v2: String): String = (v1, v2) match {
      case ("", b) => b
      case (a, "") => a
      case (a, b) if a == b => a
      case (a, b) => s"$a;$b"
    }

    def mergeRows(
                   rowA: Row,
                   rowB: Row,
                   colA: String,
                   colB: String
                 ): Row = {
      val valueA = rowA.getOrElse(colA, "")
      val valueB = rowB.getOrElse(colB, "")
      val joinKey = mergeValues(valueA, valueB)

      val keysA = rowA.keySet - colA
      val keysB = rowB.keySet - colB

      val conflictKeys = keysA.intersect(keysB)
      val onlyAKeys = keysA -- conflictKeys
      val onlyBKeys = keysB -- conflictKeys

      val mergedConflicts = conflictKeys.map { k =>
        val v1 = rowA.getOrElse(k, "")
        val v2 = rowB.getOrElse(k, "")
        k -> mergeValues(v1, v2)
      }

      val onlyA = onlyAKeys.map(k => k -> rowA.getOrElse(k, ""))
      val onlyB = onlyBKeys.map(k => k -> rowB.getOrElse(k, ""))

      val completedA = if (rowA.isEmpty) {
        (keysA ++ conflictKeys).map(k => k -> "")
      } else Set.empty[(String, String)]

      val completedB = if (rowB.isEmpty) {
        (keysB ++ conflictKeys).map(k => k -> "")
      } else Set.empty[(String, String)]

      Map("colKey" -> joinKey, colA -> joinKey) ++
        onlyA ++ onlyB ++ mergedConflicts ++ completedA ++ completedB
    }

    def keyValue(row: Row, col: String): String = row.getOrElse(col, "")

    val mapB = other.tableData.groupBy(row => keyValue(row, col2))

    val matched = tableData.flatMap { rowA =>
      val keyA = keyValue(rowA, col1)
      val rowsB = mapB.getOrElse(keyA, List.empty)

      rowsB.map { rowB =>
        mergeRows(rowA, rowB, col1, col2)
      }
    }

    val matchedKeys = matched.map(_("colKey")).toSet

    val onlyInA = tableData
      .filter(row => !matchedKeys.contains(keyValue(row, col1)))
      .map(row => mergeRows(row, Map.empty, col1, col2))

    val keysInA = tableData.map(row => keyValue(row, col1)).toSet

    val onlyInB = other.tableData
      .filter(row => !keysInA.contains(keyValue(row, col2)))
      .map(row => mergeRows(Map.empty, row, col1, col2))

    val resultRows = matched ++ onlyInA ++ onlyInB

    val cleanedRows = resultRows.map(row => row - "colKey")

    val allKeys = (cleanedRows.flatMap(_.keySet) ++ Set(col1)).toSet.toList.distinct
    val alignedRows = cleanedRows.map { row =>
      allKeys.map(k => k -> row.getOrElse(k, "")).toMap
    }
    Table(this.tableName, alignedRows)

  }

  // TODO 2.3
  def filter(f: FilterCond): Table = {
    val filteredValues = this.tableData.filter { row =>
      f.eval(row) match {
        case Some(true) => true
        case _ => false
      }
    }
    this.copy(tableData = filteredValues)
  }
  
  // TODO 2.4
  def update(f: FilterCond, updates: Map[String, String]): Table = {
    val updatedBody = this.tableData.map { row =>
      f.eval(row) match {
        case Some(true) =>
          row ++ updates
        case _ => row
      }
    }
    this.copy(tableData = updatedBody)
  }
  // TODO 3.5
  // Implement indexing
  def apply(index: Int): Row = { // nu este folosita functia asta..
    this.tableData(index)
  }
}

object Table {
  // TODO 1.2
//  def fromCSV(csv: String): Table = {
//    val lines = csv.split("\n")
//    val tableName = lines.head // numele tabelei
//    val header = lines(1).split(",").map(_.trim).toList
//    val tableData: Tabular = lines.drop(2).map {
//      (lines) =>
//        val values = lines.split(",").map(_.trim).toList
//        header.zip(values).toMap
//    }.toList
//    Table(tableName, tableData)
//  }
  def fromCSV(csv: String): Table = {
    val lines = csv.trim.split("\n").toList

    val header = lines.head
    val headerProcessed = header.split(",").map(_.trim).toList

    val bodyProcessed: Tabular = lines.tail.map { line =>
      val lineProcessed = line.split(",").map(_.trim).toList

      headerProcessed.zip(lineProcessed).toMap
    }
    Table("", bodyProcessed)
  }
  // ^
  // |
  // TODO 1.9
  def apply(name: String, s: String): Table = {
    val lines = s.trim.split("\n").toList

    val header = lines.head
    val headerProcessed = header.split(",").map(_.trim).toList

    val bodyProcessed: Tabular = lines.tail.map { line =>
      val lineProcessed = line.split(",").map(_.trim).toList

      headerProcessed.zip(lineProcessed).toMap
    }
    Table(name, bodyProcessed)
  }
}