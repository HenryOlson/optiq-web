# Optiq-web release history

For a full list of releases, see <a href="https://github.com/HenryOlson/optiq-web/releases">github</a>.

## <a href="https://github.com/HenryOlson/optiq-web/releases/tag/optiq-web-0.2">0.2</a> / 2014-03-17

* Allow multiple optiq columns to be derived from one HTML column - e.g. Location -> Lat, Lon
* Improved pattern match - added matchSeq to allow selection of n-th match
* Sync with optiq-0.5 - eliminate equijoin warning messages (among other things)
* Renamed and rationalized configuration fields
* Added replace patterns to cell parsing logic
* Refactored and decrufted WebEnumerator, WebReader
* Add handling for tables without <TH> elements
* Improved unit tests - add tests using local files, make URL tests contingent on network access
* Add ability to parse HTML tables in local files

## <a href="https://github.com/HenryOlson/optiq-web/releases/tag/optiq-web-0.1">0.1</a> / 2014-02-28

Initial Release

Basic feature set for querying HTML tables with SQL.  Tables may be easily defined, then refined for better parsing and querying.
Derived from an example (<a href="https://github.com/julianhyde/optiq-csv">optiq-csv</a>) by @julianhyde

* Update release history
* Built against optiq-0.4.18, linq4j-0.1.13, sqlline-1.1.7.
* Configure pom.xml for initial release
* Improve instantiation to avoid unnecessary URL reads
* Implement algorithm to find the "biggest" table (e.g. max(th*tr)) where a selector is not specified
* Add flexible date / time parsing with natty
* Rename and redact other Csv*.java from example
* Implement WebTable, WebEnumerator, RowParser to integrate with optiq
* Implement WebReader using Jsoup to get table from URL
