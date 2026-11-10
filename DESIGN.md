# BookNest visual system — Academic Catalog

## Design intent

BookNest is a personal book catalog. The interface should feel like a clear academic index: quiet, orderly, typographic, and useful for finding and maintaining records. The book data is the main visual content; decorative UI stays secondary.

## Tokens

- **Archive ink** `#1D2B33`: headings, primary text, and wordmark.
- **Catalog blue** `#28546B`: primary actions, links, and active navigation.
- **Index green** `#567565`: availability and supporting status.
- **Cool paper** `#F3F5F4`: application canvas and quiet form areas.
- **White** `#FFFFFF`: record sheets and form surfaces.
- **Rule gray** `#D8E0E2`: table rules, dividers, and input borders.

Typography uses Literata for book and page titles, with Source Sans 3 for controls, tables, and body copy. System serif and sans-serif fallbacks keep the interface usable when Google Fonts are unavailable.

## Layout contract

- Use one restrained top navigation with the text wordmark and links to Home, Books, and Categories.
- Keep page headings compact; avoid oversized marketing heroes.
- Use tables and ruled record rows for catalog data; use cards only for bounded tasks such as authentication forms or individual search results.
- Align action buttons, breadcrumbs, tables, forms, notices, and spacing across pages.
- Use square or lightly rounded surfaces, thin rules, minimal shadows, and visible focus states.
- The signature detail is a narrow catalog-blue rule beside book title/author records.
- Keep all text in English and preserve every route, form field, search behavior, ownership rule, and CRUD action.

## Avoided defaults

No gradient hero, repeated icon tiles, emoji as functional icons, generic card dashboard, all-caps monospace chrome, or decorative numbered labels.
