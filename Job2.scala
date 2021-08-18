import Problem2.result2
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, col, collect_set, count, sum, to_date, when}
object Problem2 extends App {
  val spark = SparkSession
    .builder()
    .appName("Spark SQL basic example")
    .config("spark.master", "local")
    .getOrCreate()

  import spark.implicits._

  import org.apache.spark.sql.expressions.Window
  import org.apache.spark.sql.functions.row_number

  val ordersDF = spark.read.json("C:\\Users\\BUSRA\\IdeaProjects\\Job2\\data/orders.json")
  val productsDF = spark.read.json("C:\\Users\\BUSRA\\IdeaProjects\\Job2\\data/products.json")

  val jointables = ordersDF.join(productsDF).where($"product_id" === $"productid")

  val top10seller = jointables
    .groupBy($"seller_id")
    .agg(count("productid") as "sales_amount", sum("price"))
    .orderBy(sum("price") desc)
    .select($"seller_id", $"sales_amount")
    .limit(10)

  val top_cathegories_for_seller = jointables
    .groupBy($"seller_id", $"categoryname", $"order_date")
    .agg(sum("price") as "a")
    .withColumn("top_selling_category", collect_set("categoryname").over(Window.partitionBy("seller_id", "a")))
    .withColumn("rn", row_number.over(Window.partitionBy($"seller_id").orderBy($"a" desc)))
    .where($"rn" === 1)
    .select($"seller_id", $"top_selling_category", to_date($"order_date") as "date")

  val result2 = top10seller.join(top_cathegories_for_seller, "seller_id")

      result2.show(50, false)

}



