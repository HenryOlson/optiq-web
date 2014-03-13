!connect jdbc:optiq:model=target/test-classes/misfortune.json admin admin

select sum("Profits") as "TotalProfits", sum("BadAssets") as "BadAssets" from "Companies";

