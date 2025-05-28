object Queries {
  def query_1(db: Database, ageLimit: Int, cities: List[String]): Option[Table] = {
//    val aici = db.tables.find(_.name == "Customers")
//    db.tables.find(_.name == "Customers") match {
//      case Some(table) => {
//        Some(table.filter(
//          And(
//            Field("age", _.toInt > ageLimit),
//            Field("city", cities.contains)
//          )
//        ).sort("id"))
//      }
//      case None => None
//    }
    db.tables.find(_.name == "Customers").map(
      _.filter(
        And(
          Field("age", _.toInt > ageLimit),
          Field("city", cities.contains)
        )
      ).sort("id")
    )
  }

  def query_2(db: Database, date: String, employeeID: Int): Option[Table] = {
    db.tables.find(_.name == "Orders").map {
      _.filter(
        And(
          Field("date", _ > date),
          Field("employee_id", _ != employeeID.toString)
        )
        ).select(List("order_id", "cost"))
        .sort("cost", false)
    }
  }

  def query_3(db: Database, minCost: Int): Option[Table] = {
    db.tables.find(_.name == "Orders").map {
      _.filter(
          Field("cost", _.toInt > minCost)
      ).select(List("order_id", "employee_id", "cost"))
        .sort("employee_id")
    }
  }
}
