/*
// Licensed to Henry Olson under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Henry Olson licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package net.hydromatic.optiq.impl.web;

import net.hydromatic.linq4j.function.Function1;

import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.sql.*;
import java.util.Properties;

/**
 * System test of the Optiq adapter for web tables.
 */
public class SQLTest {

  // tests

  /**
   * Reads from a local file and checks the result
   */
  @Test
  public void testFileSelect() throws SQLException {
    checkSql("testModel", "select H1 from T1 where H0 = 'R1C0'", "H1=R1C1\n");
  }

  /**
   * Reads from a local file without table headers <TH> and checks the result
   */
  @Test
  public void testNoTHSelect() throws SQLException {
    Assume.assumeTrue(AllTests.hazNetwork());
    checkSql("testModel", "select \"col1\" from T1_NO_TH where \"col0\" like 'R0%'",
        "col1=R0C1\n");
  }

  /**
   * Reads from a local file - finds larger table even without <TH> elements
   */
  @Test
  public void testFindBiggerNoTH() throws SQLException {
    checkSql("testModel", "select \"col4\" from TABLEX2 where \"col0\" like 'R1%'",
        "col4=R1C4\n");
  }

  /**
   * Reads from a URL and checks the result
   */
  @Test
  public void testURLSelect() throws SQLException {
    Assume.assumeTrue(AllTests.hazNetwork());
    checkSql("wiki", "select \"State\", \"Statehood\" from \"States\" where \"State\" = 'California'",
        "State=California; Statehood=1850-09-09\n");
  }

  /**
   * Reads from a URL and checks the result - tests replace attribute
   */
  @Test
  public void testReplace() throws SQLException {
    Assume.assumeTrue(AllTests.hazNetwork());
    checkSql("fortune", "select \"Company\" from \"Companies\" order by \"Market Value\" desc limit 1",
        "Company=Apple\n");
  }

  // helper functions

  private void checkSql(String model, String sql) throws SQLException {
    checkSql(sql, model, new Function1<ResultSet, Void>() {
      public Void apply(ResultSet resultSet) {
        try {
          output(resultSet, System.out);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    });
  }

  private void checkSql(String model, String sql, final String expected)
    throws SQLException {
    checkSql(sql, model, new Function1<ResultSet, Void>() {
      public Void apply(ResultSet resultSet) {
        try {
          String actual = SQLTest.toString(resultSet);
          if (!expected.equals(actual)) {
                System.out.println("Assertion failure:");
                System.out.println("\tExpected: '" + expected + "'");
                System.out.println("\tActual: '" + actual + "'");
          }
          assertTrue(expected.equals(actual));
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    });
  }

  private void checkSql(String sql, String model, Function1<ResultSet, Void> fn)
    throws SQLException {
    Connection connection = null;
    Statement statement = null;
    try {
      Properties info = new Properties();
      info.put("model", "target/test-classes/" + model + ".json");
      connection = DriverManager.getConnection("jdbc:optiq:", info);
      statement = connection.createStatement();
      final ResultSet resultSet =
          statement.executeQuery(
              sql);
      fn.apply(resultSet);
    } finally {
      close(connection, statement);
    }
  }

  private static String toString(ResultSet resultSet) throws SQLException {
    StringBuilder buf = new StringBuilder();
    while (resultSet.next()) {
      int n = resultSet.getMetaData().getColumnCount();
      String sep = "";
      for (int i = 1; i <= n; i++) {
        buf.append(sep)
            .append(resultSet.getMetaData().getColumnLabel(i))
            .append("=")
            .append(resultSet.getObject(i));
        sep = "; ";
      }
      buf.append("\n");
    }
    return buf.toString();
  }

  private void output(ResultSet resultSet, PrintStream out)
    throws SQLException {
    final ResultSetMetaData metaData = resultSet.getMetaData();
    final int columnCount = metaData.getColumnCount();
    while (resultSet.next()) {
      for (int i = 1;; i++) {
        out.print(resultSet.getString(i));
        if (i < columnCount) {
          out.print(", ");
        } else {
          out.println();
          break;
        }
      }
    }
  }

  private void close(Connection connection, Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        // ignore
      }
    }
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        // ignore
      }
    }
  }

}

// End SQLTest.java
