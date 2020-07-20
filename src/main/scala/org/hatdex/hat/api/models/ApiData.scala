/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package org.hatdex.hat.api.models

import org.joda.time.LocalDateTime

@Deprecated
case class ApiDataField(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    tableId: Option[Int],
    name: String,
    values: Option[Seq[ApiDataValue]])

@Deprecated
case class ApiDataRecord(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    tables: Option[Seq[ApiDataTable]])

@Deprecated
object ApiDataRecord {
  def flattenRecordValues(record: ApiDataRecord): Map[String, Any] = {
    Map(record.tables.getOrElse(Seq()).map { table =>
      table.name -> ApiDataTable.flattenTableValues(table)
    }: _*)
  }
}

@Deprecated
case class ApiDataTable(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    source: String,
    fields: Option[Seq[ApiDataField]],
    subTables: Option[Seq[ApiDataTable]])

@Deprecated
object ApiDataTable {
  /*
   * Reformats API Data table with values to a format where field/subtable name is an object key and values or subtables are the values
   */
  def flattenTableValues(dataTable: ApiDataTable): Map[String, Any] = {
    val fieldObjects = dataTable.fields.map { fields =>
      Map[String, Any](
        fields flatMap { field =>
          val maybeValues = field.values match {
            case Some(values) if values.isEmpty     => None
            case Some(values) if values.length == 1 => Some(values.head.value)
            case Some(values)                       => Some(values.map(_.value))
            case None                               => None
          }
          maybeValues.map { values => field.name -> values }
        }: _*)
    }

    val subtableObjects = dataTable.subTables.map { subtables =>
      Map[String, Any](subtables map { subtable =>
        subtable.name -> flattenTableValues(subtable)
      }: _*)
    }

    fieldObjects.getOrElse(Map()) ++ subtableObjects.getOrElse(Map())
  }

  def extractValues(dataTable: ApiDataTable): List[ApiDataValue] = {
    val fieldValues = dataTable.fields.map { fields =>
      fields.toList.flatMap { field =>
        field.values.getOrElse(List())
      }
    } getOrElse List()

    val subtableValues = dataTable.subTables.map { subtables =>
      subtables.toList.flatMap(extractValues)
    } getOrElse List()

    fieldValues ++ subtableValues
  }
}

@Deprecated
case class ApiDataValue(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    value: String,
    field: Option[ApiDataField],
    record: Option[ApiDataRecord])

@Deprecated
case class ApiRecordValues(
    record: ApiDataRecord,
    values: Seq[ApiDataValue])
