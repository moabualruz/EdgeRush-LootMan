# Interim Dashboard Sharing

This folder provides a seed dataset and checklist for publishing the FLPS dashboard until the automated service is live.

## Files
- `mock_flps.csv` – Exported from `docs/data/mock_flps_output.json`; load into a Google Sheet or Notion database to populate the `FLPS Rankings` tab described in `docs/dashboard-prototype.md`.

## Import Steps (Google Sheets)
1. Create a new Google Sheet named `EdgeRush FLPS Dashboard`.
2. Rename the first tab to `FLPS Rankings` and import `mock_flps.csv` (File → Import → Upload).
3. Add computed columns for `RMS`, `IPI`, and `FLPS` using the formulas provided in `docs/dashboard-prototype.md` (the CSV already includes values for verification).
4. Add a second tab `RDF Tracker` and manually enter RDF cooldown data using the schema in the prototype doc.
5. Protect formula cells and share the sheet as view-only with raiders; grant edit access to loot council officers.

## Notion Alternative
- Create a database with the same columns as the CSV.
- Paste the rows via clipboard and add formula properties mirroring the Google Sheets formulas.

## Distribution Checklist
- [ ] Upload Sheet/Notion link to Discord `#loot-announcements` (pin message for visibility).
- [ ] Update `PROJECT_PROGRESS.md` once the live link is shared.
- [ ] Schedule weekly refresh reminders until automation drops CSV exports automatically.
