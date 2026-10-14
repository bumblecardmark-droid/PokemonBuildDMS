# Module 7 demonstration guide

Adriel Portillo Perez - Pokemon Build Management System

Record your face and your screen together. Use the IntelliJ console or `run.bat`,
and enlarge the console text so the menu and results are readable. Aim for about
4-6 minutes. Show the real program running and pause briefly on each result.

Start with a fresh run. Follow these steps in rubric order.

## 1. Menu and validation

Say: "My name is Adriel Portillo Perez. This is Phase 1 of my Pokemon Build
Management System. It uses a console menu and an ArrayList to manage build records."

Type `hello`, then `99`, then a blank line at the menu. Show that each is rejected.

Say: "The menu rejects invalid choices and lets the user try again. Zero exits."

## 2. File input

Choose `1`. Enter a blank line and `missing.txt`, then enter `data.txt`.
Show the message **20 added, 0 skipped**.

Say: "The user supplies a file location. Blank and missing locations are handled.
This file has twenty different sample records."

## 3. Display

Choose `2` and show the records. Scroll through the table if necessary.

Say: "Each build contains an ID, name, two type fields, role, base stat total,
usage percentage, selected status, and updated date. All fields are displayed."

## 4. Create a record

Choose `3`. Enter these responses as the prompts appear:

| Prompt | Responses to type in order |
| --- | --- |
| Build ID | `abc`, `101`, then `121` |
| Species | blank line, then `Lucario` |
| Primary type | `Laser`, then `Fighting` |
| Secondary type | `Fighting`, then `Steel` |
| Role | `Sweeper` |
| Base stat total | `-50`, then `525` |
| Usage rate | `NaN`, then `18.2` |
| Selected for team | `maybe`, then `false` |
| Last updated | `2026-02-30`, then `2026-10-13` |

Show the printed Lucario record. Choose `2` and show **21 records**.

Say: "The program rejects a duplicate ID, missing text, invalid types, invalid
numbers, an unsupported boolean, and an impossible date. It stores the record
only after all fields are valid."

## 5. Remove a record

Choose `4`, enter `999`, and show the not-found message. Choose `4` again,
enter `121`, review Lucario, and enter `yes`. Choose `2` and show **20 records**.

Say: "Removing a missing record is handled. A real deletion requires confirmation,
and Lucario no longer appears when the records are displayed."

## 6. Update a record

Choose `5` and enter `102`. Show the menu listing all nine editable fields.
Choose field `7` for usage rate. Enter `150`, then `30`.
Show Rotom-Heat's updated usage rate and choose `2` to show the stored change.

Say: "Every field can be changed, including the ID and date. Here I update Rotom-Heat's
usage rate. The invalid percentage is rejected, and the valid replacement is saved."

For an additional quick ID example, choose `5`, enter `120`, choose field `1`,
enter `101` to show duplicate rejection, then enter `220`.

## 7. Custom calculation

Choose `6`. With the Rotom-Heat edit above, the score is **64.79/100**.
Without that edit, the original file produces **64.45/100**.

Say: "The Team Readiness Score uses selected builds only. It combines average base
stats, represented types, different roles, and average usage. The output explains
each part of the calculation."

Optional team-size example: choose `5`, ID `106`, field `8`, and `true` to fill the
sixth slot. Then choose `5`, ID `107`, field `8`, and `true`. Show that a seventh
selection is rejected. Enter `false` to finish that edit safely.

Choose `0` to exit.

Say: "The application returns to the menu after each action, and the user chooses
when to exit."

## Submit

Upload the recording to your chosen cloud-storage service and enable view access
for the instructor. Submit its link, rather than uploading the video file to
Canvas. Also submit the project ZIP, executable JAR, sample text file, and actual
GitHub repository URL. Open your submitted files and test the video link afterward.

The assignment slides require all five deliverables and a visible face in the
demonstration. A script alone does not satisfy the demonstration requirement.
