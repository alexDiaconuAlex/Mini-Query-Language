import scala.language.implicitConversions

trait FilterCond {
  def eval(r: Row): Option[Boolean]

  // TODO 2.2
  def ===(other: FilterCond): FilterCond = {
    Equal(this, other)
  }
  def &&(other: FilterCond): FilterCond = {
    And(this, other)
  }
  def ||(other: FilterCond): FilterCond = {
    Or(this, other)
  }
  def unary_! : FilterCond = {
    Not(this)
  }
}
//aceasta fct .get() ia fiecare col si .map() verif daca respecta un anumit predicat

case class Field(colName: String, predicate: String => Boolean) extends FilterCond {
  override def eval(r: Row): Option[Boolean] =  {
    r.get(colName).map(predicate)
  }
}

case class Compound(op: (Boolean, Boolean) => Boolean, conditions: List[FilterCond]) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    val result = conditions.flatMap(cond => cond.eval(r))

    Some(result.reduce(op))
  }
}

case class Not(f: FilterCond) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    f.eval(r).map(booleanValue => !booleanValue)
  }
}

def And(f1: FilterCond, f2: FilterCond): FilterCond = {
  All(List(f1, f2))
}
def Or(f1: FilterCond, f2: FilterCond): FilterCond = {
  Any(List(f1, f2))
}
case class Equal(f1: FilterCond, f2: FilterCond) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    val res1 = f1.eval(r)
    val res2 = f2.eval(r)

    (res1, res2) match {
      case (Some(a), Some(b)) => Some(a == b)
      case _ => None
    }
  }
}

case class Any(fs: List[FilterCond]) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    fs.foldLeft(Some(false): Option[Boolean]) { (acc, cond) =>
      acc match {
        case Some(true) => Some(true)
        case None => None
        case Some(false) => cond.eval(r)
      }
    }
  }
}

case class All(fs: List[FilterCond]) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    fs.foldLeft(Some(true): Option[Boolean]) { (acc, cond) =>
      acc match {
        case Some(false) => Some(false)
        case None => None
        case Some(true) => cond.eval(r)
      }
    }
  }
}