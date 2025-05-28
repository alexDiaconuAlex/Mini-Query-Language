case class Database(tables: List[Table]) {

  // TODO 3.0
  override def toString: String = {
    tables.map { // functia asta nu primeste punctaj aparent
      table => s"${table.name}\n${table.data.toString}"
    }.mkString("\n")
  }

  // TODO 3.1
  def insert(tableName: String): Database = {
    val checkExists = tables.exists(_.name == tableName)
    if (checkExists) {
      this
    } else {
      val newTable = Table(tableName, List.empty)

      val newTables = tables :+ newTable

      this.copy(tables = newTables)
    }
  }

  // TODO 3.2
  def update(tableName: String, newTable: Table): Database = {
    val existName = this.tables.exists(_. name == tableName)

    if (existName) {
      val newTables = this.tables.map {table =>
        if (table.name == tableName) {
          newTable
        } else {
          table
        }
      }
      this.copy(tables = newTables)
    } else {
      this
    }
  }
  // TODO 3.3
  def delete(tableName: String): Database = {
    val newTables: List[Table] = this.tables.filterNot(tables =>
      tables.name == tableName
    )
    this.copy(tables = newTables)
//    Database(newTables)
  }

  // TODO 3.4
  def selectTables(tableNames: List[String]): Option[Database] = {
    val missingElement: Option[String] = tableNames.find { currentName => // find retunreaza un option
      !this.tables.exists(table => table.name == currentName)
    }

    missingElement match {
      case Some(name) => None
      case None => {
        val filteredTables = this.tables.filter {
          table => tableNames.contains(table.name)
        }
        Option(Database(filteredTables))
      }
    }

  }

  // TODO 3.5
  // Implement indexing here
  def apply(index: Int): Table = {
    this.tables(index)
  }
}
