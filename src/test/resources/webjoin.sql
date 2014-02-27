!connect jdbc:optiq:model=target/test-classes/wiki.json admin admin

select c."Rank", c."City", c."State", c."Population" "City Population", s."Population" "State Population", (100 * c."Population" / s."Population") "Pct State Population" from "Cities" c, "States" s where c."State" = s."State" and s."State" = 'California';

select count(*) "City Count", sum(100 * c."Population" / s."Population") "Pct State Population" from "Cities" c, "States" s where c."State" = s."State" and s."State" = 'California';
