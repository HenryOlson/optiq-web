!connect jdbc:optiq:model=src/test/resources/fortune.json admin admin

select * from "Companies" limit 10;
