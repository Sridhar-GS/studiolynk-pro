# Portfolio and S3 Specification

## Portfolio
One freelancer -> one portfolio.

Portfolio:
- categories
- images
- image order

CRUD:
- create category
- edit category
- delete category
- upload image
- edit image metadata if used
- delete image
- reorder images

## S3 layout
Suggested:
- `studio-profiles/{studioId}/...`
- `freelancer-profiles/{freelancerId}/...`
- `portfolio/{freelancerId}/{categoryId}/...`

## Upload
Frontend should send a validated upload through backend or a controlled upload mechanism. For the prototype, backend-mediated upload is acceptable and simpler.

Validate:
- MIME type
- extension
- reasonable content size
- ownership

## Database
Store:
- object key
- URL
- original filename if needed
- content type
- size if needed
- order
- category relation

## Public prototype
S3 objects may be public for the college prototype. Keep bucket credentials private.

## UI
Portfolio categories use image grids.
Edit mode supports drag-and-drop ordering.
