# jutil

[![Build Status](https://travis-ci.org/chrisgleissner/jutil.svg?branch=master)](https://travis-ci.org/chrisgleissner/jutil)
[![Maven Central](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/github/chrisgleissner/jutil-protobuf/maven-metadata.xml.svg)](https://search.maven.org/artifact/com.github.chrisgleissner/jutil-protobuf)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/jutil/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/jutil?branch=master)

Various Java utilities for Protobuf messages, pretty-printing of tables, etc.


## Protobuf

Maven Dependency:

```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>jutil-protobuf</artifactId>
    <version>1.0.3</version>
</dependency>
```

The [ProtobufFieldPartitioner](https://github.com/chrisgleissner/jutil/blob/master/protobuf/src/main/java/com/github/chrisgleissner/jutil/protobuf/ProtobufFieldPartitioner.java) 
is useful for distributing the elements of a repeated field in a Protobuf message over multiple newly created messages. 

This allows for sending a Protobuf message where size restrictions exist, for example when using the
<a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quotas">Azure Message ServiceBus</a>.

Example:
```java
Collection<Message> msgs = ProtbufFieldPartitioner.partition(msg, repeatedFieldToBePartitioned, 100);
```

## Table Printer

### Getting Started

The [TablePrinter](https://github.com/chrisgleissner/jutil/blob/master/table/src/main/java/com/github/chrisgleissner/jutil/table/TablePrinter.java) 
serializes a table to a pretty-printed string, either using ASCII or UTF borders.

A table consists of a header and a random number of rows. These can can be specified as an `Iterable<String>` header 
and `Iterable<Iterable<String>>` rows. Adapters to various 3rd party frameworks are available, see below. 


Maven Dependency:

```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>jutil-table</artifactId>
    <version>1.0.3</version>
</dependency>
```


Example:
```java
Iterable<String> headers = Arrays.asList("firstName", "lastName");
Iterable<Iterable<String>> rows = Arrays.asList(Arrays.asList("john", "doe"), Arrays.asList("joe", "doe"));
System.out.println(DefaultTablePrinter.print(headers, rows));
```
results in:
```
+===========+==========+
| firstName | lastName |
|===========|==========|
| john      | doe      |
| joe       | doe      |
+===========+==========+
```

### Configuration

The TablePrinter is fully configurable to customize your output:

```java
Iterable<String> headers = Arrays.asList("firstName", "lastName");
Iterable<Iterable<String>> rows = Arrays.asList(
        Arrays.asList("Tom", "Selleck"), 
        Arrays.asList("John", "Hillerman"),
        Arrays.asList("Roger E.", null), 
        Arrays.asList("Larry", "Manetti"));

System.out.println(TablePrinter.builder()
        .horizontalDividers(true)
        .nullValue("n/a")
        .tableFormat(new Utf8TableFormat())
        .rowNumbers(true)
        .startRow(1)
        .endRow(3)
        .maxCellWidth(5)
        .build().print(headers, rows));
```
results in:
```
╔═══╤═══════╤═══════╗
║ # │ first │ lastN ║
╠═══╪═══════╪═══════╣
║ 1 │ John  │ Hille ║
╟───┼───────┼───────╢
║ 2 │ Roger │ n/a   ║
╟───┼───────┼───────╢
║ 3 │ Larry │ Manet ║
╚═══╧═══════╧═══════╝
```

As per the example above, if you have a very large data structure, you may want to use the `startRow` and `endRow` builder methods 
to only print the specified range.

Likewise, if you have very long columns, you can limit their printed lengths with the `maxCellWidth` method.

### 3rd Party Adapters

Any data structure that implements the [Table](https://github.com/chrisgleissner/jutil/blob/master/table/src/main/java/com/github/chrisgleissner/jutil/table/Table.java) interface
can be printed and various adapters for this interface are available:

<a href="https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">DB ResultSet</a>:
```java
Class.forName("org.h2.Driver");
Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
String s = DefaultTablePrinter.print(
        new ResultSetTable(conn.createStatement().executeQuery("select * from foo"))));
```

<a href="https://www.univocity.com/pages/about-parsers">Univocity CSV Parser</a> 
```java
CsvParserSettings settings = new CsvParserSettings();
settings.setHeaderExtractionEnabled(true);
CsvParser parser = new CsvParser(settings);
String s = DefaultTablePrinter.print(
        UnivocityTable.of(parser.iterateRecords(new File("sample.csv"))));
```

<a href="https://commons.apache.org/proper/commons-csv/">Apache Commons CSV Parser</a>
```java
String s = DefaultTablePrinter.print(
    new ApacheCsvTable(CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(
        new FileReader(new File("sample.csv")))))
                
```
