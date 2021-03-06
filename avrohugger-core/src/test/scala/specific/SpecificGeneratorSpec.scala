package specific

import java.io.File

import avrohugger._
import avrohugger.format.SpecificRecord
import org.specs2._

class SpecificGeneratorSpec extends mutable.Specification {

  "a SpecificGenerator" should {

    "correctly generate from a protocol with messages" in {
      val infile = new java.io.File("avrohugger-core/src/test/avro/mail.avpr")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(infile, outDir)
      val sourceClass = util.Util.readFile(s"$outDir/example/proto/Message.scala")
      val sourceTrait = util.Util.readFile(s"$outDir/example/proto/Mail.scala")
      sourceClass ===
        """|/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
          |package example.proto
          |
          |import scala.annotation.switch
          |
          |case class Message(var to: String, var from: String, var body: String) extends org.apache.avro.specific.SpecificRecordBase {
          |  def this() = this("", "", "")
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        to
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 1 => {
          |        from
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 2 => {
          |        body
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.to = {
          |        value.toString
          |      }.asInstanceOf[String]
          |      case pos if pos == 1 => this.from = {
          |        value.toString
          |      }.asInstanceOf[String]
          |      case pos if pos == 2 => this.body = {
          |        value.toString
          |      }.asInstanceOf[String]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Message.SCHEMA$
          |}
          |
          |object Message {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Message\",\"namespace\":\"example.proto\",\"fields\":[{\"name\":\"to\",\"type\":\"string\"},{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"body\",\"type\":\"string\"}]}")
          |}""".stripMargin
          
      sourceTrait ===
        """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
          |package example.proto
          |
          |@SuppressWarnings(Array("all")) @org.apache.avro.specific.AvroGenerated trait Mail {
          |  def send(message: example.proto.Message): java.lang.CharSequence
          |  @SuppressWarnings(Array("all")) trait Callback extends Mail {
          |    final val PROTOCOL: org.apache.avro.Protocol = example.proto.Mail.PROTOCOL
          |    /** @throws java.io.IOException The async call could not be completed. */
          |    def send(message: example.proto.Message, callback: org.apache.avro.ipc.Callback[java.lang.CharSequence]): Unit
          |  }
          |}
          |
          |object Mail {
          |  final val PROTOCOL: org.apache.avro.Protocol = org.apache.avro.Protocol.parse("{\"protocol\":\"Mail\",\"namespace\":\"example.proto\",\"types\":[{\"type\":\"record\",\"name\":\"Message\",\"fields\":[{\"name\":\"to\",\"type\":\"string\"},{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"body\",\"type\":\"string\"}]}],\"messages\":{\"send\":{\"request\":[{\"name\":\"message\",\"type\":\"Message\"}],\"response\":\"string\"}}}")
          |}""".stripMargin
    }



    "correctly generate a specific case class definition from a schema as a string" in {
      val schemaString = """{"type":"record","name":"Person","namespace":"test","fields":[{"name":"name","type":"string"}],"doc:":"A basic schema for storing Twitter messages"}"""
      val gen = new Generator(SpecificRecord)
      val List(source) = gen.stringToStrings(schemaString)

      source ===
        """package test
          |
          |import scala.annotation.switch
          |
          |case class Person(var name: String) extends org.apache.avro.specific.SpecificRecordBase {
          |  def this() = this("")
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        name
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.name = {
          |        value.toString
          |      }.asInstanceOf[String]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Person.SCHEMA$
          |}
          |
          |object Person {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Person\",\"namespace\":\"test\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"}],\"doc:\":\"A basic schema for storing Twitter messages\"}")
          |}""".stripMargin
    }

    "correctly generate enums with SCHEMA$" in {
      val infile = new java.io.File("avrohugger-core/src/test/avro/enums.avsc")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(infile, outDir)

      val source = util.Util.readFile(s"$outDir/example/Suit.java")
      source ====
        """/**
          | * Autogenerated by Avro
          | * 
          | * DO NOT EDIT DIRECTLY
          | */
          |package example;  
          |@SuppressWarnings("all")
          |@org.apache.avro.specific.AvroGenerated
          |public enum Suit { 
          |  SPADES, DIAMONDS, CLUBS, HEARTS  ;
          |  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"Suit\",\"namespace\":\"example\",\"symbols\":[\"SPADES\",\"DIAMONDS\",\"CLUBS\",\"HEARTS\"]}");
          |  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
          |}
          |""".stripMargin
    }

    "correctly generate enums in AVDLs with `SpecificRecord`" in {
      val infile = new java.io.File("avrohugger-core/src/test/avro/enums.avdl")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(infile, outDir)

      val sourceEnum = util.Util.readFile(s"$outDir/example/idl/Suit.java")
      sourceEnum ====
        """/**
          | * Autogenerated by Avro
          | * 
          | * DO NOT EDIT DIRECTLY
          | */
          |package example.idl;  
          |@SuppressWarnings("all")
          |@org.apache.avro.specific.AvroGenerated
          |public enum Suit { 
          |  SPADES, DIAMONDS, CLUBS, HEARTS  ;
          |  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"Suit\",\"namespace\":\"example.idl\",\"symbols\":[\"SPADES\",\"DIAMONDS\",\"CLUBS\",\"HEARTS\"]}");
          |  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
          |}
          |""".stripMargin
  
      val sourceRecord = util.Util.readFile(s"$outDir/example/idl/EnumProtocol.scala")
      sourceRecord ====
        """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
          |package example.idl
          |
          |import scala.annotation.switch
          |
          |case class Card(var suit: Suit, var number: Int) extends org.apache.avro.specific.SpecificRecordBase {
          |  def this() = this(null, 0)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        suit
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 1 => {
          |        number
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.suit = {
          |        value
          |      }.asInstanceOf[Suit]
          |      case pos if pos == 1 => this.number = {
          |        value
          |      }.asInstanceOf[Int]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Card.SCHEMA$
          |}
          |
          |object Card {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Card\",\"namespace\":\"example.idl\",\"fields\":[{\"name\":\"suit\",\"type\":{\"type\":\"enum\",\"name\":\"Suit\",\"symbols\":[\"SPADES\",\"DIAMONDS\",\"CLUBS\",\"HEARTS\"]}},{\"name\":\"number\",\"type\":\"int\"}]}")
          |}""".stripMargin.trim
    }

    "correctly generate bytes with SCHEMA$" in {
      val infile = new java.io.File("avrohugger-core/src/test/avro/bytes.avsc")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(infile, outDir)

      val source = util.Util.readFile(s"$outDir/example/Binary.scala")
      source ====
        """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
          |package example
          |
          |import scala.annotation.switch
          |
          |case class Binary(var data: Array[Byte]) extends org.apache.avro.specific.SpecificRecordBase {
          |  def this() = this(null)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        java.nio.ByteBuffer.wrap(data)
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.data = {
          |        value match {
          |          case (buffer: java.nio.ByteBuffer) => {
          |            buffer.array()
          |          }
          |        }
          |      }.asInstanceOf[Array[Byte]]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Binary.SCHEMA$
          |}
          |
          |object Binary {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Binary\",\"namespace\":\"example\",\"fields\":[{\"name\":\"data\",\"type\":\"bytes\"}]}")
          |}""".stripMargin.trim
    }

    "correctly generate bytes in AVDLs with `SpecificRecord`" in {
      val infile = new java.io.File("avrohugger-core/src/test/avro/bytes.avdl")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(infile, outDir)

      val sourceRecord = util.Util.readFile(s"$outDir/example/idl/BinaryIDL.scala")
      sourceRecord ====
        """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
          |package example.idl
          |
          |import scala.annotation.switch
          |
          |case class Binary(var data: Array[Byte]) extends org.apache.avro.specific.SpecificRecordBase {
          |  def this() = this(null)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        java.nio.ByteBuffer.wrap(data)
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.data = {
          |        value match {
          |          case (buffer: java.nio.ByteBuffer) => {
          |            buffer.array()
          |          }
          |        }
          |      }.asInstanceOf[Array[Byte]]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Binary.SCHEMA$
          |}
          |
          |object Binary {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Binary\",\"namespace\":\"example.idl\",\"fields\":[{\"name\":\"data\",\"type\":\"bytes\"}]}")
          |}""".stripMargin.trim
    }



    "correctly generate nested enums in AVSCs with `SpecificRecord`" in {
      val infile = new java.io.File("avrohugger-core/src/test/avro/enums_nested.avsc")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(infile, outDir)

      val sourceEnum = util.Util.readFile(s"$outDir/example/Direction.java")
      sourceEnum ====
      """/**
        | * Autogenerated by Avro
        | * 
        | * DO NOT EDIT DIRECTLY
        | */
        |package example;  
        |@SuppressWarnings("all")
        |@org.apache.avro.specific.AvroGenerated
        |public enum Direction { 
        |  NORTH, SOUTH, EAST, WEST  ;
        |  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"Direction\",\"namespace\":\"example\",\"symbols\":[\"NORTH\",\"SOUTH\",\"EAST\",\"WEST\"]}");
        |  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
        |}
        |""".stripMargin

      val sourceRecord = util.Util.readFile(s"$outDir/example/Compass.scala")
      sourceRecord ====
      """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
        |package example
        |
        |import scala.annotation.switch
        |
        |case class Compass(var direction: Direction) extends org.apache.avro.specific.SpecificRecordBase {
        |  def this() = this(null)
        |  def get(field$: Int): AnyRef = {
        |    (field$: @switch) match {
        |      case pos if pos == 0 => {
        |        direction
        |      }.asInstanceOf[AnyRef]
        |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
        |    }
        |  }
        |  def put(field$: Int, value: Any): Unit = {
        |    (field$: @switch) match {
        |      case pos if pos == 0 => this.direction = {
        |        value
        |      }.asInstanceOf[Direction]
        |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
        |    }
        |    ()
        |  }
        |  def getSchema: org.apache.avro.Schema = Compass.SCHEMA$
        |}
        |
        |object Compass {
        |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Compass\",\"namespace\":\"example\",\"fields\":[{\"name\":\"direction\",\"type\":{\"type\":\"enum\",\"name\":\"Direction\",\"symbols\":[\"NORTH\",\"SOUTH\",\"EAST\",\"WEST\"]}}]}")
        |}""".stripMargin
    }


    "correctly generate default values in AVDL with `SpecificRecord`" in {
      val infile = new java.io.File("avrohugger-core/src/test/avro/defaults.avdl")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(infile, outDir)

      val sourceRecord = util.Util.readFile(s"$outDir/example/idl/Defaults.scala")
      sourceRecord ====
        """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
          |package example.idl
          |
          |import scala.annotation.switch
          |
          |sealed trait Defaults extends Product with Serializable
          |
          |final case class Embedded(var inner: Int) extends org.apache.avro.specific.SpecificRecordBase with Defaults {
          |  def this() = this(0)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        inner
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.inner = {
          |        value
          |      }.asInstanceOf[Int]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Embedded.SCHEMA$
          |}
          |
          |final object Embedded {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Embedded\",\"namespace\":\"example.idl\",\"fields\":[{\"name\":\"inner\",\"type\":\"int\"}]}")
          |}
          |
          |final case class DefaultTest(var suit: DefaultEnum = DefaultEnum.SPADES, var number: Int = 0, var str: String = "str", var optionString: Option[String] = None, var optionStringValue: Option[String] = Some("default"), var embedded: Embedded = new Embedded(1), var defaultArray: List[Int] = List(1, 3, 4, 5), var optionalEnum: Option[DefaultEnum] = None, var defaultMap: Map[String, String] = Map("Hello" -> "world", "Merry" -> "Christmas")) extends org.apache.avro.specific.SpecificRecordBase with Defaults {
          |  def this() = this(DefaultEnum.SPADES, 0, "str", None, Some("default"), new Embedded(1), List(1, 3, 4, 5), None, Map("Hello" -> "world", "Merry" -> "Christmas"))
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        suit
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 1 => {
          |        number
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 2 => {
          |        str
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 3 => {
          |        optionString match {
          |          case Some(x) => x
          |          case None => null
          |        }
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 4 => {
          |        optionStringValue match {
          |          case Some(x) => x
          |          case None => null
          |        }
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 5 => {
          |        embedded
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 6 => {
          |        scala.collection.JavaConversions.bufferAsJavaList({
          |          defaultArray map { x =>
          |            x
          |          }
          |        }.toBuffer)
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 7 => {
          |        optionalEnum match {
          |          case Some(x) => x
          |          case None => null
          |        }
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 8 => {
          |        val map: java.util.HashMap[String, Any] = new java.util.HashMap[String, Any]
          |        defaultMap foreach { kvp =>
          |          val key = kvp._1
          |          val value = kvp._2
          |          map.put(key, value)
          |        }
          |        map
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.suit = {
          |        value
          |      }.asInstanceOf[DefaultEnum]
          |      case pos if pos == 1 => this.number = {
          |        value
          |      }.asInstanceOf[Int]
          |      case pos if pos == 2 => this.str = {
          |        value.toString
          |      }.asInstanceOf[String]
          |      case pos if pos == 3 => this.optionString = {
          |        value match {
          |          case null => None
          |          case _ => Some(value.toString)
          |        }
          |      }.asInstanceOf[Option[String]]
          |      case pos if pos == 4 => this.optionStringValue = {
          |        value match {
          |          case null => None
          |          case _ => Some(value.toString)
          |        }
          |      }.asInstanceOf[Option[String]]
          |      case pos if pos == 5 => this.embedded = {
          |        value
          |      }.asInstanceOf[Embedded]
          |      case pos if pos == 6 => this.defaultArray = {
          |        value match {
          |          case (array: java.util.List[_]) => {
          |            List((scala.collection.JavaConversions.asScalaIterator(array.iterator).toSeq map { x =>
          |              x
          |            }: _*))
          |          }
          |        }
          |      }.asInstanceOf[List[Int]]
          |      case pos if pos == 7 => this.optionalEnum = {
          |        value match {
          |          case null => None
          |          case _ => Some(value)
          |        }
          |      }.asInstanceOf[Option[DefaultEnum]]
          |      case pos if pos == 8 => this.defaultMap = {
          |        value match {
          |          case (map: java.util.Map[_,_]) => {
          |            scala.collection.JavaConversions.mapAsScalaMap(map).toMap map { kvp =>
          |              val key = kvp._1.toString
          |              val value = kvp._2
          |              (key, value.toString)
          |            }
          |          }
          |        }
          |      }.asInstanceOf[Map[String, String]]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = DefaultTest.SCHEMA$
          |}
          |
          |final object DefaultTest {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"DefaultTest\",\"namespace\":\"example.idl\",\"fields\":[{\"name\":\"suit\",\"type\":{\"type\":\"enum\",\"name\":\"DefaultEnum\",\"symbols\":[\"SPADES\",\"DIAMONDS\",\"CLUBS\",\"HEARTS\"]},\"default\":\"SPADES\"},{\"name\":\"number\",\"type\":\"int\",\"default\":0},{\"name\":\"str\",\"type\":\"string\",\"default\":\"str\"},{\"name\":\"optionString\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"optionStringValue\",\"type\":[\"string\",\"null\"],\"default\":\"default\"},{\"name\":\"embedded\",\"type\":{\"type\":\"record\",\"name\":\"Embedded\",\"fields\":[{\"name\":\"inner\",\"type\":\"int\"}]},\"default\":{\"inner\":1}},{\"name\":\"defaultArray\",\"type\":{\"type\":\"array\",\"items\":\"int\"},\"default\":[1,3,4,5]},{\"name\":\"optionalEnum\",\"type\":[\"null\",\"DefaultEnum\"],\"default\":null},{\"name\":\"defaultMap\",\"type\":{\"type\":\"map\",\"values\":\"string\"},\"default\":{\"Hello\":\"world\",\"Merry\":\"Christmas\"}}]}")
          |}""".stripMargin
    }

    "correctly generate records depending on others defined in different AVDL and AVSC files" in {
      val importing = new java.io.File("avrohugger-core/src/test/avro/import.avdl")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(importing, outDir)

      val sourceRecord = util.Util.readFile(s"$outDir/example/idl/ImportProtocol.scala")
      sourceRecord ===
      """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
        |package example.idl
        |
        |import scala.annotation.switch
        |
        |import other.ns.ExternalDependency
        |
        |import other.ns.ImportedSchema
        |
        |sealed trait ImportProtocol extends Product with Serializable
        |
        |final case class DependentRecord(var dependency: ExternalDependency, var number: Int) extends org.apache.avro.specific.SpecificRecordBase with ImportProtocol {
        |  def this() = this(new ExternalDependency, 0)
        |  def get(field$: Int): AnyRef = {
        |    (field$: @switch) match {
        |      case pos if pos == 0 => {
        |        dependency
        |      }.asInstanceOf[AnyRef]
        |      case pos if pos == 1 => {
        |        number
        |      }.asInstanceOf[AnyRef]
        |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
        |    }
        |  }
        |  def put(field$: Int, value: Any): Unit = {
        |    (field$: @switch) match {
        |      case pos if pos == 0 => this.dependency = {
        |        value
        |      }.asInstanceOf[ExternalDependency]
        |      case pos if pos == 1 => this.number = {
        |        value
        |      }.asInstanceOf[Int]
        |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
        |    }
        |    ()
        |  }
        |  def getSchema: org.apache.avro.Schema = DependentRecord.SCHEMA$
        |}
        |
        |final object DependentRecord {
        |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"DependentRecord\",\"namespace\":\"example.idl\",\"fields\":[{\"name\":\"dependency\",\"type\":{\"type\":\"record\",\"name\":\"ExternalDependency\",\"namespace\":\"other.ns\",\"fields\":[{\"name\":\"number\",\"type\":\"int\"}]}},{\"name\":\"number\",\"type\":\"int\"}]}")
        |}
        |
        |final case class DependentRecord2(var dependency: ImportedSchema, var name: String) extends org.apache.avro.specific.SpecificRecordBase with ImportProtocol {
        |  def this() = this(new ImportedSchema, "")
        |  def get(field$: Int): AnyRef = {
        |    (field$: @switch) match {
        |      case pos if pos == 0 => {
        |        dependency
        |      }.asInstanceOf[AnyRef]
        |      case pos if pos == 1 => {
        |        name
        |      }.asInstanceOf[AnyRef]
        |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
        |    }
        |  }
        |  def put(field$: Int, value: Any): Unit = {
        |    (field$: @switch) match {
        |      case pos if pos == 0 => this.dependency = {
        |        value
        |      }.asInstanceOf[ImportedSchema]
        |      case pos if pos == 1 => this.name = {
        |        value.toString
        |      }.asInstanceOf[String]
        |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
        |    }
        |    ()
        |  }
        |  def getSchema: org.apache.avro.Schema = DependentRecord2.SCHEMA$
        |}
        |
        |final object DependentRecord2 {
        |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"DependentRecord2\",\"namespace\":\"example.idl\",\"fields\":[{\"name\":\"dependency\",\"type\":{\"type\":\"record\",\"name\":\"ImportedSchema\",\"namespace\":\"other.ns\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"}]}},{\"name\":\"name\",\"type\":\"string\"}]}")
        |}""".stripMargin
    }

    "correctly generate records depending on others defined in a different AVDL file and in a nested field" in {
      val importing = new java.io.File("avrohugger-core/src/test/avro/import-nested.avdl")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(importing, outDir)

      val sourceRecord = util.Util.readFile(s"$outDir/example/idl/ImportNestedProtocol.scala")
      sourceRecord ===
        """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
          |package example.idl
          |
          |import scala.annotation.switch
          |
          |import other.ns.ExternalDependency
          |
          |case class DependentOptionalRecord(var dependency: Option[ExternalDependency], var number: Int) extends org.apache.avro.specific.SpecificRecordBase {
          |  def this() = this(None, 0)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        dependency match {
          |          case Some(x) => x
          |          case None => null
          |        }
          |      }.asInstanceOf[AnyRef]
          |      case pos if pos == 1 => {
          |        number
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.dependency = {
          |        value match {
          |          case null => None
          |          case _ => Some(value)
          |        }
          |      }.asInstanceOf[Option[ExternalDependency]]
          |      case pos if pos == 1 => this.number = {
          |        value
          |      }.asInstanceOf[Int]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = DependentOptionalRecord.SCHEMA$
          |}
          |
          |object DependentOptionalRecord {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"DependentOptionalRecord\",\"namespace\":\"example.idl\",\"fields\":[{\"name\":\"dependency\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"ExternalDependency\",\"namespace\":\"other.ns\",\"fields\":[{\"name\":\"number\",\"type\":\"int\"}]}]},{\"name\":\"number\",\"type\":\"int\"}]}")
          |}""".stripMargin
    }

    "not generate copy of imported classes in the importing package" in {
      val importing = new java.io.File("avrohugger-core/src/test/avro/import.avdl")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(importing, outDir)

      (new File(s"$outDir/example/idl/ImportedProtocol.scala")).exists === false
    }

    "Generate imported classes in the declared package" in {
      val importing = new java.io.File("avrohugger-core/src/test/avro/import.avdl")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(importing, outDir)

      (new File(s"$outDir/other/ns/ImportedProtocol.scala")).exists === true
    }

    "correctly generate an empty case class definition from an empty record" in {
      val infile = new java.io.File("avrohugger-core/src/test/avro/AvroTypeProviderTestEmptyRecord.avdl")
      val gen = new Generator(SpecificRecord)
      val outDir = gen.defaultOutputDir + "/specific/"
      gen.fileToFile(infile, outDir)
      val source = util.Util.readFile(s"$outDir/test/Calculator.scala")
      source ===
        """/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
          |package test
          |
          |import scala.annotation.switch
          |
          |sealed trait Calculator extends Product with Serializable
          |
          |final case class Added(var value: Int) extends org.apache.avro.specific.SpecificRecordBase with Calculator {
          |  def this() = this(0)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        value
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.value = {
          |        value
          |      }.asInstanceOf[Int]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Added.SCHEMA$
          |}
          |
          |final object Added {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Added\",\"namespace\":\"test\",\"fields\":[{\"name\":\"value\",\"type\":\"int\"}]}")
          |}
          |
          |final case class Subtracted(var value: Int) extends org.apache.avro.specific.SpecificRecordBase with Calculator {
          |  def this() = this(0)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        value
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.value = {
          |        value
          |      }.asInstanceOf[Int]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Subtracted.SCHEMA$
          |}
          |
          |final object Subtracted {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Subtracted\",\"namespace\":\"test\",\"fields\":[{\"name\":\"value\",\"type\":\"int\"}]}")
          |}
          |
          |final case class Divided(var value: Int) extends org.apache.avro.specific.SpecificRecordBase with Calculator {
          |  def this() = this(0)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        value
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.value = {
          |        value
          |      }.asInstanceOf[Int]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Divided.SCHEMA$
          |}
          |
          |final object Divided {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Divided\",\"namespace\":\"test\",\"fields\":[{\"name\":\"value\",\"type\":\"int\"}]}")
          |}
          |
          |final case class Multiplied(var value: Int) extends org.apache.avro.specific.SpecificRecordBase with Calculator {
          |  def this() = this(0)
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => {
          |        value
          |      }.asInstanceOf[AnyRef]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case pos if pos == 0 => this.value = {
          |        value
          |      }.asInstanceOf[Int]
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Multiplied.SCHEMA$
          |}
          |
          |final object Multiplied {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Multiplied\",\"namespace\":\"test\",\"fields\":[{\"name\":\"value\",\"type\":\"int\"}]}")
          |}
          |
          |final case class Reset() extends org.apache.avro.specific.SpecificRecordBase with Calculator {
          |  def get(field$: Int): AnyRef = {
          |    (field$: @switch) match {
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |  }
          |  def put(field$: Int, value: Any): Unit = {
          |    (field$: @switch) match {
          |      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
          |    }
          |    ()
          |  }
          |  def getSchema: org.apache.avro.Schema = Reset.SCHEMA$
          |}
          |
          |final object Reset {
          |  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Reset\",\"namespace\":\"test\",\"fields\":[]}")
          |}""".stripMargin
          
      }  
        
  }

}
