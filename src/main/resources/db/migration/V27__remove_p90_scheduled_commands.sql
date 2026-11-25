DELETE FROM `scheduled_command_failures`
WHERE `command_id` IN (
    SELECT `id` FROM `scheduled_command`
    WHERE LOWER(TRIM(`command`)) = '~p90'
       OR LOWER(TRIM(`command`)) LIKE '~p90 %'
       OR LOWER(TRIM(`command`)) = '~webm'
       OR LOWER(TRIM(`command`)) LIKE '~webm %'
);

DELETE FROM `scheduled_command`
WHERE LOWER(TRIM(`command`)) = '~p90'
   OR LOWER(TRIM(`command`)) LIKE '~p90 %'
   OR LOWER(TRIM(`command`)) = '~webm'
   OR LOWER(TRIM(`command`)) LIKE '~webm %';
