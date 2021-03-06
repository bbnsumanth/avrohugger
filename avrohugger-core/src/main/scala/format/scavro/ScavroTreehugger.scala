package avrohugger
package format
package scavro

import trees.{ ScavroCaseClassTree, ScavroObjectTree, ScavroTraitTree }
import input.reflectivecompilation.schemagen._

import avrohugger.input.DependencyInspector._
import avrohugger.input.NestedSchemaExtractor._

import org.apache.avro.{ Protocol, Schema }
import org.apache.avro.Schema.Field
import org.apache.avro.Schema.Type.{ ENUM, RECORD }
import treehugger.forest._
import definitions._
import treehuggerDSL._

import scala.collection.JavaConversions._

object ScavroTreehugger {

	def asScalaCodeString(
		classStore: ClassStore,
    schemaOrProtocol: Either[Schema, Protocol],
		namespace: Option[String],
    typeMatcher: TypeMatcher,
	  schemaStore: SchemaStore): String = {
			
		val imports: List[Import] = schemaOrProtocol match {
			case Left(schema) => 
			  if(schema.getType == RECORD) {
					ScavroImports.getImports(schema, namespace, schemaStore)
				}
				else List.empty
			case Right(protocol) => protocol.getTypes.toList.flatMap(schema => 
				ScavroImports.getImports(schema, namespace, schemaStore))
		}

		val topLevelDefs: List[Tree] = 
			asTopLevelDef(
				classStore,
				namespace,
				schemaOrProtocol,
				typeMatcher,
				None,
			  None)

    // wrap the imports and classdef in a block with a comment and a package
		val tree = {
      val blockContent = imports ++ topLevelDefs
      if (namespace.isDefined) BLOCK(blockContent).inPackage(namespace.get)
      else BLOCK(blockContent:_*).withoutPackage
    }.withDoc("MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY")
		
    // SpecificCompiler can't return a tree for Java enums, so return
    // a String here for a consistent api vis a vis *ToFile and *ToStrings
    treeToString(tree)
  }
	
	def asTopLevelDef(
    classStore: ClassStore,
    namespace: Option[String],
		schemaOrProtocol: Either[Schema, Protocol],
    typeMatcher: TypeMatcher,
    maybeBaseTrait: Option[String],
	  maybeFlags: Option[List[Long]]): List[Tree] = {
			
		schemaOrProtocol match {
			case Left(schema) => ScavroSchemaHandler.toTrees(
				classStore,
				namespace,
				schema,
				typeMatcher,
				maybeBaseTrait,
				maybeFlags
			)
			case Right(protocol) => ScavroProtocolHandler.toTrees(
				classStore,
				namespace,
				protocol,
				typeMatcher,
				maybeBaseTrait,
				maybeFlags
			)
		}
		
  }
	
}
